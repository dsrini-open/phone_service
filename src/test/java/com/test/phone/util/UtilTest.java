package com.test.phone.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.test.phone.dto.Dir;
import com.test.phone.dto.SortDto;

public class UtilTest {

	@ParameterizedTest
	@NullAndEmptySource
	public void testIsEmpty_valid(String str) {
		assertTrue(Util.isEmpty(str));
	}

	@ParameterizedTest
	@ValueSource(strings = { "null", "INVALID", "VALID" })
	public void testIsEmpty_invalid(String str) {
		assertFalse(Util.isEmpty(str));
	}

	@ParameterizedTest
	@CsvSource(value = { "0,10,region,ASC", "-1,-10,some,DESC", "-100,100,invalid,invalid" })
	public void testGetSortDto(int start, int limit, String sort, String dir) {

		SortDto dto = Util.getSortDto(start, limit, sort, dir);

		assertNotNull(dto);
		if (Util.isEmpty(dir) || !(dir.equals("ASC") || dir.equals("DESC")))
			assertEquals(Dir.ASC, dto.getDir());
		else
			assertEquals(Dir.valueOf(dir), dto.getDir());
		if (Util.isEmpty(sort))
			assertEquals(sort, dto.getSort());
		else
			assertEquals(sort.trim().toLowerCase(), dto.getSort());
		assertEquals(limit < 0 ? 0 : limit, dto.getLimit());
		assertEquals(start < 0 ? 0 : start, dto.getStart());

	}

}
