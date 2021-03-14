package com.telstra.phone.controller.impl;

import static com.telstra.phone.util.Util.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.telstra.phone.controller.IPhoneController;
import com.telstra.phone.dto.PhoneActivateDto;
import com.telstra.phone.dto.SearchDto;
import com.telstra.phone.service.IPhoneService;
import com.telstra.phone.util.Util;

@RestController
@RequestMapping({ "/api/v1" })
public final class PhoneController implements IPhoneController {

	@Value("${search.max_limit}")
	private int SRCH_MAX_LIMIT = 15;

	private static final String DEFAULT_LIMIT = "10";
	private static final String DEFAULT_SORT = "number";
	private static final String DEFAULT_DIR = "ASC";
	private static final String DEFAULT_START = "0";

	private final IPhoneService phoneService;

	@Autowired
	public PhoneController(final IPhoneService phoneService) {
		this.phoneService = phoneService;
	}

	@Override
	@GetMapping(value = { "/phones" }, produces = { APPLICATION_JSON_VALUE })
	public final ResponseEntity<Collection<String>> list(
			@RequestParam(required = false, defaultValue = DEFAULT_START) int start,
			@RequestParam(required = false, defaultValue = DEFAULT_LIMIT) int limit,
			@RequestParam(required = false, defaultValue = DEFAULT_SORT) final String sort,
			@RequestParam(required = false, defaultValue = DEFAULT_DIR) final String dir,
			@RequestParam(required = false, defaultValue = "") final String customerId) {
		SearchDto search = Util.getSearchDto(start, limit > SRCH_MAX_LIMIT ? SRCH_MAX_LIMIT : limit, sort, dir);
		if (isEmpty(customerId)) {
			return new ResponseEntity<Collection<String>>(phoneService.list(search), HttpStatus.OK);
		} else {
			Collection<String> phones = phoneService.listByCustomer(search, customerId);
			return new ResponseEntity<Collection<String>>(phones, HttpStatus.OK);
		}
	}

	@Override
	@PutMapping(value = { "/phones/{phoneNumber}/activate" }, consumes = { APPLICATION_JSON_VALUE })
	public final ResponseEntity<?> activate(@PathVariable("phoneNumber") final String phoneNumber,
			@RequestBody final PhoneActivateDto activateDto) {
		phoneService.activate(phoneNumber, activateDto);
		return ResponseEntity.noContent().build();
	}
}
