package com.logicoy.bpelmon.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.logicoy.bpelmon.helpers.TransactionUtils;
import com.logicoy.bpelmon.models.AlertResponseModel;
import com.logicoy.bpelmon.models.BpelMonitoringModel;
import com.logicoy.bpelmon.models.BpelUniqueInstanceData;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceAlertData;
import com.logicoy.bpelmon.models.TotalRequestsModel;
import com.logicoy.bpelmon.models.UniqueBpelPaginatedResponse;
import com.logicoy.bpelmon.models._AlertNotificationSourceModel;
import com.logicoy.bpelmon.models._SourceModel;
import com.logicoy.bpelmon.models._TransactionSourceModel;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.AppLogger;
import com.logicoy.bpelmon.utils.Utils;

@Repository
public class BPELRequestImpl implements BPELRequest {
	@Autowired
	private Utils utils;
	@Autowired
	TransactionUtils transUtils;
	private RestHighLevelClient eClient = null;
	private int trialCount = 0, trialCount1 = 0, trialCount2 = 0, trialCount3 = 0, trialCount4 = 0;
	private AppLogger LOGGER = new AppLogger(Logger.getLogger(this.getClass().getName()));
	private String TOTAL_COUNT = "total_count";
	private String FAULTED = "faulted";
	private String RUNNING = "running";

	@Override
	public TotalRequestsModel getTotalRequests(RequestModel reqModel) {

		eClient = utils.getElasticClient(reqModel.getClientId());
		TotalRequestsModel totalReqModel = new TotalRequestsModel();

		SearchRequest searchRequest = utils.createBpelMonIndexSearchRequest(reqModel.getClientId());

		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), false, SortOrder.DESC);

		builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel))
				.must(QueryBuilders.matchQuery(AppConstants.EVENT_TYPE, "BP_INSTANCE")));

		AggregationBuilder aggBuilder = AggregationBuilders.terms(TOTAL_COUNT).field(AppConstants.STATUS);
		builder.aggregation(aggBuilder);
		searchRequest.source(builder);
		SearchResponse resp = null;

		// Now fetching from bpel mon event
		try {
			resp = eClient.search(searchRequest);
			Terms count = resp.getAggregations().get(TOTAL_COUNT);
			SearchHit[] searchHit = resp.getHits().getHits();

			if (searchHit.length != 0) {
				// Requests present.
				long runningRequests = count.getBucketByKey(RUNNING) != null
						? count.getBucketByKey(RUNNING).getDocCount()
						: 0;
				long faultedRequests = count.getBucketByKey(FAULTED) != null
						? count.getBucketByKey(FAULTED).getDocCount()
						: 0;
				long completedRequests = count.getBucketByKey("completed") != null
						? count.getBucketByKey("completed").getDocCount()
						: 0;

				totalReqModel.setTotalRequests(runningRequests);
				totalReqModel.setTotalFaulted(faultedRequests / 2);
				totalReqModel.setTotalCompleted(completedRequests);
				// totalReqModel.setSourceList(null);
				trialCount = 0;
				LOGGER.logInfo("Unique bpel instances: " + totalReqModel.getTotalCompleted());
				return totalReqModel;

			} else {
				// No requests
				totalReqModel.setTotalCompleted(0);
				totalReqModel.setTotalFaulted(0);
				totalReqModel.setTotalRequests(0);
				totalReqModel.setTotalTerminated(0);
				return totalReqModel;
			}

		} catch (Exception e) {
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
			if (trialCount++ < 3) {
				return this.getTotalRequests(reqModel);
			} else {
				return null;
			}
		} finally {
			utils.closeElasticConnection();
		}
	}

	@Override
	public List<_SourceModel> getInstanceDetails(RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = utils.createTransactionIndexSearchRequest(reqModel.getClientId());

		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), true, SortOrder.ASC);
		LOGGER.logInfo("Searching for instance id: " + reqModel.getInstanceId());
		builder.query(QueryBuilders.matchQuery(AppConstants.INSTANCE_ID, reqModel.getInstanceId().trim())
				.operator(Operator.AND));
		searchRequest.source(builder);
		SearchResponse resp = null;
		try {
			resp = eClient.search(searchRequest);
			trialCount1 = 0;
			return utils.createSourceModelList(resp.getHits().getHits(), 0, SortOrder.ASC);
		} catch (Exception e) {
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
			if (trialCount1++ < 3) {
				return this.getInstanceDetails(reqModel);
			}
			return null;
		} finally {
			utils.closeElasticConnection();
		}
	}

	@Override
	public TotalRequestsModel getTotalRequestsOnly(RequestModel reqModel) {
		TotalRequestsModel totalReqModel = new TotalRequestsModel();
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = utils.createTransactionIndexSearchRequest(reqModel.getClientId());

		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), false, SortOrder.DESC);

		builder.query(
				QueryBuilders.boolQuery().must(QueryBuilders.multiMatchQuery("faulted running", AppConstants.STATUS))
						.must(utils.getRangeQuery(reqModel)));

		AggregationBuilder aggBuilder = AggregationBuilders.terms(TOTAL_COUNT).field(AppConstants.STATUS);
		builder.aggregation(aggBuilder);
		searchRequest.source(builder);
		SearchResponse resp = null;

		try {
			resp = eClient.search(searchRequest);
			SearchHit[] searchHit = resp.getHits().getHits();
			if (searchHit.length != 0) {
				// Requests present.
				Terms count = resp.getAggregations().get(TOTAL_COUNT);
				long runningRequests = count.getBucketByKey(RUNNING) != null
						? count.getBucketByKey(RUNNING).getDocCount()
						: 0;
				long faultedRequests = count.getBucketByKey(FAULTED) != null
						? count.getBucketByKey(FAULTED).getDocCount()
						: 0;
				totalReqModel.setTotalRequests(runningRequests);
				totalReqModel.setTotalFaulted(faultedRequests / 2);
				trialCount2 = 0;
				return totalReqModel;

			} else {
				// No requests
				totalReqModel.setTotalCompleted(0);
				totalReqModel.setTotalFaulted(0);
				totalReqModel.setTotalRequests(0);
				totalReqModel.setTotalTerminated(0);
				trialCount2 = 0;
				return totalReqModel;
			}
		} catch (Exception e) {
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
			if (trialCount2++ < 3) {
				return this.getTotalRequestsOnly(reqModel);
			}
			return null;
		} finally {
			utils.closeElasticConnection();
		}
	}

	@Override
	public UniqueBpelPaginatedResponse getUniqueBpelInstances(RequestModel reqModel) {
		UniqueBpelPaginatedResponse response = new UniqueBpelPaginatedResponse();
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest request = utils.createBpelCountMgmtSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		builder.from(reqModel.getFetchFrom());
		builder.size(reqModel.getRecordSize());
		if (reqModel.getSearchQuery() != null && !reqModel.getSearchQuery().isEmpty()) {
			// Search parameters found
			builder.query(this.getBpelSearchQueryBuilder(reqModel));
		} else {
			builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel)));
		}
		// AggregationBuilder countAgg =
		// AggregationBuilders.terms("su_count").field("bpel_id.keyword")
		// .size(100);
		// countAgg.subAggregation(
		// AggregationBuilders.terms("instance_count").field("instance_id.keyword").size(1000));
		//
		// builder.aggregation(countAgg);
		request.source(builder);
		List<BpelUniqueInstanceData> bUIDList = new LinkedList<>();
		try {
			SearchResponse resp = eClient.search(request);
			SearchHit[] searchHits = resp.getHits().getHits();
			for (SearchHit hit : searchHits) {
				Map<String, Object> sourceMap = hit.getSourceAsMap();
				String bpelId = sourceMap.get("bpel_id").toString();
				bUIDList.add(new BpelUniqueInstanceData(bpelId.substring(bpelId.lastIndexOf("}") + 1),
						sourceMap.get("instance_id").toString(), (long) sourceMap.get("event_seq_no")));
			}
			response.setData(bUIDList);
			response.setDraw(reqModel.getDraw());
			response.setRecordsTotal((int) resp.getHits().getTotalHits());
			response.setRecordsFiltered((int) resp.getHits().getTotalHits());
			// Terms statusTerms = resp.getAggregations().get("su_count");
			// for (Bucket bucket : statusTerms.getBuckets()) {
			// Terms instanceTerms = bucket.getAggregations().get("instance_count");
			// for (Bucket innerBucket : instanceTerms.getBuckets()) {
			// String bpelId = bucket.getKeyAsString();
			// bUIDList.add(new
			// BpelUniqueInstanceData(bpelId.substring(bpelId.lastIndexOf("}") + 1),
			// innerBucket.getKeyAsString()));
			// }
			// }
			return response;
		} catch (IOException e) {

			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
		} finally {
			utils.closeElasticConnection();
		}
		return null;
	}

	private BoolQueryBuilder getBpelSearchQueryBuilder(RequestModel reqModel) {
		String search = reqModel.getSearchQuery();
		LOGGER.logInfo("Searching for : " + reqModel.getSearchQuery());
		return QueryBuilders.boolQuery()
				.should(QueryBuilders.boolQuery()
						.must(QueryBuilders.matchQuery("bpel_id", search).operator(Operator.OR))
						.must(utils.getRangeQuery(reqModel)))
				.should(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("instance_id.keyword", search))
						.must(utils.getRangeQuery(reqModel)))
				.should(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("status", search))
						.must(utils.getRangeQuery(reqModel)));
	}

	@Override
	public BpelMonitoringModel getBpelTrackingData(RequestModel reqModel) {

		eClient = utils.getElasticClient(reqModel.getClientId());
		LOGGER.logInfo("======================= Bpel Tracking API ========================");
		LOGGER.logInfo("++ Bpel tracking payload ++");
		LOGGER.logInfo(reqModel.toString());
		SearchRequest request = null;
		request = utils.createBpelMonIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		builder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("instance_id.keyword", reqModel.getInstanceId()))
				.must(utils.getRangeQuery(reqModel)));

		AggregationBuilder eventTypeAgg = AggregationBuilders.terms("event_type_agg").field(AppConstants.EVENT_TYPE);
		AggregationBuilder maxTimeAgg = AggregationBuilders.max("event_completion_time").field("event_time")
				.format("epoch_millis");
		AggregationBuilder minTimeAgg = AggregationBuilders.min("event_start_time").field("event_time")
				.format("epoch_millis");

		builder.aggregation(eventTypeAgg);
		builder.aggregation(maxTimeAgg);
		builder.aggregation(minTimeAgg);
		request.source(builder);
		BpelMonitoringModel responseModel = new BpelMonitoringModel();
		List<_SourceModel> mainDataList = null;
		List<_TransactionSourceModel> transactionMainDataList = null;
		try {
			SearchResponse resp = eClient.search(request);

			Terms eventTypeTerms = resp.getAggregations().get("event_type_agg");
			LOGGER.logInfo("Event type aggregation length: " + eventTypeTerms.getBuckets().size());

			long bpelActivities = eventTypeTerms.getBucketByKey("bp_activity") != null
					? eventTypeTerms.getBucketByKey("bp_activity").getDocCount()
					: 0;
			long bpelVariables = eventTypeTerms.getBucketByKey("bp_variable") != null
					? eventTypeTerms.getBucketByKey("bp_variable").getDocCount()
					: 0;
			Max eventCompletionTimeAgg = resp.getAggregations().get("event_completion_time");
			Min eventStartTimeAgg = resp.getAggregations().get("event_start_time");

			mainDataList = utils.createSourceModelList(resp.getHits().getHits(), 0, SortOrder.DESC);
			long endTimeinMillis = 0;
			long startTimeInMillis = 0;
			if (eventCompletionTimeAgg != null) {
				endTimeinMillis = (long) eventCompletionTimeAgg.getValue();
			}
			if (eventStartTimeAgg != null) {
				startTimeInMillis = (long) eventStartTimeAgg.getValue();
			}
			long running_time = TimeUnit.SECONDS.convert(endTimeinMillis - startTimeInMillis, TimeUnit.MILLISECONDS);
			LOGGER.logInfo("Runnint time value: " + running_time + " s");
			LOGGER.logInfo("bpelActivities: " + bpelActivities + " bpelVariables: " + bpelVariables);
			responseModel.setAlerts(getNotifications(reqModel, 0));
			responseModel.setBpelActivities(bpelActivities);
			responseModel.setBpelVariables(bpelVariables);
			responseModel.setCompletionTime(running_time);
			responseModel.setNotifications(getNotifications(reqModel, 1));
			responseModel.setBpelActivityList(mainDataList.stream()
					.filter(p -> p.getEventType().equalsIgnoreCase("bp_activity")).collect(Collectors.toList()));
			responseModel.setBpelVariableList(mainDataList.stream()
					.filter(p -> p.getEventType().equalsIgnoreCase("bp_variable")).collect(Collectors.toList()));
			responseModel.setOutboundInvokes(mainDataList.stream().filter(p -> p.getActivityXpath().contains("invoke"))
					.collect(Collectors.toList()).size());

			// Get transactions from service_transaction index;
			request = transUtils.createTransactionIndexSearchRequest(reqModel.getClientId());
			builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
			builder.query(QueryBuilders.boolQuery()
					.must(QueryBuilders.matchQuery("instance_id.keyword", reqModel.getInstanceId()))
					.must(utils.getRangeQuery(reqModel)));
			AggregationBuilder statusAgg = AggregationBuilders.terms("status_count").field("status");
			builder.aggregation(statusAgg);
			request.source(builder);

			SearchResponse transactionSearchRsponse = eClient.search(request);

			Terms statusTerms = transactionSearchRsponse.getAggregations().get("status_count");
			long started = statusTerms.getBucketByKey("started") != null
					? statusTerms.getBucketByKey("started").getDocCount()
					: 0;

			transactionMainDataList = transUtils.createSourceListNoOrder(transactionSearchRsponse.getHits().getHits(),
					0);
			LOGGER.logInfo("Transaction size: " + transactionSearchRsponse.getHits().getTotalHits() + " object: "
					+ transactionMainDataList.size());
			responseModel.setTransactions(started);
			// No running transactions
			transactionMainDataList = utils.combineData(transactionMainDataList);
			Collections.sort(transactionMainDataList, utils.getEventSequenceComparator());
			responseModel.setTransactionList(transactionMainDataList);

			trialCount3 = 0;
			LOGGER.logInfo("\n\n" + responseModel.toString() + "\n\n");
			return responseModel;
		} catch (IOException e) {
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
			if (trialCount3++ < 5) {

				return this.getBpelTrackingData(reqModel);
			}
		} catch (NumberFormatException ex) {
			LOGGER.logInfo(ex.getMessage() + " " + ex.getCause());
		} finally {
			utils.closeElasticConnection();
		}
		BpelMonitoringModel model = new BpelMonitoringModel();
		model.setAlerts(0);
		model.setCompletionTime(0);
		model.setNotifications(0);
		model.setOutboundInvokes(0);
		model.setTransactions(0);
		model.setBpelActivityList(new ArrayList<_SourceModel>());
		model.setBpelVariableList(new ArrayList<_SourceModel>());
		model.setTransactionList(new ArrayList<_TransactionSourceModel>());
		return model;
	}

	private long getNotifications(RequestModel reqModel, int ch) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = utils.createAlertNotificationSearchRequest(reqModel.getClientId(), ch);

		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), false, SortOrder.DESC);

		builder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("instance_id.keyword", reqModel.getInstanceId()))
				.must(utils.getRangeQuery(reqModel)));
		searchRequest.source(builder);
		try {
			return eClient.search(searchRequest).getHits().totalHits;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.logError("Get alerts/notifications exception: " + e.getMessage());
			return 0;
		} finally {
			utils.closeElasticConnection();
		}
	}

	@Override
	public AlertResponseModel getBpelAlerts(RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = utils.createAlertNotificationSearchRequest(reqModel.getClientId(), 0);
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), true, SortOrder.DESC);

		TermsAggregationBuilder timeAgg = AggregationBuilders.terms("time_agg").field("event_time")
				.format("yyyy-MM-dd HH:mm:ss");
		TermsAggregationBuilder alertAgg = AggregationBuilders.terms("type_agg").field("type");
		timeAgg.subAggregation(alertAgg);
		builder.query(
				QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery()).must(utils.getRangeQuery(reqModel)));
		builder.aggregation(timeAgg);
		searchRequest.source(builder);
		SearchResponse resp = null;
		try {
			resp = eClient.search(searchRequest);
			AlertResponseModel model = new AlertResponseModel();
			List<ServiceAlertData> dataList = null;
			trialCount1 = 0;
			List<_AlertNotificationSourceModel> sourceList = utils
					.createAlertNotificationSourceModelList(resp.getHits().getHits(), 0, SortOrder.DESC);
			model.setSourceList(sourceList);
			Terms instanceTerms = resp.getAggregations().get("time_agg");
			Map<String, ServiceAlertData> innerMap = new HashMap<>();

			for (Bucket term : instanceTerms.getBuckets()) {
				ServiceAlertData innerData = new ServiceAlertData();
				innerData.setSuName("");
				System.out.println("Event time: " + term.getKeyAsString());
				Terms typeTerms = term.getAggregations().get("type_agg");
				Bucket majorDataBucket = typeTerms.getBucketByKey("bp_alert_critical");
				Bucket minorDataBucket = typeTerms.getBucketByKey("bp_alert_major");
				innerData.setMajor(majorDataBucket != null ? majorDataBucket.getDocCount() : 0);
				innerData.setMinor(minorDataBucket != null ? minorDataBucket.getDocCount() : 0);
				ServiceAlertData mapData = innerMap.get(term.getKeyAsString());
				if (mapData != null) {
					mapData.setMajor(mapData.getMajor() + innerData.getMajor());
					mapData.setMinor(mapData.getMinor() + innerData.getMinor());
					innerMap.put(term.getKeyAsString(), mapData);
				} else {
					innerMap.put(term.getKeyAsString(), innerData);
				}
			}
			dataList = new ArrayList<>();
			for (Map.Entry<String, ServiceAlertData> entry : innerMap.entrySet()) {
				ServiceAlertData innerData = new ServiceAlertData();
				innerData.setSuName(getServiceNameForInstaceId(entry.getKey(), reqModel));
				innerData.setMajor(entry.getValue().getMajor());
				innerData.setMinor(entry.getValue().getMinor());
				dataList.add(innerData);
			}
			model.setData(dataList);
			trialCount4 = 0;
			return model;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
			if (trialCount4++ < 3) {
				return this.getBpelAlerts(reqModel);
			}
			return null;
		} finally {
			utils.closeElasticConnection();
		}
	}

	@Override
	public AlertResponseModel getBpelNotifications(RequestModel reqModel) {

		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchRequest = utils.createAlertNotificationSearchRequest(reqModel.getClientId(), 1);

		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), true, SortOrder.DESC);

		builder.query(
				QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery()).must(utils.getRangeQuery(reqModel)));

		TermsAggregationBuilder timeAgg = null;
		TermsAggregationBuilder alertAgg = null;
		if (reqModel.getRecordSize() != 0) {
			builder.size(reqModel.getRecordSize());
			timeAgg = AggregationBuilders.terms("time_agg").field("event_time").format("yyyy-MM-dd HH:mm:ss")
					.size(reqModel.getRecordSize());
			alertAgg = AggregationBuilders.terms("type_agg").field("type").size(reqModel.getRecordSize());
		} else {
			timeAgg = AggregationBuilders.terms("time_agg").field("event_time").format("yyyy-MM-dd HH:mm:ss");
			alertAgg = AggregationBuilders.terms("type_agg").field("type");
		}

		timeAgg.subAggregation(alertAgg);
		builder.query(
				QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery()).must(utils.getRangeQuery(reqModel)));
		builder.aggregation(timeAgg);
		searchRequest.source(builder);
		SearchResponse resp = null;
		LOGGER.logInfo("Fetching notifications");
		try {
			resp = eClient.search(searchRequest);
			LOGGER.logInfo("notifications: " + resp.getHits().totalHits);
			AlertResponseModel model = new AlertResponseModel();
			List<ServiceAlertData> dataList = null;
			trialCount1 = 0;
			List<_AlertNotificationSourceModel> sourceList = utils
					.createAlertNotificationSourceModelList(resp.getHits().getHits(), 0, SortOrder.DESC);
			model.setSourceList(sourceList);
			Terms instanceTerms = resp.getAggregations().get("time_agg");
			Map<String, ServiceAlertData> innerMap = new HashMap<>();

			for (Bucket term : instanceTerms.getBuckets()) {
				ServiceAlertData innerData = new ServiceAlertData();
				// System.out.println("Event time: " + term.getKeyAsString());
				Terms typeTerms = term.getAggregations().get("type_agg");
				Bucket majorDataBucket = typeTerms.getBucketByKey("bp_alert_warning");
				Bucket minorDataBucket = typeTerms.getBucketByKey("bp_alert_minor");
				innerData.setMajor(majorDataBucket != null ? majorDataBucket.getDocCount() : 0);
				innerData.setMinor(minorDataBucket != null ? minorDataBucket.getDocCount() : 0);
				innerData.setTime(term.getKeyAsString());
				ServiceAlertData mapData = innerMap.get(term.getKeyAsString());
				if (mapData != null) {
					mapData.setMajor(mapData.getMajor() + innerData.getMajor());
					mapData.setMinor(mapData.getMinor() + innerData.getMinor());
					innerMap.put(term.getKeyAsString(), mapData);
				} else {

					innerMap.put(term.getKeyAsString(), innerData);
				}
			}
			dataList = new ArrayList<>();
			for (Map.Entry<String, ServiceAlertData> entry : innerMap.entrySet()) {
				ServiceAlertData innerData = new ServiceAlertData();
				innerData.setSuName(entry.getKey());
				innerData.setTime(entry.getKey());
				innerData.setMajor(entry.getValue().getMajor());
				innerData.setMinor(entry.getValue().getMinor());
				dataList.add(innerData);
			}
			model.setData(dataList);
			trialCount4 = 0;
			return model;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			if (e instanceof IllegalStateException) {
				e.printStackTrace();
				// Reinitialize eClient;
				utils.reInitializeElasticConnection();
				if (trialCount4++ < 3)
					return this.getBpelAlerts(reqModel);
				return null;
			}
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
			if (trialCount4++ < 3) {
				return this.getBpelAlerts(reqModel);
			}
			return null;
		} finally {
			utils.closeElasticConnection();
		}
	}

	private String getServiceNameForInstaceId(String key, RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		LOGGER.logInfo("Searching for: " + key);
		SearchRequest searchRequest = utils.createBpelMonIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		builder.query(QueryBuilders.matchQuery("instance_id.keyword", key));
		searchRequest.source(builder);
		try {
			SearchResponse resp = eClient.search(searchRequest);

			SearchHits hits = resp.getHits();
			if (hits.totalHits > 0) {
				SearchHit hit = hits.getAt(0);
				return hit.getFields().get("su_name").getName();
			}
		} catch (IOException e) {
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused By: " + e.getCause());
		} finally {
			utils.closeElasticConnection();
		}

		return key;
	}

	@Override
	public GenericResponseModel updateNotificationsAlerts(RequestModel reqModel) {
		RestHighLevelClient eClient = utils.getElasticClient(reqModel.getClientId());
		GenericResponseModel resp = new GenericResponseModel();
		resp.setStatus(1);
		resp.setMessage("notification/alert updated");
		LOGGER.logInfo("========================== Update Notification/Alert API =============================");
		LOGGER.logInfo("DocId: " + reqModel.getDocId() + " Index: " + reqModel.getIndex() + " Type: "
				+ reqModel.getIndexType());
		UpdateRequest updateRequest = new UpdateRequest(reqModel.getIndex(), reqModel.getIndexType(),
				reqModel.getDocId());
		Map<String, Object> updatedVal = new HashMap<>();
		updatedVal.put("status", reqModel.getUpdateStatus());
		updateRequest.doc(updatedVal);
		eClient.updateAsync(updateRequest, new ActionListener<UpdateResponse>() {

			@Override
			public void onResponse(UpdateResponse response) {
				LOGGER.logInfo("Notification updated: " + response.toString());
			}

			@Override
			public void onFailure(Exception e) {
				LOGGER.logInfo("Notification update failure: " + e.getMessage());
			}
		});
		return resp;
	}
}
