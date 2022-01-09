package com.logicoy.bpelmon.DAO;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.logicoy.bpelmon.models.CompanyInstanceDTO;
import com.logicoy.bpelmon.models.CompanyInstanceListResponse;
import com.logicoy.bpelmon.models.UpdateCompanyInstanceMappingDTO;

public interface CompanyInstanceManagement {

	public CompanyInstanceListResponse getCompanyInstanceList(int companyId);
	public CompanyInstanceListResponse getCompanyInstanceList(String companyName);
	public List<CompanyInstanceDTO> getInstanceList();
	
	public String addCompanyInstance(int companyId, List<CompanyInstanceDTO> instanceList);
	public String addCompanyInstance(String companyName, List<CompanyInstanceDTO> instanceList);
	public boolean addNewInstance(CompanyInstanceDTO dto);
	public boolean updateCompanyInstanceMapping(UpdateCompanyInstanceMappingDTO dto);
}
