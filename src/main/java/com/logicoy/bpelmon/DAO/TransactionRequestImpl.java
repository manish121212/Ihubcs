package com.logicoy.bpelmon.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.logicoy.bpelmon.helpers.TransactionCounter;
import com.logicoy.bpelmon.helpers.TransactionUtils;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RawPayload;
import com.logicoy.bpelmon.models.ReprocessConfigDTO;
import com.logicoy.bpelmon.models.ReprocessConfigModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailsResponse;
import com.logicoy.bpelmon.models.TotalTransactionRequestsModel;
import com.logicoy.bpelmon.models._TransactionSourceModel;
import com.logicoy.bpelmon.services.SoapService;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.FileReprocessUtility;
import com.logicoy.bpelmon.utils.FileTransferService;
import com.logicoy.bpelmon.utils.Utils;
@Repository
public class TransactionRequestImpl implements TransactionRequest {
	@Autowired
	TransactionCounter transactionCounter;
	@Autowired
	ReprocessConfigDAO reprocessConfig;
	@Autowired
	Utils utils;
	@Autowired
	SoapService exchangeService;
	@Autowired
	FileReprocessUtility fileReprocessUtil;
	@Autowired
	FileTransferService ftpService;
	@Autowired
	private TransactionUtils transactionUtils;
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public TotalTransactionRequestsModel getTotalTransactions(RequestModel reqModel) {
		TotalTransactionRequestsModel model = new TotalTransactionRequestsModel();
		try {
			model.setSourceList(transactionCounter.getTopTenTransactions(reqModel).get());
			model.setTotalCompleted(0);
			model.setTotalFaulted(0);
			model.setTotalRequests(0);
			model.setTotalTerminated(0);
			return model;
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<ServiceTransactionDetailModel> getServiceTransactionDetails(RequestModel reqModel, int ch) {
		return transactionCounter.calculateUniqueTransactionsPerService(reqModel, ch);
	}

	@Override
	public ServiceTransactionDetailsResponse getServiceTransactionTypeDetailsListTest(RequestModel reqModel, int start,
			int length, String search, int multiplyFactor) {
		return transactionCounter.getTransactionsForService(reqModel, start, length, search);
	}

	@Override
	public List<_TransactionSourceModel> getServiceTransactionTypeDetailsList(RequestModel reqModel) {
		return null;
	}

	@Override
	public List<_TransactionSourceModel> getSubServiceTransactions(RequestModel reqModel) {
		return transactionCounter.getSubServiceTransactions(reqModel);
	}

	@Override
	public long getActiveTransactions(RequestModel reqModel) {
		try {
			return transactionCounter.getRunningTransactions(reqModel).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getMessage());
		}
		return 0;
	}

	@Override
	public List<ReprocessConfigDTO> getReprocessConfiguration(ReprocessConfigModel reqModel) {
		return reprocessConfig.getConfiguration(reqModel);
	}

	@Override
	public GenericResponseModel reprocessTransaction(ReprocessConfigDTO reprocessData) {
		GenericResponseModel response = new GenericResponseModel();
		response.setStatus(1);
		response.setMessage("Transaction Reprocessed");
		// LOGGER.logInfo("==========Transaction Reprocess Object==========");
		// LOGGER.logInfo(reprocessData.toString());
		String username = reprocessData.getUsername().trim();
		String password = reprocessData.getPassword().trim();
		boolean sendAuth = true;
		if (username.isEmpty() || password.isEmpty()) {
			sendAuth = false;
		}
		try {
			if (reprocessData.getTransactionType().equalsIgnoreCase("http")
					|| reprocessData.getTransactionType().equalsIgnoreCase("https")) {
				String res = exchangeService.serviceExchange(reprocessData.getMethod().trim(),
						reprocessData.getServiceEndpoint().trim(), reprocessData.getTransactionDataTemplate(), true, "",
						"", sendAuth, reprocessData.getContentType(), reprocessData.getAcceptType());
				if (res == null) {
					// Some error occured
					response.setStatus(0);
					response.setMessage("Something went wrong. Please try again.");
				}
			} else if (reprocessData.getTransactionType().equalsIgnoreCase("ftp")) {
				if (reprocessData.getXslTransformation() != null) {
					fileReprocessUtil.getTransformedData(reprocessData.getTransactionDataTemplate(),
							reprocessData.getXslTransformation());
				}
			} else if (reprocessData.getTransactionType().equalsIgnoreCase("file")) {
				ftpService.sendFileToHost("", "", username, reprocessData.getServiceEndpoint(), "");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setStatus(0);
			response.setMessage(e.getMessage());
		}
		return response;
	}

	@Override
	public GenericResponseModel updateConfiguration(ReprocessConfigDTO config) {
		return reprocessConfig.updateConfiguration(config);
	}

	@Override
	public List<RawPayload> getRawPayload(RequestModel reqModel) {
//		RawPayload payload = new RawPayload();
		List<RawPayload> payload = new ArrayList<>();
		
		RestHighLevelClient eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest request = utils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		builder.fetchSource(new String[]{"raw_payload","event_time", "event_seq_no"}, null);
		builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel))
				.must(QueryBuilders.matchQuery(AppConstants.TRANSACTION_ID + ".keyword", reqModel.getTransactionId())));
//		builder.size(1);
		request.source(builder);

		try {
			SearchResponse resp = eClient.search(request);
			List<_TransactionSourceModel> dataList = transactionUtils.createSourceList(resp.getHits().getHits(), 0,
					SortOrder.DESC);
			for(_TransactionSourceModel data: dataList) {
				payload.add(new RawPayload(data.getRawPayload(), data.getEventTime()));
			}
//			payload.setRawPayload(dataList.get(0).getRawPayload());
		} catch(RuntimeException e) {
			throw e;
		} catch (IOException e) {
			logger.info("TransactionRequestImpl::getRawPayload", e.getCause());
		} finally {
			utils.closeElasticConnection();
		}
		return payload;
	}

}
