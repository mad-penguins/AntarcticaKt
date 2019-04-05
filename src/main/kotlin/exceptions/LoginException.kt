package exceptions

class LoginException(val kind: Kind) : Exception() {
    enum class Kind {
        CONNECTION_ERROR, WRONG_LOGIN, WRONG_PASSWORD
    }
}
