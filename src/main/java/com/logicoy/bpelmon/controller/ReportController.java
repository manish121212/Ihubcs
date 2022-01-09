package com.logicoy.bpelmon.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.logicoy.bpelmon.DAO.ReportRequest;
import com.logicoy.bpelmon.DAO.TransactionRequest;
import com.logicoy.bpelmon.models.GenericResponseModel;
import com.logicoy.bpelmon.models.ReportConfig;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailModel;
import com.logicoy.bpelmon.models._TransactionSourceModel;
import com.logicoy.bpelmon.utils.InspirageReportGenerator;
import com.logicoy.bpelmon.utils.SUNameReader;
import java.util.logging.Level;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	@Autowired
	ReportRequest reportReq;
	@Autowired
	TransactionRequest transReq;
	@Autowired
	InspirageReportGenerator iRG;
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy - HH:mm:ss");

	// @GetMapping("/generateTransactionReport")
	public ResponseEntity<Resource> generateTransactionReport(String clientId, String startDate, String endDate,
			String[] reportConfig, String timezone, String sign) {
		System.gc();
		logger.log(Level.INFO, "Start date: {0}", startDate);
		logger.log(Level.INFO, "End date: {0}", endDate);
		logger.log(Level.INFO, "client id: {0} {1}", new Object[] { clientId, clientId.equalsIgnoreCase("null") });
		logger.log(Level.INFO, "Report config: {0}", Arrays.toString(reportConfig));
		logger.log(Level.INFO, "TimeZone: {0} {1}", new Object[] { timezone, sign });

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		RequestModel reqModel = new RequestModel();
		reqModel.setStartDate(startDate);
		reqModel.setEndDate(endDate);
		reqModel.setClientId(clientId.equalsIgnoreCase("null") ? null : clientId);
		// String timezoneWithSign = (sign.equalsIgnoreCase("plus") ? "+" : "-") +
		// timezone;
		String timezoneWithSign = "+0000";
		if (sign != null && !sign.isEmpty()) {
			timezoneWithSign = (sign.equalsIgnoreCase("plus") ? "+" : "-") + timezone;
		}
		reqModel.setTimezone(timezoneWithSign);
		ReportConfig repConfig = new ReportConfig();

		repConfig.setCompleted(reportConfig[0].equalsIgnoreCase("true"));
		repConfig.setFaulted(reportConfig[1].equalsIgnoreCase("true"));
		String reportTitle = "Transaction report from date period ";
		if (reportConfig[0].equalsIgnoreCase("true")) {
			reportTitle = "Completed transaction report from date period ";
		} else if (reportConfig[1].equalsIgnoreCase("true")) {
			reportTitle = "Faulted transaction report from date period ";
		}

		if (reportConfig[2].equalsIgnoreCase("true")) {
			logger.info("Service report type");
			// Fetch details for service transaction
			List<ServiceTransactionDetailModel> serviceTransactionList = transReq
					.getServiceTransactionDetails(reqModel, 1);
			logger.log(Level.INFO, "Service ist size: {0}", serviceTransactionList.size());
			if (serviceTransactionList.size() == 0) {
				return ResponseEntity.ok().contentLength(0).header("isEmpty", "true")
						.contentType(MediaType.parseMediaType("application/json")).body(null);
			}
			List<String> headerList = new ArrayList<>();
			Map<String, List<List<String>>> mapp = new HashMap<>();
			List<List<String>> data = new ArrayList<>();
			headerList.add("Service Name");
			headerList.add("Started");
			headerList.add("Running");
			headerList.add("Completed");
			headerList.add("Faulted");

			for (ServiceTransactionDetailModel model : serviceTransactionList) {
				List<String> dataList = new ArrayList<>();
				dataList.add(String.valueOf(model.getStarted() + model.getCompleted() + model.getFaulted()));
				dataList.add(String.valueOf(model.getStarted()));
				dataList.add(String.valueOf(model.getCompleted()));
				dataList.add(String.valueOf(model.getFaulted()));
				data.add(dataList);
				String name = model.getSuName().split("-")[0];
				try {
					String dName = SUNameReader.getValue(name);
					if (dName != null && dName.trim().length() > 0) {
						name = dName;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mapp.put(name, data);
				data = new ArrayList<>();
			}
			try {
				String generatedDate = sdf.format(reqModel.getStartDate()) + " to " + sdf.format(reqModel.getEndDate());
				Date generationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

				String fileName = "IHubCSTransReport_" + new SimpleDateFormat("MM-dd_HHmmss").format(generationTime)
						+ ".pdf";

				boolean result = iRG.generateReport(fileName,
						"Service transaction summary report from date period " + generatedDate,
						sdf.format(generationTime), headerList, mapp);
				if (result) {
					// File generated. Fetch from location.
					logger.info("++++++++++ Fetching file +++++++++");
					File f = new File(fileName);
					logger.log(Level.INFO, "File {0} exists: {1} at {2}",
							new Object[] { fileName, f.exists(), f.getAbsolutePath() });
					if (f.exists()) {
						InputStreamResource resource = new InputStreamResource(new FileInputStream(f));
						// f.delete();
						return ResponseEntity.ok().contentLength(f.length())
								.contentType(MediaType.parseMediaType("application/octet-stream"))
								.header("Content-disposition", "attachment; filename=" + fileName).body(resource);
					}
				}
				logger.log(Level.INFO, "Conversion status: {0}", result);
			} catch (Exception e) {
				logger.severe(e.getMessage() + " Caused by: " + e.getCause());
			}
		}
		reqModel.setReportConfig(repConfig);
		List<_TransactionSourceModel> transactionList = reportReq.generateReport(reqModel);
		logger.log(Level.INFO, "Transaction size: {0}", transactionList.size());
		if (transactionList.isEmpty()) {
			return ResponseEntity.ok().contentLength(0).header("isEmpty", "true")
					.contentType(MediaType.parseMediaType("application/json")).body(null);
		}

		Map<String, Map<String, List<List<String>>>> serviceData = new HashMap<>();
		Map<String, List<List<String>>> serviceDataList = new HashMap<>();
		Map<String, List<String>> serviceHeaderList = new HashMap<>();
		String arr[] = null;
		for (_TransactionSourceModel trans : transactionList) {
			String showableData[] = trans.getShowableData().split("\\<\\|\\|\\>");
			String name = trans.getSuName().split("-")[0];
			try {
				String dName = SUNameReader.getValue(name);
				if (dName != null && dName.trim().length() > 0) {
					name = dName;
				}
			} catch (Exception e) {
				logger.severe(e.getMessage() + " Caused by: " + e.getCause());
			}

			if (serviceData.containsKey(name)) {

				List<String> headerList = serviceHeaderList.get(name);
				headerList.clear();
				String headers[] = trans.getShowableData().split("\\<\\|\\|\\>");
				String[] pTid = trans.getPrimaryTrackingId().split("\\~\\#\\~");// TODO REVERT
				String pid = pTid.length > 1 ? pTid[0] : "PRIMARY-TRACKING-ID";
				headerList.add(pid);
				headerList.add("Service Name");
				headerList.add("Instance Id");
				// logger.info("===== Headers list =====");
				int displayHeaderCount = headers.length;
				for (int i = 0; i < displayHeaderCount; i++) {
					String data1 = "";
					try {
						data1 = showableData[i];
					} catch (ArrayIndexOutOfBoundsException ex) {
						data1 = "";
					}
					arr = data1.split("(\\~\\#\\~)");
					String formatDate = arr.length > 1 ? arr[0] : "TRACKING-VAR-" + (i + 1);
					headerList.add(formatDate);
				}
				headerList.add("Event\nTime");
				headerList.add("Status");
				// logger.info("===== Headers list =====");
				// logger.info(headerList.toString());
				// logger.info("===== Data list =====");
				serviceHeaderList.put(name, headerList);

			} else {
				List<List<String>> dataT = new ArrayList<>();
				Map<String, List<List<String>>> mappT = new HashMap<>();
				serviceDataList.put(name, dataT);
				serviceData.put(name, mappT);
				List<String> headerList = new ArrayList<>();

				String headers[] = trans.getShowableData().split("\\<\\|\\|\\>");
				String[] pTid = trans.getPrimaryTrackingId().split("\\~\\#\\~");// TODO REVERT
				String pid = pTid != null && pTid.length > 1 ? pTid[0] : "PRIMARY-TRACKING-ID";
				headerList.add(pid);
				headerList.add("Service Name");
				headerList.add("Instance Id");
				// logger.info("===== Headers list =====");
				int displayHeaderCount = headers.length;
				for (int i = 0; i < displayHeaderCount; i++) {
					String data1 = "";
					try {
						data1 = showableData[i];
					} catch (ArrayIndexOutOfBoundsException ex) {
						data1 = "";
					}
					arr = data1.split("(\\~\\#\\~)");
					String formatDate = arr.length > 1 ? arr[0] : "TRACKING-VAR-" + (i + 1);
					headerList.add(formatDate);
				}
				headerList.add("Event\nTime");
				headerList.add("Status");
				// logger.info("===== Headers list =====");
				// logger.info(headerList.toString());
				// logger.info("===== Data list =====");
				serviceHeaderList.put(name, headerList);
			}
		}

		for (_TransactionSourceModel trans : transactionList) {
			String name = trans.getSuName().split("-")[0];
			try {
				String dName = SUNameReader.getValue(name);
				if (dName != null && dName.trim().length() > 0) {
					name = dName;
				}
			} catch (Exception e) {
				logger.severe(e.getMessage() + " Caused by: " + e.getCause());
			}
			Map<String, List<List<String>>> mapp = serviceData.get(name);
			List<List<String>> data = serviceDataList.get(name);
			List<String> headerList = serviceHeaderList.get(name);
			// Get sub transactions
			reqModel.setPrimaryTrackingValue(trans.getPrimaryTrackingValue());
			reqModel.setInstanceId(trans.getInstanceId());
			// List<_TransactionSourceModel> subTransList =
			// transReq.getSubServiceTransactions(reqModel);
			// logger.info("+++++++ sub transactions : " + subTransList.size());
			// logger.info(subTransList.toString());
			// logger.info("Transaction id: " + trans.getTransactionId());
			// List<_TransactionSourceModel> matchList = subTransList.stream()
			// .filter(p -> p.getTransactionId().equalsIgnoreCase(trans.getTransactionId()))
			// .collect(Collectors.toList());

			String showableData[] = trans.getShowableData().split("\\<\\|\\|\\>");

			List<String> dataList = new ArrayList<>();
			// dataList.add(trans.getPrimaryTrackingValue());
			dataList.add(name);
			dataList.add(trans.getInstanceId());
			int displayHeaderCount = headerList.size() - 5;
			for (int i = 0; i < displayHeaderCount; i++) {
				String data1 = "";
				try {
					data1 = showableData[i];
				} catch (ArrayIndexOutOfBoundsException ex) {
					data1 = "";
				}
				arr = data1.split("(\\~\\#\\~)");
				// logger.info(arr[0] + " :: " + arr[1]);
				String formatDate = arr.length > 1 ? arr[1] : "NA";
				// try {
				// formatDate =
				// sdf1.format(javax.xml.bind.DatatypeConverter.parseDateTime(arr[1]));
				// } catch(IllegalArgumentException ex) {
				// logger.severe(ex.getMessage());
				// formatDate = arr[1];
				// }
				dataList.add(formatDate);
			}
			dataList.add(trans.getEventTime());
			dataList.add(
					trans.getStatus().substring(0, 1).toUpperCase() + trans.getStatus().substring(1).toLowerCase());
			// logger.log(Level.INFO, "Data for {0} : {1}",new Object[] {
			// trans.getPrimaryTrackingValue(), dataList.toString() });
			data.add(dataList);
			// // Add values for sub transaction. If any
			// if (matchList.size() > 0) {
			// for (_TransactionSourceModel subTrans : matchList) {
			// showableData = subTrans.getShowableData().split("\\<\\|\\|\\>");
			// dataList = new ArrayList<>();
			// dataList.add(subTrans.getSuName());
			// dataList.add(subTrans.getInstanceId());
			// for (String data1 : showableData) {
			// arr = data1.split("(\\~\\#\\~)");
			// dataList.add(arr[1]);
			// }
			// dataList.add(subTrans.getEventTime());
			// dataList.add(subTrans.getStatus().toLowerCase(Locale.US));
			// data.add(dataList);
			// }
			// }
			String headerkey[] = trans.getPrimaryTrackingId().split("\\~\\#\\~");
			if (headerkey.length > 1)
				mapp.put(headerkey[1], data);
			else
				mapp.put("NA", data);
		}
		try {
			String generatedDate = sdf.format(reqModel.getStartDate()) + " to " + sdf.format(reqModel.getEndDate());
			Date generationTime = Calendar.getInstance().getTime();

			String fileName = "IHubCSTransReport_" + new SimpleDateFormat("MM-dd_HHmmss").format(generationTime)
					+ ".pdf";

			boolean result = iRG.generateReport(fileName, reportTitle + generatedDate, sdf.format(generationTime),
					serviceHeaderList, serviceData, reqModel.getClientId());
			if (result) {
				// File generated. Fetch from location.
				logger.info("++++++++++ Fetching file +++++++++");
				File f = new File(fileName);
				logger.log(Level.INFO, "File {0} exists: {1} at {2}",
						new Object[] { fileName, f.exists(), f.getAbsolutePath() });
				if (f.exists()) {
					InputStreamResource resource = new InputStreamResource(new FileInputStream(f));
					// f.delete();
					return ResponseEntity.ok().contentLength(f.length())
							.contentType(MediaType.parseMediaType("application/octet-stream"))
							.header("Content-disposition", "attachment; filename=" + fileName).body(resource);
				}
			}
			logger.log(Level.INFO, "Conversion status: {0}", result);
		} catch (Exception e) {
			logger.severe(e.getMessage() + " Caused by: " + e.getCause());
		}

		return ResponseEntity.ok().contentLength(0).header("isEmpty", "true")
				.contentType(MediaType.parseMediaType("application/json")).body(null);
	}

	@GetMapping("/generateTransactionReport")
	public void generateTransactionReportNew(String clientId, String startDate, String endDate, String[] reportConfig,
			String timezone, String sign, HttpServletResponse httpServletResponse) {
		int start = 0;
		int size = 500;
		logger.log(Level.INFO, "Start date: {0}", startDate);
		logger.log(Level.INFO, "End date: {0}", endDate);
		logger.log(Level.INFO, "client id: {0} {1}", new Object[] { clientId, clientId.equalsIgnoreCase("null") });
		logger.log(Level.INFO, "Report config: {0}", Arrays.toString(reportConfig));
		logger.log(Level.INFO, "TimeZone: {0} {1}", new Object[] { timezone, sign });

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		RequestModel reqModel = new RequestModel();
		reqModel.setStartDate(startDate);
		reqModel.setEndDate(endDate);
		reqModel.setClientId(clientId.equalsIgnoreCase("null") ? null : clientId);
		// String timezoneWithSign = (sign.equalsIgnoreCase("plus") ? "+" : "-") +
		// timezone;
		String timezoneWithSign = "+0000";
		if (sign != null && !sign.isEmpty()) {
			timezoneWithSign = (sign.equalsIgnoreCase("plus") ? "+" : "-") + (timezone.length() == 2 ? timezone + "00" : timezone);
		}
		reqModel.setTimezone(timezoneWithSign);
		ReportConfig repConfig = new ReportConfig();

		repConfig.setCompleted(reportConfig[0].equalsIgnoreCase("true"));
		repConfig.setFaulted(reportConfig[1].equalsIgnoreCase("true"));
		repConfig.setExcel("true".equalsIgnoreCase(reportConfig[3]));
		String reportTitle = "Transaction report from date period ";
		reqModel.setReportConfig(repConfig);
		if (reportConfig[0].equalsIgnoreCase("true")) {
			reportTitle = "Completed transaction report from date period ";
		} else if (reportConfig[1].equalsIgnoreCase("true")) {
			reportTitle = "Faulted transaction report from date period ";
		}

		if (reportConfig[2].equalsIgnoreCase("true")) {
			logger.info("Service report type");
			// Fetch details for service transaction
			List<ServiceTransactionDetailModel> serviceTransactionList = transReq
					.getServiceTransactionDetails(reqModel, 1);
			logger.log(Level.INFO, "Service list size: {0}", serviceTransactionList.size());
			// if (serviceTransactionList.size() == 0) {
			// return ResponseEntity.ok().contentLength(0).header("isEmpty", "true")
			// .contentType(MediaType.parseMediaType("application/json")).body(null);
			// }
			List<String> headerList = new ArrayList<>();
			Map<String, List<List<String>>> mapp = new HashMap<>();
			List<List<String>> data = new ArrayList<>();
			headerList.add("Service Name");
			headerList.add("Started");
			headerList.add("Running");
			headerList.add("Completed");
			headerList.add("Faulted");
			headerList.add("Average Processing Time");
			long millis = 0;
			String avgProcessingTime = "";
			for (ServiceTransactionDetailModel model : serviceTransactionList) {
				List<String> dataList = new ArrayList<>();
				dataList.add(String.valueOf(model.getStarted() + model.getCompleted() + model.getFaulted()));
				dataList.add(String.valueOf(model.getStarted()));
				dataList.add(String.valueOf(model.getCompleted()));
				dataList.add(String.valueOf(model.getFaulted()));
				millis = model.getAverageTransactionTime();
				avgProcessingTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
			            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
			            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
				dataList.add(avgProcessingTime);
				data.add(dataList);
				String name = model.getSuName().split("-")[0];
				try {
					String dName = SUNameReader.getValue(name);
					if (dName != null && dName.trim().length() > 0) {
						name = dName;
					}
				} catch (Exception e) {

				}
				mapp.put(name, data);
				data = new ArrayList<>();
			}
			File f = null;
			try {
				String generatedDate = sdf.format(reqModel.getStartDate()) + " to " + sdf.format(reqModel.getEndDate());
				Date generationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

				String fileName = "IHubCSTransReport_" + new SimpleDateFormat("MM-dd_HHmmss").format(generationTime)
						+ ".pdf";

				boolean result = iRG.generateReport(fileName,
						"Service transaction summary report from date period " + generatedDate,
						sdf.format(generationTime), headerList, mapp);
				if (result) {
					// File generated. Fetch from location.
					logger.info("++++++++++ Fetching file +++++++++");
					f = new File(fileName);
					logger.log(Level.INFO, "File {0} exists: {1} at {2}",
							new Object[] { fileName, f.exists(), f.getAbsolutePath() });
					// Return file
					httpServletResponse.setContentType("application/pdf");
					httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + f.getName());
					httpServletResponse.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
					OutputStream out = httpServletResponse.getOutputStream();
					FileInputStream in = new FileInputStream(f);
					IOUtils.copy(in, out);
					out.close();
					in.close();
					f.delete();
				}
				logger.log(Level.INFO, "Conversion status: {0}", result);
			} catch (Exception e) {
				logger.severe(e.getMessage() + " Caused by: " + e.getCause());
				if(f != null && f.exists()) {
					f.delete();
					f = null;
				}
			}
		} else {
			reqModel.setReportConfig(repConfig);
			int transListSize = 0;
			reqModel.setFetchDataFrom(start);
			reqModel.setRecordSize(size);
			logger.info("Fetching report data: " + reqModel.getFetchDataFrom() + " size: " + reqModel.getRecordSize());
			List<_TransactionSourceModel> transactionList = reportReq.generateReportNew(reqModel);
			transListSize = transactionList != null ? transactionList.size() : 0;
			logger.info("Transaction list size: " + transListSize);
			
			while (transListSize != 0) {
				start = start + size;
				reqModel.setFetchDataFrom(start);
				List<_TransactionSourceModel> nextTransactionList = reportReq.generateReportNew(reqModel);
				if (nextTransactionList != null && nextTransactionList.size() > 0) {
					transListSize = nextTransactionList.size();
					transactionList.addAll(nextTransactionList);
//					logger.info("Report data FROM: " + reqModel.getFetchDataFrom() + " SIZE: "
//							+ reqModel.getRecordSize() + " Transaction list size: " + transListSize);
				} else {
					transListSize = 0;
					logger.info("No further records");
					break;
				}
				try {
					nextTransactionList = null;
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.log(Level.INFO, "Final Transaction size: {0}", transactionList.size());

			try {
				String generatedDate = sdf.format(reqModel.getStartDate()) + " to " + sdf.format(reqModel.getEndDate());
				Date generationTime = Calendar.getInstance().getTime();

				String fileName = "IHubCSTransReport_" + new SimpleDateFormat("MM-dd_HHmmss").format(generationTime)
						+ ".pdf";

				// boolean result = iRG.generateReport(fileName, reportTitle + generatedDate,
				// sdf.format(generationTime),
				// serviceHeaderList, serviceData);
				// boolean result = iRG.generateBgReport(fileName, reportTitle + generatedDate,
				// sdf.format(generationTime),
				// serviceHeaderList, serviceData, reqModel.getClientId()).get();
				if (reportConfig[3].equalsIgnoreCase("true")) {
					// Create excel
					File excelFile = reportReq.generateExcelReport(transactionList, reqModel);
					httpServletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64");
					httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFile.getName());
					httpServletResponse.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
					OutputStream out = httpServletResponse.getOutputStream();
					FileInputStream in = new FileInputStream(excelFile);
					IOUtils.copy(in, out);
					out.close();
					in.close();
					excelFile.delete();
					System.gc();
				} else {
					boolean result = iRG
							.generateBgReport(fileName, reportTitle + generatedDate, sdf.format(generationTime),
									this.createPDFData(transactionList, reqModel.getTimezone()), reqModel.getClientId())
							.get();
					if (result) {
						// File generated. Fetch from location.
						logger.info("++++++++++ Fetching file +++++++++");
						File f = new File(fileName);
						logger.log(Level.INFO, "File {0} exists: {1} at {2}",
								new Object[] { fileName, f.exists(), f.getAbsolutePath() });
						if (f.exists()) {
							// InputStreamResource resource = new InputStreamResource(new
							// FileInputStream(f));
							// f.delete();
							// return ResponseEntity.ok().contentLength(f.length())
							// .contentType(MediaType.parseMediaType("application/octet-stream"))
							// .header("Content-disposition", "attachment; filename=" +
							// fileName).body(resource);
							httpServletResponse.setContentType("application/pdf");
							httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + f.getName());
							httpServletResponse.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
							OutputStream out = httpServletResponse.getOutputStream();
							FileInputStream in = new FileInputStream(f);
							IOUtils.copy(in, out);
							out.close();
							in.close();
							f.delete();
							System.gc();
						}
					}
					logger.log(Level.INFO, "Conversion status: {0}", result);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				// Send error response
				// httpServletResponse.setStatus(200);
				try {
					httpServletResponse.sendError(200);
				} catch (IOException e1) {

					e1.printStackTrace();
				}
			}
		}
		// return ResponseEntity.ok().contentLength(0).header("isEmpty", "true")
		// .contentType(MediaType.parseMediaType("application/json")).body(null);
	}

	@GetMapping("/deleteReportFile")
	public GenericResponseModel deleteReportFile(String fileName) {
		GenericResponseModel model = new GenericResponseModel();
		model.setStatus(0);
		model.setMessage("");
		File f = new File(fileName);
		if (f.exists()) {
			if (f.delete()) {
				model.setStatus(1);
				model.setMessage("File deleted successfully");
			} else {

				model.setMessage("Unable to delete file " + fileName);
			}
		} else {
			model.setMessage("File " + fileName + " does not exist.");
		}
		return model;
	}

	private Map<String, List<String>> createPDFData(List<_TransactionSourceModel> transactionList, String timezone) {
		Map<String, List<String>> dataMap = new LinkedHashMap<>();
		sdf.setTimeZone(TimeZone.getTimeZone(timezone));
		List<String> dataList = null;
		for (_TransactionSourceModel transaction : transactionList) {
			dataList = dataMap.get(transaction.getSuName());
			if (dataList != null) {
				// Record exists in map
				dataList.add(transaction.getPrimaryTrackingId() + "<||>" + transaction.getShowableData() + "<||>"
						+ "Event Time~#~" + sdf.format(new Date(Long.parseLong(transaction.getEventSeqNo()))) + "<||>"
						+ "Status~#~" + transaction.getStatus());
				dataMap.put(transaction.getSuName(), dataList);
			} else {
				// Add new record in map
				dataList = new ArrayList<>();
				dataList.add(transaction.getPrimaryTrackingId() + "<||>" + transaction.getShowableData() + "<||>"
						+ "Event Time~#~" + sdf.format(new Date(Long.parseLong(transaction.getEventSeqNo()))) + "<||>"
						+ "Status~#~" + transaction.getStatus());
				dataMap.put(transaction.getSuName(), dataList);
			}
		}
		return dataMap;
	}
	@GetMapping("/generateTransactionReportTemp")
	public void generateTransactionReportNewPT(String clientId, String startDate, String endDate, String[] reportConfig,
			String timezone, String sign, HttpServletResponse httpServletResponse) throws IOException {
		RequestModel reqModel = new RequestModel();
		reqModel.setStartDate(startDate);
		reqModel.setEndDate(endDate);
		reqModel.setClientId(clientId.equalsIgnoreCase("null") ? null : clientId);
		// String timezoneWithSign = (sign.equalsIgnoreCase("plus") ? "+" : "-") +
		// timezone;
		String timezoneWithSign = "+0000";
		if (sign != null && !sign.isEmpty()) {
			timezoneWithSign = (sign.equalsIgnoreCase("plus") ? "+" : "-") + timezone;
		}
		reqModel.setTimezone(timezoneWithSign);
		logger.info("Start Date: " + reqModel.getStartDate() + " End Date: " + reqModel.getEndDate());
		File excelFile = reportReq.generateExcelReport(null, reqModel);
		httpServletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64");
		httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFile.getName());
		httpServletResponse.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
		OutputStream out = httpServletResponse.getOutputStream();
		FileInputStream in = new FileInputStream(excelFile);
		IOUtils.copy(in, out);
		out.close();
		in.close();
	}
}