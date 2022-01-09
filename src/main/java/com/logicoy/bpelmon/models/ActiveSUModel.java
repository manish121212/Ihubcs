package com.logicoy.bpelmon.models;

import java.util.List;

public class ActiveSUModel {
	long startedSU;
	long inactiveSU;
	List<_SourceModel> sourceList;

	public long getStartedSU() {
		return startedSU;
	}

	public void setStartedSU(long startedSU) {
		this.startedSU = startedSU;
	}

	public long getInactiveSU() {
		return inactiveSU;
	}

	public void setInactiveSU(long inactiveSU) {
		this.inactiveSU = inactiveSU;
	}

	public List<_SourceModel> getSourceList() {
		return sourceList;
	}

	public void setSourceList(List<_SourceModel> sourceList) {
		this.sourceList = sourceList;
	}
}
