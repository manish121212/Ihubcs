package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReprocessConfigModel {

	@Override
	public String toString() {
		return "ReprocessConfigModel [serviceName=" + serviceName + ", clientId=" + clientId + ", bpelId=" + bpelId
				+ ", type=" + type + "]";
	}

	private String serviceName;
	private String clientId;
	private String bpelId;
	private String type = null;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getBpelId() {
		return bpelId;
	}

	public void setBpelId(String bpelId) {
		this.bpelId = bpelId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
