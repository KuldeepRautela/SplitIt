package models

data class User(val username: String, val password: String) {
    fun validate() = !(username.length < 5 || password.length < 5)
}