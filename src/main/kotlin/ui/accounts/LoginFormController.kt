package ui.accounts

import exceptions.LoginException
import exceptions.RegisterException
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.Stage
import khttp.post
import models.User
import ui.MainWindowController
import java.io.IOException

class LoginFormController {

    lateinit var loginField: TextField
    lateinit var passwordField: PasswordField
    lateinit var logInButton: Button

    @FXML
    fun initialize() {
        loginField.textProperty().addListener { _, old_value, new_value ->
            if (new_value.contains(" ")) {
                loginField.text = old_value
            }
        }

        passwordField.textProperty().addListener { _, old_value, new_value ->
            if (new_value.contains(" ")) {
                passwordField.text = old_value
            }
        }
    }

    @Throws(LoginException::class)
    private fun logIn(): User {
        val resp = post(
                "https://127.0.0.1:8081/login",
                data = mapOf(
                        "login" to loginField.text.trim { it <= ' ' },
                        "password" to passwordField.text.trim { it <= ' ' }
                )
        ) // TODO: fix checking of certificate or edit certificate

        val jsonObject = resp.jsonObject
        if (!(jsonObject["error"] as String).isEmpty()) {
            when (Integer.parseInt(jsonObject["error"] as String)) {
                -1 -> throw LoginException(LoginException.Kind.WRONG_LOGIN)
                -2 -> throw LoginException(LoginException.Kind.WRONG_PASSWORD)
            }
        }

        if (!(jsonObject.has("id") && jsonObject.has("name"))) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Server error"
            alert.headerText = "Server error"
            alert.contentText = "The server sent wrong response and I can't login properly. :(" +
                    "\nKeep calm and try to update me.\n" +
                    "Error code is 10."
            alert.showAndWait()
            assert(true)
        }
        return User(jsonObject["id"] as Int, jsonObject["login"] as String, jsonObject["name"] as String)
    }

    fun logInClicked() {
        println("Logging into " + loginField.text)

        var response: User? = null
        var id = -1
        try {
            response = logIn()
            id = response.id
        } catch (le: LoginException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Wrong login data"
            when (le.kind) {
                LoginException.Kind.WRONG_LOGIN -> {
                    alert.headerText = "Wrong login"
                    alert.contentText = "Please check your login."
                }
                LoginException.Kind.WRONG_PASSWORD -> {
                    alert.headerText = "Wrong password"
                    alert.contentText = "Please check your password."
                }
            }
            alert.showAndWait()
        } catch (e: Exception) {
            when (e) {
                is java.net.ConnectException, is java.net.SocketException -> {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Connection error"
                    alert.headerText = "Connection error"
                    alert.contentText = "There's a problem with your connection.\nPlease check your internet connection."
                    alert.showAndWait()
                } else -> {
                    println("Some unknown unhandled exception:")
                    e.printStackTrace()
                }
            }
        }

        if (id != -1) {
            val loader = FXMLLoader(javaClass.getResource("/MainWindow.fxml"))
            var root: Parent? = null
            try {
                root = loader.load<Parent>()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val controller = loader.getController<MainWindowController>()
            response!!.password = passwordField.text
            controller.setUser(response)

            assert(root != null)
            val scene = Scene(root!!)

            val stage = logInButton.scene.window as Stage
            stage.scene = scene
            stage.show()
        }
    }

    @Throws(RegisterException::class)
    private fun register(login: String, name: String, password: String) {
        val resp = post(
                "https://localhost:8081/register",
                data = mapOf(
                        "login" to login.trim { it <= ' ' },
                        "name" to name.trim { it <= ' ' },
                        "password" to password.trim { it <= ' ' }
                )
        ) // TODO: fix checking of certificate or edit certificate

        val jsonObject = resp.jsonObject
        if (!(jsonObject["error"] as String).isEmpty()) {
            if (Integer.parseInt(jsonObject["error"] as String) == -1) {
                throw RegisterException()
            }
        }
    }


    @Throws(Exception::class)
    fun registerClicked() {
        val loader = FXMLLoader(javaClass.getResource("/RegisterForm.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<RegisterFormController>()

        val stage = Stage()
        stage.scene = Scene(root)
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.isResizable = false
        stage.showAndWait()

        val login = controller.loginField.text
        val name = controller.nameField.text
        val password = controller.passwordField.text
        if (login.isEmpty() || name.isEmpty() || password.isEmpty()) {
            return
        }

        try {
            register(login, name, password)
        } catch (re: RegisterException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error while registration"
            if (re.kind == RegisterException.Kind.ALREADY_REGISTERED) {
                alert.headerText = "User $name is already registered!"
            }
            alert.showAndWait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
