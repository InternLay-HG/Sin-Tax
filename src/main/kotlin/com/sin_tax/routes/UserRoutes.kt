package com.sin_tax.routes

import com.sin_tax.model.User
import com.sin_tax.plugins.createToken
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import repository.UserRepository
import repository.Users

fun Routing.userRoutes() {

    val userRepository = UserRepository()

    transaction {
        SchemaUtils.create(Users)
    }

    route("/user") {

        post("/create") {
            try {
                val user = call.receive<User>()
                val userId = userRepository.create(user)
                call.respond(createToken(userId.toString()))
            } catch(e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown Exception")
            }
        }

        get("/{id}") {
            val userId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val user = userRepository.getUser(userId.toInt())
            call.respond(user ?: HttpStatusCode.NotFound)
        }
    }
}