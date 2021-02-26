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
        if (file.extension != "html") throw IllegalArgumentException("Only HTML files are allowed. Use \"sendFile\" for sending arbitrary files.")

        return sendFile(file, Content.HTML)
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

    fun sendFile(file: File, contentType: Content, responseCode: Status = Status.HTTP_200_OK): Response {
        val reader = file.inputStream().buffered()
        val sb = StringBuilder()
        sb.append("HTTP/1.1 ${responseCode.desc}\n")
            .append("Content-Type: ${contentType.desc}\n")
            .append("Content-Length: ${file.length()}\n")

        for ((k, v) in headers)
            sb.append("$k: $v\n")

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
}