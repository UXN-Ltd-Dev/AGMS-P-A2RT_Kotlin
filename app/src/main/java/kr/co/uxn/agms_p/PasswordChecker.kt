package kr.co.uxn.agms_p

object PasswordChecker {

    private val REGEX_LOWERCASE = Regex("[a-z]")
    private val REGEX_UPPERCASE = Regex("[A-Z]")
    private val REGEX_NUMBER = Regex("\\d")

    fun checkPwd(pwd: String): PwdError {

        val hasSpecial = Regex("[₩~!@#$%^&*()\\-_=+?]").containsMatchIn(pwd)
        val allowedRegex = Regex("^[A-Za-z0-9₩~!@#$%^&*()\\-_=+?]+$")

        return when {
            pwd.length !in 8..16 -> PwdError.TOO_SHORT_OR_LONG
            !allowedRegex.matches(pwd) -> PwdError.INVALID_CHAR
            !pwd.contains(Regex("[a-z]")) -> PwdError.NO_LOWERCASE
            !pwd.contains(Regex("[A-Z]")) -> PwdError.NO_UPPERCASE
            !pwd.contains(Regex("[0-9]")) -> PwdError.NO_NUMBER
            !hasSpecial -> PwdError.NO_SPECIAL_CHAR
            else -> PwdError.NO_ERROR
        }
    }

    enum class PwdError {
        TOO_SHORT_OR_LONG,
        INVALID_CHAR,
        NO_UPPERCASE,
        NO_LOWERCASE,
        NO_NUMBER,
        NO_SPECIAL_CHAR,
        NO_ERROR
    }

}