package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceIntegrationsData {
	@JsonProperty("bpel_name")
	public String bpelName;
	@JsonProperty("bpel_instances")
	public String bpelInstances;

	public String getBpelName() {
		return bpelName;
	}

	public void setBpelName(String bpelName) {
		this.bpelName = bpelName;
	}

	public String getBpelInstances() {
		return bpelInstances;
	}

	public void setBpelInstances(String bpelInstances) {
		this.bpelInstances = bpelInstances;
	}
}
