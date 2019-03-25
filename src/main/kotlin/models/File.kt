package models

import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "files")
class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    lateinit var name: String
    var path: String? = null
    var content: ByteArray? = null

    @Basic
    var created: Timestamp? = null
    @Basic
    var modified: Timestamp? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    var `package`: Package? = null

    constructor() {

    }

    constructor(id: Int, name: String, path: String, content: ByteArray) {
        this.id = id
        this.name = name
        this.path = path
        this.content = content
    }

    constructor(name: String, path: String, content: ByteArray, created: Timestamp, modified: Timestamp) {
        this.name = name
        this.path = path
        this.content = content
        this.created = created
        this.modified = modified
    }

    override fun toString(): String {
        return "$path/$name"
    }
}
