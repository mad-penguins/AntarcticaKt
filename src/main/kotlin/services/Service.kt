package services

abstract class Service<Model>(protected var userID: Int, protected var password: String) {

    abstract fun reload(): Service<Model>

    abstract fun find(id: Int): Model?

    abstract fun save(obj: Model)

    abstract fun update(obj: Model)

    abstract fun delete(obj: Model)

    abstract fun getAll(): List<Model>?

}
