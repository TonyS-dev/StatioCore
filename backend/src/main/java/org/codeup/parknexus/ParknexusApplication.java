package org.codeup.parknexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ParknexusApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParknexusApplication.class, args);
    }
}
