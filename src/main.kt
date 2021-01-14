fun main() {
    val app = Klask()
    app.route("/") { _, res ->
        res.renderTemplate("index.html")
    }

    app.route("/hello") { _, res ->
        res.makeResponse("Hello World!", "text/plain")
    }

    app.route("/json") { _, res ->
        res.makeResponse("""{"hello":"world!"}""", "application/json")
    }

    app.route("/test", methods = listOf("POST")) { req, res ->
        println(req.body)
        res.makeResponse("home=Cosby&favorite+flavor=flies", "application/x-www-form-urlencoded")
    }

    app.run()
}