package com.logicoy.bpelmon.controller;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logicoy.bpelmon.DAO.BPELRequest;
import com.logicoy.bpelmon.models.AlertResponseModel;
import com.logicoy.bpelmon.models.BpelMonitoringModel;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.TotalRequestsModel;
import com.logicoy.bpelmon.models.UniqueBpelPaginatedResponse;
import com.logicoy.bpelmon.models._SourceModel;

@RestController
@RequestMapping("/api/bpel")
public class BPELController {

	@Autowired
	BPELRequest bpelReq;

	@PostMapping("/getTotalBpelRequests")
	public TotalRequestsModel getTotalRequests(@RequestBody RequestModel reqModel) {
		if (reqModel != null)
			return bpelReq.getTotalRequests(reqModel);
		else
			return null;
	}

	@PostMapping("/getInstanceDetails")
	public List<_SourceModel> getInstanceDetails(@RequestBody RequestModel reqModel) {
		if (reqModel != null)
			return bpelReq.getInstanceDetails(reqModel);
		else
			return null;
	}

	@PostMapping("/getTotalRequestsOnly")
	public TotalRequestsModel getTotalRequestsOnly(@RequestBody RequestModel reqModel) {
		if (reqModel != null)
			return bpelReq.getTotalRequestsOnly(reqModel);
		else
			return null;
	}

	@PostMapping("/getUniqueBpelInstances")
	public UniqueBpelPaginatedResponse getUniqueBpelInstances(@RequestBody RequestModel reqModel, String draw,
			int start, int length, String search) {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Search query: {0}", search);
		if (reqModel != null) {
			reqModel.setRecordSize(length);
			reqModel.setFetchFrom(start);
			reqModel.setDraw(draw);
			reqModel.setSearchQuery(search);
			return bpelReq.getUniqueBpelInstances(reqModel);
		} else {
			return null;
		}
	}

	@PostMapping("/getBpelTrackingData")
	public BpelMonitoringModel getBpelTrackingData(@RequestBody RequestModel reqModel) {
		if (reqModel != null) {
			return bpelReq.getBpelTrackingData(reqModel);
		}
		return null;
	}

	@PostMapping("/getBpelAlerts")
	public AlertResponseModel getBpelAlerts(@RequestBody RequestModel reqModel) {
		System.out.println("Bpel alert: " + reqModel.toString());
		return bpelReq.getBpelAlerts(reqModel);
	}

	@PostMapping("/getBpelNotifications")
	public AlertResponseModel getBpelNotifications(@RequestBody RequestModel reqModel) {
		if (reqModel != null)
			return bpelReq.getBpelNotifications(reqModel);
		else
			return null;
	}

	@PostMapping("/updateNotifications")
	public GenericResponseModel updateNotifications(@RequestBody RequestModel reqModel) {
		return bpelReq.updateNotificationsAlerts(reqModel);
	}

	@PostMapping("/updateAlerts")
	public GenericResponseModel updateAlerts(@RequestBody RequestModel reqModel) {
		return bpelReq.updateNotificationsAlerts(reqModel);
	}
}
