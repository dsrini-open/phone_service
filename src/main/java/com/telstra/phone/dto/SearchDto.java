package com.telstra.phone.dto;

import lombok.Data;

@Data
public class SearchDto {
	private int start;
	private int limit;
	private String sort;
	private Dir dir = Dir.ASC;

	public SearchDto(int start, int limit, String sort, Dir dir) {
		this.start = start;
		this.limit = this.start + limit;
		this.sort = sort;
		this.dir = dir;
	}
}
