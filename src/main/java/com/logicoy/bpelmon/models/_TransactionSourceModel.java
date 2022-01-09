package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class _TransactionSourceModel {

	@JsonProperty("transaction_id")
	public String transactionId;
	@JsonProperty("bpel_id")
	public String bpelId;
	@JsonProperty("su_name")
	public String suName;
	@JsonProperty("raw_payload")
	public String rawPayload;
	@JsonProperty("instance_id")
	public String instanceId;
	@JsonProperty("transaction_xml")
	public String transactionXml;
	@JsonProperty("event_seq_no")
	public String eventSeqNo;
	@JsonProperty("event_time")
	public String eventTime;
	@JsonProperty("status")
	public String status;
	@JsonProperty("showable_data")
	public String showableData;
	@JsonProperty("client_id")
	public String clientId;
	@JsonProperty("primary_tracking_display")
	public String primaryTrackingId;
	@JsonProperty("primary_tracking_value")
	public String primaryTrackingValue;
	@JsonProperty("secondary_tracking_value")
	public String secondaryTrackingValue;
	@JsonProperty("showable_data_history")
	public List<String> showableDataHistory;
	@JsonProperty("id")
	public String id;
	@JsonProperty("index")
	public String index;
	@JsonProperty("index_type")
	public String indexType;
	@JsonProperty("processing_time")
	public long processingTime;

	public long getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getShowableDataHistory() {
		return showableDataHistory;
	}

	public void setShowableDataHistory(List<String> showableDataHistory) {
		this.showableDataHistory = showableDataHistory;
	}

	public String getSecondaryTrackingValue() {
		return secondaryTrackingValue;
	}

	public void setSecondaryTrackingValue(String secondaryTrackingValue) {
		this.secondaryTrackingValue = secondaryTrackingValue;
	}

	public String getPrimaryTrackingValue() {
		return primaryTrackingValue;
	}

	public void setPrimaryTrackingValue(String primaryTrackingValue) {
		this.primaryTrackingValue = primaryTrackingValue;
	}

	public String getPrimaryTrackingId() {
		return primaryTrackingId;
	}

	public void setPrimaryTrackingId(String primaryTrackingId) {
		this.primaryTrackingId = primaryTrackingId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getBpelId() {
		return bpelId;
	}

	public void setBpelId(String bpelId) {
		this.bpelId = bpelId;
	}

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public String getRawPayload() {
		return rawPayload;
	}

	public void setRawPayload(String rawPayload) {
		this.rawPayload = rawPayload;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getTransactionXml() {
		return transactionXml;
	}

	public void setTransactionXml(String transactionXml) {
		this.transactionXml = transactionXml;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getShowableData() {
		return showableData;
	}

	public void setShowableData(String showableData) {
		this.showableData = showableData;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return transactionId + " " + bpelId + " " + suName + " " + instanceId + " " + eventTime
				+ " " + status + " " + showableData + " " + primaryTrackingId + " "
				+ primaryTrackingValue + " " + secondaryTrackingValue;
	}
}
