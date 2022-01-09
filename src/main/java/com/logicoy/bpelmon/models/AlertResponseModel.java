package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlertResponseModel {

	@JsonProperty("data")
	public List<ServiceAlertData> data = null;
	@JsonProperty("source_list")
	public List<_AlertNotificationSourceModel> sourceList = null;

	public List<ServiceAlertData> getData() {
		return data;
	}

	public void setData(List<ServiceAlertData> data) {
		this.data = data;
	}

	public List<_AlertNotificationSourceModel> getSourceList() {
		return sourceList;
	}

	public void setSourceList(List<_AlertNotificationSourceModel> sourceList) {
		this.sourceList = sourceList;
	}
}
