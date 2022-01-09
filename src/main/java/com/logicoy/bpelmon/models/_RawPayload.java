package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class _RawPayload {
	private String prefix;
	private String key;
	private String value;
	private String rawXML;

	public String getRawXML() {
		return rawXML;
	}

	public void setRawXML(String rawXML) {
		this.rawXML = rawXML;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "_RawPayload [prefix=" + prefix + ", key=" + key + ", value=" + value + "]";
	}
}
