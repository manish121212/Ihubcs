package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class STDetailData {
	@JsonProperty("transaction_type")
	public String transactionType;
	@JsonProperty("started")
	public String started;
	@JsonProperty("completed")
	public String completed;
	@JsonProperty("faulted")
	public String faulted;

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getStarted() {
		return started;
	}

	public void setStarted(String started) {
		this.started = started;
	}

	public String getCompleted() {
		return completed;
	}

	public void setCompleted(String completed) {
		this.completed = completed;
	}

	public String getFaulted() {
		return faulted;
	}

	public void setFaulted(String faulted) {
		this.faulted = faulted;
	}

}
