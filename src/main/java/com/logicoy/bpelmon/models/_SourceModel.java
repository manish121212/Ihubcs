package com.logicoy.bpelmon.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class _SourceModel {

	@JsonProperty("is_fault")
	public String isFault;
	@JsonProperty("bpel_id")
	public String bpelId;
	@JsonProperty("su_name")
	public String suName;
	@JsonProperty("crmp_invoke_id")
	public String crmpInvokeId;
	@JsonProperty("su_zip_archive")
	public String suZipArchive;
	@JsonProperty("has_faulted")
	public String hasFaulted;
	@JsonProperty("crmp_receive_id")
	public String crmpReceiveId;
	@JsonProperty("instance_id")
	public String instanceId;
	@JsonProperty("event_type")
	public String eventType;
	@JsonProperty("fault_name")
	public String faultName;
	@JsonProperty("engine_id")
	public String engineId;
	@JsonProperty("var_value")
	public String varValue;
	@JsonProperty("scope_id")
	public String scopeId;
	@JsonProperty("var_id")
	public String varId;
	@JsonProperty("event_seq_no")
	public String eventSeqNo;
	@JsonProperty("event_time")
	public String eventTime;
	@JsonProperty("var_name")
	public String varName;
	@JsonProperty("activity_xpath")
	public String activityXpath;
	@JsonProperty("status")
	public String status;

	public String getIsFault() {
		return isFault != null ? isFault : "";
	}

	public void setIsFault(String isFault) {
		this.isFault = isFault;
	}

	public String getBpelId() {
		return bpelId != null ? bpelId : "";
	}

	public void setBpelId(String bpelId) {
		this.bpelId = bpelId;
	}

	public String getSuName() {
		return suName != null ? suName : "";
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public String getCrmpInvokeId() {
		return crmpInvokeId != null ? crmpInvokeId : "";
	}

	public void setCrmpInvokeId(String crmpInvokeId) {
		this.crmpInvokeId = crmpInvokeId;
	}

	public String getSuZipArchive() {
		return suZipArchive != null ? suZipArchive : "";
	}

	public void setSuZipArchive(String suZipArchive) {
		this.suZipArchive = suZipArchive;
	}

	public String getHasFaulted() {
		return hasFaulted != null ? bpelId : "";
	}

	public void setHasFaulted(String hasFaulted) {
		this.hasFaulted = hasFaulted;
	}

	public String getCrmpReceiveId() {
		return crmpReceiveId != null ? crmpReceiveId : "";
	}

	public void setCrmpReceiveId(String crmpReceiveId) {
		this.crmpReceiveId = crmpReceiveId;
	}

	public String getInstanceId() {
		return instanceId != null ? instanceId : "";
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getEventType() {
		return eventType != null ? eventType : "";
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getFaultName() {
		return faultName != null ? faultName : "";
	}

	public void setFaultName(String faultName) {
		this.faultName = faultName;
	}

	public String getEngineId() {
		return engineId != null ? engineId : "";
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}

	public String getVarValue() {
		return varValue != null ? varValue : "";
	}

	public void setVarValue(String varValue) {
		this.varValue = varValue;
	}

	public String getScopeId() {
		return scopeId != null ? scopeId : "";
	}

	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}

	public String getVarId() {
		return varId != null ? varId : "";
	}

	public void setVarId(String varId) {
		this.varId = varId;
	}

	public String getEventSeqNo() {
		return eventSeqNo != null ? eventSeqNo : "";
	}

	public void setEventSeqNo(String eventSeqNo) {
		this.eventSeqNo = eventSeqNo;
	}

	public String getEventTime() {
		return eventTime != null ? eventTime : "";
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}

	public String getVarName() {
		return varName != null ? varName : "";
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getActivityXpath() {
		return activityXpath != null ? activityXpath : "";
	}

	public void setActivityXpath(String activityXpath) {
		this.activityXpath = activityXpath;
	}

	public String getStatus() {
		return status != null ? status : "";
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "_SourceModel [isFault=" + isFault + ", bpelId=" + bpelId + ", suName=" + suName + ", crmpInvokeId="
				+ crmpInvokeId + ", suZipArchive=" + suZipArchive + ", hasFaulted=" + hasFaulted + ", crmpReceiveId="
				+ crmpReceiveId + ", instanceId=" + instanceId + ", eventType=" + eventType + ", faultName=" + faultName
				+ ", engineId=" + engineId + ", varValue=" + varValue + ", scopeId=" + scopeId + ", varId=" + varId
				+ ", eventSeqNo=" + eventSeqNo + ", eventTime=" + eventTime + ", varName=" + varName
				+ ", activityXpath=" + activityXpath + ", status=" + status + "]";
	}
}
