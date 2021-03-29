package com.test.phone.repo;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface IRepo<T> {
	default Collection<T> doSort(final Collection<T> data, int start, int end, final Comparator<T> comp) {
		List<T> sortData = (List<T>) data.stream().sorted(comp).limit(end).skip(start).collect(Collectors.toList());
		return sortData;
	}
}
