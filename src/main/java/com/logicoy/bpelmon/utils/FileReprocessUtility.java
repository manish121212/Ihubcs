package com.logicoy.bpelmon.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@Component
public class FileReprocessUtility {
	/**
     * Method to transform the xmlData with XSL
     * @param xmlData
     * @param xsltLocation
     * @return
     * @throws Exception 
     */
    public String getTransformedData(String xmlData, String xsltLocation) throws Exception {

        if (xsltLocation == null || xsltLocation.trim().length() == 0) {
            return null;
        }
        
        File stylesheet = new File(xsltLocation);
        //File xmlSource = new File("src/main/resources/data.xml");

        Document document = convertStringToDocument(xmlData);

        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(stylesource);
        Source source = new DOMSource(document);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(bo);
        transformer.transform(source, outputTarget);

        return String.valueOf(bo);
    }

    private Document convertStringToDocument(String xmlStr) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            return doc;
        } catch (Exception e) {
            throw e;
        }
    }

    //Test methid
    public static void main(String args[]) throws Exception {
        File stylesheet = new File("style.xsl");
        //File xmlSource = new File("src/main/resources/data.xml");
        String xmlData = "<?xml version=\"1.0\"?>\n"
                + "<Sites>\n"
                + "<Site id=\"101\" name=\"NY-01\" location=\"New York\">\n"
                + "    <Hosts>\n"
                + "        <Host id=\"1001\">\n"
                + "           <Host_Name>srv001001</Host_Name>\n"
                + "           <IP_address>10.1.2.3</IP_address>\n"
                + "           <OS>Windows</OS>\n"
                + "           <Load_avg_1min>1.3</Load_avg_1min>\n"
                + "           <Load_avg_5min>2.5</Load_avg_5min>\n"
                + "           <Load_avg_15min>1.2</Load_avg_15min>\n"
                + "        </Host>\n"
                + "        <Host id=\"1002\">\n"
                + "           <Host_Name>srv001002</Host_Name>\n"
                + "           <IP_address>10.1.2.4</IP_address>\n"
                + "           <OS>Linux</OS>\n"
                + "           <Load_avg_1min>1.4</Load_avg_1min>\n"
                + "           <Load_avg_5min>2.5</Load_avg_5min>\n"
                + "           <Load_avg_15min>1.2</Load_avg_15min>\n"
                + "        </Host>\n"
                + "        <Host id=\"1003\">\n"
                + "           <Host_Name>srv001003</Host_Name>\n"
                + "           <IP_address>10.1.2.5</IP_address>\n"
                + "           <OS>Linux</OS>\n"
                + "           <Load_avg_1min>3.3</Load_avg_1min>\n"
                + "           <Load_avg_5min>1.6</Load_avg_5min>\n"
                + "           <Load_avg_15min>1.8</Load_avg_15min>\n"
                + "        </Host>\n"
                + "        <Host id=\"1004\">\n"
                + "           <Host_Name>srv001004</Host_Name>\n"
                + "           <IP_address>10.1.2.6</IP_address>\n"
                + "           <OS>Linux</OS>\n"
                + "           <Load_avg_1min>2.3</Load_avg_1min>\n"
                + "           <Load_avg_5min>4.5</Load_avg_5min>\n"
                + "           <Load_avg_15min>4.2</Load_avg_15min>\n"
                + "        </Host>     \n"
                + "    </Hosts>\n"
                + "</Site>\n"
                + "</Sites>";

        Document document = new FileReprocessUtility().convertStringToDocument(xmlData);

        StreamSource stylesource = new StreamSource(stylesheet);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(stylesource);
        Source source = new DOMSource(document);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(bo);
        transformer.transform(source, outputTarget);

        System.out.println("--->\n\n" + String.valueOf(bo));
    }
}
