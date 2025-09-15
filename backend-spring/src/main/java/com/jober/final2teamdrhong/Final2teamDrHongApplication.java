package com.jober.final2teamdrhong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Final2teamDrHongApplication {

    public static void main(String[] args) {
        SpringApplication.run(Final2teamDrHongApplication.class, args);
    }

}
