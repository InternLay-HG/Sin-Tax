package com.sin_tax.di

import org.koin.dsl.module
import repository.BusinessRepository
import repository.UserRepository

val appModule = module {
    single { UserRepository() }
    single { BusinessRepository() }
}