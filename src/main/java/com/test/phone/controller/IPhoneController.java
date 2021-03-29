package com.test.phone.controller;

import java.util.Collection;

import org.springframework.http.ResponseEntity;

import com.test.phone.dto.PhoneActivateDto;

public interface IPhoneController {
	ResponseEntity<Collection<String>> list(int start, int limit, final String sort, final String dir, final String customerId);

	ResponseEntity<?> activate(final String phoneNumber, final PhoneActivateDto activateDto);

}
