package com.logicoy.bpelmon.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConstants {

	@Value("${elastic.server.host}")
	private String ELASTIC_SERVER_HOST;

	@Value("${elastic.server.port}")
	private String ELASTIC_SERVER_PORT;

	@Value("${elastic.server.protocol}")
	private String ELASTIC_SERVER_PROTOCOL;

	@Value("${elastic.server.index}")
	private String ELASTIC_SERVER_INDEX;

	@Value("${elastic.server.indexType}")
	private String ELASTIC_SERVER_INDEX_TYPE;

	@Value("${test-prop}")
	private String testProperty;
	
	@Value("${default-hostname}")
	private String defaultHostname;
	
	@Value("${old-client}")
	private String oldClients;
	
	@Value("${document-threshold}")
	private long documentThreshold;

	@Value("${partition-chunk-size}")
	private long partitionChunkSize;
	
	@Value("${location.config.file.servicenameresolver}")
	private String frontendConfigFolder;
	
	@Value("${app.mail.protocol}")
	private String emailProtocol;
	
	@Value("${app.mail.host}")
	private String emailHost;
	
	@Value("${app.mail.port}")
	private String emailPort;
	
	@Value("${app.mail.username}")
	private String emailUsername;
	
	@Value("${app.mail.password}")
	private String emailPassword;
	
	@Value("${app.mail.auth}")
	private String auth;
	
	@Value("${app.mail.starttls}")
	private String startTls;
	
	@Value("${app.mail.instance-url}")
	private String instanceUrl;
	
	@Value("${app.mail.subject-registration}")
	private String registrationMailSubject;
	
	@Value("${app.es.max-range}")
	private String esMaxRange;
	
	public String getInstanceUrl() {
		return instanceUrl;
	}

	public void setInstanceUrl(String instanceUrl) {
		this.instanceUrl = instanceUrl;
	}

	public long getPartitionChunkSize() {
		return partitionChunkSize;
	}

	public void setPartitionChunkSize(long partitionChunkSize) {
		this.partitionChunkSize = partitionChunkSize;
	}

	public long getDocumentThreshold() {
		return documentThreshold;
	}

	public void setDocumentThreshold(long documentThreshold) {
		this.documentThreshold = documentThreshold;
	}

	public String getOldClients() {
		return oldClients;
	}

	public void setOldClients(String oldClients) {
		this.oldClients = oldClients;
	}

	public String getDefaultHostname() {
		return defaultHostname;
	}

	public void setDefaultHostname(String defaultHostname) {
		this.defaultHostname = defaultHostname;
	}

	public String getTestProperty() {
		return testProperty;
	}

	public void setTestProperty(String testProperty) {
		this.testProperty = testProperty;
	}

	public String[] getHost() {
		return ELASTIC_SERVER_HOST.split(",");
	}

	public int[] getPort() {
		String portArr[] = ELASTIC_SERVER_PORT.split(",");
		int portArrInt[] = new int[portArr.length];
		for (int i = 0; i < portArr.length; i++) {
			portArrInt[i] = Integer.parseInt(portArr[i]);
		}
		return portArrInt;
	}

	public String getProtocol() {
		return ELASTIC_SERVER_PROTOCOL;
	}

	public String getIndex() {
		return ELASTIC_SERVER_INDEX.split(",")[0];
	}

	public String getIndexType() {
		return ELASTIC_SERVER_INDEX_TYPE.split(",")[0];
	}

	public String getTransactionIndex() {
		try {
			return ELASTIC_SERVER_INDEX.split(",")[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String getTransactionIndexType() {
		try {
			return ELASTIC_SERVER_INDEX_TYPE.split(",")[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String[] getIndices() {
		return ELASTIC_SERVER_INDEX.split(",");
	}

	public String[] getIndexTypes() {
		return ELASTIC_SERVER_INDEX_TYPE.split(",");
	}

	public String getSubTransactionIndex() {
		try {
			return ELASTIC_SERVER_INDEX.split(",")[2];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String getSubTransactionIndexType() {
		try {
			return ELASTIC_SERVER_INDEX_TYPE.split(",")[2];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String getBpelMonCountIndex() {
		try {
			return ELASTIC_SERVER_INDEX.split(",")[3];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String getBpelMonCountIndexType() {
		try {
			return ELASTIC_SERVER_INDEX_TYPE.split(",")[3];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String getBpelAlertIndex() {
		try {
			return ELASTIC_SERVER_INDEX.split(",")[4];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public String getBpelNotificationIndex() {
		try {
			return ELASTIC_SERVER_INDEX.split(",")[5];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	public String getTransactionCountIndex() {
		try {
			return ELASTIC_SERVER_INDEX.split(",")[6];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	public String getTransactionCountIndexType() {
		try {
			return ELASTIC_SERVER_INDEX_TYPE.split(",")[4];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	public String getFrontendConfigFolder() {
		return frontendConfigFolder;
	}

	public void setFrontendConfigFolder(String frontendConfigFolder) {
		this.frontendConfigFolder = frontendConfigFolder;
	}

	public String getEmailProtocol() {
		return emailProtocol;
	}

	public void setEmailProtocol(String emailProtocol) {
		this.emailProtocol = emailProtocol;
	}

	public String getEmailHost() {
		return emailHost;
	}

	public void setEmailHost(String emailHost) {
		this.emailHost = emailHost;
	}

	public String getEmailPort() {
		return emailPort;
	}

	public void setEmailPort(String emailPort) {
		this.emailPort = emailPort;
	}

	public String getEmailUsername() {
		return emailUsername;
	}

	public void setEmailUsername(String emailUsername) {
		this.emailUsername = emailUsername;
	}

	public String getEmailPassword() {
		return emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getStartTls() {
		return startTls;
	}

	public void setStartTls(String startTls) {
		this.startTls = startTls;
	}



	public String getRegistrationMailSubject() {
		return registrationMailSubject;
	}

	public void setRegistrationMailSubject(String registrationMailSubject) {
		this.registrationMailSubject = registrationMailSubject;
	}

	public String getEsMaxRange() {
		return esMaxRange;
	}

	public void setEsMaxRange(String esMaxRange) {
		this.esMaxRange = esMaxRange;
	}



	// Field constants for elastic search index mapping updation
	public static final String INSTANCE_ID = "instance_id";
	public static final String BPEL_ID = "bpel_id";
	public static final String SU_NAME = "su_name";
	public static final String STATUS = "status";
	public static final String EVENT_TYPE = "event_type";
	public static final String PRIMARY_TRACKING_VALUE = "primary_tracking_value";
	public static final String SECONDARY_TRACKING_VALUE = "secondary_tracking_value";
	public static final String TRANSACTION_TYPE = "transaction_type";
	public static final String TYPE = "type";
	public static final String TRANSACTION_ID = "transaction_id";
}
