package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawPayload {
	private String rawPayload;
	private String eventTime;

	
	public RawPayload() {
	}
	

	public RawPayload(String rawPayload, String eventTime) {
		super();
		this.rawPayload = rawPayload;
		this.eventTime = eventTime;
	}


	public String getRawPayload() {
		return rawPayload;
	}

	public void setRawPayload(String rawPayload) {
		this.rawPayload = rawPayload;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
	
}
