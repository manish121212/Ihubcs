package com.logicoy.bpelmon.models;

import java.util.List;

public class CompanyInstanceListResponse {

	private int companyId;
	private String companyName;
	private List<CompanyInstanceDTO> instanceList;

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public List<CompanyInstanceDTO> getInstanceList() {
		return instanceList;
	}

	public void setInstanceList(List<CompanyInstanceDTO> instanceList) {
		this.instanceList = instanceList;
	}

}
