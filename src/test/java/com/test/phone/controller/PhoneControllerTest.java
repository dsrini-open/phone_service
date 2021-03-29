package com.test.phone.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.test.phone.controller.impl.PhoneController;
import com.test.phone.dto.Dir;
import com.test.phone.dto.PhoneActivateDto;
import com.test.phone.dto.SortDto;
import com.test.phone.service.IPhoneService;

public class PhoneControllerTest {

	private static int SRCH_MAX_LIMIT = 15;

	private IPhoneController controller;

	@Mock
	private IPhoneService service;

	@Captor
	private ArgumentCaptor<SortDto> sortCaptor;

	@Captor
	private ArgumentCaptor<String> stringCaptor;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);

		controller = new PhoneController(service);
		ReflectionTestUtils.setField(controller, "SRCH_MAX_LIMIT", SRCH_MAX_LIMIT);
	}

	@ParameterizedTest
	@CsvSource(value = { "1,10,number,ASC,", "10,5,region,DESC,", "4,14,activationDate,ASC,",
			"4,14,activationDate,ASC,null" }, nullValues = { "null" })
	public void testList_all_valid(final int start, final int limit, final String sort, final String dir,
			final String custId) {

		@SuppressWarnings("unchecked")
		Collection<String> mockResp = mock(Collection.class);
		@SuppressWarnings("unchecked")
		Collection<String> invMock = mock(Collection.class);

		when(service.list(any(SortDto.class))).thenReturn(mockResp);
		when(service.listByCustomer(any(SortDto.class), anyString())).thenReturn(invMock);
		final ResponseEntity<Collection<String>> resp = controller.list(start, limit, sort, dir, custId);

		verify(service).list(sortCaptor.capture());
		verify(service, times(0)).listByCustomer(any(SortDto.class), anyString());
		SortDto contDto = sortCaptor.getValue();

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertEquals(start, contDto.getStart());
		assertEquals(limit + start, contDto.getLimit());
		assertEquals(sort.trim().toLowerCase(), contDto.getSort());
		assertEquals(Dir.valueOf(dir), contDto.getDir());
	}

	@ParameterizedTest
	@CsvSource(value = { "-1,56,INVALID_FIELD,NOSORT,", "-10,500,ssn,,", "-100,14,null,null,null" }, nullValues = {
			"null" })
	public void testList_all_invalidinputs(final int start, final int limit, final String sort, final String dir,
			final String custId) {

		@SuppressWarnings("unchecked")
		Collection<String> mockResp = mock(Collection.class);
		@SuppressWarnings("unchecked")
		Collection<String> invMock = mock(Collection.class);
		

		when(service.list(any(SortDto.class))).thenReturn(mockResp);
		when(service.listByCustomer(any(SortDto.class), anyString())).thenReturn(invMock);
		final ResponseEntity<Collection<String>> resp = controller.list(start, limit, sort, dir, custId);

		verify(service).list(sortCaptor.capture());
		verify(service, times(0)).listByCustomer(any(SortDto.class), anyString());
		SortDto contDto = sortCaptor.getValue();

		assertEquals(0, contDto.getStart());
		assertEquals(limit > SRCH_MAX_LIMIT ? SRCH_MAX_LIMIT : limit, contDto.getLimit());
		assertEquals(sort != null ? sort.trim().toLowerCase() : sort, contDto.getSort());
		assertEquals(Dir.ASC, contDto.getDir());

		assertEquals(HttpStatus.OK, resp.getStatusCode());
	}

	@Test
	public void testList_exception() {
		doThrow(RuntimeException.class).when(service).list(any(SortDto.class));

		RuntimeException exc = assertThrows(RuntimeException.class, () -> {
			controller.list(anyInt(), anyInt(), anyString(), anyString(), null);
		});
		
		assertNotNull(exc);
	}

	@ParameterizedTest
	@CsvSource(value = { "1,10,number,ASC,a3", "10,5,region,DESC,a5", "4,14,activationDate,ASC,a123",
			"4,14,activationDate,ASC,c1" }, nullValues = { "null" })
	public void testList_customer_valid(final int start, final int limit, final String sort, final String dir,
			final String custId) {

		@SuppressWarnings("unchecked")
		Collection<String> mockResp = mock(Collection.class);
		@SuppressWarnings("unchecked")
		Collection<String> invMock = mock(Collection.class);

		when(service.list(any(SortDto.class))).thenReturn(invMock);
		when(service.listByCustomer(any(SortDto.class), anyString())).thenReturn(mockResp);
		ResponseEntity<Collection<String>> resp = controller.list(start, limit, sort, dir, custId);

		verify(service, times(0)).list(sortCaptor.capture());
		verify(service).listByCustomer(sortCaptor.capture(), stringCaptor.capture());
		SortDto contDto = sortCaptor.getValue();

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertEquals(custId, stringCaptor.getValue());

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertEquals(start, contDto.getStart());
		assertEquals(limit + start, contDto.getLimit());
		assertEquals(sort.trim().toLowerCase(), contDto.getSort());
		assertEquals(Dir.valueOf(dir), contDto.getDir());
	}

	@ParameterizedTest
	@ValueSource(strings = {"c1", "a123", "valid"})
	public void testlist_customer_valid_exception(final String custId) {
		doThrow(RuntimeException.class).when(service).listByCustomer(any(SortDto.class), anyString());

		RuntimeException exc = assertThrows(RuntimeException.class, () -> {
			controller.list(anyInt(), anyInt(), anyString(), anyString(), anyString());
		});
		
		assertNotNull(exc);
	}

	@Test
	public void testActivate() {
		PhoneActivateDto dto = new PhoneActivateDto();
		dto.setImei("12345");

		doNothing().when(service).activate(anyString(), any(PhoneActivateDto.class));

		ResponseEntity<?> resp = controller.activate("1234567890", dto);

		verify(service).activate("1234567890", dto);

		assertEquals(resp.getStatusCode(), HttpStatus.NO_CONTENT);
	}

	@Test
	public void testActivate_exception() {

		PhoneActivateDto dto = new PhoneActivateDto();
		String test = "testString";

		doThrow(RuntimeException.class).when(service).activate(test, dto);

		assertThrows(RuntimeException.class, () -> {
			controller.activate(test, dto);
		});
	}

}
