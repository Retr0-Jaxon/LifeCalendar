package com.example.lifecalendar

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns

class LifeCalendarProvider : ContentProvider() {

    private lateinit var dbHelper: LifespanDbHelper
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    companion object {
        const val AUTHORITY = "com.example.lifecalendar.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/lifespan")
        val MEMO_URI: Uri = Uri.parse("content://$AUTHORITY/memo")
        val TIME_CHECK_URI: Uri = Uri.parse("content://$AUTHORITY/timecheck")

        const val LIFESPAN_TABLE = "lifespan"
        const val LIFESPAN_COLUMN_WEEKS = "weeks"
        const val LIFESPAN_COLUMN_TIME = "time_string"
        
        const val MEMO_TABLE = "memo"
        const val MEMO_COLUMN_ID = "_id"
        const val MEMO_COLUMN_CONTENT = "content"
        const val MEMO_COLUMN_TIMESTAMP = "timestamp"

        const val TIME_CHECK_TABLE = "timecheck"
        const val TIME_CHECK_COLUMN_ID = "_id"
        const val TIME_CHECK_COLUMN_TIMESTAMP = "timestamp"
        const val TIME_CHECK_COLUMN_FORMATTED_TIME = "formatted_time"

        const val LIFESPAN_ID = 1
        const val MEMO_ID = 2
        const val TIME_CHECK_ID = 3
    }

    init {
        uriMatcher.addURI(AUTHORITY, "lifespan", LIFESPAN_ID)
        uriMatcher.addURI(AUTHORITY, "memo", MEMO_ID)
        uriMatcher.addURI(AUTHORITY, "timecheck", TIME_CHECK_ID)
    }

    override fun onCreate(): Boolean {
        dbHelper = LifespanDbHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        val cursor = when (uriMatcher.match(uri)) {
            LIFESPAN_ID -> db.query(
                LIFESPAN_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
            MEMO_ID -> db.query(
                MEMO_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
            TIME_CHECK_ID -> db.query(
                TIME_CHECK_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            LIFESPAN_ID -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$LIFESPAN_TABLE"
            MEMO_ID -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$MEMO_TABLE"
            TIME_CHECK_ID -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$TIME_CHECK_TABLE"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = when (uriMatcher.match(uri)) {
            LIFESPAN_ID -> db.insert(LIFESPAN_TABLE, null, values)
            MEMO_ID -> db.insert(MEMO_TABLE, null, values)
            TIME_CHECK_ID -> db.insert(TIME_CHECK_TABLE, null, values)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        if (id > 0) {
            context?.contentResolver?.notifyChange(uri, null)
            return Uri.withAppendedPath(uri, id.toString())
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        val rowsDeleted = when (uriMatcher.match(uri)) {
            LIFESPAN_ID -> db.delete(LIFESPAN_TABLE, selection, selectionArgs)
            MEMO_ID -> db.delete(MEMO_TABLE, selection, selectionArgs)
            TIME_CHECK_ID -> db.delete(TIME_CHECK_TABLE, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        if (rowsDeleted > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsDeleted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db = dbHelper.writableDatabase
        val rowsUpdated = when (uriMatcher.match(uri)) {
            LIFESPAN_ID -> db.update(LIFESPAN_TABLE, values, selection, selectionArgs)
            MEMO_ID -> db.update(MEMO_TABLE, values, selection, selectionArgs)
            TIME_CHECK_ID -> db.update(TIME_CHECK_TABLE, values, selection, selectionArgs)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        if (rowsUpdated > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsUpdated
    }

    private class LifespanDbHelper(context: android.content.Context) :
        SQLiteOpenHelper(context, "lifespan.db", null, 2) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS $LIFESPAN_TABLE (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "$LIFESPAN_COLUMN_WEEKS INTEGER)"
            )
            
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS $MEMO_TABLE (" +
                    "$MEMO_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$MEMO_COLUMN_CONTENT TEXT," +
                    "$MEMO_COLUMN_TIMESTAMP INTEGER)"
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS $TIME_CHECK_TABLE (" +
                    "$TIME_CHECK_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$TIME_CHECK_COLUMN_TIMESTAMP INTEGER," +
                    "$TIME_CHECK_COLUMN_FORMATTED_TIME TEXT)"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                // 添加新表
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS $TIME_CHECK_TABLE (" +
                        "$TIME_CHECK_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "$TIME_CHECK_COLUMN_TIMESTAMP INTEGER," +
                        "$TIME_CHECK_COLUMN_FORMATTED_TIME TEXT)"
                )
            }
        }
    }
}