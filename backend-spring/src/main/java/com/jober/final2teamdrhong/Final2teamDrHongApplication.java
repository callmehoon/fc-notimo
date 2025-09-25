package com.jober.final2teamdrhong;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.jober.final2teamdrhong.config.AuthProperties;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(AuthProperties.class)
public class Final2teamDrHongApplication {

    /**
     * 애플리케이션의 기본 시간대를 'Asia/Seoul'로 설정합니다.
     * 이 설정은 @Query의 CURRENT_TIMESTAMP를 포함한 모든 시간 관련 동작에 영향을 미칩니다.
     */
    @PostConstruct
    public void setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Final2teamDrHongApplication.class, args);
    }

}
