package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceAlertsModel {

	@JsonProperty("su_name")
	private String suName;
	@JsonProperty("major")
	private String major;
	@JsonProperty("minor")
	private String minor;

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}
}
