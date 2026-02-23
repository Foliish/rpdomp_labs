package com.example.labs.data.model

data class Author(
    var id: Long = 0,
    var name: String = "",
    var rating: Double = 0.0,
    var lastQuoteId: Long? = null
)
