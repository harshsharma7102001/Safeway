package com.world4tech.homework.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Notes)
    @Delete
    suspend fun delete(note: Notes)
    @Query("Select * from Data ORDER BY id DESC")
    fun getAllNotes():LiveData<List<Notes>>
}