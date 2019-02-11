package utils;

import models.Repository;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PackageManagerUtil {
    public static String getPackageManagerName() {
        return "zypper";
    }

    public static List<Repository> getReposList() throws Exception {
        List<Repository> result = new ArrayList<>();
        switch (getPackageManagerName()) {
            case "zypper": {
                File repos_dir = new File("/etc/zypp/repos.d"); // zypper only now :(
                if (repos_dir.exists()) {
                    if (repos_dir.isDirectory()) {
                        for (File repo : Objects.requireNonNull(repos_dir.listFiles())) {
                            Repository _repo = new Repository();
                            _repo.setManager("zypper");
                            if (repo.isFile()) {
                                try (Stream<String> stream = Files.lines(repo.toPath())) {
                                    stream.forEach(line -> {
                                        if (line.contains("name")) {
                                            _repo.setName(line.substring(line.lastIndexOf('=')+1).trim());
                                        }
                                        if (line.contains("baseurl")) {
                                            _repo.setUrl(line.substring(line.lastIndexOf('=')+1).trim());
                                        }
                                    });
                                }

                                if (!_repo.getName().isEmpty() && !_repo.getUrl().isEmpty()) {
                                    result.add(_repo);
                                }
                            }
                        }
                    }
                }
                break;
            }
            default:
                throw new Exception("unsupported package manager");
        }
        return result;
    }
}
