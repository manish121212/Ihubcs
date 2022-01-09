package com.logicoy.bpelmon.DAO;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

import com.logicoy.bpelmon.models.CompanyDetails;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.ServiceNameResolverModel;

public interface SettingsDAO {

	public GenericResponseModel updateCompanyDetails(CompanyDetails updatedCompany);
	public List<ServiceNameResolverModel> getServiceDisplayNameList(String clientId);
	public GenericResponseModel updateServiceDisplayNameList(ServiceNameResolverModel model);
	public GenericResponseModel addServiceDisplayNameList(@RequestBody ServiceNameResolverModel model);
}
