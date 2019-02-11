package models;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table (name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String path;
    private byte[] content;

    @Basic
    private Timestamp created;
    @Basic
    private Timestamp modified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private Package _package;

    public File() {

    }

    public File(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public void setModified(Timestamp modified) {
        this.modified = modified;
    }

    public void setPackage(Package _package) {
        this._package = _package;
    }

    public File(String name, String path, byte[] content, Timestamp created, Timestamp modified) {
        this.name =  name;
        this.path = path;
        this.content = content;
        this.created = created;
        this.modified = modified;
    }

    @Override
    public String toString() {
        return path + "/" + name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Timestamp getCreated() {
        return created;
    }

    public byte[] getContent() {
        return content;
    }

    public Timestamp getModified() {
        return modified;
    }

    public Package getPackage() {
        return _package;
    }
}
