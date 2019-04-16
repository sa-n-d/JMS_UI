package parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectParser {

    private static Document projectXMLDocument;
    private static String configFilePath;
    public static ArrayList<Project> projectsList = new ArrayList<>();
    private static DocumentBuilder builder;
    private static Transformer transformer;
    private static XPath xPath;
    private static NodeList projectsNodeList;

    static{
        configFilePath = System.getProperty("user.dir") + "/config/projectsList";
        try{
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPathFactory pathFactory = XPathFactory.newInstance();
            xPath = pathFactory.newXPath();
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (ParserConfigurationException e) {
            System.out.println("Ошибка парсера конфигурации проектов");
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            System.out.println("Ошибка создания трансформера");
            e.printStackTrace();
        }
    }

    public static ArrayList<Project> parseProjectConfig(){

        try {
            projectXMLDocument = builder.parse(configFilePath);
            projectsNodeList = projectXMLDocument.getElementsByTagName("project");
            for (int i = 0; i < projectsNodeList.getLength(); i++) {
                Node currentProject = projectsNodeList.item(i);
                if(currentProject.getNodeType() == Node.TEXT_NODE) continue;
                Project project;
                String projectName = currentProject.getAttributes().getNamedItem("name").getTextContent();
                String projectPath = currentProject.getChildNodes().item(1).getTextContent();

                if(projectPath.equals("") || !Files.exists(Paths.get(projectPath))){
                    project = new Project(projectName);
                    projectsList.add(project);
                    continue;
                }
                project = parseProjectFile(projectPath, projectName);
                projectsList.add(project);
            }
        } catch (SAXException e) {
            System.out.println("Ошибка парсера конфигурации");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Не удалось найти файл конфигурации проектов, либо файл залочен");
            e.printStackTrace();
        }
        return projectsList;
    }

    public static Project parseProjectFile(String projectFilePath, String projectName){

        try{
            Document projectDoc = builder.parse(projectFilePath);
            Node name = (Node) xPath.evaluate("//name", projectDoc, XPathConstants.NODE);
            Node type = (Node) xPath.evaluate("//type", projectDoc, XPathConstants.NODE);
            Node inputQueue = (Node) xPath.evaluate("//inputQueue", projectDoc, XPathConstants.NODE);
            Node outputQueue = (Node) xPath.evaluate("//outputQueue", projectDoc, XPathConstants.NODE);
            Node wfName = (Node) xPath.evaluate("//wfName", projectDoc, XPathConstants.NODE);
            NodeList requests = (NodeList) xPath.evaluate("//request", projectDoc, XPathConstants.NODESET);

            Map<String,String> requestsMap = new HashMap<>();
            for(int j=0; j < requests.getLength(); j++){
                Node currentRequest = requests.item(j);
                if(currentRequest.getNodeType() == 3)continue;
                String requestName = currentRequest.getAttributes().getNamedItem("name").getTextContent();
                requestsMap.put(requestName, currentRequest.getTextContent());
            }
            return new Project(name.getTextContent(), type.getTextContent(), inputQueue.getTextContent(),
                    outputQueue.getTextContent(), wfName.getTextContent(), projectFilePath, requestsMap);
        }
        catch (SAXException | IOException exception){
            System.out.println("Не удалось распарсить проект");
            return new Project(projectName);
        }
        catch (XPathExpressionException exception){
            System.out.println("Некорректный файл проекта");
        }
        return null;
    }


    public static boolean addProjectToList(Project project){
        if(!projectNameIsExisted(project.toString())){
            projectsList.add(project);
            return true;
        }
        return false;
    }

    public static void deleteProject(String name){
        for(Project project: projectsList){
            if(project.toString().equals(name)){
                projectsList.remove(project);
                break;
            }
        }
        try {
            Node nodeForDeleting = (Node) xPath.evaluate("//project[@name='" + name + "']", projectXMLDocument, XPathConstants.NODE);
            if(nodeForDeleting != null) projectXMLDocument.getDocumentElement().removeChild(nodeForDeleting);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        saveDocument(projectXMLDocument, configFilePath);
    }

    private static void saveDocument(Document doc, String filePath){
        try{
            DOMSource source = new DOMSource(doc);
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            StreamResult result = new StreamResult(fileOutputStream);
            transformer.transform(source, result);
        } catch (FileNotFoundException e) {
            System.out.println("Не найден файл конфигурации");
            e.printStackTrace();
        } catch (TransformerException e) {
            System.out.println("Ошибка трансформации документа");
            e.printStackTrace();
        }
    }

    public static boolean projectNameIsExisted(String projectName){
        int count = ProjectParser.projectsList.stream().filter(proj ->
                proj.toString().equals(projectName)).collect(Collectors.toList()).size();
        return count != 0;
    }

    public static void saveProjectConfig(){
        Document configDoc = builder.newDocument();
        Element rootElement = configDoc.createElement("projectsList");
        for(Project project : projectsList){
            if(!project.needSave()){
                Element childElement = configDoc.createElement("project");
                childElement.setAttribute("name", project.toString());
                Element filePathElement = configDoc.createElement("filePath");
                filePathElement.setTextContent(project.getFileDirectory());
                childElement.appendChild(filePathElement);
                rootElement.appendChild(childElement);
            }
        }
        configDoc.appendChild(rootElement);
        saveDocument(configDoc, configFilePath);
    }
}
