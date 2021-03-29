package com.test.phone.service;

import java.util.Collection;

import com.test.phone.dto.PhoneActivateDto;
import com.test.phone.dto.SortDto;
import com.test.phone.exception.DuplicateRequestException;

public interface IPhoneService {
	Collection<String> list(final SortDto sortDto);

	Collection<String> listByCustomer(final SortDto sortDto, final String customerId);

	void activate(final String phoneNumber, final PhoneActivateDto activateDto) throws DuplicateRequestException;
}
