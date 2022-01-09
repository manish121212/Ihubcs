package com.logicoy.bpelmon.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppFactory {

	int i = 0;
	@Autowired
	private AppConstants appConst;

	AppLogger logger = new AppLogger(Logger.getLogger(this.getClass().getName()));

	private static RestHighLevelClient elasticClient = null;
	private static RestHighLevelClient elasticClient1 = null;
	private static RestHighLevelClient elasticClient2 = null;
	private static Map<String, RestHighLevelClient> clientPoolMap = new HashMap<>();

	public RestHighLevelClient getElasticClient() {

		if (AppFactory.elasticClient == null) {
			int portArray[] = appConst.getPort();
			HttpHost[] hostList = new HttpHost[portArray.length];
			for (int i = 0; i < portArray.length; i++) {
				hostList[i] = new HttpHost(appConst.getHost()[i], portArray[i], appConst.getProtocol());

			}
			AppFactory.elasticClient = new RestHighLevelClient(RestClient.builder(hostList));

			return AppFactory.elasticClient;
		} else {
			return AppFactory.elasticClient;
		}
	}
	// Un-implemented functionality
	public RestHighLevelClient getNewClientForPool() {
		int portArray[] = appConst.getPort();
		HttpHost[] hostList = new HttpHost[portArray.length];
		for (int i = 0; i < portArray.length; i++) {
			hostList[i] = new HttpHost(appConst.getHost()[i], portArray[i], appConst.getProtocol());

		}
		return new RestHighLevelClient(RestClient.builder(hostList));
	}
	// Un-implemented functionality
	public RestHighLevelClient getElasticClientFromPool(String clientId) {
		
		if(clientPoolMap.get(clientId) != null) {
			return clientPoolMap.get(clientId);
		} else {
			clientPoolMap.put(clientId, getNewClientForPool());
			return clientPoolMap.get(clientId);
		}
	}
	
	public RestHighLevelClient getElasticClientBPEL() {
		if (AppFactory.elasticClient1 != null) {
			return AppFactory.elasticClient1;
		} else {
			List<HttpHost> hostList = new ArrayList<>();
			int portArray[] = appConst.getPort();
			for (int i = 0; i < portArray.length; i++) {
				hostList.add(new HttpHost(appConst.getHost()[i], portArray[i], appConst.getProtocol()));
			}
			AppFactory.elasticClient1 = new RestHighLevelClient(RestClient.builder(hostList.iterator().next()));
			return elasticClient1;
		}
	}

	public RestHighLevelClient getElasticClientTransaction() {
		if (AppFactory.elasticClient2 != null) {
			return AppFactory.elasticClient2;
		} else {
			List<HttpHost> hostList = new ArrayList<>();
			int portArray[] = appConst.getPort();
			for (int i = 0; i < portArray.length; i++) {
				hostList.add(new HttpHost(appConst.getHost()[i], portArray[i], appConst.getProtocol()));
			}
			AppFactory.elasticClient2 = new RestHighLevelClient(RestClient.builder(hostList.iterator().next()));
			return elasticClient2;
		}
	}
	// Un-implemented functionality
	public boolean removeClientFromPool(String clientId) {
		if(clientPoolMap.get(clientId) != null) {
			RestHighLevelClient client =  clientPoolMap.get(clientId);
			try {
				client.close();
			} catch (IOException e) {
				logger.logError(e.getMessage() + " Caused by: " + e.getCause());
			}
			client = null;
			clientPoolMap.remove(clientId);
			return true;
		}
		return false;
	}
}
