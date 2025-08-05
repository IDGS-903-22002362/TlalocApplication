package com.lrosas.tlalocapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lrosas.tlalocapplication.data.model.Telemetry
import com.lrosas.tlalocapplication.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(private val zoneId: String) : ViewModel() {

    private val _readings = MutableStateFlow<List<Telemetry>>(emptyList())
    val readings: StateFlow<List<Telemetry>> = _readings

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            HistoryRepository()
                .latest(zoneId)
                .collect { list ->
                    _readings.value = list
                }
        }
    }

    companion object {
        fun provideFactory(zoneId: String): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(zoneId) as T
                }
            }
    }
}