package com.logicoy.bpelmon.models;

import java.util.List;

public class ServiceNameResolverModel {
	private String clientId;
	private List<ServiceNameModel> serviceNames;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public List<ServiceNameModel> getServiceNames() {
		return serviceNames;
	}

	public void setServiceNames(List<ServiceNameModel> serviceNames) {
		this.serviceNames = serviceNames;
	}

	@Override
	public String toString() {
		return "ServiceNameResolverModel [clientId=" + clientId + ", serviceNames=" + serviceNames.toString() + "]";
	}

}
