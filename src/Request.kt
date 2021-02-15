class Request(var method: String = "",
              var contentType: Content = Content.NONE,
              var body: String = "",
              var params: MutableMap<String, String> = mutableMapOf(),
              var args: MutableMap<String, String> = mutableMapOf(),
)