package com.logicoy.bpelmon.controller;

import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.logicoy.bpelmon.DAO.CompanyManagement;
import com.logicoy.bpelmon.DAO.UserRegistration;
import com.logicoy.bpelmon.models.Authority;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailsResponse;
import com.logicoy.bpelmon.models.TimeZoneDTO;
import com.logicoy.bpelmon.models.UserAuthority;
import com.logicoy.bpelmon.models.UserDTO;
import com.logicoy.bpelmon.models.UserDetails;
import com.logicoy.bpelmon.services.SoapService;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.AppLogger;
import com.logicoy.bpelmon.utils.PasswordUtil;

@RestController
@RequestMapping("/api")
public class MasterController {

	@Autowired
	UserRegistration user;
	@Autowired
	SoapService client;
	@Autowired
	AppConstants appConst;
	@Autowired
	PasswordUtil passwordUtil;
	@Autowired
	CompanyManagement companyManagement;

	AppLogger logger = new AppLogger(Logger.getLogger(this.getClass().getName()));

	@GetMapping("/status")
	public String status() {
		return "{\"status\":\"Backend up and running.\"}";
	}

	@PostMapping("/register")
	public GenericResponseModel registerUser(@RequestBody UserDetails body) {

		int isUserUpdated = user.registerUser(body);
		GenericResponseModel model = new GenericResponseModel();
		if (isUserUpdated == 1) {
			model.setStatus(1);
			model.setMessage("updated");
		} else if (isUserUpdated == 2) {
			model.setStatus(2);
			model.setMessage("Internal server error");
		} else if (isUserUpdated == 3) {
			model.setStatus(3);
			model.setMessage("User name already exist");
		} else if (isUserUpdated == 4) {
			model.setStatus(4);
			model.setMessage("Email already exist");
		} else {
			model.setStatus(0);
			model.setMessage("Failed");
		}
		System.out.println("in the register user " + isUserUpdated);
		return model;
	}

	@GetMapping("/getAllUserDetails")
	public List<UserDetails> getAllUserDetails() {
		logger.logInfo("fetching all user details from db");
		List<UserDetails> userDetais = user.getAllUserDetails();
		return userDetais;

	}

	@PostMapping("/updateExistingUser")
	public GenericResponseModel updateExistingUser(@RequestBody UserDetails details) {
		int isUserUpdated = user.updateExistingUser(details);
		GenericResponseModel model = new GenericResponseModel();
		if (isUserUpdated == 1) {
			model.setStatus(1);
			model.setMessage("updated");
		} else if (isUserUpdated == 2) {
			model.setStatus(2);
			model.setMessage("Internal server error");
		} else if (isUserUpdated == 3) {
			model.setStatus(3);
			model.setMessage("User name already exist");
		} else if (isUserUpdated == 4) {
			model.setStatus(4);
			model.setMessage("Email already exist");
		} else {
			model.setStatus(0);
			model.setMessage("Failed");
		}
		return model;
	}

	@GetMapping("/searchAllUserDetailsByUsername")
	public List<UserDetails> getAllUserDetailsByUsername(@RequestBody UserDetails details) {
		logger.logInfo("fetching all user details from db");
		List<UserDetails> userDetais = user.searchAllUserDetailsByUsername(details);
		return userDetais;

	}

	@GetMapping("/getUserDetailsByUsername")
	public List<UserDetails> getUserDetailsByUsername(@RequestBody UserDetails details) {
		logger.logInfo("fetching all user details from db");
		List<UserDetails> userDetais = user.getUserDetailsByUserName(details);
		return userDetais;

	}

	@GetMapping("/getUserRoles")
	public List<Authority> getUserRoles() {
		logger.logInfo("fetching all roles from db");
		List<Authority> userDetais = user.getAllUserRoles();
		return userDetais;

	}

	@GetMapping("/getCompanyDetails")
	public List<Authority> getCompanyDetails() {
		logger.logInfo("fetching all company details from db");
		List<Authority> userDetais = user.getAllUserRoles();
		return userDetais;

	}

	@PostMapping("/getSelectedUserRoleAuthorities")
	public List<Authority> getSelectedUserRoleAuthorities(@RequestBody UserDetails body) {
		logger.logInfo("fetching selected user roles from db");
		List<Authority> userDetais = user.getSelectedUserRoles(body.getUsername());
		return userDetais;

	}

	@PostMapping("/updateUserRole")
	public GenericResponseModel updateUserRole(@RequestBody UserAuthority body) {

		int isRoleUpdated = user.updateUserRoles(body);
		GenericResponseModel model = new GenericResponseModel();
		System.out.println("the int is " + isRoleUpdated);
		if (isRoleUpdated == 1) {
			model.setStatus(1);
			model.setMessage("updated");
		} else if (isRoleUpdated == 2) {
			model.setStatus(2);
			model.setMessage("Internal server error");
		} else {
			model.setStatus(0);
			model.setMessage("Failed");
		}
		return model;
	}

	@RequestMapping(value = "/testXML", method = RequestMethod.POST, consumes = "text/plain")
	public ResponseEntity<GenericResponseModel> receiveXML(@RequestBody String xml) {
		GenericResponseModel response = new GenericResponseModel();
		response.setStatus(1);
		response.setMessage("success");
		ResponseEntity<GenericResponseModel> resp = new ResponseEntity<GenericResponseModel>(response,
				HttpStatus.ACCEPTED);
		JacksonXmlModule module = new JacksonXmlModule();
		module.setDefaultUseWrapper(false);
		return resp;
	}

	@GetMapping("/testSoap")
	public String checkSoap() {
		return client.sendSoapRequest();
	}

	@GetMapping("/testProperty")
	public String testProperty() {
		return appConst.getTestProperty();
	}

	@PostMapping("/updateProfile")
	public GenericResponseModel updateProfile(@RequestBody UserDTO details) {
		logger.logInfo(details.toString());

		return user.updateUserProfile(details);
	}

	@PostMapping("/updateUserPassword")
	public GenericResponseModel updatePassword(@RequestBody UserDTO details) {
		logger.logInfo("Updating user password!! ");
		return user.changeUserPassword(details.getPassword(), details);
	}

	@PostMapping("/getTimezoneList")
	public List<TimeZoneDTO> getTimezoneList() {
		return companyManagement.getTimezoneList();
	}
	@GetMapping("/getTimeZoneForCode")
	public TimeZoneDTO getTimeZoneForCode(String tzCode) {
		logger.logInfo("Getting TImeZoneDTO for: " + tzCode);
		return companyManagement.getTimeZoneForCode(tzCode);
	}
}
