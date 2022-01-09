/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logicoy.bpelmon.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Report generator util class for inspirage BPEL monitoring
 *
 * @author Param
 */
@Component
public class InspirageReportGenerator {

	static Font blueFont = FontFactory.getFont(FontFactory.COURIER, 9, Font.NORMAL, new CMYKColor(255, 0, 0, 0));
	static Font blackBold = FontFactory.getFont(FontFactory.COURIER, 9, Font.BOLD, new CMYKColor(0, 0, 0, 255));
	static Font blackBoldNormal = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.BOLD,
			new CMYKColor(0, 0, 0, 255));
	static Font blackBoldLarge = FontFactory.getFont(FontFactory.TIMES_ROMAN, 16, Font.ITALIC,
			new CMYKColor(0, 0, 0, 255));
	static Font blackBold_H = FontFactory.getFont(FontFactory.COURIER, 9, Font.BOLD, new CMYKColor(0, 0, 0, 255));

	static Font blackNormal = FontFactory.getFont(FontFactory.COURIER, 9, Font.NORMAL, new CMYKColor(0, 0, 0, 255));
	static Font blackBold_table = FontFactory.getFont(FontFactory.COURIER, 9, Font.NORMAL, new CMYKColor(0, 0, 0, 255));
	static Font blackNormal_table = FontFactory.getFont(FontFactory.COURIER, 9, Font.NORMAL,
			new CMYKColor(10, 10, 10, 255));
	static Font yellowFont = FontFactory.getFont(FontFactory.COURIER_OBLIQUE, 14, Font.NORMAL,
			new CMYKColor(0, 0, 255, 0));
	private Logger LOGGER = Logger.getLogger(this.getClass().getName());
	private static ExecutorService fileDeleteExecutor = Executors.newFixedThreadPool(5);

	/**
	 * Test Method
	 *
	 * @param args
	 * @throws BadElementException
	 * @throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		List<String> Header = new ArrayList<>();
		Map<String, List<String>> serviceHeaders = new HashMap<>();
		// Key Header
		Header.add("Sales Order Number");

		// List Header
		Header.add("TransactionId");
		Header.add("ItemName");
		Header.add("ItemValue");
		Header.add("Custom1");
		Header.add("Date/Time");

		serviceHeaders.put("ServiceCA1", Header);

		List<String> HeaderCa2 = new ArrayList<>();

		HeaderCa2.add("Sales Order Number");
		HeaderCa2.add("ItemName");
		HeaderCa2.add("Date/Time");
		HeaderCa2.add("Custom");

		serviceHeaders.put("ServiceCA2", HeaderCa2);

		Map<String, List<List<String>>> mapp = new HashMap<>();

		Map<String, Map<String, List<List<String>>>> serviceData = new HashMap<>();

		List<List<String>> data = new ArrayList<>();

		List<String> m1 = new ArrayList<>();
		m1.add("123");
		m1.add("XYC");
		m1.add("123232");
		m1.add("455");
		m1.add("2018-9-19 01:10:10");

		List<String> m2 = new ArrayList<>();
		m2.add("1235");
		m2.add("XYCFAS");
		m2.add("43412");
		m2.add("&%&$^%");
		m2.add("2018-9-18 01:10:10");

		data.add(m1);
		data.add(m2);

		mapp.put("123141431", data);

		serviceData.put("ServiceCA1", mapp);

		Map<String, List<List<String>>> mapp2 = new HashMap<>();

		List<List<String>> data2 = new ArrayList<>();

		List<String> m11 = new ArrayList<>();
		m11.add("XYC");
		m11.add("123232");
		m11.add("2018-9-19 01:10:10");

		List<String> m22 = new ArrayList<>();
		m22.add("XYCFAS");
		m22.add("43412");
		m22.add("2018-9-18 01:10:10");

		data2.add(m11);
		data2.add(m22);

		mapp2.put("123141431", data2);

		serviceData.put("ServiceCA2", mapp2);

		new InspirageReportGenerator().generateReport("Report1.pdf",
				"Transaction Report for period 2018-09-10 to 2018-10-19", new Date().toString(), serviceHeaders,
				serviceData, "");

	}

	public Future<Boolean> generateBgReportOld(String reportFileName, String reportTitle, String generatedDate,
			Map<String, List<String>> tableHeaderList, Map<String, Map<String, List<List<String>>>> reportTableDataList,
			String clientId) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			return (Future<Boolean>) executor.submit(() -> {
				return generateReport(reportFileName, reportTitle, generatedDate, tableHeaderList, reportTableDataList,
						clientId);
			});
		} finally {
			executor.shutdown();
		}
	}

	public Future<Boolean> generateBgReport(String reportFileName, String reportTitle, String generatedDate,
			Map<String, List<String>> pdfData, String clientId) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			return (Future<Boolean>) executor.submit(() -> {
				return generatePDF(reportFileName, reportTitle, generatedDate, pdfData, clientId);
			});
		} finally {
			executor.shutdown();
		}
	}

	/**
	 * Generate report for given parameters
	 *
	 * @param reportFileName
	 * @param reportTitle
	 * @param generatedDate
	 * @param tableHeaderList
	 * @param reportTableDataList
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	public boolean generateReport(String reportFileName, String reportTitle, String generatedDate,
			Map<String, List<String>> tableHeaderList, Map<String, Map<String, List<List<String>>>> reportTableDataList,
			String clientId) throws Exception {

		// Document document = new Document();
		try {

			// Generate multiple pdf per service
			int i = 0;
			String[] subPdfName = new String[reportTableDataList.keySet().size()];
			List<InputStream> list = new ArrayList<InputStream>();

			for (String serviceName : reportTableDataList.keySet()) {

				String subReportFileName = serviceName + UUID.randomUUID() + ".pdf";
				LOGGER.info("Creating pdf " + subReportFileName + " for service : " + serviceName);
				subPdfName[i++] = subReportFileName;
				this.writeNewPDF(subReportFileName, new Document(), serviceName, tableHeaderList, reportTableDataList,
						clientId);
				list.add(new FileInputStream(subReportFileName));
			}

			// Merge PDF

			this.mergePdf(reportTitle, generatedDate, list, new FileOutputStream(reportFileName), clientId, subPdfName);

			// Paragraph gDatePara = new Paragraph("\nGenerated Date : " + generatedDate,
			// blackNormal);
			// document.add(gDatePara);

			// Paragraph p4 = new Paragraph("\n\n\n© 2018 Inspirage. All rights reserved" +
			// "\n"
			// + "© Inspirage is a registered trademark of Inspirage, LLC." + "\n"
			// + "600 108th Avenue NE, Suite 540 | Bellevue, WA 98004", blackNormal);
			//
			// document.add(p4);
			// document.close();
			// writer.close();

		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
		}
		return true;

	}

	private void mergePdf(String reportTitle, String generatedDate, List<InputStream> list, OutputStream outputStream,
			String clientId, String[] subPdfName) throws DocumentException, IOException {
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		document.open();

		Rectangle two = new Rectangle(900, 400);
		document.setPageSize(two);
		document.setMargins(10, 10, 10, 10);
		document.open();
		// Adding Inspirage image
		/*
		 * String imageUrl =
		 * "http://d3r4a0fzkb48db.cloudfront.net/wp-content/themes/Inspirage/assets/images/Inspirage_logo.png";
		 * Image inspirageImage = Image.getInstance(new URL(imageUrl));
		 * inspirageImage.scaleToFit(200, 100); inspirageImage.setIndentationLeft(-20f);
		 * document.add(inspirageImage);
		 */
		Paragraph topPara = new Paragraph();

		// PdfPTable table = new PdfPTable(2); // 3 columns.
		// table.setWidthPercentage(100); // Width 100%
		// table.setSpacingBefore(0f); // Space before table
		// table.setSpacingAfter(0f); // Space after table

		String imageUrl = "http://d3r4a0fzkb48db.cloudfront.net/wp-content/themes/Inspirage/assets/images/Inspirage_logo.png";
		Image inspirageImage = Image.getInstance(new URL(imageUrl));
		inspirageImage.scaleToFit(450, 900);
		topPara.add(inspirageImage);
		document.add(topPara);
		// inspirageImage.setIndentationLeft(-50f);
		// inspirageImage.set(-20f);
		topPara = new Paragraph((reportTitle.split("from")[0] + "for " + clientId).toUpperCase(), blackBoldLarge);
		document.add(topPara);
		topPara = new Paragraph("Period: " + reportTitle.split("period")[1].trim(), blackBoldLarge);
		document.add(topPara);
		topPara = new Paragraph("Report Generation Date: " + generatedDate, blackBoldLarge);
		document.add(topPara);
		topPara = new Paragraph(
				"\nWelcome to iHubCS Reporting. \nThe report below groups transaction by their respective service.",
				blackBoldLarge);
		document.add(topPara);
		// PdfPCell cell1 = new PdfPCell(inspirageImage);
		// cell1.setBorderColor(BaseColor.GRAY);
		// cell1.setPaddingLeft(0);
		// cell1.setBorderWidth(0.0f);
		// cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
		// cell1.setVerticalAlignment(Element.ALIGN_LEFT);
		// cell1.setFollowingIndent(0);
		// table.addCell(cell1);
		// inspirageImage = null;
		// cell1 = new PdfPCell(new Paragraph("\n" + reportTitle, blackBoldLarge));
		// cell1.setBorderColor(BaseColor.GRAY);
		// cell1.setPaddingLeft(0);
		// cell1.setBorderWidth(0.0f);
		// cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
		// cell1.setVerticalAlignment(Element.ALIGN_CENTER);
		// cell1.setFollowingIndent(0);
		// table.addCell(cell1);
		//
		// cell1 = new PdfPCell(new Paragraph("\n" + "Report Date: " + generatedDate,
		// blackBold));
		// cell1.setBorderColor(BaseColor.GRAY);
		// cell1.setPaddingLeft(0);
		// cell1.setBorderWidth(0.0f);
		// cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
		// cell1.setVerticalAlignment(Element.ALIGN_RIGHT);
		// cell1.setFollowingIndent(0);
		// table.addCell(cell1);
		//
		// document.add(table);

		// Paragraph p1 = new Paragraph("\nWelcome to iHubCS Reporting. The report below
		// groups transactions by their respective service name.\n", blackNormal);
		// document.add(p1);

		document.addAuthor("Monitoring team");
		document.addCreationDate();
		document.addCreator("www.inspirage.com");
		document.addTitle("BPEL Monitoring Report : " + reportTitle);
		document.addSubject("BPEL Monitoring Report : " + reportTitle);

		// Paragraph gDatePara2 = new Paragraph("\nDetails is as
		// below.\n========================================================================",
		// blackNormal);
		// document.add(gDatePara2);
		Paragraph newLine = new Paragraph("\n");
		document.add(newLine);
		two = new Rectangle(900, 1200);
		document.setPageSize(two);
		document.setMargins(10, 10, 10, 10);

		PdfContentByte cb = writer.getDirectContent();

		PdfImportedPage page = null;
		PdfReader reader = null;
		for (InputStream in : list) {
			reader = new PdfReader(in);
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				document.newPage();
				// import the page from source pdf
				page = writer.getImportedPage(reader, i);
				// add the page to the destination pdf
				cb.addTemplate(page, 0, 0);
			}
			
			in.close();
		}
		document.newPage();
		Paragraph p4 = new Paragraph("\n\n\n© 2018 Inspirage. All rights reserved" + "\n"
				+ "© Inspirage is a registered trademark of Inspirage, LLC." + "\n"
				+ "600 108th Avenue NE, Suite 540 | Bellevue, WA 98004", blackNormal);

		document.add(p4);
		outputStream.flush();
		document.close();
		outputStream.close();
		
		if(fileDeleteExecutor != null) {
			fileDeleteExecutor.execute(new DeleteSubReports(subPdfName));
		}
	}

	private void writeNewPDF(String reportFileName, Document document, String serviceName,
			Map<String, List<String>> tableHeaderList, Map<String, Map<String, List<List<String>>>> reportTableDataList,
			String clientId) throws Exception {
//		LOGGER.info("Data size: " + reportTableDataList.size());
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(reportFileName));
		Rectangle two = new Rectangle(900, 1200);
		document.setPageSize(two);
		document.setMargins(10, 10, 10, 10);
		document.open();
		String displayName = SUNameReader.getValue(clientId + "." + serviceName);
		displayName = displayName != null && !displayName.isEmpty() ? displayName : serviceName;
		document.add(new Paragraph("\nService Name : " + displayName + "\n\n", blackBoldNormal));
		// Map<String, List<List<String>>> serviceTableDataList =
		// reportTableDataList.get(serviceName);
		// List<String> headerDataList = tableHeaderList.get(serviceName);
		addTableDataNew(tableHeaderList.get(serviceName), reportTableDataList.get(serviceName), document);
		document.close();
		writer.close();
	}

	/**
	 * Generate report for given parameters
	 *
	 * @param reportFileName
	 * @param reportTitle
	 * @param generatedDate
	 * @param tableHeaderList
	 * @param reportTableDataList
	 * @return
	 * @throws Exception
	 */
	public boolean generateReport(String reportFileName, String reportTitle, String generatedDate,
			List<String> tableHeaderList, Map<String, List<List<String>>> reportTableDataList) throws Exception {

		Document document = new Document();
		try {

			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(reportFileName));
			document.open();

			// Adding Inspirage image
			String imageUrl = "http://d3r4a0fzkb48db.cloudfront.net/wp-content/themes/Inspirage/assets/images/Inspirage_logo.png";
			Image inspirageImage = Image.getInstance(new URL(imageUrl));
			inspirageImage.scaleToFit(200, 100);
			inspirageImage.setIndentationLeft(-20f);
			document.add(inspirageImage);

			Paragraph p1 = new Paragraph(reportTitle, blackBold);
			document.add(p1);

			document.addAuthor("Monitoring team");
			document.addCreationDate();
			document.addCreator("www.inspirage.com");
			document.addTitle("BPEL Monitoring Report : " + reportTitle);
			document.addSubject("BPEL Monitoring Report : " + reportTitle);

			// Paragraph gDatePara2 = new Paragraph("\nDetails is as
			// below.\n========================================================================",
			// blackNormal);
			// document.add(gDatePara2);
			Paragraph newLine = new Paragraph("\n");
			document.add(newLine);

			addTableData(tableHeaderList, reportTableDataList, document);

			Paragraph gDatePara = new Paragraph("\nGenerated Date : " + generatedDate, blackNormal);
			document.add(gDatePara);

			Paragraph p4 = new Paragraph("\n\n\n© 2018 Inspirage. All rights reserved" + "\n"
					+ "© Inspirage is a registered trademark of Inspirage, LLC." + "\n"
					+ "600 108th Avenue NE, Suite 540 | Bellevue, WA 98004", blackNormal);

			document.add(p4);
			document.close();
			writer.close();
			document.close();

		} catch (Exception e) {
			LOGGER.severe(e.getMessage() + " Caused by: " + e.getCause());
		}
		return true;

	}

	/**
	 * Add table data in PDF
	 *
	 * @param headerList
	 * @param data
	 * @param document
	 * @throws Exception
	 */
	private void addTableData(List<String> headerList, Map<String, List<List<String>>> data, Document document)
			throws Exception {

		int noOfColumns = 0;

		for (String key : data.keySet()) {
			List<List<String>> dataList = data.get(key);
			for (List<String> mapp : dataList) {
				if (noOfColumns < mapp.size()) {
					noOfColumns = mapp.size();
				}
			}
		}

		noOfColumns = noOfColumns + 1; // One for Key

		PdfPTable table = new PdfPTable(noOfColumns); // 3 columns.
		table.setWidthPercentage(100); // Width 100%
		table.setSpacingBefore(0f); // Space before table
		table.setSpacingAfter(0f); // Space after table

		// Set Column widths
		float[] columnWidths = new float[noOfColumns];

		for (int i = 0; i < noOfColumns; i++) {
			columnWidths[i] = 1.f;
		}

		table.setWidths(columnWidths);
		PdfPCell cell1 = null;
		PdfPCell cell2 = null;
		for (int i = 0; i < noOfColumns; i++) {
			String lbl = "";
			if (i >= headerList.size()) {
				lbl = "NOT-DEFINED";
			} else {
				lbl = headerList.get(i);
			}
			cell1 = new PdfPCell(new Paragraph(lbl, blackBold));
			cell1.setBorderColorTop(BaseColor.WHITE);
			cell1.setBorderColorLeft(BaseColor.WHITE);
			cell1.setBorderColorRight(BaseColor.WHITE);
			cell1.setBorderColorBottom(BaseColor.BLACK);
			// cell1.setPaddingLeft(10);
			cell1.setBorderWidthBottom(0.5f);
			// cell1.setPaddingLeft(10);
			cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell1.setVerticalAlignment(Element.ALIGN_LEFT);
			// cell1.setFollowingIndent(10);
			table.addCell(cell1);
		}

		for (String key : data.keySet()) {

			// List<List<String>> dataList = data.get(key);
			for (List<String> mapp : data.get(key)) {

				cell1 = new PdfPCell(new Paragraph(key, blackBold_table));
				cell1.setBorderWidth(0.0f);
				// cell1.setBorderColor(BaseColor.WHITE);
				// cell1.setPaddingLeft(10);
				cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell1.setVerticalAlignment(Element.ALIGN_LEFT);
				// cell1.setFollowingIndent(10);
				table.addCell(cell1);

				for (String value : mapp) {
					cell2 = new PdfPCell(new Paragraph(value, blackNormal_table));
					// cell2.setBorderColor(BaseColor.BLACK);
					cell2.setBorderWidth(0.0f);
					// cell2.setPaddingLeft(10);
					cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell2.setVerticalAlignment(Element.ALIGN_LEFT);
					// cell2.setFollowingIndent(10);
					table.addCell(cell2);
				}
			}
		}
		headerList = null;
		data = null;
		document.add(table);
	}

	private void addTableDataNew(List<String> headerList, Map<String, List<List<String>>> data, Document document)
			throws Exception {

		int noOfColumns = headerList.size();

		// for (String key : data.keySet()) {
		// List<List<String>> dataList = data.get(key);
		// for (List<String> mapp : dataList) {
		// if (noOfColumns < mapp.size()) {
		// noOfColumns = mapp.size();
		// }
		// }
		// }

		// noOfColumns = noOfColumns + 1; // One for Key

		PdfPTable table = new PdfPTable(noOfColumns);
		table.setWidthPercentage(100); // Width 100%
		table.setSpacingBefore(0f); // Space before table
		table.setSpacingAfter(0f); // Space after table

		// Set Column widths
		float[] columnWidths = new float[noOfColumns];

		for (int i = 0; i < noOfColumns; i++) {
			columnWidths[i] = 1.f;
		}

		table.setWidths(columnWidths);
		PdfPCell cell1 = null;
		PdfPCell cell2 = null;
		for (int i = 0; i < noOfColumns; i++) {
			String lbl = "";
			if (i >= headerList.size()) {
				lbl = "NOT-DEFINED";
			} else {
				lbl = headerList.get(i);
			}
			cell1 = new PdfPCell(new Paragraph(lbl, blackBold));
			cell1.setBorderColorTop(BaseColor.WHITE);
			cell1.setBorderColorLeft(BaseColor.WHITE);
			cell1.setBorderColorRight(BaseColor.WHITE);
			cell1.setBorderColorBottom(BaseColor.BLACK);
			// cell1.setPaddingLeft(10);
			cell1.setBorderWidthBottom(0.5f);
			// cell1.setPaddingLeft(10);
			cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell1.setVerticalAlignment(Element.ALIGN_LEFT);
			// cell1.setFollowingIndent(10);
			table.addCell(cell1);
		}

		for (String key : data.keySet()) {

			// List<List<String>> dataList = data.get(key);
			for (List<String> mapp : data.get(key)) {

				cell1 = new PdfPCell(new Paragraph(key, blackBold_table));
				cell1.setBorderWidth(0.0f);
				// cell1.setBorderColor(BaseColor.WHITE);
				// cell1.setPaddingLeft(10);
				cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell1.setVerticalAlignment(Element.ALIGN_LEFT);
				// cell1.setFollowingIndent(10);
				table.addCell(cell1);

				for (String value : mapp) {
					cell2 = new PdfPCell(new Paragraph(value, blackNormal_table));
					// cell2.setBorderColor(BaseColor.BLACK);
					cell2.setBorderWidth(0.0f);
					// cell2.setPaddingLeft(10);
					cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
					cell2.setVerticalAlignment(Element.ALIGN_LEFT);
					// cell2.setFollowingIndent(10);
					table.addCell(cell2);
				}
			}
		}
		headerList = null;
		data = null;
		document.add(table);
	}

	private boolean generatePDF(String reportFileName, String reportTitle, String generatedDate,
			Map<String, List<String>> pdfData, String clientId) {
		try {

			// Generate multiple pdf per service
			String[] subPdfName = new String[pdfData.keySet().size()];
			List<InputStream> list = new ArrayList<InputStream>();
			Set<String> keySet = pdfData.keySet();
			String serviceName = "";
			int subReportCounter = 0;
			while(keySet.size() > 0) {
				serviceName = keySet.iterator().next();
				String subReportFileName = serviceName + UUID.randomUUID() + ".pdf";
				LOGGER.info("Creating pdf " + subReportFileName + " for service : " + serviceName);
				subPdfName[subReportCounter++] = subReportFileName;
				// this.writeNewPDF(subReportFileName, new Document(), serviceName,
				// tableHeaderList, reportTableDataList, clientId);
				this.createServiceWisePDF(subReportFileName, new Document(), serviceName, pdfData.get(serviceName),
						clientId);
				list.add(new FileInputStream(subReportFileName));
				keySet.remove(serviceName);
			}

			// Merge PDF

			this.mergePdf(reportTitle, generatedDate, list, new FileOutputStream(reportFileName), clientId, subPdfName);

			// Paragraph gDatePara = new Paragraph("\nGenerated Date : " + generatedDate,
			// blackNormal);
			// document.add(gDatePara);

			// Paragraph p4 = new Paragraph("\n\n\n© 2018 Inspirage. All rights reserved" +
			// "\n"
			// + "© Inspirage is a registered trademark of Inspirage, LLC." + "\n"
			// + "600 108th Avenue NE, Suite 540 | Bellevue, WA 98004", blackNormal);
			//
			// document.add(p4);
			// document.close();
			// writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private void createServiceWisePDF(String reportFileName, Document document, String serviceName,
			List<String> dataList, String clientId) throws Exception {
		LOGGER.info("Data size: " + dataList.size());
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(reportFileName));
		Rectangle two = new Rectangle(900, 1200);
		document.setPageSize(two);
		document.setMargins(10, 10, 10, 10);
		document.open();
		String displayName = SUNameReader.getValue(clientId + "." + serviceName);
		displayName = displayName != null && !displayName.isEmpty() ? displayName : serviceName;
		document.add(new Paragraph("\nService Name : " + displayName + "\n\n", blackBoldNormal));

		PdfPTable table = null;
		PdfPCell cell1 = null;
		PdfPCell cell2 = null;
		String[] showableData;
		int noOfColumns = 0;
		float[] columnWidths = null;
		for (int i = 0; i < dataList.size(); i++) {
			// Split into key value pair
			showableData = dataList.get(i).split("\\<\\|\\|\\>");

			noOfColumns = showableData.length;
			table = new PdfPTable(noOfColumns);
			table.setWidthPercentage(100); // Width 100%
			table.setSpacingBefore(5.0f); // Space before table
			table.setSpacingAfter(5.0f); // Space after table

			// Set Column widths
			columnWidths = new float[noOfColumns];

			for (int j = 0; j < noOfColumns; j++) {
				columnWidths[j] = 1.0f;
			}

			table.setWidths(columnWidths);

			// ADD Table headers

			for (int k = 0; k < noOfColumns; k++) {

				cell1 = new PdfPCell(new Paragraph(showableData[k].split("\\~\\#\\~")[0], blackBold));
				cell1.setBorderColorTop(BaseColor.WHITE);
				cell1.setBorderColorLeft(BaseColor.WHITE);
				cell1.setBorderColorRight(BaseColor.WHITE);
				cell1.setBorderColorBottom(BaseColor.BLACK);
				if (k == 0) {
					cell1.setBorderColorLeft(BaseColor.BLACK);
				}
				if (k == noOfColumns - 1) {
					cell1.setBorderColorRight(BaseColor.BLACK);
				}
				// cell1.setPaddingLeft(10);
				cell1.setBorderWidthBottom(0.2f);
				// cell1.setPaddingLeft(10);
				cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell1.setVerticalAlignment(Element.ALIGN_LEFT);
				// cell1.setFollowingIndent(10);
				table.addCell(cell1);
			}

			// Add Table Data

			for (int l = 0; l < showableData.length; l++) {
				String showableValue = showableData[l].split("\\~\\#\\~").length > 1 ? showableData[l].split("\\~\\#\\~")[1] : "NA"; 
				cell2 = new PdfPCell(new Paragraph(showableValue, blackNormal_table));
				cell2.setBorderColorTop(BaseColor.WHITE);
				cell2.setBorderColorLeft(BaseColor.WHITE);
				cell2.setBorderColorRight(BaseColor.WHITE);
				cell2.setBorderColorBottom(BaseColor.WHITE);
				cell2.setBorderWidth(0.2f);
				// cell2.setPaddingLeft(10);
				cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell2.setVerticalAlignment(Element.ALIGN_LEFT);
				if (l == 0) {
					cell2.setBorderColorLeft(BaseColor.BLACK);
				}
				if (l == showableData.length - 1) {
					cell2.setBorderColorRight(BaseColor.BLACK);
				}
				// cell2.setFollowingIndent(10);
				table.addCell(cell2);
			}

			document.add(table);
			dataList.remove(i);
			showableData = null;
		}
		document.close();
		writer.close();
		dataList = null;
//		File f = new File(reportFileName);
//		if (f != null && f.exists()) {
//			f.delete();
//		}
	}

	private class DeleteSubReports implements Runnable{
		private String[] subReportFileNames; 
		
		public DeleteSubReports(String[] subReportFileNames) {
			this.subReportFileNames = subReportFileNames;
		}

		@Override
		public void run() {
			File fileToBeDeleted = null;
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Deleting sub reports : {0}", subReportFileNames.length);
			for(String fileName: subReportFileNames) {
				fileToBeDeleted = new File(fileName);
				if(fileToBeDeleted != null && fileToBeDeleted.exists()) {
					fileToBeDeleted.delete();
				}
			}
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "All files deleted");
		}
	}
}
