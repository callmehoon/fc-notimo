package com.jober.final2teamdrhong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.jober.final2teamdrhong.config.AuthProperties;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(AuthProperties.class)
public class Final2teamDrHongApplication {

    public static void main(String[] args) {
        SpringApplication.run(Final2teamDrHongApplication.class, args);
    }

}
