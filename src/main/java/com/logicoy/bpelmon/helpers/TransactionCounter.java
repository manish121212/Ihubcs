package com.logicoy.bpelmon.helpers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
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
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailsResponse;
import com.logicoy.bpelmon.models._TransactionSourceModel;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.Utils;

@Component
public class TransactionCounter {

	@Autowired
	private Utils utils;
	private RestHighLevelClient eClient = null;
	private Logger logger = Logger.getLogger(this.getClass());
	@Autowired
	private TransactionUtils transactionUtils;
	@Autowired
	AppConstants appConstants;
	int trialCount3 = 0;
	private static final SimpleDateFormat SDF_ES = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	/**
	 * Calculate total transactions with status for every service
	 * 
	 * @param reqModel
	 *            Request Body from controller of type RequestModel
	 * @return List of type ServiceTransactionDetailModel
	 */
	public List<ServiceTransactionDetailModel> calculateUniqueTransactionsPerService(RequestModel reqModel, int ch) {
		logger.info("Calculating unique transactions");
		List<ServiceTransactionDetailModel> dataList = new ArrayList<>();
		ServiceTransactionDetailModel model = null;

		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId());
		/** BELOW LINE CAUSED PROD FAILURE. KEEP COMMENTED */
//		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, false, SortOrder.DESC);
		
		/** FIX FOR ABOVE. KEEP UNCOMMENTED*/
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, false, SortOrder.DESC);
		builder.query(utils.getRangeQuery(reqModel));

		AggregationBuilder uniqueServiceAgg = AggregationBuilders.terms(AppConstants.SU_NAME)
				.field(AppConstants.SU_NAME + ".keyword").size(Integer.MAX_VALUE);
		AggregationBuilder statusAgg = AggregationBuilders.terms(AppConstants.STATUS).field(AppConstants.STATUS);
		
		AggregationBuilder latestTime = AggregationBuilders.max("latest_status").field("event_time");
		
		statusAgg.subAggregation(latestTime);
		uniqueServiceAgg.subAggregation(statusAgg);
		builder.aggregation(uniqueServiceAgg);
		searchRequest.source(builder);

		try {
			SearchResponse resp = eClient.search(searchRequest);
			Terms uniqueServiceTerms = resp.getAggregations().get(AppConstants.SU_NAME);
			logger.info("UniqueServiceTerms: " + uniqueServiceTerms.getBuckets().size());
			for (Bucket serviceBucket : uniqueServiceTerms.getBuckets()) {
				model = new ServiceTransactionDetailModel();
				Terms statusTerms = serviceBucket.getAggregations().get(AppConstants.STATUS);
				long completed = statusTerms.getBucketByKey("completed") != null
						? statusTerms.getBucketByKey("completed").getDocCount()
						: 0;
				long faulted = statusTerms.getBucketByKey("faulted") != null
						? statusTerms.getBucketByKey("faulted").getDocCount()
						: 0;
				long started = statusTerms.getBucketByKey("started") != null
						? statusTerms.getBucketByKey("started").getDocCount()
						: 0;

				model.setSuName(serviceBucket.getKeyAsString());
				model.setCompleted(completed);
				model.setFaulted(faulted);
				model.setStarted(started);
				model.setTransactionType("");
				model.setStatus(this.getServiceStatus(serviceBucket.getKeyAsString(), reqModel.getClientId()));
				Max latestEvent = statusTerms.getBucketByKey("completed") != null ? statusTerms.getBucketByKey("completed").getAggregations().get("latest_status") : null;
				model.setLastEventTime(latestEvent != null ? latestEvent.getValueAsString() : "");
				if(ch == 1)
					model.setAverageTransactionTime(getAvgTransactionTime(serviceBucket.getKeyAsString(), reqModel));
				
				dataList.add(model);
			}
			logger.info("Active Integrations: " + dataList.size());
			// Query bpel_mon index to get unique service list.
			List<String> uniqueServiceListBpel = this.getServiceListFromBpelMonEvent(eClient, reqModel.getClientId());
			logger.info("Total Integrations: " + uniqueServiceListBpel.size());
			List<ServiceTransactionDetailModel> finalDataList = new ArrayList<>();
			ServiceTransactionDetailModel finalModel = new ServiceTransactionDetailModel();
			for(String serviceName: uniqueServiceListBpel) {
				ServiceTransactionDetailModel bpelServiceName = dataList.stream().filter(p -> {
					return p.getSuName().equalsIgnoreCase(serviceName);
				}).findAny().orElse(null);
				if(bpelServiceName != null) {
//					logger.info("Service exists");
					finalModel = bpelServiceName;
				} else {
					finalModel.setAverageTransactionTime(0);
					finalModel.setCompleted(0);
					finalModel.setFaulted(0);
					finalModel.setLastEventTime(this.getServiceLastEventTime(serviceName, reqModel.getClientId()));
					finalModel.setStarted(0);
					finalModel.setStatus(this.getServiceStatus(serviceName, reqModel.getClientId()));
					finalModel.setSuName(serviceName);
					finalModel.setTransactionType("");
				}
				finalDataList.add(finalModel);
				finalModel = new ServiceTransactionDetailModel();
			}
			
			return finalDataList;
		} catch(RuntimeException e) {
			throw e;
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("TransactionCounter::calculateUniqueTransactionsPerService()", e);
		}
		return null;
	}
	private String getServiceLastEventTime(String serviceName, String clientId) {
		
		SearchRequest searchRequest = utils.createTransactionCountIndexSearchRequest(clientId);
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, false, SortOrder.DESC);
		builder.query(QueryBuilders.matchQuery(AppConstants.SU_NAME + ".keyword", serviceName));
		AggregationBuilder statusAgg = AggregationBuilders.terms(AppConstants.STATUS).field(AppConstants.STATUS);
		AggregationBuilder latestTime = AggregationBuilders.max("latest_status").field("event_time");
		statusAgg.subAggregation(latestTime);
		builder.aggregation(statusAgg);
		searchRequest.source(builder);
		try {
			SearchResponse resp = eClient.search(searchRequest);
			Terms latestStatus = resp.getAggregations().get(AppConstants.STATUS);
			Max latestEvTime = latestStatus.getBucketByKey("completed") != null ? latestStatus.getBucketByKey("completed").getAggregations().get("latest_status") : null;
			return latestEvTime != null ? latestEvTime.getValueAsString() : "";
			} catch (IOException e) {
				e.printStackTrace();
			}
		return "";
	}
	
	private List<String> getServiceListFromBpelMonEvent(RestHighLevelClient eClient, String clientId) {
		SearchRequest searchRequest = utils.createTransactionCountIndexSearchRequest(clientId);
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, false, SortOrder.DESC);
		AggregationBuilder uniqueSu = AggregationBuilders.terms("uniqueSu").field(AppConstants.SU_NAME + ".keyword").size(100);
		builder.aggregation(uniqueSu);
		builder.query(QueryBuilders.rangeQuery("event_time").gte(appConstants.getEsMaxRange()));
		searchRequest.source(builder);
		List<String> uniqueServiceList = new ArrayList<>();
		
		try {
			SearchResponse resp = eClient.search(searchRequest);
			Terms uniqueSuTerms = resp.getAggregations().get("uniqueSu");
			for(Bucket suName: uniqueSuTerms.getBuckets()) {
				uniqueServiceList.add(suName.getKeyAsString());
			}
		} catch(RuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("TransactionCounter::getServiceListFromBpelMonEvent()", e);
		}
//		uniqueServiceList.sort((p1, p2) -> {
//			return p1.compareTo(p2);
//		});
		return uniqueServiceList;
	}
	private long getAvgTransactionTime(String suName, RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = utils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
//		builder.fetchSource(new String[] {"event_time", "transaction_id"}, null);
		builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel)).must(QueryBuilders.matchQuery(AppConstants.SU_NAME + ".keyword", suName)));
		
		searchRequest.source(builder);
		
		try {
			SearchResponse searchResp = eClient.search(searchRequest);
			SearchHit hits[] = searchResp.getHits().getHits();
			
			List<_TransactionSourceModel> sourceList = transactionUtils.createSourceList(hits, 0, SortOrder.DESC);
			List<Long> avgTimeList = new ArrayList<>();
			String startTime = "";
			String endTime = "";
			int i = 0;
			while(i < sourceList.size()) {
				_TransactionSourceModel outerData = sourceList.get(i);
				String transactionId = outerData.getTransactionId();
				endTime = outerData.getEventTime();
				for(int j = i + 1; j < sourceList.size() ; j++) {
					_TransactionSourceModel compareData = sourceList.get(j);
					
					if(compareData.getTransactionId().equalsIgnoreCase(transactionId)) {
						startTime = compareData.getEventTime();
						i = j;
						
					}
				}
				i++;
				// Get time difference and add to list
				if(!startTime.isEmpty()) {
					Date startTimeDate = SDF_ES.parse(startTime);
					Date endTimeDate = SDF_ES.parse(endTime);
				
					long diffInMillies = Math.abs(endTimeDate.getTime() - startTimeDate.getTime());
					long diff = TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
					System.out.println("End Date: " + endTimeDate.getTime() + " Matched: " + startTimeDate.getTime() + " Diff: " + diff);
					avgTimeList.add(diff);
				}	
			}
			// Get average milliseconds
			long total = 0L;
			for(long timeDiff : avgTimeList) {
				total += timeDiff;
			}
			return avgTimeList.size() > 0 ? (total / avgTimeList.size()) : 0;
			
		} catch(RuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("TransactionCounter::getAvgTransactionTime()", e);
		}
		return 0L;
	}

	/**
	 * Find transactions for a given service, return results in pages
	 * 
	 * @param reqModel
	 *            RequestModel type from controller
	 * @param start
	 *            Start index
	 * @param length
	 *            length of data to return
	 * @param search
	 *            Search parameters if any
	 * @return Object of type ServiceTransactionDetailsResponse
	 */
	public ServiceTransactionDetailsResponse getTransactionsForService(RequestModel reqModel, int start, int length,
			String search) {
		logger.info("Getting transactions for: " + reqModel.getServiceUnitName());
		ServiceTransactionDetailsResponse response = new ServiceTransactionDetailsResponse();
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = null;
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		if (search.isEmpty()) {
			searchRequest = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId());
			String includes[] = { "transaction_id" };
			builder.fetchSource(includes, null);
			builder.from(start);
			builder.size(length);

			builder.query(QueryBuilders.boolQuery()
					.must(QueryBuilders.matchQuery(AppConstants.SU_NAME + ".keyword", reqModel.getServiceUnitName()))
					.must(utils.getRangeQuery(reqModel)));

			searchRequest.source(builder);

			try {
				SearchResponse resp = eClient.search(searchRequest);
				long totalHits = resp.getHits().totalHits;
				SearchHit hits[] = resp.getHits().getHits();
				List<String> transactionIdList = this.createTransactionIdListFromHits(hits);

				List<_TransactionSourceModel> dataList = utils
						.combineData(this.getTransactionsForId(transactionIdList, reqModel));

				response.setData(dataList);
				response.setRecordsTotal(totalHits);
				response.setRecordsFiltered(totalHits);
				response.setCurrentPageStart(start);
				response.setSearchLength(length);
				logger.info("Transactions for: " + reqModel.getServiceUnitName() + " Total: " + totalHits
						+ " list size: " + dataList.size());
				return response;

			} catch(RuntimeException e) {
				throw e;
			} catch (Exception e) {
				logger.error("TransactionCounter::getTransactionsForService", e.getCause());
			} finally {
				utils.closeElasticConnection();
			}
		} else {
			// Search for query string in elastic
			return this.searchInElastic(search, reqModel, start, length);
		}
		return null;
	}

	/**
	 * Search in service_transaction index for given term
	 * 
	 * @param searchParam
	 *            Search string
	 * @param reqModel
	 *            RequestModel from controller
	 * @return Returns list of type _TransactionSourceModel
	 */
	private ServiceTransactionDetailsResponse searchInElastic(String searchParam, RequestModel reqModel, int start,
			int length) {

		logger.info("Searching in elastic for: " + searchParam);
		ServiceTransactionDetailsResponse response = new ServiceTransactionDetailsResponse();
		eClient = utils.getElasticClient(reqModel.getClientId());
		int ch = 0;
		if (searchParam.equalsIgnoreCase("faulted") || searchParam.equalsIgnoreCase("started")
				|| searchParam.equalsIgnoreCase("completed")) {
			ch = 1;
		}
		final String searchQuery = searchParam;
		SearchRequest searchReq = utils
				.createTransactionIndexSearchRequest(reqModel.getClientId().toLowerCase().trim());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, false, SortOrder.DESC);

		builder.query(
				QueryBuilders
						.boolQuery().should(
								QueryBuilders.boolQuery()
										.must(QueryBuilders
												.multiMatchQuery(searchParam, AppConstants.TRANSACTION_ID + ".keyword",
														AppConstants.STATUS, AppConstants.INSTANCE_ID + ".keyword",
														"primary_tracking_value", "primary_tracking_display"))
										.must(QueryBuilders.matchQuery("su_name.keyword",
												reqModel.getServiceUnitName()))
										.must(utils.getRangeQuery(reqModel)))
						.should(QueryBuilders.boolQuery()
								.must(QueryBuilders
										.queryStringQuery("*" + this.stripSpecialChars(searchParam).toLowerCase() + "*")
										.defaultField("showable_data").analyzeWildcard(true))
								.must(QueryBuilders.matchQuery("su_name.keyword", reqModel.getServiceUnitName()))
								.must(utils.getRangeQuery(reqModel))
				// .should(QueryBuilders.boolQuery()
				// .must(QueryBuilders.multiMatchQuery(searchParam.toLowerCase(),
				// "primary_tracking_value","primary_tracking_display"
				// ))
				// .must(QueryBuilders.matchQuery("su_name", reqModel.getServiceUnitName()))
				// .must(utils.getRangeQuery(reqModel)))
				));

		AggregationBuilder instanceAggregation = AggregationBuilders.terms("unique_trans")
				.field(AppConstants.TRANSACTION_ID + ".keyword").size(Integer.MAX_VALUE)
				.order(BucketOrder.aggregation("max_event", false));
		AggregationBuilder maxEventAgg = AggregationBuilders.max("max_event").field("event_time");
		instanceAggregation.subAggregation(maxEventAgg);
		builder.aggregation(instanceAggregation);
		searchReq.source(builder);
		try {
			SearchResponse resp = eClient.search(searchReq);
			logger.info("Search list doc size: " + resp.getHits().getTotalHits());
			Terms transactionIdTerms = resp.getAggregations().get("unique_trans");

			List<String> transactionIdList = new ArrayList<>();
			List<? extends Bucket> dataBuckets = transactionIdTerms.getBuckets();
			long totalFilteredRecords = dataBuckets.size();

			dataBuckets = dataBuckets.subList(start < dataBuckets.size() ? start : dataBuckets.size(),
					(start + length) < dataBuckets.size() ? (start + length) : dataBuckets.size());

			for (Bucket model : dataBuckets) {
				transactionIdList.add(model.getKeyAsString());
			}
			// Query elastic once more to get all transactions for corresponding transaction

			List<_TransactionSourceModel> finalList = null;

			if (ch == 0) {
				finalList = utils.combineData(this.getTransactionsForId(transactionIdList, reqModel));
			} else {
				finalList = this.getTransactionsForId(transactionIdList, reqModel).stream()
						.filter(p -> p.getStatus().equalsIgnoreCase(searchQuery)).collect(Collectors.toList());
			}

			// Set values in model
//			response.setData(finalList);
			response.setData(utils.combineData(this.getTransactionsForId(transactionIdList, reqModel)));
			response.setRecordsFiltered(totalFilteredRecords);
			response.setCurrentPageStart(start);
			response.setRecordsTotal(this.getTotalTransactionsCountForService(reqModel));
			response.setSearchLength(length);
			logger.info(
					"Search complete: Total records: " + response.getRecordsTotal() + " Data list:" + finalList.size());
			return response;

		} catch(RuntimeException e) {
			throw e;
		}
		catch (Exception e) {

			logger.error("TransactionCounter::searchInElastic()", e);
		} finally {
			utils.closeElasticConnection();
		}
		return null;
	}

	/**
	 * Gets status of any service; 0 if un-deployed/faulted , 1 if running
	 *
	 * @param suName
	 * @return
	 */
	public int getServiceStatus(String suName, String clientId) {
//		logger.info("Getting service status for " + suName);

		eClient = utils.getElasticClient(clientId);
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
//			logger.info("Latest status: " + latestStatus);
			switch (latestStatus.toLowerCase()) {
			case "undeployed":
				return 0;
			case "shutdown":
				return 0;
			default:
				return 1;
			}
		} catch (IOException | ElasticsearchStatusException e) {
			logger.debug(e.getMessage());
		} finally {
			utils.closeElasticConnection();
		}
		return 1;
	}

	/**
	 * Creates a list of transaction id from search response hits array
	 * 
	 * @param hits
	 *            Array of type SearchHit from response
	 * 
	 * @return returns a list containing transaction identities
	 */
	public List<String> createTransactionIdListFromHits(SearchHit hits[]) {
		List<String> transactionIdList = new ArrayList<>();
		for (int i = 0; i < hits.length; i++) {
			transactionIdList.add(hits[i].getSourceAsMap().get(AppConstants.TRANSACTION_ID).toString());
		}
		return transactionIdList;
	}

	/**
	 * Finds all records for every transaction id present in input list. Pass this
	 * array to utils.combineData to club similiar transactions.
	 * 
	 * @param transactionIdList
	 *            List of type String containing distinct transaction identities
	 * @param reqModel
	 *            Request Model from controller
	 *           
	 * @return List of type _TransactionSourceModel
	 */
	public List<_TransactionSourceModel> getTransactionsForId(List<String> transactionIdList, RequestModel reqModel) {

		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchReq = utils
				.createTransactionIndexSearchRequest(reqModel.getClientId().toLowerCase().trim());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.ASC);
		String excludes[] = { "raw_payload", "transaction_xml", "engine_id", "transaction_type", "var_id" };
		builder.fetchSource(null, excludes);		

		builder.query(QueryBuilders.termsQuery(AppConstants.TRANSACTION_ID + ".keyword", transactionIdList));
		searchReq.source(builder);
		// logger.info("Transactions query: " + builder.query().toString());
		try {
			SearchResponse resp = eClient.search(searchReq);
			List<_TransactionSourceModel> returnList = transactionUtils
					.createSourceListNoOrder(resp.getHits().getHits(), 0);
			return returnList;
		} catch (IOException e) {
			logger.debug(e.getMessage());
		} finally {
			utils.closeElasticConnection();
		}
		return new ArrayList<>();
	}

	private String stripSpecialChars(String search) {
		return search.replaceAll("[\\.\\/\\?\\*\\?\\^]", "");
	}

	private long getTotalTransactionsCountForService(RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = null;
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, false, SortOrder.DESC);

		searchRequest = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId());
		String includes[] = { "transaction_id" };
		builder.fetchSource(includes, null);
		// AggregationBuilder totalTransactions =
		// AggregationBuilders.cardinality("total_transactions")
		// .field(AppConstants.TRANSACTION_ID +
		// ".keyword").precisionThreshold(Integer.MAX_VALUE);
		// builder.aggregation(totalTransactions);

		builder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery(AppConstants.SU_NAME, reqModel.getServiceUnitName()))
				.must(utils.getRangeQuery(reqModel)));

		searchRequest.source(builder);

		try {
			SearchResponse resp = eClient.search(searchRequest);
			return resp.getHits().totalHits;

		} catch (IOException e) {
			logger.debug(e.getMessage());
		} finally {
			utils.closeElasticConnection();
		}
		return 0;
	}

	/**
	 * Returns last 10 transactions for the client
	 * 
	 * @param reqModel
	 *            RequestModel object from controller
	 * @return list of type _TransactionSourceModel
	 */
	public Future<List<_TransactionSourceModel>> getTopTenTransactions(RequestModel reqModel) {
		logger.info("Getting top 10 transactions");
		CompletableFuture<List<_TransactionSourceModel>> completableFuture = new CompletableFuture<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.submit(() -> {
			eClient = utils.getElasticClient(reqModel.getClientId());
			SearchRequest searchRequest = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId());
			SearchSourceBuilder builder = utils.createBasicSearchBuilder(10, true, SortOrder.DESC);
			builder.query(utils.getRangeQuery(reqModel));
			searchRequest.source(builder);

			try {
				SearchResponse response = eClient.search(searchRequest);
				logger.info("Hits: " + response.getHits().totalHits);
				if (response.getHits().getHits() != null) {
					completableFuture.complete(
							transactionUtils.createSourceList(response.getHits().getHits(), 0, SortOrder.DESC));
				} else {
					completableFuture.complete(new ArrayList<>());
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
				completableFuture.complete(null);
			} finally {
				utils.closeElasticConnection();
			}
			return null;
		});
		executor.shutdown();
		return completableFuture;
	}

	/**
	 * Calculates running transactions for a particular client
	 * 
	 * @param reqModel
	 *            RequestModel object from controller
	 * @return number of type long
	 */
	public Future<Long> getRunningTransactions(RequestModel reqModel) {
		CompletableFuture<Long> completableFuture = new CompletableFuture<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.submit(() -> {
			eClient = utils.getElasticClient(reqModel.getClientId());
			SearchRequest searchRequest = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId());
			SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, false, SortOrder.DESC);
			builder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(AppConstants.STATUS, "started"))
					.must(utils.getRangeQuery(reqModel)));
			searchRequest.source(builder);

			try {
				SearchResponse response = eClient.search(searchRequest);
				completableFuture.complete(response.getHits().totalHits);
			} catch (IOException e) {
				logger.error(e.getMessage());
			} finally {
				utils.closeElasticConnection();
			}
			return null;
		});
		executor.shutdown();
		return completableFuture;
	}

	/**
	 * Returns sub service transactions for a particular transaction
	 * 
	 * @param reqModel
	 *            RequestModel object from controller
	 * @return List of type _TransactionSourceModel
	 */
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
			logger.info("Sub transaction list size: " + dataList.size());

			dataList = utils.combineSubTransactionData(dataList);

			return dataList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			if (trialCount3++ < 5) {
				return this.getSubServiceTransactions(reqModel);
			}
		} finally {
			utils.closeElasticConnection();
		}
		return null;
	}
}
