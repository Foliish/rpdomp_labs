package com.example.labs.ui.screens.quotedetail


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.labs.data.model.Author
import com.example.labs.data.model.Quote
import com.example.labs.data.repository.QuotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*

data class QuoteDetailUiState(
    val header: String = "",
    val content: String = "",
    val rating: Double = 0.0,
    val authorName: String = ""
)

class QuoteDetailViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = QuotesRepository(application)

    var uiState by mutableStateOf(QuoteDetailUiState())
        private set

    fun loadQuote(id: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {

                val quote: Quote? = repository.getQuoteById(id)
                val author: Author? =
                    quote?.let { repository.getAuthorById(it.authorId) }

                if (quote != null) {
                    QuoteDetailUiState(
                        header = quote.header,
                        content = quote.content,
                        rating = quote.rating,
                        authorName = author?.name ?: "Unknown"
                    )
                } else {
                    QuoteDetailUiState()
                }
            }

            uiState = result
        }
    }
}