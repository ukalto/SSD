package ssd;

import java.io.File;
import java.io.FileReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public class SSD {
    private static DocumentBuilderFactory documentBuilderFactory;
    private static DocumentBuilder documentBuilder;

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
            System.err.println("Usage: java SSD <input.xml> <freight.xml> <output.xml>");
            System.exit(1);
        }

		String inputPath = args[0];
		String freightPath = args[1];
		String outputPath = args[2];

    
       initialize();
       transform(inputPath, freightPath, outputPath);
    }
	
	private static void initialize() throws Exception {    
       documentBuilderFactory = DocumentBuilderFactory.newInstance();
       documentBuilderFactory.setNamespaceAware(true);
       documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
	
	/**
     * Use this method to encapsulate the main logic for this example. 
     * 
     * First read in the shipment document. 
     * Second create a ShipmentHandler and an XMLReader (SAX parser)
     * Third parse the freight document
     * Last get the Document from the ShipmentHandler and use a
     *    Transformer to print the document to the output path.
     * 
     * @param inputPath Path to the xml file to get read in.
     * @param freightPath Path to the freight xml file
     * @param outputPath Path to the output xml file.
     */
    private static void transform(String inputPath, String freightPath, String outputPath) throws Exception {
        // Read in the data from the shipment xml document, created in exercise 1
        File inputFile = new File(inputPath);
        Document document = documentBuilder.parse(inputFile);
        
        // Create an input source for the freight document
        InputSource freightSource = new InputSource(new FileReader(freightPath));
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = parserFactory.newSAXParser();
        XMLReader parser = saxParser.getXMLReader();
        ShipmentHandler shipmentHandler = new ShipmentHandler(document);
        parser.setContentHandler(shipmentHandler);

        // start the actual parsing
        parser.parse(freightSource);

        // Validate file before storing        
        File schemaFile = new File("resources/shipment.xsd");
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        
        // get the document from the ShipmentHandler
        document = shipmentHandler.getDocument();
        DOMSource domSource = new DOMSource(document);
        
        // validate
        try {
            validator.validate(domSource);
        } catch (SAXException e) {
            exit("It wasn't possible to validate the DOM source!" + "\n" + e.getMessage());
        }
        
        //store the document
        StreamResult result = new StreamResult(new File(outputPath));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(domSource, result);
    }

    /**
     * Prints an error message and exits with return code 1
     * 
     * @param message The error message to be printed
     */
    public static void exit(String message) {
    	System.err.println(message);
    	System.exit(1);
    }
    

}
