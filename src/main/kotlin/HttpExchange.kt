data class HttpExchange(var request: Request,
                   val response: Response,
                   val allowedMethods: List<String>,
                   val handler: (Request, Response) -> Unit)