package utils

import models.Repository
import java.io.File
import java.nio.file.Files
import java.util.*

object PackageManagerUtil {
    val packageManagerName: String
        get() = "zypper"

    // zypper only now :(
    val reposList: List<Repository>
        @Throws(Exception::class)
        get() {
            val result = ArrayList<Repository>()
            when (packageManagerName) {
                "zypper" -> {
                    val reposDir = File("/etc/zypp/repos.d")
                    if (reposDir.exists()) {
                        if (reposDir.isDirectory) {
                            for (repo in reposDir.listFiles()) {
                                val tempRepo = Repository(0)
                                tempRepo.manager = "zypper"
                                if (repo.isFile) {
                                    Files.lines(repo.toPath()).use { stream ->
                                        stream.forEach { line ->
                                            if (line.contains("name")) {
                                                tempRepo.name = line.substring(line.lastIndexOf('=') + 1).trim { it <= ' ' }
                                            }
                                            if (line.contains("baseurl")) {
                                                tempRepo.url = line.substring(line.lastIndexOf('=') + 1).trim { it <= ' ' }
                                            }
                                        }
                                    }

                                    if (!tempRepo.name.isEmpty() && !tempRepo.url.isEmpty()) {
                                        result.add(tempRepo)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> throw Exception("unsupported package manager")
            }
            return result
        }
}
