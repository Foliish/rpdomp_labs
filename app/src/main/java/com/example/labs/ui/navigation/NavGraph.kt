package com.example.labs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.labs.ui.screens.splash.SplashScreen
import com.example.labs.ui.screens.quotes.QuotesScreen
import com.example.labs.ui.screens.addquote.AddQuoteScreen
import com.example.labs.ui.screens.quotedetail.QuoteDetailScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
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
            SplashScreen {
                navController.navigate(Screen.Quotes.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
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