// ui/HistoryViewModel.kt

package com.lrosas.tlalocapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lrosas.tlalocapplication.data.repository.HistoryRepository
import com.lrosas.tlalocapplication.data.model.Reading
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(private val zoneId: String) : ViewModel() {

    private val _readings = MutableStateFlow<List<Reading>>(emptyList())
    val readings: StateFlow<List<Reading>> = _readings

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        HistoryRepository()
            .latest(zoneId)
            .collect { list ->
                _readings.value = list
            }
    }

    companion object {
        fun provideFactory(zoneId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return HistoryViewModel(zoneId) as T
                }
            }
    }
}
