package com.sin_tax.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = -1,
    val email: String,
    val password: String,
    val phoneNo: Long
)