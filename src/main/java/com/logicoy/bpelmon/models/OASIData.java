package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OASIData {
	@JsonProperty("event_date")
	public String eventDate;
	@JsonProperty("integrations")
	public String integrations;

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}

	public String getIntegrations() {
		return integrations;
	}

	public void setIntegrations(String integrations) {
		this.integrations = integrations;
	}
}
