package com.sin_tax.routes

import com.sin_tax.model.Queue
import com.sin_tax.repository.QueueRepository
import com.sin_tax.repository.Queues
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Routing.queueRoutes() {

    transaction {
        SchemaUtils.create(Queues)
    }

//    val queueRepository: QueueRepository by inject()
    val queueRepository = QueueRepository()

    route("/event") {

        authenticate("business-jwt") {
            post("/createQueue") {
                val newQueue = call.receive<Queue>()
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val eventId = principal.getClaim("eventId", String::class) ?: return@post call.respond(HttpStatusCode.Forbidden)
                queueRepository.create(newQueue, eventId.toInt())
                call.respond(HttpStatusCode.OK)
            }
        }

//        post("/add/{queueId}/{customerId}") {
//            val queueId = call.parameters["queueId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
//            val customerId = call.parameters["customerId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
//
//            queueRepository.addNewEntry(queueId.toInt(), customerId.toInt())
//            call.respond(HttpStatusCode.OK)
//        }
//
//        post("/remove/{queueId}/{customerId}") {
//            val queueId = call.parameters["queueId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
//            val customerId = call.parameters["customerId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
//
//            queueRepository.removeCustomer(queueId.toInt(), customerId.toInt())
//            call.respond(HttpStatusCode.OK)
//        }

        val sessions = Collections.synchronizedList<WebSocketServerSession>(ArrayList())

        webSocket("/connect/{queueId}") {
            sessions.add(this)
            val queueId = call.parameters["queueId"]?.toInt() ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
            val customers = queueRepository.getAllEntries(queueId)

            for (customer in customers) {
                sendSerialized(customer)
            }

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val msg = frame.readText()
                    if (msg[0] == 'A') {
                        val customerId = msg.substringAfter("ADD:")
                        queueRepository.addNewEntry(queueId, customerId.toInt())
                        outgoing.send(Frame.Text(customerId))
                    }

                    else if (msg[0] == 'R') {
                        val customerId = msg.substringAfter("REMOVE:")
                        queueRepository.removeCustomer(queueId, customerId.toInt())
                        outgoing.send(Frame.Text(customerId))
                    }
                }
            }
        }

        /*
        routing {
        val sessions =
            Collections.synchronizedList<WebSocketServerSession>(ArrayList())

        webSocket("/tasks") {
            sendAllTasks()
            close(CloseReason(CloseReason.Codes.NORMAL, "All done"))
        }

        webSocket("/tasks2") {
            sessions.add(this)
            sendAllTasks()

            while(true) {
                val newTask = receiveDeserialized<Task>()
                TaskRepository.addTask(newTask)
                for(session in sessions) {
                    session.sendSerialized(newTask)
                }
            }
        }
         */
    }
}