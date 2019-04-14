package utils

import models.File
import models.Package
import services.FileService
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.sql.Timestamp
import java.util.*

object FileUtil {

    private fun processPath(path: String) : String {
        if (path.contains(System.getProperty("user.home"))) {
            return path.replace(System.getProperty("user.home"), "~")
        } else if (path.contains("~") && path.indexOfFirst {it == '~'} == 0) {
            return path.replace("~", System.getProperty("user.home"))
        }
        return path
    }

    fun fileIsDownloaded(path: String): Boolean {
        return java.io.File(processPath(path)).exists()
    }

    @Throws(IOException::class)
    fun downloadFile(obj: File) {
        val file = java.io.File("${processPath(obj.path!!)}/${obj.name}")
        if (!file.exists()) {
            if (!file.parentFile.exists() && !file.parentFile.mkdirs()) {
                return
            }
            if (!file.createNewFile()) {
                return
            }
        }
        Files.write(file.toPath(), obj.content)

        val attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView::class.java)
        attributes.setTimes(
                FileTime.from(obj.created!!.toInstant()),
                FileTime.from(obj.modified!!.toInstant()),
                FileTime.from(obj.modified!!.toInstant())
        )
    }

    fun deleteFile(obj: File) {
        val file = java.io.File("${processPath(obj.path!!)}/${obj.name}")
        file.delete()
    }

    data class FileTimes(val created: FileTime, val modified: FileTime)

    private fun getFileTimes(file: java.io.File): FileTimes {
        return FileTimes(
                Files.readAttributes(
                        file.toPath(),
                        BasicFileAttributes::class.java
                ).creationTime(),
                Files.readAttributes(
                        file.toPath(),
                        BasicFileAttributes::class.java
                ).lastModifiedTime()
        )
    }

    @Throws(IOException::class)
    fun insertFileIntoDB(dirFile: java.io.File, path: String, pkg: Package, service: FileService): File {
        val name = path.substring(path.lastIndexOf('/') + 1)
        val location = processPath(path.substring(0, path.lastIndexOf('/')))

        val content = Files.readAllBytes(dirFile.toPath())

        val file = File(
                name,
                location,
                content,
                Timestamp.from(FileUtil.getFileTimes(dirFile).created.toInstant()),
                Timestamp.from(FileUtil.getFileTimes(dirFile).modified.toInstant())
        )
        file.`package` = pkg
        service.save(file)

        return file
    }

    @Throws(IOException::class)
    fun insertCatalogIntoDB(catalog: java.io.File, pkg: Package, service: FileService): List<File> {
        val filesInDir = catalog.listFiles()!!

        val files = ArrayList<File>()
        for (dirFile in filesInDir) {
            if (dirFile.isDirectory) {
                files.addAll(insertCatalogIntoDB(dirFile, pkg, service.reload()))
            } else if (dirFile.isFile) {
                val path = dirFile.absolutePath
                files.add(insertFileIntoDB(dirFile, path, pkg, service.reload()))
            }
        }
        return files
    }
}