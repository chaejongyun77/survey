package com.woongjin.survey.domain.auth.service;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeStatus;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.jwt.JwtErrorCode;
import com.woongjin.survey.global.jwt.JwtProperties;
import com.woongjin.survey.global.jwt.JwtTokenProvider;
import com.woongjin.survey.domain.auth.infra.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직
 * - 로그인: ID/PW 검증 → 토큰 발급 → Redis에 Refresh Token 저장
 * - 로그아웃: Redis에서 Refresh Token 삭제
 * - 토큰 재발급: Refresh Token 검증 → 새 토큰 발급 (Rotation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenRepository redisTokenRepository;
    private final JwtProperties jwtProperties;
    private final EmployeeRepository employeeRepository;

    /**
     * 로그인
     *
     * 흐름:
     * 1) AuthenticationManager가 CustomUserDetailsService를 호출해서 DB 조회
     * 2) 비밀번호 검증 (BCrypt)
     * 3) 검증 성공 → UserPrincipal 객체 획득
     * 4) Access Token + Refresh Token 생성
     * 5) Redis에 Refresh Token 저장
     *
     * @param empNo    사번 (로그인 ID)
     * @param password 비밀번호 (평문)
     * @return Access Token + Refresh Token
     */
    public TokenResponse login(String empNo, String password) {
        // 1~2) ID/PW 검증 (내부에서 CustomUserDetailsService.loadUserByUsername 호출)
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(empNo, password));

        // 3) 검증 성공 → UserPrincipal 꺼내기
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long empId = principal.getEmpId();
        String empName = principal.getEmpName();
        String role = principal.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        // 4) 토큰 생성
        String accessToken  = jwtTokenProvider.generateAccessToken(empId, empNo, role, empName);
        String refreshToken = jwtTokenProvider.generateRefreshToken(empId);

        // 5) Redis에 Refresh Token 저장
        redisTokenRepository.saveRefreshToken(empId, refreshToken, jwtProperties.getRefreshTokenExpiration());

        log.info("로그인 성공: empId={}, empNo={}", empId, empNo);
        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     *
     * 흐름:
     * 1) Redis에서 Refresh Token 삭제
     * 2) 쿠키 삭제는 AuthController에서 처리
     *
     * @param empId 로그아웃할 직원 PK
     */
    public void logout(Long empId) {
        redisTokenRepository.deleteRefreshToken(empId);
        log.info("로그아웃: empId={}", empId);
    }

    /**
     * 토큰 재발급 (Refresh Token Rotation)
     *
     * 흐름:
     * 1) getClaims()로 Refresh Token 검증 (만료/위변조 시 JwtAuthException 자동 throw)
     * 2) Redis에 저장된 값과 비교
     * 3) DB에서 최신 직원 정보 조회 (권한 변경 반영)
     * 4) 새 Access Token + 새 Refresh Token 발급
     * 5) Redis에 새 Refresh Token 저장 (기존 것 교체)
     *
     */
    @Transactional(readOnly = true)
    public TokenResponse reissue(String refreshToken) {
        // 1) 검증 + empId 추출 (실패 시 JwtAuthException throw → GlobalExceptionHandler 처리)
        Long empId = Long.valueOf(jwtTokenProvider.getClaims(refreshToken).getSubject());

        // 2) Redis에 저장된 값과 비교
        String savedToken = redisTokenRepository.getRefreshToken(empId);

        if (savedToken == null) {
            throw new JwtAuthException(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (!savedToken.equals(refreshToken)) {
            // 탈취 의심 → 저장된 Refresh Token도 삭제
            redisTokenRepository.deleteRefreshToken(empId);
            throw new JwtAuthException(JwtErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 3) DB에서 최신 직원 정보 조회
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 아이디입니다."));

        // 4) 새 토큰 생성
        String newAccessToken  = jwtTokenProvider.generateAccessToken(
                empId, employee.getEmpNo(), employee.getRole().name(),employee.getEmpName()
        );
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(empId);

        // 5) Redis에 새 Refresh Token 저장 (Rotation)
        redisTokenRepository.saveRefreshToken(
                empId, newRefreshToken, jwtProperties.getRefreshTokenExpiration()
        );

        log.info("토큰 재발급 성공: empId={}", empId);
        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
