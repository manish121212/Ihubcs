package com.logicoy.bpelmon.DAO;

import java.util.List;

import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.ReprocessConfigDTO;
import com.logicoy.bpelmon.models.ReprocessConfigModel;

public interface ReprocessConfigDAO {

	public List<ReprocessConfigDTO> getConfiguration(ReprocessConfigModel configModel);
	public GenericResponseModel updateConfiguration(ReprocessConfigDTO config);
	public GenericResponseModel addConfiguration(ReprocessConfigDTO config);
	public List<ReprocessConfigDTO> getConfigurationForClient(String clientId);
}
