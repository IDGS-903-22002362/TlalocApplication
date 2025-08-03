package com.lrosas.tlalocapplication.ui.zones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lrosas.tlalocapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ZonesViewModel(
    private val repo: ZoneRepository = ZoneRepository()
) : ViewModel() {

    val zones = repo.getZones()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
