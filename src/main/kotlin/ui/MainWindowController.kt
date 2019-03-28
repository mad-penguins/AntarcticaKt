package ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.CheckBoxTreeTableCell
import javafx.scene.layout.AnchorPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import models.File
import models.Package
import models.Repository
import models.User
import services.FileService
import services.PackageService
import services.RepositoryService
import ui.packages.AddPackageDialogController
import ui.packages.PackageConfigManagerController
import utils.FileUtil
import utils.UIUtil
import java.io.IOException
import java.sql.Timestamp
import java.util.*


class MainWindowController {

    private lateinit var user: User

    lateinit var rootAP: AnchorPane
    lateinit var usernameLabel: Label

    internal fun setUser(user: User) {
        this.user = user
        usernameLabel.text = "Files of " + user.name

        updateFilesTable()
        updatePackagesTable()
    }


    // files logic

    lateinit var addFileButton: Button
    lateinit var filesTree: TreeTableView<FileRow>
    lateinit var fileNameColumn: TreeTableColumn<FileRow, String>
    lateinit var createdColumn: TreeTableColumn<FileRow, String>
    lateinit var modifiedColumn: TreeTableColumn<FileRow, String>
    lateinit var fileDownloadedColumn: TreeTableColumn<FileRow, Boolean>

    inner class FileRow(var id: List<Int>, name: String, created: Timestamp, lastModified: Timestamp, downloaded: Boolean) {
        var nameProperty = SimpleStringProperty(name)
        var createdProperty = SimpleStringProperty(
                if (created.equals(Timestamp(0)))
                    ""
                else
                    created.toString()
        )
        var lastModifiedProperty = SimpleStringProperty(
                if (lastModified.equals(Timestamp(0)))
                    ""
                else
                    lastModified.toString()
        )
        var downloadedProperty = SimpleBooleanProperty(downloaded)
    }

    private fun updateFilesTable() {
        val fileService = FileService(user.id, user.password)
        val userFiles = fileService.getAll()

        val root = TreeItem(FileRow(listOf(-1), "n", Timestamp(0), Timestamp(0), true)) // TODO: check all the files in this directory

        fileNameColumn.cellFactory = Callback<TreeTableColumn<FileRow, String>, TreeTableCell<FileRow, String>> {
            object : TreeTableCell<FileRow, String>() {
                var btn = Button("X")

                public override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty) {
                        graphic = null
                        text = null
                    } else {
                        btn.setOnAction {
                            for (id in treeTableRow.item.id) {
                                fileService.delete(fileService.find(id)!!)
                            }
                            updateFilesTable()
                            updatePackagesTable()
                        }
                        if (treeTableRow.item != null) {
                            graphic = btn
                            text = treeTableRow.item.nameProperty.get()
                        } else {
                            graphic = null
                            text = null
                        }
                    }
                }
            }
        }

        fileNameColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<FileRow, String> ->
            param.value.value.nameProperty
        }
        createdColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<FileRow, String> ->
            param.value.value.createdProperty
        }
        modifiedColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<FileRow, String> ->
            param.value.value.lastModifiedProperty
        }

        fileDownloadedColumn.cellValueFactory = Callback<TreeTableColumn.CellDataFeatures<FileRow, Boolean>, ObservableValue<Boolean>> { param ->
            val file = param.value.value
            val downloadedProperty = SimpleBooleanProperty(file.downloadedProperty.get())
            downloadedProperty.addListener { _, _, newValue ->
                downloadedProperty.set(newValue)
                when (newValue) {
                    true -> {
                        for (id in file.id) {
                            FileUtil.downloadFile(fileService.find(id)!!)
                        }
                    }
                    false -> {
                        for (id in file.id) {
                            FileUtil.deleteFile(fileService.find(id)!!)
                        }
                    }
                }
                updateFilesTable()
            }
            downloadedProperty
        }

        fileDownloadedColumn.cellFactory = Callback<TreeTableColumn<FileRow, Boolean>, TreeTableCell<FileRow, Boolean>> {
            val cell = CheckBoxTreeTableCell<FileRow, Boolean>()
            cell.alignment = Pos.CENTER
            cell.isEditable = true
            cell
        }
        filesTree.isEditable = true

        val filesByDir = HashMap<String, ArrayList<FileRow>>()
        for (userFile in userFiles) {
            val newRow = FileRow(
                    listOf(userFile.id),
                    userFile.name,
                    userFile.created!!,
                    userFile.modified!!,
                    FileUtil.fileIsDownloaded("${userFile.path}/${userFile.name}")
            )
            if (filesByDir.containsKey(userFile.path)) {
                filesByDir[userFile.path]!!.add(newRow)
            } else {
                filesByDir[userFile.path!!] = ArrayList(listOf(newRow))
            }
        }

        for ((key, value) in filesByDir) {
            val children = mutableListOf<TreeItem<FileRow>>()
            val ids = mutableListOf<Int>()
            var dirIsDownloaded = true
            for (_file in value) {
                val row = TreeItem(_file)
                ids.addAll(_file.id)
                children.add(row)
                if (!_file.downloadedProperty.get())
                    dirIsDownloaded = false
            }
            // TODO: add directory created and modified times
            val dir = TreeItem(FileRow(ids, key, Timestamp(0), Timestamp(0), dirIsDownloaded))
            dir.children.addAll(children)
            root.children.add(dir)
        }
        filesTree.root = root
        filesTree.isShowRoot = false
    }

    @Throws(IOException::class)
    fun addFileClicked() {
        UIUtil.addFile(FileService(user.id, user.password))
        updateFilesTable()
    }


    // packages logic

    lateinit var addPackageButton: Button
    lateinit var packagesTree: TreeTableView<PackageRow>
    lateinit var packageNameColumn: TreeTableColumn<PackageRow, String>
    lateinit var repositoryColumn: TreeTableColumn<PackageRow, String>
    lateinit var configsListColumn: TreeTableColumn<PackageRow, String>

    inner class PackageRow(var id: List<Int>, name: String, repository: String, configsList: List<File>, var isRepo: Boolean?) {
        var nameProperty = SimpleStringProperty(name)
        var repositoryProperty = SimpleStringProperty(repository)
        var configsListProperty = SimpleStringProperty(
                if (configsList.isEmpty())
                    ""
                else
                    run {
                        var result = "${configsList[0].path}/${configsList[0].name}"
                        if (configsList.size > 1) {
                            for (config in configsList.drop(1))
                                result += ", ${config.path}/${config.name}"
                        }
                        result
                    }
        )
    }

    private fun updatePackagesTable() {
        val packageService = PackageService(user.id, user.password)
        val userPackages = packageService.getAll()

        val root = TreeItem(PackageRow(listOf(-1), "", "", ArrayList(), true))

        packageNameColumn.cellFactory = Callback<TreeTableColumn<PackageRow, String>, TreeTableCell<PackageRow, String>> {
            object : TreeTableCell<PackageRow, String>() {
                var btn = Button("X")

                public override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty) {
                        graphic = null
                        text = null
                    } else {
                        btn.setOnAction {
                            for (id in treeTableRow.item.id) {
                                packageService.delete(packageService.find(id)!!)
                            }
                            updatePackagesTable()
                            updateFilesTable()
                        }
                        if (treeTableRow.item != null) {
                            graphic = btn
                            text = treeTableRow.item.nameProperty.get()
                        } else {
                            graphic = null
                            text = null
                        }
                    }
                }
            }
        }

        packageNameColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<PackageRow, String> ->
            param.value.value.nameProperty
        }
        repositoryColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<PackageRow, String> ->
            param.value.value.repositoryProperty
        }
        configsListColumn.cellFactory = Callback<TreeTableColumn<PackageRow, String>, TreeTableCell<PackageRow, String>> {
            object : TreeTableCell<PackageRow, String>() {
                var btn = Button("Edit")

                public override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty) {
                        graphic = null
                        text = null
                    } else {
                        btn.setOnAction {
                            manageConfigs(packageService.find(treeTableRow.item.id[0])!!) // there's a guarantee that this row can be only a package
                            updatePackagesTable()
                            updateFilesTable()
                        }
                        if (treeTableRow.item != null) {
                            if (!treeTableRow.item.isRepo!!) {
                                graphic = btn
                                text = treeTableRow.item.configsListProperty.get()
                            }
                        } else {
                            graphic = null
                            text = null
                        }
                    }
                }
            }
        }

        configsListColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<PackageRow, String> ->
            param.value.value.configsListProperty
        }

        val packagesByRepository = HashMap<String, ArrayList<PackageRow>>()
        for (userPackage in userPackages!!) {
            val newRow = PackageRow(
                    listOf(userPackage.id),
                    userPackage.name!!,
                    userPackage.repository!!.name,
                    userPackage.files!!,
                    false
            )
            if (packagesByRepository.containsKey(userPackage.repository!!.name)) {
                packagesByRepository[userPackage.repository!!.name]!!.add(newRow)
            } else {
                packagesByRepository[userPackage.repository!!.name] = ArrayList(listOf(newRow))
            }
        }

        for ((key, value) in packagesByRepository) {
            val children = mutableListOf<TreeItem<PackageRow>>()
            val ids = mutableListOf<Int>()
            for (_package in value) {
                val row = TreeItem(_package)
                children.add(row)
                ids.addAll(_package.id)
            }
            val repo = TreeItem(PackageRow(ids, key, "", ArrayList(), true))
            repo.children.addAll(children)
            root.children.add(repo)
        }

        packagesTree.root = root
        packagesTree.isShowRoot = false
    }

    private fun manageConfigs(pkg: Package) {
        val loader = FXMLLoader(javaClass.getResource("/PackageConfigManager.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<PackageConfigManagerController>()
        controller.setPackage(pkg, user)

        val stage = Stage()
        stage.scene = Scene(root)
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.isResizable = false
        stage.showAndWait()
    }

    @Throws(Exception::class)
    fun addPackageClicked() {
        val repositoryService = RepositoryService(user.id, user.password)
        if (repositoryService.find(2).manager == "no_manager") { // on fresh installation
            repositoryService.update(Repository.default())
        }

        val loader = FXMLLoader(javaClass.getResource("/AddPackageDialog.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<AddPackageDialogController>()

        val stage = Stage()
        stage.scene = Scene(root)
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.isResizable = false
        stage.showAndWait()

        if (controller.nameInput.text.isEmpty()) {
            return
        }

        val configs = ArrayList<File>()
        val filesList = controller.configsPathInput.text
        if (!filesList.isEmpty()) {
            val filesPath = filesList.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (_path in filesPath) {
                val path = _path.trim { it <= ' ' }
                val tempFile = java.io.File(path)
                if (!tempFile.exists()) {
                    UIUtil.showInvalidPathAlert(path)
                    return
                }

                val filesService = FileService(user.id, user.password)
                val name = path.substring(path.lastIndexOf('/') + 1)
                val location = path.substring(0, path.lastIndexOf('/'))
                val entry = filesService.find(name, location)
                if (entry != null) { // TODO: fix error while adding a package with already added config file
                    configs.add(entry)
                } else {
                    if (tempFile.isFile) {
                        configs.add(FileUtil.insertFileIntoDB(tempFile, path, Package.default(), FileService(user.id, user.password)))
                        updateFilesTable()
                    }

                    if (tempFile.isDirectory) {
                        configs.addAll(FileUtil.insertCatalogIntoDB(tempFile, Package.default(), FileService(user.id, user.password)))
                        updateFilesTable()
                    }
                }
            }
        }

        var repository = repositoryService.find(controller.repositoryInput.value.url)

        if (repository == null) {
            repositoryService.save(controller.repositoryInput.value)
            repository = repositoryService.find(controller.repositoryInput.value.url)
        }

        val pkg = Package(
                controller.nameInput.text.trim { it <= ' ' },
                repository!!
        )
        pkg.files = configs

        val packageService = PackageService(user.id, user.password)
        packageService.save(pkg)
        updatePackagesTable()

        val fileService = FileService(user.id, user.password)
        for (config in configs) {
            config.`package` = pkg
            fileService.update(config)
        }
    }
}
