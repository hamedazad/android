package com.example.memoraapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.memoraapp.ui.theme.MemoraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Retrieve token from SharedPreferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("user_token", null)

        // ✅ If no token, show toast and exit
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            MemoraTheme {
                // ✅ Pass a lambda for onLogout
                ChatScreen(token = token) {
                    // 👈 This block runs when user clicks logout
                    prefs.edit().remove("user_token").apply()
                    Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
