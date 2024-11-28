package com.sin_tax.repository

import org.jetbrains.exposed.sql.Table

object CustomerQueues : Table() {
    val customerId = reference("customerId", Customers)
    val queueId = reference("queueId", Queues)

    override val primaryKey = PrimaryKey(customerId, queueId)
}