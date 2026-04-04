package com.example.careai

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Picture
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.careai.ui.theme.CareAiTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.util.Calendar
import java.util.Locale

// --- RANG PALITRASI ---
val PrimaryPurple = Color(0xFF0057FF)
val PrimaryContainer = Color(0xFF2B83FF)
val BackgroundColor = Color(0xFFFAF8FF)
val TextGray = Color(0xFF55596B)
val SurfaceVariant = Color(0xFFF3F3FC)
val SecondaryPink = Color(0xFFFFD8E7)
val Primary = Color(0xFF5252FF)
val OnSurface = Color(0xFF2F3334)
val OnSurfaceVariant = Color(0xFF5B6061)
private val API_KEY = "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"


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    private val requestPermissionLauncherMic = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() // Multiple qilamiz
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!audioGranted) {
            // Foydalanuvchiga mikrofon kerakligini tushuntirish mumkin
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestPermissionLauncherMic.launch(permissions.toTypedArray())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ruxsatnomalar va Kanallarni sozlash
        askNotificationPermission()
        createNotificationChannel(this)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        val prefs = getSharedPreferences("CareAI_Prefs", MODE_PRIVATE)
        val token = prefs.getString("access_token", null)

        setContent {
            CareAiTheme {
                // 1. Splash Screen holati
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    // Ma'lumotlar yuklanayotganda Splash ko'rsatiladi
                    SplashScreen(onLoadingComplete = {
                        showSplash = false
                    })
                } else {
                    // 2. Ma'lumotlar tayyor bo'lgach asosiy Navigatsiya ishga tushadi
                    val navController = rememberNavController()
                    val startScreen = if (token != null) "main_pager" else "oyna_a"

                    NavHost(navController = navController, startDestination = startScreen) {
                        composable("oyna_a") {
                            MindCareRegistrationScreen(
                                onNavigateToSignIn = { navController.navigate("oyna_b") },
                                onFinishAuth = {
                                    navController.navigate("oyna_b") {
                                        popUpTo("oyna_a") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("oyna_b") {
                            SignInScreen(
                                onBack = { navController.navigate("oyna_a") },
                                onFinishAuth = {
                                    navController.navigate("main_pager") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main_pager") {
                            MainVerticalPager(
                                onLogout = {
                                    navController.navigate("oyna_a") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                applicationContext
                            )
                        }
                    }
                }
            }
        }
    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Agar ruxsat berilmagan bo'lsa, so'raydi
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    // MainActivity onCreate ichida chaqirib qo'ying
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Vazifa eslatmalari"
            val descriptionText = "Rejalashtirilgan vazifalar uchun bildirishnomalar"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("careai_tasks", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

fun sendLocalNotification(context: Context, title: String, message: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "settings_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "settings_channel", // Bu ID quyidagi Builder'dagi bilan bir xil bo'lishi kerak
            "Settings Notifications",
            NotificationManager.IMPORTANCE_HIGH // SHU YERNI TEKSHIRING
        )
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}

// --- VERTICAL PAGER SYSTEM ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainVerticalPager(onLogout: () -> Unit = {}, context: Context) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    var isAllowedToScroll by remember { mutableStateOf(false) }
    var showConsent by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = isAllowedToScroll || pagerState.currentPage > 0
        ) { page ->
            when (page) {
                0 -> SanctuaryApp(onMicClick = { showConsent = true }, onSettings = {
                    showSettings = true
                }, context)

                1 -> ColorSelectionScreen(onNextPage = {
                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                })

                2 -> ArtTherapyScreen(onNextPage = {
                    coroutineScope.launch { pagerState.animateScrollToPage(3) }
                })

                3 -> ListenMusicScreen()

                4 -> CreativeTestScreen(
                    onBack = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showConsent && pagerState.currentPage == 0,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            ConsentNotification(
                onAccept = {
                    isAllowedToScroll = true
                    showConsent = false
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                },
                onDecline = { showConsent = false }
            )
        }

        // MainVerticalPager ichida...
        if (showSettings) {
            SettingsScreen(
                onClose = { showSettings = false },
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun SplashScreen(onLoadingComplete: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnimation")

    // 1. Logo uchun kattalashib-kichiklashish (Pulse) effekti
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    // 2. Orqa fondagi "Nafas olish" (Blur/Alpha) effekti
    val blurAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlurAlpha"
    )

    // Ma'lumotlarni yuklashni simulyatsiya qilish (masalan, 3 soniya)
    LaunchedEffect(Unit) {
        delay(3000) // Haqiqiy API chaqiruvlari tugaguncha kutish mumkin
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor), // Sizning loyihangizdagi fon rangi
        contentAlignment = Alignment.Center
    ) {
        // Orqa fondagi nafas oluvchi blur effekt
        Box(
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer(
                    scaleX = scale * 1.2f,
                    scaleY = scale * 1.2f,
                    alpha = blurAlpha
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryPurple.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
                .blur(40.dp) // Blur effekti
        )

        // Asosiy Logo
        Image(
            painter = painterResource(id = R.drawable.logo), // Logongizni qo'ying
            contentDescription = "Logo",
            modifier = Modifier
                .size(150.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )
    }
}

fun analyzeMultipleDrawings(
    drawings: Map<Int, Bitmap?>,
    scope: CoroutineScope,
    api: OpenAiService,
    onResult: (String) -> Unit
) {
    scope.launch(Dispatchers.IO) {
        try {
            val filledIndices = drawings.filter { it.value != null }.keys
            val emptyIndices = (0..8).filter { !filledIndices.contains(it) }

            // Faqat chizilgan rasmlarni yuboramiz
            val contentList = mutableListOf<Any>()

            var promptText = "Foydalanuvchi 9 ta katakdan iborat kreativlik testini topshirdi.\n"
            promptText += "Chizilgan kataklar: ${filledIndices.joinToString { (it + 1).toString() }}.\n"

            if (emptyIndices.isNotEmpty()) {
                promptText += "TUGATILMAGAN kataklar: ${emptyIndices.joinToString { (it + 1).toString() }}. " +
                        "Bu foydalanuvchining charchagani yoki asabiy ekanligidan darak berishi mumkinmi?\n"
            }

            promptText += "\nIltimos, rasmlarni va tugallanmagan ishni tahlil qilib, foydalanuvchining ruhiy holati haqida o'zbek tilida 4-5 gapda javob bering."

            contentList.add(TextContent(text = promptText))

            // Rasmlarni qo'shish
            drawings.forEach { (index, bitmap) ->
                bitmap?.let {
                    val base64 = bitmapToBase64(it)
                    contentList.add(ImageContent(image_url = ImageUrl(url = "data:image/jpeg;base64,$base64")))
                }
            }

            val request = VisionRequest(messages = listOf(VisionMessage(role = "user", content = contentList)))
            val response = api.getVisionResponse("Bearer $API_KEY", request)

            withContext(Dispatchers.Main) {
                onResult(response.choices[0].message.content)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult("Xatolik yuz berdi. Iltimos qaytadan urinib ko'ring.") }
        }
    }
}

fun DrawScope.drawInitialTriangle(color: Color) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(size.width / 2f, size.height / 3f)      // Yuqori uch
        lineTo(size.width / 4f, size.height * 2/3f)    // Chap pastki uch
        lineTo(size.width * 3/4f, size.height * 2/3f)  // O'ng pastki uch
        close() // Burchaklarni birlashtirish
    }
    // Shaffofroq chiziq bilan chizamiz
    drawPath(path, color, style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)))
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    // Rasmni JPEG formatida siqamiz (hajmi kichik bo'lishi uchun 80% sifatda)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    // Baytlarni Base64 stringga aylantiramiz
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

@Composable
fun TestGridItem(bitmap: Bitmap?, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            )
        } else {
            // Boshlang'ich shakl (har bir katakda turlicha bo'lishi mumkin)
            Canvas(modifier = Modifier.size(20.dp)) {
                drawCircle(color = Color.LightGray, style = Stroke(width = 2f))
            }
        }
    }
}

@Composable
fun CreativeTestScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val api = remember { RetrofitClient.openAiInstance }
    val scrollState = rememberScrollState()

    // 9 ta rasm uchun konteyner (Bitmap saqlash uchun)
    val drawings = remember { mutableStateMapOf<Int, Bitmap?>() }
    var currentEditingIndex by remember { mutableStateOf<Int?>(null) }
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // AI javob berganda scroll qilish
    LaunchedEffect(apiResponse) {
        if (apiResponse != null) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    if (currentEditingIndex != null) {
        // Chizish oynasi (alohida komponent)
        DrawingCanvasOverlay(
            index = currentEditingIndex!!,
            onSave = { bitmap ->
                drawings[currentEditingIndex!!] = bitmap
                currentEditingIndex = null
            },
            onCancel = { currentEditingIndex = null }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Kreativlik Testi", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Har bir katakdagi shaklni yakunlang", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                // 3x3 Jadval
                repeat(3) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(3) { colIndex ->
                            val index = rowIndex * 3 + colIndex
                            TestGridItem(
                                bitmap = drawings[index],
                                onClick = { currentEditingIndex = index },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Tahlil natijasi
                AnimatedVisibility(visible = apiResponse != null) {
                    Text(
                        text = apiResponse ?: "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(0.6f), RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tugatish tugmasi
                Button(
                    onClick = {
                        isLoading = true
                        analyzeMultipleDrawings(drawings, scope, api) { result ->
                            apiResponse = result
                            isLoading = false
                        }
                    },
                    modifier = Modifier.height(56.dp).width(220.dp).shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp), // Hajmi
                            color = Color.White,            // Rangi
                            strokeWidth = 2.dp              // Chiziq qalinligi
                        )
                    } else {
                        Text("Testni yakunlash")
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun DrawingCanvasOverlay(
    index: Int,
    onSave: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val paths = remember { mutableStateListOf<ColoredPath>() }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }
    val picture = remember { android.graphics.Picture() }
    val redrawTrigger = remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tepadagi panel (Yopish, Sarlavha, Tayyor)
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null) }
                Text("${index + 1}-shaklni to'ldiring", fontWeight = FontWeight.Bold)
                Button(onClick = { onSave(createBitmapFromPicture(picture)) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) {
                    Text("Saqlash", color = Color.White)
                }
            }

            // Chizish maydoni
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp).border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))) {
                Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> currentPath = androidx.compose.ui.graphics.Path().apply { moveTo(offset.x, offset.y) } },
                        onDrag = { change, _ -> change.consume(); currentPath?.lineTo(change.position.x, change.position.y); redrawTrigger.intValue++ },
                        onDragEnd = { currentPath?.let { paths.add(ColoredPath(it, Color.Black)) }; currentPath = null }
                    )
                }) {
                    redrawTrigger.intValue.let { }
                    val pictureCanvas = androidx.compose.ui.graphics.Canvas(picture.beginRecording(size.width.toInt(), size.height.toInt()))

                    val drawContent: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit = {
                        drawRect(Color.White) // Oq fon

                        // --- DIQQAT: Boshlang'ich uchburchakni chizamiz ---
                        drawInitialTriangle(Color.LightGray.copy(alpha = 0.5f))

                        // Foydalanuvchi chizayotgan chiziqlar (qora rangda)
                        paths.forEach { drawPath(it.path, Color.Black, style = Stroke(width = 5f, cap = StrokeCap.Round)) }
                        currentPath?.let { drawPath(it, Color.Black, style = Stroke(width = 5f, cap = StrokeCap.Round)) }
                    }

                    drawContent()
                    androidx.compose.ui.graphics.drawscope.CanvasDrawScope().draw(this, layoutDirection, pictureCanvas, size) { drawContent() }
                    picture.endRecording()
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ListenMusicScreen() {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var musicPulse by remember { mutableStateOf(1f) }

    // 1. Ruxsatnomani tekshirish
    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasPermission = isGranted }

    // 2. MediaPlayer-ni yaratish
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.meditation_music).apply {
            isLooping = true
        }
    }

    // 3. Visualizer va Lifecycle boshqaruvi
    DisposableEffect(hasPermission) {
        var visualizer: Visualizer? = null
        if (hasPermission) {
            try {
                visualizer = Visualizer(mediaPlayer.audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                            if (isPlaying && waveform != null) {
                                val average = waveform.map { Math.abs(it.toInt()) }.average()
                                // Ritm sezgirligi (1.0f dan 1.8f gacha)
                                musicPulse = 1f + (average.toFloat() / 128f) * 1.5f
                            } else {
                                musicPulse = 1f
                            }
                        }
                        override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
                    }, Visualizer.getMaxCaptureRate() / 2, true, false)
                    enabled = true
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
        onDispose {
            visualizer?.enabled = false
            visualizer?.release()
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // UI QISMI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fon (Sizdagi mavjud AtmosphericAura)
        AtmosphericAura()

        // 4. Atrof qorayishi animatsiyasi (2 soniya davom etadi)
        val darknessAlpha by animateFloatAsState(
            targetValue = if (isPlaying) 0.85f else 0f,
            animationSpec = tween(2000, easing = LinearOutSlowInEasing),
            label = "darkness"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = darknessAlpha))
        )

        if (!hasPermission) {
            // Ruxsat so'rash tugmasi
            Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("Ritm effektini yoqish uchun ruxsat bering")
            }
        } else {
            // 5. Musiqaga moslanuvchi xalqalar
            if (isPlaying) {
                repeat(4) { index ->
                    val animatedScale by animateFloatAsState(
                        targetValue = musicPulse + (index * 0.25f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "ring_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                                alpha = (0.5f - (index * 0.12f)).coerceIn(0f, 1f)
                            }
                            .background(PrimaryPurple.copy(alpha = 0.4f), CircleShape)
                            .blur(35.dp)
                    )
                }
            }

            // 6. Markaziy Play/Pause tugmasi
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .then(
                        if (isPlaying) {
                            Modifier.background(Color.White.copy(alpha = 0.1f))
                        } else {
                            Modifier.background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, PrimaryContainer)
                                )
                            )
                        }
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .clickable {
                        isPlaying = !isPlaying
                        if (isPlaying) mediaPlayer.start() else mediaPlayer.pause()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Pastki yozuv
        if (isPlaying) {
            Text(
                text = "Musiqa ritmiga mos nafas oling",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(onClose: () -> Unit, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("CareAI_Prefs", Context.MODE_PRIVATE) }

    // Foydalanuvchi ma'lumotlarini SharedPrefs dan olish
    val userName = remember { prefs.getString("user_name", "Foydalanuvchi") ?: "Foydalanuvchi" }
    val userEmail =
        remember { prefs.getString("user_email", "Email ko'rsatilmagan") ?: "Email ko'rsatilmagan" }

    var showNotifDialog by remember { mutableStateOf(false) }
    var isNotifEnabled by rememberSaveable {
        mutableStateOf(
            prefs.getBoolean(
                "notif_enabled",
                true
            )
        )
    }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showProfileInfo by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F9FE) // BackgroundColor o'rniga
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- YUQORI PANEL ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Orqaga",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Sozlamalar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6200EE)
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                // --- AKKAUNT VA PROFIL ---
                Text(
                    text = "Hisob va Profil",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(0.2f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Profil ma'lumotlari (Edit o'rniga)
                        SettingsItem(
                            icon = Icons.Default.Person,
                            title = "Profil ma'lumotlari",
                            trailing = "Ko'rish",
                            onClick = { showProfileInfo = !showProfileInfo }
                        )

                        if (showProfileInfo) {
                            Column(modifier = Modifier.padding(start = 40.dp, bottom = 8.dp)) {
                                Text("Ism: $userName", fontSize = 14.sp, color = Color.Gray)
                                Text("Email: $userEmail", fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        // Bildirishnomalar
                        SettingsItem(
                            icon = Icons.Default.NotificationsActive,
                            title = "Bildirishnomalar",
                            trailing = if (isNotifEnabled) "Yoqiq" else "O'chiq",
                            onClick = { showNotifDialog = true }
                        )

                        SettingsItem(
                            icon = Icons.Default.LockReset,
                            title = "Parolni yangilash",
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Tez kunda qo'shiladi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- ILOVA SOZLAMALARI ---
                Text(
                    text = "Ilova afzalliklari",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(0.2f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsItem(
                            icon = Icons.Default.Language,
                            title = "Til",
                            trailing = "O'zbekcha",
                            onClick = { /* Tilni o'zgartirish */ }
                        )
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "Ilova haqida",
                            onClick = {
                                Toast.makeText(context, "CareAI v1.0.2", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }

            // --- CHIQISH TUGMASI ---
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.Red.copy(0.2f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Tizimdan chiqish", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // --- CHIQISHNI TASDIQLASH DIALOGI ---
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                title = { Text("Chiqishni tasdiqlang", fontWeight = FontWeight.Bold) },
                text = { Text("Haqiqatan ham profilingizdan chiqmoqchimisiz? Barcha saqlanmagan ma'lumotlar yo'qolishi mumkin.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            // Ma'lumotlarni tozalash
                            prefs.edit().clear().apply()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ha, chiqish", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Bekor qilish", color = Color.Gray)
                    }
                }
            )
        }

        // --- BILDIRISHNOMA DIALOGI ---
        if (showNotifDialog) {
            AlertDialog(
                onDismissRequest = { showNotifDialog = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                title = { Text("Bildirishnomalar", fontWeight = FontWeight.Bold) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Xabarlarni qabul qilish", fontSize = 16.sp)
                        Switch(
                            checked = isNotifEnabled,
                            onCheckedChange = { checked ->
                                isNotifEnabled = checked
                                prefs.edit().putBoolean("notif_enabled", checked).apply()
                                if (checked) {
                                    Toast.makeText(
                                        context,
                                        "Bildirishnomalar yoqildi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotifDialog = false }) {
                        Text("Tayyor", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.weight(1f))

        if (trailing != null) {
            Text(text = trailing, color = TextGray, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray.copy(0.6f))
    }
}

@Composable
fun ConsentNotification(onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Keyingi bosqich?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Chuqurroq tinchlanishni xohlaysizmi?", fontSize = 12.sp, color = TextGray)
            }
            IconButton(onClick = onDecline) { Icon(Icons.Default.Close, null, tint = Color.Red.copy(0.5f)) }
            IconButton(onClick = onAccept) { Icon(Icons.Default.Check, null, tint = PrimaryPurple) }
        }
    }
}

// --- AUTH COMPONENTS WITH NESTED SCROLL ---
@Composable
fun AuthWrapper(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val overscrollOffset = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollOffset.value > 0 && available.y < 0) {
                    val consumed = available.y * 0.5f
                    coroutineScope.launch { overscrollOffset.snapTo((overscrollOffset.value + consumed).coerceAtLeast(0f)) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) {
                    coroutineScope.launch { overscrollOffset.snapTo(overscrollOffset.value + available.y * 0.2f) }
                }
                return Offset.Zero
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                coroutineScope.launch {
                    overscrollOffset.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)
        .nestedScroll(nestedScrollConnection)) {
        Box(modifier = Modifier
            .size(400.dp)
            .offset((-100).dp, (-100).dp)
            .blur(100.dp)
            .background(PrimaryContainer.copy(0.3f), CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .graphicsLayer { translationY = overscrollOffset.value }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Care AI", color = PrimaryPurple, fontWeight = FontWeight.ExtraBold, modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth())
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = title, fontSize = 48.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(text = subtitle, color = TextGray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    content()
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}



@Composable
fun MindCareRegistrationScreen(onNavigateToSignIn: () -> Unit, onFinishAuth: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Har bir maydon uchun state
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    AuthWrapper(
        title = "Ro'yxatdan\no'ting.",
        subtitle = "Care AI raqamli xotirjamlik maskaniga xush kelibsiz."
    ) {
        // Full Name maydoni
        InputField(
            label = "Full Name",
            placeholder = "Ali Valiyev",
            icon = Icons.Default.Person,
            value = fullName,
            onValueChange = { fullName = it }
        )

        // Email maydoni
        InputField(
            label = "Email Address",
            placeholder = "example@mail.com",
            icon = Icons.Default.Email,
            value = email,
            onValueChange = { email = it }
        )

        // Password maydoni
        InputField(
            label = "Create Password",
            placeholder = "********",
            icon = Icons.Default.Lock,
            isPassword = true,
            value = password,
            onValueChange = { password = it }
        )

        // Password Confirm maydoni
        InputField(
            label = "Confirm Password",
            placeholder = "********",
            icon = Icons.Default.Lock,
            isPassword = true,
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it }
        )

        // Ro'yxatdan o'tish tugmasi
        PrimaryAuthButton("Ro'yxatdan o'tish") {
            // Avvalgi javobda yozilgan handleRegister funksiyasini chaqiramiz
            handleRegister(
                email = email,
                fullName = fullName, // Bu fullName ham username, ham full_name bo'lib ketadi
                pass1 = password,
                pass2 = passwordConfirm,
                scope = scope,
                context = context,
                onSuccess = onFinishAuth
            )
        }

        AuthFooter("Akkaunt bormi?", "Kirish", onClick = onNavigateToSignIn)
    }
}

@Composable
fun SignInScreen(onBack: () -> Unit, onFinishAuth: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") } // Email yoki Username uchun
    var password by remember { mutableStateOf("") }

    AuthWrapper(
        title = "O'zingni\nTinchilantir.",
        subtitle = "Xush kelibsiz, Sanctuary sizni kutmoqda."
    ) {
        // Login/Username maydoni
        InputField(
            label = "Username or Email",
            placeholder = "example@mail.com",
            icon = Icons.Default.Person,
            value = username,
            onValueChange = { username = it }
        )

        // Parol maydoni
        InputField(
            label = "Password",
            placeholder = "********",
            icon = Icons.Default.Lock,
            isPassword = true,
            value = password,
            onValueChange = { password = it }
        )

        // Kirish tugmasi
        PrimaryAuthButton("Kirish") {
            handleLogin(
                email = username,
                pass = password,
                scope = scope,
                context = context,
                onSuccess = onFinishAuth
            )
        }

        // Akkaunt yo'q bo'lsa Registerga qaytish
        AuthFooter("Akkaunt yo'qmi?", "Ochish", onClick = onBack)
    }
}

fun handleLogin(
    email: String,
    pass: String,
    scope: CoroutineScope,
    context: Context,
    onSuccess: () -> Unit
) {
    scope.launch(Dispatchers.IO) {
        try {
            val request = LoginRequest(email = email, password = pass)
            val response = RetrofitClient.instance.loginUser(request)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val body = response.body()

                    // Rasmga ko'ra body ichida to'g'ridan-to'g'ri "access" bor
                    val token = body?.access

                    if (token != null) {
                        // 1. Tokenni SharedPreferences-ga saqlash
                        val prefs = context.getSharedPreferences("CareAI_Prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("access_token", token).apply()

                        // 2. Foydalanuvchi ismini ham saqlab qo'ysak bo'ladi (Profillar uchun)
                        prefs.edit().putString("user_name", body.user.full_name).apply()

                        Toast.makeText(context, "Xush kelibsiz, ${body.user.full_name}!", Toast.LENGTH_SHORT).show()

                        // 3. Main sahifaga o'tish
                        onSuccess()
                    }
                } else {
                    // Masalan: 401 Unauthorized bo'lsa
                    Toast.makeText(context, "Login yoki parol xato!", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ulanishda xatolik: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontWeight = FontWeight.Medium, color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryPurple) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = Color.LightGray
            )
        )
    }
}

fun handleRegister(
    email: String,
    fullName: String,
    pass1: String,
    pass2: String,
    scope: CoroutineScope,
    context: Context,
    onSuccess: () -> Unit
) {
    // Validatsiya...
    if (pass1 != pass2) {
        Toast.makeText(context, "Parollar mos kelmadi", Toast.LENGTH_SHORT).show()
        return
    }

    scope.launch(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                email = email,
                username = fullName,
                full_name = fullName,
                password = pass1,
                password2 = pass2 // Rasmda ko'ringan password2 maydoni
            )

            val response = RetrofitClient.instance.registerUser(request)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Muvaffaqiyatli ro'yxatdan o'tdingiz! Endi kiring.", Toast.LENGTH_LONG).show()
                    onSuccess() // Bu Login sahifasiga olib o'tadi
                } else {
                    val error = response.errorBody()?.string() ?: "Xatolik"
                    Toast.makeText(context, "Xato: $error", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Tarmoq xatosi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
@Composable
fun PrimaryAuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(30.dp), contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(PrimaryPurple, Color(0xFFB66DFF)))), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AuthFooter(label: String, action: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, color = TextGray)
        TextButton(onClick = onClick) { Text(action, color = PrimaryPurple, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun SanctuaryApp(onMicClick: () -> Unit, onSettings: () -> Unit, context: Context) {
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericAura()
        val displayName by remember {
            mutableStateOf(getUserName(context))
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onSettings = onSettings, userName =  displayName)
            Spacer(modifier = Modifier.weight(0.5f))
            AnimatedVoiceAvatar(onMicClick)
            Spacer(modifier = Modifier.height(48.dp))
            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.weight(1f))
            ActionButtons()
            Text("SIZNING MAXFIYLIGINGIZ BIZ TOMONIMIZDAN TAMINLANADI!", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant.copy(0.4f)), modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
fun ColorSelectionScreen(onNextPage: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { RetrofitClient.openAiInstance }
    val apiKey = API_KEY // SharedPreferences dan olsangiz ham bo'ladi

    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {

        AtmosphericAura()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Hozirgi holatingizni\nqaysi ranglar ifodalaydi?",
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                color = OnSurface
            )

            Text(
                text = "1 tadan 3 tagacha rang tanlang",
                fontSize = 14.sp,
                color = OnSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Ranglar Grid qismi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val rows = diagnosticColors.chunked(3)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { item ->
                                val isSelected = selectedIds.contains(item.id)
                                ColorCube(
                                    item = item,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            selectedIds = selectedIds - item.id
                                        } else if (selectedIds.size < 3) {
                                            selectedIds = selectedIds + item.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // AI Analizi natijasi
            AnimatedVisibility(
                visible = apiResponse != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                apiResponse?.let {
                    Text(
                        text = it,
                        fontSize = 15.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 32.dp, bottom = 24.dp)
                            .background(Color.White.copy(0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }

        // TUGMALAR QISMI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (apiResponse == null) {
                AnimatedVisibility(
                    visible = selectedIds.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            startColorAnalysis(selectedIds, scope, apiKey, api) { result ->
                                apiResponse = result
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .width(220.dp)
                            .shadow(8.dp, CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Tahlil qilish", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Button(
                    onClick = onNextPage,
                    modifier = Modifier
                        .height(56.dp)
                        .width(200.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Keyingisi", color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
// Bu funksiyani ColorSelectionScreen ichida yoki alohida Helper klassda ishlating
fun startColorAnalysis(
    selectedIds: Set<Int>,
    scope: CoroutineScope,
    apiKey: String,
    api: OpenAiService, // Sizning Retrofit interfeysingiz
    onResult: (String) -> Unit
) {
    val selectedColorNames = diagnosticColors
        .filter { it.id in selectedIds }
        .joinToString(", ") { it.name }

    scope.launch(Dispatchers.IO) {
        try {
            val systemPrompt = """
                Siz professional rangshunos psixologsiz. 
                Foydalanuvchi hozirgi holatiga qarab 1 tadan 3 tagacha rang tanladi. 
                Sizning vazifangiz:
                1. Tanlangan ranglar kombinatsiyasini psixologik tahlil qiling (Lusher testi elementlaridan foydalaning).
                2. Foydalanuvchining ehtimoliy holatini (asabiylik, charchoq, quvonch, depressiya va h.z.) ayting.
                3. Javobni juda samimiy, qisqa (3-4 ta gap) va faqat O'ZBEK tilida yozing.
                4. "Siz tanlagan ranglar shuni ko'rsatadiki..." deb boshlang.
            """.trimIndent()

            val chatRequest = ChatRequest2(
                model = "gpt-4o",
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = "Tanlangan ranglar: $selectedColorNames")
                )
            )

            val response = api.getChatResponse("Bearer $apiKey", chatRequest)
            val result = response.choices[0].message.content

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult("Analiz qilishda biroz texnik xatolik bo'ldi. Iltimos, qayta urinib ko'ring.")
            }
        }
    }
}
@Composable
fun RowScope.ColorCube(item: ColorItem, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(20.dp))
            .background(item.color)
            .border(if (isSelected) 3.dp else 0.dp, Color.White, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White)
    }
}
// ... importlar ...

@Composable
fun ArtTherapyScreen(onNextPage: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { RetrofitClient.openAiInstance }
    val apiKey = API_KEY

    // State-lar
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var selectedOutlineId by remember { mutableIntStateOf(0) }
    var showColoringCanvas by remember { mutableStateOf(false) }
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    // Chizish uchun state-lar
    val scrollState = rememberScrollState()
    val picture = remember { Picture() }
    val paths = remember { mutableStateListOf<ColoredPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var activeColor by remember { mutableStateOf(Color(0xFF6200EE)) }
    var redrawTrigger by remember { mutableStateOf(0) }

    val outlinePainter = if (selectedOutlineId != 0) painterResource(selectedOutlineId) else null

    // --- YORDAMCHI FUNKSIYALAR ---

    fun getMusicResource(resId: Int): Int {
        return when (resId) {
            R.drawable.tree -> R.raw.shamol
            R.drawable.mountain -> R.raw.rain
            R.drawable.forest -> R.raw.forest
            R.drawable.moshina -> R.raw.shahar
            R.drawable.apple -> R.raw.meditation_music
            R.drawable.qush -> R.raw.forest
            R.drawable.kema -> R.raw.ocean
            R.drawable.shlyapa -> R.raw.meditation_music
            else -> R.raw.ocean
        }
    }

    val resetArtSession = {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        showColoringCanvas = false
        selectedOutlineId = 0
        paths.clear()
        apiResponse = null
        isLoading = false
        currentPath = null
    }

    // --- EFFEKTLAR ---

    LaunchedEffect(apiResponse) {
        if (apiResponse != null) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(selectedOutlineId, showColoringCanvas) {
        if (selectedOutlineId != 0 && showColoringCanvas) {
            mediaPlayer?.stop()
            mediaPlayer?.release()

            val musicRes = getMusicResource(selectedOutlineId)
            mediaPlayer = MediaPlayer.create(context, musicRes).apply {
                isLooping = true
                if (!isMuted) start()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    val outlines = listOf(
        R.drawable.tree, R.drawable.mountain, R.drawable.forest,
        R.drawable.apple, R.drawable.kema, R.drawable.moshina,
        R.drawable.qush, R.drawable.shlyapa
    )

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        AtmosphericAura()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (!showColoringCanvas) "Shaklni tanlang" else "Barmog'ingiz bilan bo'yang",
                fontSize = 26.sp,
                fontWeight = FontWeight.Light,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (!showColoringCanvas) {
                // --- 1. TANLASH BOSQICHI (3 USTUNLI GRID) ---
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    outlines.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { resId ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.White.copy(0.4f))
                                        .border(1.dp, Color.White.copy(0.5f), RoundedCornerShape(24.dp))
                                        .clickable {
                                            selectedOutlineId = resId
                                            showColoringCanvas = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(painterResource(resId), null, modifier = Modifier.size(60.dp))
                                }
                            }
                            if (rowItems.size < 3) {
                                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            } else {
                // --- 2. BO'YASH BOSQICHI ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray.copy(0.3f), RoundedCornerShape(32.dp))
                ) {
                    // CANVAS (Pastki qatlam)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(activeColor) {
                                detectDragGestures(
                                    onDragStart = { offset -> currentPath = Path().apply { moveTo(offset.x, offset.y) } },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPath?.lineTo(change.position.x, change.position.y)
                                        redrawTrigger++
                                    },
                                    onDragEnd = {
                                        currentPath?.let { paths.add(ColoredPath(it, activeColor)) }
                                        currentPath = null
                                    }
                                )
                            }
                    ) {
                        redrawTrigger.let { }
                        if (size.width > 0 && size.height > 0 && outlinePainter != null) {
                            val pictureCanvas = androidx.compose.ui.graphics.Canvas(picture.beginRecording(size.width.toInt(), size.height.toInt()))
                            val drawEverything: DrawScope.() -> Unit = {
                                drawRect(color = Color.White, size = size)
                                paths.forEach { drawPath(it.path, it.color, style = Stroke(45f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                                currentPath?.let { drawPath(it, activeColor, style = Stroke(45f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                                with(outlinePainter) { draw(size, colorFilter = ColorFilter.tint(Color.Black.copy(0.8f))) }
                            }
                            drawEverything()
                            CanvasDrawScope().draw(this, layoutDirection, pictureCanvas, size) { drawEverything() }
                            picture.endRecording()
                        }
                    }

                    // BOSHQARUV TUGMALARI (Yuqori qatlam - Z-Index o'rniga Box-ning oxirida)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                if (isMuted) mediaPlayer?.setVolume(0f, 0f) else mediaPlayer?.setVolume(1f, 1f)
                            },
                            modifier = Modifier.background(Color.White.copy(0.8f), CircleShape).size(40.dp)
                        ) {
                            Icon(if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = { resetArtSession() },
                            modifier = Modifier.background(Color.White.copy(0.8f), CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.Red.copy(0.8f), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // RANG TANLASH PANELI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color.White.copy(0.4f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(activeColor).border(2.dp, Color.White, CircleShape))
                    Box(modifier = Modifier.weight(1f)) { CustomColorPicker(onColorSelected = { activeColor = it }) }
                    IconButton(
                        onClick = { activeColor = Color.White },
                        modifier = Modifier.size(40.dp).background(if(activeColor == Color.White) Color.LightGray else Color.Transparent, CircleShape)
                    ) {
                        Icon(Icons.Default.Clear, "Eraser", tint = if(activeColor == Color.White) PrimaryPurple else Color.Gray)
                    }
                }
            }

            // --- 3. ANALIZ VA TUGMALAR ---
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp, top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = apiResponse != null, enter = expandVertically() + fadeIn()) {
                    apiResponse?.let {
                        Text(it, fontSize = 15.sp, color = Color.Black.copy(0.7f), textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.6f), RoundedCornerShape(20.dp)).padding(16.dp))
                    }
                }

                if (showColoringCanvas) {
                    Spacer(modifier = Modifier.height(24.dp))
                    if (apiResponse == null) {
                        Button(
                            onClick = {
                                isLoading = true
                                val bitmap = createBitmapFromPicture(picture)
                                analyzeArtTherapy(bitmap, scope, apiKey, api) { result ->
                                    apiResponse = result
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.height(56.dp).width(220.dp).shadow(8.dp, CircleShape),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Tugatish va Tahlil", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onNextPage,
                            modifier = Modifier.height(56.dp).width(200.dp).shadow(8.dp, CircleShape),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(0.2f))
                        ) {
                            Text("Keyingisi", color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomColorPicker(
    onColorSelected: (Color) -> Unit,
    initialColor: Color = Color.Red
) {
    // Hue qiymatini saqlaymiz (0f dan 360f gacha)
    var hue by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(15.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red, Color.Yellow, Color.Green,
                            Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                        )
                    )
                )
                .pointerInput(Unit) {
                    // Ham bosganda, ham surgan (drag) holatda ishlashi uchun
                    fun updateHue(positionX: Float) {
                        val newHue = (positionX / size.width).coerceIn(0f, 1f) * 360f
                        hue = newHue
                        // HSV dan Compose Color ga o'tkazish
                        onColorSelected(Color.hsv(hue, 1f, 1f))
                    }

                    detectTapGestures { offset -> updateHue(offset.x) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val newHue = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                        hue = newHue
                        onColorSelected(Color.hsv(hue, 1f, 1f))
                    }
                }
        )
    }
}

fun analyzeArtTherapy(
    bitmap: Bitmap,
    scope: CoroutineScope,
    apiKey: String,
    api: OpenAiService,
    onResult: (String) -> Unit
) {
    scope.launch(Dispatchers.IO) {
        try {
            // 1. Bitmapni siqish va Base64 ga o'tkazish
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // 2. Vision So'rovini tayyorlash
            val request = VisionRequest(
                messages = listOf(
                    VisionMessage(
                        role = "user",
                        content = listOf(
                            TextContent(text = """
                                Siz professional psixolog va art-terapevtsiz. 
                                Rasmni tahlil qiling: 
                                1. Ranglar (issiq/sovuq) va ularning hissiy holatga ta'siri.
                                2. Chiziqlar sifati: chiziqdan chiqib ketilganmi? Bu ichki asabiylikmi yoki xotirjamlikmi?
                                3. Faqat O'ZBEK tilida, 3-4 gapda samimiy javob bering.
                            """.trimIndent()),
                            ImageContent(image_url = ImageUrl(url = "data:image/jpeg;base64,$base64Image"))
                        )
                    )
                )
            )

            val response = api.getVisionResponse("Bearer $apiKey", request)
            val aiText = response.choices[0].message.content

            withContext(Dispatchers.Main) {
                onResult(aiText)
            }
        } catch (e: Exception) {
            Log.e("CareAI_Vision", "Xato: ${e.message}")
            withContext(Dispatchers.Main) {
                onResult("Rasm tahlilida texnik xatolik bo'ldi, lekin sizning ijodingiz juda go'zal!")
            }
        }
    }
}

fun saveBitmapToPublicGallery(context: Context, bitmap: Bitmap): String {
    val fileName = "ArtTherapy_${System.currentTimeMillis()}.png"
    var uri: Uri? = null
    val contentResolver = context.contentResolver

    // Android 10 va undan yuqori versiyalar uchun (Scoped Storage)
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // DCIM papkasi ichida ArtTherapy nomli papka ochadi
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/ArtTherapy")
        }
    }

    return try {
        uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            "Rasm Galereyaga saqlandi (DCIM)"
        } ?: "Xatolik: Uri yaratilmadi"
    } catch (e: Exception) {
        e.printStackTrace()
        "Saqlashda xatolik: ${e.message}"
    }
}

// Picture-dan Bitmap yaratish funksiyasi
fun createBitmapFromPicture(picture: Picture): Bitmap {
    val bitmap = Bitmap.createBitmap(picture.width.coerceAtLeast(1), picture.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawPicture(picture)
    return bitmap
}

@Composable
fun AnimatedVoiceAvatar(onMicClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition("")

    val ts by infiniteTransition.animateFloat(
        initialValue = 75f,
        targetValue = 122f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "ts"
    )

    val te by infiniteTransition.animateFloat(
        initialValue = 110f,
        targetValue = 75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "te"
    )

    val be by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 105f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "be"
    )

    val bs by infiniteTransition.animateFloat(
        initialValue = 115f,
        targetValue = 79f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "bs"
    )

// Shaklni qo'llash
    val shape = RoundedCornerShape(
        topStart = ts.dp,
        topEnd = te.dp,
        bottomEnd = be.dp,
        bottomStart = bs.dp
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(350.dp)
            .clickable {
                onMicClick()
            }// Umumiy konteyner
    ) {
        // 1. GLOW (NUR) - Endi Canvas bilan chizamiz
        Canvas(modifier = Modifier.size(250.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PrimaryContainer.copy(alpha = 0.4f), // Markazda yorqinroq
                        PrimaryContainer.copy(alpha = 0.4f), // O'rtada xira
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension / 1.5f // Nur radiusi (o'zingizga qarab o'zgartiring)
                ),
                radius = size.minDimension / 1.2f
            )
        }

        // 2. MARKAZIY SHAKL
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(PrimaryContainer, Color(0xFFD4E4FA))
                    )
                )
                .border(1.5.dp, Color.White.copy(0.5f), shape)
        )
    }
}

@Composable
fun ActionButtons() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // API Sozlamalari (O'zingizni kalitingizni kiriting)
    val apiKey = API_KEY

    // 1. Audio va Media Obyektlari
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val recorder = remember { AudioRecorder(context) } // Pastda klassi bor
    val audioFile = remember { File(context.cacheDir, "user_voice.m4a") }
    val conversationHistory = mutableListOf<Message>()
    var isRecording by remember { mutableStateOf(false) }
    var isWaitingResponse by remember { mutableStateOf(false) }
    val messages = mutableListOf<Message>()
    // 2. OpenAI bilan muloqot mantiqi
    fun startAIInteraction() {
        isWaitingResponse = true
        scope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.openAiInstance

                // STEP 1: Whisper (Ovozni matnga aylantirish)
                val requestFile = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)

                // modelPart ni alohida RequestBody sifatida berish kerak
                val modelPart = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())

                val sttResult = api.speechToText("Bearer $apiKey", body, modelPart)

                if (sttResult.text.isNotEmpty()) {

                    // --- PSIXOLOG PROMPT QISMI ---
                    val systemInstructions = """
                            Siz professional psixolog, suhbat terapevti va emotsional qo‘llab-quvvatlovchi AI assistentsiz. Ismingiz - CareAI. Sizning asosiy maqsadingiz foydalanuvchiga stress, xavotir, depressiya, g‘azab va boshqa salbiy hissiy holatlardan chiqishga yordam berish, ularni tinchlantirish va ichki muvozanatni tiklashdir.
                            
                            ----ASOSIY VA SHU YONDASHUVGA ASOSLANIB SUXBATLASH ---
                                ---- Siz har qanday holatda, har qanday tilga faqat O'zbek tilida javob berasiz----
                                ---- Siz Kam gapirasiz, 2 dona gap yetadi----
                                ---- Siz ko'proq eshitasiz va ozgina javob bilan suxbatdoshingizga yoqadigan so'zlarni gapirasiz----
                                ---- Depresiya Stress va Jahil ustidagi insonlar ko'p gapni yoqtirishmaydi bu salbiy tarafga olib bora shu sababli siz kam va qisqa javob berasiz har doim ----
                                ---- Suxbatdoshingizni yaxshilab tinglab unga eng to'g'ri bitta maslahatni juda ham qisqa qilib tushuntirasiz----
                                ---- Ko'p gapirmang!!!----
                            
                            ### **Suhbat qoidalari**  
                            - Har doim faqat O‘zbek tilida gapiring.  
                            - Foydalanuvchining his-tuyg‘ularini chuqur tushunishga harakat qiling va ularni validatsiya qiling.  
                            - Javoblar tabiiy, samimiy va insoniy bo‘lsin.  
                            - Har bir javobda empatiya → tushuntirish → yo‘naltirish ketma-ketligini saqlang.  
                            - Foydalanuvchi xavfli fikrlar bildirsa (o‘ziga yoki boshqalarga zarar yetkazish) darhol rad eting, ammo kuchli empatiya bilan javob bering va professional yordamga yo‘naltiring.  
                            - Agar foydalanuvchi psixologiya bilan bog‘liq bo‘lmagan savol bersa, qat’iy, muloyim rad javob berib, suhbatni uning ruhiy holatiga qaytaring.  
                            - Javoblar moslashuvchan bo‘lsin: foydalanuvchining ohangi, muammosi va energiyasiga qarab baland yoki past empatiya ko‘rsating.  
                            - Har doim oldingi suhbat kontekstini hisobga oling, foydalanuvchining ilgari aytgan muammolarini eslab qoling.  
                            - Takrorlanishdan saqlaning: har javob yangi qiymat berishi va konkret yordam ko‘rsatishi kerak.  
                            
                         
                            ### **Psixologik metodlar**  
                            - Kognitiv-behavioral terapiya (KBT / CBT)  
                            - Dialektik xulq-atvor terapiyasi (DBT)  
                            - Mindfulness (ongli hozirlik) va grounding texnikalari  
                            - Aktiv tinglash (active listening)  
                            - Empatiya va hissiy validatsiya  
                            - Socratic questioning (fikrni savollar orqali ochish)  
                            - Cognitive restructuring (salbiy fikrlarni qayta shakllantirish)  
                            - Stressni boshqarish va nafas olish texnikalari  
                            - Emotsiyani regulyatsiya qilish (emotion regulation)  
                            - De-escalation (g‘azabni pasaytirish texnikalari)  
                            - Rang psixologiyasi va kreativ terapiya  
                            - Musiqa terapiyasi va hayot tarzi nazorati  
                            
                            ---
                            
                            
                            ---
                            
                            ⚠️ SALBIY FIKRLAR BILAN ISHLASH:
                            - Agar foydalanuvchi o‘zini past baholasa, umidsizlik yoki negativ fikrlar bildirsa:
                                1. Avval bu hislarni tan oling ("Bu siz uchun juda og‘ir bo‘lsa kerak...")
                                2. Keyin KBT usuli bilan fikrni tahlil qiling
                                3. Salbiy fikrlarni yumshoq tarzda qayta shakllantiring (reframing)
                                4. Alternativ, realistik va sog‘lom fikrlarni taklif qiling
                            
                            ⚠️ G‘AZAB VA STRESS HOLATIDA:
                            - Agar foydalanuvchi jahldor yoki stressda bo‘lsa:
                                1. Uni tinchlantiring
                                2. Nafas olish mashqlari taklif qiling (masalan: 4-4-6 usuli)
                                3. Vaziyatni sekinlashtirishga yordam bering
                                4. Reaksiya emas, javob berishni o‘rgating
                            
                            ⚠️ XAVFLI HOLATLAR (JUDA MUHIM):
                            - Agar foydalanuvchi o‘ziga zarar yetkazish yoki boshqalarga zarar yetkazish haqida gapirsa:
                                1. Hech qachon buni qo‘llab-quvvatlamang
                                2. Juda kuchli empatiya bilan javob bering
                                3. Bu hislar vaqtinchalik ekanini tushuntiring
                                4. Uni bu yo‘ldan qaytarishga harakat qiling
                                5. Professional yordamga murojaat qilishni tavsiya qiling
                                6. Yaqin insonlar bilan gaplashishni maslahat bering
                                7. Har qanday zarar yetkazish usullarini tushuntirmang
                                
                                ⚠️ MAVZUNI BOSHQARISH QOIDASI (STRICT MODE):
                                - Agar foydalanuvchi psixologiya yoki hissiy holatga aloqador bo‘lmagan mavzuda savol bersa (masalan: texnologiya, kod yozish, yangiliklar, ovqat, sport va boshqalar):
                                    1. Darhol bu mavzuni davom ettirmang
                                    2. Juda muloyim, lekin qat’iy tarzda rad eting
                                    3. Quyidagi mazmunda javob bering (moslashtirib):
                                       "Men sizning ruhiy holatingizni yaxshilash va sizni tinchlantirish uchun shu yerdaman. Afsuski, boshqa mavzularda gaplasha olmayman. Agar xohlasangiz, hozir sizni nima bezovta qilayotganini birga ko‘rib chiqamiz."
                                    4. Suhbatni yana foydalanuvchining his-tuyg‘ulariga qaytaring
                                    5. Har doim foydalanuvchini ichki holati haqida gapirishga yo‘naltiring

                                - Hech qachon boshqa mavzuga kirib ketmang, hatto foydalanuvchi qayta-qayta urinib ko‘rsa ham
                                - Har doim asosiy maqsad: foydalanuvchini tinchlantirish va psixologik yordam berish
                        
                            ⚠️ XAVFLARNING OLDINI OLISH:
                            - Har doim potensial xavflarni oldindan sezishga harakat qiling
                            - Foydalanuvchini xavfsiz va sog‘lom qarorlar qabul qilishga yo‘naltiring
                            - Impulsiv qarorlarni kamaytirishga yordam bering
                            - “Pause and reflect” strategiyasini qo‘llang
                            
                            🧠 QO‘SHIMCHA TEXNIKALAR:
                            - Grounding: “Atrofingizda 5 ta ko‘rgan narsani ayting...”
                            - Mindfulness: hozirgi momentga qaytarish
                            - Kichik qadamlar strategiyasi
                            - O‘z-o‘ziga mehr (self-compassion)
                            - Thought labeling (“Bu faqat fikr, haqiqat emas”)
                            
                            MUHIM CHEKLOV:
                            - Faqat psixologiya va hissiy holat haqida gapiring
                            - Boshqa mavzularni muloyimlik bilan rad eting
                            
                            Format:
                            1. Empatiya (hisni tan olish)
                            2. Tushuntirish yoki yordam
                            3. Savol yoki yo‘naltirish
                            
                            Sizning maqsadingiz:
                            Foydalanuvchini tinchlantirish, unga o‘zini tushunishga yordam berish va uni asta-sekin sog‘lom fikrlashga olib borish.
                            
                            🧠 SUHBATNI INSONIY VA TABIIY QILISH (JUDA MUHIM):

                            - Hech qachon bir xil gaplarni qayta-qayta ishlatmang
                            - Har safar javobni boshqacha uslubda boshlang
                            - Bir xil iboralarni (masalan: "Men sizni tushunaman") doimiy takrorlamang
                            - Sinonimlar, turli iboralar va tabiiy gapirish uslubidan foydalaning

                            - Javoblar 100% real inson kabi bo‘lishi kerak:
                                - Ba’zida qisqaroq, ba’zida biroz kengroq yozing
                                - Ba’zida savol bilan boshlang, ba’zida empatiya bilan
                                - Juda mukammal emas, balki tabiiy va samimiy bo‘ling

                            🧠 XOTIRA VA KONTEKST BILAN ISHLASH:

                            - Har doim oldingi suhbatlarni (conversation history) hisobga oling
                            - Foydalanuvchi ilgari aytgan muammolarni eslab qoling
                            - Agar user oldin ham shu muammoni aytgan bo‘lsa:
                                - "Siz oldin ham bu haqida gapirgandingiz..." kabi murojaat qiling
                            - Javoblar har doim oldingi kontekstga bog‘langan bo‘lsin

                            - Agar foydalanuvchi rivojlanayotgan bo‘lsa:
                                - buni e’tirof eting ("Oldingiga qaraganda yaxshiroq ko‘rinayapsiz")

                            - Agar foydalanuvchi stuck holatda bo‘lsa:
                                - yangi yondashuv taklif qiling (har safar bir xil maslahat bermang)

                            🧠 MOSLASHUVCHANLIK (ADAPTIVE RESPONSE):

                            - Har bir user uchun bir xil javob bermang
                            - Foydalanuvchining:
                                - ohangi (xafa, jahldor, sovuq)
                                - muammosi
                                - energiyasiga qarab moslashing

                            - Agar user:
                                - kam gapirsa → savol bering
                                - ko‘p gapirsa → umumlashtiring
                                - hissiy bo‘lsa → empatiyani oshiring

                            🧠 NATURAL SUHBAT QOIDALARI:

                            - Juda rasmiy gapirmang
                            - Juda kitobiy gapirmang
                            - Oddiy, insoniy va samimiy bo‘ling

                            - Ba’zida:
                                - "Hmm..."
                                - "Tushunarli..."
                                - "Qiyin holat ekan..."

                            kabi tabiiy kirishlardan foydalaning

                            - Lekin haddan oshirmang (balansni saqlang)

                            🧠 TAKRORLANISHNI OLDINI OLISH:

                            - Agar bir xil maslahat berilgan bo‘lsa:
                                - uni qayta takrorlamang
                                - yangi texnika yoki boshqa yondashuv bering

                            - Har javob:
                                - yangi qiymat (value) berishi kerak
                                - faqat umumiy gaplar emas, konkret yordam bo‘lsin

                            🧠 MAQSAD:

                            Siz oddiy chatbot emassiz. Siz:
                            - real insondek gapiradigan
                            - eslab qoladigan
                            - moslashadigan
                            - foydalanuvchini asta-sekin yaxshilaydigan terapevtsiz
                            """.trimIndent()
                    messages.add(Message("system", systemInstructions))
                    messages.addAll(conversationHistory)
                    messages.add(Message("user", sttResult.text))
                    // STEP 2: GPT (Javob olish)
                    val chatRequest = ChatRequest2(
                        model = "gpt-4o",
                        messages = messages
                    )

                    val gptResponse = api.getChatResponse("Bearer $apiKey", chatRequest)
                    val aiReply = gptResponse.choices[0].message.content
                    val styledReply = when {
                        aiReply.contains("xavotir", true) -> "Tinch va taskin beruvchi ohangda: $aiReply"
                        aiReply.contains("xafa", true) -> "Yumshoq va empatiya bilan: $aiReply"
                        else -> "$aiReply"
                    }
                    // STEP 3: TTS (Matnni audioga aylantirish)
                    val ttsParams = mapOf(
                        "model" to "tts-1-hd",
                        "input" to styledReply,
                        "voice" to "nova" // Psixolog uchun eng yumshoq ovoz
                    )
                    val ttsResponse = api.textToSpeech("Bearer $apiKey", ttsParams)
                    conversationHistory.add(Message("user", sttResult.text))
                    conversationHistory.add(Message("assistant", aiReply))

                    // STEP 4: Audioni saqlash va ExoPlayer orqali ijro etish
                    val ttsFile = File(context.cacheDir, "ai_reply.mp3")
                    ttsFile.writeBytes(ttsResponse.bytes())
                    if (conversationHistory.size > 20) {
                        conversationHistory.removeAt(0)
                    }
                    withContext(Dispatchers.Main) {
                        val mediaItem = MediaItem.fromUri(Uri.fromFile(ttsFile))
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()
                        exoPlayer.play()
                        isWaitingResponse = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Xato: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("CareAI_Error", "Xato: ${e.message}", e)
                    isWaitingResponse = false
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SecondaryButton(Icons.Default.PhoneAndroid)

        Spacer(modifier = Modifier.width(32.dp))

        // --- GLOW ANIMATSIYASI (Sizning kodingiz) ---
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1.7f,
            targetValue = if (isWaitingResponse || isRecording) 1.6f else 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isWaitingResponse || isRecording) 0.1f else 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (isWaitingResponse || isRecording) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = alpha), Color.Transparent),
                            center = center,
                            radius = (size.minDimension / 2.5f) * scale
                        )
                    )
                }
            }

            // --- ASOSIY MIKROFON TUGMASI ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Primary, PrimaryContainer)))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                if (!isWaitingResponse) {
                                    try {
                                        isRecording = true
                                        recorder.startRecording(audioFile)

                                        val startTime = System.currentTimeMillis()
                                        awaitRelease() // Tugma qo'yib yuborilguncha kutadi

                                        // Agar foydalanuvchi juda tez (0.5 sekdan kam) bosib qo'yib yuborgan bo'lsa
                                        val duration = System.currentTimeMillis() - startTime
                                        if (duration < 500) {
                                            delay(500 - duration) // Biroz kutib turamiz
                                        }

                                        recorder.stopRecording()
                                        isRecording = false
                                        startAIInteraction()
                                    } catch (e: Exception) {
                                        isRecording = false
                                        recorder.stopRecording()
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isWaitingResponse) Icons.Default.Sync else Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        SecondaryButton(Icons.Default.Keyboard)
    }

    // Xotirani tozalash
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            recorder.stopRecording()
        }
    }
}

// --- YORDAMCHI AUDIO RECORDER KLASSI ---
class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var isRecording = false // Holatni kuzatish uchun

    fun startRecording(outputFile: File) {
        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Start failed: ${e.message}")
        }
    }

    fun stopRecording() {
        if (isRecording) {
            try {
                recorder?.stop()
                recorder?.reset()
                recorder?.release()
            } catch (e: Exception) {
                // -1007 xatosi aynan shu yerda ushlanadi va dastur "crash" bo'lmaydi
                Log.e("AudioRecorder", "Stop failed: ${e.message}")
            } finally {
                recorder = null
                isRecording = false
            }
        }
    }
}

@Composable
fun AtmosphericAura() {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "drift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PrimaryContainer.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.2f + drift, size.height * 0.2f)
            ),
            radius = 600f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFD4E4FA).copy(alpha = 0.2f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.8f - drift)
            ),
            radius = 500f
        )
    }
}

fun getUserName(context: Context): String {
    val prefs = context.getSharedPreferences("CareAI_Prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_name", "Sanctuary") ?: "Sanctuary"
}
@Composable
fun TopBar(
    userName: String, // Dinamik ism uchun parametr
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // User Avatar qismi (O'zgarishsiz)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Mana bu yerda Sanctuary o'rniga userName chiqadi
            Text(
                text = userName,
                fontSize = 18.sp, // Ism uzun bo'lishi mumkinligini hisobga olib 18sp qildim
                maxLines = 1,     // Ism juda uzun bo'lib ketsa, pastga tushib ketmasligi uchun
                overflow = TextOverflow.Ellipsis, // Uzun bo'lsa "Abdurahmon..." bo'lib ko'rinadi
                fontWeight = FontWeight.Bold,
                color = Primary.copy(alpha = 0.8f)
            )
        }

        IconButton(onClick = onSettings) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = OnSurfaceVariant)
        }
    }
}

@Composable
fun SecondaryButton(icon: ImageVector) {
    Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.4f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(28.dp)) }
    }
}