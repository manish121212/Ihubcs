package com.logicoy.bpelmon.models;

import javax.persistence.Column;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//@Entity
//@Table(name = "reprocess_config")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReprocessConfigDTO {

	@Id
	@Column(name = "client_id")
	private String clientId;
	@Column(name = "su_name")
	private String suName;
	@Column(name = "bpel_id")
	private String bpelId;
	@Column(name = "transaction_type")
	private String transactionType;
	@Column(name = "transaction_data_template")
	private String transactionDataTemplate;
	@Column(name = "service_endpoint")
	private String serviceEndpoint;
	@Column(name = "username")
	private String username;
	@Column(name = "password")
	private String password;
	@Column(name = "method")
	private String method;
	@Column(name = "content_type")
	private String contentType;
	@Column(name = "accept_type")
	private String acceptType;
	@Column(name = "xsl_transformation")
	private String xslTransformation;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSuName() {
		return suName;
	}

	public void setSuName(String suName) {
		this.suName = suName;
	}

	public String getBpelId() {
		return bpelId;
	}

	public void setBpelId(String bpelId) {
		this.bpelId = bpelId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getTransactionDataTemplate() {
		return transactionDataTemplate;
	}

	public void setTransactionDataTemplate(String transactionDataTemplate) {
		this.transactionDataTemplate = transactionDataTemplate;
	}

	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getAcceptType() {
		return acceptType;
	}

	public void setAcceptType(String acceptType) {
		this.acceptType = acceptType;
	}

	public String getXslTransformation() {
		return xslTransformation;
	}

	public void setXslTransformation(String xslTransformation) {
		this.xslTransformation = xslTransformation;
	}

	@Override
	public String toString() {
		return "ReprocessConfigDTO [clientId=" + clientId + ", suName=" + suName + ", bpelId=" + bpelId
				+ ", transactionType=" + transactionType + ", transactionDataTemplate=" + transactionDataTemplate
				+ ", serviceEndpoint=" + serviceEndpoint + ", username=" + username + ", password=" + password
				+ ", method=" + method + ", contentType=" + contentType + ", acceptType=" + acceptType
				+ ", xslTransformation=" + xslTransformation + "]";
	}

}
