package repository

import com.sin_tax.model.Customer
import com.sin_tax.model.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object Users : IntIdTable("users") {

    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 15)
    val phoneNo = long("phoneNo")
    val business = reference("business", Businesses).nullable()
    val customer = reference("customer", Customers).nullable()
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<UserEntity>(Users)

    var email by Users.email
    var password by Users.password
    var phoneNo by Users.phoneNo
    var business by BusinessEntity optionalReferencedOn Users.business
    var customer by CustomerEntity optionalReferencedOn Users.customer
}

fun UserEntity.mapToUser() = User(
    email = this.email,
    password = this.password,
    phoneNo = this.phoneNo
)

class UserRepository {
    suspend fun create(user: User) = dbQuery {
        UserEntity.new {
            this.email = user.email
            this.password = user.password
            this.phoneNo = user.phoneNo
            this.business = null
        }.id.value
    }

    suspend fun getUser(userId: Int): User? = dbQuery {
        UserEntity.findById(userId)?.mapToUser()
    }

    suspend fun getUserEntity(userId: Int): UserEntity? = dbQuery {
        UserEntity.findById(userId)
    }

    suspend fun registerBusiness(userId: Int, businessEntity: BusinessEntity?) = dbQuery {
        val userEntity = UserEntity.findById(userId)
        if (userEntity != null) {
            userEntity.business = businessEntity
            userEntity.flush()
        }
    }

    suspend fun registerCustomer(userId: Int, customerEntity: CustomerEntity?) = dbQuery {
        val userEntity = UserEntity.findById(userId)
        if (userEntity != null) {
            userEntity.customer = customerEntity
            userEntity.flush()
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}