package UI;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import models.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.IOException;

import static java.lang.Math.toIntExact;

class LoginException extends Exception {
    public enum Kind {
        WRONG_LOGIN, WRONG_PASSWORD
    }

    private Kind kind;

    LoginException(Kind kind) {
        this.kind = kind;
    }

    Kind getKind() {
        return kind;
    }
}

public class LoginFormController {
    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public Button logInButton;

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

    private User logIn() throws Exception {
        String url = "http://localhost:8080/login";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        String urlParameters = "name=" + loginField.getText() + "&password=" + passwordField.getText();
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
        if (!((String) jsonObject.get("error")).isEmpty()) {
            switch ((Integer.parseInt((String) jsonObject.get("error")))) {
                case -1:
                    throw new LoginException(LoginException.Kind.WRONG_LOGIN);
                case -2:
                    throw new LoginException(LoginException.Kind.WRONG_PASSWORD);
            }
        }

        assert !(jsonObject.containsKey("id") && jsonObject.containsKey("name"));
        return new User(toIntExact(((Long) jsonObject.get("id"))), (String)jsonObject.get("name"));
    }

    public void logInClicked() {
        System.out.println("Logging into " + loginField.getText());

        User response = null;
        int id = -1;
        try {
            response = logIn();
            id = response.getID();
        } catch (LoginException le) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Wrong login data");
            switch (le.getKind()) {
                case WRONG_LOGIN:
                    alert.setHeaderText("Wrong login");
                    break;
                case WRONG_PASSWORD:
                    alert.setHeaderText("Wrong password");
                    break;
            }
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (id != -1) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MainWindowController controller = loader.getController();
            response.setPassword(passwordField.getText());
            controller.setUser(response);

            assert root != null;
            Scene scene = new Scene(root);

            Stage stage = (Stage) logInButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
}
