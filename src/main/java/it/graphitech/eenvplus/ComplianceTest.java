package it.graphitech.eenvplus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class ComplianceTest {

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	static boolean isDocumentValid = false;

	public static void main(String[] args) {

		String input = null;
		String schemaSource = null;
		boolean result = false;
		// System.out.println(args.length);

		if (args.length > 0) {

			if (args[0].equals("-s")) {
				schemaSource = new String(args[1]);
				if (args[2].equals("-f")) {
					input = new String(args[3]);
					result = makeFileRequest(input, schemaSource);
				} else if (args[2].equals("-u")) {
					input = new String(args[3]);
					result = makeURLRequest(input, schemaSource);
				} else {
					printUsage();
				}
			} else if (args[0].equals("-f")) {
				input = new String(args[1]);
				result = makeFileRequest(input, null);
			} else if (args[0].equals("-u")) {
				input = new String(args[1]);
				result = makeURLRequest(input, null);
			} else {
				printUsage();
			}

			SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

			if (result) {
				System.out.print("[" + ft.format(System.currentTimeMillis()) + "] Test passed!\n");
			} else {
				System.out.print("[" + ft.format(System.currentTimeMillis()) + "] Test failed!\n");
			}

		} else {
			printUsage();
			// input = new String("http://eenvplus.sinergis.it/deegree/services/wfs?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetFeature&LANGUAGE=ENG&TYPENAMES=au:AdministrativeUnit&count=50");
			// input = new String("/Users/a.giori/Documents/Progetti/eENVplus/GML Pilots Gen2015/EP08/EP08_AU.gml");
			// schemaSource = new String("/Users/a.giori/Documents/Progetti/eENVplus/XSD INSPIRE/xsd_06-10-2014/AdministrativeUnits.xsd");

			// input = "/Users/a.giori/Documents/eENVplus/QoS - Tests/wfs/Deegree-wfs-getCapabilities_with-ExtendedCapabilities.xml";
			// schemaSource = "/Users/a.giori/Documents/eENVplus/XSD INSPIRE/xsd_06-10-2014/inspire_ds.xsd";
			// input = "/Users/a.giori/Documents/eENVplus/QoS - Tests/wfs/Deegree-wfs-getFeature.xml";
			// schemaSource = "/Users/a.giori/Documents/eENVplus/XSD INSPIRE/xsd_06-10-2014/AdministrativeUnits.xsd";

			// result = makeFileRequest(input, schemaSource);
			// SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
			// if (result) {
			// System.out.print("[" + ft.format(System.currentTimeMillis()) + "] Test passed!\n");
			// } else {
			// System.out.print("[" + ft.format(System.currentTimeMillis()) + "] Test failed!\n");
			// }
		}

	}

	static void printUsage() {
		System.out.println("USAGE: java -jar ComplianceTest.jar [-s \"schema.xsd\"] -f \"file_path\" or -u \"URL\"");
	}

	// --- validate from file --- //
	static boolean makeFileRequest(String file, String schemaSource) {

		isDocumentValid = true;

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);

			SAXParser parser = factory.newSAXParser();
			parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

			if (schemaSource != null) {
				parser.setProperty(JAXP_SCHEMA_SOURCE, new File(schemaSource));
			}

			XMLReader reader = parser.getXMLReader();
			reader.setErrorHandler(new SimpleErrorHandler());
			reader.parse(new InputSource(file));

		} catch (IOException e) {
			System.out.print("[error] file not found!\n");
			isDocumentValid = false;
		} catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			System.out.print("[error] document is invalid!\n");
			isDocumentValid = false;
		}

		return isDocumentValid;
	}

	// --- validate from URL --- //
	static boolean makeURLRequest(String url, String schemaSource) {

		HttpGet httpGet = new HttpGet(url);
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				try {
					HttpEntity httpEntity = httpResponse.getEntity();
					if (httpEntity != null) {

						byte[] inputHttpBytes = EntityUtils.toByteArray(httpEntity);

						FileOutputStream out = new FileOutputStream("response.xml");
						out.write(inputHttpBytes);
						out.close();
						isDocumentValid = true;

						try {
							SAXParserFactory factory = SAXParserFactory.newInstance();
							factory.setValidating(true);
							factory.setNamespaceAware(true);

							SAXParser parser = factory.newSAXParser();
							if (schemaSource != null) {
								parser.setProperty(JAXP_SCHEMA_SOURCE, new File(schemaSource));
							} else {
								parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
							}
							XMLReader reader = parser.getXMLReader();
							reader.setErrorHandler(new SimpleErrorHandler());
							reader.parse(new InputSource("response.xml"));

						} catch (SAXException | ParserConfigurationException e) {
							System.out.print("[error] document is invalid!\n");
							isDocumentValid = false;
						}

					} else {
						System.out.print("[error] httpEntity == null\n");
						isDocumentValid = false;
					}

					EntityUtils.consume(httpEntity);
				} finally {
					httpResponse.close();
				}

			} else {
				System.out.print("[error] Download failed, status code: " + httpResponse.getStatusLine().getStatusCode() + "\n");
				isDocumentValid = false;
			}
		} catch (IOException e) {
			System.out.print("[error] IOException\n");
			isDocumentValid = false;
		}

		return isDocumentValid;
	}

	static class SimpleErrorHandler implements ErrorHandler {
		public void warning(SAXParseException e) throws SAXException {
			isDocumentValid = false;
			System.out.println(e.getMessage());
		}

		public void error(SAXParseException e) throws SAXException {
			isDocumentValid = false;
			System.out.println(e.getMessage());
		}

		public void fatalError(SAXParseException e) throws SAXException {
			isDocumentValid = false;
			System.out.println(e.getMessage());
		}
	}
}
