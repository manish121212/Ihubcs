package com.logicoy.bpelmon.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

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
import com.logicoy.bpelmon.services.ThreadPoolService;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.AppLogger;
import com.logicoy.bpelmon.utils.FileReprocessUtility;
import com.logicoy.bpelmon.utils.FileTransferService;
import com.logicoy.bpelmon.utils.Utils;

public class TransactionRequestImplOld implements TransactionRequest {

	@Autowired
	Utils utils;
	@Autowired
	TransactionUtils transactionUtils;
	@Autowired
	ReprocessConfigDAO reprocessConfig;
	@Autowired
	SoapService exchangeService;
	@Autowired
	FileTransferService ftpService;
	@Autowired
	FileReprocessUtility fileReprocessUtil;
	@Autowired
	ThreadPoolService tPoolService;
	@Autowired
	AppConstants appConst;
	@Autowired
	TransactionCounter transactionCounter;
	private AppLogger LOGGER = new AppLogger(Logger.getLogger(this.getClass().getName()));

	private int trialCount = 0;
	private int trialCount2 = 0;
	private int trialCount3 = 0;

	private static RestHighLevelClient eClient = null;

	@Override
	public TotalTransactionRequestsModel getTotalTransactions(final RequestModel reqModel) {

		// LOGGER.logInfo("Getting total transactions for client: " +
		// reqModel.getClientId().toLowerCase().trim());
		eClient = utils.getElasticClient(reqModel.getClientId());
		TotalTransactionRequestsModel model = new TotalTransactionRequestsModel();
		SearchRequest searchReq = transactionUtils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(50, true, SortOrder.DESC);
		builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel)));
		String includes[] = {};
		String excludes[] = {"raw_payload", "transaction_xml", "transaction_type" };
		builder.fetchSource(includes, excludes);
		builder.from(0);
		builder.size(50);
		searchReq.source(builder);
		try {
			SearchResponse resp = eClient.search(searchReq);
			// LOGGER.logInfo("========== Received data....Creating source list==========
			// ");
			List<_TransactionSourceModel> sourceList = transactionUtils.createSourceList(resp.getHits().getHits(), 0,
					SortOrder.DESC);
			// LOGGER.logInfo("Source list created....size: " + sourceList.size());
			// LOGGER.logInfo("========== Combining data ==========");
			List<_TransactionSourceModel> mainList = utils.combineData(sourceList);
			// LOGGER.logInfo("========== Data combine finished ==========");
			// Terms uniqueTrans = resp.getAggregations().get("unique_transactions");
			long completed = mainList.stream().filter(p -> p.getStatus().equalsIgnoreCase("completed"))
					.collect(Collectors.toList()).size();
			long started = mainList.stream().filter(p -> p.getStatus().equalsIgnoreCase("started"))
					.collect(Collectors.toList()).size();
			long faulted = mainList.stream().filter(p -> p.getStatus().equalsIgnoreCase("faulted"))
					.collect(Collectors.toList()).size();
			model.setTotalCompleted(completed);
			model.setTotalRequests(started + completed + faulted);
			model.setTotalFaulted(faulted);
			model.setSourceList(mainList.subList(0,
					reqModel.getRecordSize() <= mainList.size() ? reqModel.getRecordSize() : mainList.size()));
			trialCount = 0;
			// LOGGER.logInfo("All operations finished. Return response");
			// this.getTotalTransaction(reqModel);
			return model;
		} catch (IOException e) {
			LOGGER.logInfo(e.getMessage());
			if (trialCount++ < 3) {
				return this.getTotalTransactions(reqModel);
			}
			return null;
		}
	}

	@Override
	public List<ServiceTransactionDetailModel> getServiceTransactionDetails(RequestModel reqModel, int ch) {
		return this.findTransactions(reqModel);
		// return this.transactionCounter.calculateUniqueTransactions(reqModel);
		// return null;
	}

	@Override
	public List<_TransactionSourceModel> getServiceTransactionTypeDetailsList(RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchReq = transactionUtils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		// LOGGER.logInfo("Service unit name: " + reqModel.getServiceUnitName());
		builder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("su_name", reqModel.getServiceUnitName()))
				// .must(QueryBuilders.matchQuery("transaction_type.keyword",
				// reqModel.getTransactionType()))
				.must(utils.getRangeQuery(reqModel)));

		TermsAggregationBuilder status_agg = AggregationBuilders.terms("status_terms").field("status")
				.order(BucketOrder.aggregation("max_time", false));
		MaxAggregationBuilder maxAgg = AggregationBuilders.max("max_time").field("event_time");
		status_agg.subAggregation(maxAgg);
		String includes[] = {};
		String excludes[] = { "raw_payload", "transaction_xml" };
		builder.fetchSource(includes, excludes);
		builder.aggregation(status_agg);
		searchReq.source(builder);
		List<_TransactionSourceModel> dataList = null;
		List<_TransactionSourceModel> finalList = null;
		// LOGGER.logInfo("====== Query result ======");
		// LOGGER.logInfo(searchReq.source().query().toString());
		// LOGGER.logInfo(builder.toString());
		try {
			SearchResponse resp = eClient.search(searchReq);

			SearchHit[] hits = resp.getHits().getHits();
			// LOGGER.logInfo("Total hits: " + hits.length);
			dataList = transactionUtils.createSourceList(hits, 0, SortOrder.ASC);
			// LOGGER.logInfo("Data List size: " + dataList.size());
			finalList = utils.combineData(dataList);
			// dataList.addAll(faultedDataList);
			trialCount2 = 0;
			return finalList.size() > 0 ? finalList : null;

		} catch (IOException | NullPointerException e) {

			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
			if (trialCount2++ < 5) {
				return this.getServiceTransactionTypeDetailsList(reqModel);
			}
		} finally {
			dataList = null;
			finalList = null;
		}
		return null;
	}

	@Override
	public ServiceTransactionDetailsResponse getServiceTransactionTypeDetailsListTest(RequestModel reqModel, int start,
			int length, String search, int multiplyFactor) {
		ServiceTransactionDetailsResponse response = new ServiceTransactionDetailsResponse();
		// LOGGER.logInfo("============== Params ==============");
		// LOGGER.logInfo("Start: " + start + " Length: " + length);
		// LOGGER.logInfo("Search value: " + search);
		// LOGGER.logInfo("============== Params ==============");

		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchReq = utils.createTransactionIndexSearchRequest(reqModel.getClientId());
		
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		// LOGGER.logInfo("Service unit name: " + reqModel.getServiceUnitName());
		builder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("su_name.keyword", reqModel.getServiceUnitName()))
				.must(utils.getRangeQuery(reqModel)));
		String include[] = {};
		String exclude[] = { "raw_payload", "transaction_xml" };
		builder.fetchSource(null, exclude);
		TermsAggregationBuilder status_agg = AggregationBuilders.terms("status_terms").field("status");
		builder.aggregation(status_agg);
		if (length != 0) {
			builder.from(start != 0 ? start * multiplyFactor : start);
			builder.size(((length * multiplyFactor) - 1));
		}
		searchReq.source(builder);
		List<_TransactionSourceModel> fullList = null;
		try {
			
			fullList = this.getServiceTransactionTypeDetailsList(reqModel);

			LOGGER.logInfo("Full size: " + fullList.size());
			response.setRecordsTotal(fullList.size());
			if (search.isEmpty()) {
				// LOGGER.logInfo("Search empty");
				int finalIndex = (start + length) < fullList.size() ? (start + length) : fullList.size();
				response.setData(fullList.size() > 0 ? fullList.subList(start, finalIndex) : new ArrayList<>());
				response.setRecordsFiltered(fullList.size());
			} else {
				// Search in elastic

				List<_TransactionSourceModel> filteredList = this.searchInElastic(search, reqModel);
				int finalIndex = (start + length) < filteredList.size() ? (start + length) : filteredList.size();
				response.setData(filteredList.subList(start, finalIndex));
				response.setRecordsFiltered(filteredList.size());
				if (filteredList.size() == 0) {
					response.setRecordsTotal(0);
				}
			}
			trialCount2 = 0;
			return response;

		} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
			if (trialCount2++ < 5) {
				return this.getServiceTransactionTypeDetailsListTest(reqModel, start, length, search, multiplyFactor);
			}
		}
		return null;
	}

	@Override
	public List<_TransactionSourceModel> getSubServiceTransactions(RequestModel reqModel) {

		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchReq = transactionUtils.createSubTransactionSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		builder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("primary_tracking_value.keyword", reqModel.getPrimaryTrackingValue()))
				.must(QueryBuilders.matchQuery("instance_id.keyword", reqModel.getInstanceId()))
				.must(utils.getRangeQuery(reqModel)));
		searchReq.source(builder);
		try {
			SearchResponse resp = eClient.search(searchReq);
			List<_TransactionSourceModel> dataList = transactionUtils.createSourceListNoOrder(resp.getHits().getHits(),
					0);
			LOGGER.logInfo("Sub transaction list size: " + dataList.size());

			dataList = utils.combineSubTransactionData(dataList);
			
			return dataList;
		} catch (IOException e) {
			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
			if (trialCount3++ < 5) {
				return this.getSubServiceTransactions(reqModel);
			}
		}
		return null;
	}

	@Override
	public long getActiveTransactions(RequestModel reqModel) {
		
//		eClient = utils.getElasticClient(reqModel.getClientId());
//		SearchRequest request = transactionUtils.createTransactionIndexSearchRequest(reqModel.getClientId());
//		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
//		String excludes[] = {"transaction_xml", "raw_payload", "transaction_type", "engine_id" };
//		builder.fetchSource(null, excludes);
//		builder.query(utils.getRangeQuery(reqModel));
//		request.source(builder);
//		try {
//			SearchResponse resp = eClient.search(request);
//			// LOGGER.logInfo("List size: " + resp.getHits().getHits().length);
//			if (resp.getHits().getHits().length > 0) {
//				List<_TransactionSourceModel> transactionList = transactionUtils
//						.createSourceList(resp.getHits().getHits(), 0, SortOrder.DESC);
//				List<_TransactionSourceModel> transactions = utils.combineData(transactionList);
//
//				return transactions.stream().filter(p -> p.getStatus().equalsIgnoreCase("started"))
//						.collect(Collectors.toList()).size();
//			} else {
//				return 0;
//			}
//		} catch (IOException e) {
//			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
//		}
//		return 0;
		List<ServiceTransactionDetailModel> transList = this.findTransactions(reqModel);
		int count = 0;
		for(int i = 0 ; i < transList.size() ; i++) {
			count += transList.get(i).getStarted();
		}
		return count;
	}

	/**
	 * Gets status of any service; 0 if un-deployed/faulted , 1 if running
	 *
	 * @param suName
	 * @return
	 */
	private int getServiceStatus(String suName, String clientId) {

		eClient = utils.getElasticClient(clientId);
//		SearchRequest request = utils.createBpelMonIndexSearchRequest(clientId);
//		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, true, SortOrder.DESC);
//		builder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("status", "undeployed"))
//				.must(QueryBuilders.matchQuery("su_name", suName)));
//
//		request.source(builder);
//		try {
//			SearchResponse resp = eClient.search(request);
//			if (resp.getHits().getTotalHits() == 0) {
//				return 1;
//			} else {
//				return 0;
//			}
//		} catch (IOException e) {
//			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
//		}
//		return 0;
		// New Functionality
		SearchRequest request = utils.createBpelMonIndexSearchRequest(clientId);
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, true, SortOrder.DESC);
		builder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("event_type", "bp_service_unit"))
				.must(QueryBuilders.matchQuery("su_name.keyword", suName)));

		request.source(builder);
		try {
			SearchResponse resp = eClient.search(request);
			if(resp.getHits().getHits().length == 0) { 
				return 1;
			}
			String latestStatus = (String) resp.getHits().getHits()[0].getSourceAsMap().get("status");
			LOGGER.logInfo("Latest status: " + latestStatus);
			switch (latestStatus.toLowerCase()) {
			case "undeployed":
				return 0;
			case "shutdown":
				return 0;
			default:
				return 1;
			}
		} catch (IOException | ElasticsearchStatusException e) {
			e.printStackTrace();
		} finally {
			utils.closeElasticConnection();
		}
		return 1;
	}

	@Override
	public List<ReprocessConfigDTO> getReprocessConfiguration(ReprocessConfigModel configModel) {
		// TODO Auto-generated method stub
		return reprocessConfig.getConfiguration(configModel);
	}

	@Override
	public GenericResponseModel reprocessTransaction(ReprocessConfigDTO reprocessData) {
		GenericResponseModel response = new GenericResponseModel();
		response.setStatus(1);
		response.setMessage("Transaction Reprocessed");
		if (reprocessData.getTransactionType() == null) {
			reprocessData.setTransactionType("https");
		}
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
						reprocessData.getServiceEndpoint().trim(), reprocessData.getTransactionDataTemplate(), true,
						"Basic", exchangeService.getBase64String(username, password), sendAuth,
						reprocessData.getContentType(), reprocessData.getAcceptType());
				if (res == null) {
					// Some error occured
					response.setStatus(0);
					response.setMessage("Something went wrong. Please try again.");
				} else {
					response.setMessage(res);
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
			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
			response.setStatus(0);
			response.setMessage(e.getMessage());
		}
		return response;
	}

	@Override
	public GenericResponseModel updateConfiguration(ReprocessConfigDTO config) {
		// TODO Auto-generated method stub
		return reprocessConfig.updateConfiguration(config);
	}

	private List<_TransactionSourceModel> searchInElastic(String searchParam, RequestModel reqModel) {
		// searchParam = "*" + searchParam + "*";
		// LOGGER.logInfo("Searching for: " + searchParam);
		eClient = utils.getElasticClient(reqModel.getClientId());
		int ch = 0;
		if (searchParam.trim().equalsIgnoreCase("fault"))
			searchParam = "faulted";
		if (searchParam.equalsIgnoreCase("fault") || searchParam.equalsIgnoreCase("faulted")
				|| searchParam.equalsIgnoreCase("started") || searchParam.equalsIgnoreCase("completed")) {
			ch = 1;
		}
		SearchRequest searchReq = utils
				.createTransactionIndexSearchRequest(reqModel.getClientId().toLowerCase().trim());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);

		// If not partial match
		builder.query(QueryBuilders
				.boolQuery().should(
						QueryBuilders.boolQuery()
								.must(QueryBuilders.multiMatchQuery(searchParam,
										AppConstants.TRANSACTION_ID + ".keyword", AppConstants.STATUS,
										"primary_tracking_display", "primary_tracking_value", AppConstants.INSTANCE_ID
												+ ".keyword"))
								.must(QueryBuilders.matchQuery("su_name", reqModel.getServiceUnitName())))
				.should(QueryBuilders.boolQuery()
						.must(QueryBuilders
								.queryStringQuery("*" + this.stripSpecialChars(searchParam).toLowerCase() + "*")
								.defaultField("showable_data").analyzeWildcard(true))
						.must(QueryBuilders.matchQuery("su_name", reqModel.getServiceUnitName())))
				.mustNot(QueryBuilders.rangeQuery("event_time").lte(reqModel.getStartDate())));

		String includes[] = {};
		String excludes[] = { "transaction_xml", "raw_payload" };
		builder.fetchSource(includes, excludes);
		searchReq.source(builder);
		try {
			SearchResponse resp = eClient.search(searchReq);
			LOGGER.logInfo("Search list doc size: " + resp.getHits().getTotalHits());
			List<_TransactionSourceModel> dataList = transactionUtils.createSourceList(resp.getHits().getHits(), 0,
					SortOrder.DESC);
			List<String> transactionIdList = new ArrayList<>();
			for (_TransactionSourceModel model : dataList) {
				transactionIdList.add(model.getTransactionId());
			}
			// Query elastic once more to get all transactions for corresponding transaction
			// id
			List<_TransactionSourceModel> finalList = null;

			if (ch == 0) {
				finalList = this.getTransactionsForId(transactionIdList, reqModel);
			} else {
				finalList = dataList;
			}
			return finalList;

		} catch (NullPointerException | IOException e) {
			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
		}
		return new ArrayList<_TransactionSourceModel>();
	}

	private String stripSpecialChars(String search) {
		return search.replaceAll("[\\:\\.\\~\\/\\?\\*\\?\\^]", "");
	}

	private List<_TransactionSourceModel> getTransactionsForId(List<String> transactionId, RequestModel reqModel) {

		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchReq = utils
				.createTransactionIndexSearchRequest(reqModel.getClientId().toLowerCase().trim());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		// builder.query(QueryBuilders.multiMatchQuery(transactionId,
		// "transaction_id.keyword").operator(Operator.OR));
		builder.query(QueryBuilders.termsQuery("transaction_id.keyword", transactionId));
		searchReq.source(builder);

		try {
			SearchResponse resp = eClient.search(searchReq);
			List<_TransactionSourceModel> returnList = utils
					.combineData(transactionUtils.createSourceList(resp.getHits().getHits(), 0, SortOrder.DESC));
			return returnList;
		} catch (IOException e) {
			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
		}
		return new ArrayList<>();
	}

	private List<ServiceTransactionDetailModel> findTransactions(RequestModel reqModel) {
		LOGGER.logInfo("Finding total transaction details");
		eClient = utils.getElasticClient(reqModel.getClientId());
		ServiceTransactionDetailModel model = null;
		List<ServiceTransactionDetailModel> dataList = new ArrayList<>();
		SearchRequest searchReq = transactionUtils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, false, SortOrder.DESC);
		builder.query(utils.getRangeQuery(reqModel));
		AggregationBuilder suAgg = AggregationBuilders.terms("unique_service").field(AppConstants.SU_NAME + ".keyword")
				.size(Integer.MAX_VALUE)
				.subAggregation(AggregationBuilders.terms(AppConstants.STATUS).field(AppConstants.STATUS)
						.size(Integer.MAX_VALUE).subAggregation(AggregationBuilders.cardinality("unique_transaction")
								.field(AppConstants.TRANSACTION_ID + ".keyword").precisionThreshold(10000)));
		builder.aggregation(suAgg);
		searchReq.source(builder);

		try {
			SearchResponse resp = eClient.search(searchReq);

			Terms suNameAgg = resp.getAggregations().get("unique_service");
			LOGGER.logInfo("Unique services: " + suNameAgg.getBuckets().size());
			for (Bucket suBucket : suNameAgg.getBuckets()) {
				model = new ServiceTransactionDetailModel();
				model.setSuName(suBucket.getKeyAsString());
				long started = 0;
				long completed = 0;
				long faulted = 0;
				Terms statusTerms = suBucket.getAggregations().get(AppConstants.STATUS);

				Bucket startedBucket = statusTerms.getBucketByKey("started");
				Bucket completedBucket = statusTerms.getBucketByKey("completed");
				Bucket faultedBucket = statusTerms.getBucketByKey("faulted");

				Cardinality startedTransactions = startedBucket != null
						? startedBucket.getAggregations().get("unique_transaction")
						: null;
				Cardinality completedTransactions = completedBucket != null
						? completedBucket.getAggregations().get("unique_transaction")
						: null;
				Cardinality faultedTransactions = faultedBucket != null
						? faultedBucket.getAggregations().get("unique_transaction")
						: null;

				started = startedTransactions != null ? startedTransactions.getValue() : 0;
				completed = completedTransactions != null ? completedTransactions.getValue() : 0;
				faulted = faultedTransactions != null ? faultedTransactions.getValue() : 0;

				started = (started - (completed + faulted)) >= 0 ? started - (completed + faulted) : 0;

				model.setStarted(started);
				model.setCompleted(completed);
				model.setFaulted(faulted);
				model.setStatus(this.getServiceStatus(model.getSuName(), reqModel.getClientId()));
				dataList.add(model);

			}

			return dataList;

		} catch (IOException e) {
			LOGGER.logError(e.getMessage() + " Caused by: " + e.getCause());
		}
		return dataList;
	}

	@Override

	public List<RawPayload> getRawPayload(RequestModel reqModel) {
List<RawPayload> payload = new ArrayList<>();
		
		RestHighLevelClient eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest request = utils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		builder.fetchSource(new String[]{"raw_payload","event_time", "event_seq_no"}, null);
		builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel))
				.must(QueryBuilders.matchQuery(AppConstants.TRANSACTION_ID + ".keyword", reqModel.getTransactionId())));
		builder.size(1);
		request.source(builder);

		try {
			SearchResponse resp = eClient.search(request);
			List<_TransactionSourceModel> dataList = transactionUtils.createSourceList(resp.getHits().getHits(), 0,
					SortOrder.DESC);
			for(_TransactionSourceModel data: dataList) {
				payload.add(new RawPayload(data.getRawPayload(), data.getEventTime()));
			}
//			payload.setRawPayload(dataList.get(0).getRawPayload());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			utils.closeElasticConnection();
		}
		return payload;
	}
}
