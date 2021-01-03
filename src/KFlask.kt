import kotlinx.coroutines.*
import java.io.IOException
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.exitProcess

@ExperimentalCoroutinesApi
class KFlask {

    private val server = ServerSocket()
    private val routeMappings = mutableMapOf<String, HttpExchange>()

    fun run(host: String = "127.0.0.1", port: Int = 8080) {
        server.bind(InetSocketAddress(host, port), 0)
        runBlocking {
            while (true) {
                try {
                    withContext(Dispatchers.IO) {
                        val clientSocket = server.accept()
                        println("Client connected! " + this.coroutineContext)
                        val reader = clientSocket.getInputStream().bufferedReader()
                        val requestData = mutableListOf<String>()

                        var line = reader.readLine()
                        while (line != "") {
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
                            requestData.removeAt(requestData.lastIndex)

                        // Start processing the request
                        val (method, URI, _) = requestData[0].split(" ")
                        val httpExchange = routeMappings[URI]

                        // Route is not defined
                        if (httpExchange == null) {
                            clientSocket.sendNotFoundError()
                            return@withContext
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
                            clientSocket.sendMethodNotAllowedError(allowedMethods)
                        else {
                            httpExchange.handler(request, response)
                            clientSocket.sendResponse(response.body)
                        }
                    }

                } catch (e: IOException) {
                    println("Error serving the client:")
                    println(e)
                    exitProcess(1)
                }
            }
        }
    }

    fun route(route: String, methods: List<String> = listOf("GET", "POST"), endpointHandler: (Request, Response) -> Unit) {
        routeMappings[route] = HttpExchange(Request(), Response(), methods, endpointHandler)
    }

    private val List<String>.getContentLength: Int
        get() {
            this.forEach {
                val result = """Content-Length: (.*)""".toRegex().find(it)
                if (result != null)
                    return result.groups[1]?.value?.toInt() ?: 0
            }
            return 0
    }

    private val List<String>.getContentType: String
        get() {
            this.forEach {
                val result = """Content-Type: (.*)""".toRegex().find(it)
                if (result != null)
                    return result.groups[1]?.value ?: ""
            }
            return ""
    }

    // *************************** Returning Responses ***************************

    private fun Socket.sendResponse(response: String) {
        val writer = PrintWriter(this.getOutputStream())
        writer.println(response)
        writer.flush()
        writer.close()
    }

    private fun Socket.sendMethodNotAllowedError(allowed: List<String>) {
        val writer = PrintWriter(this.getOutputStream())
        val sb = StringBuilder()
        sb.append("HTTP/1.1 405 Method Not Allowed\n").append("Allow: ").append(allowed.joinToString(", ")).append("\r\n")
        writer.println(sb.toString())
        writer.flush()
        writer.close()
    }

    private fun Socket.sendNotFoundError() {
        val writer = PrintWriter(this.getOutputStream())
        writer.println("HTTP/1.1 404 Not Found\r\n")
        writer.flush()
        writer.close()
    }
}