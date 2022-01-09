package com.logicoy.bpelmon.controller;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logicoy.bpelmon.DAO.CompanyInstanceManagement;
import com.logicoy.bpelmon.DAO.CompanyManagement;
import com.logicoy.bpelmon.models.CompanyDetails;
import com.logicoy.bpelmon.models.CompanyInstanceDTO;
import com.logicoy.bpelmon.models.CompanyInstanceListResponse;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.UpdateCompanyInstanceMappingDTO;
import com.logicoy.bpelmon.utils.AppLogger;

@RestController
@RequestMapping("/api")
public class CompanyController {

	@Autowired
	CompanyManagement comp_Management;
	@Autowired
	CompanyInstanceManagement companyInstanceMgmt;

	AppLogger logger = new AppLogger(Logger.getLogger(this.getClass().getName()));

	@GetMapping("/getAllCompanyDetails")
	public List<CompanyDetails> getAllCompanyDetails() {
		List<CompanyDetails> all_Comp_Details = comp_Management.getAllCompanyDetails();
		return all_Comp_Details;
	}

	@PostMapping("/addCompanyDetails")
	public GenericResponseModel addCompanyDetails(@RequestBody CompanyDetails comp_Details) {

		int isCompDetalsUpdated = comp_Management.addCompanyDetails(comp_Details);
		GenericResponseModel model = new GenericResponseModel();
		if (isCompDetalsUpdated == 1) {
			model.setStatus(1);
			model.setMessage("updated");
		} else if (isCompDetalsUpdated == 2) {
			model.setStatus(2);
			model.setMessage("Internal server error");
		} else if (isCompDetalsUpdated == 3) {
			model.setStatus(3);
			model.setMessage("Company Name already exist");
		} else if (isCompDetalsUpdated == 4) {
			model.setStatus(4);
			model.setMessage("Email already exist");
		} else if (isCompDetalsUpdated == 5) {
			model.setStatus(5);
			model.setMessage("Contact number already exist");
		} else if (isCompDetalsUpdated == 6) {
			model.setStatus(6);
			model.setMessage("website already exist");
		} else {
			model.setStatus(0);
			model.setMessage("Failed");
		}
		return model;
	}
	@GetMapping("/getCompanyInstanceList")
	public CompanyInstanceListResponse getCompanyInstanceList(int companyId) {
		return companyInstanceMgmt.getCompanyInstanceList(companyId);
	}
	@GetMapping("/getInstanceList")
	public List<CompanyInstanceDTO> getInstanceList() {
		return companyInstanceMgmt.getInstanceList();
	}
	@PostMapping("/addNewInstance")
	public GenericResponseModel addNewInstance(@RequestBody CompanyInstanceDTO dto) {
		GenericResponseModel model = new GenericResponseModel();
		boolean res = companyInstanceMgmt.addNewInstance(dto);
		model.setStatus(1);
		model.setMessage("Op Success " + res);
		return model;
	}	
	@PostMapping("/updateCompanyInstanceMapping")
	public GenericResponseModel updateCompanyInstanceMapping(@RequestBody UpdateCompanyInstanceMappingDTO dto) {
		GenericResponseModel model = new GenericResponseModel();
		// Update user company mapping
		model.setStatus(1);
		boolean res = companyInstanceMgmt.updateCompanyInstanceMapping(dto);
		model.setMessage("Op success " + res);
		return model;
	}
}
