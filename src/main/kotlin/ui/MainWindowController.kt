package ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
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

import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.sql.Timestamp
import java.util.*
import javafx.beans.value.ObservableValue
import utils.FileUtil
import javafx.geometry.Pos
import javafx.scene.control.cell.CheckBoxTreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableCell
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.DoubleProperty






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

    private fun showInvalidPathAlert(path: String) {
        if (!path.isEmpty()) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Wrong file"
            alert.headerText = "Wrong file"
            alert.contentText = "File $path does not exist!"
            alert.showAndWait()
        }
    }


    // files logic

    lateinit var addFileButton: Button
    lateinit var filesTree: TreeTableView<FileRow>
    lateinit var fileNameColumn: TreeTableColumn<FileRow, String>
    lateinit var createdColumn: TreeTableColumn<FileRow, String>
    lateinit var modifiedColumn: TreeTableColumn<FileRow, String>
    lateinit var deleteFileColumn: TreeTableColumn<FileRow, String>
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
        fileNameColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<FileRow, String> ->
            param.value.value.nameProperty
        }
        createdColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<FileRow, String> ->
            param.value.value.createdProperty
        }
        modifiedColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<FileRow, String> ->
            param.value.value.lastModifiedProperty
        }

        deleteFileColumn.cellFactory = Callback<TreeTableColumn<FileRow, String>, TreeTableCell<FileRow, String>> {
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
                        graphic = btn
                        text = null
                    }
                }
            }
        }

        fileDownloadedColumn.cellValueFactory = Callback<TreeTableColumn.CellDataFeatures<FileRow, Boolean>, ObservableValue<Boolean>> { param ->
            val file = param.value.value
            val downloadedProperty = SimpleBooleanProperty(file.downloadedProperty.get())
            downloadedProperty.addListener {
                observable, oldValue, newValue ->
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
            if (filesByDir.containsKey(userFile.path)) {
                filesByDir[userFile.path]!!.add(
                        FileRow(
                                listOf(userFile.id),
                                userFile.name,
                                userFile.created!!,
                                userFile.modified!!,
                                FileUtil.fileIsDownloaded(userFile.path + "/" + userFile.name)
                        )
                )
            } else {
                filesByDir[userFile.path!!] = ArrayList(
                        listOf(
                                FileRow(
                                        listOf(userFile.id),
                                        userFile.name,
                                        userFile.created!!,
                                        userFile.modified!!,
                                        FileUtil.fileIsDownloaded(userFile.path + "/" + userFile.name)
                                )
                        )
                )
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
            val dir = TreeItem(FileRow(ids, key, Timestamp(0), Timestamp(0), dirIsDownloaded))
            dir.children.addAll(children)
            root.children.add(dir)
        }
        filesTree.root = root
        filesTree.isShowRoot = false
    }

    @Throws(IOException::class)
    fun addFileClicked() {
        val loader = FXMLLoader(javaClass.getResource("/AddFileDialog.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<AddFileDialogController>()

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
                showInvalidPathAlert(path)
                return
            }

            if (tempFile.isFile) {
                insertFileIntoDB(tempFile, path)
                updateFilesTable()
            }

            if (tempFile.isDirectory) {
                insertCatalogIntoDB(tempFile)
                updateFilesTable()
            }
        }
    }

    @Throws(IOException::class)
    private fun insertFileIntoDB(dirFile: java.io.File, path: String): File {
        val name = path.substring(path.lastIndexOf('/') + 1)
        val location = path.substring(0, path.lastIndexOf('/'))

        val content = Files.readAllBytes(dirFile.toPath())

        val packageService = PackageService(user.id, user.password)
        val file = File(
                name,
                location,
                content,
                Timestamp.from(
                        Files.readAttributes(
                                dirFile.toPath(),
                                BasicFileAttributes::class.java
                        ).creationTime().toInstant()
                ),
                Timestamp.from(
                        Files.readAttributes(
                                dirFile.toPath(),
                                BasicFileAttributes::class.java
                        ).lastModifiedTime().toInstant()
                )
        )
        file.`package` = packageService.find(1)

        val fileService = FileService(user.id, user.password)
        fileService.save(file)

        return file
    }

    @Throws(IOException::class)
    private fun insertCatalogIntoDB(catalog: java.io.File): List<File> {
        val filesInDir = catalog.listFiles()!!

        val files = ArrayList<File>()
        for (dirFile in filesInDir) {
            if (dirFile.isDirectory) {
                files.addAll(insertCatalogIntoDB(dirFile))
            } else if (dirFile.isFile) {
                val path = dirFile.absolutePath
                files.add(insertFileIntoDB(dirFile, path))
            }
        }
        return files
    }


    // packages logic

    lateinit var addPackageButton: Button
    lateinit var packagesTree: TreeTableView<PackageRow>
    lateinit var packageNameColumn: TreeTableColumn<PackageRow, String>
    lateinit var repositoryColumn: TreeTableColumn<PackageRow, String>
    lateinit var configsListColumn: TreeTableColumn<PackageRow, String>
    lateinit var deletePackageColumn: TreeTableColumn<PackageRow, String>

    inner class PackageRow(var id: List<Int>, name: String, repository: String, configsList: List<File>) {
        var nameProperty = SimpleStringProperty(name)
        var repositoryProperty = SimpleStringProperty(repository)
        var lastConfigsListProperty = SimpleStringProperty(
                if (configsList.isEmpty())
                    ""
                else
                    configsList.toString()
        )
    }

    private fun updatePackagesTable() {
        val packageService = PackageService(user.id, user.password)
        val userPackages = packageService.getAll()

        val root = TreeItem(PackageRow(listOf(-1), "", "", ArrayList()))
        packageNameColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<PackageRow, String> ->
            param.value.value.nameProperty
        }
        repositoryColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<PackageRow, String> ->
            param.value.value.repositoryProperty
        }
        configsListColumn.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<PackageRow, String> ->
            param.value.value.lastConfigsListProperty
        }

        deletePackageColumn.cellFactory = Callback<TreeTableColumn<PackageRow, String>, TreeTableCell<PackageRow, String>> {
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
                        graphic = btn
                        text = null
                    }
                }
            }
        }

        val packagesByRepository = HashMap<String, ArrayList<PackageRow>>()
        for (userPackage in userPackages!!) {
            if (packagesByRepository.containsKey(userPackage.repository!!.name)) {
                packagesByRepository[userPackage.repository!!.name]!!.add(
                        PackageRow(
                                listOf(userPackage.id),
                                userPackage.name!!,
                                userPackage.repository!!.name,
                                userPackage.files!!
                        )
                )
            } else {
                packagesByRepository[userPackage.repository!!.name] = ArrayList(
                        listOf(
                                PackageRow(
                                        listOf(userPackage.id),
                                        userPackage.name!!,
                                        userPackage.repository!!.name,
                                        userPackage.files!!
                                )
                        )
                )
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
            val dir = TreeItem(PackageRow(ids, key, "", ArrayList()))
            dir.children.addAll(children)
            root.children.add(dir)
        }

        packagesTree.root = root
        packagesTree.isShowRoot = false
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
                    showInvalidPathAlert(path)
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
                        configs.add(insertFileIntoDB(tempFile, path))
                        updateFilesTable()
                    }

                    if (tempFile.isDirectory) {
                        configs.addAll(insertCatalogIntoDB(tempFile))
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
