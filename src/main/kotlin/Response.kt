import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Response(private var contentType: Content = Content.NONE, private var responseCode: Status = Status.NONE, var headers: MutableMap<String, String> = mutableMapOf()) {

    // Making the getter of body public but setter private
    lateinit var body: String
        private set

    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss")
    init {
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    }

    fun renderTemplate(fileName: String): Response {
        val file = File("src/test/kotlin/templates/$fileName") // TODO: Fix hardcoded path
        if (file.extension != "html") throw IllegalArgumentException("Only HTML files are allowed. Use \"Response.sendFile\" for sending arbitrary files.")

        return sendFile(file)
    }

    fun makeResponse(response: String, contentType: Content, responseCode: Status = Status.HTTP_200_OK): Response {
        val sb = StringBuilder()
        sb.append("HTTP/1.1 ${responseCode.desc}\n")
            .append("Content-Type: ${contentType.desc}; charset=UTF-8\n")
            .append("Date: ${dateFormat.format(Date())} GMT\n")
            .append("Content-Length: ${response.length}\n")
            .append(if (contentType == Content.JSON) "Accept: ${Content.JSON.desc}\n" else "")

        for ((k, v) in headers)
            sb.append("$k: $v\n")

        sb.append("\r\n$response")

        this.contentType = contentType
        this.responseCode = responseCode
        this.body = sb.toString()
        return this
    }

    fun sendFile(file: File, lastModified: Boolean = true, responseCode: Status = Status.HTTP_200_OK): Response {
        val contentType = file.extension.toContentType()

        val reader = file.inputStream().buffered()
        val sb = StringBuilder()
        sb.append("HTTP/1.1 ${responseCode.desc}\n")
            .append("Content-Type: ${contentType.desc}\n")
            .append("Content-Length: ${file.length()}\n")

        for ((k, v) in headers)
            sb.append("$k: $v\n")

        // Primarily
        if (lastModified)
            sb.append("Last-Modified: ${dateFormat.format(file.lastModified())} GMT\n")

        sb.append("Date: ${dateFormat.format(Date())} GMT\r\n\n")

        var line = reader.read()
        while (line != -1) {
            sb.append(line.toChar())
            line = reader.read()
        }
        reader.close()

        this.contentType = contentType
        this.responseCode = responseCode
        this.body = sb.toString()
        return this
    }

    fun sendJson(payload: String): Response = makeResponse(payload, Content.JSON)

    // reified makes the compiler use the actual type at runtime - myVar is T => myVar is String (if the type was string at runtime)
    inline fun <reified T> sendJson(payload: T): Response {
        return makeResponse(Json.encodeToString(payload), Content.JSON)
    }

    fun redirect(redirectEndpoint: String): Response {
        val sb = StringBuilder()
        sb.append("HTTP/1.1 ${Status.HTTP_301_MOVED_PERMANENTLY.desc}\n")
            .append("Location: $redirectEndpoint\r\n\n")

        this.contentType = Content.NONE
        this.responseCode = Status.HTTP_301_MOVED_PERMANENTLY
        this.body = sb.toString()
        return this
    }
    fun sendStatus(status: Status): Response = makeResponse(status.desc, Content.PLAIN, status)

    private fun String.toContentType(): Content {
        for (type in Content.values()) {
            if (type.extension != this)
                continue
            return type
        }
        return Content.NONE
    }
}