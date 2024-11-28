package com.sin_tax.routes

import com.sin_tax.model.Queue
import com.sin_tax.repository.CustomerQueues
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
        SchemaUtils.create(Queues, CustomerQueues)
    }

//    val queueRepository: QueueRepository by inject()
    val queueRepository = QueueRepository()

    route("/queue") {

        post("/create/{eventId}") {
            val eventId = call.parameters["eventId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val newQueue = call.receive<Queue>()
            queueRepository.create(newQueue, eventId.toInt())
            call.respond(HttpStatusCode.OK)
        }

        val sessions = Collections.synchronizedList<WebSocketServerSession>(ArrayList())

        webSocket("/connect/{queueId}") {
            sessions.add(this)
            val queueId =
                call.parameters["queueId"]?.toInt() ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
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
                        for (session in sessions)
                            outgoing.send(Frame.Text(customerId))
                    } else if (msg[0] == 'R') {
                        val customerId = msg.substringAfter("REMOVE:")
                        queueRepository.removeCustomer(queueId, customerId.toInt())
                        for (session in sessions)
                            outgoing.send(Frame.Text(customerId))
                    }
                }
            }
        }

        get("/running/all") {
            val queues = queueRepository.getCurrentQueues()

            call.respond(queues)
        }

        get("/{queueId}") {
            val id = call.parameters["queueId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val event = queueRepository.getQueueById(id.toInt()) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(event)
        }

        get("/{eventId}/all") {
            val eventId = call.parameters["eventId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val queues = queueRepository.getQueuesForEvent(eventId.toInt())
            call.respond(queues)
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