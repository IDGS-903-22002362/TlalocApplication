package com.lrosas.tlalocapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.lrosas.tlalocapplication.data.HistoryRepository
import com.lrosas.tlalocapplication.data.model.Reading

class HistoryViewModel(private val zoneId: String = "zone1") : ViewModel() {

    private val _readings = MutableStateFlow<List<Reading>>(emptyList())
    val  readings: StateFlow<List<Reading>> = _readings

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        HistoryRepository.latest(zoneId).collect { _readings.value = it }
    }
}