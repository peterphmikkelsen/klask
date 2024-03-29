# Klask

Kotlin version of Flask.
[![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-purple.svg)](https://kotlinlang.org/)

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

## Sending JSON
Sending a JSON string is super simple! Just use the `Response.sendJson` function
```kotlin
app.route("/json") { _, res ->
    res.sendJson("""{"hello":"world!"}""")
}
```
The function takes either a string as above or any serializable object (using the [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) library)
```kotlin
@kotlinx.serialization.Serializable
data class Order(val id: String, val name: String, val price: Double, val date: String)

app.route("/json") { _, res ->
    val order = Order("100", "hammer", 10.2, "20/05/22")
    res.sendJson(order)
}
```

## Advanced examples

More examples
```kotlin
app.route("/") { req, res ->
    res.renderTemplate("index.html")
}

app.route("/plain") { req, res ->
    res.headers["Access-Control-Allow-Origin"] = "*" // Control the response headers
    res.makeResponse("Hello World!", Content.PLAIN, Status.HTTP_200_OK) // Explicitly defining the response code
}

app.route("/xml") { _, res ->
    res.makeResponse("""
        <?xml version="1.0" encoding="UTF-8"?>
        <root>
            <ele>hello world!</ele>
        </root>
    """.trimIndent(), Content.XML)
}

app.route("/urlenc") { _, res ->
    res.makeResponse("username=peter&favorite%color=blue", Content.URLEncoded)
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

## POST/PUT and DELETE
This example shows how to do a simple POST request. Use the `Request.receiveJsonObject<T>` function to automatically take the request-body and decode it into the desired object
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

## Sending files
Sending files is super simple. Just use the `Response.sendFile` function
```kotlin
app.route("/testfile") { _, res ->
    res.sendFile(File("myroot/mydir/myfile.pdf"))
}
```

## Redirecting
Klask is also able to redirect by using the HTTP response `301 Moved Permanently`
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

## Separate routes to different files
If you wish to keep your routes separate it is also possible to add routes using the `Klask.setRoutes` function. This is done by creating an extension function, like so
```kotlin
// OrderRoutes.kt
val orders = mutableListOf<Order>()

fun Klask.orderRoutes() {
    route("/orders") { req, res ->
        if (req.method == "GET") {
            res.sendJson(orders)
        } else {
            val order = req.receiveJsonObject<Order>()
            orders.add(order)
            res.sendStatus(Status.HTTP_201_CREATED)
        }
    }
    
    route("/orders/<id>", methods = listOf("GET", "DELETE")) { req, res ->
        val id = req.params["id"]
        val orderIdx = orders.indices.find { orders[it].id == id }
        if (orderIdx != null) {
            if (req.method == "GET") {
                res.sendJson(orders[orderIdx])
            } else {
                orders.removeAt(orderIdx)
                res.makeResponse("Successfully deleted order!", Content.PLAIN, Status.HTTP_202_ACCEPTED)
            }
        } else {
            res.sendStatus(Status.HTTP_404_NOT_FOUND)
        }
    }
}
```
And then add them to the main file
```kotlin
val app = Klask()

app.setRoutes {
    orderRoutes()
}
```

## Debugging
It is also possible to enable debug-mode, like so
```kotlin
val app = Klask()

// ...

app.run(debug = true)
```
This will enable logging and various prints to the console. For the logging, a file - `klask.log` - will be created, which will contain details about the running application.
