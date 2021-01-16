fun main() {
    val app = Klask()

    app.route("/") { _, res ->
        println("Client connected at /")
        res.renderTemplate("index.html")
    }

    app.route("/plain") { _, res ->
        println("Client connected at /plain")
        res.makeResponse("Hello World!", Content.PLAIN)
    }

    // Throws custom DuplicateRouteException
    /*app.route("/plain") { _, res ->
        res.makeResponse("Hello World!", Content.PLAIN)
    }*/

    app.route("/json") { _, res ->
        println("Client connected at /json")
        res.makeResponse("""{"hello":"world!"}""", Content.JSON)
    }

    app.route("/xml") { _, res ->
        println("Client connected at /xml")
        res.makeResponse("""
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
                <ele>hello world!</ele>
            </root>
        """.trimIndent(), Content.XML)
    }

    app.route("/urlenc") { _, res ->
        println("Client connected at /urlenc")
        res.makeResponse("username=peter&favorite%color=blue", Content.URLEncoded)
    }

    app.route("/testpost", methods = listOf("POST")) { req, res ->
        println("Client connected at /testpost")
        res.makeResponse("You sent: ${req.body}", Content.PLAIN)
    }

    app.route("/testparams/<idx1>/<idx2>") { req, res ->
        println("Client connected at /testparams/${req.params["idx1"]}/${req.params["idx2"]}")
        res.makeResponse("<p>You wrote <b>${req.params["idx1"]}</b> and <b>${req.params["idx2"]}</b> as parameters!</p>", Content.HTML)
    }

    // The server will run with host=localhost and port=80 if no other parameters are given
    app.run()
}