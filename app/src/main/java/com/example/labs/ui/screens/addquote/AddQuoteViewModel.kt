package com.example.labs.ui.screens.addquote

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

class AddQuoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuotesRepository(application)

    var header by mutableStateOf("")
    var content by mutableStateOf("")
    var authorName by mutableStateOf("")
    var rating by mutableStateOf(0f)

    fun saveQuote(onSaved: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val trimmedName = authorName.trim()

                if (trimmedName.isEmpty()) return@withContext

                val existingAuthor = repository.getAuthorByName(trimmedName)

                val authorId = if (existingAuthor != null) {
                    existingAuthor.id
                } else {
                    repository.insertAuthor(
                        Author(
                            name = trimmedName,
                            rating = rating.toDouble()
                        )
                    )
                }

                repository.insertQuote(
                    Quote(
                        authorId = authorId,
                        header = header.trim(),
                        content = content.trim(),
                        rating = rating.toDouble(),
                        readTime = content.length / 10
                    )
                )
            }

            onSaved()
        }
    }
}