interface URLConverter {
    fun getURLParameters(savedURL: String, accessedURL: String): MutableMap<String, String>
}