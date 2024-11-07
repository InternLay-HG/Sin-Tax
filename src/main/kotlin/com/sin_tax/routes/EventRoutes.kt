package com.sin_tax.routes

import com.sin_tax.model.Event
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import repository.*

fun Routing.eventRoutes() {

    transaction {
        SchemaUtils.create(Events)
    }

    val userRepository = UserRepository()
    val eventRepository = EventRepository()

    route("/event") {

        authenticate("user-jwt") {
            post("/create") {
                val event = call.receive<Event>()
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.getClaim("userId", String::class) ?: return@post call.respond(HttpStatusCode.Forbidden)
                val businessEntity = dbQuery {
                    val userEntity = UserEntity.findById(userId.toInt())
                    val businessEntity = userEntity?.business
                    businessEntity
                }

                if (businessEntity == null) return@post call.respond(HttpStatusCode.BadRequest)
                eventRepository.create(event, businessEntity)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
