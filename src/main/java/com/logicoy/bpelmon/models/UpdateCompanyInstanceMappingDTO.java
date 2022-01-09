package com.logicoy.bpelmon.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCompanyInstanceMappingDTO {
	private int companyId;
	private List<CompanyInstanceDTO> instanceList;

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public List<CompanyInstanceDTO> getInstanceList() {
		return instanceList;
	}

	public void setInstanceList(List<CompanyInstanceDTO> instanceList) {
		this.instanceList = instanceList;
	}

}
