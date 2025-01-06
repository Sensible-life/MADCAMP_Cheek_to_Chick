package com.google.ar.core.examples.kotlin.helloar

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.gson.Gson
import com.mpackage.network.LikedBooks
import com.mpackage.network.Page

class BooksDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "Books.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE Books (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                pages TEXT,
                likes INTEGER,
                ranking INTEGER,
                createdAt TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Books")
        onCreate(db)
    }

    fun insertBooks(books: List<LikedBooks>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (book in books) {
                val insertQuery = """
                    INSERT INTO Books (title, pages, likes, ranking, createdAt)
                    VALUES (?, ?, ?, ?, ?)
                """
                db.execSQL(
                    insertQuery,
                    arrayOf(
                        book.title,
                        book.pages.toString(), // JSON 문자열로 변환
                        if (book.likes) 1 else 0,
                        book.ranking,
                        book.createdAt
                    )
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllBooks(): List<LikedBooks> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Books", null)
        val books = mutableListOf<LikedBooks>()
        Log.d("getting", "all books")
        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val pages = cursor.getString(cursor.getColumnIndexOrThrow("pages"))
                val likes = cursor.getInt(cursor.getColumnIndexOrThrow("likes")) == 1
                val ranking = cursor.getInt(cursor.getColumnIndexOrThrow("ranking"))
                val createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))

                val data = "\"\"\"" + pages + "\"\"\""
                val p = mutableListOf<Page>()
                val regex = Regex("""Page\(content=(.*?), image_url=(.*?)\)""")
                regex.findAll(data).forEach { matchResult ->
                    val content = matchResult.groups[1]?.value?.trim() ?: ""
                    val imageUrl = matchResult.groups[2]?.value?.trim() ?: ""
                    p.add(Page(content, imageUrl))
                }


                books.add(
                    LikedBooks(
                        title = title,
                        pages = p, // JSON 파싱 필요
                        likes = likes,
                        ranking = ranking,
                        createdAt = createdAt
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        Log.d("created", books.toString())
        return books
    }
}
