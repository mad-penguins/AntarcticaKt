package UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Repository;
import utils.PackageManagerUtil;


public class AddPackageDialogController {

    public AnchorPane rootAP;
    public TextField nameInput;
    public ChoiceBox<Repository> repositoryInput;
    public TextField configsPathInput;
    public Button selectPathButton;
    public Button OKButton;
    public Button CancelButton;

    @FXML
    public void initialize() {
        repositoryInput.getItems().add(Repository.Default());
        repositoryInput.getSelectionModel().selectFirst();
        try {
            for (Repository rep : PackageManagerUtil.getReposList()) {
                repositoryInput.getItems().add(rep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void selectButtonClicked() {
        Stage stage = (Stage) rootAP.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file or directory");
        String files = fileChooser.showOpenMultipleDialog(stage).toString();

        configsPathInput.setText(files.substring(1, files.length()-1));

    }

    public void OKButtonClicked() {
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }

    public void CancelButtonClicked() {
        nameInput.setText("");
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }
}
