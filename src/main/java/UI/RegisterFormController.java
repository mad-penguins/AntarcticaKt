package UI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

class RegisterException extends Exception {
    public enum Kind {
        ALREADY_REGISTERED
    }

    private Kind kind;

    RegisterException(Kind kind) {
        this.kind = kind;
    }

    Kind getKind() {
        return kind;
    }
}

public class RegisterFormController {

    public TextField loginField;
    public TextField nameField;
    public PasswordField passwordField;
    public AnchorPane rootAP;

    @FXML
    public void initialize() {
        loginField.textProperty().addListener(
                (observable, old_value, new_value) -> {
                    if(new_value.contains(" ")) {
                        loginField.setText(old_value);
                    }
                }
        );

        passwordField.textProperty().addListener(
                (observable, old_value, new_value) -> {
                    if(new_value.contains(" ")) {
                        passwordField.setText(old_value);
                    }
                }
        );
    }

    public void registerClicked() {
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }

    public void cancelClicked() {
        loginField.setText("");
        Stage stage = (Stage) rootAP.getScene().getWindow();
        stage.close();
    }
}
