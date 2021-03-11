# Klask

Kotlin version of Flask.

## Built With

Build entirely in Kotlin using ServerSocket.

## How it works

The overall structure is very similar to Flask:
```kotlin
val app = Klask()

app.route("/") { req, res ->
    res.makeResponse("<h1>Hello World!</h1>", Content.HTML) // Default response code is 200
}

app.run() // Starts listening on localhost port 80 by default
```
One can now simply enter localhost/ in the browser and see the HTML shown.

## Advanced examples

Showing current full potential:
```kotlin
app.route("/") { req, res ->
    res.renderTemplate("index.html")
}

app.route("/plain") { req, res ->
    res.makeResponse("Hello World!", Content.PLAIN, Status.HTTP_200_OK) // Explicitly defining the response code
}

app.route("/json") { _, res ->
    res.headers["Access-Control-Allow-Origin"] = "*" // Control the response headers
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

// This only allows POST requests. Default is GET and POST.
app.route("/testpost", methods = listOf("POST")) { req, res ->
    res.makeResponse("You sent: ${req.body}", Content.PLAIN)
}

app.route("/testparams/<idx1>/<idx2>") { req, res ->
    res.makeResponse("<p>You wrote <b>${req.params["idx1"]}</b> and <b>${req.params["idx2"]}</b> as parameters!</p>", Content.HTML)
}
```
## Sending files
Sending files is super simple. Just use the ".sendFile" function
```kotlin
app.route("/testfile") { _, res ->
    res.sendFile(File("myroot/mydir/myfile.txt"), Content.PLAIN)
}
```

## Redirecting
Klask is also able to redirect by using the HTTP response "301 Moved Permanently"
```kotlin
app.route("/testredirect") { _, res ->
    res.redirect("/redirected")
}

app.route("/redirected") { _, res ->
    res.makeResponse("Redirected from /testredirect", Content.PLAIN)
}
```

