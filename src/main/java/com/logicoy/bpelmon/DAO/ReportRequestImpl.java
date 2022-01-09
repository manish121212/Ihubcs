package com.logicoy.bpelmon.DAO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.logicoy.bpelmon.helpers.TransactionCounter;
import com.logicoy.bpelmon.helpers.TransactionUtils;
import com.logicoy.bpelmon.models.ReportConfig;
import com.logicoy.bpelmon.models.RequestModel;
import com.logicoy.bpelmon.models.ServiceTransactionDetailsResponse;
import com.logicoy.bpelmon.models._TransactionSourceModel;
import com.logicoy.bpelmon.utils.AppConstants;
import com.logicoy.bpelmon.utils.Utils;

@Repository
public class ReportRequestImpl implements ReportRequest {

	@Autowired
	TransactionUtils transUtils;
	@Autowired
	Utils utils;
	@Autowired
	AppConstants appConst;
	@Autowired
	TransactionCounter transCounter;
	Logger logger = Logger.getLogger(this.getClass().getName());
	private int[] portArray;
	HttpHost[] hostList;
	// Bold Font
	private XSSFCellStyle boldFontStyle = null;
	private XSSFFont boldFont = null;

	@PostConstruct
	private void init() {
		portArray = appConst != null ? appConst.getPort() : new int[] { 9200, 9300, 9400 };
		hostList = new HttpHost[portArray.length];
		for (int i = 0; i < portArray.length; i++) {
			hostList[i] = new HttpHost(appConst.getHost()[i], portArray[i], appConst.getProtocol());
		}
	}

	@Override
	public List<_TransactionSourceModel> generateReport(RequestModel reqModel) {

		// int[] portArray = appConst.getPort();
		// HttpHost[] hostList = new HttpHost[portArray.length];
		// logger = Logger.getLogger(this.getClass().getName());
		// for (int i = 0; i < portArray.length; i++) {
		// hostList[i] = new HttpHost(appConst.getHost(), portArray[i],
		// appConst.getProtocol());
		//
		// }
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		String includes[] = {};
		String excludes[] = { "transaction_xml", "raw_payload" };
		builder.fetchSource(includes, excludes);
		SearchRequest searchReq = utils.createTransactionIndexSearchRequest(reqModel.getClientId());
		ReportConfig reportConfig = reqModel.getReportConfig();

		builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel)));
		searchReq.source(builder);
		List<_TransactionSourceModel> transList;

		try (RestHighLevelClient eClient = new RestHighLevelClient(RestClient.builder(hostList))) {
			SearchResponse resp = eClient.search(searchReq);
			transList = transUtils.createSourceListNoOrder(resp.getHits().getHits(), 0);
			logger.info("Main List Size: " + transList.size());

			List<_TransactionSourceModel> finalList = utils.combineData(transList);

			if (reportConfig != null) {
				boolean isCompleted = reportConfig.isCompleted();
				boolean isFaulted = reportConfig.isFaulted();
				if (isCompleted && !isFaulted) {
					return finalList.stream().filter(p -> p.getStatus().equalsIgnoreCase("completed"))
							.collect(Collectors.toList());
				} else if (isFaulted && !isCompleted) {
					return finalList.stream().filter(p -> p.getStatus().equalsIgnoreCase("faulted"))
							.collect(Collectors.toList());
				}
			}
			return finalList;

		} catch (IOException e) {
			logger.severe("Exception: " + e.getMessage() + "  Caused by: " + e.getCause());

		} finally {
			transList = null;
			utils.closeElasticConnection();
		}
		return null;
	}

	@Override
	public List<_TransactionSourceModel> generateReportNew(RequestModel reqModel) {
		// portArray = appConst.getPort();
		// hostList = new HttpHost[portArray.length];
		logger = Logger.getLogger(this.getClass().getName());

		SearchSourceBuilder builder = utils.createBasicSearchBuilder(0, true, SortOrder.DESC);
		builder.fetchSource(new String[] { AppConstants.TRANSACTION_ID }, null);
		builder.from(reqModel.getFetchDataFrom());
		builder.size(reqModel.getRecordSize());
		// logger.info("Report From: " + reqModel.getFetchDataFrom() + " Size: " +
		// reqModel.getRecordSize());
		SearchRequest searchReq = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId());
		ReportConfig reportConfig = reqModel.getReportConfig();
		logger.info("Report From: " + reqModel.getFetchDataFrom() + " To: "
				+ (reqModel.getFetchDataFrom() + reqModel.getRecordSize()));
		builder.query(utils.getRangeQuery(reqModel));

		if (reportConfig != null) {
			boolean isCompleted = reportConfig.isCompleted();
			boolean isFaulted = reportConfig.isFaulted();
			if (isCompleted && !isFaulted) {
				builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel))
						.must(QueryBuilders.matchQuery(AppConstants.STATUS, "completed")));
			} else if (isFaulted && !isCompleted) {
				builder.query(QueryBuilders.boolQuery().must(utils.getRangeQuery(reqModel))
						.must(QueryBuilders.matchQuery(AppConstants.STATUS, "faulted")));
			}
		}
		searchReq.source(builder);
		// logger.info(builder.query().toString());
		List<_TransactionSourceModel> transList;

		try (RestHighLevelClient eClient = new RestHighLevelClient(RestClient.builder(hostList))) {
			SearchResponse resp = eClient.search(searchReq);
			// List<String> transactionIdList = ;
			transList = transCounter.getTransactionsForId(
					transCounter.createTransactionIdListFromHits(resp.getHits().getHits()), reqModel);
			// logger.info("Main List Size: " + transList.size());
			return utils.combineData(transList);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			transList = null;
		}
		return null;
	}

	public File writeExcel(List<_TransactionSourceModel> transactionList, String clientName, RequestModel reqModel) {
		int status = reqModel.getReportConfig().isCompleted() ? 1 : 0;
		String[] names = this.generateReportFileAndTitle(clientName, status, reqModel);
		String fileName = names[0];
		String reportTitle = names[1];
		// transactionList = utils.combineData(transactionList).stream()
		// .filter(p ->
		// p.getStatus().equalsIgnoreCase("faulted")).collect(Collectors.toList());
		// LOGGER.info("Excel Transaction list size: " + transactionList.size());
		Map<String, List<_TransactionSourceModel>> serviceMap = utils.createServiceWiseMap(transactionList);

		// Start Excel writing
		// LOGGER.log(Level.INFO, "Creating excel report for {0}", clientName);
		// fileLogger.info("Creating excel report for " + clientName);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheetOverview = workbook.createSheet("Overview - " + reportTitle);
		XSSFSheet sheetComprehensive = workbook.createSheet("Comprehensive -" + reportTitle);
		sheetComprehensive.setDefaultColumnWidth(50);
		sheetOverview.setDefaultColumnWidth(50);
		// Excel specific fonts
		/* Create HSSFFont object from the workbook */
		// Bold Font
		boldFontStyle = workbook.createCellStyle();
		boldFont = workbook.createFont();
		boldFont.setColor(IndexedColors.BLACK.getIndex());
		boldFont.setBold(true);
		boldFontStyle.setFont(boldFont);
		// Bold Large Font
		XSSFCellStyle boldFontLargeStyle = workbook.createCellStyle();
		XSSFFont boldFontLarge = workbook.createFont();
		boldFontLarge.setFontHeight(14);
		boldFontLargeStyle.setFont(boldFontLarge);
		// Red normal Font
		XSSFCellStyle redFontStyle = workbook.createCellStyle();
		XSSFFont redFont = workbook.createFont();
		redFont.setColor(IndexedColors.RED.getIndex());
		redFontStyle.setFont(redFont);

		XSSFCellStyle greenFontStyle = workbook.createCellStyle();
		XSSFFont greenFont = workbook.createFont();
		greenFont.setColor(IndexedColors.GREEN.getIndex());
		greenFontStyle.setFont(greenFont);

		// Top Bottom Border
		XSSFCellStyle borderedBoldStyle = workbook.createCellStyle();
		borderedBoldStyle.setFont(boldFont);
		borderedBoldStyle.setBorderTop(BorderStyle.MEDIUM);
		borderedBoldStyle.setBorderBottom(BorderStyle.MEDIUM);
		borderedBoldStyle.setBorderLeft(BorderStyle.MEDIUM);
		borderedBoldStyle.setBorderRight(BorderStyle.MEDIUM);
		// Text wrap bottom thin border
		XSSFCellStyle wrapThinBorderBottomStyle = workbook.createCellStyle();
		wrapThinBorderBottomStyle.setBorderBottom(BorderStyle.THIN);
		wrapThinBorderBottomStyle.setWrapText(true);

		XSSFCellStyle showableDataStyle = workbook.createCellStyle();
		showableDataStyle.setWrapText(true);
		showableDataStyle.setBorderBottom(BorderStyle.THIN);
		showableDataStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

		int rowNum = 0;

		// Create Overview - START
		Row overview = sheetOverview.createRow(rowNum++);

		Cell cell = overview.createCell(0);
		cell.setCellStyle(boldFontLargeStyle);
		cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
		cell.setCellValue(reportTitle);

		// overview = sheetOverview.createRow(rowNum++);
		// cell = overview.createCell(0, CellType.BLANK);
		//
		// overview = sheetOverview.createRow(rowNum++);
		// cell = overview.createCell(0);
		// cell.setCellStyle(boldFontLargeStyle);
		// cell.setCellValue("Overview");
		// cell.getCellStyle().setBorderBottom(BorderStyle.DASHED);
		//
		// overview = sheetOverview.createRow(rowNum++);
		// cell = overview.createCell(0, CellType.BLANK);

		Set<String> serviceKeySet = serviceMap.keySet();
		Iterator<String> serviceKeyIterator = serviceKeySet.iterator();

		Row serviceNameRow = sheetOverview.createRow(rowNum++);
		Cell serviceNameTitleCell = serviceNameRow.createCell(0);
		serviceNameRow.setRowStyle(serviceNameTitleCell.getCellStyle());

		while (serviceKeyIterator.hasNext()) {

			String suName = serviceKeyIterator.next();
			List<_TransactionSourceModel> transList = serviceMap.get(suName);

			// Create headers
			rowNum = this.createOverviewHeaders(sheetOverview, rowNum, transList.get(0));

			String secondHeader = getSecondTableHeader(transList.get(0))[0];
			Row trackingVarsValueRow = sheetOverview.createRow(rowNum++);
			int colNum = 0;
			Cell trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
			trackingVarsValueRow.setRowStyle(trackingVarsValueCell.getCellStyle());

			// LOGGER.log(Level.INFO, "Creating table data");
			transList = utils.combineData(transList);
			for (_TransactionSourceModel transaction : transList) {
				String[] primaryTrackingDisplay = transaction.getPrimaryTrackingId().split("\\~\\#\\~");

				String tblVal1 = primaryTrackingDisplay[0] + " : " + primaryTrackingDisplay[1];
				String tblVal2 = this.getSecondTableValue(transaction, secondHeader);
				String tblVal3 = transaction.getEventTime();

				trackingVarsValueCell.setCellValue(suName);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellValue(tblVal1);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellValue(tblVal2);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellValue(tblVal3);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellType(CellType.STRING);
				trackingVarsValueCell.setCellValue(transaction.getProcessingTime() / 1000 + " s");

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellType(CellType.STRING);
				trackingVarsValueCell.setCellValue(transaction.getStatus());

				// Create Separator
				// trackingVarsValueRow = sheet.createRow(rowNum++);
				// trackingVarsValueCell = trackingVarsValueRow.createCell(0, CellType.BLANK);

				// Reinitialize for next iteration
				trackingVarsValueRow = sheetOverview.createRow(rowNum++);
				colNum = 0;
				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
			}

			serviceNameRow = sheetOverview.createRow(rowNum++);
			serviceNameTitleCell = serviceNameRow.createCell(0);
			serviceNameRow.setRowStyle(serviceNameTitleCell.getCellStyle());
		}
		// Create Overview - END
		rowNum = 0;
		// Create Comprehensive Details Section - START

		Row comprehensiveDetailsRow = sheetComprehensive.createRow(rowNum++);
		cell = comprehensiveDetailsRow.createCell(0);
		cell.setCellStyle(boldFontLargeStyle);
		cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
		cell.setCellValue(reportTitle);

		comprehensiveDetailsRow = sheetComprehensive.createRow(rowNum++);

		cell = comprehensiveDetailsRow.createCell(0);
		cell.setCellStyle(boldFontLargeStyle);
		cell.setCellValue("Comprehensive Details");
		cell.getCellStyle().setBorderBottom(BorderStyle.DASHED);

		// comprehensiveDetailsRow = sheetComprehensive.createRow(rowNum++);

		serviceKeyIterator = serviceKeySet.iterator();
		// Write Comprehensive details - START

		while (serviceKeyIterator.hasNext()) {
			comprehensiveDetailsRow = sheetComprehensive.createRow(rowNum++);
			cell = comprehensiveDetailsRow.createCell(1, CellType.BLANK);

			String suName = serviceKeyIterator.next();
			// LOGGER.log(Level.INFO, "Creating comprehensive report for {0}", suName);
			List<_TransactionSourceModel> transList = serviceMap.get(suName);

			Row tableServiceNameHeaderRow = sheetComprehensive.createRow(rowNum++);
			Cell tSNHeaderCell = tableServiceNameHeaderRow.createCell(0);
			tSNHeaderCell.setCellStyle(boldFontStyle);
			tSNHeaderCell.setCellValue("Service Name");

			// Create headers

			Row tableDataRow = sheetComprehensive.createRow(rowNum++);
			int colNum = 0;
			Cell tableDataCell = tableDataRow.createCell(colNum++);
			// Add tracking var headers
			String[] showableValues = this.getHeaders(transList);
			String showableHeaderArr[] = new String[showableValues.length];
			int showableHeaderArrIndexCount = 0;
			String primaryTrackingArr[] = transList.get(0).getPrimaryTrackingId().split("\\~\\#\\~");

			tableDataCell.setCellStyle(borderedBoldStyle);
			tableDataCell.setCellValue(primaryTrackingArr[0]);

			tableDataCell = tableDataRow.createCell(colNum++);
			tableDataCell.setCellStyle(borderedBoldStyle);
			tableDataCell.setCellValue("Service Name");

			// tableDataCell = tableDataRow.createCell(colNum++);
			// tableDataCell.setCellStyle(borderedBoldStyle);
			// tableDataCell.setCellValue("Instance Id");
			tableDataCell = tableDataRow.createCell(colNum++);
			for (String key : showableValues) {
				String tblHeaderVal = key.split("\\~\\#\\~")[0];
				tableDataCell.setCellStyle(borderedBoldStyle);
				tableDataCell.setCellValue(tblHeaderVal);
				tableDataCell = tableDataRow.createCell(colNum++);
				showableHeaderArr[showableHeaderArrIndexCount] = tblHeaderVal;
			}
			// tableDataCell = tableDataRow.createCell(colNum++);
			tableDataCell.setCellStyle(borderedBoldStyle);
			tableDataCell.setCellValue("Status");

			tableDataCell = tableDataRow.createCell(colNum++);
			tableDataCell.setCellStyle(borderedBoldStyle);
			tableDataCell.setCellValue("Event Time");
			tableDataCell = tableDataRow.createCell(colNum++);
			tableDataCell.setCellStyle(borderedBoldStyle);
			tableDataCell.setCellValue("Processing Time");

			tableDataRow = sheetComprehensive.createRow(rowNum++);
			colNum = 0;
			tableDataCell = tableDataRow.createCell(colNum++);
			
			// Add tracking var data
			for (_TransactionSourceModel transaction : transList) {
				String[] transShowableValues = transaction.getShowableData().split("\\<\\|\\|\\>");
				tableDataCell.setCellStyle(showableDataStyle);
				tableDataCell.setCellValue(transaction.getPrimaryTrackingId().split("\\~\\#\\~")[1] != null ? transaction.getPrimaryTrackingId().split("\\~\\#\\~")[1] : "NA");

				tableDataCell = tableDataRow.createCell(colNum++);
				tableDataCell.setCellStyle(showableDataStyle);
				tableDataCell.setCellValue(transaction.getSuName());

				// tableDataCell = tableDataRow.createCell(colNum++);
				// tableDataCell.setCellStyle(showableDataStyle);
				// tableDataCell.setCellValue(transaction.getInstanceId());
				tableDataCell = tableDataRow.createCell(colNum++);
				if(showableValues.length != transShowableValues.length) {
					logger.info("ReportRequestImpl::writeExcel : Length DIFFERENT!");
					if(transShowableValues.length < showableValues.length) {
						// Missing headers
						transShowableValues = reconstructData(transShowableValues, showableValues);
					}
				}
				for (String key : transShowableValues) {
					
					String tblHeaderVal = key.split("\\~\\#\\~").length > 1
							? (key.split("\\~\\#\\~")[1].trim().length() > 1 ? key.split("\\~\\#\\~")[1] : "NA")
							: "NA";
					tableDataCell.setCellStyle(showableDataStyle);
					tableDataCell.setCellValue(tblHeaderVal);
					// tableDataCell.setCellStyle(wrapThinBorderBottomStyle);
					// tableDataCell.getCellStyle().setWrapText(true);
					// tableDataCell.getCellStyle().setBorderBottom(BorderStyle.THIN);

					tableDataCell = tableDataRow.createCell(colNum++);
				}
				// tableDataCell = tableDataRow.createCell(colNum++);
				tableDataCell.setCellStyle(showableDataStyle);
				tableDataCell.setCellValue(transaction.getStatus());

				tableDataCell = tableDataRow.createCell(colNum++);
				tableDataCell.setCellStyle(showableDataStyle);
				tableDataCell.setCellValue(transaction.getEventTime());

				tableDataCell = tableDataRow.createCell(colNum++);
				tableDataCell.setCellStyle(showableDataStyle);
				tableDataCell.setCellValue(transaction.getProcessingTime() + " ms");

				// Reinitialize for next iteration
				tableDataRow = sheetComprehensive.createRow(rowNum++);
				colNum = 0;
				tableDataCell = tableDataRow.createCell(colNum++);
			}
		}
		// Write Comprehensive details - END
		// Create Comprehensive Details Section - END

		// CREATE WORKBOOK - END
		// LOGGER.log(Level.INFO, "Creating excel file with name {0}", fileName);
		// fileLogger.info("Creating excel file with name " + fileName);
		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new File(fileName);
	}

	private String[] reconstructData(String[] transShowableValues, String[] showableValues) {
		String[] constructedVal = new String[showableValues.length];
		int flag = 0;
		for(int i = 0 ; i < showableValues.length ; i++) {
			flag = 0;
			String toBeAdded = showableValues[i];
			for(String key: transShowableValues) {
				if(key.split("\\~\\#\\~")[0].equalsIgnoreCase(toBeAdded.split("\\~\\#\\~")[0])) {
					toBeAdded = key;
					flag = 1;
					break;
				}
			}
			if(flag == 0) {
				toBeAdded = showableValues[i].split("\\~\\#\\~")[0] + "~#~NA";
			}
			constructedVal[i] = toBeAdded;
		}
		return constructedVal;
	}

	private int createOverviewHeaders(XSSFSheet sheet, int rowNum, _TransactionSourceModel transactionSourceModel) {
		Row headerRow = sheet.createRow(rowNum++);
		String tableHeader = "Primary Tracking Display";
		String secondHeader = getSecondTableHeader(transactionSourceModel)[0];
		String thirdHeader = "Event Time";

		Cell headerCell = headerRow.createCell(0);
		headerCell.setCellStyle(boldFontStyle);
		headerCell.setCellValue("Service Name");
		headerCell = headerRow.createCell(1);
		headerCell.setCellStyle(boldFontStyle);
		headerCell.setCellValue(tableHeader);
		headerCell = headerRow.createCell(2);
		headerCell.setCellStyle(boldFontStyle);
		headerCell.setCellValue(secondHeader);
		headerCell = headerRow.createCell(3);
		headerCell.setCellStyle(boldFontStyle);
		headerCell.setCellValue(thirdHeader);
		headerCell = headerRow.createCell(4);
		headerCell.setCellStyle(boldFontStyle);
		headerCell.setCellValue("Processing Time(s)");
		headerCell = headerRow.createCell(5);
		headerCell.setCellStyle(boldFontStyle);
		headerCell.setCellValue("Status");
		return rowNum;
	}

	private String[] getHeaders(List<_TransactionSourceModel> transList) {
		String[] showableValues = new String[0];
		int max = 0;
		for (int i = 0; i < transList.size(); i++) {
			String[] curShowable = transList.get(i).getShowableData().split("\\<\\|\\|\\>");
			if (curShowable.length > max) {
				showableValues = curShowable;
				max = curShowable.length;
			}
		}
		return showableValues;
	}

	private String[] getSecondTableHeader(_TransactionSourceModel firstRecord) {
		String firstShowable = firstRecord.getShowableData().split("\\<\\|\\|\\>")[0];
		String keyValue[] = firstShowable.split("\\~\\#\\~");
		return keyValue;
	}

	private String getSecondTableValue(_TransactionSourceModel record, String header) {
		String showableArr[] = record.getShowableData().split("\\<\\|\\|\\>");
		for (String showable : showableArr) {
			String keyValPair[] = showable.split("\\~\\#\\~");
			if (header.equalsIgnoreCase(keyValPair[0])) {
				return keyValPair.length > 1 ? (keyValPair[1].trim().length() > 1 ? keyValPair[1] : "NA") : "NA";
			}
		}
		return "NA";
	}

	private String[] generateReportFileAndTitle(String clientName, int status, RequestModel reqModel) {
		String[] reportTitleArray = new String[2];
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy ");
		long startDate = reqModel.getStartDate();
		long endDate = reqModel.getEndDate();
		SimpleDateFormat sdfFile = new SimpleDateFormat("ddMMyy_");
		sdfFile.setTimeZone(TimeZone.getTimeZone(reqModel.getTimezone()));
		cal.setTimeInMillis(startDate);
		String fileName = "";
		String reportTitle = "";
		if (status == 0) {
			fileName = "FaultedReport-" + clientName + "-" + sdfFile.format(cal.getTime()) + "0000";
			reportTitle = "Faulted Transactions for period " + sdf.format(cal.getTime()) + "00:00:00";
		} else {
			fileName = "SuccessReport-" + clientName + "-" + sdfFile.format(cal.getTime()) + "0000";
			;
			reportTitle = "Successful Transactions for period " + sdf.format(cal.getTime()) + "00:00:00";
		}
		// cal = Calendar.getInstance();
		cal.setTimeInMillis(endDate);

		fileName += "-" + sdfFile.format(cal.getTime()) + "2359" + ".xlsx";
		reportTitle += " - " + sdf.format(cal.getTime()) + "23:59:59";
		reportTitleArray[0] = fileName;
		reportTitleArray[1] = reportTitle;
		return reportTitleArray;
	}

	@Override
	public File generateExcelReport(List<_TransactionSourceModel> transactionList, RequestModel reqModel) {
		return this.writeExcel(transactionList, reqModel.getClientId(), reqModel);
		// return this.generateExcelWithProcessingTime(reqModel);
	}

	public File generateExcelWithProcessingTime(RequestModel reqModel) {

		// reqModel.setServiceUnitName("LgfPOSCA-LgfPOSBPEL");
		// ServiceTransactionDetailsResponse data =
		// transCounter.getTransactionsForService(reqModel, 0, 1000, "");
		SearchSourceBuilder builder = utils.createBasicSearchBuilder(1, false, SortOrder.DESC);
		builder.query(utils.getRangeQuery(reqModel));
		AggregationBuilder uniqueSU = AggregationBuilders.terms("unique_su").field("su_name.keyword").size(50);
		builder.aggregation(uniqueSU);
		SearchRequest request = utils.createTransactionCountIndexSearchRequest(reqModel.getClientId());
		RestHighLevelClient eClient = utils.getElasticClient("");
		request.source(builder);
		List<String> serviceList = new ArrayList<>();
		try {
			SearchResponse resp = eClient.search(request);
			Terms uniqueSUTerms = resp.getAggregations().get("unique_su");

			for (Bucket suBucket : uniqueSUTerms.getBuckets()) {
				serviceList.add(suBucket.getKeyAsString());
			}
			logger.info("Total service: " + serviceList.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServiceTransactionDetailsResponse data;
		// serviceList.remove(0);
		if (serviceList.size() > 0) {
			for (String suName : serviceList) {
				reqModel.setServiceUnitName(suName);
				data = transCounter.getTransactionsForService(reqModel, 0, 100000, "");
				this.writeExcelWithPT(data, suName.replaceAll("\\\\", "_").replaceAll(":", "_").trim() + ".xlsx");
			}
		}
		return null;
	}

	private File writeExcelWithPT(ServiceTransactionDetailsResponse data, String fileName) {

		String reportTitle = "Report";
		// transactionList = utils.combineData(transactionList).stream()
		// .filter(p ->
		// p.getStatus().equalsIgnoreCase("faulted")).collect(Collectors.toList());
		// LOGGER.info("Excel Transaction list size: " + transactionList.size());
		Map<String, List<_TransactionSourceModel>> serviceMap = utils.createServiceWiseMap(data.getData());

		// Start Excel writing
		// LOGGER.log(Level.INFO, "Creating excel report for {0}", clientName);
		// fileLogger.info("Creating excel report for " + clientName);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheetOverview = workbook.createSheet("Overview - " + reportTitle);
		// XSSFSheet sheetComprehensive = workbook.createSheet("Comprehensive -" +
		// reportTitle);
		// sheetComprehensive.setDefaultColumnWidth(50);
		sheetOverview.setDefaultColumnWidth(50);
		// Excel specific fonts
		/* Create HSSFFont object from the workbook */
		// Bold Font
		boldFontStyle = workbook.createCellStyle();
		boldFont = workbook.createFont();
		boldFont.setColor(IndexedColors.BLACK.getIndex());
		boldFont.setBold(true);
		boldFontStyle.setFont(boldFont);
		// Bold Large Font
		XSSFCellStyle boldFontLargeStyle = workbook.createCellStyle();
		XSSFFont boldFontLarge = workbook.createFont();
		boldFontLarge.setFontHeight(14);
		boldFontLargeStyle.setFont(boldFontLarge);
		// Red normal Font
		XSSFCellStyle redFontStyle = workbook.createCellStyle();
		XSSFFont redFont = workbook.createFont();
		redFont.setColor(IndexedColors.RED.getIndex());
		redFontStyle.setFont(redFont);

		XSSFCellStyle greenFontStyle = workbook.createCellStyle();
		XSSFFont greenFont = workbook.createFont();
		greenFont.setColor(IndexedColors.GREEN.getIndex());
		greenFontStyle.setFont(greenFont);

		// Top Bottom Border
		XSSFCellStyle borderedBoldStyle = workbook.createCellStyle();
		borderedBoldStyle.setFont(boldFont);
		borderedBoldStyle.setBorderTop(BorderStyle.MEDIUM);
		borderedBoldStyle.setBorderBottom(BorderStyle.MEDIUM);
		borderedBoldStyle.setBorderLeft(BorderStyle.MEDIUM);
		borderedBoldStyle.setBorderRight(BorderStyle.MEDIUM);
		// Text wrap bottom thin border
		XSSFCellStyle wrapThinBorderBottomStyle = workbook.createCellStyle();
		wrapThinBorderBottomStyle.setBorderBottom(BorderStyle.THIN);
		wrapThinBorderBottomStyle.setWrapText(true);

		XSSFCellStyle showableDataStyle = workbook.createCellStyle();
		showableDataStyle.setWrapText(true);
		showableDataStyle.setBorderBottom(BorderStyle.THIN);
		showableDataStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());

		int rowNum = 0;

		// Create Overview - START
		Row overview = sheetOverview.createRow(rowNum++);

		Cell cell = overview.createCell(0);
		cell.setCellStyle(boldFontLargeStyle);
		cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
		cell.setCellValue(reportTitle);

		Set<String> serviceKeySet = serviceMap.keySet();
		Iterator<String> serviceKeyIterator = serviceKeySet.iterator();

		Row serviceNameRow = sheetOverview.createRow(rowNum++);
		Cell serviceNameTitleCell = serviceNameRow.createCell(0);
		serviceNameRow.setRowStyle(serviceNameTitleCell.getCellStyle());

		while (serviceKeyIterator.hasNext()) {

			String suName = serviceKeyIterator.next();
			List<_TransactionSourceModel> transList = serviceMap.get(suName);

			// Create headers
			rowNum = this.createOverviewHeaders(sheetOverview, rowNum, transList.get(0));

			String secondHeader = getSecondTableHeader(transList.get(0))[0];
			Row trackingVarsValueRow = sheetOverview.createRow(rowNum++);
			int colNum = 0;
			Cell trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
			trackingVarsValueRow.setRowStyle(trackingVarsValueCell.getCellStyle());

			// LOGGER.log(Level.INFO, "Creating table data");
			// transList = utils.combineData(transList);
			for (_TransactionSourceModel transaction : transList) {
				String[] primaryTrackingDisplay = transaction.getPrimaryTrackingId().split("\\~\\#\\~");

				String tblVal1 = primaryTrackingDisplay[0] + " : " + primaryTrackingDisplay[1];
				String tblVal2 = this.getSecondTableValue(transaction, secondHeader);
				String tblVal3 = transaction.getEventTime();

				trackingVarsValueCell.setCellValue(suName);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellValue(tblVal1);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellValue(tblVal2);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellValue(tblVal3);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellType(CellType.STRING);
				trackingVarsValueCell.setCellValue(transaction.getProcessingTime() / 1000);

				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
				trackingVarsValueCell.setCellType(CellType.STRING);
				trackingVarsValueCell.setCellValue(transaction.getStatus());

				// Create Separator
				// trackingVarsValueRow = sheet.createRow(rowNum++);
				// trackingVarsValueCell = trackingVarsValueRow.createCell(0, CellType.BLANK);

				// Reinitialize for next iteration
				trackingVarsValueRow = sheetOverview.createRow(rowNum++);
				colNum = 0;
				trackingVarsValueCell = trackingVarsValueRow.createCell(colNum++);
			}

			serviceNameRow = sheetOverview.createRow(rowNum++);
			serviceNameTitleCell = serviceNameRow.createCell(0);
			serviceNameRow.setRowStyle(serviceNameTitleCell.getCellStyle());
		}
		// Create Overview - END
		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new File(fileName);
	}
}
