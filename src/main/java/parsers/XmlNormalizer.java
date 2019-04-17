package parsers;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlNormalizer {

    private static DocumentBuilder builder;
    private static Transformer transformer;

    static {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        }

        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    }

    public static String normalizeXml(String xmlInput) throws IOException, SAXException, TransformerException {

        InputSource inputSource = new InputSource();
        Document document;
        String outXml;

        try(StringReader reader = new StringReader(xmlInput)){
            inputSource.setCharacterStream(reader);
            document = builder.parse(inputSource);
        }

        DOMSource source = new DOMSource(document);
        try(StringWriter writer = new StringWriter()){
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            outXml = writer.toString();
        }

        return outXml;
    }
}
