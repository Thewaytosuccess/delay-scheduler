package com.ph.timer.delay.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author lenovo
 */
@SpringBootApplication
@ComponentScan("com.ph.timer.delay.redis")
public class RedissonApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedissonApplication.class,args);
    }
}
