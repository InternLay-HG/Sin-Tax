package com.sin_tax

import com.sin_tax.di.configureKoin
import com.sin_tax.plugins.*
import com.sin_tax.routes.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureRouting()
    configureKoin()
}
