package com.example.labs.ui.screens.quotes

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.labs.data.model.Author
import com.example.labs.data.model.Quote
import com.example.labs.data.repository.QuotesRepository
import com.example.labs.service.ConnectivityObserver.ConnectivityObserver
import com.example.labs.service.ConnectivityObserver.NetworkConnectivityObserver
import com.example.labs.service.quotegen.QuoteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class QuoteUiModel(
    val id: Long,
    val header: String,
    val content: String,
    val rating: Double,
    val authorName: String
)

class QuotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuotesRepository(application)
    private val quoteService = QuoteService()

    private val connectivityObserver = NetworkConnectivityObserver(application)

    var quotes by mutableStateOf<List<QuoteUiModel>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isOnline by mutableStateOf(false)
        private set

    init {
        observeNetworkChanges()
        loadQuotes()
    }

    private fun observeNetworkChanges() {
        connectivityObserver.observe().onEach { status ->
            isOnline = status == ConnectivityObserver.Status.Available
        }.launchIn(viewModelScope)
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

    fun generateAndSaveNewQuote() {
        if (!isOnline) {
            Toast.makeText(getApplication(), "Нет подключения к интернету", Toast.LENGTH_SHORT).show()
            return
        }

        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            try {
                val authors = listOf("Джейсон Стэтхем", "Том Круз", "Альберт Эйнштейн", "Шрек")
                val selectedAuthor = authors.random()
                val seed = Random.nextInt(0, 10000)

                val generated = quoteService.generateQuote(selectedAuthor, seed)

                withContext(Dispatchers.IO) {
                    var author = repository.getAuthorByName(selectedAuthor)
                    val authorId: Long

                    if (author == null) {
                        val newAuthor = Author(name = selectedAuthor)
                        authorId = repository.insertAuthor(newAuthor)
                    } else {
                        authorId = author.id
                    }

                    val newQuote = Quote(
                        authorId = authorId,
                        header = generated.title,
                        content = generated.text,
                        rating = 5.0,
                        readTime = 1
                    )
                    repository.insertQuote(newQuote)
                }

                loadQuotes()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Ошибка при получении цитаты", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}