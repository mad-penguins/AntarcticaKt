package utils

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.stage.Modality
import javafx.stage.Stage
import models.Package
import services.FileService
import ui.files.AddFileDialogController

object UIUtil {
    fun showInvalidPathAlert(path: String) {
        if (!path.isEmpty()) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Wrong file"
            alert.headerText = "Wrong file"
            alert.contentText = "File $path does not exist!"
            alert.showAndWait()
        }
    }

    fun addFile(service: FileService, pkg: Package = Package.default(), defaultText: String = "") {
        val loader = FXMLLoader(javaClass.getResource("/AddFileDialog.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<AddFileDialogController>()
        controller.filePathInput.text = defaultText

        val stage = Stage()
        stage.scene = Scene(root)
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.isResizable = false
        stage.showAndWait()

        val filesList = controller.filePathInput.text
        if (filesList.isEmpty()) {
            return
        }
        val filesPath = filesList.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (_path in filesPath) {
            val path = _path.trim { it <= ' ' }
            val tempFile = java.io.File(path)
            if (!tempFile.exists()) {
                UIUtil.showInvalidPathAlert(path)
                return
            }

            if (tempFile.isFile) {
                FileUtil.insertFileIntoDB(tempFile, path, pkg, service)
            }

            if (tempFile.isDirectory) {
                FileUtil.insertCatalogIntoDB(tempFile, pkg, service)
            }
        }
    }
}