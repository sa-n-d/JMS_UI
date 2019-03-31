package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parsers.Project;
import parsers.ProjectParser;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class NewProjectViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ChoiceBox<String> typeChoiceBox;

    @FXML
    private Button addProjectButton;

    @FXML
    private TextField inputQueueTextField;

    @FXML
    private TextField outputTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField wfNameTextField;

    @FXML
    void initialize() {
        ObservableList<String> projectType = FXCollections.observableArrayList();
        projectType.addAll("dummy", "input");
        typeChoiceBox.setItems(projectType);

        addProjectButton.setOnAction(event -> {
            if(nameTextField.getText().equals("") || typeChoiceBox.getValue() == null
                    || inputQueueTextField.getText().equals("") || outputTextField.getText().equals("")){
                nameTextField.setStyle("-fx-border-color: red; -fx-border-radius: 5");
                typeChoiceBox.setStyle("-fx-border-color: red; -fx-border-radius: 5");
                inputQueueTextField.setStyle("-fx-border-color: red; -fx-border-radius: 5");
                outputTextField.setStyle("-fx-border-color: red; -fx-border-radius: 5");
                return;
            }
            Project newProject = new Project(nameTextField.getText(), typeChoiceBox.getValue(), inputQueueTextField.getText(),
                    outputTextField.getText(), wfNameTextField.getText(), null, new HashMap<>());
            newProject.addRequest("newRequest", "Set requestText");
            if(!ProjectParser.addProjectToList(newProject)){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("THIS NAME ALREADY EXISTS");
                alert.show();
                return;
            }
            Stage stage = (Stage) addProjectButton.getScene().getWindow();
            stage.close();
        });
    }
}