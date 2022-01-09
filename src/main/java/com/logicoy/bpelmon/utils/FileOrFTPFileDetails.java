package com.logicoy.bpelmon.utils;

public class FileOrFTPFileDetails {
	String hostname, port, username, password, keyFileLocation, destFilePath;

	@Override
	public String toString() {
		return ("Hostname : " + hostname + ",Port : " + port + ",Username : " + username + ",Passworod : " + password
				+ ", KeyFileLocation : " + keyFileLocation + ", Destination File Path : " + destFilePath);
	}

	/**
	 * Get File hostname, port etcc from URI
	 * 
	 * @param uri
	 * @return
	 */
	public FileOrFTPFileDetails getDetails(String uri) throws Exception {
		// String uri =
		// "ftp://hostname=param.ser.com&port=22&username=param&password=welcome123";
		if (uri != null && uri.indexOf("://") > 0) {
			String part1 = uri.substring(uri.indexOf("://") + 3);
			System.out.println(part1);
			String arr1[] = part1.split("&");
			FileOrFTPFileDetails ftpDetail = new FileOrFTPFileDetails();
			for (String arr : arr1) {
				System.out.println(arr);
				if (arr.toLowerCase().startsWith("hostname")) {
					ftpDetail.hostname = arr.split("=")[1];
				} else if (arr.toLowerCase().startsWith("port")) {
					ftpDetail.port = arr.split("=")[1];
				} else if (arr.toLowerCase().startsWith("username")) {
					ftpDetail.username = arr.split("=")[1];
				} else if (arr.toLowerCase().startsWith("password")) {
					ftpDetail.password = arr.split("=")[1];
				} else if (arr.toLowerCase().startsWith("keyFileLocation")) {
					ftpDetail.keyFileLocation = arr.split("=")[1];
				} else if (arr.toLowerCase().startsWith("destFilePath")) {
					ftpDetail.keyFileLocation = arr.split("=")[1];
				}

			}
			System.out.println(ftpDetail);
			return ftpDetail;
		} else {
			throw new Exception("Not a valid file/FTP URI pattern");
		}
	}
}
