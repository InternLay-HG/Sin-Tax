package com.sin_tax.routes

import com.sin_tax.model.Event
import com.sin_tax.plugins.createToken
import com.sin_tax.repository.*
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
import org.koin.ktor.ext.inject

fun Routing.eventRoutes() {

    transaction {
        SchemaUtils.create(Events)
    }

    //val eventRepository: EventRepository by inject()
    val eventRepository = EventRepository()

    route("/event") {

        authenticate("business-jwt") {
            post("/create") {
                val event = call.receive<Event>()
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val businessId = principal.getClaim("businessId", String::class) ?: return@post call.respond(HttpStatusCode.Forbidden)
                val businessEntity = dbQuery {
                    BusinessEntity.findById(businessId.toInt())
                }

                if (businessEntity == null) return@post call.respond(HttpStatusCode.BadRequest)
                val eventId = eventRepository.create(event, businessEntity)
                call.respond(createToken("eventId", eventId.toString()))
            }
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
