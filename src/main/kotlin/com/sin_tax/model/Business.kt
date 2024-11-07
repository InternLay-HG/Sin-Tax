package com.sin_tax.model

import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val name: String,
    val address: String,
    val category: EventCategory
)
