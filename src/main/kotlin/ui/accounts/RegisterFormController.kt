package ui.accounts

import javafx.fxml.FXML
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage


class RegisterFormController {

    lateinit var loginField: TextField
    lateinit var nameField: TextField
    lateinit var passwordField: PasswordField
    lateinit var rootAP: AnchorPane

    @FXML
    fun initialize() {
        loginField.textProperty().addListener { observable, old_value, new_value ->
            if (new_value.contains(" ")) {
                loginField.text = old_value
            }
        }

        passwordField.textProperty().addListener { observable, old_value, new_value ->
            if (new_value.contains(" ")) {
                passwordField.text = old_value
            }
        }
    }

    fun registerClicked() {
        val stage = rootAP.scene.window as Stage
        stage.close()
    }

    fun cancelClicked() {
        loginField.text = ""
        val stage = rootAP.scene.window as Stage
        stage.close()
    }
}
