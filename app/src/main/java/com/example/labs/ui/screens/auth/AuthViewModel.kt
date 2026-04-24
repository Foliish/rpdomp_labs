package com.example.labs.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.labs.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun onLogin(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Заполните все поля"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.loginWithEmail(email, password).onSuccess {
                onSuccess()
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Ошибка входа"
            }
            isLoading = false
        }
    }

    fun onRegister(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Заполните все поля"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.registerWithEmail(email, password).onSuccess {
                onSuccess()
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Ошибка регистрации"
            }
            isLoading = false
        }
    }

    fun onAnonymousLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.signInAnonymously().onSuccess {
                onSuccess()
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Ошибка анонимного входа"
            }
            isLoading = false
        }
    }
}
