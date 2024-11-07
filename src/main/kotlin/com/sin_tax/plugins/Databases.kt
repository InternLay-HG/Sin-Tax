package com.sin_tax.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/queue_it_db",
        user = "postgres",
        driver = "org.postgresql.Driver",
        password = "abhinav99_pg",
    )
}
