package com.example.reservationsystem.ui.navigation
/*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reservationsystem.R
import com.example.reservationsystem.ui.screens.movimientos.MovimientosScreen
import com.example.reservationsystem.ui.screens.movimientos.NuevoMovimientoScreen
import com.example.reservationsystem.ui.screens.reservas.NuevaReservaScreen
import com.example.reservationsystem.ui.screens.reservas.ReservasScreen
import com.example.reservationsystem.ui.screens.resumen.InicioScreen
import com.example.reservationsystem.viewmodel.MainViewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Inicio.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 350))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 350))
            }
        ) {
            composable(AppScreen.Inicio.route) {
                InicioScreen(viewModel = viewModel)
            }

            composable(AppScreen.Movimientos.route) {
                MovimientosScreen(
                    viewModel = viewModel,
                    onAgregarMovimiento = {
                        navController.navigate(AppScreen.NuevoMovimiento.route)
                    },
                    onEditarMovimiento = { movimientoId ->
                        navController.navigate("editar_movimiento/$movimientoId")
                    }
                )
            }

            composable(AppScreen.NuevoMovimiento.route) {
                NuevoMovimientoScreen(
                    viewModel = viewModel,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(
                route = AppScreen.EditarMovimiento.route,
                arguments = listOf(
                    navArgument("movimientoId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val movimientoId =
                    backStackEntry.arguments?.getInt("movimientoId") ?: 0

                NuevoMovimientoScreen(
                    viewModel = viewModel,
                    movimientoId = movimientoId,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(AppScreen.Reservas.route) {
                ReservasScreen(
                    viewModel = viewModel,
                    onAgregarReserva = {
                        navController.navigate(AppScreen.NuevaReserva.route)
                    }
                )
            }

            composable(AppScreen.NuevaReserva.route) {
                NuevaReservaScreen(
                    viewModel = viewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    data class Destino(
        val ruta: String,
        val etiqueta: String,
        val icono: Int
    )

    val destinos = listOf(
        Destino(
            ruta = AppScreen.Inicio.route,
            etiqueta = "Inicio",
            icono = R.drawable.home_24dp_e3e3e3_fill0_wght400_grad0_opsz24
        ),
        Destino(
            ruta = AppScreen.Movimientos.route,
            etiqueta = "Movimientos",
            icono = R.drawable.receipt_long_24dp_e3e3e3_fill0_wght400_grad0_opsz24
        ),
        Destino(
            ruta = AppScreen.Reservas.route,
            etiqueta = "Reservas",
            icono = R.drawable.event_24dp_e3e3e3_fill0_wght400_grad0_opsz24
        )
    )

    NavigationBar {
        destinos.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = {
                    navController.navigate(destino.ruta) {
                        popUpTo(AppScreen.Inicio.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(destino.icono),
                        contentDescription = destino.etiqueta
                    )
                },
                label = {
                    Text(destino.etiqueta)
                }
            )
        }
    }
}

*/

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.reservationsystem.R
import com.example.reservationsystem.domain.auth.Permiso
import com.example.reservationsystem.domain.auth.UsuarioSesion
import com.example.reservationsystem.ui.screens.auth.PerfilScreen
import com.example.reservationsystem.ui.screens.movimientos.MovimientosScreen
import com.example.reservationsystem.ui.screens.movimientos.NuevoMovimientoScreen
import com.example.reservationsystem.ui.screens.reservas.NuevaReservaScreen
import com.example.reservationsystem.ui.screens.reservas.ReservasScreen
import com.example.reservationsystem.ui.screens.resumen.InicioScreen
import com.example.reservationsystem.viewmodel.MainViewModel

private const val RUTA_PERFIL = "perfil"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    usuario: UsuarioSesion,
    cerrandoSesion: Boolean,
    mensajeSesion: String?,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()

    val volverInicio: () -> Unit = {
        navController.navigate(AppScreen.Inicio.route) {
            popUpTo(AppScreen.Inicio.route) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                usuario = usuario
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Inicio.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(180))
            },
            exitTransition = {
                fadeOut(tween(180))
            }
        ) {
            composable(AppScreen.Inicio.route) {
                AccesoProtegido(
                    usuario = usuario,
                    permiso = Permiso.VER_INICIO,
                    onVolver = volverInicio
                ) {
                    InicioScreen(viewModel = viewModel)
                }
            }

            composable(AppScreen.Movimientos.route) {
                AccesoProtegido(
                    usuario = usuario,
                    permiso = Permiso.GESTIONAR_MOVIMIENTOS,
                    onVolver = volverInicio
                ) {
                    MovimientosScreen(
                        viewModel = viewModel,
                        onAgregarMovimiento = {
                            navController.navigate(
                                AppScreen.NuevoMovimiento.route
                            )
                        },
                        onEditarMovimiento = { movimientoId ->
                            navController.navigate(
                                "editar_movimiento/$movimientoId"
                            )
                        }
                    )
                }
            }

            composable(AppScreen.NuevoMovimiento.route) {
                AccesoProtegido(
                    usuario = usuario,
                    permiso = Permiso.GESTIONAR_MOVIMIENTOS,
                    onVolver = volverInicio
                ) {
                    NuevoMovimientoScreen(
                        viewModel = viewModel,
                        onVolver = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = AppScreen.EditarMovimiento.route,
                arguments = listOf(
                    navArgument("movimientoId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                AccesoProtegido(
                    usuario = usuario,
                    permiso = Permiso.GESTIONAR_MOVIMIENTOS,
                    onVolver = volverInicio
                ) {
                    val movimientoId = backStackEntry.arguments
                        ?.getInt("movimientoId") ?: 0

                    NuevoMovimientoScreen(
                        viewModel = viewModel,
                        movimientoId = movimientoId,
                        onVolver = { navController.popBackStack() }
                    )
                }
            }

            composable(AppScreen.Reservas.route) {
                AccesoProtegido(
                    usuario = usuario,
                    permiso = Permiso.GESTIONAR_RESERVAS,
                    onVolver = volverInicio
                ) {
                    ReservasScreen(
                        viewModel = viewModel,
                        onAgregarReserva = {
                            navController.navigate(
                                AppScreen.NuevaReserva.route
                            )
                        }
                    )
                }
            }

            composable(AppScreen.NuevaReserva.route) {
                AccesoProtegido(
                    usuario = usuario,
                    permiso = Permiso.SOLICITAR_RESERVA,
                    onVolver = volverInicio
                ) {
                    NuevaReservaScreen(
                        viewModel = viewModel,
                        usuario = usuario,
                        onVolver = { navController.popBackStack() }
                    )
                }
            }

            composable(RUTA_PERFIL) {
                PerfilScreen(
                    usuario = usuario,
                    cargando = cerrandoSesion,
                    mensaje = mensajeSesion,
                    onSolicitarReserva = {
                        navController.navigate(
                            AppScreen.NuevaReserva.route
                        )
                    },
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
    }
}

@Composable
private fun AccesoProtegido(
    usuario: UsuarioSesion,
    permiso: Permiso,
    onVolver: () -> Unit,
    contenido: @Composable () -> Unit
) {
    if (usuario.tienePermiso(permiso)) {
        contenido()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Acceso restringido",
                style = MaterialTheme.typography.headlineSmall
            )

            Text("Tu cuenta no tiene permisos para abrir esta pantalla.")

            Button(onClick = onVolver) {
                Text("Volver al inicio")
            }
        }
    }
}

private data class DestinoPrincipal(
    val ruta: String,
    val etiqueta: String,
    val icono: Int?,
    val permiso: Permiso?
)

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    usuario: UsuarioSesion
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    val destinos = listOf(
        DestinoPrincipal(
            ruta = AppScreen.Inicio.route,
            etiqueta = "Inicio",
            icono = R.drawable.home_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
            permiso = Permiso.VER_INICIO
        ),
        DestinoPrincipal(
            ruta = AppScreen.Movimientos.route,
            etiqueta = "Movimientos",
            icono = R.drawable.receipt_long_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
            permiso = Permiso.GESTIONAR_MOVIMIENTOS
        ),
        DestinoPrincipal(
            ruta = AppScreen.Reservas.route,
            etiqueta = "Reservas",
            icono = R.drawable.event_24dp_e3e3e3_fill0_wght400_grad0_opsz24,
            permiso = Permiso.GESTIONAR_RESERVAS
        ),
        DestinoPrincipal(
            ruta = RUTA_PERFIL,
            etiqueta = "Perfil",
            icono = null,
            permiso = null
        )
    ).filter { destino ->
        val permiso = destino.permiso
        permiso == null || usuario.tienePermiso(permiso)
    }

    NavigationBar {
        destinos.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = {
                    navController.navigate(destino.ruta) {
                        popUpTo(AppScreen.Inicio.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    val icono = destino.icono

                    if (icono != null) {
                        Icon(
                            painter = painterResource(icono),
                            contentDescription = destino.etiqueta
                        )
                    } else {
                        // Puedes reemplazarlo por un drawable local de persona.
                        Text("P")
                    }
                },
                label = { Text(destino.etiqueta) }
            )
        }
    }
}