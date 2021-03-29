package com.test.phone.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.test.phone.model.Phone;

public class IRepoTest {

	private static class TestPhoneRepo implements IRepo<Phone> {
	}

	private List<Phone> phonesData;

	private IRepo<Phone> repo;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);

		repo = new TestPhoneRepo();

		Phone[] phones = { new Phone("testNumber0", "testRegion0", "testCustomer0"),
				new Phone("testNumber1", "testRegion1", "testCustomer1"),
				new Phone("testNumber2", "testRegion2", "testCustomer2"),
				new Phone("testNumber3", "testRegion3", "testCustomer3"),
				new Phone("testNumber4", "testRegion4", "testCustomer4"),
				new Phone("testNumber5", "testRegion5", "testCustomer5"),
				new Phone("testNumber6", "testRegion6", "testCustomer6"),
				new Phone("testNumber7", "testRegion7", "testCustomer7"),
				new Phone("testNumber8", "testRegion8", "testCustomer8"),
				new Phone("testNumber9", "testRegion9", "testCustomer9") };

		phones[4].setActivationDate(LocalDate.of(2020, 1, 15));
		phones[5].setActivationDate(LocalDate.of(2021, 2, 15));
		phones[6].setActivationDate(LocalDate.of(2021, 2, 15));
		phones[7].setActivationDate(LocalDate.of(2021, 2, 21));

		phonesData = Arrays.asList(phones);
	}

	@Test
	public void testDoSort_numberSort() {
		List<Phone> retPhones = (List<Phone>) repo.doSort(phonesData, 0, phonesData.size(),
				Comparator.comparing(Phone::getNumber).reversed());

		assertEquals(retPhones.size(), 10);
		assertEquals(retPhones.get(0).getNumber(), "testNumber9");
		assertEquals(retPhones.get(9).getNumber(), "testNumber0");
	}

	@Test
	public void testDoSort_regionSort() {
		List<Phone> retPhones = (List<Phone>) repo.doSort(phonesData, 2, 6, Comparator.comparing(Phone::getRegion));

		assertEquals(retPhones.size(), 4);
		assertEquals(retPhones.get(0).getRegion(), "testRegion2");
		assertEquals(retPhones.get(3).getRegion(), "testRegion5");
	}

	@Test
	public void testDoSort_customerSort() {
		List<Phone> retPhones = (List<Phone>) repo.doSort(phonesData, 3, 8,
				Comparator.comparing(Phone::getCustomerId).reversed());

		assertEquals(retPhones.size(), 5);
		assertEquals(retPhones.get(0).getCustomerId(), "testCustomer6");
		assertEquals(retPhones.get(4).getCustomerId(), "testCustomer2");
	}

}
