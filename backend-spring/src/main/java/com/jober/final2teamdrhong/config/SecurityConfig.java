package com.jober.final2teamdrhong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 기능을 비활성화합니다. (API 서버는 보통 세션을 사용하지 않으므로 비활성화가 안전합니다)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 모든 요청에 대해 인증을 요구하도록 설정합니다.
                        .anyRequest().authenticated()
                )
                // HTTP Basic 인증을 활성화합니다.
                .httpBasic(withDefaults());

        return http.build();
    }
}
