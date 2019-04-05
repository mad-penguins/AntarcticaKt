package exceptions

class RegistrationException(val kind: Kind) : Exception() {
    enum class Kind {
        CONNECTION_ERROR, ALREADY_REGISTERED
    }
}