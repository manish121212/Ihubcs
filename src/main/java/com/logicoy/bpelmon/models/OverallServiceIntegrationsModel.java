package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OverallServiceIntegrationsModel {

	@JsonProperty("su_name")
	public String suName;
	@JsonProperty("data")
	List<OASIData> dataList;

	public String getSuName() {

		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public List<OASIData> getDataList() {
		return dataList;
	}

	public void setDataList(List<OASIData> dataList) {
		this.dataList = dataList;
	}

}
