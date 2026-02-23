package com.example.labs.data.dao

// QuoteDao.kt
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.labs.data.database.DatabaseHelper
import com.example.labs.data.model.Quote

class QuoteDao(private val dbHelper: DatabaseHelper) {

    fun insertQuote(quote: Quote): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID, quote.authorId)
            put(DatabaseHelper.COLUMN_QUOTE_HEADER, quote.header)
            put(DatabaseHelper.COLUMN_QUOTE_CONTENT, quote.content)
            put(DatabaseHelper.COLUMN_QUOTE_RATING, quote.rating)
            put(DatabaseHelper.COLUMN_QUOTE_READ_TIME, quote.readTime)
        }

        val id = db.insert(DatabaseHelper.TABLE_QUOTES, null, values)
        db.close()
        quote.id = id
        return id
    }

    fun insertQuoteWithAuthorUpdate(quote: Quote, authorDao: AuthorDao): Long {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID, quote.authorId)
                put(DatabaseHelper.COLUMN_QUOTE_HEADER, quote.header)
                put(DatabaseHelper.COLUMN_QUOTE_CONTENT, quote.content)
                put(DatabaseHelper.COLUMN_QUOTE_RATING, quote.rating)
                put(DatabaseHelper.COLUMN_QUOTE_READ_TIME, quote.readTime)
            }

            val quoteId = db.insert(DatabaseHelper.TABLE_QUOTES, null, values)

            val authorValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID, quoteId)
            }

            db.update(
                DatabaseHelper.TABLE_AUTHORS,
                authorValues,
                "${DatabaseHelper.COLUMN_AUTHOR_ID} = ?",
                arrayOf(quote.authorId.toString())
            )

            db.setTransactionSuccessful()
            quote.id = quoteId
            return quoteId
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun insertQuotes(quotes: List<Quote>): List<Long> {
        val db = dbHelper.writableDatabase
        val ids = mutableListOf<Long>()

        db.beginTransaction()
        try {
            quotes.forEach { quote ->
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID, quote.authorId)
                    put(DatabaseHelper.COLUMN_QUOTE_HEADER, quote.header)
                    put(DatabaseHelper.COLUMN_QUOTE_CONTENT, quote.content)
                    put(DatabaseHelper.COLUMN_QUOTE_RATING, quote.rating)
                    put(DatabaseHelper.COLUMN_QUOTE_READ_TIME, quote.readTime)
                }
                val id = db.insert(DatabaseHelper.TABLE_QUOTES, null, values)
                ids.add(id)
                quote.id = id
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
        return ids
    }

    fun getQuoteById(id: Long): Quote? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_QUOTES,
            null,
            "${DatabaseHelper.COLUMN_QUOTE_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var quote: Quote? = null
        if (cursor.moveToFirst()) {
            quote = cursorToQuote(cursor)
        }
        cursor.close()
        db.close()
        return quote
    }

    fun getAllQuotes(): List<Quote> {
        val quotes = mutableListOf<Quote>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_QUOTES,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_QUOTE_ID} DESC"
        )

        while (cursor.moveToNext()) {
            quotes.add(cursorToQuote(cursor))
        }
        cursor.close()
        db.close()
        return quotes
    }

    fun getQuotesByAuthor(authorId: Long): List<Quote> {
        val quotes = mutableListOf<Quote>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_QUOTES,
            null,
            "${DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID} = ?",
            arrayOf(authorId.toString()),
            null,
            null,
            "${DatabaseHelper.COLUMN_QUOTE_RATING} DESC"
        )

        while (cursor.moveToNext()) {
            quotes.add(cursorToQuote(cursor))
        }
        cursor.close()
        db.close()
        return quotes
    }

    fun getTopRatedQuotes(limit: Int = 10): List<Quote> {
        val quotes = mutableListOf<Quote>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_QUOTES,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_QUOTE_RATING} DESC",
            limit.toString()
        )

        while (cursor.moveToNext()) {
            quotes.add(cursorToQuote(cursor))
        }
        cursor.close()
        db.close()
        return quotes
    }

    fun searchQuotesByHeader(searchQuery: String): List<Quote> {
        val quotes = mutableListOf<Quote>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_QUOTES,
            null,
            "${DatabaseHelper.COLUMN_QUOTE_HEADER} LIKE ?",
            arrayOf("%$searchQuery%"),
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            quotes.add(cursorToQuote(cursor))
        }
        cursor.close()
        db.close()
        return quotes
    }

    fun updateQuote(quote: Quote): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID, quote.authorId)
            put(DatabaseHelper.COLUMN_QUOTE_HEADER, quote.header)
            put(DatabaseHelper.COLUMN_QUOTE_CONTENT, quote.content)
            put(DatabaseHelper.COLUMN_QUOTE_RATING, quote.rating)
            put(DatabaseHelper.COLUMN_QUOTE_READ_TIME, quote.readTime)
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_QUOTES,
            values,
            "${DatabaseHelper.COLUMN_QUOTE_ID} = ?",
            arrayOf(quote.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun updateQuoteRating(id: Long, newRating: Double): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_QUOTE_RATING, newRating)
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_QUOTES,
            values,
            "${DatabaseHelper.COLUMN_QUOTE_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteQuote(quote: Quote): Int {
        return deleteQuoteById(quote.id)
    }

    fun deleteQuoteById(id: Long): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(
            DatabaseHelper.TABLE_QUOTES,
            "${DatabaseHelper.COLUMN_QUOTE_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteQuotesByAuthor(authorId: Long): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(
            DatabaseHelper.TABLE_QUOTES,
            "${DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID} = ?",
            arrayOf(authorId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteAllQuotes(): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(DatabaseHelper.TABLE_QUOTES, null, null)
        db.close()
        return rowsAffected
    }

    private fun cursorToQuote(cursor: Cursor): Quote {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUOTE_ID))
        val authorId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID))
        val header = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUOTE_HEADER))
        val content = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUOTE_CONTENT))
        val rating = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUOTE_RATING))
        val readTime = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUOTE_READ_TIME))

        return Quote(id, authorId, header, content, rating, readTime)
    }

    fun getQuotesCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_QUOTES}", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun getQuotesCountByAuthor(authorId: Long): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_QUOTES,
            arrayOf("COUNT(*)"),
            "${DatabaseHelper.COLUMN_QUOTE_AUTHOR_ID} = ?",
            arrayOf(authorId.toString()),
            null,
            null,
            null
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun getAverageQuoteRating(): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT AVG(${DatabaseHelper.COLUMN_QUOTE_RATING}) FROM ${DatabaseHelper.TABLE_QUOTES}", null)
        var avgRating = 0.0
        if (cursor.moveToFirst()) {
            avgRating = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return avgRating
    }

    fun getAverageReadTime(): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT AVG(${DatabaseHelper.COLUMN_QUOTE_READ_TIME}) FROM ${DatabaseHelper.TABLE_QUOTES}", null)
        var avgTime = 0.0
        if (cursor.moveToFirst()) {
            avgTime = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return avgTime
    }
}