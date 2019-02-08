package UI;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class AddFileDialogController {
    public AnchorPane rootAP;
    public TextField filePathInput;
    public Button OKButton;
    public Button CancelButton;

    public void selectButtonClicked() {
        Stage stage = (Stage) rootAP.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file or directory");
        String files = fileChooser.showOpenMultipleDialog(stage).toString();

        filePathInput.setText(files.substring(1, files.length()-1));

    }

    public void OKButtonClicked() {
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }

    public void CancelButtonClicked() {
        filePathInput.setText("");
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }
}
