package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jms.JMSManager;
import parsers.Project;
import parsers.ProjectParser;
import parsers.SettingsParser;
import utility.TreeProjectCell;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

public class MainWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ImageView settingsImageButton;

    @FXML
    private ImageView reconnectImageButton;

    @FXML
    private ImageView runImageButton;

    @FXML
    private ImageView stopImageButton;

    @FXML
    private ImageView newImageButton;

    @FXML
    private ImageView importImageButton;

    @FXML
    private ImageView statusImage;

    @FXML
    private ChoiceBox<String> serverName;

    @FXML
    private TextArea loggerTextArea;

    @FXML
    private TreeView<String> projectsTree;

    @FXML
    private TextArea workingTextArea;

    @FXML
    private TextArea responseTextArea;

    //@FXML
    //private ContextMenu projectContextMenu;

    private String currentServer;
    private JMSManager jmsManager;
    private Project selectedProject;
    private String currentRequestName;
    private String selectedProjectType;
    private String inputQueue;
    private String outputQueue;
    private boolean connectionIsActive = false;
    private ArrayList<Project> listOfProjects;

    @FXML
    void mouseEnteredMenuButton(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setStyle("-fx-border-color: grey; -fx-border-radius: 10;" +
                " -fx-background-color: #c4ec9e; -fx-background-radius: 10;");
    }

    @FXML
    void mouseExitedMenuButton(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setStyle("-fx-border-color: grey; -fx-border-radius: 10;" +
                " -fx-background-color: #ecf1c8; -fx-background-radius: 10;");
    }

    @FXML
    void runClicked() {
        if(connectionIsActive && selectedProject != null){
            if(selectedProject.getType().equals("input")){
                String xmlRequest = workingTextArea.getText();
                responseTextArea.clear();
                jmsManager.sendMessage(xmlRequest, inputQueue, outputQueue);
            }
            else{
                String dummyXML = workingTextArea.getText();
                responseTextArea.clear();
                jmsManager.runDummy(dummyXML, inputQueue, outputQueue);
                loggerTextArea.appendText("Заглушка активирована\n");
            }
        }
        else if(!connectionIsActive){
            loggerTextArea.appendText("Необходимо выбрать сервер\n");
        }
    }

    @FXML
    void stopClicked(){
        if(jmsManager.dummyIsRunning()){
            jmsManager.stopDummy();
            loggerTextArea.appendText("Заглушка отключена\n");
        }
    }

    @FXML
    void importButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.importImageButton.getScene().getWindow());
        if(file !=null){
            Project project = ProjectParser.parseProjectFile(file.getAbsolutePath(), null);
            ProjectParser.addProjectToList(project);
            this.refreshProjectTree();
        }
    }

    @FXML
    void reconnectClicked() {
        currentServer = serverName.getValue();
        if (currentServer != null) {
            if (connectionIsActive) {
                jmsManager.closeCurrentConnection();
            }
            Map<String, String> connParam = SettingsParser.connectSettings.get(currentServer);
            connectionIsActive = jmsManager.setConnection(connParam.get("hostESB"), connParam.get("portESB"),
                    connParam.get("channel"), connParam.get("queueManager"));
            if (connectionIsActive) {
                statusImage.setImage(new Image("/graphics/icons/connectStatusImage.png"));
                loggerTextArea.appendText("Установлено соединение с шиной сервера " + currentServer + "\n");
            } else {
                statusImage.setImage(new Image("/graphics/icons/disconnectStatusImage.png"));
                loggerTextArea.appendText("Не удалось установить соединение с шиной сервера " + currentServer + "\n");
            }
        }
    }

    @FXML
    void newProjectButtonClicked() {

        Parent newProjectRoot = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/newProjectView.fxml"));
            loader.load();
            newProjectRoot = loader.getRoot();
        }
        catch (IOException ex){
            System.out.println("Не удалось загрузить newProjectView.fxml");
        }
        Stage newProjectStage = new Stage();
        newProjectStage.setScene(new Scene(newProjectRoot, 550, 290));
        newProjectStage.setResizable(false);
        newProjectStage.initModality(Modality.WINDOW_MODAL);
        newProjectStage.initOwner(newImageButton.getScene().getWindow());
        newProjectStage.showAndWait();
        refreshProjectTree();
    }



    @FXML
    void settingsButtonClicked() {
        Parent settingsRoot = null;
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/settingsView.fxml"));
            loader.load();
            settingsRoot = loader.getRoot();
        }
        catch (IOException exception){
            System.out.println("Не удалось загрузить settingsView.fxml");
        }
        Stage settingsStage = new Stage();
        settingsStage.setScene(new Scene(settingsRoot, 560, 240));
        settingsStage.setResizable(false);
        settingsStage.initModality(Modality.WINDOW_MODAL);
        settingsStage.initOwner(settingsImageButton.getScene().getWindow());
        settingsStage.showAndWait();
    }

    @FXML
    void initialize() {

        jmsManager = new JMSManager(this.responseTextArea, this.loggerTextArea);

        // Распарсить файл конфига и получить параметры (static field)
        SettingsParser.getConnectSettings();

        ObservableList<String> choiceBoxList = FXCollections.observableArrayList();
        choiceBoxList.addAll(SettingsParser.connectSettings.keySet());
        serverName.setItems(choiceBoxList);

        // Распарсить файл с проектами
        listOfProjects = ProjectParser.parseProjectConfig();

        projectsTree.setEditable(false);
        projectsTree.setCellFactory(param -> new TreeProjectCell(projectsTree.getScene(), this));
        this.refreshProjectTree();
        projectsTree.setShowRoot(false);

        projectsTree.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(event.getClickCount() == 2){

                TreeItem<String> selectedItem = projectsTree.getSelectionModel().getSelectedItems().get(0);
                if(selectedItem == null) return;
                if(selectedItem.isLeaf()){
                    if(selectedProject != null){
                        String workTextContent = workingTextArea.getText();
                        String oldTextRequest = selectedProject.requests.get(currentRequestName);
                        if(!workTextContent.equals(oldTextRequest)){
                            selectedProject.projectIsChanged = true;
                            selectedProject.requests.put(currentRequestName, workTextContent);
                        }
                    }
                    String projectName = selectedItem.getParent().getValue();
                    for (Project project: listOfProjects) {
                        if(project.toString().equals(projectName)){
                            selectedProject = project;
                            currentRequestName = selectedItem.getValue();
                            selectedProjectType = project.getType();
                            inputQueue = project.getInputQueue();
                            outputQueue = project.getOutputQueue();
                            String xmlRequest = project.requests.get(currentRequestName);
                            this.workingTextArea.setText(xmlRequest);
                            break;
                        }
                    }
                }
            }
        });

        serverName.setOnAction(event -> {
            if(connectionIsActive){
                jmsManager.closeCurrentConnection();
            }
            currentServer = serverName.getValue();
            Map<String,String> connParam = SettingsParser.connectSettings.get(currentServer);
            connectionIsActive = jmsManager.setConnection(connParam.get("hostESB"), connParam.get("portESB"),
                    connParam.get("channel"), connParam.get("queueManager"));
            if(connectionIsActive) {
                statusImage.setImage(new Image("/graphics/icons/connectStatusImage.png"));
                loggerTextArea.appendText("Установлено соединение с шиной сервера " + currentServer + "\n");
            }
            else {
                statusImage.setImage(new Image("/graphics/icons/disconnectStatusImage.png"));
                loggerTextArea.appendText("Не удалось установить соединение с шиной сервера " + currentServer + "\n");
            }
        });

    }

    public void refreshProjectTree(){

        if(listOfProjects != null){
            TreeItem<String> rootItem = new TreeItem<>("PROJECTS");
            for (Project project : listOfProjects) {
                ImageView iconProject = new ImageView();
                TreeItem<String> projectItem = new TreeItem<>(project.toString());
                if(project.projectFileNotFound) {
                    iconProject.setImage(new Image("/graphics/icons/missing.png",15,
                            15,false,false));
                    projectItem.setGraphic(iconProject);
                }
                else {
                    if(project.getType().equals("dummy")) {
                        iconProject.setImage(new Image("/graphics/icons/dummy.png",15,
                                15,false,false));
                        projectItem.setGraphic(iconProject);
                    }
                    else {
                        iconProject.setImage(new Image("/graphics/icons/sender.png",17,
                                17,false,false));
                        projectItem.setGraphic(iconProject);
                    }
                }
                rootItem.getChildren().add(projectItem);
                for(String requestName : project.requests.keySet()){
                    TreeItem<String> request = new TreeItem<>(requestName);
                    projectItem.getChildren().add(request);
                }
            }
            projectsTree.setRoot(rootItem);
        }
    }

}

