package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDetailsResponse {
	private int recordsFiltered;
	private long recordsTotal;
	private List<ServiceTransactionDetailModel> data;
	private int currentPartition;
	private int currentPartitionLength;
	private int searchStart;
	private int searchLength;

	public int getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(int recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public long getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public List<ServiceTransactionDetailModel> getData() {
		return data;
	}

	public void setData(List<ServiceTransactionDetailModel> data) {
		this.data = data;
	}

	public int getCurrentPartition() {
		return currentPartition;
	}

	public void setCurrentPartition(int currentPartition) {
		this.currentPartition = currentPartition;
	}

	public int getCurrentPartitionLength() {
		return currentPartitionLength;
	}

	public void setCurrentPartitionLength(int currentPartitionLength) {
		this.currentPartitionLength = currentPartitionLength;
	}

	public int getSearchStart() {
		return searchStart;
	}

	public void setSearchStart(int searchStart) {
		this.searchStart = searchStart;
	}

	public int getSearchLength() {
		return searchLength;
	}

	public void setSearchLength(int searchLength) {
		this.searchLength = searchLength;
	}

}
