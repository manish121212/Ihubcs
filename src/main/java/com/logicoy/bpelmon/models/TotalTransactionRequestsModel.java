package com.logicoy.bpelmon.models;

import java.util.List;

public class TotalTransactionRequestsModel {
	private long totalRequests;
	private long totalFaulted;
	private long totalTerminated;
	private long totalCompleted;
	private List<_TransactionSourceModel> sourceList;

	public long getTotalRequests() {
		return totalRequests;
	}

	public void setTotalRequests(long totalRequests) {
		this.totalRequests = totalRequests;
	}

	public long getTotalFaulted() {
		return totalFaulted;
	}

	public void setTotalFaulted(long totalFaulted) {
		this.totalFaulted = totalFaulted;
	}

	public long getTotalTerminated() {
		return totalTerminated;
	}

	public void setTotalTerminated(long totalTerminated) {
		this.totalTerminated = totalTerminated;
	}

	public long getTotalCompleted() {
		return totalCompleted;
	}

	public void setTotalCompleted(long totalCompleted) {
		this.totalCompleted = totalCompleted;
	}

	public List<_TransactionSourceModel> getSourceList() {
		return sourceList;
	}

	public void setSourceList(List<_TransactionSourceModel> sourceList) {
		this.sourceList = sourceList;
	}

	@Override
	public String toString() {
		return "TransactionRequestModel [totalRequests=" + totalRequests + ", totalFaulted=" + totalFaulted
				+ ", totalTerminated=" + totalTerminated + ", totalCompleted=" + totalCompleted + ", sourceList="
				+ sourceList + "]";
	}
}
