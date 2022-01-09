package com.logicoy.bpelmon.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class RequestModel {

	private String instanceId;
	private String clientId;
	private String startDate;
	private String endDate = null;
	private SimpleDateFormat sdf = null;
	private int recordSize = 0;
	private String serviceUnitName = "";
	private String transactionType = "";
	private String primaryTrackingValue = "";
	private int fetchFrom = 0;
	private String docId;
	private String index;
	private String indexType;
	private String updateStatus;
	private String filePath;
	private FtpClientModel ftpDetails;
	private FtpConfigurationModel ftpConfig;
	private ReportConfig reportConfig;
	private int fetchDataFrom;
	private int fetchDataTo;
	private int lastPartition;
	private int lastPartitionSize;
	private int lastSearchStart;
	private int lastSearchLength;
	private String transactionId;
	private String timezone;
	private Logger LOGGER = Logger.getLogger(this.getClass());
	private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	private String draw;
	private String searchQuery;

	public RequestModel() {
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
	}
	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public int getLastPartition() {
		return lastPartition;
	}

	public void setLastPartition(int lastPartition) {
		this.lastPartition = lastPartition;
	}

	public int getLastPartitionSize() {
		return lastPartitionSize;
	}

	public void setLastPartitionSize(int lastPartitionSize) {
		this.lastPartitionSize = lastPartitionSize;
	}

	public int getLastSearchStart() {
		return lastSearchStart;
	}

	public void setLastSearchStart(int lastSearchStart) {
		this.lastSearchStart = lastSearchStart;
	}

	public int getLastSearchLength() {
		return lastSearchLength;
	}

	public void setLastSearchLength(int lastSearchLength) {
		this.lastSearchLength = lastSearchLength;
	}

	public ReportConfig getReportConfig() {
		return reportConfig;
	}

	public int getFetchDataFrom() {
		return fetchDataFrom;
	}

	public void setFetchDataFrom(int fetchDataFrom) {
		this.fetchDataFrom = fetchDataFrom;
	}

	public int getFetchDataTo() {
		return fetchDataTo;
	}

	public void setFetchDataTo(int fetchDataTo) {
		this.fetchDataTo = fetchDataTo;
	}

	public void setReportConfig(ReportConfig reportConfig) {
		this.reportConfig = reportConfig;
	}

	public int getFetchFrom() {
		return fetchFrom;
	}

	public void setFetchFrom(int fetchFrom) {
		this.fetchFrom = fetchFrom;
	}

	public String getPrimaryTrackingValue() {
		return primaryTrackingValue;
	}

	public void setPrimaryTrackingValue(String primaryTrackingValue) {
		this.primaryTrackingValue = primaryTrackingValue;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public long getStartDate() {
//		LOGGER.info("start date: " + startDate + " : " + this.getTimezone());
		
		if (startDate != null) {
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			try {

				cal.setTime(sdf.parse(startDate + this.getTimezone()));
				return cal.getTimeInMillis();
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
		Calendar startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		startDate.set(Calendar.MONTH, startDate.get(Calendar.MONTH) - 1);
		return startDate.getTime().getTime();
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
//		LOGGER.info("end date: " + endDate + " : " + this.getTimezone());
//		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		if (endDate != null) {
			try {
				cal.setTime(sdf.parse(endDate + this.getTimezone()));
				return cal.getTimeInMillis();
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return Calendar.getInstance().getTime().getTime();
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public int getRecordSize() {
		return recordSize;
	}

	public void setRecordSize(int recordSize) {
		this.recordSize = recordSize;
	}

	public String getServiceUnitName() {
		return serviceUnitName;
	}

	public void setServiceUnitName(String serviceUnitName) {
		this.serviceUnitName = serviceUnitName;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	public String getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(String updateStatus) {
		this.updateStatus = updateStatus;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public FtpClientModel getFtpDetails() {
		return ftpDetails;
	}

	public void setFtpDetails(FtpClientModel ftpDetails) {
		this.ftpDetails = ftpDetails;
	}

	public FtpConfigurationModel getFtpConfig() {
		return ftpConfig;
	}

	public void setFtpConfig(FtpConfigurationModel ftpConfig) {
		this.ftpConfig = ftpConfig;
	}
	
	public String getDraw() {
		return draw;
	}
	public void setDraw(String draw) {
		this.draw = draw;
	}
	
	public String getSearchQuery() {
		return searchQuery;
	}
	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}
	@Override
	public String toString() {
		return "RequestModel [instanceId=" + instanceId + ", clientId=" + clientId + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", recordSize=" + recordSize + ", serviceUnitName=" + serviceUnitName
				+ ", transactionType=" + transactionType + ", primaryTrackingValue=" + primaryTrackingValue
				+ ", fetchFrom=" + fetchFrom + ", docId=" + docId + ", index=" + index + ", indexType=" + indexType
				+ ", updateStatus=" + updateStatus + ", filePath=" + filePath + ", ftpDetails=" + ftpDetails + "]";
	}
}
