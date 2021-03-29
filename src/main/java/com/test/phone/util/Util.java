package com.test.phone.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.phone.dto.Dir;
import com.test.phone.dto.SortDto;

public final class Util {
	private static final Logger log = LoggerFactory.getLogger(Util.class);

	public static final boolean isEmpty(CharSequence cs) {
		return StringUtils.isBlank(cs);
	}

	public static final SortDto getSortDto(final int start, final int limit, final String sort, final String dir) {
		int beg = (start < 0) ? 0 : start;
		int end = (limit < 0) ? 0 : limit;
		Dir direction = Dir.ASC;
		try {
			if (!isEmpty(dir))
				direction = Dir.valueOf(dir.trim().toUpperCase());
		} catch (Exception e) {
			log.error("Incorrect direction", e);
		}
		String sortBy = isEmpty(sort) ? sort : sort.trim().toLowerCase();
		SortDto dto = new SortDto(beg, end, sortBy, direction);
		return dto;
	}

	private Util() {
	}
}
