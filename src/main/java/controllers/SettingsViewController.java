package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import parsers.SettingsParser;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingsViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField hostESB;

    @FXML
    private TextField portESB;

    @FXML
    private TextField queueManager;

    @FXML
    private TextField portSiebel;

    @FXML
    private TextField enterprise;

    @FXML
    private TextField hostSiebel;

    @FXML
    private TextField channel;

    @FXML
    private ChoiceBox<String> serverName;

    @FXML
    private Button saveButton;

    private Map<String,String> currentParams;                       // параметры текущего сервера
    private String currentServer;                                   // текущий выбранный сервер

    @FXML
    void initialize() {

        ObservableList<String> choiceBoxList = FXCollections.observableArrayList();
        choiceBoxList.addAll(SettingsParser.connectSettings.keySet());
        serverName.setItems(choiceBoxList);

        Object[] str = SettingsParser.connectSettings.keySet().toArray();
        currentServer = (String) str[0];
        currentParams = SettingsParser.connectSettings.get(currentServer);

        serverName.setValue(currentServer);
        hostESB.setText(currentParams.get("hostESB"));
        portESB.setText(currentParams.get("portESB"));
        channel.setText(currentParams.get("channel"));
        queueManager.setText(currentParams.get("queueManager"));
        hostSiebel.setText(currentParams.get("hostSiebelServer"));
        portSiebel.setText(currentParams.get("portSiebelServer"));
        enterprise.setText(currentParams.get("enterpriseServer"));


        serverName.setOnAction(event -> {
            currentServer = serverName.getValue();
            currentParams = SettingsParser.connectSettings.get(currentServer);
            hostESB.setText(currentParams.get("hostESB"));
            portESB.setText(currentParams.get("portESB"));
            channel.setText(currentParams.get("channel"));
            queueManager.setText(currentParams.get("queueManager"));
            hostSiebel.setText(currentParams.get("hostSiebelServer"));
            portSiebel.setText(currentParams.get("portSiebelServer"));
            enterprise.setText(currentParams.get("enterpriseServer"));
        });

        saveButton.setOnAction(event -> {
            currentParams.put("hostESB", hostESB.getText());
            currentParams.put("portESB", portESB.getText());
            currentParams.put("channel", channel.getText());
            currentParams.put("queueManager", queueManager.getText());
            currentParams.put("hostSiebelServer", hostSiebel.getText());
            currentParams.put("portSiebelServer", portSiebel.getText());
            currentParams.put("enterpriseServer", enterprise.getText());
            SettingsParser.connectSettings.put(currentServer, currentParams);

            SettingsParser.saveConnectSettings(currentServer);
        });
    }
}