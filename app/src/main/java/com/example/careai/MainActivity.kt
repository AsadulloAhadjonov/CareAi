package com.example.careai

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.pipe.core.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.OutputStream
import java.util.Calendar

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
        askNotificationPermission()
        createNotificationChannel(this)
        enableEdgeToEdge()
        // 1. Tokenni tekshirish
        checkAndRequestPermissions()
        val prefs = getSharedPreferences("CareAI_Prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)
        val startScreen = if (token != null) "main_pager" else "oyna_a"

        setContent {
            CareAiTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = startScreen) {

                    // REGISTER EKRANI (Boshlang'ich nuqta)
                    composable("oyna_a") {
                        MindCareRegistrationScreen(
                            onNavigateToSignIn = { navController.navigate("oyna_b") },
                            onFinishAuth = {
                                // Ro'yxatdan o'tgach Login oynasiga o'tkazish
                                navController.navigate("oyna_b") {
                                    popUpTo("oyna_a") { inclusive = true }
                                }
                            }
                        )
                    }

                    // LOGIN EKRANI
                    composable("oyna_b") {
                        SignInScreen(
                            onBack = {
                                // Agar orqaga qaytsa Registerga qaytishi kerak
                                navController.navigate("oyna_a")
                            },
                            onFinishAuth = {
                                navController.navigate("main_pager") {
                                    popUpTo("oyna_b") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main_pager") {
                        MainVerticalPager()
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
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
@Preview(showBackground = true)
fun MainVerticalPager() {
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
                })

                1 -> ColorSelectionScreen(onNextPage = {
                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                })

                2 -> ArtTherapyScreen(onNextPage = {
                    coroutineScope.launch { pagerState.animateScrollToPage(3) }
                })

                3 -> ListenMusicScreen()

                4 -> PlanScreen()
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
            SettingsScreen(onClose = { showSettings = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var taskList by remember { mutableStateOf(mutableListOf(TaskItem())) }
    var showTimePickerForIndex by remember { mutableStateOf<Int?>(null) }
    val timePickerState = rememberTimePickerState()
    val sharedPreferences = remember {
        context.getSharedPreferences("CareAI_Storage", Context.MODE_PRIVATE)
    }
    val gson = Gson()
    val savedJson = sharedPreferences.getString("task_list", null)
    val initialList = if (savedJson != null) {
        val type = object : TypeToken<MutableList<TaskItem>>() {}.type
        gson.fromJson<MutableList<TaskItem>>(savedJson, type)
    } else {
        mutableListOf(TaskItem())
    }

    LaunchedEffect(Unit) {
        val savedJson = sharedPreferences.getString("saved_tasks", null)
        if (savedJson != null) {
            val type = object : TypeToken<MutableList<TaskItem>>() {}.type
            val restoredList: MutableList<TaskItem> = gson.fromJson(savedJson, type)
            taskList = restoredList
        } else {

            taskList = mutableListOf(TaskItem())
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {
        // Fon bezagi
        AtmosphericAura()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Sarlavha qismi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keyingi rejalar",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                // Qo'shish tugmasi
                IconButton(
                    onClick = {
                        val newList = taskList.toMutableList()
                        newList.add(TaskItem())
                        taskList = newList
                    },
                    modifier = Modifier.background(PrimaryPurple, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }

            // Rejalar ro'yxati
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                itemsIndexed(taskList) { index, task ->
                    TaskEntryRow(
                        task = task,
                        onTextChange = { newText ->
                            val newList = taskList.toMutableList()
                            newList[index] = newList[index].copy(taskText = newText)
                            taskList = newList
                        },
                        onTimeClick = { showTimePickerForIndex = index }
                    )
                }
            }
        }

        // Saqlash tugmasi (Pastda qat'iy turadi)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            PrimaryAuthButton(text = "Rejalarni saqlash") {
                val json = gson.toJson(taskList)
                sharedPreferences.edit().putString("task_list", json).apply()

                taskList.forEach { task ->
                    if (task.taskText.isNotEmpty()) {
                        scheduleTaskAlarm(context, task)
                    }
                }
                Toast.makeText(context, "Rejalar saqlandi va eslatmalar o'rnatildi!", Toast.LENGTH_SHORT).show()
            }
        }

        // Time Picker Dialog
        if (showTimePickerForIndex != null) {
            AlertDialog(
                onDismissRequest = { showTimePickerForIndex = null },
                confirmButton = {
                    TextButton(onClick = {
                        val idx = showTimePickerForIndex!!
                        val newList = taskList.toMutableList()
                        newList[idx] = newList[idx].copy(
                            time = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                        taskList = newList
                        showTimePickerForIndex = null
                    }) {
                        Text("Tanlash", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White,
                text = { TimePicker(state = timePickerState) }
            )
        }
    }
}

@Composable
fun TaskEntryRow(
    task: TaskItem,
    onTextChange: (String) -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = task.taskText,
            onValueChange = onTextChange,
            placeholder = { Text("Vazifa nomi...", color = Color.LightGray) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Vaqt tanlash qismi
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryPurple.copy(alpha = 0.1f))
                .clickable { onTimeClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = task.time, color = PrimaryPurple, fontWeight = FontWeight.Bold)
        }
    }
}

@SuppressLint("ScheduleExactAlarm")
fun scheduleTaskAlarm(context: Context, task: TaskItem) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // 1. Ruxsatnomani tekshirish (Android 12+ uchun)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return // Ruxsatnoma berilmaguncha davom etib bo'lmaydi
        }
    }

    // 2. BroadcastReceiver uchun Intent tayyorlash
    // DIQQAT: AlarmReceiver — bu siz yaratgan BroadcastReceiver klassi bo'lishi kerak
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("TASK_TEXT", task.taskText)
        putExtra("TASK_ID", task.id)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.toInt(), // Har bir vazifa uchun unikal ID juda muhim
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 3. Vaqtni hisoblash
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, task.hour)
        set(Calendar.MINUTE, task.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // Vaqt o'tib ketgan bo'lsa, ertaga o'tkazish
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    // 4. Alarmni o'rnatish
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
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
fun SettingsScreen(onClose: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showNotifDialog by remember { mutableStateOf(false) }
    var isNotifEnabled by rememberSaveable { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Teppa Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Yopish", tint = OnSurfaceVariant)
                }
                Text(
                    text = "Sanctuary Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())) {
                Text(
                    text = "Account & Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = OnSurface,
                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(0.4f))
                        .border(1.dp, Color.White.copy(0.6f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsItem(icon = Icons.Default.Person, title = "Personal Info", trailing = "Edit")

                        // Notifications qatori
                        SettingsItem(
                            icon = Icons.Default.NotificationsActive,
                            title = "Notifications",
                            trailing = if (isNotifEnabled) "On" else "Off",
                            onClick = { showNotifDialog = true }
                        )

                        SettingsItem(icon = Icons.Default.LockReset, title = "Change Password")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "App Preferences",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = OnSurface,
                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(0.4f))
                        .border(1.dp, Color.White.copy(0.6f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsItem(icon = Icons.Default.Language, title = "Language", trailing = "O'zbekcha")
                        SettingsItem(icon = Icons.Default.Info, title = "About CareAI")
                    }
                }
            }

            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.Red.copy(0.1f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tizimdan chiqish", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // --- NOTIFICATION DIALOG ---
        if (showNotifDialog) {
            AlertDialog(
                onDismissRequest = { showNotifDialog = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                title = {
                    Text("Notifications", fontWeight = FontWeight.Bold, color = Primary)
                },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bildirishnomalar", color = OnSurface, fontSize = 16.sp)
                        Switch(
                            checked = isNotifEnabled,
                            onCheckedChange = { checked ->
                                isNotifEnabled = checked
                                if (checked) {
                                    sendLocalNotification(context, "Notification", "Bildirishnomalar yoqildi ✅")
                                } else {
                                    sendLocalNotification(context, "Notification", "Bildirishnomalar o'chirildi ❌")
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = SurfaceVariant
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotifDialog = false }) {
                        Text("Tayyor", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
fun SanctuaryApp(onMicClick: () -> Unit, onSettings: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericAura()
        Column(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onSettings)
            Spacer(modifier = Modifier.weight(0.5f))
            AnimatedVoiceAvatar(onMicClick)
            Spacer(modifier = Modifier.height(48.dp))
            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.weight(1f))
            ActionButtons()
            Text("YOUR PRIVATE SANCTUARY IS ALWAYS OPEN", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant.copy(0.4f)), modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
fun ColorSelectionScreen(onNextPage: () -> Unit) {
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Scroll holatini saqlash uchun
    val scrollState = rememberScrollState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundColor)) {

        AtmosphericAura()

        // 1. ASOSIY MAZMUN (Scroll bo'ladigan qism)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState) // Scroll qo'shildi
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

            // API Response Text qismi
            AnimatedVisibility(
                visible = apiResponse != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                apiResponse?.let {
                    Text(
                        text = it,
                        fontSize = 15.sp,
                        color = Primary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 32.dp, bottom = 24.dp)
                            .background(Color.White.copy(0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            // Tugmalar uchun joy tashlaymiz (tugmalar ekranning eng tagida turishi uchun)
            Spacer(modifier = Modifier.height(120.dp))
        }

        // 2. TUGMALAR QISMI (Ekran tagiga fiksatsiyalangan)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding() // Telefon menyusi tugmalari ostida qolib ketmasligi uchun
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Gradient fon (agar tugma orqasidagi matn ko'rinmasligini xohlasangiz)
            // Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Brush.verticalGradient(...)))

            if (apiResponse == null) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = selectedIds.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            apiResponse = "Sizning tanlovingiz ichki xotirjamlikka bo'lgan ehtiyojni ko'rsatmoqda. Bu ranglar sizning hozirgi hissiy holatingizni juda aniq ifodalaydi."
                            isLoading = false
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .width(220.dp)
                            .shadow(8.dp, CircleShape) // Tugma ajralib turishi uchun shadow
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, Color(0xFFB66DFF))
                                ),
                                shape = CircleShape
                            ),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Tahlil qilish", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    var selectedOutlineId by remember { mutableIntStateOf(0) }
    var showColoringCanvas by remember { mutableStateOf(false) }
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val picture = remember { android.graphics.Picture() }
    val paths = remember { mutableStateListOf<ColoredPath>() }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }
    var activeColor by remember { mutableStateOf(Color(0xFF6200EE)) }

    // Canvas o'lchamini qat'iy ushlab turish uchun
    var redrawTrigger by remember { mutableStateOf(0) }

    val outlines = listOf(R.drawable._947292__1_, R.drawable._947292__1_, R.drawable._947292__1_)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF5F5F5))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (!showColoringCanvas) "Shaklni tanlang" else "Barmog'ingiz bilan bo'yang",
                fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.Black
            )
            Spacer(modifier = Modifier.height(30.dp))

            if (!showColoringCanvas) {
                // --- 1. TANLASH BOSQICHI ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    outlines.forEach { resId ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .clickable { selectedOutlineId = resId; showColoringCanvas = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(resId), contentDescription = null, modifier = Modifier.size(70.dp))
                        }
                    }
                }
            } else {
                // --- 2. BO'YASH BOSQICHI ---
                val painter = painterResource(selectedOutlineId)

                // MUHIM: Canvasni Box ichiga olamiz va weight(1f) beramiz.
                // Bu Box pastdagi elementlar o'zgarsa ham o'z nisbatini saqlaydi.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(32.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(activeColor) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPath = androidx.compose.ui.graphics.Path()
                                            .apply { moveTo(offset.x, offset.y) }
                                    },
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

                        // Picture faqat joriy o'lcham noldan katta bo'lsa yozadi
                        if (size.width > 0 && size.height > 0) {
                            val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                                picture.beginRecording(size.width.toInt(), size.height.toInt())
                            )

                            val drawEverything: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit = {
                                drawRect(color = Color.White, size = size)
                                paths.forEach { coloredPath ->
                                    drawPath(
                                        path = coloredPath.path, color = coloredPath.color,
                                        style = Stroke(width = 45f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                                currentPath?.let { path ->
                                    drawPath(
                                        path = path, color = activeColor,
                                        style = Stroke(width = 45f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                                with(painter) {
                                    draw(size = size, colorFilter = ColorFilter.tint(Color.Black.copy(0.8f)))
                                }
                            }

                            // Ekran uchun
                            drawEverything()

                            // Picture xotirasi uchun
                            androidx.compose.ui.graphics.drawscope.CanvasDrawScope().draw(
                                this, layoutDirection, pictureCanvas, size
                            ) {
                                drawEverything()
                            }
                            picture.endRecording()
                        }
                    }
                }

                // Ranglar palitrasi - Balandligi o'zgarmas (Fixed height)
                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val colors = listOf(Color(0xFFFFB7B7), Color(0xFFB7D7FF), Color(0xFFB7FFB7), Color(0xFFFFF4B7), Color(0xFF6200EE), Color.DarkGray)
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (activeColor == color) 3.dp else 1.dp,
                                    color = if (activeColor == color) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { activeColor = color }
                        )
                    }
                }
            }

            // --- 3. BOTTOM ACTIONS ---
            // Bu qismning balandligini qat'iy (fixed) qilamiz, shunda u yuqoridagi Canvas-ni surmaydi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), // Matn chiqsa ham balandlik o'zgarmaydi
                contentAlignment = Alignment.TopCenter
            ) {
                if (showColoringCanvas) {
                    if (apiResponse == null) {
                        Button(
                            onClick = {
                                isLoading = true
                                val bitmap = createBitmapFromPicture(picture)
                                saveBitmapToPublicGallery(context, bitmap)
                                apiResponse = "Saqlandi!"
                                isLoading = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (isLoading) "Tahlil..." else "Tugatish va Tahlil")
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rasm Galereyaga saqlandi ✨", color = Color(0xFF4CAF50))
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onNextPage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("Keyingisi")
                            }
                        }
                    }
                }
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
fun createBitmapFromPicture(picture: android.graphics.Picture): Bitmap {
    val bitmap = Bitmap.createBitmap(picture.width.coerceAtLeast(1), picture.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawPicture(picture)
    return bitmap
}

// Recomposition uchun yordamchi class
class MotionEvent(val x: Float, val y: Float) {
    companion object {
        fun create(x: Float, y: Float) = MotionEvent(x, y)
    }

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
        modifier = Modifier.size(350.dp)
            .clickable{
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
    val recorder = remember { VoiceRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }

//    val sessionIdByClient = remember {
//        47
//    }
    var currentSessionId by remember { mutableStateOf<Int?>(null) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    val playResponse: (String) -> Unit = { url ->
        // Agar server faqat yo'lni yuborsa, IPni qo'shamiz
        val fullUrl = if (url.startsWith("http")) url else "http://213.230.91.55:8111$url"

        val mediaItem = MediaItem.fromUri(fullUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
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

        // Animatsiya: Glow'ning kengayishi va qisqarishi
        val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
        val sc by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = if (isRecording) 1.5f else 1.0f, // Yozayotganda qattiqroq pulsatsiya
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            // 1. Glow (Nurlanish)
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (isRecording) { // Faqat yozayotganda nur chiqadi
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 0.6f), Color.Transparent),
                            center = center,
                            radius = (size.minDimension / 2) * sc
                        )
                    )
                }
            }

            // 2. Asosiy tugma
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Primary, PrimaryContainer)
                        )
                    )
                    // ... (ActionButtons ichidagi clickable qismi)
                    .clickable {
                        if (isRecording) {
                            val file = recorder.stopRecording()
                            isRecording = false
                            file?.let { audioFile ->
                                scope.launch {
                                    // currentSessionId yuboramiz (null bo'lsa server yangi yaratadi)
                                    val responseData =
                                        sendFileToBackend(audioFile, context, currentSessionId)

                                    if (responseData != null) {
                                        // Serverdan kelgan session_id ni saqlaymiz
                                        currentSessionId = responseData.sessionId

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                responseData.reply,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        responseData.audioUrl?.let { playResponse(it) }
                                    }
                                }
                            }
                        } else {
                            recorder.startRecording()
                            isRecording = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        SecondaryButton(Icons.Default.Keyboard)
    }
}

suspend fun sendFileToBackend(
    file: File,
    context: Context,
    sessionId: Int?  // null bo'lishi mumkin
): ChatResponse? {
    val prefs = context.getSharedPreferences("CareAI_Prefs", Context.MODE_PRIVATE)
    val token = prefs.getString("access_token", "") ?: ""
    Toast.makeText(context, "$sessionId", Toast.LENGTH_SHORT).show()
    return withContext(Dispatchers.IO) {
        try {
            val requestFile = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            builder.addFormDataPart("file", file.name, requestFile)

            // session_id faqat mavjud bo'lsa yuboramiz
            if (sessionId != null) {
                builder.addFormDataPart("session_id", sessionId.toString())
            }

            val response = RetrofitClient.instance.sendAudio("Bearer $token", body,
                sessionId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()))

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("CareAI_Error", "Xato: ${e.message}")
            null
        }
    }
}

fun playRecordedAudio(file: File, context: Context) {
    try {
        val mediaPlayer = android.media.MediaPlayer()
        mediaPlayer.setDataSource(file.absolutePath)
        mediaPlayer.prepare()
        mediaPlayer.start()

        // Tugagandan keyin resursni bo'shatish
        mediaPlayer.setOnCompletionListener {
            it.release()
            Toast.makeText(context, "Boldi", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        android.util.Log.e("CareAI", "Ovozni qo'yishda xato: ${e.message}")
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

@Composable
fun TopBar(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White, CircleShape)
            ) {
                // contentAlignment qo'shildi, shunda ichidagi Icon markazda bo'ladi
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center // SHU JOYI MUHIM
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
            Text(
                text = "Sanctuary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Primary.copy(alpha = 0.8f)
            )
        }
        IconButton(onClick = {
            onSettings()
        }) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = OnSurfaceVariant)
        }
    }
}



@Composable
fun SuggestionChips(onMicClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Feeling overwhelmed", "Need peace").forEach { text ->
            Surface(
                color = Color.White.copy(alpha = 0.4f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                onClick = {
                    onMicClick()
                }
            ) {
                Text(text, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SecondaryButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.4f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(28.dp)) }
    }
}
