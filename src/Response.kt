import java.io.File

class Response(var body: String = "", var contentType: String = "") {

    fun renderTemplate(fileName: String): Response {
        val file = File("src/templates/$fileName")
        val reader = file.bufferedReader()
        val sb = StringBuilder()
        sb.append("HTTP/1.1 200 OK\n")
        sb.append("Content-Type: text/html; charset=utf-8\n")
        sb.append("Content-Length: ${file.length()}\r\n\n")
        var line = reader.readLine()
        while (line != null) {
            sb.append("$line\n")
            line = reader.readLine()
        }
        reader.close()

        this.contentType = "text/html"
        this.body = sb.toString()
        return this
    }

    fun makeResponse(response: String, contentType: String): Response {
        val sb = StringBuilder()
        sb.append("HTTP/1.1 200 OK\n").append("Content-Type: $contentType; charset=utf-8\n").append("Content-Length: ${response.length}")
        if (contentType == "application/json")
            sb.append("\nAccept: application/json")
        sb.append("\r\n\n$response")

        this.contentType = contentType
        this.body = sb.toString()
        return this
    }

}