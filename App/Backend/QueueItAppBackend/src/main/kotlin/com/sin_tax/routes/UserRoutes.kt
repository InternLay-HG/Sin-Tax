package com.sin_tax.routes

import com.sin_tax.model.User
import com.sin_tax.plugins.createToken
import com.sin_tax.repository.IncorrectPasswordException
import com.sin_tax.repository.UserRepository
import com.sin_tax.repository.Users
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.userRoutes() {

//    val userRepository: UserRepository by inject()
    val userRepository = UserRepository()

    transaction {
        SchemaUtils.create(Users)
    }

    route("/user") {

        post("/create") {
            try {
                val user = call.receive<User>()
                val userId = userRepository.create(user)
                call.respond(createToken("userId", userId.toString()))
            } catch(e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown Exception")
            }
        }

        post("/login") {
            try {
                val credentials = call.receive<User>()
                val user = userRepository.findUser(credentials) ?: return@post call.respond(HttpStatusCode.NotFound)
                call.respond(createToken("userId", user.id.toString()))
            } catch (e: IncorrectPasswordException) {
                call.respond(HttpStatusCode.Forbidden, "Incorrect Password!")
            } catch(e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown Exception")
            }
        }

        get("/{id}") {
            val userId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val user = userRepository.getUser(userId.toInt())
            call.respond(user ?: HttpStatusCode.NotFound)
        }

        authenticate("user-jwt") {
            get("/this") {
                val principal = call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.getClaim("userId", String::class) ?: return@get call.respond(HttpStatusCode.Forbidden)
                call.respond(userRepository.getUser(userId.toInt()) ?: HttpStatusCode.NotFound)
            }
        }
    }
}