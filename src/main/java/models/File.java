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

    public File() {

    }

    public File(String name, String path) {
        this.name = name;
        this.path = path;
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
        return "models.File" +
                "id=" + id +
                ", name='" + name + "\'" +
                ", path='" + path + "\'" +
                ", content_len = '" + content.length + "\'" +
                ", created='" + created.toString() + "/'" +
                ", modified='" + modified.toString() + "/'" +
                '}';
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
}
