package UI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import models.File;
import models.User;
import services.FileService;

import java.sql.Timestamp;

public class MainWindowController {

    @FXML
    private Label usernameLabel;

    private User user;

    void setUser(User user) {
        this.user = user;

        FileService fileService = new FileService(user.getID(), user.getPassword());
        File file = new File("index.html", "/home/nick", "<html></html>".getBytes(), Timestamp.valueOf("2002-05-21 21:42:33"));
        fileService.save(file);

        usernameLabel.setText("Files of " + user.getName());
    }
}
