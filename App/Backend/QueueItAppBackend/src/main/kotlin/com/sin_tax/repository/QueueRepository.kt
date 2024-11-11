package com.sin_tax.repository

import com.sin_tax.model.Queue
import com.sin_tax.repository.CustomerQueues.queueId
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.context.GlobalContext
import kotlin.math.max

object Queues : IntIdTable("queues") {
    val title = varchar("title", 50)
    val maxLimit = integer("max_limit")
    val event = reference("event", Events)
    val customers = reference("customers", Customers)
}

class QueueEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<QueueEntity>(Queues)

    var title by Queues.title
    var maxLimit by Queues.maxLimit
    var event by EventEntity referencedOn Queues.event
    var customers by CustomerEntity via CustomerQueues
}

fun QueueEntity.toQueue() = Queue(
    id = this.id.value,
    title = this.title,
    maxLimit = this.maxLimit,
)

class QueueRepository {

    private val eventRepository: EventRepository by GlobalContext.get().inject<EventRepository>()

    suspend fun create(queue: Queue, eventId: Int) {
        var event: EventEntity? = null
        dbQuery {
            event = eventRepository.getEventEntity(eventId) ?: throw IllegalStateException()
        }
        dbQuery {
            QueueEntity.new {
                this.title = queue.title
                this.maxLimit = queue.maxLimit
                this.event = event!!
            }
        }
    }

    suspend fun getAllEntries(queueId: Int) = dbQuery {
        val queue = QueueEntity.findById(queueId) ?: throw IllegalArgumentException()
        queue.customers.toList()
    }

    suspend fun addNewEntry(qId: Int, cId: Int) {
        dbQuery {
            CustomerQueues.insert {
                it[customerId] = cId
                it[queueId] = qId
            }
        }
    }

    suspend fun removeCustomer(qId: Int, cId: Int) {
        dbQuery {
            CustomerQueues.deleteWhere {
                (customerId eq cId) and (queueId eq qId)
            }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}