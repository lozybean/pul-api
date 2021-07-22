package me.lyon.pul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
public class PulApplication {
    public static void main(String[] args) {
        SpringApplication.run(PulApplication.class);
    }
}
