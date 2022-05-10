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
    private val parser = RequestParser(BaseURLConverter())

    fun run(host: String = "127.0.0.1", port: Int = 80, debug: Boolean = false) {
        server.bind(InetSocketAddress(host, port), 0)

        println("Running Klask Server...")

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
        val contentLength = requestData.contentLength

        // Reading the request body
        val sb = StringBuilder()
        repeat(contentLength) { sb.append(reader.read().toChar()) }
        requestData.add(sb.toString())

        // Handling weird error when doing cURL request. The content is not usually read in the first loop of
        // the request, but when doing a cURL, it - for some reason - is
        if (requestData.last() == "")
            requestData.removeLast()

        if (requestData.isEmpty()) return

        // Start parsing the request. The request object is only to access the URL even when
        val httpExchange = parser.parse(requestData, routeMappings)

        // Route is not defined - check static files
        if (httpExchange == null) {
            val staticFile = File("src/test/kotlin/static${requestData[0].split(" ")[1]}") // TODO: Fix hardcoded path
            if (!staticFile.exists()) {
                writer.sendNotFoundError(); writer.close()
                return
            }
            writer.sendStaticFile(staticFile); writer.close()
            return
        }

        // Sending back response
        if (httpExchange.request.method !in httpExchange.allowedMethods)
            writer.sendMethodNotAllowedError(httpExchange.allowedMethods)
        else {
            httpExchange.handler(httpExchange.request, httpExchange.response)
            writer.sendResponse(httpExchange.response.body)
        }

        writer.close()
    }

    // A little unnecessary to use an extension property here - but it looks cool :)
    private val List<String>.contentLength: Int
        get() {
            this.forEach {
                val result = "Content-Length: (.*)".toRegex().find(it)
                if (result != null)
                    return result.groups[1]?.value?.toInt() ?: 0
            }
            return 0
    }

    // *************************** Returning Responses ***************************

    private fun DataOutputStream.sendResponse(response: String) = this.writeBytes(response)

    private fun DataOutputStream.sendStaticFile(file: File) {
        this.sendResponse(Response().sendFile(file).body)
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