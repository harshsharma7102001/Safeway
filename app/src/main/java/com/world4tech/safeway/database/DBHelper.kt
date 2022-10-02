package com.world4tech.safeway.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                NAME_COl + " TEXT," +
                EMAIL_COL + " TEXT," +
                PHONE_COL + " TEXT," +
                EPHONE_COL_ONE + " TEXT,"+
                EPHONE_COL_TWO + " TEXT"+ ")")
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    // This method is for adding data in our database
    fun addName(name : String, email : String,phone:String,ephoneone :String,ephonetwo:String ){

        // below we are creating
        // a content values variable
        val values = ContentValues()
        values.put(NAME_COl, name)
        values.put(EMAIL_COL, email)
        values.put(PHONE_COL, phone)
        values.put(EPHONE_COL_ONE, ephoneone)
        values.put(EPHONE_COL_TWO, ephonetwo)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }
    fun getName(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null)

    }

    companion object{
        private val DATABASE_NAME = "USER_INFO_TABLE"
        private val DATABASE_VERSION = 1
        val TABLE_NAME = "user_Data"
        val ID_COL = "id"
        val NAME_COl = "name"
        val EMAIL_COL = "email"
        val PHONE_COL = "phone"
        val EPHONE_COL_ONE = "ephoneone"
        val EPHONE_COL_TWO = "ephonetwo"

    }
}