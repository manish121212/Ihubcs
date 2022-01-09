/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicoy.bpelmon.DAO;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 *
 * @author sharmila
 */
public class UserDAO extends JdbcDaoSupport {

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	public void updateUserToken(String authToken, String userName, String expiryTime) {
		try {
			Object[] parameters = new Object[] { userName, authToken, expiryTime };
			jdbcTemplate.update(
					"insert into bpelmon.user_token_manage(username, auth_token,expiry_time) values(?,?,?) ",
					parameters);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
