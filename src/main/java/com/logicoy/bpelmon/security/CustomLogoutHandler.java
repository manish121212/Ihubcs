/**
 * *****************************************************************************
 * Copyright (c) 2008-2015 LogiCoy, Inc. All rights reserved. This program and
 * the accompanying materials are made available under the terms of LogiCoy
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.logicoy.com
 *
 * Contributors: LogiCoy, Inc. - initial API and implementation
 * ****************************************************************************
 */
/*
 * (c) Copyright 2005-2012 JAXIO, www.jaxio.com
 * Source code generated by Celerio, a Jaxio product
 * Want to use Celerio within your company? email us at info@jaxio.com
 * Follow us on twitter: @springfuse
 * Template pack-backend:src/main/java/project/security/AccountDetailsServiceImpl-spring3.p.vm.java
 */
package com.logicoy.bpelmon.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Controller;

/**
 *
 * @author root
 */
@Controller
public class CustomLogoutHandler implements LogoutHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomLogoutHandler.class);

	@Override
	public void logout(HttpServletRequest hsr, HttpServletResponse hsr1, Authentication a) {

		LOGGER.info("Inside logout handler.. ");
		HttpSession session = hsr.getSession(false);

		LOGGER.info("Sending file upload data to elastic search engine...success");
		session.removeAttribute("loggingLogged");
	}

}
