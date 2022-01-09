package com.logicoy.bpelmon.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LogiCoy
 */
public class PropertyConfigListener {

	private PropertyConfigListener() {

	}

	/**
	 * Singleton Helper, only loads when required
	 */
	private static class SingletonHelper {
		private static final PropertyConfigListener INSTANCE = new PropertyConfigListener();
	}

	/**
	 * Thread safe get Instance implementation
	 *
	 * @return
	 */
	public static synchronized PropertyConfigListener getReader() {
		return SingletonHelper.INSTANCE;
	}

	private final Logger logger = Logger.getLogger(PropertyConfigListener.class.getName());

	private Properties envProperties = new Properties();

	private long lastModified = 0L;
	private String propLocation = null;

	public void loadProperties(String propLocation) {
		FileInputStream fis = null;
		this.propLocation = propLocation;
		try {
			File f = new File(propLocation);
			logger.log(Level.INFO, "Loading listener properties from file :{0}", propLocation);
			fis = new FileInputStream(propLocation);
			envProperties.load(fis);
			lastModified = f.lastModified();
		} catch (Exception ex) {
			Logger.getLogger(PropertyConfigListener.class.getName()).log(Level.SEVERE,
					"Exception while loading listener properties", ex);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ex) {
					Logger.getLogger(PropertyConfigListener.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	public String getValue(String key) {
		File f = new File(this.propLocation);

		if (f.lastModified() > lastModified) {
			logger.log(Level.INFO, "Listener config file Last modified at : {0} ; variable lastModified: {1}",
					new Object[] { f.lastModified(), lastModified });
			loadProperties(this.propLocation);
		}

		return envProperties.getProperty(key);

	}

}