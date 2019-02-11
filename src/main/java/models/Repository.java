package models;

import javax.persistence.*;
import java.util.List;

@Entity
@Table (name = "repositories")
public class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String url;
    private String manager;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Package> packages;

    public Repository () {

    }

    public Repository(String name, String url, String manager) {
        this.name = name;
        this.url = url;
        this.manager = manager;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getManager() {
        return manager;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    @Override
    public String toString() {
        return "models.User" +
                "id=" + id +
                ", name='" + name + "\'" +
                ", url='" + url + "\'" +
                ", package manager='" + manager + "\'" +
                '}';
    }
}
