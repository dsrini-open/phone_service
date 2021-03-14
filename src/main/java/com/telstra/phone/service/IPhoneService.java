package com.telstra.phone.service;

import java.util.Collection;

import com.telstra.phone.dto.PhoneActivateDto;
import com.telstra.phone.dto.SearchDto;
import com.telstra.phone.exception.DuplicateRequestException;

public interface IPhoneService {
	Collection<String> list(final SearchDto paramSearchDto);

	Collection<String> listByCustomer(final SearchDto search, final String customerId);

	void activate(final String phoneNumber, final PhoneActivateDto activateDto) throws DuplicateRequestException;
}
