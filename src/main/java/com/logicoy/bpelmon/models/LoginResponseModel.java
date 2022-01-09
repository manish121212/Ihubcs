/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Shrivats
 */
public class LoginResponseModel {
	@JsonProperty("token")
	private String token;
	@JsonProperty("expiry_time")
	private String expiryTime;
	@JsonProperty("status")
	private int status; // 0 - fail ; 1- successful ; 2 - unauthorized role
	@JsonProperty("message")
	private String message;
	@JsonProperty("authority")
	private List<Authority> authority;
	@JsonProperty("userDetails")
	private UserDTO userdetails;

	public UserDTO getUserdetails() {
		return userdetails;
	}

	public void setUserdetails(UserDTO userdetails) {
		this.userdetails = userdetails;
	}

	public List<Authority> getAuthority() {
		return authority;
	}

	public void setAuthority(List<Authority> authority) {
		this.authority = authority;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
