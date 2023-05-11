package ssd;

import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * TODO: Implement this content handler.
 */
public class ShipmentHandler extends DefaultHandler {
    /**
     * Use this xPath variable to create xPath expression that can be
     * evaluated over the XML document.
     */
    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    /**
     * Store and manipulate the shipment XML document here.
     */
    private final Document shipmentDoc;

    /**
     * This variable stores the text content of XML Elements.
     */
    private String eleText;

    /**
     * Insert local variables here
     */
    private final Random random = new Random();
    private Product currProduct;
    private final List<String> catalogsToDelete = new ArrayList<>();
    private List<String> catalogs;
    private Element product;


    static class Product {
        private String producerElement;
        private String catalogElement;
        private String nameElement;
        private String typeElement;
        private String foodType;
        private String storageInfo;
        private String destinationElement;
        private String refElement;
        private String sid;
        private String labelElement;
        private final List<String> tagElements = new ArrayList<>();

        @Override
        public String toString() {
            return "Product{" +
                    "producerElement='" + producerElement + '\'' +
                    ", nameElement='" + nameElement + '\'' +
                    ", typeElement='" + typeElement + '\'' +
                    ", foodType='" + foodType + '\'' +
                    ", storageInfo='" + storageInfo + '\'' +
                    ", destinationElement='" + destinationElement + '\'' +
                    ", refElement='" + refElement + '\'' +
                    ", sid='" + sid + '\'' +
                    ", labelElement='" + labelElement + '\'' +
                    ", tagElements=" + tagElements +
                    '}';
        }
    }

    public ShipmentHandler(Document doc) {
        shipmentDoc = doc;
    }

    @Override
    /**
     * SAX calls this method to pass in character data
     */
    public void characters(char[] text, int start, int length) throws SAXException {
        eleText = new String(text, start, length);
    }

    /**
     * Return the current stored shipment document
     *
     * @return XML Document
     */
    public Document getDocument() {
        return shipmentDoc;
    }

    //***TODO***
    //Specify additional methods to parse the freight document and modify the shipmentDoc
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "type":
                currProduct.foodType = attributes.getValue("foodType");
                currProduct.storageInfo = attributes.getValue("storageInfo");
                break;
            case "product":
                currProduct = new Product();
                break;
            case "ref":
                currProduct.sid = attributes.getValue("sid");
                break;
            case "shortage":
                break;
        }

        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        switch (qName) {
            case "producer":
                currProduct.producerElement = eleText;
                break;
            case "name":
                currProduct.nameElement = eleText;
                break;
            case "type":
                currProduct.typeElement = eleText;
                break;
            case "destination":
                currProduct.destinationElement = eleText;
                break;
            case "ref":
                currProduct.refElement = eleText;
                break;
            case "label":
                currProduct.labelElement = eleText;
                break;
            case "tag":
                currProduct.tagElements.add(eleText);
                break;
            case "catalog":
                catalogsToDelete.add(eleText);
                break;
            case "product":
                try {
                    currProduct.catalogElement = handleCatalog();
                    Node products = (Node) xPath.evaluate("//products", shipmentDoc, XPathConstants.NODE);
                    createProduct();
                    products.appendChild(product);
                    product = null;
                    currProduct = null;
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }
                break;
            case "shortage":
                deleteProductWithCatalog();
                break;
        }
    }

    private void createProduct() {
        // product
        product = shipmentDoc.createElement("product");
        // name
        product.appendChild(createElementWithContent("name", currProduct.nameElement));
        // type
        Element typeEl = shipmentDoc.createElement("type");
        if (currProduct.typeElement.equals("food")) {
            Element food = shipmentDoc.createElement("food");
            food.setAttribute("foodType", currProduct.foodType);
            food.setAttribute("storageInfo", currProduct.storageInfo);
            typeEl.appendChild(food);
        } else if (currProduct.typeElement.equals("clothing")) {
            typeEl.appendChild(shipmentDoc.createElement("clothing"));
        }
        product.appendChild(typeEl);
        // label
        List<String> sids = new ArrayList<>();
        try {
            NodeList nodeSids = (NodeList) xPath.evaluate("//ships/ship/@sid", shipmentDoc, XPathConstants.NODESET);
            for (int i = 0; i < nodeSids.getLength(); i++) {
                sids.add(nodeSids.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        Element labelEl = createElementWithContent("label", currProduct.labelElement);
        labelEl.appendChild(createElementWithContent("producer", currProduct.producerElement));
        labelEl.appendChild(createElementWithContent("destination", currProduct.destinationElement));
        if (sids.contains(currProduct.sid)) {
            Element refEl = shipmentDoc.createElement("ref");
            refEl.setTextContent(currProduct.refElement);
            refEl.setAttribute("sid", currProduct.sid);
            labelEl.appendChild(refEl);
        }
        product.appendChild(labelEl);

        // catalog
        product.appendChild(createElementWithContent("catalog", currProduct.catalogElement));

        // tag
        if (!currProduct.tagElements.isEmpty()) {
            List<String> tags = new ArrayList<>();
            try {
                NodeList nodeTags = (NodeList) xPath.evaluate("//tags/t/@tagname", shipmentDoc, XPathConstants.NODESET);
                for (int i = 0; i < nodeTags.getLength(); i++) {
                    tags.add(nodeTags.item(i).getNodeValue());
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            Element tagsEl = shipmentDoc.createElement("tags");
            int count = 0;
            for (String tag : currProduct.tagElements) {
                if (tags.contains(tag)) {
                    tagsEl.appendChild(createElementWithContent("tag", tag));
                    count++;
                }
            }
            if (count > 0) {
                product.appendChild(tagsEl);
            }
        }
    }

    private Element createElementWithContent(String name, String content) {
        Element element = shipmentDoc.createElement(name);
        element.setTextContent(content);
        return element;
    }

    private void deleteProductWithCatalog() {
        for (String catToDel : catalogsToDelete) {
            try {
                NodeList productsToRemove = (NodeList) xPath.evaluate("//products/product[catalog/text() = '"+catToDel+"']", shipmentDoc, XPathConstants.NODESET);
                for (int i = 0; i < productsToRemove.getLength(); i++) {
                    productsToRemove.item(i).getParentNode().removeChild(productsToRemove.item(i));
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    }

    private String handleCatalog() {
        try {
            if (catalogs == null) {
                catalogs = new ArrayList<>();
                NodeList products = (NodeList) xPath.evaluate("//proudcts/product", shipmentDoc, XPathConstants.NODESET);
                for (int i = 0; i < products.getLength(); i++) {
                    catalogs.add(products.item(i).getAttributes().getNamedItem("catalog").getNodeValue());
                }
            }
            String catalog = generateCatalog();
            while (catalogs.contains(catalog)) {
                catalog = generateCatalog();
            }
            catalogs.add(catalog);
            return catalog;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateCatalog() {
        String catalog;
        int firstUppercaseIndex = 'A'; // for uppercase
        int firstLowercaseIndex = 'a'; // for lowercase
        int letterIndex = random.nextInt(26); // random number between 0 and 26
        catalog = (char) (firstUppercaseIndex + letterIndex) + "#";
        for (int i = 0; i < 4; i++) {
            catalog += random.nextInt(10);
        }
        catalog += "#";
        for (int i = 0; i < 3; i++) {
            catalog += (char) (random.nextInt(7) + firstLowercaseIndex);
        }
        return catalog;
    }
}

