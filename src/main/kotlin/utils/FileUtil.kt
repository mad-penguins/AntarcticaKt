package utils

import models.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime

object FileUtil {

    fun fileIsDownloaded(path: String): Boolean {
        return java.io.File(path).exists()
    }

    @Throws(IOException::class)
    fun downloadFile(obj: File) {
        val path = obj.path + '/'.toString() + obj.name
        val file = java.io.File(path)
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
        val path = obj.path + '/'.toString() + obj.name
        val file = java.io.File(path)
        if (file.delete()) {
            println("deleted succesfully")
            return
        }
        print("delete failed")
    }
}