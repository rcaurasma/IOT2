package com.ev.iot2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ev.iot2.ui.screens.access.AccessManagementScreen
import com.ev.iot2.ui.screens.access.SensorsManagementScreen
import com.ev.iot2.ui.screens.login.LoginScreen
import com.ev.iot2.ui.screens.menu.MainMenuScreen
import com.ev.iot2.ui.theme.IOT2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IOT2Theme {
                AppNavigator()
            }
        }
    }
}

sealed class Route(val route: String) {
    object Login : Route("login")
    object Menu : Route("menu")
    object Sensors : Route("sensors")
    object Access : Route("access")
    object SensorsManagement : Route("sensors_management")
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Login.route) {
        composable(Route.Login.route) {
            LoginScreen(
                onNavigateToRegister = { /* existing registration screen if any */ },
                onNavigateToRecovery = { /* recovery */ },
                onLoginSuccess = { token ->
                    // after login navigate to menu
                    navController.navigate(Route.Menu.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Menu.route) {
            MainMenuScreen(
                onNavigateToUserManagement = { /* TODO: implement */ },
                onNavigateToSensors = { navController.navigate(Route.Sensors.route) },
                onNavigateToDeveloper = { /* TODO */ },
                onNavigateToArduino = { navController.navigate(Route.Access.route) },
                onLogout = {
                    // clear token and go back to login
                    com.ev.iot2.network.TokenManager.clear()
                    navController.navigate(Route.Login.route) {
                        popUpTo(Route.Menu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Sensors.route) {
            // existing sensors screen (non-management) if you have one; fallback to SensorsManagement
            SensorsManagementScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Access.route) {
            AccessManagementScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.SensorsManagement.route) {
            SensorsManagementScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    IOT2Theme {
        AppNavigator()
    }
}