package com.aifleetops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aifleetops.mapper")
public class AiFleetOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiFleetOpsApplication.class, args);
    }

}
