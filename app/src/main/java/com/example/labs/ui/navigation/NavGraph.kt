package com.example.labs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.labs.ui.screens.splash.SplashScreen
import com.example.labs.ui.screens.quotes.QuotesScreen
import com.example.labs.ui.screens.addquote.AddQuoteScreen
import com.example.labs.ui.screens.quotedetail.QuoteDetailScreen
import com.example.labs.ui.screens.auth.LoginScreen
import com.example.labs.ui.screens.auth.RegisterScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Quotes : Screen("quotes")
    object AddQuote : Screen("add_quote")
    object QuoteDetail : Screen("quote_detail/{quoteId}") {
        fun createRoute(id: Long) = "quote_detail/$id"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen { nextRoute ->
                navController.navigate(nextRoute) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Quotes.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Quotes.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Quotes.route) {
            QuotesScreen(
                onAddClick = {
                    navController.navigate(Screen.AddQuote.route)
                },
                onQuoteClick = { id ->
                    navController.navigate(Screen.QuoteDetail.createRoute(id))
                }
            )
        }

        composable(Screen.AddQuote.route) {
            AddQuoteScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QuoteDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("quoteId")?.toLong() ?: 0
            QuoteDetailScreen(
                quoteId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}