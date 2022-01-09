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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import com.logicoy.bpelmon.models.FileDetails;

/**
 *
 * @author Shrivats
 */
public class LFTPClient {

    private static final Logger logger = Logger.getLogger(LFTPClient.class.getName());
    private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final int DATA_TIMEOUT = 60 * 60 * 1000; //1 hour
    private static final int CONNECTION_TIMEOUT = 60 * 60 * 1000;
    private String host;
    private String user;
    private String password;
    private int port;
    private static FTPClient ftp = null;
    private boolean connected = false;
    private boolean loggedIn = false;

    /**
     *
     * @param host
     * @param user
     * @param password
     */
    public LFTPClient(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }
    static {
    	if(ftp == null)
    		ftp = new FTPClient();
    }

    public LFTPClient() {
		// TODO Auto-generated constructor stub
	}

    public boolean connect() {
        try {
            logger.info("Connecting to " + host);
            if(!ftp.isConnected())
            	ftp.connect(host, port);
            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                logger.severe("FTP server refused connection.");
                connected = false;
                return false;
            }
            logger.info("Connected to " + host);
            connected = true;
        } catch (IOException ex) {
            Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);

            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ex1) {
                    Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return connected;
    }

    /**
     *
     * @return
     */
    public boolean login() {
        if (connected) {
            try {
                loggedIn = ftp.login(user, password);

                if (!loggedIn) {
                    ftp.logout();
                } else {
                    logger.info("Successfully logged in ");
                }
            } catch (IOException ex) {
                Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
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
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                ftp.setDataTimeout(DATA_TIMEOUT);
                ftp.setConnectTimeout(CONNECTION_TIMEOUT);

                //ftp.changeWorkingDirectory(remoteFilePath);
                if (!remoteFilePath.endsWith("/")) {
                    remoteFilePath = remoteFilePath + "/";
                }
                if (!downloadFolder.endsWith("/")) {
                    downloadFolder = downloadFolder + "/";
                }
                FTPFile[] fileNames = ftp.listFiles(remoteFilePath);
                if (fileNames != null) {
                    //logger.info("Listing filenames in " + remoteFilePath + ": " + fileNames);
                    for (FTPFile ftpFile : fileNames) {
                        logger.info(ftpFile.getName());
                        boolean matches = Pattern.matches(filePattern, ftpFile.getName());
                        if (matches) {
                            OutputStream output = new FileOutputStream(downloadFolder + ftpFile.getName());
                            logger.info("downloading file.." + ftpFile.getName());
                            received = ftp.retrieveFile(remoteFilePath + ftpFile.getName(), output);
                            logger.info("download status :" + received);
                            if (received) {
                                //receivedFile = remoteFilePath + ftpFile.getName();
                                receivedFile = ftpFile.getName();
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
            Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return receivedFile;
    }
    
    /**
     *
     * @param remoteFilePath
     * @return
     */
    public List<FileDetails> receiveFileList(String remoteFilePath) {
        List<FileDetails> fileList = new ArrayList<>();
        try {
            if (connected && loggedIn) {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                ftp.setDataTimeout(DATA_TIMEOUT);
                ftp.setConnectTimeout(CONNECTION_TIMEOUT);
                
                FTPFile[] fileNames = ftp.listFiles(remoteFilePath);                
                if (fileNames != null) {
                    for (FTPFile ftpFile : fileNames) {
                        FileDetails fileDetail = new FileDetails();
                        fileDetail.setFileName(ftpFile.getName());
                        fileDetail.setCreatedTime(ftpFile.getTimestamp().getTime());
                        fileDetail.setFileSize(ftpFile.getSize());
                        
                        if(!ftpFile.isDirectory()) {
                        	fileDetail.setType("file");
                        	fileDetail.setFileExtension(ftpFile.getName().substring(ftpFile.getName().lastIndexOf('.') + 1));
                        } else {
                        	fileDetail.setType("dir");
                        	fileDetail.setFileExtension("directory");
                        }
                        fileList.add(fileDetail);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileList;
    }


    /**
     *
     * @param localFilePath
     * @param remoteFilePath
     * @param fileName
     * @return boolean
     */
    public boolean sendFileAndRename(String localFilePath, String remoteFilePath, String fileName) {
        boolean processed = false;
        try {
            if (connected && loggedIn) {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                ftp.setDataTimeout(DATA_TIMEOUT);
                ftp.setConnectTimeout(CONNECTION_TIMEOUT);
            }
            //ftp.changeWorkingDirectory(remoteFilePath);
            if (!localFilePath.endsWith("\\")) {
                localFilePath = localFilePath + "\\";
            }
            if (!remoteFilePath.endsWith("/")) {
                remoteFilePath = remoteFilePath + "/";
            }
            logger.log(Level.INFO, "localFilePath{0}", localFilePath);
            FileInputStream fis = null;
            File file = null;
            try {
                file = new File(localFilePath + fileName);
                fis = new FileInputStream(file);
                ftp.storeFile(remoteFilePath + fileName, fis);
                logger.log(Level.INFO, "Uploaded file to :{0}{1}", new Object[]{remoteFilePath, fileName});
                processed = true;
            } catch (Exception e) {
                Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
            // move the file to the process folder.
            File processedDir = new File(localFilePath + "processed");
            if (!processedDir.exists()) {
                processedDir.mkdir();
            }
            // move the file to process folder
            if (file != null) {
                boolean moved = file.renameTo(new File(localFilePath + "processed", fileName));
            }
            return processed;
        } catch (IOException ex) {
            Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return processed;
    }

    /**
     *
     * @param remoteFilePath
     * @param filePattern
     * @param downloadFolder
     * @param postReceiveLocation
     * @return
     */
    public String receiveFileAndRename(String remoteFilePath, String filePattern, String downloadFolder, String postReceiveLocation) {

        String receivedFile = "";
        boolean received = false;
        try {
            if (connected && loggedIn) {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.enterLocalPassiveMode();
                ftp.setDataTimeout(DATA_TIMEOUT);
                ftp.setConnectTimeout(CONNECTION_TIMEOUT);

                //ftp.changeWorkingDirectory(remoteFilePath);
                if (!remoteFilePath.endsWith("/")) {
                    remoteFilePath = remoteFilePath + "/";
                }
                if (!downloadFolder.endsWith("/")) {
                    downloadFolder = downloadFolder + "/";
                }
                FTPFile[] fileNames = ftp.listFiles(remoteFilePath);
                if (fileNames != null) {
                    logger.info("Listing filenames in " + remoteFilePath);
                    for (FTPFile ftpFile : fileNames) {
                        String ftpFileName = ftpFile.getName();
                        logger.info(ftpFileName);
                        boolean matches = Pattern.matches(filePattern, ftpFileName);
                        if (matches) {

                            File outputFile = new File(downloadFolder + ftpFileName + ".tmp");
                            if (!isAlreadyDownloaded(ftpFileName + ".tmp", downloadFolder)) {

                                OutputStream output = new FileOutputStream(outputFile);
                                logger.info("downloading file.." + ftpFileName);
                                try {
                                    received = ftp.retrieveFile(remoteFilePath + ftpFileName, output);
                                } catch (Exception e) {
                                    logger.severe("Exception while downloading file: " + ftpFileName + " : " + e.getMessage());
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
                                boolean renameStatus = ftp.rename(remoteFilePath + ftpFileName, renameTo);
                                logger.info("RENAME status : " + renameStatus);
                                if (renameStatus == false) {
                                    receivedFile = "";
                                } else {

                                    File downloadedFile = new File(downloadFolder + ftpFileName + ".tmp");
                                    boolean localRenamed = downloadedFile.renameTo(new File(downloadFolder + ftpFileName));
                                    logger.info("local renamed:" + localRenamed);
                                    if (!localRenamed) {
                                        boolean deleted = deleteIfExists(downloadFolder + ftpFileName);
                                        logger.info("is local deleted for overwriting? " + deleted);
                                        if (deleted) {
                                            localRenamed = downloadedFile.renameTo(new File(downloadFolder + ftpFileName));
                                            logger.info("local renamed:" + localRenamed);
                                        }
                                    }
                                    if (localRenamed) {
                                        if (ftpFileName.endsWith(".zip") || ftpFileName.endsWith("ZIP")) {
                                            receivedFile = unzip(downloadFolder + ftpFileName, downloadFolder);
                                            logger.info("Unzipped File: " + receivedFile);
                                        } else {
                                            receivedFile = ftpFileName;
                                        }
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
            received = false;
            Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                renamed = ftp.rename(from, to);
                logger.info("RENAME status : " + renamed);
            } catch (IOException ex) {
                Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
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
                    //logger.info("Extracting directory: " + entry.getName());
                    // This is not robust, just for demonstration purposes.
                    //(new File(extractFolder + entry.getName())).mkdir();
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
            logger.log(Level.SEVERE, "Unhandled exception:", ioe);
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
    private static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
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
            try {
                ftp.logout();
                logger.info("Logged out.");
            } catch (IOException ex) {
                Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            loggedIn = false;
        }
    }

    /**
     *
     */
    public void disconnect() {
        if (connected) {
            try {
                ftp.disconnect();
                logger.info("Disconnected.");
            } catch (IOException ex) {
                Logger.getLogger(LFTPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            connected = false;
        }
    }

    /**
     *
     * @param host
     * @param user
     * @param password
     * @param remoteFilePath
     * @param filePattern
     * @param downloadFolder
     * @param postReceiveLocation
     * @return
     */
    public static String download(String host,int port, String user, String password, String remoteFilePath, String filePattern, String downloadFolder, String postReceiveLocation) {
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
        String timeStampStr = new LFTPClient().format.format(new Date());
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
