package com.world4tech.homework.database

import androidx.lifecycle.LiveData

class NotesRepository(private var notesDao: NotesDao) {
    val allNotes:LiveData<List<Notes>> = notesDao.getAllNotes()
    suspend fun insert(notes: Notes){
        notesDao.insert(notes)
    }
    suspend fun delete(notes: Notes){
        notesDao.delete(notes)
    }
}