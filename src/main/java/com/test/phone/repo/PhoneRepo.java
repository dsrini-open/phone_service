package com.test.phone.repo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.test.phone.dto.SortDto;
import com.test.phone.exception.ImproperDataException;
import com.test.phone.model.Phone;
import com.test.phone.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PhoneRepo implements IRepo<Phone> {

	private static final <T extends Object> BinaryOperator<T> getMergeFn() {
		return (o1, o2) -> o1;
	}

	private static Collection<Phone> phonesData;
	private static Map<String, Phone> phoneIndex;
	private static Map<String, Set<Phone>> customerIndex;

	/**
	 * Init load for static data
	 */
	@PostConstruct
	protected void init() {
		Phone[] phones = { new Phone("8999999999", "1", "a1"), new Phone("8999999998", "2", "a2"),
				new Phone("8999999997", "1", "a10"), new Phone("8999999996", "4", "a3"),
				new Phone("8999999995", "1", "a4"), new Phone("8999999994", "6", "a5"),
				new Phone("8999999993", "1", "a6"), new Phone("8999999992", "1", "a6"),
				new Phone("8999999991", "1", "a10"), new Phone("8999999990", "3", "a6"),
				new Phone("8999999989", "1", "a11"), new Phone("8999999988", "2", "a6"),
				new Phone("8999999987", "2", "a25"), new Phone("8999999986", "1", "a3"),
				new Phone("8999999985", "3", "a12"), new Phone("8999999983", "2", "a2"),
				new Phone("8999999984", "2", "a2") };

		phonesData = new ArrayList<>(Arrays.asList(phones));
		phoneIndex = phonesData.parallelStream()
				.collect(Collectors.toMap(Phone::getNumber, Function.identity(), getMergeFn(), ConcurrentHashMap::new));
		customerIndex = phonesData.parallelStream()
				.collect(Collectors.toMap(Function.identity(), Phone::getCustomerId, getMergeFn(),
						ConcurrentHashMap::new))
				.entrySet().parallelStream().collect(Collectors.groupingBy(Map.Entry::getValue,
						Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));

		log.debug("Phones index {}", phoneIndex);
		log.debug("cust index {}", customerIndex);

	}

	public void save(final Phone phone) {
		final String phoneNum = phone.getNumber();

		/* Future use implementation. Usually DB does this to update indexes. */
		if (!phoneIndex.containsKey(phoneNum)) {
			phonesData.add(phone);
			phoneIndex.put(phoneNum, phone);
			customerIndex.computeIfAbsent(phone.getCustomerId(), v -> new HashSet<>()).add(phone);
		}
		// in-memory updates, skipping
	}

	public Collection<Phone> findAllBy(final SortDto sort, final Comparator<Phone> comp) {
		return doSort(phonesData, sort.getStart(), sort.getLimit(), comp);
	}

	public Optional<Collection<Phone>> findByCustomer(final SortDto sort, final Comparator<Phone> comp,
			final String customerId) {
		if (Util.isEmpty(customerId))
			throw new ImproperDataException();

		Collection<Phone> phones = customerIndex.get(customerId);
		if (null != phones) {
			return Optional.of(doSort(phones, sort.getStart(), sort.getLimit(), comp));
		}
		return Optional.empty();

	}

	public Optional<Phone> findByPhoneNumber(final String phoneNumber) {
		if (Util.isEmpty(phoneNumber))
			throw new ImproperDataException();

		return Optional.ofNullable(phoneIndex.get(phoneNumber));
	}
}
