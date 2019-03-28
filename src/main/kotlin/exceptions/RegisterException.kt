package exceptions

class RegisterException : Exception() {

    val kind: Kind

    enum class Kind {
        ALREADY_REGISTERED
    }

    init {
        this.kind = Kind.ALREADY_REGISTERED
    }
}