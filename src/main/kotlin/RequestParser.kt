class RequestParser {

    fun parse(requestData: MutableList<String>): Request {
        val request = Request()

        val (method, URL, protocolAndVersion) = requestData[0].split(" ")
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

        return request
    }

    private fun String.toContentType(): Content {
        for (contentType in Content.values())
            if (contentType.desc == this) return contentType
        return Content.NONE
    }
}