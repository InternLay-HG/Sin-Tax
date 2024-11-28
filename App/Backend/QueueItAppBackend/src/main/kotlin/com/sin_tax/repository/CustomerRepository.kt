package com.sin_tax.repository

import com.sin_tax.model.Customer
import com.sin_tax.model.Gender
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.context.GlobalContext

object Customers : IntIdTable("customers") {
    var name = varchar("name", 50)
    var age = integer("age")
    var gender = enumeration<Gender>("gender")
}

class CustomerEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<CustomerEntity>(Customers)

    var name by Customers.name
    var age by Customers.age
    var gender by Customers.gender
    var queues by QueueEntity via CustomerQueues
}

fun CustomerEntity.mapToCustomer() = Customer(
    age = this.age,
    id = this.id.value,
    name = this.name,
    gender = this.gender
)

class CustomerRepository {
    private val userRepository: UserRepository by GlobalContext.get().inject<UserRepository>()
    suspend fun register(customer: Customer, userId: Int): Int {

        var customerEntity: CustomerEntity? = null
        dbQuery {
            customerEntity = CustomerEntity.new {
                this.name = customer.name
                this.age = customer.age
                this.gender = customer.gender
            }
        }

        dbQuery {
            userRepository.registerCustomer(userId, customerEntity)
        }

        return customerEntity!!.id.value
    }

    suspend fun getCustomerById(customerId: Int) = dbQuery {
        CustomerEntity.findById(customerId)?.mapToCustomer()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}