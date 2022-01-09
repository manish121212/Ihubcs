package com.logicoy.bpelmon.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AppLogger {
	private Logger appLogger;
	private String message = "";

	public AppLogger(Logger appLogger) {
		super();
		this.appLogger = appLogger;
	}

	public void logInfo(String message) {
		this.message = message;
		Thread t= new Thread(logMessage);
//		appLogger.log(Level.INFO, message);
		t.start();
	}

	public void logError(String message) {
		appLogger.log(Level.SEVERE, "-----------------------" + message + "-----------------------");
	}

	public void logWarn(String msg) {
		appLogger.log(Level.WARNING, "-----------------------" + msg + "-----------------------");
	}
	private Runnable logMessage = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			appLogger.log(Level.INFO, message);
		}
	};
}
