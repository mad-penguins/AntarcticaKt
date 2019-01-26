package UI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import models.File;
import models.User;
import services.FileService;
import services.UserService;

import java.sql.Timestamp;

public class MainWindowController {
    private int userID;

    @FXML
    private Label usernameLabel;

    private String password;

    void setUserID(int userID, String password) {
        this.userID = userID;
        this.password = password;

        UserService userService = new UserService();
        User user = userService.find(this.userID);

        FileService fileService = new FileService(userID, password);
        File file = new File("index.html", "/home/nick", "<html></html>".getBytes(), Timestamp.valueOf("2002-05-21 21:42:33"));
        fileService.save(file);

        usernameLabel.setText("Files of " + user.getName());
    }
}
