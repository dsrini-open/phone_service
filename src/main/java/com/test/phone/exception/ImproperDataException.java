package com.test.phone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ImproperDataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MSG = "Unexpected Data query";

	public ImproperDataException() {
		super(DEFAULT_MSG);
	}

}
