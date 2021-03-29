package com.test.phone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_MODIFIED)
public class DuplicateRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateRequestException(String message) {
		super(message);
	}

}
