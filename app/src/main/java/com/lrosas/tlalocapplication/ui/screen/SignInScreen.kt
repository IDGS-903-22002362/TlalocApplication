package com.lrosas.tlalocapplication.ui.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lrosas.tlalocapplication.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.lrosas.tlalocapplication.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    vm: AuthViewModel = viewModel(),
    onDone: () -> Unit
) {
    val uiState by vm.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    val snack = remember { SnackbarHostState() }

    val greenPrimary = colorResource(id = R.color.green_primary)
    val white = colorResource(id = R.color.white)

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthViewModel.State.Success -> onDone()
            is AuthViewModel.State.Error -> {
                val msg = (uiState as AuthViewModel.State.Error).e
                snack.showSnackbar(msg)
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(greenPrimary)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snack) },
            containerColor = Color.Transparent
        ) { insets ->
            Column(
                modifier = Modifier
                    .padding(insets)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Card (
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = white)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Inicia sesión",
                            style = MaterialTheme.typography.headlineMedium,
                            color = greenPrimary
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = { Text("Contraseña") },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Button(
                            onClick = { vm.signIn(email.trim(), pass) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is AuthViewModel.State.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = greenPrimary,
                                contentColor = white
                            )
                        ) {
                            if (uiState is AuthViewModel.State.Loading) {
                                CircularProgressIndicator(
                                    color = white,
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
        }
    }
}
