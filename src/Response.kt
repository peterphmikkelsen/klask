import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Response(private var contentType: String = "", private var responseCode: Int = -1) {

    // Making the getter of body public but setter private
    lateinit var body: String
        private set

    private val responseCodes = mapOf(200 to "200 OK", 404 to "404 Not Found", 405 to "405 Method Not Allowed") // TODO: All codes should be available

    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
    init {
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    }

    fun renderTemplate(fileName: String): Response {
        val file = File("test/templates/$fileName") // TODO: Fix hardcoded path
        val reader = file.bufferedReader()
        val sb = StringBuilder()
        sb.append("HTTP/1.1 200 OK\n")
            .append("Content-Type: text/html; charset=utf-8\n")
            .append("Connection: keep-alive\n")
            .append("Date: ${dateFormat.format(Date())} GMT\r\n\n")

        var line = reader.readLine()
        while (line != null) {
            sb.append("$line\n")
            line = reader.readLine()
        }
        reader.close()

        this.contentType = "text/html"
        this.responseCode = 200
        this.body = sb.toString()
        return this
    }

    fun makeResponse(response: String, contentType: Content, responseCode: Int = 200): Response {
        val sb = StringBuilder()
        sb.append("HTTP/1.1 ${responseCodes[responseCode]}\n")
            .append("Content-Type: ${contentType.desc}; charset=utf-8\n")
            .append("Connection: keep-alive\n")
            .append("Date: ${dateFormat.format(Date())} GMT\n")
            .append("Content-Length: ${response.length}\n")
            .append(if (contentType.desc == "application/json") "Accept: application/json\n" else "")
            .append("\r\n$response")

        this.contentType = contentType.desc
        this.responseCode = responseCode
        this.body = sb.toString()
        return this
    }
}