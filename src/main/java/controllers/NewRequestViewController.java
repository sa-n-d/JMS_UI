package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import parsers.Project;

import java.net.URL;
import java.util.ResourceBundle;

public class NewRequestViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField nameTextField;

    @FXML
    private Button addButton;

    private Project project;

    @FXML
    void initialize() {

        this.nameTextField.setText("NewRequest");

        addButton.setOnAction(event -> {
            String requestName = this.nameTextField.getText();
            if(project.requests.containsKey(requestName)){
                nameTextField.setStyle("-fx-border-color: red");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("THIS NAME ALREADY EXISTS");
                alert.show();
            }
            else {
                project.requests.put(requestName, "Set Request Text");
                ((Stage) this.nameTextField.getScene().getWindow()).close();
            }
        });
    }

    /**
     * Method sets selected project
     * @param project   selected project
     */
    public void setProject(Project project){
        this.project = project;
    }
}
