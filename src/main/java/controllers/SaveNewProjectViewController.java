package controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import parsers.Project;

public class SaveNewProjectViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button directoryButton;

    @FXML
    private TextField fileNameTextField;

    @FXML
    private TextField directoryTextField;

    @FXML
    private Button saveButton;

    private String directoryPath;
    private Project project;
    private Stage stage;

    @FXML
    void initialize() {

        directoryButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File file = directoryChooser.showDialog(directoryButton.getScene().getWindow());
            this.directoryPath = file.getAbsolutePath();
            if(directoryPath != null) directoryTextField.setText(directoryPath);
        });

        saveButton.setOnAction(event -> {
            if(directoryPath == null){
                Alert alert = new Alert(Alert.AlertType.NONE, "Directory isn't set", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            else if(fileNameTextField.getText().equals("")){
                Alert alert = new Alert(Alert.AlertType.NONE, "FileName isn't set", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            String filePath = directoryPath + "\\" + fileNameTextField.getText() + ".xml";
            project.setFileDirectory(filePath);
            project.transformToXmlFile(filePath);
            stage.close();
        });
    }

    public void setProject(Project project){
        stage = (Stage) this.fileNameTextField.getScene().getWindow();
        this.project = project;
    }
}

