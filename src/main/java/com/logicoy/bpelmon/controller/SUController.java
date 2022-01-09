package com.logicoy.bpelmon.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logicoy.bpelmon.DAO.SURequest;
import com.logicoy.bpelmon.models.ActiveSUModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceIntegrationsModel;

@RestController
@RequestMapping("/api/su")
public class SUController {

	@Autowired
	private SURequest suReq;

	@PostMapping("/getServiceIntegrations")
	private ActiveSUModel getServiceIntegrations(@RequestBody RequestModel reqModel) {
		
		if(reqModel != null) {
			System.out.println("SUController.class ==> Getting service transactions");
			return suReq.getServiceIntegrations(reqModel);
		} else {
			return null;
		}
	}

	@PostMapping("/getOverallIntegrations")
	private List<ServiceIntegrationsModel> getOverallIntegrations(@RequestBody RequestModel reqModel) {
		if (reqModel != null)
			return suReq.getOverallIntegrations(reqModel);
		else
			return null;
	}
}
