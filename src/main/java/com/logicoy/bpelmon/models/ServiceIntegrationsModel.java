package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceIntegrationsModel {
	@JsonProperty("su_name")
	public String suName;
	@JsonProperty("data")
	public List<ServiceIntegrationsData> data;

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public List<ServiceIntegrationsData> getData() {
		return data;
	}

	public void setData(List<ServiceIntegrationsData> data) {
		this.data = data;
	}

}