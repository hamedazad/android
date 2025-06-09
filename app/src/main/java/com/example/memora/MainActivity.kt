package com.example.memoraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoraTheme {
                var token by remember { mutableStateOf<String?>(null) }

                if (token == null) {
                    LoginScreen { receivedToken ->
                        token = receivedToken
                    }
                } else {
                    ChatScreen(token!!)
                }
            }
        }
    }
}
