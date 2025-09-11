package com.jober.final2teamdrhong.config;

import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
    
    @Bean
    public LettuceBasedProxyManager<byte[]> lettuceBasedProxyManager() {
        try {
            // Redis URL 동적 생성
            String redisUrl = buildRedisUrl();
            log.info("Redis 연결 시도: host={}, port={}", redisHost, redisPort);
            
            RedisClient redisClient = RedisClient.create(redisUrl);
            
            return LettuceBasedProxyManager.builderFor(redisClient)
                    .build();
        } catch (Exception e) {
            log.error("Redis 연결 실패: host={}, port={}, error={}", redisHost, redisPort, e.getMessage());
            throw new IllegalStateException("Redis 연결에 실패했습니다. Redis 서버 상태를 확인해주세요.", e);
        }
    }
    
    /**
     * application.properties 설정을 기반으로 Redis URL 생성
     */
    private String buildRedisUrl() {
        StringBuilder urlBuilder = new StringBuilder("redis://");
        
        // 비밀번호가 있는 경우 추가
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            urlBuilder.append(":").append(redisPassword).append("@");
        }
        
        urlBuilder.append(redisHost).append(":").append(redisPort);
        
        return urlBuilder.toString();
    }
}