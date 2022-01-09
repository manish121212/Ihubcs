package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BpelUniqueInstanceData {

	@JsonProperty("su_name")
	private String suName;
	@JsonProperty("instance_id")
	private String instanceId;
	@JsonProperty("event_time")
	private long eventTime;

	

	public BpelUniqueInstanceData(String suName, String instanceId, long eventTime) {
		super();
		this.suName = suName;
		this.instanceId = instanceId;
		this.eventTime = eventTime;
	}

	public BpelUniqueInstanceData() {
	}

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

}
