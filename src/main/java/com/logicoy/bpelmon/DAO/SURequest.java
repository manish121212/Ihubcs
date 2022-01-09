package com.logicoy.bpelmon.DAO;

import java.util.List;

import com.logicoy.bpelmon.models.ActiveSUModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceIntegrationsModel;

public interface SURequest {

	public ActiveSUModel getServiceIntegrations(RequestModel reqModel);

	public List<ServiceIntegrationsModel> getOverallIntegrations(RequestModel reqModel);
}
