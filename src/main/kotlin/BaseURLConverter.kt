class BaseURLConverter: URLConverter {

   @OptIn(ExperimentalStdlibApi::class)
   override fun getURLParameters(savedURL: String, accessedURL: String): MutableMap<String, Any> {
        val params = mutableMapOf<String, Any>()
        val paramKeys = savedURL.split("/")
        val paramValues = accessedURL.split("/")

        for (i in paramKeys.indices) {
            // Ensure only the real parameters (e.g. <...>) are added to the request object
            if (paramKeys[i] == "" || !(paramKeys[i][0] == '<' && paramKeys[i].last() == '>'))
                continue

            val formattedKey = paramKeys[i].replace("[<\$>]".toRegex(), "") // Get rid of the < >
            if (formattedKey.contains(":")) {
                val (name, type) = formattedKey.split(":").map(String::trim)

                val exception = Exception("Parameter-type was specified as $type but you entered \"${paramValues[i]}\"")
                val valueToKotlinType: Any = when (type) {
                    "int" -> {
                        try {
                            paramValues[i].toInt()
                        } catch (e: NumberFormatException) {
                            throw exception
                        }
                    }
                    "str" -> paramValues[i]
                    "bool" -> {
                        if (paramValues[i].lowercase() == "true" || paramValues[i].lowercase() == "false") {
                            paramValues[i].toBoolean()
                        } else {
                            throw exception
                        }
                    }
                    "double" -> {
                        try {
                            paramValues[i].toDouble()
                        } catch (e: NumberFormatException) {
                            throw exception
                        }
                    }
                    "list" -> {
                        if (!paramValues[i].contains(","))
                            throw exception
                        paramValues[i].split(",")
                    }
                    else -> paramValues[i]
                }
                params[name] = valueToKotlinType
            } else {
                params[formattedKey] = paramValues[i]
            }
        }
        return params
    }
}