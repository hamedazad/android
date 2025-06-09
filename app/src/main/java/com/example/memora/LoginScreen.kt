package com.example.memoraapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    // Hardcoded username and password for demonstration
    val username = "azad"
    val password = "azad"
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header / Logo
        Text(
            text = "Memora Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Card container for login fields
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Username",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = username,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = password,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        isLoading = true
                        error = null
                        scope.launch {
                            try {
                                // 🚀 Login API call
                                val response = RetrofitClient.apiService.login(
                                    LoginRequest(username.trim(), password.trim())
                                )
                                if (response.isSuccessful) {
                                    val loginResponse = response.body()
                                    if (!loginResponse?.token.isNullOrEmpty()) {
                                        onLoginSuccess(loginResponse!!.token)
                                    } else {
                                        error = "Invalid login response"
                                    }
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    error = "Error: ${response.code()} - ${errorBody ?: "Unknown error"}"
                                }
                            } catch (e: Exception) {
                                error = "Exception: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Login")
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        if (error != null) {
            Text(
                "Error: $error",
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
