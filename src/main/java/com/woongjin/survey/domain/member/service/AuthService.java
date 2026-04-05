package com.woongjin.survey.domain.member.service;

import com.woongjin.survey.domain.member.dto.TokenResponse;
import com.woongjin.survey.domain.member.entity.Member;
import com.woongjin.survey.domain.member.repository.MemberMapper;
import com.woongjin.survey.global.auth.LoginMember;
import com.woongjin.survey.global.jwt.JwtProperties;
import com.woongjin.survey.global.jwt.JwtTokenProvider;
import com.woongjin.survey.global.redis.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

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
    private final MemberMapper memberMapper;

    /**
     * 로그인
     *
     * 흐름:
     * 1) AuthenticationManager가 CustomUserDetailsService를 호출해서 DB 조회
     * 2) 비밀번호 검증 (BCrypt)
     * 3) 검증 성공 → LoginMember 객체 획득
     * 4) Access Token + Refresh Token 생성
     * 5) Redis에 Refresh Token 저장
     *
     * @param loginId  사용자 로그인 ID
     * @param password 사용자 비밀번호 (평문)
     * @return Access Token + Refresh Token
     */
    public TokenResponse login(String loginId, String password) {
        // 1~2) ID/PW 검증 (내부에서 CustomUserDetailsService.loadUserByUsername 호출)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginId, password)
        );

        // 3) 검증 성공 → LoginMember 꺼내기
        LoginMember loginMember = (LoginMember) authentication.getPrincipal();
        Long memberId = loginMember.getMemberId();
        String role = loginMember.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        // 4) 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(memberId, loginId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);

        // 5) Redis에 Refresh Token 저장
        redisTokenRepository.saveRefreshToken(
                memberId, refreshToken, jwtProperties.getRefreshTokenExpiration()
        );

        log.info("로그인 성공: memberId={}, loginId={}", memberId, loginId);
        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 로그아웃
     *
     * 흐름:
     * 1) Redis에서 Refresh Token 삭제
     * 2) 쿠키 삭제는 AuthController에서 처리
     *
     * @param memberId 로그아웃할 회원 PK
     */
    public void logout(Long memberId) {
        redisTokenRepository.deleteRefreshToken(memberId);
        log.info("로그아웃: memberId={}", memberId);
    }

    /**
     * 토큰 재발급 (Refresh Token Rotation)
     *
     * 흐름:
     * 1) Refresh Token 유효성 검증
     * 2) Redis에 저장된 값과 비교
     * 3) DB에서 최신 사용자 정보 조회
     * 4) 새 Access Token + 새 Refresh Token 발급
     * 5) Redis에 새 Refresh Token 저장 (기존 것 교체)
     *
     * @param refreshToken 클라이언트가 보낸 Refresh Token
     * @return 새 Access Token + 새 Refresh Token
     * @throws IllegalArgumentException 토큰이 유효하지 않거나 Redis 값과 불일치 시
     */
    public TokenResponse reissue(String refreshToken) {
        // 1) Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 2) Redis에 저장된 값과 비교
        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        String savedToken = redisTokenRepository.getRefreshToken(memberId);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            // 탈취 의심 → 저장된 Refresh Token도 삭제
            redisTokenRepository.deleteRefreshToken(memberId);
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다. 다시 로그인해주세요.");
        }

        // 3) DB에서 최신 사용자 정보 조회 (권한 변경 반영)
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 4) 새 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                memberId, member.getLoginId(), member.getRole()
        );
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId);

        // 5) Redis에 새 Refresh Token 저장 (Rotation)
        redisTokenRepository.saveRefreshToken(
                memberId, newRefreshToken, jwtProperties.getRefreshTokenExpiration()
        );

        log.info("토큰 재발급 성공: memberId={}", memberId);
        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
