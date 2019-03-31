package parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SettingsParser {

    private static Document settingsXMLDocument;
    public static Map<String, Map<String, String>> connectSettings = new HashMap<>();    // настройки коннекта к шине и Siebel
    private static String filePath;

    public static void getConnectSettings() {

        filePath = System.getProperty("user.dir") + "/config/settings";
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            settingsXMLDocument = documentBuilder.parse(filePath);

            NodeList connections = settingsXMLDocument.getElementsByTagName("connection");
            for (int i = 0; i < connections.getLength(); i++) {
                Map<String, String> connectionParam = new HashMap<>();
                Node currentParent = connections.item(i);
                for (int j = 0; j < currentParent.getChildNodes().getLength(); j++) {
                    Node currentChild = currentParent.getChildNodes().item(j);
                    if(!currentChild.getNodeName().equals("#text")){
                        connectionParam.put(currentChild.getNodeName(), currentChild.getTextContent());
                    }
                }
                connectSettings.put(currentParent.getAttributes().getNamedItem("serverName").getTextContent(), connectionParam);

            }

        } catch (ParserConfigurationException e) {
            System.out.println("Ошибка парсера конфигурации");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Не удалось найти файл конфигурации (получить доступ)");
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("Ошибка парсера конфигурации");
            e.printStackTrace();
        }
    }

    public static void saveConnectSettings(String serverName){
        Set listOfParam = connectSettings.get(serverName).keySet();

        XPathFactory  pathFactory = XPathFactory.newInstance();
        XPath xPath = pathFactory.newXPath();
        try {
            Node serverSettings = ((NodeList) xPath.evaluate("//connection[@serverName='" + serverName + "']",
                    settingsXMLDocument, XPathConstants.NODESET)).item(0);
            for(Object nameTag: listOfParam){
                Node currentNode = ((NodeList) xPath.evaluate((String) nameTag, serverSettings, XPathConstants.NODESET)).item(0);
                currentNode.setTextContent(connectSettings.get(serverName).get(nameTag));
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        try{
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(settingsXMLDocument);
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            StreamResult result = new StreamResult(fileOutputStream);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            System.out.println("Ошибка создания трансформера");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("Не найден файл конфигурации");
            e.printStackTrace();
        } catch (TransformerException e) {
            System.out.println("Ошибка трансформации документа");
            e.printStackTrace();
        }
    }
}
