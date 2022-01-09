package com.logicoy.bpelmon.security;

import org.springframework.security.authentication.AccountStatusException;

public class InvalidLoginException extends AccountStatusException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidLoginException(String msg) {
		super(msg);
	}

	public InvalidLoginException(String msg, Throwable t) {
		super(msg, t);
	}

}
