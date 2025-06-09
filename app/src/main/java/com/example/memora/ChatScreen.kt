package com.example.memoraapp

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import com.example.memoraapp.RetrofitClient
import com.example.memoraapp.Memory
import com.example.memoraapp.MemoryRequest
import androidx.compose.ui.platform.LocalContext
import android.speech.tts.TextToSpeech

@Composable
fun ChatScreen(token: String, onLogout: () -> Unit) {
    var questionInput by remember { mutableStateOf("") }
    val chatHistory = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(false) }
    var memories by remember { mutableStateOf<List<Memory>>(emptyList()) }
    var showInputFor by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScoroutineScope()
    var latestResponse by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val tts = remember {
        TextToSpeech(context, null)
    }

    fun loadMemories(authToken: String) {
        coroutineScope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.apiService.getMemories("Token $authToken")
                if (response.isSuccessful) {
                    memories = response.body() ?: emptyList()
                } else {
                    scaffoldState.snackbarHostState.showSnackbar("Failed to load memories: ${response.code()}")
                }
            } catch (e: Exception) {
                scaffoldState.snackbarHostState.showSnackbar("Error loading memories: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            loadMemories(token)
        }
    }

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
                backgroundColor = Color(0xFF9C27B0),
                contentColor = Color.White,
                actions = {
                    IconButton(onClick = { loadMemories(token) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Memories")
                    }
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { showInputFor = "remember" },
                    backgroundColor = Color(0xFF9C27B0),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Remember")
                }
                FloatingActionButton(
                    onClick = { showInputFor = "ask" },
                    backgroundColor = Color(0xFF6200EE),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Ask")
                }
            }
        },
        scaffoldState = scaffoldState
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

                if (showInputFor != null) {
                    OutlinedTextField(
                        value = questionInput,
                        onValueChange = { questionInput = it },
                        label = { Text(if (showInputFor == "remember") "What do you want to remember?" else "What do you want to ask?") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF6200EE),
                            unfocusedBorderColor = Color(0xFF9C27B0),
                            textColor = Color.Black
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (questionInput.isBlank()) {
                                        coroutineScope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Please enter something!")
                                        }
                                        return@IconButton
                                    }
                                    coroutineScope.launch {
                                        isLoading = true
                                        // Handle saving memory or asking question
                                        if (showInputFor == "remember") {
                                            try {
                                                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                                                val memoryRequest = MemoryRequest(content = questionInput, timestamp = timestamp)
                                                val authHeader = "Token $token"
                                                val response = RetrofitClient.apiService.saveMemory(memoryRequest, authHeader)

                                                if (response.isSuccessful) {
                                                    latestResponse = "Got it!"
                                                } else {
                                                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                                    coroutineScope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar("Error saving memory: ${response.code()} - $errorBody")
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                coroutineScope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar("Exception saving memory: ${e.localizedMessage}")
                                                }
                                            }
                                            questionInput = ""
                                            showInputFor = null
                                            isLoading = false
                                        } else { // showInputFor == "ask"
                                            sendMessage(questionInput, token) { response, exception ->
                                                if (exception != null) {
                                                    coroutineScope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar("Error asking question: ${exception.localizedMessage}")
                                                    }
                                                } else if (response != null) {
                                                    latestResponse = response.reply
                                                } else {
                                                    coroutineScope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar("Unknown error occurred while asking question.")
                                                    }
                                                }
                                                questionInput = ""
                                                showInputFor = null
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colors.primary)
                            }
                        }
                    )

                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
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
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .weight(1f, fill = false)
                ) {
                    items(memories) { memory ->
                        Card(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = memory.content,
                                    style = MaterialTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = memory.timestamp,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    items(chatHistory) { message ->
                        Card(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
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
