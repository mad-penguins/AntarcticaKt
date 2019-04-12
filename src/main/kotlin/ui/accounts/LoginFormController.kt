package ui.accounts

import exceptions.LoginException
import exceptions.RegistrationException
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
import sun.security.validator.ValidatorException
import ui.MainWindowController
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

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
        val postData = mapOf(
                "login" to loginField.text.trim { it <= ' ' },
                "password" to passwordField.text.trim { it <= ' ' }
        )
        val resp = try { // TODO: fix checking of certificate or edit certificate, current workaround is adding of client certificate into JRE keychain
            post("https://165.22.64.118:8081/login", data = postData)
        } catch (e: Exception) {
            val temp = when (e) {
                is SSLHandshakeException, is ValidatorException, is ConnectException, is SocketException -> { // something wrong with server HTTPS certificate
                    val httpResponse = try {
                        println("Couldn't connect through HTTPS, using HTTP")
                        post("http://165.22.64.118:8080/login", data = postData)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    httpResponse
                }
                else -> {
                    e.printStackTrace()
                    null
                }
            }
            temp
        } ?: throw LoginException(LoginException.Kind.CONNECTION_ERROR)

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
            alert.title = "Error while logging in"
            when (le.kind) {
                LoginException.Kind.CONNECTION_ERROR -> {
                    alert.headerText = "Connection error"
                    alert.contentText = "Cannot establish neither HTTPS nor HTTP connection to server.\nTry again later."
                }
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

    @Throws(RegistrationException::class)
    private fun register(login: String, name: String, password: String) {
        val postData = mapOf(
                "login" to login.trim { it <= ' ' },
                "name" to name.trim { it <= ' ' },
                "password" to password.trim { it <= ' ' }
        )

        val resp = try { // TODO: fix checking of certificate or edit certificate, current workaround is adding of client certificate into JRE keychain
            post("https://165.22.64.118:8081/register", data = postData)
        } catch (e: Exception) {
            val temp = when (e) {
                is SSLHandshakeException, is ValidatorException -> { // something wrong with server HTTPS certificate
                    val httpResponse = try {
                        post("http://165.22.64.118:8080/register", data = postData)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    httpResponse
                }
                else -> {
                    e.printStackTrace()
                    null
                }
            }
            temp
        } ?: throw RegistrationException(RegistrationException.Kind.CONNECTION_ERROR)

        val jsonObject = resp.jsonObject
        if (!(jsonObject["error"] as String).isEmpty()) {
            if (Integer.parseInt(jsonObject["error"] as String) == -1)
                throw RegistrationException(RegistrationException.Kind.ALREADY_REGISTERED)
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
        } catch (re: RegistrationException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error while registration"
            when (re.kind) {
                RegistrationException.Kind.CONNECTION_ERROR -> {
                    alert.headerText = "Connection error"
                    alert.contentText = "Cannot establish neither HTTPS nor HTTP connection to server.\nTry again later."
                }
                RegistrationException.Kind.ALREADY_REGISTERED -> {
                    alert.headerText = "Already registered"
                    alert.contentText = "User $name is already registered!"
                }
            }
            alert.showAndWait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
