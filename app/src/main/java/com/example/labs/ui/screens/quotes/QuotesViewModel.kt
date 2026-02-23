package com.example.labs.ui.screens.quotes


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.labs.data.repository.QuotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*

data class QuoteUiModel(
    val id: Long,
    val header: String,
    val content: String,
    val rating: Double,
    val authorName: String
)

class QuotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuotesRepository(application)

    var quotes by mutableStateOf<List<QuoteUiModel>>(emptyList())
        private set

    init {
        loadQuotes()
    }

    fun loadQuotes() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getAllQuotes().map { quote ->
                    val author = repository.getAuthorById(quote.authorId)
                    QuoteUiModel(
                        id = quote.id,
                        header = quote.header,
                        content = quote.content,
                        rating = quote.rating,
                        authorName = author?.name ?: "Unknown"
                    )
                }
            }
            quotes = result
        }
    }
}