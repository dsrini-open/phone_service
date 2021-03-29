package com.test.phone.dto;

import lombok.Data;

@Data
public class SortDto {
	private int start;
	private int limit;
	private String sort;
	private Dir dir = Dir.ASC;

	public SortDto(int start, int limit, String sort, Dir dir) {
		this.start = start;
		this.limit = this.start + limit;
		this.sort = sort;
		this.dir = dir;
	}
}
