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
import models.Package;
import models.User;
import services.FileService;
import services.PackageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.util.*;

public class MainWindowController {

    private User user;

    void setUser(User user) {
        this.user = user;
        usernameLabel.setText("Files of " + user.getName());

        updateFilesTable();
        updatePackagesTable();
    }

    public AnchorPane rootAP;
    public Label usernameLabel;


    // files logic
    class FileRow {
        SimpleStringProperty nameProperty;
        SimpleStringProperty createdProperty;
        SimpleStringProperty lastModifiedProperty;

        FileRow(String name, Timestamp created, Timestamp lastModified) {
            this.nameProperty = new SimpleStringProperty(name);
            this.createdProperty = new SimpleStringProperty(created.toString());
            this.lastModifiedProperty = new SimpleStringProperty(lastModified.toString());
        }

        SimpleStringProperty getNameProperty() {
            return nameProperty;
        }

        SimpleStringProperty getCreatedProperty() {
            return createdProperty;
        }

        SimpleStringProperty getLastModifiedProperty() {
            return lastModifiedProperty;
        }
    }

    public Button addFileButton;
    public TreeTableView<FileRow> filesTree;
    public TreeTableColumn<FileRow, String> fileNameColumn;
    public TreeTableColumn<FileRow, String> createdColumn;
    public TreeTableColumn<FileRow, String> modifiedColumn;

    private void updateFilesTable() {
        FileService fileService = new FileService(user.getID(), user.getPassword());
        List<File> userFiles = fileService.getAll();

        TreeItem<FileRow> root = new TreeItem<>(new FileRow("n", new Timestamp(0), new Timestamp(0)));
        fileNameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<FileRow, String> param)
                        -> param.getValue().getValue().getNameProperty()
        );
        createdColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<FileRow, String> param)
                        -> param.getValue().getValue().getCreatedProperty()
        );
        modifiedColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<FileRow, String> param)
                        -> param.getValue().getValue().getLastModifiedProperty()
        );

        HashMap<String, ArrayList<FileRow>> filesByDir = new HashMap<>();
        for (File userFile : userFiles) {
            if (filesByDir.containsKey(userFile.getPath())) {
                filesByDir.get(userFile.getPath()).add(
                        new FileRow(userFile.getName(), userFile.getCreated(), userFile.getModified())
                );
            } else {
                filesByDir.put(
                        userFile.getPath(),
                        new ArrayList<>(
                                Collections.singletonList(
                                        new FileRow(userFile.getName(), userFile.getCreated(), userFile.getModified())
                                )
                        )
                );
            }
        }

        for (Map.Entry<String, ArrayList<FileRow>> entry : filesByDir.entrySet()) {
            TreeItem<FileRow> dir = new TreeItem<>(new FileRow(entry.getKey(), new Timestamp(0), new Timestamp(0)));
            for (FileRow _file : entry.getValue()) {
                TreeItem<FileRow> row = new TreeItem<>(_file);
                dir.getChildren().add(row);
            }
            root.getChildren().add(dir);
        }
        filesTree.setRoot(root);
        filesTree.setShowRoot(false);
    }

    public void addFileClicked() throws IOException {
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

        for (String _path : files_path) {
            String path = _path.trim();
            java.io.File temp_file = new java.io.File(path);
            if (!temp_file.exists()) {
                if (!path.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Wrong file");
                    alert.setHeaderText("Wrong file");
                    alert.setContentText("File " + path + " does not exist!");
                    alert.showAndWait();
                }
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
                                BasicFileAttributes.class
                        ).creationTime().toInstant()
                ),
                Timestamp.from(
                        Files.readAttributes(
                                dirFile.toPath(),
                                BasicFileAttributes.class
                        ).lastModifiedTime().toInstant()
                )
        );

        FileService fileService = new FileService(user.getID(), user.getPassword());
        fileService.save(file);
    }

    private void insertCatalogIntoDB(java.io.File catalog) throws IOException {
        java.io.File[] filesInDir = catalog.listFiles();
        assert filesInDir != null;
        for (java.io.File dirFile : filesInDir) {
            if (dirFile.isDirectory()) {
                insertCatalogIntoDB(dirFile);
            } else if (dirFile.isFile()) {
                String path = dirFile.getAbsolutePath();
                insertFileIntoDB(dirFile, path);
            }
        }
    }

    private void downloadFile(File obj) throws IOException {
        String path = obj.getPath()+'/'+obj.getName();
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs()) {
                return;
            }
            if (!file.createNewFile()) {
                return;
            }
        }
        Files.write(file.toPath(), obj.getContent());

        BasicFileAttributeView attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class);
        attributes.setTimes(
                FileTime.from(obj.getCreated().toInstant()),
                FileTime.from(obj.getModified().toInstant()),
                FileTime.from(obj.getModified().toInstant())
        );
    }


    // packages logic
    class PackageRow {
        SimpleStringProperty nameProperty;
        SimpleStringProperty repositoryProperty;
        SimpleStringProperty configsListProperty;

        PackageRow(String name, String repository, List<File> configsList) {
            this.nameProperty = new SimpleStringProperty(name);
            this.repositoryProperty = new SimpleStringProperty(repository);
            this.configsListProperty = new SimpleStringProperty(configsList.toString());
        }

        SimpleStringProperty getNameProperty() {
            return nameProperty;
        }

        SimpleStringProperty getRepositoryProperty() {
            return repositoryProperty;
        }

        SimpleStringProperty getLastConfigsListProperty() {
            return configsListProperty;
        }
    }

    public Button addPackageButton;
    public TreeTableView<PackageRow> packagesTree;
    public TreeTableColumn<PackageRow, String> packageNameColumn;
    public TreeTableColumn<PackageRow, String> repositoryColumn;
    public TreeTableColumn<PackageRow, String> configsListColumn;

    private void updatePackagesTable() {
        PackageService packageService = new PackageService(user.getID(), user.getPassword());
        List<Package> userPackages = packageService.getAll();

        TreeItem<PackageRow> root = new TreeItem<>(new PackageRow("", "", new ArrayList<>()));
        packageNameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<PackageRow, String> param)
                        -> param.getValue().getValue().getNameProperty()
        );
        repositoryColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<PackageRow, String> param)
                        -> param.getValue().getValue().getRepositoryProperty()
        );
        configsListColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<PackageRow, String> param)
                        -> param.getValue().getValue().getLastConfigsListProperty()
        );

        HashMap<String, ArrayList<PackageRow>> packagesByRepository = new HashMap<>();
        for (Package userPackage : userPackages) {
            if (packagesByRepository.containsKey(userPackage.getRepository().getName())) {
                packagesByRepository.get(userPackage.getRepository().getName()).add(
                        new PackageRow(userPackage.getName(), userPackage.getRepository().getName(), userPackage.getFiles())
                );
            } else {
                packagesByRepository.put(
                        userPackage.getRepository().getName(),
                        new ArrayList<>(
                                Collections.singletonList(
                                        new PackageRow(userPackage.getName(), userPackage.getRepository().getName(), userPackage.getFiles())
                                )
                        )
                );
            }
        }

        for (Map.Entry<String, ArrayList<PackageRow>> entry : packagesByRepository.entrySet()) {
            TreeItem<PackageRow> dir = new TreeItem<>(new PackageRow(entry.getKey(), "", new ArrayList<>()));
            for (PackageRow _package : entry.getValue()) {
                TreeItem<PackageRow> row = new TreeItem<>(_package);
                dir.getChildren().add(row);
            }
            root.getChildren().add(dir);
        }

        packagesTree.setRoot(root);
        packagesTree.setShowRoot(false);
    }

    public void addPackageClicked(ActionEvent actionEvent) {

    }

    private void insertPackageIntoDB() {

    }

    private void installPackage(Package pkg) {

    }
}
