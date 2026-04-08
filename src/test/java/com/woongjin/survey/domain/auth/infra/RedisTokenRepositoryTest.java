package com.woongjin.survey.domain.auth.infra;

import com.woongjin.survey.global.redis.RedisConfig;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import static org.assertj.core.api.Assertions.*;

/**
 * RedisTokenRepository 테스트
 */
@DisplayName("RedisTokenRepository 테스트")
class RedisTokenRepositoryTest {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    private static LettuceConnectionFactory connectionFactory;
    private static RedisTemplate<String, String> redisTemplate;
    private static RedisTokenRepository redisTokenRepository;

    // 테스트용 고정 데이터
    private static final Long MEMBER_ID = 9999L;
    private static final String TEST_TOKEN = "test-refresh-token-value";
    private static final long EXPIRY_MS = 1000L * 10;

    @BeforeAll
    static void setUpRedis() {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(REDIS_HOST, REDIS_PORT);
        connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet(); // 직접 초기화

        RedisConfig redisConfig = new RedisConfig();
        redisTemplate = redisConfig.redisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet(); // 직접 초기화

        redisTokenRepository = new RedisTokenRepository(redisTemplate);
    }

    @AfterAll
    static void tearDownRedis() {
        connectionFactory.destroy();
    }

    @AfterEach
    void cleanup() {
        // 각 테스트 후 테스트용 키 정리 (다른 테스트에 영향 없도록)
        redisTemplate.delete("RT:" + MEMBER_ID);
    }

    @Nested
    @DisplayName("saveRefreshToken()")
    class SaveRefreshToken {

        @Test
        @DisplayName("저장 후 조회 - 저장한 값과 일치")
        void 저장_후_조회_일치() {
            redisTokenRepository.saveRefreshToken(MEMBER_ID, TEST_TOKEN, EXPIRY_MS);

            String saved = redisTokenRepository.getRefreshToken(MEMBER_ID);
            assertThat(saved).isEqualTo(TEST_TOKEN);
        }

        @Test
        @DisplayName("RT:{memberId} 형태로 저장")
        void 키_형식_RT_prefix() {
            redisTokenRepository.saveRefreshToken(MEMBER_ID, TEST_TOKEN, EXPIRY_MS);

            Boolean exists = redisTemplate.hasKey("RT:" + MEMBER_ID);
            assertThat(exists).isTrue();
        }

    }

    @Nested
    @DisplayName("getRefreshToken()")
    class GetRefreshToken {

        @Test
        @DisplayName("존재하는 키 - 저장된 토큰을 반환")
        void 존재하는_키_반환() {
            redisTokenRepository.saveRefreshToken(MEMBER_ID, TEST_TOKEN, EXPIRY_MS);

            String result = redisTokenRepository.getRefreshToken(MEMBER_ID);
            assertThat(result).isEqualTo(TEST_TOKEN);
        }

        @Test
        @DisplayName("존재하지 않는 키 - null을 반환")
        void 존재하지_않는_키_null() {
            String result = redisTokenRepository.getRefreshToken(MEMBER_ID);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("만료된 키 - TTL 이후 null을 반환")
        void 만료_후_null() throws InterruptedException {
            long shortExpiry = 500L;
            redisTokenRepository.saveRefreshToken(MEMBER_ID, TEST_TOKEN, shortExpiry);

            Thread.sleep(700L);

            String result = redisTokenRepository.getRefreshToken(MEMBER_ID);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("deleteRefreshToken()")
    class DeleteRefreshToken {

        @Test
        @DisplayName("삭제 후 조회 - null을 반환")
        void 삭제_후_조회_null() {
            redisTokenRepository.saveRefreshToken(MEMBER_ID, TEST_TOKEN, EXPIRY_MS);
            redisTokenRepository.deleteRefreshToken(MEMBER_ID);

            String result = redisTokenRepository.getRefreshToken(MEMBER_ID);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 키 삭제")
        void 존재하지_않는_키_삭제_예외없음() {
            // 저장하지 않고 바로 삭제 시도
            assertThatCode(() -> redisTokenRepository.deleteRefreshToken(MEMBER_ID))
                    .doesNotThrowAnyException();
        }
    }
}
