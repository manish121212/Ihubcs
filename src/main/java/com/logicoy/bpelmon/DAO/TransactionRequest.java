package com.logicoy.bpelmon.DAO;

import java.util.List;

import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RawPayload;
import com.logicoy.bpelmon.models.ReprocessConfigDTO;
import com.logicoy.bpelmon.models.ReprocessConfigModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailsResponse;
import com.logicoy.bpelmon.models.TotalTransactionRequestsModel;
import com.logicoy.bpelmon.models._TransactionSourceModel;

public interface TransactionRequest {
	public TotalTransactionRequestsModel getTotalTransactions(RequestModel reqModel);

	public List<ServiceTransactionDetailModel> getServiceTransactionDetails(RequestModel reqModel, int ch);

	public ServiceTransactionDetailsResponse getServiceTransactionTypeDetailsListTest(RequestModel reqModel, int start, int length, String search, int multiplyFactor);
	
	public List<_TransactionSourceModel> getServiceTransactionTypeDetailsList(RequestModel reqModel);

	public List<_TransactionSourceModel> getSubServiceTransactions(RequestModel reqModel);

	public long getActiveTransactions(RequestModel reqModel);
	
	public List<ReprocessConfigDTO> getReprocessConfiguration(ReprocessConfigModel reqModel);
	
	public GenericResponseModel reprocessTransaction(ReprocessConfigDTO reprocessData);

	public GenericResponseModel updateConfiguration(ReprocessConfigDTO config);
	
	public List<RawPayload> getRawPayload(RequestModel reqModel);
//	public ServiceTransactionDetailsResponse getTransactionsNew(RequestModel reqModel, int start, int length, String search);
}
