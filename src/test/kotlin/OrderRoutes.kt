import kotlinx.serialization.Serializable

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

@Serializable
data class Order(val id: String, val name: String, val price: Double, val date: String)