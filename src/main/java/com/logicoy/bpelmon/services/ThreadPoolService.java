package com.logicoy.bpelmon.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class ThreadPoolService {

	public ExecutorService getThreadPoolExecutor(int numOfThreads) {
		return Executors.newFixedThreadPool(numOfThreads);
	}
}
