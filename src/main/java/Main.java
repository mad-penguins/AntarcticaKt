import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.*;

public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/loginForm.fxml"));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
        stage.setTitle("Antarctica");
    }
}
