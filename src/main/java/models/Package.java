package models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private Repository repository;

    @OneToMany(mappedBy = "_package", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files;

    public Package() {

    }

    public Package(String name, Repository repository) {
        this.name = name;
        this.repository = repository;
        this.files = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Repository getRepository() {
        return repository;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}
