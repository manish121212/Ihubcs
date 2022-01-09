package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class _AlertSourceModel {

	@JsonProperty("instance_id")
	private String instanceId;
	@JsonProperty("bpel_id")
	private String bpelId;
	@JsonProperty("status")
	private String status;
	@JsonProperty("message")
	private String message;
	@JsonProperty("event_seq_no")
	private String eventSeqNo;
	@JsonProperty("event_time")
	private String eventTime;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getBpelId() {
		return bpelId;
	}

	public void setBpelId(String bpelId) {
		this.bpelId = bpelId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEventSeqNo() {
		return eventSeqNo;
	}

	public void setEventSeqNo(String eventSeqNo) {
		this.eventSeqNo = eventSeqNo;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}

	@Override
	public String toString() {
		return "_AlertSourceModel [instanceId=" + instanceId + ", bpelId=" + bpelId + ", status=" + status
				+ ", message=" + message + ", eventSeqNo=" + eventSeqNo + ", eventTime=" + eventTime + "]";
	}
}
