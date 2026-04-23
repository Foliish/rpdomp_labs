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
    private val firestoreRepository = com.example.labs.data.repository.FirestoreRepository()

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

                val author = if (existingAuthor != null) {
                    existingAuthor
                } else {
                    val newAuthor = Author(
                        name = trimmedName,
                        rating = rating.toDouble()
                    )
                    newAuthor.id = repository.insertAuthor(newAuthor)
                    newAuthor
                }

                val quote = Quote(
                    authorId = author.id,
                    header = header.trim(),
                    content = content.trim(),
                    rating = rating.toDouble(),
                    readTime = content.length / 10
                )
                quote.id = repository.insertQuote(quote)

                // Sync to Firestore
                firestoreRepository.saveAuthor(author, onSuccess = {}, onFailure = {})
                firestoreRepository.saveQuote(quote, onSuccess = {}, onFailure = {})
            }

            onSaved()
        }
    }
}