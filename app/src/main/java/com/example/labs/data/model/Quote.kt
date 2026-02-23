package com.example.labs.data.model

data class Quote(
    var id: Long = 0,
    var authorId: Long = 0,
    var header: String = "",
    var content: String = "",
    var rating: Double = 0.0,
    var readTime: Int = 0
)
