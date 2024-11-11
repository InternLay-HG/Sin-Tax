package com.sin_tax.routes

import com.sin_tax.model.Customer
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.sin_tax.repository.CustomerRepository

fun Routing.customerRoutes() {
    val customerRepository = CustomerRepository()

    route("/customer") {

        authenticate("user-jwt") {
            post("/register") {
                val customer = call.receive<Customer>()
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.getClaim("userId", String::class) ?: return@post call.respond(HttpStatusCode.Forbidden)
                customerRepository.register(customer, userId.toInt())
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}