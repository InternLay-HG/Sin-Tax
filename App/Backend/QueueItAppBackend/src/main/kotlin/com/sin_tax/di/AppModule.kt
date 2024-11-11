package com.sin_tax.di

import com.sin_tax.repository.*
import org.koin.dsl.module

val appModule = module {
    single { UserRepository() }
    single { BusinessRepository() }
    single { CustomerRepository() }
    single { EventRepository() }
    single { QueueRepository() }
}