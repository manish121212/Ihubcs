package com.logicoy.bpelmon.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import com.logicoy.bpelmon.models.FileDetails;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

public class LSFTPClient {
	private static final Logger logger = Logger.getLogger(LSFTPClient.class.getName());
	private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private static final int dataTimeout = 60 * 60 * 1000; // 1 hour
	private static final int connectionTimeout = 60 * 60 * 1000;
	private String host;
	private String user;
	private String password;
	private int port;
	private SshClient ssh = null;
	private static SftpClient sftp = null;
	private boolean connected = false;
	private boolean loggedIn = false;

	/**
	 *
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 */
	public LSFTPClient(String host, int port, String user, String password) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	public LSFTPClient() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 *
	 * @return
	 */
	public boolean connect() {
		if (ssh != null)
			ssh = new SshClient();

		try {
			logger.info("Connecting to " + host);
			SshConnectionProperties properties = new SshConnectionProperties();
			if (ssh != null && !ssh.isConnected()) {
				ssh.connect(host, port, (arg0, arg1) -> true);
			}

			// After connection attempt, you should check the reply code to verify
			// success.
			// int reply = ftp.getReplyCode();
			if (ssh != null && !ssh.isConnected()) {
				ssh.disconnect();
				logger.severe("SFTP server refused connection.");
			}
			logger.info("Connected to " + host);
			connected = true;
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			if (ssh.isConnected()) {
				ssh.disconnect();
			}
		}
		return connected;
	}

	/**
	 *
	 * @return
	 */
	public boolean login() {
		logger.info("EJBFTP : Login process...start ");
		if (connected) {
			try {
				PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
				// String username = "HumanaVitality";
				pwd.setUsername(user);
				// String password = "Rxh4A$zMs";
				pwd.setPassword(password);

				int result = ssh.authenticate(pwd);
				if (result == AuthenticationProtocolState.COMPLETE) {
					loggedIn = true;
					sftp = ssh.openSftpClient();
					logger.info("Successfully logged in ");
					logger.info("EJBFTP : Login process...success ");
				}
			} catch (IOException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}
		return loggedIn;
	}

	/**
	 *
	 * @param remoteFilePath
	 * @param filePattern
	 * @param downloadFolder
	 * @return
	 */
	public String receiveFile(String remoteFilePath, String filePattern, String downloadFolder) {
		String receivedFile = "";
		boolean received = false;
		try {
			if (connected && loggedIn) {
				// ftp.changeWorkingDirectory(remoteFilePath);
				if (!remoteFilePath.endsWith("/")) {
					remoteFilePath = remoteFilePath + "/";
				}
				if (!downloadFolder.endsWith("/")) {
					downloadFolder = downloadFolder + "/";
				}
				List<SftpFile> sftpFiles = sftp.ls(remoteFilePath);
				if (sftpFiles != null) {
					// logger.info("Listing filenames in " + remoteFilePath + ": " + fileNames);
					for (SftpFile sftpFile : sftpFiles) {
						logger.info(sftpFile.getFilename());
						boolean matches = Pattern.matches(filePattern, sftpFile.getFilename());
						if (matches) {
							OutputStream output = new FileOutputStream(downloadFolder + sftpFile.getFilename());
							logger.info("downloading file.." + sftpFile.getFilename());
							sftp.get(remoteFilePath + sftpFile.getFilename(), output);
							FileAttributes fileAttrib = sftp.stat("");
							logger.info("download status :" + received);
							if (fileAttrib != null) {
								// receivedFile = remoteFilePath + ftpFile.getName();
								receivedFile = sftpFile.getFilename();
								logger.info("receivedFile :" + receivedFile);
							}
							output.close();
							break;
						}
					}
				}
			}
		} catch (IOException ex) {
			received = false;
			logger.log(Level.SEVERE, null, ex);
		}

		return receivedFile;
	}

	/**
	 *
	 * @param localFilePath
	 * @param remoteFilePath
	 * @param originalFileName
	 * @param targetFileName
	 * @return
	 */
	public boolean copyFileToFTP(String localFilePath, String remoteFilePath, String originalFileName,
			String targetFileName) {
		boolean processed = false;
		try {
			String[] filePaths = remoteFilePath.split(",");
			File file = null;
			for (String strPath : filePaths) {
				if (connected && loggedIn) {
				}
				// ftp.changeWorkingDirectory(remoteFilePath);
				if (!localFilePath.endsWith("/")) {
					localFilePath = localFilePath + "/";
				}
				if (!strPath.endsWith("/")) {
					strPath = strPath + "/";
				}
				logger.info("localFilePath : " + localFilePath);
				FileInputStream fis = null;

				try {
					file = new File(localFilePath + originalFileName);
					fis = new FileInputStream(file);
					sftp.put(fis, strPath + targetFileName);
					logger.info("Copying " + originalFileName + " to target FTP location :" + strPath + " as "
							+ targetFileName);
					processed = true;

				} catch (Exception e) {
					logger.log(Level.SEVERE, null, e);
					processed = false;
					
				} finally {
					if (fis != null) {
						fis.close();
					}
				}
			}
			return processed;
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			processed = false;
		}
		return processed;
	}

	/**
	 *
	 * @param localFilePath
	 * @param remoteFilePath
	 * @param fileName
	 * @return
	 */
	int reTryCounter = 0;

	public boolean sendFileAndRename(String localFilePath, String remoteFilePath, String fileName) {
		boolean processed = false;

		try {

			String[] filePaths = remoteFilePath.split(",");
			File file = null;
			for (String strPath : filePaths) {
				if (connected && loggedIn) {
				}
				// ftp.changeWorkingDirectory(remoteFilePath);
				if (!localFilePath.endsWith("/")) {
					localFilePath = localFilePath + "/";
				}
				if (!strPath.endsWith("/")) {
					strPath = strPath + "/";
				}
				logger.log(Level.INFO, "localFilePath{0}", localFilePath);
				logger.log(Level.INFO, ":::: Checking for Two remote path::::strPath------>{0}", strPath);
				FileInputStream fis = null;
				try {

					file = new File(localFilePath + fileName);
					fis = new FileInputStream(file);

					boolean isPathConfigured = true;

					if (isPathConfigured) {
						logger.log(Level.INFO,
								" :::This path is not set to use Tmp extension and renaming So uploading  : ...{0}",
								localFilePath);
						sftp.put(fis, strPath + fileName);
					} else {
						sftp.put(fis, strPath + fileName + ".tmp");
						logger.info("Will wait for one sec before renaming file to actual filename ");
						Thread.currentThread().sleep(1000);
						sftp.rename(strPath + fileName + ".tmp", strPath + fileName);
					}
					logger.log(Level.INFO, "Uploaded file..to :{0}{1}", new Object[] { strPath, fileName });
					processed = true;

				} catch (IOException ex) {
					ex.printStackTrace();
					logger.log(Level.INFO, ":::Ftp Service has thrown exception while uploading ,Exception details{0}",
							ex);
					processed = false;
					FileAttributes list = null;
					reTryCounter++;
					if (reTryCounter < 4) {
						logger.log(Level.INFO, "ReTrying to upload SFTP file {0}{1}",
								new Object[] { strPath, fileName });
						try {
							list = sftp.stat(strPath + fileName + ".tmp");
						} catch (IOException IoEx) {
							IoEx.printStackTrace();
							logger.log(Level.INFO, "Temp file was not created for {0}{1}.tmp",
									new Object[] { strPath, fileName });
							sendFileAndRename(localFilePath, remoteFilePath, fileName);
						}
						boolean isuploaded = list != null;
						if (isuploaded) {
							logger.log(Level.INFO, "deleting file from SFTP location{0}{1}.tmp",
									new Object[] { strPath, fileName });
							sftp.rm(strPath + fileName + ".tmp");
							sendFileAndRename(localFilePath, remoteFilePath, fileName);
						}
					}

				} catch (Exception e) {
					logger.log(Level.SEVERE, null, e);
					processed = false;
				} finally {
					if (fis != null) {
						fis.close();
					}
				}
			}

			// move the file to the process folder.
			if (processed) {
				File processedDir = new File(localFilePath + "processed");
				if (!processedDir.exists()) {
					processedDir.mkdir();
				}

				// move the file to process folder
				if (file != null) {
					boolean moved = file
							.renameTo(new File(localFilePath + "processed", resolveFileNameToRename(fileName)));
					logger.log(Level.INFO, "File is moved to processed folder : {0}", moved);
				}
			}
			return processed;
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			processed = false;
			
		}
		return processed;
	}

	public boolean pathNotToUseTempExtension(String localpath) {
		return true;
	}

	/**
	 * @param remoteFilePath
	 * @param filePattern
	 * @return
	 */
	public List<FileDetails> listFiles(String remoteFilePath, String filePattern) {
		List<FileDetails> fileDetails = new ArrayList<FileDetails>();
		try {
			if (connected && loggedIn) {
				List<SftpFile> sftpFiles = sftp.ls(remoteFilePath);
				if (sftpFiles != null) {
					for (SftpFile sftpFile : sftpFiles) {
						FileDetails file = new FileDetails();
						String ftpFileName = sftpFile.getFilename();
						logger.info(ftpFileName);
						file.setFileName(ftpFileName);
						file.setCreatedTime(new Date(sftpFile.getAttributes().getModifiedTime().longValue()));
						if (sftpFile.isDirectory()) {
							file.setType("dir");
							file.setFileExtension("Directory");
						} else {
							file.setType("file");
							file.setFileExtension(
									sftpFile.getFilename().substring(sftpFile.getFilename().lastIndexOf(".") + 1));
						}
						file.setFileSize(0);
						boolean matches = Pattern.matches(filePattern, ftpFileName);
						logger.log(Level.INFO, "pattern matched? -> {0}", matches);
						if (matches) {
							fileDetails.add(file);
						}
					}
				}
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return fileDetails;
	}

	/**
	 *
	 * @param remoteFilePath
	 * @param filePattern
	 * @param downloadFolder
	 * @param postReceiveLocation
	 * @return
	 */
	public String receiveFileAndRename(String remoteFilePath, String filePattern, String downloadFolder,
			String postReceiveLocation) {

		String receivedFile = "";
		boolean received = false;
		FileAttributes fileAttrib = null;
		try {
			if (connected && loggedIn) {

				// ftp.changeWorkingDirectory(remoteFilePath);
				if (!remoteFilePath.endsWith("/")) {
					remoteFilePath = remoteFilePath + "/";
				}
				if (!downloadFolder.endsWith("/")) {
					downloadFolder = downloadFolder + "/";
				}
				List<SftpFile> sftpFiles = sftp.ls(remoteFilePath);
				if (sftpFiles != null) {
					logger.info("Listing filenames in " + remoteFilePath);
					for (SftpFile sftpFile : sftpFiles) {
						String ftpFileName = sftpFile.getFilename();
						logger.info(ftpFileName);
						boolean matches = Pattern.matches(filePattern, ftpFileName);
						if (matches) {
							File outputFile = new File(downloadFolder + ftpFileName + ".tmp");
							if (!isAlreadyDownloaded(ftpFileName + ".tmp", downloadFolder)) {

								OutputStream output = new FileOutputStream(outputFile);
								logger.info("downloading file.." + ftpFileName);
								try {
									sftp.get(remoteFilePath + ftpFileName, output);
									FileAttributes fileAttributes = sftp.stat("");
									received = fileAttributes != null;
								} catch (Exception e) {
									logger.severe("Exception while downloading file: " + ftpFileName + " : "
											+ e.getMessage());
									if (outputFile.exists()) {
										outputFile.delete();
									}
									e.printStackTrace();
								}
								output.close();
								logger.info("download status :" + received);
							} else {
								received = true;
							}

							if (received) {
								if (!postReceiveLocation.endsWith("/")) {
									postReceiveLocation = postReceiveLocation + "/";
								}
								String renameTo = postReceiveLocation + LFTPClient.resolveFileNameToRename(ftpFileName);

								logger.info("Renaming " + remoteFilePath + ftpFileName + " TO " + renameTo);
								boolean renameStatus = false;
								try {
									sftp.rename(remoteFilePath + ftpFileName, renameTo);
									renameStatus = true;
								} catch (IOException ioException) {
									ioException.printStackTrace();
									renameStatus = false;
								}
								logger.info("RENAME status : " + renameStatus);
								if (renameStatus == false) {
									receivedFile = "";
								} else {
									File downloadedFile = new File(downloadFolder + ftpFileName + ".tmp");
									boolean localRenamed = downloadedFile
											.renameTo(new File(downloadFolder + ftpFileName));
									logger.info("local renamed:" + localRenamed);
									if (!localRenamed) {
										boolean deleted = deleteIfExists(downloadFolder + ftpFileName);
										logger.info("is local deleted for overwriting? " + deleted);
										if (deleted) {
											localRenamed = downloadedFile
													.renameTo(new File(downloadFolder + ftpFileName));
											logger.info("local renamed:" + localRenamed);
										}
									}

									if (localRenamed) {
										// if (ftpFileName.endsWith(".zip") || ftpFileName.endsWith("ZIP")) {
										// receivedFile = unzip(downloadFolder + ftpFileName, downloadFolder);
										// logger.info("Unzipped File: " + receivedFile);
										// } else {
										receivedFile = ftpFileName;
										// }
									}
									logger.info("receivedFile :" + receivedFile);
								}
							} else {
								if (outputFile.exists()) {
									outputFile.delete();
								}
							}
							break;
						}
					}
				}
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			received = false;
			
		}
		return receivedFile;
	}

	/**
	 *
	 * @param remoteFilePath
	 * @param filePattern
	 * @param downloadFolder
	 * @param postReceiveLocation
	 * @param copyRemoteLocation
	 * @return
	 */
	public String receiveFileAndRename(String remoteFilePath, String filePattern, String downloadFolder,
			String postReceiveLocation, String copyRemoteLocation) {

		String receivedFile = "";
		boolean received = false;
		FileAttributes fileAttrib = null;
		try {
			if (connected && loggedIn) {

				// ftp.changeWorkingDirectory(remoteFilePath);
				if (!remoteFilePath.endsWith("/")) {
					remoteFilePath = remoteFilePath + "/";
				}
				if (!downloadFolder.endsWith("/")) {
					downloadFolder = downloadFolder + "/";
				}
				List<SftpFile> sftpFiles = sftp.ls(remoteFilePath);
				if (sftpFiles != null) {
					logger.info("Listing filenames in " + remoteFilePath);
					for (SftpFile sftpFile : sftpFiles) {
						String ftpFileName = sftpFile.getFilename();
						logger.info(ftpFileName);
						boolean matches = Pattern.matches(filePattern, ftpFileName);
						logger.info("pattern matched? -> " + matches);
						if (matches) {
							File outputFile = new File(downloadFolder + ftpFileName + ".tmp");
							if (!isAlreadyDownloaded(ftpFileName + ".tmp", downloadFolder)) {

								OutputStream output = new FileOutputStream(outputFile);
								logger.info("downloading file.." + ftpFileName);
								try {
									sftp.get(remoteFilePath + ftpFileName, output);
									FileAttributes fileAttributes = sftp.stat("");
									received = fileAttributes != null;
								} catch (Exception e) {
									e.printStackTrace();
									logger.severe("Exception while downloading file: " + ftpFileName + " : "
											+ e.getMessage());
									if (outputFile.exists()) {
										outputFile.delete();
									}
								}
								output.close();
								logger.info("download status :" + received);
							} else {
								received = true;
							}
							if (received) {
								if (!postReceiveLocation.endsWith("/")) {
									postReceiveLocation = postReceiveLocation + "/";
								}
								String renameTo = postReceiveLocation + LFTPClient.resolveFileNameToRename(ftpFileName);

								logger.info("Renaming " + remoteFilePath + ftpFileName + " TO " + renameTo);
								boolean renameStatus = false;
								try {
									sftp.rename(remoteFilePath + ftpFileName, renameTo);
									renameStatus = true;
								} catch (IOException ioException) {
									ioException.printStackTrace();
									renameStatus = false;
								}
								logger.info("RENAME status : " + renameStatus);
								if (renameStatus == false) {
									receivedFile = "";
								} else {
									boolean copyStatus = false;
									File downloadedFile = new File(downloadFolder + ftpFileName + ".tmp");
									if (copyRemoteLocation != null && !copyRemoteLocation.equals("")) {
										if (!copyRemoteLocation.endsWith("/")) {
											copyRemoteLocation = copyRemoteLocation + "/";
										}
										logger.info("Remote Copying " + remoteFilePath + ftpFileName + " TO "
												+ copyRemoteLocation + ftpFileName);
										InputStream is = null;
										try {
											if (downloadedFile != null) {
												is = new FileInputStream(downloadedFile);
												if (is != null) {
													sftp.put(is, copyRemoteLocation + ftpFileName);
													logger.info(
															"Remote Copied to :" + copyRemoteLocation + ftpFileName);
													copyStatus = true;
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
											logger.log(Level.WARNING, e.getMessage(), e);
											copyStatus = false;
										} finally {
											if (is != null) {
												is.close();
											}
										}
										logger.info("REMOTE COPY status : " + copyStatus);
									} else {
										logger.info("Remote copy location is empty. Not copying");
										copyStatus = true;
									}
									boolean localRenamed = downloadedFile
											.renameTo(new File(downloadFolder + ftpFileName));
									logger.info("local renamed:" + localRenamed);
									if (!localRenamed) {
										boolean deleted = deleteIfExists(downloadFolder + ftpFileName);
										logger.info("is local deleted for overwriting? " + deleted);
										if (deleted) {
											localRenamed = downloadedFile
													.renameTo(new File(downloadFolder + ftpFileName));
											logger.info("local renamed:" + localRenamed);
										}
									}

									if (localRenamed) {
										// if (ftpFileName.endsWith(".zip") || ftpFileName.endsWith("ZIP")) {
										// receivedFile = unzip(downloadFolder + ftpFileName, downloadFolder);
										// logger.info("Unzipped File: " + receivedFile);
										// } else {
										receivedFile = ftpFileName;
										// }
									}

									if (copyStatus == false) {
										receivedFile = "";
									}
									logger.info("receivedFile :" + receivedFile);
								}
							} else {
								if (outputFile.exists()) {
									outputFile.delete();
								}
							}
							break;
						}
					}
				}
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
			received = false;
			
		}
		logger.info("LSFTPClient.receiveFileAndRename(...) receivedFile ->: " + receivedFile);
		return receivedFile;
	}

	/**
	 *
	 * @param filePath
	 * @return
	 */
	private boolean deleteIfExists(String filePath) {
		File f = new File(filePath);
		if (f.exists()) {
			return f.delete();
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean renameFile(String from, String to) {
		boolean renamed = false;
		if (connected && loggedIn) {
			try {
				logger.info("Renaming " + from + " TO " + to);
				sftp.rename(from, to);
				renamed = true;
				logger.info("RENAME status : " + renamed);
			} catch (IOException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}
		return renamed;
	}

	/**
	 *
	 * @param zipFilePath
	 * @param extractFolder
	 * @return
	 */
	private String unzip(String zipFilePath, String extractFolder) {

		String fileName = "";
		Enumeration entries;
		ZipFile zipFile = null;

		if (!extractFolder.endsWith("/")) {
			extractFolder = extractFolder + "/";
		}
		try {
			zipFile = new ZipFile(zipFilePath);
			entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					// logger.info("Extracting directory: " + entry.getName());
					// This is not robust, just for demonstration purposes.
					// (new File(extractFolder + entry.getName())).mkdir();
					continue;
				} else {
					logger.info("Extracting file: " + entry.getName());
					fileName = entry.getName();
					copyInputStream(zipFile.getInputStream(entry),
							new BufferedOutputStream(new FileOutputStream(extractFolder + entry.getName())));
					break;
				}
			}
			
			return fileName;
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, null, ioe);
			return fileName;
		} finally {
			try {
				if(zipFile != null)
					zipFile.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unhandled exception:", e);
			}
		}
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 *
	 */
	public void logout() {
		if (connected && loggedIn) {
			// ftp.logout();
			loggedIn = false;
			logger.info("Logged out.");
		}
	}

	/**
	 *
	 */
	public void disconnect() {
		if (connected) {
			try {
				ssh.disconnect();
				logger.info("Disconnected.");
			} catch (Exception ex) {
				logger.log(Level.SEVERE, null, ex);
			}
			connected = false;
		}
	}

	public static String download(String host, int port, String user, String password, String remoteFilePath, String filePattern,
			String downloadFolder, String postReceiveLocation) {
		LFTPClient ftpClient = new LFTPClient(host, port, user, password);
		if (ftpClient.connect()) {
			if (ftpClient.login()) {
				String receivedFile = ftpClient.receiveFile(remoteFilePath, filePattern, downloadFolder);
				if (!"".equals(receivedFile)) {
					if (!postReceiveLocation.endsWith("/")) {
						postReceiveLocation = postReceiveLocation + "/";
					}
					String renameTo = postReceiveLocation + resolveFileNameToRename(receivedFile);
					return String.valueOf(ftpClient.renameFile(receivedFile, renameTo));
				}
			}
		}
		ftpClient.logout();
		ftpClient.disconnect();
		return Boolean.FALSE.toString();
	}

	/**
	 *
	 * @param filePath
	 * @return
	 */
	public static String resolveFileNameToRename(String filePath) {
		String fileName = retrieveFileName(filePath);
		String timeStampStr = new LSFTPClient().format.format(new Date());
		String renameFileName = fileName + ".processed" + timeStampStr;
		return renameFileName;
	}

	/**
	 *
	 * @param filePath
	 * @return
	 */
	public static String retrieveFileName(String filePath) {
		int lastIndex = filePath.lastIndexOf("/");
		String fileName = filePath.substring(lastIndex + 1, filePath.length());
		return fileName;
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		testDownload();
	}

	/**
	 *
	 */
	private static void testDownload() {
		String host = "https://172.31.1.98:6565";
		String user = "sftpuser";
		String password = "sFtp!us7r";
		String remoteFilePath = "/u01/sftpuser/samples/";
		// String downloadFolder = "d:/temp/";
		// String remoteFilePath = "/Home/esbftp/processed/";
		String downloadFolder = "d:/temp/";
		String fileName = "In2Cloud_Plan_4.zip";
		String processedFolder = "/tmp/processed/";
		String filePattern = "[\\w\\s-]*.([tTcC][xXsS][tTvV])";
		// download(host, user, password, remoteFilePath, filePattern, downloadFolder,
		// processedFolder);
		LSFTPClient ftpClient = new LSFTPClient(host, 22, user, password);
		if (ftpClient.connect()) {
			if (ftpClient.login()) {
				ftpClient.receiveFileAndRename(remoteFilePath + "/" + fileName, filePattern, downloadFolder, processedFolder, "");
				// ftpClient.sendFileAndRename(downloadFolder, remoteFilePath, fileName);

			}
		}
	}

	/**
	 *
	 */
	private static void testUpload() {
		String host = "localhost";
		String user = "HumanaVitality";
		String password = "Rxh4A$zMs";
		String remoteFilePath = "/Home/mckesson/test/inbound_eligibility/";
		// String downloadFolder = "d:/temp/";
		// String remoteFilePath = "/Home/esbftp/processed/";
		String downloadFolder = "d:/temp/";
		String fileName = "mckessondummy.csv";
		String processedFolder = "/Home/mckesson/test/inbound_eligibility/processed/";
		String filePattern = "[a-zA-Z0-9_-]*.([cC][sS][vV])";
		// download(host, user, password, remoteFilePath, filePattern, downloadFolder,
		// processedFolder);
		LSFTPClient ftpClient = new LSFTPClient(host, 22, user, password);
		if (ftpClient.connect()) {
			if (ftpClient.login()) {

				ftpClient.sendFileAndRename(downloadFolder, remoteFilePath, fileName);

			}
		}
	}

	/**
	 *
	 * @param fileName
	 * @param downloadFolder
	 * @return
	 */
	private boolean isAlreadyDownloaded(String fileName, String downloadFolder) {
		if (!downloadFolder.endsWith("/")) {
			downloadFolder = downloadFolder + "/";
		}
		File f = new File(downloadFolder + fileName);
		logger.info(downloadFolder + fileName + " Already downloaded? " + f.exists());
		return f.exists();
	}
}
