package exceptions

class LoginException(val kind: Kind) : Exception() {
    enum class Kind {
        WRONG_LOGIN, WRONG_PASSWORD
    }
}
