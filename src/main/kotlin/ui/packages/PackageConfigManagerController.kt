package ui.packages

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import javafx.util.Callback
import models.File
import models.Package
import models.User
import services.FileService
import services.PackageService
import utils.FileUtil
import utils.UIUtil


class PackageConfigManagerController {
    lateinit var rootAP: AnchorPane
    lateinit var label: Label
    lateinit var configsList: ListView<File>
    lateinit var addConfigButton: Button
    lateinit var okButton: Button
    lateinit var pkg: Package
    lateinit var fileService: FileService
    lateinit var pkgService: PackageService

    fun setPackage(pkg: Package, user: User) {
        this.pkg = pkg
        this.fileService = FileService(user.id, user.password)
        this.pkgService = PackageService(user.id, user.password)

        label.text = "Manage configs of ${pkg.name}"

        updateList()

        configsList.cellFactory = object : Callback<ListView<File>, ListCell<File>> {
            override fun call(param: ListView<File>): ListCell<File> {
                return object : ListCell<File>() {
                    public override fun updateItem(obj: File?, empty: Boolean) {
                        super.updateItem(obj, empty)
                        if (empty) {
                            text = null
                            graphic = null
                        } else {
                            text = "${obj?.path}/${obj?.name}"

                            val layout = HBox()
                            layout.spacing = 5.0

                            val remove = Button("X")
                            val edit = Button("Edit")
                            val buttons = mutableListOf(remove, edit)

                            edit.onAction = EventHandler<ActionEvent> {
                                fileService.delete(obj!!)
                                UIUtil.addFile(fileService.reload(), pkg, text)
                                updateList()
                            }
                            remove.onAction = EventHandler<ActionEvent> {
                                fileService.delete(obj!!)
                                updateList()
                            }

                            if (!FileUtil.fileIsDownloaded("${obj!!.path}/${obj.name}")) {
                                val download = Button("Download")
                                buttons.add(download)
                                download.onAction = EventHandler<ActionEvent> {
                                    FileUtil.downloadFile(obj)
                                    updateList()
                                }
                            } else {
                                val delete = Button("Delete")
                                buttons.add(delete)
                                delete.onAction = EventHandler<ActionEvent> {
                                    FileUtil.deleteFile(obj)
                                    updateList()
                                }
                            }

                            layout.children.addAll(buttons)
                            graphic = layout
                        }
                    }
                }
            }
        }
    }

    private fun updateList() {
        val response = pkgService.reload().find(pkg.id)
        if (response == null) {
            val stage = rootAP.scene.window as Stage
            stage.close()
            return
        }
        if (pkg == response) {
            return
        }

        pkg = response

        configsList.items.clear()
        val configs = pkg.files ?: mutableListOf()
        for (config in configs) {
            configsList.items.add(config)
        }
    }

    fun onAddClicked() {
        UIUtil.addFile(fileService.reload(), pkg)
        updateList()
    }

    fun okClicked(event: ActionEvent) {
        val stage = rootAP.scene.window as Stage
        stage.close()
    }
}