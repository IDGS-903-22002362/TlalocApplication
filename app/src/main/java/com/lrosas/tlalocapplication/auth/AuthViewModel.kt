package com.lrosas.tlalocapplication.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    /** --- Estados que la UI observará --- */
    sealed interface State {
        object Idle     : State
        object Loading  : State
        object Success  : State          // ← NUEVO
        data class Error(val e: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    /** Canal opcional para eventos “de un solo disparo” */
    val events = Channel<Unit>(Channel.BUFFERED)

    /** Iniciar sesión con Firebase Auth */
    fun signIn(email: String, pass: String) = viewModelScope.launch {
        _state.value = State.Loading

        Firebase.auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _state.value = State.Success          // ← publica éxito
                events.trySend(Unit)                  // para quien aún escuche el canal
            }
            .addOnFailureListener {
                _state.value = State.Error(it.message ?: "Error desconocido")
            }
    }
}