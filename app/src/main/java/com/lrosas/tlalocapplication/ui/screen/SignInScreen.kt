package com.lrosas.tlalocapplication.ui.screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.lrosas.tlalocapplication.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    vm: AuthViewModel = viewModel(),
    onDone: () -> Unit
) {
    /* -------- estados -------- */
    val uiState by vm.state.collectAsStateWithLifecycle()       // Success | Loading | Error
    var email  by rememberSaveable { mutableStateOf("") }
    var pass   by rememberSaveable { mutableStateOf("") }
    val snack  = remember { SnackbarHostState() }

    /* -------- side-effects (éxito ↠ navega  /  error ↠ snack) -------- */
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthViewModel.State.Success -> onDone()
            is AuthViewModel.State.Error   -> {
                val msg = (uiState as AuthViewModel.State.Error).e
                snack.showSnackbar(msg)
            }
            else -> Unit
        }
    }

    /* -------- UI -------- */
    Scaffold(snackbarHost = { SnackbarHost(snack) }) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Inicia sesión", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick  = { vm.signIn(email.trim(), pass) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = uiState !is AuthViewModel.State.Loading
            ) {
                if (uiState is AuthViewModel.State.Loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Text("Entrar")
            }
        }
    }
}

