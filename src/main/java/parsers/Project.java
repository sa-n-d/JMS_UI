package parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Project {

    private String name;
    private String type;
    private String inputQueue;
    private String outputQueue;
    private String workFlowName;
    private String tempFileDirectory;

    public boolean projectIsChanged = false;
    public boolean projectFileNotFound = true;
    public boolean projectIsNew = false;

    public Map<String,String> requests = new HashMap<>();

    private static DocumentBuilder builder;
    private static Transformer transformer;

    static{
        try{
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (ParserConfigurationException e) {
            System.out.println("Ошибка парсера конфигурации проектов");
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            System.out.println("Ошибка создания трансформера");
            e.printStackTrace();
        }
    }

    public Project(String name){
        this.name = name;
    }

    public Project(String name, String projectType, String inputQueue, String outputQueue, String workFlowName,
                   String fileDirectory, Map<String, String> requests){
        this.name = name;
        this.type = projectType;
        this.requests = requests;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.workFlowName = workFlowName;
        this.tempFileDirectory = fileDirectory;
        this.projectFileNotFound = false;
        this.projectIsNew = (fileDirectory == null);

        this.parseProject();
    }

    @Override
    public String toString(){
        return name;
    }

    public String getType(){
        return type;
    }

    public String getInputQueue(){
        return inputQueue;
    }

    public String getOutputQueue(){
        return outputQueue;
    }

    public String getWorkFlowName(){
        return workFlowName;
    }

    public String getFileDirectory() {
        return tempFileDirectory;
    }

    public void setFileDirectory(String fileDirectory){
        this.tempFileDirectory = fileDirectory;
    }

    public void addRequest(String name, String request){
        if(requests.containsKey(name)){
            System.out.println("Запрос с таким именем уже существует");
            return;
        }
        this.requests.put(name, request);
        projectIsChanged = true;
    }

    public void deleteRequest(String name){
        this.requests.remove(name);
        projectIsChanged = true;
    }

    public void changeProject(String name, String projectType, String inputQueue, String outputQueue,
                              String workFlowName, String filePath){
        this.name = name;
        this.type = projectType;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.workFlowName = workFlowName;
        this.tempFileDirectory = filePath;

        this.projectIsChanged = true;
    }

    public void transformToXmlFile(){
        this.transformToXmlFile(tempFileDirectory);
    }

    public void transformToXmlFile(String filePath){

        Document document = builder.newDocument();
        Element rootNode = document.createElement("project");
        rootNode.setAttribute("name", name);
        Element nameElement = document.createElement("name");
        nameElement.setTextContent(name);
        Element typeElement = document.createElement("type");
        typeElement.setTextContent(type);
        Element inputQElement = document.createElement("inputQueue");
        inputQElement.setTextContent(inputQueue);
        Element outputQElement = document.createElement("outputQueue");
        outputQElement.setTextContent(outputQueue);
        Element wfNameElement = document.createElement("wfName");
        Element listOfRequests = document.createElement("listOfRequests");
        wfNameElement.setTextContent(workFlowName);
        rootNode.appendChild(nameElement);
        rootNode.appendChild(typeElement);
        rootNode.appendChild(inputQElement);
        rootNode.appendChild(outputQElement);
        rootNode.appendChild(wfNameElement);
        rootNode.appendChild(listOfRequests);

        for(String reqName : requests.keySet()){
            String currentRequest = requests.get(reqName);
            Element childTag = document.createElement("request");
            childTag.setAttribute("name", reqName);
            childTag.setTextContent(currentRequest);
            listOfRequests.appendChild(childTag);
        }
        document.appendChild(rootNode);

        if(projectIsNew){
            File file = new File(filePath);
            try {
                boolean fileIsCreated = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try{
            DOMSource source = new DOMSource(document);
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            StreamResult result = new StreamResult(fileOutputStream);
            transformer.transform(source, result);
        } catch (FileNotFoundException e) {
            System.out.println("Не найден файл");
            e.printStackTrace();
        } catch (TransformerException e) {
            System.out.println("Ошибка трансформации документа");
            e.printStackTrace();
        }

        this.projectIsChanged = false;
        this.projectIsNew = false;
    }

    public boolean needSave(){
        return (projectIsNew || projectIsChanged);
    }

    private void parseProject(){
    }
}
