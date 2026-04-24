package com.example.labs.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.labs.R
import com.example.labs.data.repository.AuthRepository
import com.example.labs.ui.navigation.Screen

@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {
    val authRepository = AuthRepository()

    LaunchedEffect(Unit) {
        delay(1000)
        val nextRoute = if (authRepository.isUserLoggedIn) {
            Screen.Quotes.route
        } else {
            Screen.Login.route
        }
        onNavigate(nextRoute)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier.size(120.dp)
        )
    }
}