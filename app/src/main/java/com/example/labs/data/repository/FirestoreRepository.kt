package com.example.labs.data.repository

import android.util.Log
import com.example.labs.data.model.Author
import com.example.labs.data.model.Quote
import com.google.firebase.firestore.FirebaseFirestore

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
}
