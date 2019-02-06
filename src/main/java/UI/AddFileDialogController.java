package UI;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class AddFileDialogController {
    public AnchorPane rootAP;
    public TextField filePathInput;
    public Button OKButton;
    public Button CancelButton;

    public void selectButtonClicked(ActionEvent actionEvent) {
        Stage stage = (Stage) rootAP.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file or directory");
        String files = fileChooser.showOpenMultipleDialog(stage).toString();

        filePathInput.setText(files.substring(1, files.length()-1));

    }

    public void OKButtonClicked(ActionEvent actionEvent) {
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }

    public void CancelButtonClicked(ActionEvent actionEvent) {
        filePathInput.setText("");
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }
}
