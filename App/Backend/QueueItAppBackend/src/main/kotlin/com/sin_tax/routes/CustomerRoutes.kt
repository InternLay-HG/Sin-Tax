package com.sin_tax.routes

import com.sin_tax.model.Customer
import com.sin_tax.plugins.createToken
import com.sin_tax.repository.CustomerQueues.customerId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.sin_tax.repository.CustomerRepository
import com.sin_tax.repository.Users.customer

fun Routing.customerRoutes() {
    val customerRepository = CustomerRepository()

    route("/customer") {

        authenticate("user-jwt") {
            post("/register") {
                val customer = call.receive<Customer>()
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.getClaim("userId", String::class) ?: return@post call.respond(HttpStatusCode.Forbidden)
                val customerId = customerRepository.register(customer, userId.toInt())
                call.respond(createToken("customerId", customerId.toString()))
            }
        }

        authenticate("customer-jwt") {
            get("/this") {
                val principal = call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val customerId = principal.getClaim("customerId", String::class) ?: return@get call.respond(HttpStatusCode.Forbidden)
                val customer = customerRepository.getCustomerById(customerId.toInt()) ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(customer)
            }
        }
    }
}