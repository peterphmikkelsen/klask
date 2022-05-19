import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class Request(var method: String = "",
              var protocol: String = "",
              var protocolVersion: String = "",
              var url: String = "",
              var host: String = "",
              var headers: MutableMap<String, String> = mutableMapOf(),
              var cookie: String = "",
              var contentType: Content = Content.NONE,
              var body: String = "",
              var params: MutableMap<String, Any> = mutableMapOf(),
              var queries: MutableMap<String, String> = mutableMapOf(),
) {
    inline fun <reified T> receiveJsonObject(): T {
        if (method != "POST" && method != "PUT")
            throw IllegalArgumentException("The used method must be either POST or PUT, it was $method.")

        if (contentType != Content.JSON)
            throw Klask.JsonDecodeException("Content-Type must be ${Content.JSON.desc}, it was ${contentType.desc}.")

        return Json.decodeFromString(body)
    }
}

