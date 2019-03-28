package models

import utils.PackageUtil

import javax.persistence.*

@Entity
@Table(name = "repositories")
class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    lateinit var name: String
    lateinit var url: String
    lateinit var manager: String

    @OneToMany(mappedBy = "repository", cascade = [CascadeType.ALL], orphanRemoval = true)
    var packages: List<Package>? = null

    // used for Hibernate init
    constructor() {

    }

    constructor(id: Int) {
        this.id = id
    }

    constructor(id: Int, name: String, url: String, manager: String) {
        this.id = id
        this.name = name
        this.url = url
        this.manager = manager
    }

    constructor(name: String, url: String, manager: String) {
        this.name = name
        this.url = url
        this.manager = manager
    }

    override fun toString(): String = name

    companion object {
        fun default(): Repository {
            return Repository(2, "default", "no_url", PackageUtil.packageManagerName)
        }

        fun empty(): Repository {
            return Repository(1, "", "", "")
        }
    }
}
