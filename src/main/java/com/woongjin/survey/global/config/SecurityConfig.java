package com.woongjin.survey.global.config;

import com.woongjin.survey.domain.member.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * - 폼 로그인 기반 인증
 * - BCrypt 비밀번호 암호화
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .disable()
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // H2 콘솔 iframe 허용
            )
            .userDetailsService(customUserDetailsService)
            .authorizeHttpRequests(auth -> auth
                // 로그인 페이지, 정적 리소스는 인증 없이 접근 허용
                .requestMatchers("/auth/login", "/h2-console/**", "/css/**", "/js/**", "/images/**").permitAll()
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")           // 커스텀 로그인 페이지
                .loginProcessingUrl("/auth/login")  // 폼 submit URL (Security가 처리)
                .defaultSuccessUrl("/surveys", true) // 로그인 성공 후 이동
                .failureUrl("/auth/login?error=true") // 로그인 실패 후 이동
                .usernameParameter("loginId")        // form input name
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );
        return http.build();
    }
}
