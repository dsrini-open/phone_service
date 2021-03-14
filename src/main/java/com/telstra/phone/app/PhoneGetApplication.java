package com.telstra.phone.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.telstra.phone"})
public class PhoneGetApplication {
  public static void main(String[] args) {
    SpringApplication.run(PhoneGetApplication.class, args);
  }
}

