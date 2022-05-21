import kotlinx.serialization.Serializable
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
        res.sendJson("""{"hello":"world!"}""")
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

    app.route("/testparams/<idx1>/<idx2>") { req, res ->
        println("Client connected at /testparams/${req.params["idx1"]}/${req.params["idx2"]}")
        res.makeResponse("<p>You wrote <b>${req.params["idx1"]}</b> and <b>${req.params["idx2"]}</b> as parameters!</p>", Content.HTML)
    }

    app.route("/testparamstyped/<age:int>") { req, res ->
        res.makeResponse("<p><b>age = ${req.params["age"]}</b> of type <b>${req.params["age"]!!::class.simpleName}</b></p>", Content.HTML)
    }

    app.route("/testfile") { _, res ->
        println("Client connected at /testfile")
        res.headers["Cache-Control"] = "max-age=60, public"
        res.sendFile(File("src/test/kotlin/static/myicon.ico"))
    }

    app.route("/testredirect") { _, res ->
        res.redirect("/redirecttest")
    }

    app.route("/redirecttest") { _, res ->
        res.makeResponse("redirected from /testredirect", Content.PLAIN)
    }

    app.route("/testpdf") { _, res ->
        res.sendFile(File("src/test/kotlin/static/testpdf.pdf"))
    }

    app.route("/teststatus") { _, res ->
        res.sendStatus(Status.HTTP_418_IM_A_TEAPOT)
    }

    // =========== TESTING POST AND DELETE ===========

    val people = mutableListOf<Person>()
    app.route("/testpost", methods = listOf("POST")) { req, res ->
        println("Client connected at /testpost")
        val person = req.receiveJsonObject<Person>() // Throws exception if req.method is not POST/PUT or if req.contentType is not JSON
        people.add(person)
        println(people)
        res.sendStatus(Status.HTTP_201_CREATED)
    }

    app.route("/testdelete/<id>", methods = listOf("DELETE")) { req, res ->
        val id = req.params["id"]
        if (people.removeIf { it.id == id }) {
            println(people)
            res.makeResponse("Person successfully removed!", Content.PLAIN, Status.HTTP_202_ACCEPTED)
        } else {
            res.sendStatus(Status.HTTP_404_NOT_FOUND)
        }
    }

    // =========== ALTERNATIVE WAY TO DO ROUTES ===========

    app.setRoutes {
        getOrDeleteOrderRoutes()
        addOrderRoute()
    }

    // The server will run with host=localhost and port=80 if no other parameters are given
    app.run(port = 3000)
}

@Serializable
data class Person(val id: String, val firstName: String, val lastName: String, val age: Int)