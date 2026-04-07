package com.woongjin.survey.global.jwt;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰 생성 / 검증 / 파싱
 *
 * - Access Token: 사용자 인증용 (짧은 만료)
 * - Refresh Token: Access Token 재발급용 (긴 만료, Redis에 저장)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     * - Claims: memberId, loginId, role
     */
    public String generateAccessToken(Long memberId, String loginId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("loginId", loginId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     * - 최소한의 정보만 포함 (memberId)
     */
    public String generateRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 검증
     * - 서명 위변조 확인 + 만료 여부 확인
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰 만료: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT 토큰 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 Authentication 객체 생성
     * - SecurityContextHolder에 세팅할 인증 정보
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        Long memberId = Long.valueOf(claims.getSubject());
        String loginId = claims.get("loginId", String.class);
        String role = claims.get("role", String.class);

        UserPrincipal UserPrincipal = new UserPrincipal(
                memberId,
                loginId,
                "",  // 비밀번호는 토큰 인증 시 불필요
                "",  // 이름은 필요 시 DB 조회
                List.of(new SimpleGrantedAuthority(role))
        );

        return new UsernamePasswordAuthenticationToken(UserPrincipal, token, UserPrincipal.getAuthorities());
    }

    /**
     * 토큰에서 memberId 추출
     */
    public Long getMemberId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    /**
     * 토큰 남은 만료시간 (ms)
     * - 로그아웃 시 Access Token 블랙리스트 TTL 설정에 사용
     */
    public long getRemainingExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 토큰에서 Claims 파싱
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
