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
import com.ev.iot2.network.TokenManager
import com.ev.iot2.ui.theme.IOT2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
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
    object Register : Route("register")
    object RecoveryEmail : Route("recovery_email")
    object RecoveryCode : Route("recovery_code/{email}")
    object RecoveryNewPassword : Route("recovery_new/{email}/{code}")
    object UserManagement : Route("users")
    object Developer : Route("developer")
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Login.route) {
        composable(Route.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Route.Register.route) },
                onNavigateToRecovery = { navController.navigate(Route.RecoveryEmail.route) },
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
                onNavigateToUserManagement = { navController.navigate(Route.UserManagement.route) },
                onNavigateToSensors = { navController.navigate(Route.Sensors.route) },
                onNavigateToDeveloper = { navController.navigate(Route.Developer.route) },
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

        composable(Route.Register.route) {
            com.ev.iot2.ui.screens.register.RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate(Route.Login.route) { popUpTo(Route.Register.route) { inclusive = true } } }
            )
        }

        composable(Route.RecoveryEmail.route) {
            com.ev.iot2.ui.screens.recovery.RecoveryEmailScreen(
                onNavigateBack = { navController.popBackStack() },
                onCodeSent = { email -> navController.navigate("recovery_code/$email") }
            )
        }

        composable("recovery_code/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            com.ev.iot2.ui.screens.recovery.RecoveryCodeScreen(
                email = email,
                onCodeVerified = { code -> navController.navigate("recovery_new/$email/$code") }
            )
        }

        composable("recovery_new/{email}/{code}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val code = backStackEntry.arguments?.getString("code") ?: ""
            com.ev.iot2.ui.screens.recovery.RecoveryNewPasswordScreen(
                email = email,
                code = code,
                onPasswordChanged = { navController.navigate(Route.Login.route) { popUpTo(Route.RecoveryEmail.route) { inclusive = true } } }
            )
        }

        composable(Route.UserManagement.route) {
            com.ev.iot2.ui.screens.users.UserManagementScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Developer.route) {
            com.ev.iot2.ui.screens.developer.DeveloperScreen(onBack = { navController.popBackStack() })
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