package com.logicoy.bpelmon.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.logicoy.bpelmon.DAO.TransactionRequest;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RawPayload;
import com.logicoy.bpelmon.models.ReprocessConfigDTO;
import com.logicoy.bpelmon.models.ReprocessConfigModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailsResponse;
import com.logicoy.bpelmon.models.TotalTransactionRequestsModel;
import com.logicoy.bpelmon.models._TransactionSourceModel;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
	@Autowired
	TransactionRequest transReq;

	@PostMapping("/getTotalTransactions")
	private TotalTransactionRequestsModel getTotalTransactions(@RequestBody RequestModel reqModel) {
		if (reqModel != null)
			return transReq.getTotalTransactions(reqModel);
		else
			return null;
	}

	@PostMapping("/getServiceTransactionDetails")
	private List<ServiceTransactionDetailModel> getServiceTransactionDetails(@RequestBody RequestModel reqModel) {
		if (reqModel != null)
			return transReq.getServiceTransactionDetails(reqModel, 0);
		else
			return null;
	}

	@PostMapping("/getServiceTransactionTypeList")
	private List<_TransactionSourceModel> getServiceTransactionTypeList(@RequestBody RequestModel reqModel) {
		return reqModel != null ? transReq.getServiceTransactionTypeDetailsList(reqModel) : null;
	}

	@PostMapping("/getServiceTransactionTypeListTest")
	private ServiceTransactionDetailsResponse getServiceTransactionTypeListTest(@RequestBody RequestModel reqModel,
			int start, int length, String search) {
		System.out.println("Search" + search.toString());
		int multiplyFactor = 100;

		return reqModel != null && search != null
				? transReq.getServiceTransactionTypeDetailsListTest(reqModel, start, length, search, multiplyFactor)
				: null;
	}

	@PostMapping("/getSubServiceTransactions")
	private List<_TransactionSourceModel> getSubServiceTransactions(@RequestBody RequestModel reqModel) {
		return reqModel != null ? transReq.getSubServiceTransactions(reqModel) : null;
	}

	@PostMapping("/getRunningTransactions")
	private long getRunningTransactions(@RequestBody RequestModel reqModel) {

		return transReq.getActiveTransactions(reqModel);
	}

	@PostMapping("/getReprocessConfig")
	private List<ReprocessConfigDTO> getReprocessConfig(@RequestBody ReprocessConfigModel reqModel) {
		return transReq.getReprocessConfiguration(reqModel);
	}

	@PostMapping("/reprocessTransaction")
	private GenericResponseModel reprocessTransaction(@RequestBody ReprocessConfigDTO reprocessData) {
		return transReq.reprocessTransaction(reprocessData);
	}

	@PostMapping("/getRawPayload")
	private List<RawPayload> getRawPayload(@RequestBody RequestModel reqModel) {
		return transReq.getRawPayload(reqModel);
	}

}
