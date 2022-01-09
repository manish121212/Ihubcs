package com.logicoy.bpelmon.models;

public class TransactionSearch {

	@Override
	public String toString() {
		return "TransactionSearch [regex=" + regex + ", value=" + value + "]";
	}
	private boolean regex;
	private String value;
	public boolean isRegex() {
		return regex;
	}
	public void setRegex(boolean regex) {
		this.regex = regex;
	}
	public String getValue() {
		return value != null ? value : "";
	}
	public void setValue(String value) {
		this.value = value;
	}
}
