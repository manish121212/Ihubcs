package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceAlertData {
	@JsonProperty("su_name")
	public String suName;
	@JsonProperty("major")
	public long major;
	@JsonProperty("minor")
	public long minor;
	@JsonProperty("time")
	public String time;

	@Override
	public String toString() {
		return "ServiceAlertData [suName=" + suName + ", major=" + major + ", minor=" + minor + "]";
	}

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public long getMajor() {
		return major;
	}

	public void setMajor(long l) {
		this.major = l;
	}

	public long getMinor() {
		return minor;
	}

	public void setMinor(long minor) {
		this.minor = minor;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
}
