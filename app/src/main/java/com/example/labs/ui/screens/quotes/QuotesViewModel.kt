package com.example.labs.ui.screens.quotes


import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.labs.data.repository.QuotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import com.example.labs.data.model.Author
import com.example.labs.data.model.Quote
import com.example.labs.service.quotegen.QuoteService
import kotlin.random.Random

data class QuoteUiModel(
    val id: Long,
    val header: String,
    val content: String,
    val rating: Double,
    val authorName: String
)

// ... (QuoteUiModel остается без изменений)

class QuotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuotesRepository(application)
    private val quoteService = QuoteService() // Наш сервис для работы с OpenAI

    var quotes by mutableStateOf<List<QuoteUiModel>>(emptyList())
        private set

    // Состояние для отображения крутилки загрузки
    var isLoading by mutableStateOf(false)
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    // Новая функция для генерации и сохранения
    fun generateAndSaveNewQuote() {
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplication(), "Нет подключения к интернету", Toast.LENGTH_SHORT).show()
            return
        }
        if (isLoading) return // Защита от двойного клика

        viewModelScope.launch {
            isLoading = true
            try {
                // Можно сделать массив авторов и выбирать рандомного
                val authors = listOf("Джейсон Стэтхем", "Том Круз", "Альберт Эйнштейн", "Шрек")
                val selectedAuthor = authors.random()
                val seed = Random.nextInt(0, 10000)

                // 1. Генерируем цитату через LLM
                val generated = quoteService.generateQuote(selectedAuthor, seed)

                withContext(Dispatchers.IO) {
                    // 2. Ищем автора в БД. Если нет — создаем.
                    var author = repository.getAuthorByName(selectedAuthor)
                    val authorId: Long

                    if (author == null) {
                        // Предполагаю, что у вас дата-класс Author выглядит примерно так.
                        // Подправьте поля, если они отличаются.
                        val newAuthor = Author(name = selectedAuthor)
                        authorId = repository.insertAuthor(newAuthor)
                    } else {
                        authorId = author.id // Или как у вас называется поле ID в Author
                    }

                    // 3. Создаем и сохраняем новую цитату в БД
                    val newQuote = Quote(
                        authorId = authorId,
                        header = generated.title,
                        content = generated.text,
                        rating = 5.0, // Дефолтный рейтинг для новых шедевров
                        readTime = 1
                    )
                    repository.insertQuote(newQuote)
                }

                // 4. Обновляем список на экране
                loadQuotes()
            } catch (e: Exception) {
                e.printStackTrace()
                // Здесь можно добавить логику показа Snackbar с ошибкой
            } finally {
                isLoading = false
            }
        }
    }
}