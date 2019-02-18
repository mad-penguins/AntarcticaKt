package ui

import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage


class AddFileDialogController {
    lateinit var rootAP: AnchorPane
    lateinit var filePathInput: TextField
    lateinit var okButton: Button

    fun selectButtonClicked() {
        val stage = rootAP.scene.window as Stage

        val fileChooser = FileChooser()
        fileChooser.title = "Select file or directory"
        val files = fileChooser.showOpenMultipleDialog(stage).toString()

        filePathInput.text = files.substring(1, files.length - 1)
    }

    fun okButtonClicked() {
        val stage = rootAP.scene.window as Stage
        stage.close()
    }

    fun cancelButtonClicked() {
        filePathInput.text = ""
        val stage = rootAP.scene.window as Stage
        stage.close()
    }
}
