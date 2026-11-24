package com.example.reservationsystem.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservationsystem.ui.screens.auth.AuthScreen
import com.example.reservationsystem.viewmodel.AuthViewModel
import com.example.reservationsystem.viewmodel.AuthViewModelFactory
import com.example.reservationsystem.viewmodel.MainViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppRoot(mainViewModel: MainViewModel) {
    val factory = remember { AuthViewModelFactory() }

    val authViewModel: AuthViewModel = viewModel(
        factory = factory
    )

    val state by authViewModel.uiState.collectAsState()
    val usuario = state.usuario

    if (usuario == null) {
        AuthScreen(
            state = state,
            onLogin = authViewModel::iniciarSesion,
            onRegistro = authViewModel::registrar,
            onRecuperar = authViewModel::recuperar,
            onLimpiar = authViewModel::limpiarMensajes
        )
    } else {
        key(usuario.id) {
            AppNavigation(
                viewModel = mainViewModel,
                usuario = usuario,
                cerrandoSesion = state.cargando,
                mensajeSesion = state.mensaje,
                onCerrarSesion = authViewModel::cerrarSesion
            )
        }
    }
}