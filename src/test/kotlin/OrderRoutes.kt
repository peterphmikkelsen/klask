import kotlinx.serialization.Serializable

val orders = mutableListOf<Order>()

fun Klask.getOrderRoutes() {
    route("/orders") { _, res ->
        res.sendJson(orders)
    }

    route("/orders/<id>") { req, res ->
        println("HERE")
        val id = req.params["id"]
        val order = orders.find { it.id == id }
        if (order != null) {
            res.sendJson(order)
        } else {
            res.sendStatus(Status.HTTP_404_NOT_FOUND)
        }
    }
}

fun Klask.addOrderRoute() {
   route("/orders/add", methods = listOf("POST")) { req, res ->
       val order = req.receiveJsonObject<Order>()
       orders.add(order)
       res.sendStatus(Status.HTTP_201_CREATED)
   }
}

fun Klask.deleteOrderRoute() {
    route("/orders/delete/<id>", methods = listOf("DELETE")) { req, res ->
        val id = req.params["id"]
        if (orders.removeIf { it.id == id }) {
            res.makeResponse("Successfully deleted order!", Content.PLAIN, Status.HTTP_202_ACCEPTED)
        } else {
            res.sendStatus(Status.HTTP_404_NOT_FOUND)
        }
    }
}

@Serializable
data class Order(val id: String, val name: String, val price: Double, val date: String)