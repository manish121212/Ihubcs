package com.logicoy.bpelmon.DAO;

import java.util.List;

import com.logicoy.bpelmon.models.CompanyDetails;
import com.logicoy.bpelmon.models.TimeZoneDTO;

public interface CompanyManagement {

	public List<CompanyDetails> getAllCompanyDetails();

	public int addCompanyDetails(CompanyDetails comp_Details);

	public List<TimeZoneDTO> getTimezoneList();
	
	public TimeZoneDTO getTimeZoneForCode(String tzCode);
}
