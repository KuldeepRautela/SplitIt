package models

data class Response(val responseCode: Int, val msg: String, val data: Any? = null)
