package com.ev.iot2.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object RecoveryEmail : Screen("recovery_email")
    object RecoveryCode : Screen("recovery_code/{email}") {
        fun createRoute(email: String) = "recovery_code/$email"
    }
    object RecoveryNewPassword : Screen("recovery_new_password/{email}") {
        fun createRoute(email: String) = "recovery_new_password/$email"
    }
    object MainMenu : Screen("main_menu")
    object UserManagement : Screen("user_management")
    object UserList : Screen("user_list")
    object UserForm : Screen("user_form?userId={userId}") {
        fun createRoute(userId: Long? = null) = if (userId != null) "user_form?userId=$userId" else "user_form"
    }
    object Sensors : Screen("sensors")
    object Developer : Screen("developer")
}
