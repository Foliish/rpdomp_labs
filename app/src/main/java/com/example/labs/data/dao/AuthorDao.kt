package com.example.labs.data.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.labs.data.database.DatabaseHelper
import com.example.labs.data.model.Author

class AuthorDao(private val dbHelper: DatabaseHelper) {

    fun insertAuthor(author: Author): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_AUTHOR_NAME, author.name)
            put(DatabaseHelper.COLUMN_AUTHOR_RATING, author.rating)
            author.lastQuoteId?.let {
                put(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID, it)
            }
        }

        val id = db.insert(DatabaseHelper.TABLE_AUTHORS, null, values)
        db.close()
        author.id = id
        return id
    }

    fun insertAuthors(authors: List<Author>): List<Long> {
        val db = dbHelper.writableDatabase
        val ids = mutableListOf<Long>()

        db.beginTransaction()
        try {
            authors.forEach { author ->
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_AUTHOR_NAME, author.name)
                    put(DatabaseHelper.COLUMN_AUTHOR_RATING, author.rating)
                    author.lastQuoteId?.let {
                        put(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID, it)
                    }
                }
                val id = db.insert(DatabaseHelper.TABLE_AUTHORS, null, values)
                ids.add(id)
                author.id = id
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
        return ids
    }

    fun getAuthorById(id: Long): Author? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_AUTHORS,
            null,
            "${DatabaseHelper.COLUMN_AUTHOR_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var author: Author? = null
        if (cursor.moveToFirst()) {
            author = cursorToAuthor(cursor)
        }
        cursor.close()
        db.close()
        return author
    }

    fun getAllAuthors(): List<Author> {
        val authors = mutableListOf<Author>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_AUTHORS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_AUTHOR_NAME} ASC"
        )

        while (cursor.moveToNext()) {
            authors.add(cursorToAuthor(cursor))
        }
        cursor.close()
        db.close()
        return authors
    }

    fun getAuthorsByRating(minRating: Double): List<Author> {
        val authors = mutableListOf<Author>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_AUTHORS,
            null,
            "${DatabaseHelper.COLUMN_AUTHOR_RATING} >= ?",
            arrayOf(minRating.toString()),
            null,
            null,
            "${DatabaseHelper.COLUMN_AUTHOR_RATING} DESC"
        )

        while (cursor.moveToNext()) {
            authors.add(cursorToAuthor(cursor))
        }
        cursor.close()
        db.close()
        return authors
    }

    fun updateAuthor(author: Author): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_AUTHOR_NAME, author.name)
            put(DatabaseHelper.COLUMN_AUTHOR_RATING, author.rating)
            if (author.lastQuoteId != null) {
                put(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID, author.lastQuoteId)
            } else {
                putNull(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID)
            }
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_AUTHORS,
            values,
            "${DatabaseHelper.COLUMN_AUTHOR_ID} = ?",
            arrayOf(author.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun updateAuthorRating(id: Long, newRating: Double): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_AUTHOR_RATING, newRating)
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_AUTHORS,
            values,
            "${DatabaseHelper.COLUMN_AUTHOR_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun updateLastQuoteId(authorId: Long, quoteId: Long?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues()

        if (quoteId != null) {
            values.put(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID, quoteId)
        } else {
            values.putNull(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID)
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_AUTHORS,
            values,
            "${DatabaseHelper.COLUMN_AUTHOR_ID} = ?",
            arrayOf(authorId.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteAuthor(author: Author): Int {
        return deleteAuthorById(author.id)
    }

    fun deleteAuthorById(id: Long): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(
            DatabaseHelper.TABLE_AUTHORS,
            "${DatabaseHelper.COLUMN_AUTHOR_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteAllAuthors(): Int {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(DatabaseHelper.TABLE_AUTHORS, null, null)
        db.close()
        return rowsAffected
    }

    private fun cursorToAuthor(cursor: Cursor): Author {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTHOR_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTHOR_NAME))
        val rating = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTHOR_RATING))

        val lastQuoteIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_AUTHOR_LAST_QUOTE_ID)
        val lastQuoteId = if (!cursor.isNull(lastQuoteIdIndex)) {
            cursor.getLong(lastQuoteIdIndex)
        } else {
            null
        }

        return Author(id, name, rating, lastQuoteId)
    }

    fun getAuthorsCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_AUTHORS}", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun getAverageAuthorRating(): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT AVG(${DatabaseHelper.COLUMN_AUTHOR_RATING}) FROM ${DatabaseHelper.TABLE_AUTHORS}", null)
        var avgRating = 0.0
        if (cursor.moveToFirst()) {
            avgRating = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return avgRating
    }
}