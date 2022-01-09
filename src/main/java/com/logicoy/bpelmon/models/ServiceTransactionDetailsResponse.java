package com.logicoy.bpelmon.models;

import java.util.List;

public class ServiceTransactionDetailsResponse {

	private List<_TransactionSourceModel> data;
	private int draw;
	private long recordsFiltered;
	private long recordsTotal;
	private int currentPartition;
	private int currentPartitionLength;
	private int searchLength;
	private int currentPageStart;

	public int getCurrentPageStart() {
		return currentPageStart;
	}

	public void setCurrentPageStart(int currentPageStart) {
		this.currentPageStart = currentPageStart;
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

	public int getSearchLength() {
		return searchLength;
	}

	public void setSearchLength(int searchLength) {
		this.searchLength = searchLength;
	}

	public List<_TransactionSourceModel> getData() {
		return data;
	}

	public void setData(List<_TransactionSourceModel> data) {
		this.data = data;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public long getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(long recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public long getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	@Override
	public String toString() {
		return "ServiceTransactionDetailsResponse [\ndata=" + data.size() + "\nrecordsFiltered=" + recordsFiltered
				+ "\nrecordsTotal=" + recordsTotal + "\ncurrentPartition=" + currentPartition
				+ "\ncurrentPartitionLength=" + currentPartitionLength + "\nsearchLength=" + searchLength + "\n]";
	}
}
