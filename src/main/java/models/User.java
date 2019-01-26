package models;

import javax.persistence.*;
import java.security.*;

@Entity
@Table (name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String password;

    public User() {

    }

    public User(String name, String password) {
        this.name =  name;
        this.password = password;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert md != null;
        this.password = javax.xml.bind.DatatypeConverter.printHexBinary(md.digest(password.getBytes()));
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "models.User" +
                "id=" + id +
                ", name='" + name + "\'" +
                ", password='" + password + "\'" +
                '}';
    }
}
