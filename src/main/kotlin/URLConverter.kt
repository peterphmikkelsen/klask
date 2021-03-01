interface URLConverter {
    fun getURLQueries(queryString: String): MutableMap<String, String>
    fun getURLParameters(savedURL: String, accessedURL: String): MutableMap<String, String>
}