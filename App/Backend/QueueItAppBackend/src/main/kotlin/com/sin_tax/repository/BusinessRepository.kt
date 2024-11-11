package com.sin_tax.repository

import com.sin_tax.model.Business
import com.sin_tax.model.EventCategory
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.context.GlobalContext

object Businesses : IntIdTable("businesses") {
    val name = varchar("name", 100)
    val address = varchar("address", 500)
    val category = enumeration<EventCategory>("category")
}

class BusinessEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<BusinessEntity>(Businesses)

    var name by Businesses.name
    var address by Businesses.address
    var category by Businesses.category
    val events by EventEntity referrersOn Events.business
}

private val userRepository by GlobalContext.get().inject<UserRepository>()

class BusinessRepository {

    suspend fun register(business: Business, userId: Int): Int {

        var businessEntity: BusinessEntity? = null
        dbQuery {
            businessEntity = BusinessEntity.new {
                this.name = business.name
                this.address = business.address
                this.category = business.category
            }
        }

        dbQuery {
            userRepository.registerBusiness(userId, businessEntity)
        }

        return businessEntity?.id?.value ?: 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}