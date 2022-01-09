package com.logicoy.bpelmon.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.logicoy.bpelmon.DAO.FTPServiceRequest;
import com.logicoy.bpelmon.models.FileDetails;
import com.logicoy.bpelmon.models.FtpConfigurationModel;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RequestModel;

@RestController
@RequestMapping("/api/file")
public class FileController {
	@Autowired
	FTPServiceRequest ftpService;

	@PostMapping("/getFileList")
	public List<FileDetails> getFileList(@RequestBody RequestModel reqModel) {
		return ftpService.getFileList(reqModel);
	}
	@PostMapping("/getFTPConfiguration")
	public List<FtpConfigurationModel> getConfiguration(@RequestBody RequestModel reqModel) {
		return ftpService.getConfiguration(reqModel);
	}
	@PostMapping("/updateFTPConfiguration")
	public GenericResponseModel updateConfiguration(@RequestBody RequestModel reqModel) {
		return ftpService.updateConfiguration(reqModel);
	}
}
