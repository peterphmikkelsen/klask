import java.io.File

fun main() {
    val app = Klask()

    app.route("/") { _, res ->
        println("Client connected at /")
        res.renderTemplate("index.html")
    }

    app.route("/plain") { _, res ->
        println("Client connected at /plain")
        res.headers["Access-Control-Allow-Origin"] = "*"
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
        res.makeResponse("You sent: ${req.body}\n", Content.PLAIN)
    }

    app.route("/testparams/<idx1>/<idx2>") { req, res ->
        println("Client connected at /testparams/${req.params["idx1"]}/${req.params["idx2"]}")
        println(req.queries)
        res.makeResponse("<p>You wrote <b>${req.params["idx1"]}</b> and <b>${req.params["idx2"]}</b> as parameters!</p>", Content.HTML)
    }

    app.route("/testparamstyped/<age:int>") { req, res ->
        res.makeResponse("<p><b>age = ${req.params["age"]}</b> of type <b>${req.params["age"]!!::class.simpleName}</b></p>", Content.HTML)
    }

    app.route("/testfile") { _, response ->
        println("Client connected at /testfile")
        response.headers["Cache-Control"] = "max-age=60, public"
        response.sendFile(File("src/test/kotlin/static/myicon.ico"))
    }

    app.route("/testredirect") { _, response ->
        response.redirect("/redirecttest")
    }

    app.route("/redirecttest") { _, response ->
        response.makeResponse("redirected from /testredirect", Content.PLAIN)
    }

    app.route("/testpdf") { _, response ->
        response.sendFile(File("src/test/kotlin/static/testpdf.pdf"))
    }

    app.route("/teststatus") { _, response ->
        response.sendStatus(Status.HTTP_418_IM_A_TEAPOT)
    }

    // The server will run with host=localhost and port=80 if no other parameters are given
    app.run(port = 3000)
}