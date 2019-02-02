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
    private Timestamp time;

    public File() {

    }

    public File(String name, String path, byte[] content, Timestamp time) {
        this.name =  name;
        this.path = path;
        this.content = content;
        this.time = time;
    }

    @Override
    public String toString() {
        return "models.File" +
                "id=" + id +
                ", name='" + name + "\'" +
                ", path='" + path + "\'" +
                ", content_len = '" + content.length + "\'" +
                ", time='" + time.toString() + "/'" +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Timestamp getTime() {
        return time;
    }
}
