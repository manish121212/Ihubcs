package com.logicoy.bpelmon.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseConnectionPool implements ElasticConnectionPool {

	private List<RestHighLevelClient> connectionPool;
	private List<RestHighLevelClient> usedConnectionPool = new ArrayList<>();
	private static int MIN_POOL_SIZE = 10;
	private static int MAX_POOL_SIZE = 50;
	@Autowired 
	ConnectionManager connectionManager;
	private static final Logger LOGGER = Logger.getLogger(BaseConnectionPool.class.getName());
	
	public BaseConnectionPool() {
		MIN_POOL_SIZE = 10;
		MAX_POOL_SIZE = 50;
	}
	@PostConstruct
	private void initPool() {
		LOGGER.info("Initializing elastic connection pool");
		int i = 1;
		connectionPool = new LinkedList<>();
		while(i++ <= MIN_POOL_SIZE) {
			connectionPool.add(connectionManager.createElasticConnection());
		}
		LOGGER.log(Level.INFO, "Initialized elastic connection pool with {0} connections", connectionPool.size());
	}
	@Override
	public RestHighLevelClient getConnection(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RestHighLevelClient getConnection() throws RuntimeException {
		if(connectionPool.isEmpty()) {
			if(connectionPool.size() < MAX_POOL_SIZE) {
				connectionPool.add(connectionManager.createElasticConnection());
			} else {
				throw new RuntimeException("Max Connection Pool size reached. Free up connections before continuing");
			}
		}
		RestHighLevelClient client = connectionPool.remove(connectionPool.size() - 1);
		usedConnectionPool.add(client);
		return client;
	}

	@Override
	public boolean release(RestHighLevelClient client) {
		connectionPool.add(client);
		usedConnectionPool.remove(client);
		return true;
	}
	@Override
	public int getPoolSize() {
		return usedConnectionPool.size() + connectionPool.size();
	}
	@Override
	public void shutdown() {
		usedConnectionPool.forEach(this::release);
		
		for(RestHighLevelClient client: connectionPool) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		connectionPool.clear();
		
	}
	
}
