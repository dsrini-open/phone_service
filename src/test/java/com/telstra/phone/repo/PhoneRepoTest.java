package com.telstra.phone.repo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telstra.phone.dto.Dir;
import com.telstra.phone.dto.SearchDto;
import com.telstra.phone.exception.ImproperDataException;
import com.telstra.phone.model.Phone;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PhoneRepoTest {

	private PhoneRepo repo;

	@Mock
	private SearchDto search;

	@Mock
	private Comparator<Phone> comp;

	@Captor
	private ArgumentCaptor<Collection<Phone>> dataCaptor;
	@Captor
	private ArgumentCaptor<Comparator<Phone>> compCaptor;
	@Captor
	private ArgumentCaptor<Integer> startCaptor;
	@Captor
	private ArgumentCaptor<Integer> limitCaptor;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		repo = spy(new PhoneRepo());
		/* Not need for loading from db */
		repo.init();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testfindAllBy() {

		Collection<Phone> mockResp = mock(Collection.class);
		doReturn(mockResp).when(repo).doSort(any(Collection.class), anyInt(), anyInt(), any(Comparator.class));
		when(search.getStart()).thenReturn(1);
		when(search.getLimit()).thenReturn(10);

		repo.findAllBy(search, comp);

		verify(repo).doSort(dataCaptor.capture(), startCaptor.capture(), limitCaptor.capture(), compCaptor.capture());
		verify(search).getStart();
		verify(search).getLimit();

		assertEquals(1, startCaptor.getValue());
		assertEquals(10, limitCaptor.getValue());
		assertEquals(comp, compCaptor.getValue());

		/* Asserting initial load - not needed if from db */
		Collection<Phone> some = dataCaptor.getValue();
		assertTrue(some.size() > 0);

	}

	@ParameterizedTest
	@CsvSource(value = { "a1,8999999999", "a3,8999999996|8999999986", "a2,8999999983|8999999984|8999999998" })
	public void testfindByCustomer_valid(final String custId, final String assertPhoneNumbers) {
		final SearchDto search = mock(SearchDto.class);
		final Comparator<Phone> comp = mock(Comparator.class);

		String[] assertList = assertPhoneNumbers.split("\\|");
		
		when(search.getDir()).thenReturn(Dir.ASC);
		when(search.getLimit()).thenReturn(assertList.length);
		
		Optional<Collection<Phone>> resp = repo.findByCustomer(search, comp, custId);

		assertTrue(resp.isPresent());

		Collection<Phone> phones = resp.get();
		log.debug("phones {}" , phones);
		List<String> phoneNumbers = phones.parallelStream().map(Phone::getNumber).collect(Collectors.toList());

		
		assertThat(phoneNumbers, containsInAnyOrder(assertList));
		assertEquals(assertList.length, phones.size());
	}

	@ParameterizedTest
	@CsvSource(value = { "6999999995", "6999999983", "SOMETHINGINVALID" })
	public void testfindByCustomer_invalid(final String customerId) {
		
		final SearchDto search = mock(SearchDto.class);
		final Comparator<Phone> comp = mock(Comparator.class);
		
		Optional<Collection<Phone>> resp = repo.findByCustomer(search, comp, customerId);
		
		assertFalse(resp.isPresent());
		
		verify(search, times(0)).getStart();
		verify(search, times(0)).getLimit();
	}

	@ParameterizedTest
	@NullAndEmptySource
	public void testfindByCustomer_ImproperDataException(final String customerId) {
		final SearchDto search = mock(SearchDto.class);
		final Comparator<Phone> comp = mock(Comparator.class);
		
		assertThrows(ImproperDataException.class, () -> {
			repo.findByCustomer(search, comp, customerId);
		});
	}

	@ParameterizedTest
	@CsvSource(value = { "8999999995", "8999999983", "8999999986" })
	public void testfindByPhoneNumber_valid(final String phoneNumber) {

		Optional<Phone> resp = repo.findByPhoneNumber(phoneNumber);

		assertTrue(resp.isPresent());
		assertEquals(phoneNumber, resp.get().getNumber());
	}

	@ParameterizedTest
	@CsvSource(value = { "6999999995", "6999999983", "SOMETHINGINVALID" })
	public void testfindByPhoneNumber_invalid(final String phoneNumber) {

		Optional<Phone> resp = repo.findByPhoneNumber(phoneNumber);

		assertFalse(resp.isPresent());
	}

	@ParameterizedTest
	@NullAndEmptySource
	public void testfindByPhoneNumber_ImproperDataException(final String phoneNumber) {
		assertThrows(ImproperDataException.class, () -> {
			repo.findByPhoneNumber(phoneNumber);

		});
	}

	@Test
	public void testSave_update() {

		Optional<Phone> resp = repo.findByPhoneNumber("8999999997");

		Phone phone = resp.get();
		phone.setActivated(true);

		repo.save(phone);
		/* No save functionality for now, nothing to assert */
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSave_new() {
		Collection<Phone> mockResp = mock(Collection.class);
		doReturn(mockResp).when(repo).doSort(any(Collection.class), anyInt(), anyInt(), any(Comparator.class));
		when(search.getStart()).thenReturn(1);
		when(search.getLimit()).thenReturn(10);

		repo.findAllBy(search, comp);

		verify(repo).doSort(dataCaptor.capture(), startCaptor.capture(), limitCaptor.capture(), compCaptor.capture());

		/* Asserting initial load - not needed if from db */
		int initSize = dataCaptor.getValue().size();

		Phone phone = new Phone("8888888888", "8", "BrandNewCustomer");
		repo.save(phone);

		repo.findAllBy(search, comp);
		verify(repo, times(2)).doSort(dataCaptor.capture(), startCaptor.capture(), limitCaptor.capture(), compCaptor.capture());

		Collection<Phone> newData = dataCaptor.getValue();

		/* Could only assert one more element */
		assertEquals(initSize, newData.size() - 1);

	}

}
