package com.test.phone.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.test.phone"})
public class PhoneGetApplication {
  public static void main(String[] args) {
    SpringApplication.run(PhoneGetApplication.class, args);
  }
}

