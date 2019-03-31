package utility;

import controllers.EditProjectViewController;
import controllers.MainWindowController;
import controllers.NewRequestViewController;
import controllers.RenameRequestViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import parsers.Project;
import parsers.ProjectParser;

import java.io.IOException;
import java.util.stream.Collectors;

public class TreeProjectCell extends TreeCell<String> {

    private ContextMenu requestContextMenu = new ContextMenu();
    private ContextMenu projectContextMenu = new ContextMenu();

    public TreeProjectCell(Scene mainWindowScene, MainWindowController mainWindowController){

        MenuItem renameItem = new MenuItem("Rename");
        MenuItem removeItem = new MenuItem("Remove");
        requestContextMenu.getItems().addAll(renameItem, removeItem);
        renameItem.setOnAction(event -> {
            String requestName = getTreeItem().getValue();
            String projectName = getTreeItem().getParent().getValue();
            Project project = ProjectParser.projectsList.stream().filter(proj ->
                proj.toString().equals(projectName)).collect(Collectors.toList()).get(0);
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/renameRequestView.fxml"));
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            RenameRequestViewController controller = fxmlLoader.getController();
            controller.setProject(project, requestName);
            stage.setScene(new Scene(fxmlLoader.getRoot(), 360, 90));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainWindowScene.getWindow());
            stage.showAndWait();
            mainWindowController.refreshProjectTree();
        });


        removeItem.setOnAction(event -> {
        });


        MenuItem editItem = new MenuItem("Edit");
        MenuItem addRequestItem = new MenuItem("Add Request");
        MenuItem removeProjectItem = new MenuItem("Remove");
        projectContextMenu.getItems().addAll(editItem, addRequestItem, removeProjectItem);
        editItem.setOnAction(event -> {

            String projectName = getTreeItem().getValue();
            Project project = ProjectParser.projectsList.stream().filter(proj ->
                    proj.toString().equals(projectName)).collect(Collectors.toList()).get(0);
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/editProjectView.fxml"));
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent editViewRoot = fxmlLoader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(editViewRoot, 570, 360));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainWindowScene.getWindow());
            EditProjectViewController controller = fxmlLoader.getController();
            controller.setProject(project);
            stage.showAndWait();
            mainWindowController.refreshProjectTree();
        });


        addRequestItem.setOnAction(event -> {
            String projectName = getTreeItem().getValue();
            Project project = ProjectParser.projectsList.stream().filter(proj ->
                    proj.toString().equals(projectName)).collect(Collectors.toList()).get(0);
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/newRequestView.fxml"));
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent newRequestRoot = fxmlLoader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(newRequestRoot, 350, 110));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainWindowScene.getWindow());
            NewRequestViewController controller = fxmlLoader.getController();
            controller.setProject(project);
            stage.showAndWait();
            mainWindowController.refreshProjectTree();

        });


        removeProjectItem.setOnAction(event -> {
            String projectName = getTreeItem().getValue();
            ProjectParser.deleteProject(projectName);
            mainWindowController.refreshProjectTree();
        });

    }

    @Override
    public void startEdit(){
        super.startEdit();
    }

    @Override
    public  void cancelEdit(){
        super.cancelEdit();
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
        }
        else {
            setText(getString());
            setGraphic(getTreeItem().getGraphic());
            if (getTreeItem().isLeaf() && getTreeItem().getParent().getParent()!= null){
                setContextMenu(requestContextMenu);
            }
            else if(getTreeItem().getParent().getParent() == null){
                setContextMenu(projectContextMenu);
            }
        }
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
