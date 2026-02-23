package com.example.labs.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "jason_statham.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_AUTHORS = "authors"
        const val COLUMN_AUTHOR_ID = "id"
        const val COLUMN_AUTHOR_NAME = "name"
        const val COLUMN_AUTHOR_RATING = "rating"
        const val COLUMN_AUTHOR_LAST_QUOTE_ID = "last_quote_id"

        const val TABLE_QUOTES = "quotes"
        const val COLUMN_QUOTE_ID = "id"
        const val COLUMN_QUOTE_AUTHOR_ID = "author_id"
        const val COLUMN_QUOTE_HEADER = "header"
        const val COLUMN_QUOTE_CONTENT = "content"
        const val COLUMN_QUOTE_RATING = "rating"
        const val COLUMN_QUOTE_READ_TIME = "read_time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAuthorsTable = """
            CREATE TABLE $TABLE_AUTHORS (
                $COLUMN_AUTHOR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AUTHOR_NAME TEXT NOT NULL,
                $COLUMN_AUTHOR_RATING REAL DEFAULT 0.0,
                $COLUMN_AUTHOR_LAST_QUOTE_ID INTEGER,
                FOREIGN KEY($COLUMN_AUTHOR_LAST_QUOTE_ID) REFERENCES $TABLE_QUOTES($COLUMN_QUOTE_ID)
            )
        """.trimIndent()

        val createQuotesTable = """
            CREATE TABLE $TABLE_QUOTES (
                $COLUMN_QUOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_QUOTE_AUTHOR_ID INTEGER NOT NULL,
                $COLUMN_QUOTE_HEADER TEXT NOT NULL,
                $COLUMN_QUOTE_CONTENT TEXT NOT NULL,
                $COLUMN_QUOTE_RATING REAL DEFAULT 0.0,
                $COLUMN_QUOTE_READ_TIME INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_QUOTE_AUTHOR_ID) REFERENCES $TABLE_AUTHORS($COLUMN_AUTHOR_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createIndex = """
            CREATE INDEX idx_quotes_author_id ON $TABLE_QUOTES($COLUMN_QUOTE_AUTHOR_ID)
        """.trimIndent()

        db.execSQL(createAuthorsTable)
        db.execSQL(createQuotesTable)
        db.execSQL(createIndex)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUOTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUTHORS")
        onCreate(db)
    }
}