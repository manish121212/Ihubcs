package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyInstanceDTO {

	private int id;
	private String instanceName;
	private String esClientName;
	private String instanceDetails;
	private String createdAt;
	private String companyMapped;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getEsClientName() {
		return esClientName;
	}

	public void setEsClientName(String esClientName) {
		this.esClientName = esClientName;
	}

	public String getInstanceDetails() {
		return instanceDetails;
	}

	public void setInstanceDetails(String instanceDetails) {
		this.instanceDetails = instanceDetails;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getCompanyMapped() {
		return companyMapped;
	}

	public void setCompanyMapped(String companyMapped) {
		this.companyMapped = companyMapped;
	}

}
