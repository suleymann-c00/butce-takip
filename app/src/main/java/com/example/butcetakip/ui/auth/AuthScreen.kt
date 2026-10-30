package com.example.butcetakip.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.math.sin
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val NeonGreen = Color(0xFF00E676)
private val DeepDark = Color(0xFF0A0A0F)
private val CardDark = Color(0xFF1A1A2E)
private val GoogleRed = Color(0xFFDB4437)

@Composable
fun AnimatedWaveBackground(isLoginMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val transitionColor by animateColorAsState(
        targetValue = if (isLoginMode) DeepDark else Color(0xFFE8F5E9),
        animationSpec = tween(1000)
    )
    
    val waveColorDark = NeonGreen.copy(alpha = 0.15f)
    val waveColorLight = NeonGreen.copy(alpha = 0.35f)
    val currentWaveColor by animateColorAsState(
        targetValue = if (isLoginMode) waveColorDark else waveColorLight,
        animationSpec = tween(1000)
    )

    Canvas(modifier = Modifier.fillMaxSize().background(transitionColor)) {
        val width = size.width
        val height = size.height
        
        for (i in 0..2) {
            val path = Path()
            val yOffset = height * (0.5f + (i * 0.15f))
            val amplitude = height * 0.08f * (1f - i * 0.2f)
            val frequency = 1f + i * 0.3f
            
            path.moveTo(0f, yOffset)
            
            val step = 15f
            var x = 0f
            while (x <= width) {
                val normalizedX = x / width
                val y = yOffset + sin((normalizedX * frequency * 2 * Math.PI) + phase * (1f + i * 0.3f)) * amplitude
                path.lineTo(x, y.toFloat())
                x += step
            }
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
            
            drawPath(
                path = path,
                color = currentWaveColor,
                style = Fill
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmailSent by remember { mutableStateOf(false) }

    // Hata veya başarı mesajlarını Toast ile göster (düzeni bozmamak için)
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            android.widget.Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_LONG).show()
            kotlinx.coroutines.delay(100)
            errorMessage = null
        }
    }

    LaunchedEffect(resetEmailSent) {
        if (resetEmailSent) {
            android.widget.Toast.makeText(context, "Şifre sıfırlama linki e-posta adresinize gönderildi", android.widget.Toast.LENGTH_LONG).show()
            resetEmailSent = false
        }
    }

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1016202490713-6a3d8tugu8dfgjgkp4drmio9faahmd8a.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val firebaseCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                scope.launch {
                    try {
                        auth.signInWithCredential(firebaseCredential).await()
                        onAuthSuccess()
                    } catch(e: Exception) {
                        val detail = e.message ?: e.localizedMessage ?: "Bilinmeyen Hata"
                        errorMessage = "Firebase yetkilendirme hatası: $detail"
                        isLoading = false
                    }
                }
            } catch (e: ApiException) {
                errorMessage = "Google girişi başarısız. Hata kodu: ${e.statusCode}"
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    fun performGoogleSignIn() {
        isLoading = true
        errorMessage = null
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    fun performEmailAuth() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "E-posta ve şifre boş bırakılamaz"
            return
        }
        if (!isLoginMode && password != confirmPassword) {
            errorMessage = "Şifreler eşleşmiyor"
            return
        }
        if (!isLoginMode && password.length < 6) {
            errorMessage = "Şifre en az 6 karakter olmalı"
            return
        }

        isLoading = true
        errorMessage = null
        focusManager.clearFocus()

        scope.launch {
            try {
                if (isLoginMode) {
                    auth.signInWithEmailAndPassword(email.trim(), password).await()
                } else {
                    auth.createUserWithEmailAndPassword(email.trim(), password).await()
                }
                onAuthSuccess()
            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "E-posta veya şifre hatalı"
                    e.message?.contains("EMAIL_EXISTS") == true || e.message?.contains("already in use") == true -> "Bu e-posta zaten kayıtlı"
                    e.message?.contains("INVALID_EMAIL") == true || e.message?.contains("badly formatted") == true -> "Geçersiz e-posta adresi"
                    e.message?.contains("WEAK_PASSWORD") == true -> "Şifre çok zayıf (en az 6 karakter)"
                    e.message?.contains("network") == true -> "İnternet bağlantısı yok"
                    else -> e.localizedMessage ?: "Bir hata oluştu"
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun performPasswordReset() {
        if (email.isBlank()) {
            errorMessage = "Lütfen e-posta adresinizi girin"
            return
        }
        isLoading = true
        scope.launch {
            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                resetEmailSent = true
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Şifre sıfırlama e-postası gönderilemedi"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedWaveBackground(isLoginMode = isLoginMode)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo / Başlık
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonGreen.copy(alpha = 0.3f), NeonGreen.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Bütçe Takip",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = if (isLoginMode) Color.White else DeepDark
            )
            Text(
                "Finanslarını kontrol altına al",
                fontSize = 14.sp,
                color = if (isLoginMode) Color.White.copy(alpha = 0.5f) else DeepDark.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Giriş / Kayıt Sekmesi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLoginMode) CardDark else Color.White.copy(alpha = 0.8f))
                    .padding(4.dp)
            ) {
                listOf("Giriş Yap" to true, "Kayıt Ol" to false).forEach { (label, mode) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLoginMode == mode) NeonGreen else Color.Transparent)
                            .clickable {
                                isLoginMode = mode
                                errorMessage = null
                                showForgotPassword = false
                                resetEmailSent = false
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoginMode == mode) DeepDark else (if (isLoginMode) Color.White.copy(alpha = 0.6f) else DeepDark.copy(alpha = 0.6f)),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showForgotPassword) {
                // Şifre Sıfırlama Modu
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta", color = if (isLoginMode) Color.White.copy(alpha=0.7f) else DeepDark.copy(alpha=0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (isLoginMode) Color.White else DeepDark) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { performPasswordReset() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = if (isLoginMode) Color.White.copy(alpha = 0.2f) else DeepDark.copy(alpha = 0.2f),
                        focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen,
                        focusedTextColor = if (isLoginMode) Color.White else DeepDark,
                        unfocusedTextColor = if (isLoginMode) Color.White else DeepDark
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { performPasswordReset() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepDark)
                    } else {
                        Text("Şifre Sıfırlama Linki Gönder", color = DeepDark, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { showForgotPassword = false; errorMessage = null; resetEmailSent = false }) {
                    Text("← Giriş ekranına dön", color = NeonGreen)
                }
            } else {
                // E-posta
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("E-posta", color = if (isLoginMode) Color.White.copy(alpha=0.7f) else DeepDark.copy(alpha=0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (isLoginMode) Color.White else DeepDark) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = if (isLoginMode) Color.White.copy(alpha = 0.2f) else DeepDark.copy(alpha = 0.2f),
                        focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen,
                        focusedTextColor = if (isLoginMode) Color.White else DeepDark,
                        unfocusedTextColor = if (isLoginMode) Color.White else DeepDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Şifre
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Şifre", color = if (isLoginMode) Color.White.copy(alpha=0.7f) else DeepDark.copy(alpha=0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (isLoginMode) Color.White else DeepDark) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (isLoginMode) Color.White else DeepDark
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next),
                    keyboardActions = if (isLoginMode) KeyboardActions(onDone = { performEmailAuth() }) else KeyboardActions.Default,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = if (isLoginMode) Color.White.copy(alpha = 0.2f) else DeepDark.copy(alpha = 0.2f),
                        focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen,
                        focusedTextColor = if (isLoginMode) Color.White else DeepDark,
                        unfocusedTextColor = if (isLoginMode) Color.White else DeepDark
                    )
                )

                // Şifre Tekrar (Sadece Kayıt)
                AnimatedVisibility(visible = !isLoginMode) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            label = { Text("Şifre Tekrar", color = if (isLoginMode) Color.White.copy(alpha=0.7f) else DeepDark.copy(alpha=0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (isLoginMode) Color.White else DeepDark) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { performEmailAuth() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = if (isLoginMode) Color.White.copy(alpha = 0.2f) else DeepDark.copy(alpha = 0.2f),
                                focusedLabelColor = NeonGreen,
                                cursorColor = NeonGreen,
                                focusedTextColor = if (isLoginMode) Color.White else DeepDark,
                                unfocusedTextColor = if (isLoginMode) Color.White else DeepDark
                            )
                        )
                    }
                }

                // Şifremi Unuttum (Sadece Giriş)
                if (isLoginMode) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showForgotPassword = true; errorMessage = null }) {
                            Text("Şifremi Unuttum", color = NeonGreen.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Giriş / Kayıt Butonu
                Button(
                    onClick = { performEmailAuth() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepDark)
                    } else {
                        Text(
                            if (isLoginMode) "Giriş Yap" else "Kayıt Ol",
                            color = DeepDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = if (isLoginMode) Color.White.copy(alpha = 0.15f) else DeepDark.copy(alpha = 0.15f))
                    Text("  veya  ", color = if (isLoginMode) Color.White.copy(alpha = 0.4f) else DeepDark.copy(alpha = 0.4f), fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = if (isLoginMode) Color.White.copy(alpha = 0.15f) else DeepDark.copy(alpha = 0.15f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Google Sign-In Butonu
                OutlinedButton(
                    onClick = { performGoogleSignIn() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.linearGradient(listOf(
                            if (isLoginMode) Color.White.copy(alpha = 0.3f) else DeepDark.copy(alpha = 0.3f), 
                            if (isLoginMode) Color.White.copy(alpha = 0.1f) else DeepDark.copy(alpha = 0.1f)
                        ))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isLoginMode) Color.Transparent else Color.White),
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = GoogleRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Google ile devam et", color = if (isLoginMode) Color.White else DeepDark, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
