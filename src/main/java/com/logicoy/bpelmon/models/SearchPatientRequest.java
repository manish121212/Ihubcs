package com.logicoy.bpelmon.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RequestPatient", namespace = "http://xml.netbeans.org/schema/RequestPatient")
public class SearchPatientRequest {

	@XmlElement(required = true, namespace = "http://xml.netbeans.org/schema/RequestPatient")
	private String FacilityId;
	@XmlElement(required = true, namespace = "http://xml.netbeans.org/schema/RequestPatient")
	private String PatientGivenName;
	@XmlElement(required = true, namespace = "http://xml.netbeans.org/schema/RequestPatient")
	private String PatientSurName;
	@XmlElement(required = true, namespace = "http://xml.netbeans.org/schema/RequestPatient")
	private String DateOfBirth;

	public String getFacilityId() {
		return FacilityId;
	}

	public void setFacilityId(String facilityId) {
		FacilityId = facilityId;
	}

	public String getPatientGivenName() {
		return PatientGivenName;
	}

	public void setPatientGivenName(String patientGivenName) {
		PatientGivenName = patientGivenName;
	}

	public String getPatientSurName() {
		return PatientSurName;
	}

	public void setPatientSurName(String patientSurName) {
		PatientSurName = patientSurName;
	}

	public String getDateOfBirth() {
		return DateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		DateOfBirth = dateOfBirth;
	}
}
