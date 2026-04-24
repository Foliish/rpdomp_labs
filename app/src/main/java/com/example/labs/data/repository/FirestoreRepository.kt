package com.example.labs.data.repository

import android.util.Log
import com.example.labs.data.model.Author
import com.example.labs.data.model.Quote
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun saveAuthor(author: Author, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("authors")
            .document(author.id.toString())
            .set(author)
            .addOnSuccessListener {
                Log.d("FirestoreRepo", "Author successfully saved!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreRepo", "Error saving author", e)
                onFailure(e)
            }
    }

    fun saveQuote(quote: Quote, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("quotes")
            .document(quote.id.toString())
            .set(quote)
            .addOnSuccessListener {
                Log.d("FirestoreRepo", "Quote successfully saved!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreRepo", "Error saving quote", e)
                onFailure(e)
            }
    }

    fun getQuotesFlow(): kotlinx.coroutines.flow.Flow<List<Quote>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("quotes")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val quotesList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Quote::class.java)
                } ?: emptyList()
                trySend(quotesList)
            }
        awaitClose { listener.remove() }
    }

    fun getAuthorsFlow(): kotlinx.coroutines.flow.Flow<List<Author>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("authors")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val authorsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Author::class.java)
                } ?: emptyList()
                trySend(authorsList)
            }
        awaitClose { listener.remove() }
    }
}
