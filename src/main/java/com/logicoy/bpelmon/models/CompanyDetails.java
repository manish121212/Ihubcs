package com.logicoy.bpelmon.models;

public class CompanyDetails {

	private int id;
	private String companyName;
	private String website;
	private String contactNo;
	private String contactEmail;
	private String timezone;
	private int payloadStatus;
	private String emailAlert;

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public int getPayloadStatus() {
		return payloadStatus;
	}

	public void setPayloadStatus(int payloadStatus) {
		this.payloadStatus = payloadStatus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getEmailAlert() {
		return emailAlert;
	}

	public void setEmailAlert(String emailAlert) {
		this.emailAlert = emailAlert;
	}

	@Override
	public String toString() {
		return "CompanyDetails [id=" + id + ", companyName=" + companyName + ", website=" + website + ", contactNo="
				+ contactNo + ", contactEmail=" + contactEmail + ", timezone=" + timezone + ", payloadStatus="
				+ payloadStatus + ", emailAlert=" + emailAlert + "]";
	}
	
}
