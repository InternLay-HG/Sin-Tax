package com.sin_tax.routes

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        userRoutes()
        businessRoutes()
        customerRoutes()
        eventRoutes()
        queueRoutes()
    }
}
