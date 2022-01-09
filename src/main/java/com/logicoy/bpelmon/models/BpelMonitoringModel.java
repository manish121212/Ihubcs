package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BpelMonitoringModel {

	@JsonProperty("bpel_activities")
	private long bpelActivities;
	@JsonProperty("bpel_variables")
	private long bpelVariables;
	@JsonProperty("outbound_invokes")
	private long outboundInvokes;
	@JsonProperty("completion_time")
	private double completionTime;
	@JsonProperty("transactions")
	private long transactions;
	@JsonProperty("notifications")
	private long notifications;
	@JsonProperty("alerts")
	private long alerts;
	@JsonProperty("bpel_activity_list")
	private List<_SourceModel> bpelActivityList;
	@JsonProperty("bpel_variable_list")
	private List<_SourceModel> bpelVariableList;
	@JsonProperty("transaction_list")
	private List<_TransactionSourceModel> transactionList;

	public List<_SourceModel> getBpelActivityList() {
		return bpelActivityList;
	}

	public void setBpelActivityList(List<_SourceModel> bpelActivityList) {
		this.bpelActivityList = bpelActivityList;
	}

	public List<_SourceModel> getBpelVariableList() {
		return bpelVariableList;
	}

	public void setBpelVariableList(List<_SourceModel> bpelVariableList) {
		this.bpelVariableList = bpelVariableList;
	}

	public List<_TransactionSourceModel> getTransactionList() {
		return transactionList;
	}

	public void setTransactionList(List<_TransactionSourceModel> transactionList) {
		this.transactionList = transactionList;
	}

	public long getBpelActivities() {
		return bpelActivities;
	}

	public void setBpelActivities(long bpelActivities) {
		this.bpelActivities = bpelActivities;
	}

	public long getBpelVariables() {
		return bpelVariables;
	}

	public void setBpelVariables(long bpelVariables) {
		this.bpelVariables = bpelVariables;
	}

	public long getOutboundInvokes() {
		return outboundInvokes;
	}

	public void setOutboundInvokes(long outboundInvokes) {
		this.outboundInvokes = outboundInvokes;
	}

	public double getCompletionTime() {
		return completionTime;
	}

	public void setCompletionTime(double runningSec) {
		this.completionTime = runningSec;
	}

	public long getTransactions() {
		return transactions;
	}

	public void setTransactions(long transactions) {
		this.transactions = transactions;
	}

	public long getNotifications() {
		return notifications;
	}

	public void setNotifications(long notifications) {
		this.notifications = notifications;
	}

	public long getAlerts() {
		return alerts;
	}

	public void setAlerts(long alerts) {
		this.alerts = alerts;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return "BpelMonitoringModel [bpelActivities=" + bpelActivities + ", bpelVariables=" + bpelVariables
				+ ", outboundInvokes=" + outboundInvokes + ", completionTime=" + completionTime + ", transactions="
				+ transactions + ", notifications=" + notifications + ", alerts=" + alerts + ", bpelActivityList="
				+ (bpelActivityList != null ? bpelActivityList.subList(0, Math.min(bpelActivityList.size(), maxLen))
						: null)
				+ ", bpelVariableList="
				+ (bpelVariableList != null ? bpelVariableList.subList(0, Math.min(bpelVariableList.size(), maxLen))
						: null)
				+ ", transactionList="
				+ (transactionList != null ? transactionList.subList(0, Math.min(transactionList.size(), maxLen))
						: null)
				+ "]";
	}
}
