package com.sin_tax.repository

import com.sin_tax.model.Event
import com.sin_tax.model.EventCategory
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

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

fun EventEntity.mapToEvent() = Event(
    id = this.id.value,
    title = this.title,
    description = this.description,
    category = this.category,
    waitTime = this.waitTime,
    startTime = this.openingTime.millis,
    endTime = this.closingTime.millis
)

fun unixToJoda(timestamp: Long): DateTime {
    val timeInMillis = timestamp * 1000
    return DateTime(timeInMillis, DateTimeZone.forID("Asia/Kolkata"))
}

class EventRepository {
    val userRepository: UserRepository = UserRepository()
    val businessRepository: BusinessRepository = BusinessRepository()
    suspend fun create(event: Event, business: BusinessEntity): Int {
        return dbQuery {
            EventEntity.new {
                this.title = event.title
                this.description = event.description
                this.openingTime = unixToJoda(event.startTime)
                this.closingTime = unixToJoda(event.endTime)
                this.business = business
                this.category = event.category
                this.waitTime = event.waitTime
            }
        }.id.value
    }

    suspend fun getEventEntity(id: Int) = dbQuery {
        EventEntity.findById(id)
    }

    suspend fun filterRunningEvents() = dbQuery {
        val currentTimeMillis = System.currentTimeMillis()
        val jodaCurrentTime = DateTime(currentTimeMillis)
        EventEntity.find {
            (Events.openingTime lessEq jodaCurrentTime) and (Events.closingTime greaterEq jodaCurrentTime)
        }.map { it.mapToEvent() }.toList()
    }

    suspend fun getEventsForBusiness(businessId: Int) = dbQuery {
        EventEntity.find {
            Events.business eq businessId
        }.map { it.mapToEvent()}.toList()
    }

    suspend fun getEventById(eventId: Int) = dbQuery {
        EventEntity.findById(eventId)?.mapToEvent()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}