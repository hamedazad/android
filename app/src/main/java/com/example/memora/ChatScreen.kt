package com.example.memoraapp

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.Response

@Composable
fun ChatScreen(token: String) {
    var questionInput by remember { mutableStateOf("") }
    var chatOutput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val spokenText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            questionInput = spokenText ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memora") },
                backgroundColor = Color(0xFF9C27B0),  // Purple header
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEAF3F8))
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(max = 400.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Memora – \"Speak It. Save It. Recall It\"",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // ✅ Outlined TextField for Question input
                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    label = { Text("Remember or ask...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF6200EE),
                        unfocusedBorderColor = Color(0xFF9C27B0),
                        textColor = Color.Black
                    )
                )
                Button(
                    onClick = {
                        if (questionInput.isBlank()) {
                            error = "Give me something to remember!"
                            return@Button
                        }
                        coroutineScope.launch {
                            isLoading = true
                            error = null
                            sendMessage(questionInput, token) { response, exception ->
                                if (exception != null) {
                                    error = "Error: ${exception.localizedMessage}"
                                } else if (response != null) {
                                    chatOutput = response.reply
                                    questionInput = "" // ✅ Clear input
                                } else {
                                    error = "Unknown error occurred."
                                }
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Memora")
                }




                // ✅ Mic button
                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                        }
                        speechRecognizerLauncher.launch(intent)
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(48.dp)
                        .background(Color(0xFF9C27B0), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Speak",
                        tint = Color.White
                    )
                }

                // Output
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                } else if (error != null) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else if (chatOutput.isNotEmpty()) {
                    Card(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = chatOutput,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

suspend fun sendMessage(
    message: String,
    token: String,
    onResult: (ChatResponse?, Exception?) -> Unit
) {
    try {
        val chatRequest = ChatRequest(message)
        val authHeader = "Token $token"
        val response: Response<ChatResponse> =
            RetrofitClient.apiService.sendMessage(chatRequest, authHeader)

        if (response.isSuccessful) {
            onResult(response.body(), null)
        } else {
            onResult(null, Exception("Error: ${response.code()} - ${response.message()}"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null, e)
    }
}
