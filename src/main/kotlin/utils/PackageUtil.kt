package utils

import models.Package
import models.Repository
import java.io.File
import java.nio.file.Files
import java.util.*

object PackageUtil {
    val packageManagerName: String
        get() {
            val proc = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", "cat /etc/*-release"))
            val reader = proc.inputStream.bufferedReader()

            for (line in reader.lines()) {
                val regex = Regex("ID_LIKE=(\\\")?([a-zA-Z0-9_ ]*)(\\\")?")
                if (regex.matches(line!!)) {
                    val result = regex.matchEntire(line!!)!!.value.decapitalize()
                    return when {
                        result.contains("suse") -> {
                            println("Running SUSE")
                            "zypper"
                        }
                        result.contains("debian") -> {
                            println("running Debian")
                            "apt"
                        }
                        else -> throw Exception("Unknown distro")
                    }
                }
            }
            throw Exception("Unknown distro")
        }

    private fun getZypperReposList() : ArrayList<Repository> {
        val result = ArrayList<Repository>()
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
        return result
    }

    // TODO
    private fun getAptReposList() : ArrayList<Repository> {
        val result = ArrayList<Repository>()
        val reposFile = File("/etc/apt/sources.list")
        if (reposFile.exists()) {
            Files.lines(reposFile.toPath()).use { stream ->
                stream.forEach { line ->
                    // regexp for "deb url repo-version name"
                    val repoRegexp
                            = Regex("^(deb|deb-src) (https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,}) ([a-zA-Z]\\w*) ([a-zA-Z]\\w*)$")
                    if (repoRegexp.matches(line)) {
                        print(repoRegexp.matchEntire(line)!!.toString())
                        val matchGroups = repoRegexp.matchEntire(line)!!.groupValues
                        result.add(Repository(0, name = matchGroups[4], url = matchGroups[2], manager = "apt"))
                    }
                }
            }
        }

        return result
    }

    val reposList: List<Repository> // TODO: add support of dnf and pacman support
        @Throws(Exception::class)
        get() {
            return when (packageManagerName) {
                "zypper" -> getZypperReposList()
                "apt" -> getAptReposList()
                else -> throw Exception("unsupported package manager")
            }
        }

    // TODO
    fun installPackage(pkg: Package) {

    }
}
