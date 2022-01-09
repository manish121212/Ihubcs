package com.logicoy.bpelmon.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.logicoy.bpelmon.helpers.TransactionCounter;
import com.logicoy.bpelmon.helpers.TransactionUtils;
import com.logicoy.bpelmon.models.ActiveSUModel;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceIntegrationsData;
import com.logicoy.bpelmon.models.ServiceIntegrationsModel;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.AppLogger;
import com.logicoy.bpelmon.utils.Utils;

@Repository
public class SURequestImpl implements SURequest {

	@Autowired
	private Utils utils;
	@Autowired
	private TransactionUtils transUtils;
	@Autowired
	TransactionCounter transactionCounter;
	@Autowired
	AppConstants appConstants;
	private RestHighLevelClient eClient = null;
	private AppLogger LOGGER = new AppLogger(Logger.getLogger(this.getClass().getName()));
	private int trialCount = 0;
	private int trialCount1 = 0;

	@Override
	public ActiveSUModel getServiceIntegrations(RequestModel reqModel) {

		
		// Build search request
		SearchRequest searchReq = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId()); 
		// TODO Uncomment above for performance change swap in
//		SearchRequest searchReq = utils.createTransactionIndexSearchRequest(reqModel.getClientId());
		
		/** BELOW LINE CAUSED PROD FAILURE. KEEP COMMENTED */
//		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, false, SortOrder.DESC);
		/** FIX FOR ABOVE. KEEP UNCOMMENTED*/
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, false, SortOrder.DESC);
//		builder.query(QueryBuilders.rangeQuery("event_time").gte(appConstants.getEsMaxRange()));

//		builder.query(utils.getRangeQuery(reqModel));

		// AggregationBuilder statusCount =
		// AggregationBuilders.terms("status_count").field("status");
		AggregationBuilder suCount = AggregationBuilders.terms("su_count").field("su_name.keyword")
				.size(100);
		// builder.aggregation(statusCount);
		builder.aggregation(suCount);
		searchReq.source(builder);

		eClient = utils.getElasticClient(reqModel.getClientId());
		try {
			SearchResponse resp = eClient.search(searchReq);
			Terms suAgg = resp.getAggregations().get("su_count");
			List<? extends Bucket> suBuckets = suAgg.getBuckets();
			LOGGER.logInfo("Total unique services " + suBuckets.size());
			// long startedSU = aggTerms.getBucketByKey("started") != null
			// ? aggTerms.getBucketByKey("started").getDocCount()
			// : suAgg.getBuckets().size();
			// long inactiveSU = aggTerms.getBucketByKey("shutdown") != null
			// ? aggTerms.getBucketByKey("shutdown").getDocCount()
			// : 0;
			long startedSU = suBuckets.size();
			
			long inactiveSU = 0;
			// Get Service status
			for(Bucket suBucket: suBuckets) {
				if(transactionCounter.getServiceStatus(suBucket.getKeyAsString(), reqModel.getClientId()) == 0) {
					inactiveSU++;
				}
			}
			// logger.logInfo("Count : " + initializedSU + " : " + undeployedSU);

			// numOfHits = initializedSU - undeployedSU;
			ActiveSUModel model = new ActiveSUModel();
			model.setStartedSU(startedSU);
			model.setInactiveSU(inactiveSU);
			// model.setSourceList(utils.createSourceModelList(hits, 0, SortOrder.DESC));
			// LOGGER.logInfo("getServiceIntegrations query successful");
			trialCount = 0;
			return model;
			// return resp.getAggregations();
		} catch (Exception e) {
			LOGGER.logError("SURequestImpl::getServiceIntegrations() " + e.getMessage() + "  Caused by: " + e.getCause());

			if (trialCount++ < 3) {
				return this.getServiceIntegrations(reqModel);
			}
			return null;
		} finally {
			utils.closeElasticConnection();
		}
	}

	@Override
	public List<ServiceIntegrationsModel> getOverallIntegrations(RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		SearchRequest searchReq = transUtils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), true, SortOrder.DESC);
		builder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("status", "started"))
				.must(utils.getRangeQuery(reqModel)));
		AggregationBuilder sunit_count = AggregationBuilders.terms("su_count").field("su_name.keyword")
				.size(Integer.MAX_VALUE).subAggregation(
						AggregationBuilders.terms("bpel_count").field("bpel_id.keyword").size(Integer.MAX_VALUE));
		builder.aggregation(sunit_count);
		searchReq.source(builder);

		try {
			SearchResponse resp = eClient.search(searchReq);
			Terms su_count = resp.getAggregations().get("su_count");
			Map<String, Set<String>> suBpelMap = this.getUniqueSU(su_count);
			ServiceIntegrationsModel siModel = null;
			List<ServiceIntegrationsModel> siList = new ArrayList<>();
			List<ServiceIntegrationsData> siData = new ArrayList<>();
			for (Map.Entry<String, Set<String>> entry : suBpelMap.entrySet()) {
				siData = this.getServiceIntegrationsForBpel(entry.getValue(), reqModel);
				siModel = new ServiceIntegrationsModel();
				siModel.setSuName(entry.getKey());
				siModel.setData(siData);
				siData = new ArrayList<>();
				siList.add(siModel);
			}
			trialCount = 0;
			// createGraphData(transUtils.createSourceList(hits, 0, SortOrder.DESC),
			// reqModel);
			return siList;

		} catch (Exception e) {

			LOGGER.logError("Exception: " + e.getMessage() + "  Caused by: " + e.getCause());
			if (trialCount1++ < 3) {
				return this.getOverallIntegrations(reqModel);
			}
		} finally {
			utils.closeElasticConnection();
		}
		return null;
	}

	private Map<String, Set<String>> getUniqueSU(Terms terms) {
		Map<String, Set<String>> suBpelMap = new HashMap<>();
		Set<String> bpelList = new HashSet<>();

		List<? extends Bucket> keys = terms.getBuckets();
		for (Bucket bucket : keys) {
			String key = bucket.getKeyAsString();
			Terms subTerms = bucket.getAggregations().get("bpel_count");
			for (Bucket bucketInner : subTerms.getBuckets()) {
				String[] vals = bucketInner.getKeyAsString().replace("[", "").replace("]", "").split(",");
				for (String val : vals) {
					bpelList.add(val.trim());
				}
			}
			suBpelMap.put(key, bpelList);
		}
		return suBpelMap;
	}

	private List<ServiceIntegrationsData> getServiceIntegrationsForBpel(Set<String> bpelList, RequestModel reqModel) {
		eClient = utils.getElasticClient(reqModel.getClientId());
		ServiceIntegrationsData data = new ServiceIntegrationsData();
		List<ServiceIntegrationsData> dataList = new ArrayList<>();
		SearchRequest searchReq = utils.createTransactionIndexSearchRequest(reqModel.getClientId());
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(reqModel.getRecordSize(), true, SortOrder.DESC);
		builder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.multiMatchQuery(
						bpelList.toString().substring(1, bpelList.toString().length() - 1).replaceAll(",", ""),
						"bpel_id"))
				.must(utils.getRangeQuery(reqModel)));
		AggregationBuilder instance_agg = AggregationBuilders.cardinality("instance_count")
				.field("instance_id.keyword");
		builder.aggregation(instance_agg);
		searchReq.source(builder);
		try {
			SearchResponse resp = eClient.search(searchReq);
			Cardinality instanceNum = resp.getAggregations().get("instance_count");
			data.setBpelName(bpelList.toString());
			data.setBpelInstances(instanceNum.getValueAsString());
			dataList.add(data);
		} catch (IOException e) {
			LOGGER.logError("Exception: " + e.getMessage() + "  Caused by: " + e.getCause());
			return this.getServiceIntegrationsForBpel(bpelList, reqModel);
		} finally {
			utils.closeElasticConnection();
		}
		return dataList;
	}
}
