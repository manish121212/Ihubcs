package com.logicoy.bpelmon.models;

public class UserDetails {

	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String clientName;
	private String company;
	private String userExpiryTime;
	private Authority userrole[];

	public String getFirstName() {
		return firstName;
	}

	public Authority[] getUserrole() {
		return userrole;
	}

	public void setUserrole(Authority[] userrole) {
		this.userrole = userrole;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getUserExpiryTime() {
		return userExpiryTime;
	}

	public void setUserExpiryTime(String userExpiryTime) {
		this.userExpiryTime = userExpiryTime;
	}
}
