package com.lrosas.tlalocapplication.ui.screen
import androidx.compose.material3.Button
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lrosas.tlalocapplication.auth.AuthViewModel

@Composable
fun SignInScreen(vm: AuthViewModel = viewModel(), onDone: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    Button(
        onClick = { vm.signIn(email, pass) },
        enabled = s !is AuthViewModel.State.Loading
    ) { Text("Entrar") }

    if (s is AuthViewModel.State.Error)
        Text((s as AuthViewModel.State.Error).e, color = Color.Red)
}
