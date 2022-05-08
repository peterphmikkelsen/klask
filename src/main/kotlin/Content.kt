enum class Content(val extension: String?, val desc: String) {
    NONE("",""),
    HTML("html","text/html"),
    XML("xml","application/xml"),
    PLAIN("txt", "text/plain"),
    JSON("json", "application/json"),
    URLEncoded(null, "application/x-www-form-urlencoded"),
    JAVASCRIPT("js", "application/javascript; charset=utf-8"),
    CSS("css", "text/css; charset=utf-8"),
    PNG("png", "image/png"),
    JPEG("jpeg", "image/jpeg"),
    ICON("ico", "image/x-icon"),
    PDF("pdf", "application/pdf")
}