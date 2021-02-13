import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.exitProcess

class Klask {
    private val server = ServerSocket()
    private val routeMappings = mutableMapOf<String, HttpExchange>()

    fun run(host: String = "127.0.0.1", port: Int = 80) {
        server.bind(InetSocketAddress(host, port), 0)

        while (true) {
            try {
                val clientSocket = server.accept()
                GlobalScope.launch {
                    handleIncomingClient(clientSocket)
                }
            } catch (e: IOException) {
                println("Error serving the client:")
                println(e)
                exitProcess(1)
            }
        }
    }

    fun route(route: String, methods: List<String> = listOf("GET", "POST"), endpointHandler: (Request, Response) -> Unit) {
        if (routeMappings[route] != null)
            throw DuplicateRouteException("Routes must be unique: $route is already defined.")
        routeMappings[route] = HttpExchange(Request(), Response(), methods, endpointHandler)
    }

    private fun handleIncomingClient(clientSocket: Socket) {
        val reader = clientSocket.getInputStream().bufferedReader()
        val writer = DataOutputStream(clientSocket.getOutputStream())
        val requestData = mutableListOf<String>()

        var line = reader.readLine()
        while (line != "" && line != null) {
            requestData.add(line)
            line = reader.readLine()
        }

        // In order to read the request body:
        // First finding the Content-Length header
        val contentLength = requestData.getContentLength

        // Reading the request body
        val sb = StringBuilder()
        repeat(contentLength) { sb.append(reader.read().toChar()) }
        requestData.add(sb.toString())

        // Handling weird error when doing cURL request. The content is not usually read in the first loop of
        // the request, but when doing a cURL, it - for some reason - is
        if (requestData.last() == "")
            requestData.removeLast()

        if (requestData.isEmpty()) return

        // Start processing the request
        val (method, URI, _) = requestData[0].split(" ")
        val httpExchange = routeMappings.getExchange(URI)

        // Route is not defined - check static files
        if (httpExchange == null) {
            val staticFile = File("test/static/${URI.replace("/", "")}") // TODO: Fix hardcoded path
            if (!staticFile.exists()) {
                writer.sendNotFoundError(); writer.close()
                return
            }
            writer.sendStaticFile(staticFile); writer.close()
            return
        }

        val request = httpExchange.request
        val response = httpExchange.response

        // Handling data for the request object
        if ((method == "POST" || method == "PUT") && contentLength != 0)
            request.body = requestData.last()

        request.method = method
        request.contentType = requestData.getContentType

        // Sending back response
        val allowedMethods = httpExchange.allowedMethods
        if (method !in allowedMethods)
            writer.sendMethodNotAllowedError(allowedMethods)
        else {
            httpExchange.handler(request, response)
            writer.sendResponse(response.body)
        }

        writer.close()
    }

    private val List<String>.getContentLength: Int
        get() {
            this.forEach {
                val result = "Content-Length: (.*)".toRegex().find(it)
                if (result != null)
                    return result.groups[1]?.value?.toInt() ?: 0
            }
            return 0
    }

    private val List<String>.getContentType: Content
        get() {
            this.forEach {
                val result = "Content-Type: (.*)".toRegex().find(it)
                if (result != null)
                    return result.groups[1]?.value?.toContentType() ?: Content.NONE
            }
            return Content.NONE
    }

    private fun String.toContentType(): Content {
        for (contentType in Content.values())
            if (contentType.desc == this) return contentType
        return Content.NONE
    }

    // Adds URL parameters to the request object (if they are there) and returns the HttpExchange object found
    private fun MutableMap<String, HttpExchange>.getExchange(route: String): HttpExchange? {
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

    // *************************** Returning Responses ***************************

    private fun DataOutputStream.sendResponse(response: String) = this.writeBytes(response)

    private fun DataOutputStream.sendStaticFile(file: File) {
        val content = when (file.extension) { // TODO: Handling more static file types
            "js" -> Content.JAVASCRIPT
            "css" -> Content.CSS
            "ico" -> Content.ICON
            "png" -> Content.PNG
            "txt" -> Content.PLAIN
            "json" -> Content.JSON
            else -> throw IllegalArgumentException("File type is not allowed: ${file.extension}")
        }
        this.sendResponse(Response().sendFile(file, content).body)
    }

    private fun DataOutputStream.sendMethodNotAllowedError(allowed: List<String>) {
        val sb = StringBuilder()
        sb.append("HTTP/1.1 ${Status.HTTP_405_METHOD_NOT_ALLOWED.desc}\n").append("Allow: ${allowed.joinToString(", ")}\r\n")
        this.sendResponse(sb.toString())
    }

    private fun DataOutputStream.sendNotFoundError() =
        this.sendResponse("HTTP/1.1 ${Status.HTTP_404_NOT_FOUND.desc}\n\n<h1>404 Not Found</h1>\r\n")

    private class DuplicateRouteException(msg: String): Exception(msg)
}