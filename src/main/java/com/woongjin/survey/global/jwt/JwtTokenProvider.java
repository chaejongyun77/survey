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
 *
 * [설계 원칙]
 * - getClaims()는 public으로 노출. 파싱 실패 시 JwtAuthException을 던짐.
 * - validateToken()은 제거. 호출부에서 getClaims()를 직접 호출하거나 try-catch로 처리.
 *   → boolean 반환으로 에러 정보를 숨기던 방식 대신, 예외를 통해 구체적인 실패 이유를 전달.
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
     * 토큰에서 Claims 파싱 (검증 포함)
     * - 만료: JwtAuthException(TOKEN_EXPIRED)
     * - 위변조/형식오류: JwtAuthException(TOKEN_INVALID)
     *
     * 이 메서드가 정상 반환되면 토큰은 유효함이 보장됨.
     * 호출 측에서 별도 validateToken() 없이 이 메서드 하나로 검증 + 파싱을 동시에 처리.
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰 만료: {}", e.getMessage());
            throw new JwtAuthException(JwtErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            log.warn("JWT 토큰 검증 실패: {}", e.getMessage());
            throw new JwtAuthException(JwtErrorCode.TOKEN_INVALID);
        }
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

        UserPrincipal userPrincipal = new UserPrincipal(
                memberId,
                loginId,
                "",  // 비밀번호는 토큰 인증 시 불필요
                "",  // 이름은 필요 시 DB 조회
                List.of(new SimpleGrantedAuthority(role))
        );

        return new UsernamePasswordAuthenticationToken(userPrincipal, token, userPrincipal.getAuthorities());
    }

}
