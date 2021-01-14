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

    app.route("/testpost", methods = listOf("POST")) { req, res ->
        println(req.body)
        res.makeResponse("username=peter&favorite+color=blue", "application/x-www-form-urlencoded")
    }

    app.route("/testparams/<idx1>/<idx2>") { req, res ->
        res.makeResponse("<p>You wrote <b>${req.params["idx1"]}</b> and <b>${req.params["idx2"]}</b> as parameters!</p>", "text/html")
    }

    app.run()
}