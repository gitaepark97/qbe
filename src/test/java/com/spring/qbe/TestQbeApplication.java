package com.spring.qbe;

import org.springframework.boot.SpringApplication;

public class TestQbeApplication {

    public static void main(String[] args) {
        SpringApplication.from(QbeApplication::main)
                         .with(TestcontainersConfiguration.class)
                         .run(args);
    }

}
