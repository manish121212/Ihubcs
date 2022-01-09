package com.logicoy.bpelmon.models;

import java.util.List;

public class TotalRequestsModel {
	private long totalRequests;
	private long totalFaulted;
	private long totalTerminated;
	private long totalCompleted;
	private List<_SourceModel> sourceList;

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

	public List<_SourceModel> getSourceList() {
		return sourceList;
	}

	public void setSourceList(List<_SourceModel> sourceList) {
		this.sourceList = sourceList;
	}
}
