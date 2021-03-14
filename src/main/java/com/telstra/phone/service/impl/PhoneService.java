package com.telstra.phone.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.telstra.phone.dto.Dir;
import com.telstra.phone.dto.PhoneActivateDto;
import com.telstra.phone.dto.SearchDto;
import com.telstra.phone.exception.BadRequestException;
import com.telstra.phone.exception.DuplicateRequestException;
import com.telstra.phone.exception.ResourceNotFoundException;
import com.telstra.phone.model.Phone;
import com.telstra.phone.repo.PhoneRepo;
import com.telstra.phone.service.IPhoneService;
import com.telstra.phone.util.Util;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Service
public class PhoneService implements IPhoneService {

	private static final String HYPHEN = "-";
	private static final String EMPTY = "";

	private static final String PHONE_REGEX = "^[0-9]{3}[-]?[0-9]{3}[-]?[0-9]{4}$";
	private static final String CUSTOMER_REGEX = "^[A-Za-z0-9]+$";
	private static final String IMEI_REGEX = "^[0-9]{15}$";

	private static final Comparator<Phone> getComp(final SearchDto search) {
		final String sort = Optional.ofNullable(search.getSort()).orElse("number");
		Comparator<Phone> sortCmp = Comparator.comparing(Phone::getNumber);
		switch (sort) {
		case "region":
			sortCmp = Comparator.comparing(Phone::getRegion);
			break;
		case "customerid":
			sortCmp = Comparator.comparing(Phone::getCustomerId);
			break;
		case "activationdate":
			sortCmp = Comparator.comparing(Phone::getActivationDate, Comparator.nullsFirst(Comparator.naturalOrder()));
			break;
		}
		if (search.getDir().equals(Dir.DESC))
			sortCmp = sortCmp.reversed();
		return sortCmp;
	}

	private static final Collection<String> getNumbers(final Collection<Phone> phones) {
		if(null == phones)
			return new ArrayList<>();
		return phones.stream().map(Phone::getNumber).collect(Collectors.toList());
	}

	@Value("${message.exception.phone.regex}")
	private String PHONE_REGEX_EXC;
	@Value("${message.exception.customer.regex}")
	private String CUSTOMER_REGEX_EXC;
	@Value("${message.exception.imei.regex}")
	private String IMEI_REGEX_EXC;
	@Value("${message.exception.data.nf}")
	private String DATA_NF_EXC;
	@Value("${message.exception.dup.activate}")
	private String ACTIVATE_EXC;

	private final PhoneRepo phoneRepo;

	public PhoneService(final PhoneRepo phoneRepo) {
		this.phoneRepo = phoneRepo;
	}

	@Override
	public Collection<String> list(final SearchDto search) {
		log.debug("Search for {}", search);

		Collection<Phone> phones = this.phoneRepo.findAllBy(search, getComp(search));

		Collection<String> numbers = PhoneService.getNumbers(phones);

		log.debug("Retrived recs {}", numbers);
		return numbers;
	}

	@Override
	public Collection<String> listByCustomer(final SearchDto search, final String customerId) {

		if (Util.isEmpty(customerId) || !customerId.matches(CUSTOMER_REGEX))
			throw new BadRequestException(CUSTOMER_REGEX_EXC);

		Optional<Collection<Phone>> phones = phoneRepo.findByCustomer(search, getComp(search), customerId);

		if (!phones.isPresent())
			throw new ResourceNotFoundException(DATA_NF_EXC);

		Collection<String> numbers = PhoneService.getNumbers(phones.get());

		log.debug("Retrieved phones {}", numbers);

		return numbers;
	}

	@Override
	public void activate(final String phoneNumber, final PhoneActivateDto activateDto)
			throws DuplicateRequestException {

		if (Util.isEmpty(phoneNumber) || !phoneNumber.matches(PHONE_REGEX))
			throw new BadRequestException(PHONE_REGEX_EXC);

		if (null == activateDto || Util.isEmpty(activateDto.getImei()) || !activateDto.getImei().matches(IMEI_REGEX))
			throw new BadRequestException(IMEI_REGEX_EXC);

		final String digitNumber = phoneNumber.replaceAll(HYPHEN, EMPTY);

		Optional<Phone> optPhone = phoneRepo.findByPhoneNumber(digitNumber);

		if (!optPhone.isPresent())
			throw new ResourceNotFoundException(DATA_NF_EXC);

		final Phone phone = optPhone.get();

		if (phone.isActivated())
			throw new DuplicateRequestException(ACTIVATE_EXC);

		phone.setActivated(true);
		phone.setImei(activateDto.getImei());
		phone.setActivationDate(LocalDate.now());

		phoneRepo.save(phone);

		log.debug("Activated phone.");
	}
}
