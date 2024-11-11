package com.sin_tax.repository

import com.sin_tax.model.Event
import com.sin_tax.model.EventCategory
import com.sin_tax.routes.dbQuery
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

object Events : IntIdTable("events") {
    val title = varchar("title", 50)
    val description = text("description")
    val openingTime = datetime("opening_time")
    val closingTime = datetime("closing_time")
    val category = enumeration<EventCategory>("category")
    val business = reference("business", Businesses)
    val waitTime = integer("wait_time")
}

class EventEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<EventEntity>(Events)

    var title by Events.title
    var description by Events.description
    var openingTime by Events.openingTime
    var closingTime by Events.closingTime
    var category by Events.category
    var business by BusinessEntity referencedOn Events.business
    var waitTime by Events.waitTime
}

class EventRepository {
    suspend fun create(event: Event, business: BusinessEntity): Int {
        return dbQuery {
            EventEntity.new {
                this.title = event.title
                this.description = event.description
                this.openingTime = DateTime.now()
                this.closingTime = DateTime.now().plusMinutes(event.durationInMinutes)
                this.business = business
                this.category = event.category
                this.waitTime = event.waitTime
            }
        }.id.value
    }

    suspend fun getEventEntity(id: Int) = dbQuery {
        EventEntity.findById(id)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}