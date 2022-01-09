package com.logicoy.bpelmon.services;

import org.elasticsearch.client.RestHighLevelClient;

public interface ElasticConnectionPool {

	public RestHighLevelClient getConnection(String clientId);
	public RestHighLevelClient getConnection() throws RuntimeException;
	public boolean release(RestHighLevelClient client);
	public int getPoolSize();
	public void shutdown();
}
