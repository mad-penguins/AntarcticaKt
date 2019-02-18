package dao

abstract class Dao<Model> {

    abstract val all: List<Model>?

    abstract fun findById(id: Int): Model?

    abstract fun save(obj: Model)

    abstract fun update(obj: Model)

    abstract fun delete(obj: Model)

}
