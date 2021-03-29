package com.test.phone.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Phone {
  private final String number;
  private final String region;
  private final String customerId;
    
  private boolean isActivated;
  private LocalDate activationDate;
  private String imei;
  
}

