package controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import parsers.Project;
import parsers.ProjectParser;

public class EditProjectViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ChoiceBox<String> typeChoiceBox;

    @FXML
    private Button changeProjectButton;

    @FXML
    private TextField inputQueueTextField;

    @FXML
    private TextField outputTextField;

    @FXML
    private TextField wfNameTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private Button saveProjectButton;

    @FXML
    private TextField filePathTextField;

    private Project project;
    private Project tempProject;
    private String oldProjectName;

    @FXML
    void initialize() {
        ObservableList<String> projectType = FXCollections.observableArrayList();
        projectType.addAll("dummy", "input");
        this.typeChoiceBox.setItems(projectType);

        changeProjectButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(changeProjectButton.getScene().getWindow());
            if(file != null) {
                tempProject = ProjectParser.parseProjectFile(file.getAbsolutePath(), project.toString());
                tempProject.projectIsChanged = true;
                this.showProjectParam(tempProject);
            }
        });

        saveProjectButton.setOnAction(event -> {
            String newProjectName = this.nameTextField.getText();
            if((oldProjectName.equals(newProjectName)) ? false : ProjectParser.projectNameIsExisted(newProjectName)){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("THIS NAME ALREADY EXISTS");
                alert.show();
                return;
            }
            if(tempProject == null){
                this.project.changeProject(this.nameTextField.getText(), this.typeChoiceBox.getValue(),
                        this.inputQueueTextField.getText(), this.outputTextField.getText(), this.wfNameTextField.getText(),
                        this.filePathTextField.getText());
            }
            else {
                ProjectParser.deleteProject(this.project.toString());
                this.tempProject.changeProject(this.nameTextField.getText(), this.typeChoiceBox.getValue(),
                        this.inputQueueTextField.getText(), this.outputTextField.getText(), this.wfNameTextField.getText(),
                        this.filePathTextField.getText());
                ProjectParser.addProjectToList(tempProject);
            }
            Stage stage = (Stage) saveProjectButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setProject(Project project){
        this.project = project;
        this.oldProjectName = project.toString();
        showProjectParam(project);
    }

    private void showProjectParam(Project project){

        this.typeChoiceBox.setValue(project.getType());
        this.nameTextField.setText(project.toString());
        this.inputQueueTextField.setText(project.getInputQueue());
        this.outputTextField.setText(project.getOutputQueue());
        this.wfNameTextField.setText(project.getWorkFlowName());
        this.filePathTextField.setText(project.getFileDirectory());
    }
}
