package dev.mlzzen.kaiqiu.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mlzzen.kaiqiu.ui.state.UserState
import dev.mlzzen.kaiqiu.ui.theme.KaiqiuBlue
import dev.mlzzen.kaiqiu.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    userState: UserState = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isLoading by userState.isLoading.collectAsState()
    val error by userState.error.collectAsState()
    val isLoggedIn by userState.isLoggedIn.collectAsState()
    val isMoreMode by userState.isMoreMode.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            userState.clearError()
        }
    }

    fun validateUsername(): Boolean {
        usernameError = if (username.isBlank()) "请输入账号" else null
        return usernameError == null
    }

    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> "请输入密码"
            password.length < 6 -> "密码长度不能少于6位"
            else -> null
        }
        return passwordError == null
    }

    var clickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }

    fun handleClick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > 1000) {
            clickCount = 1
        } else {
            clickCount++
        }
        lastClickTime = currentTime

        if (clickCount == 10) {
            if (isMoreMode) {
                Toast.makeText(context, "已经是隐藏模式了！", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "成功解锁隐藏模式！", Toast.LENGTH_SHORT).show()
                userState.setMoreMode(true)
            }
            clickCount = 0
        }
    }

    fun handleLogin() {
        focusManager.clearFocus()
        val isUserValid = validateUsername()
        val isPwdValid = validatePassword()
        if (!isUserValid || !isPwdValid) return
        userState.login(username, password)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(KaiqiuBlue, KaiqiuBlue.copy(alpha = 0f))
                        )
                    )
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { handleClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = KaiqiuBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "请输入开球网账号密码",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameError = null
                        },
                        label = { Text("账号") },
                        placeholder = { Text("请输入账号") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        isError = usernameError != null,
                        supportingText = usernameError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text("密码") },
                        placeholder = { Text("请输入密码") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Clear else Icons.Default.Star,
                                    contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handleLogin() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { handleLogin() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KaiqiuBlue
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("登录中...")
                        } else {
                            Text("登录", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
