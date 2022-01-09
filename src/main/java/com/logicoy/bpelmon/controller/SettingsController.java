package com.logicoy.bpelmon.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import com.logicoy.bpelmon.DAO.CompanyManagement;
import com.logicoy.bpelmon.DAO.ReprocessConfigDAO;
import com.logicoy.bpelmon.DAO.SettingsDAO;
import com.logicoy.bpelmon.models.CompanyDetails;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.ReprocessConfigDTO;
import com.logicoy.bpelmon.models.ServiceNameResolverModel;

//@RestController
//@RequestMapping("/api/settings")
public class SettingsController {

	@Autowired
	SettingsDAO settingsDAO;
	@Autowired
	ReprocessConfigDAO reprocessConfig;
	@Autowired
	CompanyManagement companyMgmt;

	// @GetMapping("/getCompanyList")
	public List<CompanyDetails> getCompanyList() {
		return companyMgmt.getAllCompanyDetails();
	}

	// @PostMapping("/updateCompanyDetails")
	public GenericResponseModel updateCompanyDetails(@RequestBody CompanyDetails updatedCompany) {
		return settingsDAO.updateCompanyDetails(updatedCompany);
	}

	// @GetMapping("/getReprocessConfigurationForClient")
	public List<ReprocessConfigDTO> getReprocessConfigurationForClient(String clientId) {
		return reprocessConfig.getConfigurationForClient(clientId);
	}

	// @PostMapping("/addReprocessConfiguration")
	public GenericResponseModel addReprocessConfiguration(@RequestBody ReprocessConfigDTO config) {
		return reprocessConfig.addConfiguration(config);
	}

	// @PostMapping("/updateReprocessConfig")
	public GenericResponseModel updateReprocessConfig(@RequestBody ReprocessConfigDTO config) {
		return reprocessConfig.updateConfiguration(config);
	}

	// @GetMapping("/getServiceDisplayNameList")
	public List<ServiceNameResolverModel> getServiceDisplayNameList(String clientId) {
		return settingsDAO.getServiceDisplayNameList(clientId);
	}

	// @PostMapping("/updateServiceDisplayNameList")
	public GenericResponseModel updateServiceDisplayNameList(@RequestBody ServiceNameResolverModel model) {
		return settingsDAO.updateServiceDisplayNameList(model);
	}

	// @PostMapping("/addServiceDisplayNameList")
	public GenericResponseModel addServiceDisplayNameList(@RequestBody ServiceNameResolverModel model) {
		return settingsDAO.addServiceDisplayNameList(model);
	}
}