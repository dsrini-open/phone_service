package com.telstra.phone.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.telstra.phone.dto.Dir;
import com.telstra.phone.dto.PhoneActivateDto;
import com.telstra.phone.dto.SortDto;
import com.telstra.phone.exception.BadRequestException;
import com.telstra.phone.exception.DuplicateRequestException;
import com.telstra.phone.exception.ResourceNotFoundException;
import com.telstra.phone.model.Phone;
import com.telstra.phone.repo.PhoneRepo;
import com.telstra.phone.service.impl.PhoneService;

@SuppressWarnings("unchecked")
public class PhoneServiceTest {

	private String PHONE_REGEX_EXC = "PHONE_REGEX_EXC";
	private String CUSTOMER_REGEX_EXC = "CUSTOMER_REGEX_EXC";
	private String IMEI_REGEX_EXC = "IMEI_REGEX_EXC";
	private String DATA_NF_EXC = "DATA_NF_EXC";
	private String ACTIVATE_EXC = "ACTIVATE_EXC";

	private IPhoneService service;

	@Mock
	private PhoneRepo repo;

	@Captor
	private ArgumentCaptor<SortDto> sortCaptor;

	@Captor
	private ArgumentCaptor<String> stringCaptor;

	@Captor
	private ArgumentCaptor<Comparator<Phone>> compCaptor;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);

		service = new PhoneService(repo);

		ReflectionTestUtils.setField(service, "PHONE_REGEX_EXC", PHONE_REGEX_EXC);
		ReflectionTestUtils.setField(service, "CUSTOMER_REGEX_EXC", CUSTOMER_REGEX_EXC);
		ReflectionTestUtils.setField(service, "IMEI_REGEX_EXC", IMEI_REGEX_EXC);
		ReflectionTestUtils.setField(service, "DATA_NF_EXC", DATA_NF_EXC);
		ReflectionTestUtils.setField(service, "ACTIVATE_EXC", ACTIVATE_EXC);
	}

	@ParameterizedTest
	@CsvSource(value = { "number,ASC,0", "region,DESC,-1", "activationdate,ASC,1", "customerid,DESC,-2" })
	public void testList_success(final String sort, final String dir, final int assertOutput) {

		SortDto sortDto = mock(SortDto.class);
		List<Phone> phones = mock(ArrayList.class);

		when(repo.findAllBy(any(SortDto.class), any(Comparator.class))).thenReturn(phones);
		when(sortDto.getSort()).thenReturn(sort);
		when(sortDto.getDir()).thenReturn(Dir.valueOf(dir));

		service.list(sortDto);

		verify(sortDto).getSort();
		verify(sortDto).getDir();
		verify(repo).findAllBy(sortCaptor.capture(), compCaptor.capture());

		assertEquals(sortCaptor.getValue(), sortDto);

		Comparator<Phone> serviceComp = compCaptor.getValue();

		final Phone p1 = new Phone("1", "3", "c99");
		p1.setActivationDate(LocalDate.of(2020, 1, 1));

		final Phone p2 = new Phone("1", "2", "c7");
		p1.setActivationDate(LocalDate.of(2020, 1, 1));

		assertEquals(assertOutput, serviceComp.compare(p1, p2));

	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "INVALID-CUSTOMER", "/./,;", "*TEXT", "!", "@" })
	public void testListByCustomer_BadRequestException(final String custId) {

		when(repo.findByCustomer(any(SortDto.class), any(Comparator.class), anyString()))
				.thenReturn(Optional.empty());

		final SortDto sortDto = mock(SortDto.class);

		BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
			service.listByCustomer(sortDto, custId);
		});

		assertEquals(CUSTOMER_REGEX_EXC, thrown.getMessage());

		verify(repo, times(0)).findByCustomer(any(SortDto.class), any(Comparator.class), anyString());
	}

	@ParameterizedTest
	@ValueSource(strings = { "VALIDCUSTOMER", "C123", "123A" })
	public void testListByCustomer_ResourceNotFoundException(final String custId) {

		final SortDto sortDto = mock(SortDto.class);

		when(sortDto.getSort()).thenReturn(null);
		when(sortDto.getDir()).thenReturn(Dir.ASC);

		when(repo.findByCustomer(any(SortDto.class), any(Comparator.class), anyString()))
				.thenReturn(Optional.empty());
		ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
			service.listByCustomer(sortDto, custId);
		});

		assertEquals(DATA_NF_EXC, thrown.getMessage());

		verify(repo).findByCustomer(sortCaptor.capture(), compCaptor.capture(), stringCaptor.capture());

		assertEquals(sortDto, sortCaptor.getValue());
		assertEquals(custId, stringCaptor.getValue());
		assertNotNull(compCaptor.getValue());

	}

	@ParameterizedTest
	@CsvSource(value = { "VALIDCUSTOMER,1234|567", "C123,1234", "123A,567|892|1234543" })
	public void testListByCustomer_valid(final String custId, final String phoneList) {

		final SortDto sortDto = mock(SortDto.class);

		when(sortDto.getSort()).thenReturn(null);
		when(sortDto.getDir()).thenReturn(Dir.ASC);
		
		final String[] phones = phoneList.split("\\|");
		final Collection<Phone> coll = Arrays.stream(phones).map(p -> new Phone(p, null, null))
				.collect(Collectors.toList());

		when(repo.findByCustomer(any(SortDto.class), any(Comparator.class), anyString()))
				.thenReturn(Optional.of(coll));

		final Collection<String> ret = service.listByCustomer(sortDto, custId);

		assertThat(ret, containsInAnyOrder(phones));

		verify(sortDto).getSort();
		verify(sortDto).getDir();
		verify(repo).findByCustomer(sortCaptor.capture(), compCaptor.capture(), stringCaptor.capture());

		assertEquals(sortDto, sortCaptor.getValue());
		assertEquals(custId, stringCaptor.getValue());
		assertNotNull(compCaptor.getValue());

	}

	@ParameterizedTest
	@CsvSource(value = { "8999999987,123456789012345", "899-999-9986,987654321012345" })
	public void testActivate_valid(final String phoneNum, final String imei) {

		Phone phoneMock = mock(Phone.class);
		PhoneActivateDto dto = mock(PhoneActivateDto.class);

		final String digits = phoneNum.replaceAll("-", "");
		when(repo.findByPhoneNumber(digits)).thenReturn(Optional.of(phoneMock));
		when(phoneMock.isActivated()).thenReturn(false);
		when(dto.getImei()).thenReturn(imei);

		service.activate(phoneNum, dto);

		verify(repo).findByPhoneNumber(digits);
		verify(phoneMock).setActivated(true);
		verify(phoneMock).setImei(imei);
		verify(phoneMock).setActivationDate(LocalDate.now());
		verify(dto, atLeast(2)).getImei();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@CsvSource(value = { "89999999870", "INVALID_PHONE", "666-77-79900", "ABCDEFGH" }, nullValues = { "null" })
	public void testActivate_BadRequestException(final String phoneNum) {

		Phone phoneMock = mock(Phone.class);
		PhoneActivateDto dto = mock(PhoneActivateDto.class);

		final String digits = null != phoneNum ? phoneNum.replaceAll("-", "") : null;
		when(repo.findByPhoneNumber(digits)).thenReturn(Optional.of(phoneMock));
		when(phoneMock.isActivated()).thenReturn(false);

		BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
			service.activate(phoneNum, dto);
		});

		assertEquals(PHONE_REGEX_EXC, thrown.getMessage());

		verify(repo, times(0)).findByPhoneNumber(digits);
		verify(dto, times(0)).getImei();
		verify(phoneMock, times(0)).isActivated();
	}

	@ParameterizedTest
	@CsvSource(value = { "8999999987,INVALIDIMEI", "899-999-9986,1234", "754-123-5534,",
			"123-456-7890,null" }, nullValues = { "null" })
	public void testActivate_BadRequestException2(final String phoneNum) {

		Phone phoneMock = mock(Phone.class);
		PhoneActivateDto dto = null;

		final String digits = phoneNum.replaceAll("-", "");
		when(repo.findByPhoneNumber(digits)).thenReturn(Optional.of(phoneMock));
		when(phoneMock.isActivated()).thenReturn(false);

		BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
			service.activate(phoneNum, dto);
		});

		assertEquals(IMEI_REGEX_EXC, thrown.getMessage());

		verify(repo, times(0)).findByPhoneNumber(digits);
		verify(phoneMock, times(0)).isActivated();
	}

	@ParameterizedTest
	@CsvSource(value = { "8999999987,INVALIDIMEI", "899-999-9986,1234", "754-123-5534,",
			"123-456-7890,null" }, nullValues = { "null" })
	public void testActivate_BadRequestException2_invalidimei(final String phoneNum, final String imei) {

		Phone phoneMock = mock(Phone.class);
		PhoneActivateDto dto = mock(PhoneActivateDto.class);

		final String digits = phoneNum.replaceAll("-", "");
		when(repo.findByPhoneNumber(digits)).thenReturn(Optional.of(phoneMock));
		when(dto.getImei()).thenReturn(imei);
		when(phoneMock.isActivated()).thenReturn(false);

		BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
			service.activate(phoneNum, dto);
		});

		assertEquals(IMEI_REGEX_EXC, thrown.getMessage());

		verify(repo, times(0)).findByPhoneNumber(digits);
		verify(dto, atLeast(1)).getImei();
		verify(phoneMock, times(0)).isActivated();
	}

	@ParameterizedTest
	@CsvSource(value = { "8999999987,123456789012345", "899-999-9986,987654321012345" })
	public void testActivate_ResourceNotFoundException(final String phoneNum, final String imei) {

		PhoneActivateDto dto = mock(PhoneActivateDto.class);

		final String digits = phoneNum.replaceAll("-", "");
		when(repo.findByPhoneNumber(digits)).thenReturn(Optional.empty());
		when(dto.getImei()).thenReturn(imei);

		ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
			service.activate(phoneNum, dto);
		});

		assertEquals(DATA_NF_EXC, thrown.getMessage());

		verify(repo).findByPhoneNumber(digits);
		verify(dto, atLeast(2)).getImei();
	}

	@ParameterizedTest
	@CsvSource(value = { "8999999987,123456789012345", "899-999-9986,987654321012345" })
	public void testActivate_DuplicateRequestException(final String phoneNum, final String imei) {

		Phone phoneMock = mock(Phone.class);
		PhoneActivateDto dto = mock(PhoneActivateDto.class);

		final String digits = phoneNum.replaceAll("-", "");
		when(repo.findByPhoneNumber(digits)).thenReturn(Optional.of(phoneMock));
		when(phoneMock.isActivated()).thenReturn(true);
		when(dto.getImei()).thenReturn(imei);

		DuplicateRequestException thrown = assertThrows(DuplicateRequestException.class, () -> {
			service.activate(phoneNum, dto);
		});

		assertEquals(ACTIVATE_EXC, thrown.getMessage());

		verify(repo).findByPhoneNumber(digits);
		verify(phoneMock, times(0)).setActivated(true);
		verify(phoneMock, times(0)).setImei(imei);
		verify(phoneMock, times(0)).setActivationDate(LocalDate.now());

	}

}
