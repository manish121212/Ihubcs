package com.logicoy.bpelmon.DAO;

import java.util.List;

import com.logicoy.bpelmon.models.AlertResponseModel;
import com.logicoy.bpelmon.models.BpelMonitoringModel;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.TotalRequestsModel;
import com.logicoy.bpelmon.models.UniqueBpelPaginatedResponse;
import com.logicoy.bpelmon.models._SourceModel;

public interface BPELRequest {

	/**
	 * Get total BPEL request on service with details
	 * 
	 * @return TotalRequestModel object
	 */
	public TotalRequestsModel getTotalRequests(RequestModel reqModel);

	/**
	 * Get details of a particular request
	 * 
	 * @param instanceId
	 *            whose details are required<String>
	 * @return List of all matching instances
	 */
	public List<_SourceModel> getInstanceDetails(RequestModel reqModel);

	/**
	 * Get total requests on service without details
	 * 
	 * @return TotalRequestsModel object
	 */
	public TotalRequestsModel getTotalRequestsOnly(RequestModel reqModel);

	/**
	 * Get unique instances
	 * 
	 * @param RequestModel
	 *            from pay load
	 * @return BpelUniqueInstance List
	 */
	public UniqueBpelPaginatedResponse getUniqueBpelInstances(RequestModel reqModel);

	/**
	 * Get bpel monitoring data against an instance id
	 * 
	 * @param RequestModel
	 *            from pay load
	 * @return BpelMonitoringModel
	 */
	public BpelMonitoringModel getBpelTrackingData(RequestModel reqModel);

	public AlertResponseModel getBpelAlerts(RequestModel reqModel);

	public AlertResponseModel getBpelNotifications(RequestModel reqModel);

	public GenericResponseModel updateNotificationsAlerts(RequestModel reqModel);
}
