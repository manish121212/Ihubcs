package com.logicoy.bpelmon.DAO;

import java.io.File;
import java.util.List;

import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models._TransactionSourceModel;

public interface ReportRequest {

	List<_TransactionSourceModel> generateReport(RequestModel reqModel);
	List<_TransactionSourceModel> generateReportNew(RequestModel reqModel);
	File generateExcelReport(List<_TransactionSourceModel> transactionList, RequestModel reqModel);
}
