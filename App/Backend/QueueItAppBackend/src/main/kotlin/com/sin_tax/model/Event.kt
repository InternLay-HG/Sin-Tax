package com.sin_tax.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val title: String,
    val description: String,
    val durationInMinutes: Int,
    val category: EventCategory,
    val waitTime: Int
)
