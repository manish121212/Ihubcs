package com.logicoy.bpelmon.services;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.Base64;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.logicoy.bpelmon.utils.AppConstants;

@Service
public class SoapService {

	@Autowired
	AppConstants appConst;
	Logger LOGGER = Logger.getLogger(this.getClass().getName());

	static {
		HttpsURLConnection.setDefaultHostnameVerifier(
				(hostname, session) -> hostname.equalsIgnoreCase(new SoapService().appConst.getDefaultHostname()));
	}

	public String sendSoapRequest() {
		String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:req=\"http://xml.netbeans.org/schema/RequestPatient\">\n"
				+ "   <soapenv:Header/>\n" + "   <soapenv:Body>\n" + "      <req:RequestPatient>\n"
				+ "         <req:FacilityId>RVC</req:FacilityId>\n"
				+ "         <req:PatientGivenName>JOHNATHAN</req:PatientGivenName>\n"
				+ "         <req:PatientSurName>DOE</req:PatientSurName>\n"
				+ "         <req:DateOfBirth>1968-7-10</req:DateOfBirth>             \n"
				+ "         <req:MatchMultiplePatients>true</req:MatchMultiplePatients>\n"
				+ "      </req:RequestPatient>\n" + "   </soapenv:Body>\n" + "</soapenv:Envelope>";
		try {
			return this.serviceExchange("POST", "https://pdmpws.logicoy.com/il/rxhistory", xml, true, "Basic",
					"YWRtaW46YWRtaW4=", true, "text/xml", "text/xml");
		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
		}
		return null;
	}

	/**
	 * Performs SOAP Request
	 * 
	 * @param httpMethod
	 *            Protocol of connection ; http/https
	 * @param serverUrl
	 *            API end point to hit
	 * @param body
	 *            body of request
	 * @param doSendBody
	 *            shouldBodyBeSent
	 * @param authenticationType
	 *            Authentication type Ex. Basic
	 * @param authBase64String
	 *            Username and password converted to base64 string
	 * @param doSendAuthHeader
	 *            Is auth enabled
	 * @param contentType_sendtype
	 *            content-type
	 * @param acceptType
	 *            Accept-Type
	 * @return String
	 * @throws Exception
	 */
	public String serviceExchange(String httpMethod, String serverUrl, String body, boolean doSendBody,
			String authenticationType, String authBase64String, boolean doSendAuthHeader, String contentType_sendtype,
			String acceptType) throws Exception {
		LOGGER.info("HTTP method: " + httpMethod);
		LOGGER.info("serverUrl: " + serverUrl);
		LOGGER.info("doSendBody: " + doSendBody);
		LOGGER.info("authenticationType: " + authenticationType);
		LOGGER.info("authBase64String: " + authBase64String);
		LOGGER.info("doSendAuthHeader: " + doSendAuthHeader);
		LOGGER.info("Content-type: " + contentType_sendtype);
		LOGGER.info("acceptType: " + acceptType);
		final URL url = new URL(serverUrl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		// urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
		if (urlConnection instanceof HttpsURLConnection) {
			// SSLContext sslContext = createSslContext(keyStoreLocation, keyStorePassword);
			// ((HttpsURLConnection)
			// urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());

			HostnameVerifier hostnameVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return hostname.equalsIgnoreCase(url.getHost());
				}
			};
			((HttpsURLConnection) urlConnection).setHostnameVerifier(hostnameVerifier);

			urlConnection.setConnectTimeout(Integer.MAX_VALUE);

			// if (httpMethod.equalsIgnoreCase("GET")) {
			// urlConnection.setDoOutput(true);
			// urlConnection.setRequestMethod("GET");
			// } else if (httpMethod.equalsIgnoreCase("POST")) {
			// urlConnection.setDoOutput(true);
			// urlConnection.setRequestMethod("POST");
			// urlConnection.setRequestProperty("Content-Type", contentType_sendtype);
			// }
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod(httpMethod);
			urlConnection.setRequestProperty("Content-Type", contentType_sendtype);

			if (doSendAuthHeader) {
				String authString = authenticationType + " " + authBase64String;
				urlConnection.setRequestProperty("Authorization", authString);
			}

			urlConnection.setRequestProperty("Accept", acceptType);

			if (doSendBody) {
				OutputStream os = urlConnection.getOutputStream();
				os.write(body.getBytes());
				os.flush();
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));

			String output;
			StringBuilder responseBuilder = new StringBuilder();

			while ((output = br.readLine()) != null) {
				responseBuilder.append(output);
			}
			System.out.println("" + responseBuilder.toString());
			urlConnection.disconnect();
			return responseBuilder.toString();

		}
		return null;
	}

	public SSLContext createSslContext(String keystorePath, String password) throws Exception {
		InputStream trustStream = null;
		try {
			trustStream = new FileInputStream(keystorePath);
			char[] trustPassword = password.toCharArray();

			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(trustStream, trustPassword);

			TrustManagerFactory trustFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustFactory.init(trustStore);
			TrustManager[] trustManagers = trustFactory.getTrustManagers();

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustManagers, null);
			SSLContext.setDefault(sslContext);

			return sslContext;
		} finally {
			if (trustStream != null)
				trustStream.close();
		}
	}

	public String getBase64String(String username, String password) {
		String authString = username + ":" + password;
		return Base64.getEncoder().encodeToString(authString.getBytes());
	}
}
