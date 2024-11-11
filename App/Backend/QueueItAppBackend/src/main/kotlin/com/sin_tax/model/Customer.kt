package com.sin_tax.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: Int = -1,
    val name: String,
    val age: Int,
    val gender: Gender
)