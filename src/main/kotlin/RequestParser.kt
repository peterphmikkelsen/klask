class RequestParser {

    // Parses the request data into a Request object and returns the belonging HttpExchange object. TODO: Handle malformed requests
    fun parse(requestData: MutableList<String>, routeMappings: MutableMap<String, HttpExchange>): HttpExchange? {
        val (method, URL, protocolAndVersion) = requestData[0].split(" ")

        val httpExchange = if ("?" !in URL) {
            routeMappings.handleURLParameters(URL)
        } else {
            val (baseURL, queryString) = URL.split("?")
            routeMappings.handleURLParameters(baseURL)?.handleURLQueries(queryString)
        }

        val request = httpExchange?.request ?: Request()

        request.method = method
        request.url = URL

        val (protocol, version) = protocolAndVersion.split("/")
        request.protocol = protocol
        request.protocolVersion = version

        request.host = requestData[1].substringAfter(": ")

        for (i in 2 until requestData.size-1) {
            val current = requestData[i]
            val headerName = current.substringBefore(":")
            val headerValue = current.substringAfter(": ")

            if (headerName == "Cookie") {
                request.cookie = headerValue
                continue
            }

            if (headerName == "Content-Type") {
                request.contentType = headerValue.toContentType()
                continue
            }

            request.headers[headerName] = headerValue
        }

        if ((method == "POST" || method == "PUT") && request.headers["Content-Length"] != null)
            request.body = requestData.last()

        return httpExchange
    }

    private fun MutableMap<String, HttpExchange>.handleURLParameters(route: String): HttpExchange? {
        val exchange = this[route]

        // If the route has an exact match (e.g. no parameters) just return it
        if (exchange != null)
            return exchange

        val paramValues = route.split("/")
        for ((k, v) in this) {
            // Don't consider the root URL
            if (k == "/") continue

            // Make sure we actually match the correct route
            if (!(k.replace("<.+>".toRegex(), ".+").toRegex().matches(route)))
                continue

            // Add the parameters to the request object
            val paramKeys = k.split("/")
            for (i in paramKeys.indices) {
                // Ensure only the real parameters (e.g. <...>) are added to the request object
                if (paramKeys[i] == "" || !(paramKeys[i][0] == '<' && paramKeys[i].last() == '>'))
                    continue

                val formattedKey = paramKeys[i].replace("[<\$>]".toRegex(), "") // Get rid of the < >
                v.request.params[formattedKey] = paramValues[i]
            }
            return v
        }
        return null
    }

    private fun HttpExchange.handleURLQueries(queryString: String): HttpExchange {
        for (query in queryString.split("&"))
            this.request.args[query.substringBefore("=")] = query.substringAfter("=")
        return this
    }

    private fun String.toContentType(): Content {
        for (contentType in Content.values())
            if (contentType.desc == this) return contentType
        return Content.NONE
    }
}