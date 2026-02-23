package com.example.labs.data.repository

import android.content.Context
import com.example.labs.data.dao.AuthorDao
import com.example.labs.data.dao.QuoteDao
import com.example.labs.data.database.DatabaseHelper
import com.example.labs.data.model.Author
import com.example.labs.data.model.Quote

class QuotesRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val authorDao = AuthorDao(dbHelper)
    private val quoteDao = QuoteDao(dbHelper)

    fun insertAuthor(author: Author): Long = authorDao.insertAuthor(author)
    fun getAuthorById(id: Long): Author? = authorDao.getAuthorById(id)
    fun getAllAuthors(): List<Author> = authorDao.getAllAuthors()
    fun updateAuthor(author: Author): Int = authorDao.updateAuthor(author)
    fun deleteAuthor(author: Author): Int = authorDao.deleteAuthor(author)
    fun deleteAuthorById(id: Long): Int = authorDao.deleteAuthorById(id)
    fun getAuthorsCount(): Int = authorDao.getAuthorsCount()
    fun getAuthorByName(name: String): Author? {
        return getAllAuthors().firstOrNull {
            it.name.equals(name.trim(), ignoreCase = true)
        }
    }

    fun insertQuote(quote: Quote): Long = quoteDao.insertQuote(quote)
    fun insertQuoteWithAuthorUpdate(quote: Quote): Long =
        quoteDao.insertQuoteWithAuthorUpdate(quote, authorDao)
    fun getQuoteById(id: Long): Quote? = quoteDao.getQuoteById(id)
    fun getAllQuotes(): List<Quote> = quoteDao.getAllQuotes()
    fun getQuotesByAuthor(authorId: Long): List<Quote> = quoteDao.getQuotesByAuthor(authorId)
    fun updateQuote(quote: Quote): Int = quoteDao.updateQuote(quote)
    fun updateQuoteRating(id: Long, newRating: Double): Int =
        quoteDao.updateQuoteRating(id, newRating)
    fun deleteQuote(quote: Quote): Int = quoteDao.deleteQuote(quote)
    fun deleteQuoteById(id: Long): Int = quoteDao.deleteQuoteById(id)
    fun getQuotesCount(): Int = quoteDao.getQuotesCount()
    fun getTopRatedQuotes(limit: Int = 10): List<Quote> = quoteDao.getTopRatedQuotes(limit)

    fun getAuthorWithQuotes(authorId: Long): Pair<Author?, List<Quote>> {
        val author = authorDao.getAuthorById(authorId)
        val quotes = quoteDao.getQuotesByAuthor(authorId)
        return Pair(author, quotes)
    }

    fun deleteAuthorCascade(authorId: Long): Boolean {
        return try {
            quoteDao.deleteQuotesByAuthor(authorId)
            authorDao.deleteAuthorById(authorId)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun close() {
        dbHelper.close()
    }
}