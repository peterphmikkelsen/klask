enum class Content(val desc: String) {
    NONE(""),
    HTML("text/html"),
    XML("application/xml"),
    PLAIN("text/plain"),
    JSON("application/json"),
    URLEncoded("application/x-www-form-urlencoded"),
    JAVASCRIPT("application/javascript; charset=utf-8"),
    CSS("text/css; charset=utf-8"),
//    PNG("image/png"),
//    JPEG("image/jpeg"),
//    ICON("image/x-icon"),
}