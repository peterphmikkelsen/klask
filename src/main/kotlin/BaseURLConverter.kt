class BaseURLConverter: URLConverter {

   override fun getURLParameters(savedURL: String, accessedURL: String): MutableMap<String, String> {
        val params = mutableMapOf<String, String>()
        val paramKeys = savedURL.split("/")
        val paramValues = accessedURL.split("/")

        for (i in paramKeys.indices) {
            // Ensure only the real parameters (e.g. <...>) are added to the request object
            if (paramKeys[i] == "" || !(paramKeys[i][0] == '<' && paramKeys[i].last() == '>'))
                continue

            val formattedKey = paramKeys[i].replace("[<\$>]".toRegex(), "") // Get rid of the < >
            params[formattedKey] = paramValues[i]
        }
        return params
    }
}