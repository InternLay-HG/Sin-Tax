package com.sin_tax.routes

import com.sin_tax.model.Business
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import repository.BusinessRepository
import repository.Businesses

fun Routing.businessRoutes() {

    //val businessRepository: BusinessRepository by inject()
    val businessRepository = BusinessRepository()

    transaction {
        SchemaUtils.create(Businesses)
    }

    route("/business") {

        authenticate("user-jwt") {
            post("/register") {
                val business = call.receive<Business>()
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.getClaim("userId", String::class) ?: return@post call.respond(HttpStatusCode.Forbidden)
                businessRepository.register(business, userId.toInt())
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}