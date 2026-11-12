package com.batchmates.app.ui.welcome

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.batchmates.app.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

class AuthViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state
    val googleAvailable get() = auth.googleAvailable

    fun google(activityContext: Context) = run {
        auth.signInWithGoogle(activityContext).exceptionOrNull()?.let { e ->
            if (e is GetCredentialCancellationException) null else e.friendly()
        }
    }

    fun signIn(email: String, password: String) = run {
        validate(email, password) ?: auth.signInWithEmail(email, password).exceptionOrNull()?.friendly()
    }

    fun signUp(name: String, email: String, password: String) = run {
        when {
            name.isBlank() -> "Tell your friends your name"
            else -> validate(email, password) ?: auth.signUpWithEmail(name, email, password).exceptionOrNull()?.friendly()
        }
    }

    fun reset(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _state.update { it.copy(error = "Enter your email above first") }; return
        }
        viewModelScope.launch {
            auth.sendPasswordReset(email)
            _state.update { it.copy(info = "Reset link sent to ${email.trim()} 📬", error = null) }
        }
    }

    fun clearMessages() = _state.update { it.copy(error = null, info = null) }

    private fun validate(email: String, password: String): String? = when {
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "That email doesn't look right"
        password.length < 6 -> "Password needs at least 6 characters"
        else -> null
    }

    private fun run(block: suspend () -> String?) {
        if (_state.value.busy) return
        _state.value = AuthUiState(busy = true)
        viewModelScope.launch {
            val error = block()
            _state.value = AuthUiState(busy = false, error = error)
        }
    }

    private fun Throwable.friendly(): String {
        val m = message.orEmpty()
        return when {
            "no user record" in m || "INVALID_LOGIN_CREDENTIALS" in m || "password is invalid" in m ||
                "credential is incorrect" in m -> "Email or password is incorrect"
            "already in use" in m -> "An account already exists for this email. Try signing in."
            "network" in m.lowercase() -> "No internet connection. Check and retry."
            "No credentials available" in m -> "No Google account found on this phone"
            else -> m.ifBlank { "Something went wrong. Please try again." }
        }
    }
}
