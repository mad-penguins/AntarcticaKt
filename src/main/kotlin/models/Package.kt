package models

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "packages")
class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    var name: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    var repository: Repository? = null

    @OneToMany(mappedBy = "package", cascade = [CascadeType.ALL], orphanRemoval = true)
    var files: MutableList<File>? = null

    constructor() {

    }

    constructor(name: String, repository: Repository) {
        this.name = name
        this.repository = repository
        this.files = ArrayList()
    }

    constructor(id: Int) {
        this.id = id
    }

    companion object {
        fun default(): Package {
            val temp = Package(1)
            temp.name = ""
            temp.repository = Repository.empty()
            return temp
        }
    }
}
