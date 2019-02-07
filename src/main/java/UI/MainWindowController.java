package UI;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.File;
import models.User;
import services.FileService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
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

    public AnchorPane rootAP;
    public Button addFileButton;
    public TreeTableView<FileRow> filesTree;
    public TreeTableColumn<FileRow, String> fileNameColumn;
    public TreeTableColumn<FileRow, String> modifiedColumn;
    public Label usernameLabel;

    private User user;

    private void updateFilesTable() {
        FileService fileService = new FileService(user.getID(), user.getPassword());
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

    void setUser(User user) {
        this.user = user;
        usernameLabel.setText("Files of " + user.getName());

        updateFilesTable();
    }

    public void addFileClicked(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFileDialog.fxml"));
        Parent root = loader.load();
        AddFileDialogController controller = loader.getController();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();

        String files_list = controller.filePathInput.getText();
        String[] files_path = files_list.split(",");

        List<File> files;

        for (String path : files_path) {
            java.io.File temp_file = new java.io.File(path);
            if (!temp_file.exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Wrong file");
                alert.setHeaderText("Wrong file");
                alert.setContentText("File " + path + " does not exist!");
                alert.showAndWait();
                return;
            }

            if (temp_file.isFile()) {
                insertFileIntoDB(temp_file, path);
                updateFilesTable();
            }

            if (temp_file.isDirectory()) {
                insertCatalogIntoDB(temp_file);
                updateFilesTable();
            }
        }
    }

    private void insertFileIntoDB(java.io.File dirFile, String path) throws IOException {
        String name = path.substring(path.lastIndexOf('/')+1);
        String location = path.substring(0, path.lastIndexOf('/'));

        byte[] content = Files.readAllBytes(dirFile.toPath());

        File file = new File(
                name,
                location,
                content,
                Timestamp.from(
                        Files.readAttributes(
                                dirFile.toPath(),
                                BasicFileAttributes.class).creationTime().toInstant()
                )
        );

        FileService fileService = new FileService(user.getID(), user.getPassword());
        fileService.save(file);
    }

    private void insertCatalogIntoDB(java.io.File catalog) throws IOException {
        java.io.File[] filesInDir = catalog.listFiles();
        for (java.io.File dirFile : filesInDir) {
            if (dirFile.isDirectory()) {
                insertCatalogIntoDB(dirFile);
            } else if (dirFile.isFile()) {
                String path = dirFile.getAbsolutePath();
                insertFileIntoDB(dirFile, path);
            }
        }
    }
}
