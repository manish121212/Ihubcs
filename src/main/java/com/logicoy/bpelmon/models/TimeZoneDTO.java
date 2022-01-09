package com.logicoy.bpelmon.models;

public class TimeZoneDTO {

	private String tzCode;
	private String tzLocation;
	private String tzDifference;

	public String getTzCode() {
		return tzCode;
	}

	public void setTzCode(String tzCode) {
		this.tzCode = tzCode;
	}

	public String getTzLocation() {
		return tzLocation;
	}

	public void setTzLocation(String tzLocation) {
		this.tzLocation = tzLocation;
	}

	public String getTzDifference() {
		return tzDifference;
	}

	public void setTzDifference(String tzDifference) {
		this.tzDifference = tzDifference;
	}
}
