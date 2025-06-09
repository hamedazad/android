package com.example.memoraapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
        Text(
            text = "Memora Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            error = "Please enter both username and password"
                            return@Button
                        }
                        
                        isLoading = true
                        error = null
                        scope.launch {
                            try {
                                Log.d("LoginScreen", "Attempting login for user: ${username.trim()}")
                                val response = RetrofitClient.apiService.login(
                                    LoginRequest(username.trim(), password.trim())
                                )
                                Log.d("LoginScreen", "Login response code: ${response.code()}")
                                
                                if (response.isSuccessful) {
                                    val loginResponse = response.body()
                                    Log.d("LoginScreen", "Login response body: $loginResponse")
                                    
                                    if (loginResponse != null) {
                                        Log.d("LoginScreen", "Login successful, token received")
                                        onLoginSuccess(loginResponse.token)
                                    } else {
                                        Log.e("LoginScreen", "Invalid login response: null response")
                                        error = "Invalid login response"
                                    }
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    Log.e("LoginScreen", "Login failed: ${response.code()} - $errorBody")
                                    error = "Login failed: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Login error", e)
                                error = "Connection error: ${e.localizedMessage}"
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
                text = error ?: "",
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
