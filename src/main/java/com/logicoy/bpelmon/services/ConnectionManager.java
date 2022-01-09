package com.logicoy.bpelmon.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.logicoy.bpelmon.utils.AppConstants;

@Service
public class ConnectionManager {

	private static final Map<String, RestHighLevelClient> clientPool = new HashMap<>();
	@Autowired
	private AppConstants appConst;
	private int portArray[] = null;
	private static HttpHost[] hostList;
	private Logger logger = null;
	private static RestHighLevelClient eClient;
	private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	@PostConstruct
	private void initPool() {
		this.portArray = appConst.getPort();
		hostList = new HttpHost[portArray.length];
		logger = Logger.getLogger(this.getClass().getName());
		for (int i = 0; i < portArray.length; i++) {
			hostList[i] = new HttpHost(appConst.getHost()[i], portArray[i], appConst.getProtocol());

		}
//		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("ui-backend", "Welcome321!"));
//		
//		RestClientBuilder builder = RestClient.builder(hostList).setHttpClientConfigCallback(new HttpClientConfigCallback() {
//			
//			@Override
//			public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//				return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
//			}
//		});
//		eClient = new RestHighLevelClient(builder);
		
		// TimerTask timerTask = new TimerTask() {
		//
		// @Override
		// public void run() {
		// StringBuilder clientPoolInfo = new StringBuilder();
		// clientPoolInfo.append("\n=========== ELASTIC CONNECTION POOL ===========\n");
		//
		// for(String key : ConnectionManager.this.getActiveConnections().keySet()) {
		// clientPoolInfo.append("ClientId: " + key + "\n");
		// }
		// clientPoolInfo.append("\n===============================================");
		// logger.log(Level.INFO, "Client Pool \n {0}", clientPoolInfo);
		// }
		// };
		//
		// Timer timer = new Timer("ClientPoolMonitor");
		// timer.scheduleAtFixedRate(timerTask, 0, 20 * 60 * 1000L);
	}

	/**
	 * Returns a connection to elastic server for logged in client;
	 * 
	 * @param clientId
	 *            - Client ID of logged in user
	 * @return Instance of RestHighLevelClient
	 */
	public RestHighLevelClient getConnectionFromPool(String clientId) {
		if (clientId == null) {
			clientId = "others";
		}
		if (clientPool.get(clientId) != null) {
			return clientPool.get(clientId);
		} else {
			clientPool.put(clientId, new RestHighLevelClient(RestClient.builder(hostList)));
			return clientPool.get(clientId);
		}
	}

	/**
	 * Removes client connection from pool
	 * 
	 * @param clientId
	 *            - Client ID of logged in user
	 * @return True or false if any exception
	 */
	public boolean removeConnectionFromPool(String clientId) {
		// if(clientPool.get(clientId) != null) {
		//
		// try {
		// clientPool.get(clientId).close();
		// } catch (IOException e) {
		// LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
		// }
		// clientPool.remove(clientId);
		// return true;
		// }
		// return false;
		/*
		 * Do not close the connection as other users of same client might be logged in
		 */
		return true;
	}

	private Map<String, RestHighLevelClient> getActiveConnections() {
		return ConnectionManager.clientPool;
	}

	public void addClientConnection(String clientId) {
		if (clientPool.get(clientId) != null) {
			clientPool.put(clientId, new RestHighLevelClient(RestClient.builder(hostList)));
			logger.log(Level.INFO, "Created connection for client on login {0}", clientId);
		} else {
			logger.log(Level.INFO, "Connection exists for logged in client {0}", clientId);
		}
	}

	public RestHighLevelClient createElasticConnection() {

		if (eClient == null) {
			eClient = new RestHighLevelClient(RestClient.builder(hostList));
		}
		return eClient;
	}
	public RestHighLevelClient createSecureElasticConnection() {
		if (eClient == null) {
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("ui-backend", "Welcome321!"));
			
			RestClientBuilder builder = RestClient.builder(hostList).setHttpClientConfigCallback(new HttpClientConfigCallback() {
				
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
					return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}
			});
			eClient = new RestHighLevelClient(builder);
		}
		return eClient;
	}

	public static void closeElasticConnection() {
		// try {
		// eClient.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// eClient = null;
	}

	public void reinitializeConnection() {
		try {
			logger.log(Level.INFO, "Reinitializing elastic connection..");
			ConnectionManager.eClient.close();
			ConnectionManager.eClient = null;
			eClient = new RestHighLevelClient(RestClient.builder(hostList));
			logger.log(Level.INFO, "Elastic Connection Restored");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
