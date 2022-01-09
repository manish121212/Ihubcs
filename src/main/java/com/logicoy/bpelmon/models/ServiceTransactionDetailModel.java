package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceTransactionDetailModel {
	@JsonProperty("su_name")
	public String suName;
	@JsonProperty("transaction_type")
	public String transactionType;
	@JsonProperty("started")
	public long started;
	@JsonProperty("completed")
	public long completed;
	@JsonProperty("faulted")
	public long faulted;
	@JsonProperty("status")
	public int status;
	@JsonProperty("last_event_time")
	public String lastEventTime;
	@JsonProperty("average_transaction_time")
	public long averageTransactionTime;

	public long getAverageTransactionTime() {
		return averageTransactionTime;
	}

	public void setAverageTransactionTime(long averageTransactionTime) {
		this.averageTransactionTime = averageTransactionTime;
	}

	public String getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(String lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public long getStarted() {
		return started;
	}

	public void setStarted(long started) {
		this.started = started;
	}

	public long getCompleted() {
		return completed;
	}

	public void setCompleted(long completed) {
		this.completed = completed;
	}

	public long getFaulted() {
		return faulted;
	}

	public void setFaulted(long faulted) {
		this.faulted = faulted;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "[suName=" + suName + ", started="
				+ started + ", completed=" + completed + ", faulted=" + faulted + ", status=" + status + "]";
	}

}
