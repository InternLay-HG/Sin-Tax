package repository

import com.sin_tax.model.Business
import com.sin_tax.model.Customer
import com.sin_tax.model.Gender
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import repository.Users.business

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
}

class CustomerRepository {
    suspend fun register(customer: Customer, userId: Int) {

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
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}