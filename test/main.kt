fun main() {
    val app = Klask()

    app.route("/") { _, res ->
        res.renderTemplate("index.html")
    }

    app.route("/plain") { _, res ->
        res.makeResponse("Hello World!", Content.PLAIN)
    }

    // Throws custom DuplicateRouteException
    /*app.route("/plain") { _, res ->
        res.makeResponse("Hello World!", Content.PLAIN)
    }*/

    app.route("/json") { _, res ->
        res.makeResponse("""{"hello":"world!"}""", Content.JSON)
    }

    app.route("/xml") { _, res ->
        res.makeResponse("""
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
                <ele>hello world!</ele>
            </root>
        """.trimIndent(), Content.XML)
    }

    app.route("/urlenc", methods = listOf("POST")) { _, res ->
        res.makeResponse("username=peter&favorite+color=blue", Content.URLEncoded)
    }

    app.route("/testpost", methods = listOf("POST")) { req, res ->
        res.makeResponse("You sent: ${req.body}", Content.PLAIN)
    }

    app.route("/testparams/<idx1>/<idx2>") { req, res ->
        res.makeResponse("<p>You wrote <b>${req.params["idx1"]}</b> and <b>${req.params["idx2"]}</b> as parameters!</p>", Content.HTML)
    }

    app.run()
}