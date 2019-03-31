import controllers.SaveNewProjectViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import parsers.Project;
import parsers.ProjectParser;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("fxml/mainWindow.fxml"));
        primaryStage.setTitle("JMS-UI");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(360);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            ButtonType yes = ButtonType.YES;
            ButtonType no = ButtonType.NO;
            Alert exitAlert = new Alert(Alert.AlertType.NONE, "Exit JMS_UI?", yes, no);
            Optional<ButtonType> closeResult = exitAlert.showAndWait();
            if(closeResult.get() == ButtonType.NO) {
                event.consume();
                return;
            }

            for(Project project : ProjectParser.projectsList){
                if(project.projectIsNew || project.projectIsChanged) {
                    ButtonType cancel = ButtonType.CANCEL;
                    Alert alert = new Alert(Alert.AlertType.NONE, "Сохранить проект " + project.toString() + "?",
                            yes, no, cancel);
                    Optional<ButtonType> saveResult = alert.showAndWait();
                    ButtonType bt = saveResult.get();
                    if(bt == ButtonType.CANCEL) event.consume();
                    if(bt == ButtonType.YES){
                        if(project.getFileDirectory() == null){
                            FXMLLoader loader = new FXMLLoader();
                            loader.setLocation(getClass().getResource("/fxml/saveNewProjectView.fxml"));
                            try {
                                loader.load();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Stage stage = new Stage();
                            stage.setScene(new Scene(loader.getRoot(),400,140));
                            stage.setResizable(false);
                            SaveNewProjectViewController controller = loader.getController();
                            controller.setProject(project);
                            stage.showAndWait();
                        }
                        project.transformToXmlFile();
                    }
                }
            }
            ProjectParser.saveProjectConfig();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }

}
