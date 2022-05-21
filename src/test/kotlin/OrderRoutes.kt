import kotlinx.serialization.Serializable

val orders = mutableListOf<Order>()

fun Klask.getOrDeleteOrderRoutes() {
    route("/orders") { _, res ->
        res.sendJson(orders)
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

fun Klask.addOrderRoute() {
   route("/orders/add", methods = listOf("POST")) { req, res ->
       val order = req.receiveJsonObject<Order>()
       orders.add(order)
       res.sendStatus(Status.HTTP_201_CREATED)
   }
}

@Serializable
data class Order(val id: String, val name: String, val price: Double, val date: String)