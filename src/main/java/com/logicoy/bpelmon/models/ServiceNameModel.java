package com.logicoy.bpelmon.models;

public class ServiceNameModel {
	private String suName;
	private String displayName;

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return "ServiceNameModel [suName=" + suName + ", displayName=" + displayName + "]";
	}

}
