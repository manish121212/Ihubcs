package com.logicoy.bpelmon.models;

public class ReportConfig {

	private boolean isCompleted;
	private boolean isFaulted;
	private boolean isStarted;
	private boolean isServiceReport;
	private boolean isExcel;

	public boolean isExcel() {
		return isExcel;
	}

	public void setExcel(boolean isExcel) {
		this.isExcel = isExcel;
	}

	public boolean isServiceReport() {
		return isServiceReport;
	}

	public void setServiceReport(boolean isServiceReport) {
		this.isServiceReport = isServiceReport;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public boolean isFaulted() {
		return isFaulted;
	}

	public void setFaulted(boolean isFaulted) {
		this.isFaulted = isFaulted;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	@Override
	public String toString() {
		return "ReportConfig [isCompleted=" + isCompleted + ", isFaulted=" + isFaulted + ", isStarted=" + isStarted
				+ "]";
	}

}
