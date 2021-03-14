package com.telstra.phone.service;

import java.util.Collection;

import com.telstra.phone.dto.PhoneActivateDto;
import com.telstra.phone.dto.SortDto;
import com.telstra.phone.exception.DuplicateRequestException;

public interface IPhoneService {
	Collection<String> list(final SortDto sortDto);

	Collection<String> listByCustomer(final SortDto sortDto, final String customerId);

	void activate(final String phoneNumber, final PhoneActivateDto activateDto) throws DuplicateRequestException;
}
