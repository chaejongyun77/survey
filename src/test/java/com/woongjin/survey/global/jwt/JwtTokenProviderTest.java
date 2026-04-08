package com.woongjin.survey.global.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

/**
 * JwtTokenProvider 단위 테스트
 */
@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString("test-secret-key-for-jwt-unit-test".getBytes());

    private static final long ACCESS_EXP = 1000L * 60 * 30;        // 30분
    private static final long REFRESH_EXP = 1000L * 60 * 60 * 24 * 7; // 7일

    private static final Long TEST_MEMBER_ID = 1L;
    private static final String TEST_LOGIN_ID = "testUser";
    private static final String TEST_ROLE = "ROLE_USER";
    private static final String TEST_NAME = "채종윤";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(TEST_SECRET);
        props.setAccessTokenExpiration(ACCESS_EXP);
        props.setRefreshTokenExpiration(REFRESH_EXP);

        jwtTokenProvider = new JwtTokenProvider(props);
        jwtTokenProvider.init();
    }

    @Nested
    @DisplayName("generateAccessToken()")
    class GenerateAccessToken {
        @Test
        @DisplayName("엑세스 토큰 정상 생성")
        void 정상_JWT_형식() {
            String token = jwtTokenProvider.generateAccessToken(TEST_MEMBER_ID, TEST_LOGIN_ID, TEST_ROLE,TEST_NAME);

            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

    }

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshToken {
        @Test
        @DisplayName("리프래시 토큰 정상 생성")
        void 정상_JWT_형식() {
            String token = jwtTokenProvider.generateRefreshToken(TEST_MEMBER_ID);

            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("만료 시간 - 엑세스토큰 보다 만료 시간이 길어야 함")
        void 만료시간_accessToken보다_김() {
            String accessToken = jwtTokenProvider.generateAccessToken(TEST_MEMBER_ID, TEST_LOGIN_ID, TEST_ROLE,TEST_NAME);
            String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_MEMBER_ID);

            Claims accessClaims = jwtTokenProvider.getClaims(accessToken);
            Claims refreshClaims = jwtTokenProvider.getClaims(refreshToken);

            assertThat(refreshClaims.getExpiration()).isAfter(accessClaims.getExpiration());
        }
    }

    @Nested
    @DisplayName("getClaims()")
    class GetClaims {

        @Test
        @DisplayName("유효한 토큰 - Claims를 정상 반환")
        void 유효한_토큰_정상_반환() {
            String token = jwtTokenProvider.generateAccessToken(TEST_MEMBER_ID, TEST_LOGIN_ID, TEST_ROLE,TEST_NAME);

            assertThatCode(() -> jwtTokenProvider.getClaims(token))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("만료된 토큰 - TOKEN_EXPIRED 예외")
        void 만료_토큰_TOKEN_EXPIRED() {
            String expiredToken = buildExpiredToken();

            assertThatThrownBy(() -> jwtTokenProvider.getClaims(expiredToken))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.TOKEN_EXPIRED));
        }

        @Test
        @DisplayName("위변조된 토큰 - TOKEN_INVALID 예외")
        void 위변조_토큰_TOKEN_INVALID() {
            String token = jwtTokenProvider.generateAccessToken(TEST_MEMBER_ID, TEST_LOGIN_ID, TEST_ROLE,TEST_NAME);
            String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

            assertThatThrownBy(() -> jwtTokenProvider.getClaims(tamperedToken))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("잘못된 형식 문자열 - TOKEN_INVALID 예외")
        void 잘못된_형식_TOKEN_INVALID() {
            assertThatThrownBy(() -> jwtTokenProvider.getClaims("this.is.not.a.jwt"))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("다른 키로 서명된 토큰 - TOKEN_INVALID 예외")
        void 다른_키_서명_TOKEN_INVALID() {
            // 다른 시크릿으로 생성한 토큰
            String otherSecret = Base64.getEncoder().encodeToString("other-secret-key-for-jwt-unit-test".getBytes());
            JwtProperties otherProps = new JwtProperties();
            otherProps.setSecret(otherSecret);
            otherProps.setAccessTokenExpiration(ACCESS_EXP);
            otherProps.setRefreshTokenExpiration(REFRESH_EXP);

            JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);
            otherProvider.init();

            String tokenFromOtherKey = otherProvider.generateAccessToken(TEST_MEMBER_ID, TEST_LOGIN_ID, TEST_ROLE,TEST_NAME);

            assertThatThrownBy(() -> jwtTokenProvider.getClaims(tokenFromOtherKey))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.TOKEN_INVALID));
        }
    }

    @Nested
    @DisplayName("getAuthentication()")
    class GetAuthentication {

        @Test
        @DisplayName("유효한 토큰 - UsernamePasswordAuthenticationToken을 반환")
        void 유효한_토큰_Authentication_반환() {
            String token = jwtTokenProvider.generateAccessToken(TEST_MEMBER_ID, TEST_LOGIN_ID, TEST_ROLE,TEST_NAME);
            Authentication auth = jwtTokenProvider.getAuthentication(token);

            assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        }

        @Test
        @DisplayName("만료된 토큰 - 예외 전파")
        void 만료토큰_예외_전파() {
            String expiredToken = buildExpiredToken();

            assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(expiredToken))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.TOKEN_EXPIRED));
        }
    }

    private String buildExpiredToken() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret(TEST_SECRET);
        expiredProps.setAccessTokenExpiration(-1000L); // 1초 전 만료
        expiredProps.setRefreshTokenExpiration(REFRESH_EXP);

        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
        expiredProvider.init();

        return expiredProvider.generateAccessToken(TEST_MEMBER_ID, TEST_LOGIN_ID, TEST_ROLE,TEST_NAME);
    }
}
