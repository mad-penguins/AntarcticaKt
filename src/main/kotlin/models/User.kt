package models

import javax.persistence.*

@Entity
@Table(name = "users")
class User(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int,
        var login: String,
        var name: String
) {

    lateinit var password: String

    override fun toString(): String {
        return "models.User" +
                "id=" + id +
                ", name='" + name + "\'" +
                '}'.toString()
    }
}
