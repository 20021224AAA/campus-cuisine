package com.CampusCuisine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.CampusCuisine.mapper")
@SpringBootApplication
public class CampusCuisineApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusCuisineApplication.class, args);
    }

}

