package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class _AlertNotificationSourceModel {

	@JsonProperty("doc_id")
	private String docId;
	@JsonProperty("index")
	private String index;
	@JsonProperty("index_type")
	private String indexType;
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
	@JsonProperty("type")
	private String type;
	@JsonProperty("su_name")
	private String suName;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	@Override
	public String toString() {
		return "_AlertNotificationSourceModel [docId=" + docId + ", index=" + index + ", indexType=" + indexType
				+ ", instanceId=" + instanceId + ", bpelId=" + bpelId + ", status=" + status + ", message=" + message
				+ ", eventSeqNo=" + eventSeqNo + ", eventTime=" + eventTime + ", type=" + type + ", suName=" + suName
				+ "]";
	}

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

}
