package com.ev.iot2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ev.iot2.data.database.DatabaseHelper
import com.ev.iot2.ui.screens.developer.DeveloperScreen
import com.ev.iot2.ui.screens.login.LoginScreen
import com.ev.iot2.ui.screens.menu.MainMenuScreen
import com.ev.iot2.ui.screens.recovery.RecoveryCodeScreen
import com.ev.iot2.ui.screens.recovery.RecoveryEmailScreen
import com.ev.iot2.ui.screens.recovery.RecoveryNewPasswordScreen
import com.ev.iot2.ui.screens.register.RegisterScreen
import com.ev.iot2.ui.screens.sensors.SensorsScreen
import com.ev.iot2.ui.screens.splash.SplashScreen
import com.ev.iot2.ui.screens.users.UserFormScreen
import com.ev.iot2.ui.screens.users.UserListScreen
import com.ev.iot2.ui.screens.users.UserManagementScreen

@Composable
fun IoTempNavHost(
    navController: NavHostController,
    databaseHelper: DatabaseHelper
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                databaseHelper = databaseHelper,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToRecovery = {
                    navController.navigate(Screen.RecoveryEmail.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.MainMenu.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                databaseHelper = databaseHelper,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RecoveryEmail.route) {
            RecoveryEmailScreen(
                databaseHelper = databaseHelper,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCodeSent = { email ->
                    navController.navigate(Screen.RecoveryCode.createRoute(email))
                }
            )
        }
        
        composable(
            route = Screen.RecoveryCode.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RecoveryCodeScreen(
                email = email,
                databaseHelper = databaseHelper,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCodeValid = {
                    navController.navigate(Screen.RecoveryNewPassword.createRoute(email)) {
                        popUpTo(Screen.RecoveryEmail.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.RecoveryNewPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RecoveryNewPasswordScreen(
                email = email,
                databaseHelper = databaseHelper,
                onPasswordChanged = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigateToUserManagement = {
                    navController.navigate(Screen.UserManagement.route)
                },
                onNavigateToSensors = {
                    navController.navigate(Screen.Sensors.route)
                },
                onNavigateToDeveloper = {
                    navController.navigate(Screen.Developer.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MainMenu.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.UserManagement.route) {
            UserManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToUserForm = {
                    navController.navigate(Screen.UserForm.createRoute())
                },
                onNavigateToUserList = {
                    navController.navigate(Screen.UserList.route)
                }
            )
        }
        
        composable(Screen.UserList.route) {
            UserListScreen(
                databaseHelper = databaseHelper,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditUser = { userId ->
                    navController.navigate(Screen.UserForm.createRoute(userId))
                }
            )
        }
        
        composable(
            route = Screen.UserForm.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userIdString = backStackEntry.arguments?.getString("userId")
            val userId = userIdString?.toLongOrNull()
            UserFormScreen(
                databaseHelper = databaseHelper,
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Sensors.route) {
            SensorsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Developer.route) {
            DeveloperScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
