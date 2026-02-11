package org.codeup.statiocore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StatiocoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatiocoreApplication.class, args);
    }
}
