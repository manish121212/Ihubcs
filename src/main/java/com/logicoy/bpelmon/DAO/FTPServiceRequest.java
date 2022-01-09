package com.logicoy.bpelmon.DAO;

import java.util.List;

import com.logicoy.bpelmon.models.FileDetails;
import com.logicoy.bpelmon.models.FtpConfigurationModel;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RequestModel;

public interface FTPServiceRequest {

	public List<FileDetails> getFileList(RequestModel reqModel);
	public List<FtpConfigurationModel> getConfiguration(RequestModel reqModel);
	public GenericResponseModel updateConfiguration(RequestModel reqModel);
}
