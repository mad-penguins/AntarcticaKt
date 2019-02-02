package UI;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import models.File;
import models.User;
import services.FileService;

import java.sql.Timestamp;
import java.util.*;

public class MainWindowController {

    class FileRow {
        SimpleStringProperty nameProperty;
        SimpleStringProperty lastModifiedProperty;

        FileRow(String name, Timestamp lastModified) {
            this.nameProperty = new SimpleStringProperty(name);
            this.lastModifiedProperty = new SimpleStringProperty(lastModified.toString());
        }

        SimpleStringProperty getNameProperty() {
            return nameProperty;
        }

        SimpleStringProperty getLastModifiedProperty() {
            return lastModifiedProperty;
        }
    }

    public Button addFileButton;
    public TreeTableView<FileRow> filesTree;
    public TreeTableColumn<FileRow, String> fileNameColumn;
    public TreeTableColumn<FileRow, String> modifiedColumn;
    public Label usernameLabel;

    private User user;

    void setUser(User user) {
        this.user = user;

        FileService fileService = new FileService(user.getID(), user.getPassword());

        usernameLabel.setText("Files of " + user.getName());

        List<File> userFiles = fileService.getAll();
        TreeItem<FileRow> root = new TreeItem<>(new FileRow("n", new Timestamp(0)));
        fileNameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileRow, String> param) -> param.getValue().getValue().getNameProperty());
        modifiedColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileRow, String> param) -> param.getValue().getValue().getLastModifiedProperty());

        HashMap<String, ArrayList<FileRow>> filesByDir = new HashMap<>();
        for (File userFile : userFiles) {
            System.out.println(userFile.toString());
            if (filesByDir.containsKey(userFile.getPath())) {
                filesByDir.get(userFile.getPath()).add(new FileRow(userFile.getName(), userFile.getTime()));
            } else {
                filesByDir.put(userFile.getPath(), new ArrayList<>(Collections.singletonList(new FileRow(userFile.getName(), userFile.getTime()))));
            }
        }

        for (Map.Entry<String, ArrayList<FileRow>> entry : filesByDir.entrySet()) {
            TreeItem<FileRow> dir = new TreeItem<>(new FileRow(entry.getKey(), new Timestamp(0)));
            for (FileRow _file : entry.getValue()) {
                TreeItem<FileRow> row = new TreeItem<>(_file);
                dir.getChildren().add(row);
            }
            root.getChildren().add(dir);
        }
        filesTree.setRoot(root);
        filesTree.setShowRoot(false);
    }

    public void addFileClicked(ActionEvent actionEvent) {

    }
}
