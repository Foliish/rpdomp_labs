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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.example.labs.data.repository.QuotesRepository
import com.example.labs.service.ConnectivityObserver.ConnectivityObserver
import com.example.labs.service.ConnectivityObserver.NetworkConnectivityObserver
import com.example.labs.service.quotegen.QuoteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine
import kotlin.random.Random
import com.example.labs.util.FuzzySearchUtil

enum class SortOrder {
    RATING_DESC, AUTHOR_ASC, TITLE_ASC
}

data class QuoteUiModel(
    val id: Long,
    val header: String,
    val content: String,
    val rating: Double,
    val authorName: String
)

class QuotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuotesRepository(application)
    private val firestoreRepository = com.example.labs.data.repository.FirestoreRepository()
    private val quoteService = QuoteService()

    private val connectivityObserver = NetworkConnectivityObserver(application)

    var quotes by mutableStateOf<List<QuoteUiModel>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
    var sortOrder by mutableStateOf(SortOrder.RATING_DESC)

    val filteredAndSortedQuotes: List<QuoteUiModel>
        get() = quotes
            .filter { quote ->
                if (searchQuery.isBlank()) true
                else {
                    FuzzySearchUtil.fuzzyMatch(searchQuery, quote.header) ||
                    FuzzySearchUtil.fuzzyMatch(searchQuery, quote.content) ||
                    FuzzySearchUtil.fuzzyMatch(searchQuery, quote.authorName)
                }
            }
            .let { list ->
                when (sortOrder) {
                    SortOrder.RATING_DESC -> list.sortedByDescending { it.rating }
                    SortOrder.AUTHOR_ASC -> list.sortedBy { it.authorName }
                    SortOrder.TITLE_ASC -> list.sortedBy { it.header }
                }
            }

    init {
        observeNetworkChanges()
        loadQuotes()
    }

    private fun observeNetworkChanges() {
        connectivityObserver.observe().onEach { status ->
            isOnline = status == ConnectivityObserver.Status.Available
        }.launchIn(viewModelScope)
    }

    private var quotesJob: Job? = null

    fun loadQuotes() {
        quotesJob?.cancel()
        quotesJob = viewModelScope.launch(Dispatchers.IO) {
            var retryCount = 0
            while (true) {
                try {
                    combine(
                        firestoreRepository.getQuotesFlow(),
                        firestoreRepository.getAuthorsFlow()
                    ) { firestoreQuotes, firestoreAuthors ->
                        retryCount = 0 // Reset retry count on successful emission
                        
                        firestoreAuthors.forEach { repository.insertOrReplaceAuthor(it) }
                        firestoreQuotes.forEach { repository.insertOrReplaceQuote(it) }

                        val authorMap = firestoreAuthors.associateBy { it.id }
                        firestoreQuotes.map { quote ->
                            val author = authorMap[quote.authorId]
                            QuoteUiModel(
                                id = quote.id,
                                header = quote.header,
                                content = quote.content,
                                rating = quote.rating,
                                authorName = author?.name ?: "Unknown"
                            )
                        }
                    }.collect { uiModels ->
                        withContext(Dispatchers.Main) {
                            quotes = uiModels
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    retryCount++
                    delay(run {
                        val wait = (1000L * retryCount).coerceAtMost(30000L)
                        wait
                    })
                }
            }
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
                    newQuote.id = repository.insertQuote(newQuote)

                    val authorToSync = author ?: Author(id = authorId, name = selectedAuthor)
                    firestoreRepository.saveAuthor(authorToSync, {}, {})
                    firestoreRepository.saveQuote(newQuote, {}, {})
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