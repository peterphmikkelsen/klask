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
```

## POST/PUT and DELETE
This example shows how to do a simple POST request. Use the `Request.receiveJsonObject<T>` method to automatically take the request-body and decode it into the desired object
```kotlin
@kotlinx.serialization.Serializable
data class Person(val id: String, val firstName: String, val lastName: String, val age: Int)

val people = mutableListOf<Person>()

// This only allows POST requests. Default is GET and POST.
app.route("/postperson", methods = listOf("POST")) { req, res ->
    val person = req.receiveJsonObject<Person>() // Throws exception if req.method is not POST/PUT or if req.contentType is not JSON
    people.add(person)
    res.sendStatus(Status.HTTP_201_CREATED)
}
```
Similarly, you can also do a DELETE request
```kotlin
app.route("/deleteperson/<id>", methods = listOf("DELETE")) { req, res ->
    val id = req.params["id"]
    if (people.removeIf { it.id == id }) {
        res.makeResponse("Person successfully removed!", Content.PLAIN, Status.HTTP_202_ACCEPTED)
    } else {
        res.sendStatus(Status.HTTP_404_NOT_FOUND)
    }
}
```

## URL Parameters
It is also possible to add named parameters to the target URL
```kotlin
app.route("/testparams/<idx1>/<idx2>") { req, res ->
    res.makeResponse("<p>You wrote <b>${req.params["idx1"]}</b> and <b>${req.params["idx2"]}</b> as parameters!</p>", Content.HTML)
}
```
And you can even use typed parameters!
```kotlin
app.route("/testparamstyped/<age:int>") { req, res ->
    res.makeResponse("<p><b>age = ${req.params["age"]}</b> of type <b>${req.params["age"]!!::class.simpleName}</b></p>", Content.HTML)
}
```
Here the first example leads to all parameters being strings (at runtime) while the second example ensures correct types (again, at runtime) on all parameters where a type is present.

If any of the types are not correct, the client will receive the message: `400 Bad Request. Parameter-type was specified as $type but you entered "$value"`

## Sending files
Sending files is super simple. Just use the `Response.sendFile` method
```kotlin
app.route("/testfile") { _, res ->
    res.sendFile(File("myroot/mydir/myfile.pdf"))
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

## Sending status
If you want to send only a status code and description
```kotlin
app.route("/status") { _, res ->
    res.sendStatus(Status.HTTP_418_IM_A_TEAPOT) // 418 I'm a teapot
}
```
