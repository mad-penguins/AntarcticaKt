package ui

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import models.Repository
import utils.PackageUtil


class AddPackageDialogController {

    lateinit var rootAP: AnchorPane
    lateinit var nameInput: TextField
    lateinit var repositoryInput: ChoiceBox<Repository>
    lateinit var configsPathInput: TextField
    lateinit var selectPathButton: Button
    lateinit var okButton: Button

    @FXML
    fun initialize() {
        repositoryInput.items.add(Repository.default())
        repositoryInput.selectionModel.selectFirst()
        try {
            for (rep in PackageUtil.reposList) {
                repositoryInput.items.add(rep)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun selectButtonClicked() {
        val stage = rootAP.scene.window as Stage

        val fileChooser = FileChooser()
        fileChooser.title = "Select file or directory"
        val files = fileChooser.showOpenMultipleDialog(stage).toString()

        configsPathInput.text = files.substring(1, files.length - 1)
    }

    fun okButtonClicked() {
        val stage = rootAP.scene.window as Stage
        stage.close()
    }

    fun cancelButtonClicked() {
        nameInput.text = ""
        val stage = rootAP.scene.window as Stage
        stage.close()
    }
}
