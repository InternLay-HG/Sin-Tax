package com.sin_tax.routes

import com.sin_tax.model.Event
import com.sin_tax.plugins.createToken
import com.sin_tax.repository.BusinessEntity
import com.sin_tax.repository.EventRepository
import com.sin_tax.repository.Events
import com.sin_tax.repository.QueueRepository
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

fun Routing.eventRoutes() {

    transaction {
        SchemaUtils.create(Events)
    }

    //val eventRepository: EventRepository by inject()
    val eventRepository = EventRepository()
    val queueRepository: QueueRepository = QueueRepository()

    route("/event") {

        authenticate("business-jwt") {
            post("/create") {
                val event = call.receive<Event>()
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val businessId = principal.getClaim("businessId", String::class) ?: return@post call.respond(
                    HttpStatusCode.Forbidden
                )
                val businessEntity = dbQuery {
                    BusinessEntity.findById(businessId.toInt())
                }

                if (businessEntity == null) return@post call.respond(HttpStatusCode.BadRequest)
                val eventId = eventRepository.create(event, businessEntity)
                call.respond(HttpStatusCode.OK)
            }
        }

        authenticate("customer-jwt") {
            get("/current/customer") {
                val principal = call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val customerId =
                    principal.getClaim("customerId", String::class) ?: return@get call.respond(HttpStatusCode.Forbidden)
                //TODO
            }
        }

        authenticate("business-jwt") {
            get("/current/business") {
                val principal =
                    call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val businessId = principal.getClaim("businessId", String::class) ?: return@get call.respond(HttpStatusCode.Forbidden)
                val events = eventRepository.getEventsForBusiness(businessId.toInt())
                call.respond(events)
            }
        }

        get("/current/all") {
            val queues = eventRepository.filterRunningEvents()
            call.respond(queues)
        }

        get("/{eventId}") {
            val id = call.parameters["eventId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val event = eventRepository.getEventById(id.toInt()) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(event)
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
