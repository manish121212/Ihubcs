package com.logicoy.bpelmon.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

@Service
public class FileTransferService {

	public boolean sendFileToHost(String keyPath, String filePathToSend, String username, String hostname,
			String targetFilePath) {

		try {
			ProcessBuilder pb = new ProcessBuilder("scp", keyPath, filePathToSend,
					username + "@" + hostname + ":" + targetFilePath);
			Process p = pb.start();
			p.waitFor();
			return true;
		} catch (IOException | InterruptedException ex) {
			Logger.getLogger(FileTransferService.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}
}
