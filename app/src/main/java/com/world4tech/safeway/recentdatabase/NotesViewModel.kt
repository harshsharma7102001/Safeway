package com.world4tech.homework.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(application: Application):AndroidViewModel(application) {
    private val repository: NotesRepository
    val allNotes:LiveData<List<Notes>>
    init {
        val dao= NotesDatabase.getDatabase(application).getNotesDao()
        repository= NotesRepository(dao)
        allNotes = repository.allNotes
    }
    fun delNotes(notes: Notes)=viewModelScope.launch(Dispatchers.IO) {
        repository.delete(notes)
    }
    fun addNotes(notes: Notes) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(notes)
    }
}