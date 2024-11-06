package com.ti4all.agendaapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ti4all.agendaapp.AgendaApplication

class AgendaViewModelFactory(private val application: AgendaApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
