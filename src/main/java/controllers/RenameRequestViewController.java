package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import parsers.Project;

public class RenameRequestViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    private TextField textField;

    private Project project;
    private String requestName;


    @FXML
    void initialize() {

        textField.setOnKeyPressed(event -> {
        if(event.getCode().equals(KeyCode.ENTER)){
            this.changeName();
        }
        });

        okButton.setOnAction(event -> this.changeName());

        cancelButton.setOnAction(event -> {
            ((Stage) this.textField.getScene().getWindow()).close();
        });
    }

    public void setProject(Project project, String requestName){
        this.project = project;
        this.requestName = requestName;
    }

    private void changeName(){
        String newRequestName = this.textField.getText();
        if(project.requests.containsKey(newRequestName)){
            Alert alert = new Alert(Alert.AlertType.NONE, "Запрос с таким именем уже существует",
                    ButtonType.OK);
            alert.showAndWait();
        }
        else {
            String xml = project.requests.get(requestName);
            project.requests.put(newRequestName, xml);
            project.requests.remove(requestName);
            project.projectIsChanged = true;
            ((Stage) this.textField.getScene().getWindow()).close();
        }
    }
}

