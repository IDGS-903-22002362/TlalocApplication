package com.lrosas.tlalocapplication.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    sealed interface State { object Idle : State
        object Loading : State
        data class Error(val e: String) : State }
    private val _state = MutableStateFlow<State>(State.Idle)
    val   state: StateFlow<State> = _state

    val events = Channel<Unit>()   // used to signal success

    fun signIn(email: String, pass: String) = viewModelScope.launch {
        _state.value = State.Loading
        Firebase.auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { events.trySend(Unit); _state.value = State.Idle }
            .addOnFailureListener { _state.value = State.Error(it.message ?: "") }
    }
}