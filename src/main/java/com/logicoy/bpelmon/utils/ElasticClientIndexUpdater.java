package com.logicoy.bpelmon.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticClientIndexUpdater implements Runnable {
	private String clientId = null;
	private Logger log = Logger.getLogger(this.getClass().getName());
	OnIndexUpdateComplete onIndexUpdateComplete;
	private int portArray[];
	private String hostList[];
	private String protocol;

	public ElasticClientIndexUpdater(String clientId, int portArray[], String hostList[], String protocol,
			OnIndexUpdateComplete onIndexUpdateComplete) {
		super();
		this.clientId = clientId.toLowerCase().trim();
		this.onIndexUpdateComplete = onIndexUpdateComplete;
		this.portArray = portArray;
		this.hostList = hostList;
		this.protocol = protocol;
	}

	@Override
	public void run() {
		HttpHost esHostList[] = new HttpHost[portArray.length];
		for (int i = 0; i < portArray.length; i++) {
			esHostList[i] = new HttpHost(hostList[i], portArray[i], protocol);

		}
		// final CredentialsProvider credentialsProvider = new
		// BasicCredentialsProvider();
		// credentialsProvider.setCredentials(AuthScope.ANY, new
		// UsernamePasswordCredentials("ui-backend", "Welcome321!"));
		// RestClientBuilder builder =
		// RestClient.builder(hostList).setHttpClientConfigCallback(new
		// HttpClientConfigCallback() {
		//
		// @Override
		// public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder
		// httpClientBuilder) {
		// return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		// }
		// });
		RestClientBuilder builder = RestClient.builder(esHostList);
		RestHighLevelClient eClient = new RestHighLevelClient(builder);

		try {
			// Update indices mapping for bpel_mon index
			eClient.getLowLevelClient().performRequest("PUT", "_all/_settings", Collections.emptyMap(), getSettings());
			String[] bPMEIArray = this.getBpelMonEventIndices();
			log.info("================== Updating bpel_mon_event_" + clientId + " ==================");
			for (String field : bPMEIArray) {

				eClient.getLowLevelClient().performRequest("PUT",
						"/bpel_mon_event_" + clientId + "/_mapping/bpel_mon_event_type_" + clientId,
						Collections.emptyMap(), getJson(field));

			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception: {0} caused by: {1}", new Object[] { e.getMessage(), e.getCause() });
		}
		// Update indices mapping for service_transaction index
		log.info("================== Updating service_transaction_" + clientId + " ==================");
		String sTMArray[] = this.getTransactionIndices();
		try {
			for (String field : sTMArray) {

				eClient.getLowLevelClient().performRequest("PUT",
						"/service_transaction_" + clientId + "/_mapping/service_transaction_type_" + clientId,
						Collections.emptyMap(), getJson(field));
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception: {0} caused by: {1}", new Object[] { e.getMessage(), e.getCause() });
		}
		// Update indices for service_sub_transaction index
		log.info("================== Updating service_sub_transaction_" + clientId + " ==================");
		String subArray[] = this.getSubTransactionIndices();
		try {
			for (String field : subArray) {
				eClient.getLowLevelClient().performRequest("PUT",
						"/service_sub_transaction_" + clientId + "/_mapping/service_sub_transaction_type_" + clientId,
						Collections.emptyMap(), getJson(field));
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception: {0} caused by: {1}", new Object[] { e.getMessage(), e.getCause() });
		}
		log.info("================== Updating service_transaction_count_mgmt_" + clientId + " ==================");
		String countIndices[] = this.getTransactionCountIndices();
		try {
			for (String field : countIndices) {

				eClient.getLowLevelClient().performRequest(
						"PUT", "/service_transaction_count_mgmt_" + clientId
								+ "/_mapping/service_transaction_count_type_" + clientId,
						Collections.emptyMap(), getTransCountJson(field));
			}

		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception: {0} caused by: {1}", new Object[] { e.getMessage(), e.getCause() });
		}

		String arr[] = this.getAlertNotificationIndices();
		try {
			for (String field : arr) {

				eClient.getLowLevelClient().performRequest("PUT",
						"/bpel_notification_" + clientId + "/_mapping/bpel_notification_" + clientId + "_type",
						Collections.emptyMap(), getJson(field));

			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception: {0} caused by: {1}", new Object[] { e.getMessage(), e.getCause() });
		}
		log.info("================== Updating alert_notification_" + clientId + " ==================");
		try {
			for (String field : arr) {
				eClient.getLowLevelClient().performRequest("PUT",
						"/bpel_alert_" + clientId + "/_mapping/bpel_alert_" + clientId + "_type",
						Collections.emptyMap(), getJson(field));
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception: {0} caused by: {1}", new Object[] { e.getMessage(), e.getCause() });
		}
		try {
			eClient.close();
		} catch (IOException e1) {
			log.log(Level.SEVERE, "Exception: {0} caused by: {1}", new Object[] { e1.getMessage(), e1.getCause() });
		}
		log.info("+++++++++++++++++++++++ Field data modification complete +++++++++++++++++++++++");
		onIndexUpdateComplete.onIndexUpdate(true);
	}

	private NStringEntity getSettings() {
		String str = "{" + "\"index.max_result_window\":10000000" + "}";
		return new NStringEntity(str, ContentType.APPLICATION_JSON);
	}

	private NStringEntity getJson(String fieldName) {

		String jsonStr = "{" + "\"properties\":" + "{\"" + fieldName + "\":" + "{" + "\"type\": \"text\","
				+ "\"fielddata\": true" + "}" + "}" + "}";
		return new NStringEntity(jsonStr, ContentType.APPLICATION_JSON);
	}

	private NStringEntity getTransCountJson(String fieldName) {
		if (fieldName.equalsIgnoreCase(AppConstants.SU_NAME)
				|| fieldName.equalsIgnoreCase(AppConstants.TRANSACTION_ID)) {
			String jsonStr = "{" + "\"properties\":" + "{\"" + fieldName + "\":" + "{" + "\"type\": \"text\","
					+ "\"fielddata\": true" + "," + "\"fields\": {" + "\"keyword\": {" + "\"type\": \"keyword\"" + "}"
					+ "}" + "}" + "}" + "}";

			log.info("Count JSON : " + jsonStr);
			return new NStringEntity(jsonStr, ContentType.APPLICATION_JSON);
		} else {
			return this.getJson(fieldName);
		}
	}

	private String[] getBpelMonEventIndices() {
		return new String[] { AppConstants.BPEL_ID, AppConstants.EVENT_TYPE, AppConstants.INSTANCE_ID,
				AppConstants.STATUS, AppConstants.SU_NAME };
	}

	private String[] getTransactionIndices() {
		return new String[] { AppConstants.BPEL_ID, AppConstants.INSTANCE_ID, AppConstants.PRIMARY_TRACKING_VALUE,
				AppConstants.STATUS, AppConstants.SU_NAME, AppConstants.TRANSACTION_ID, AppConstants.TRANSACTION_TYPE,
				"showable_data" };
	}

	private String[] getSubTransactionIndices() {
		return new String[] { AppConstants.INSTANCE_ID, AppConstants.PRIMARY_TRACKING_VALUE,
				AppConstants.SECONDARY_TRACKING_VALUE, AppConstants.TRANSACTION_ID };
	}

	private String[] getAlertNotificationIndices() {
		return new String[] { AppConstants.INSTANCE_ID, AppConstants.BPEL_ID, AppConstants.TYPE, AppConstants.STATUS };
	}

	private String[] getTransactionCountIndices() {
		return new String[] { AppConstants.SU_NAME, AppConstants.STATUS, AppConstants.TRANSACTION_ID };
	}

	public interface OnIndexUpdateComplete {
		public void onIndexUpdate(boolean status);
	}
}
