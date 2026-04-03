package com.example.careai

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.careai.ui.theme.CareAiTheme
import kotlinx.coroutines.launch

// --- RANG PALITRASI ---
val PrimaryPurple = Color(0xFF8720DE)
val PrimaryContainer = Color(0xFFD9B0FF)
val BackgroundColor = Color(0xFFFAF8FF)
val TextGray = Color(0xFF5C5F6A)
val SurfaceVariant = Color(0xFFF3F3FC)
val SecondaryPink = Color(0xFFFFD8E7)
val Primary = Color(0xFF595994)
val OnSurface = Color(0xFF2F3334)
val OnSurfaceVariant = Color(0xFF5B6061)


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Ruxsat berildi - endi bildirishnomalar keladi
        } else {
            // Ruxsat berilmadi - foydalanuvchiga bildirishnomalar ishlamasligi haqida xabar berish mumkin
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        enableEdgeToEdge()
        setContent {
            CareAiTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "oyna_a") {
                    composable("oyna_a") {
                        MindCareRegistrationScreen(
                            onNavigateToSignIn = { navController.navigate("oyna_b") },
                            onFinishAuth = { navController.navigate("main_pager") }
                        )
                    }
                    composable("oyna_b") {
                        SignInScreen(
                            onBack = { navController.popBackStack() },
                            onFinishAuth = { navController.navigate("main_pager") }
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
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    var isAllowedToScroll by remember { mutableStateOf(false) }
    var showConsent by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
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
                3 -> DecompressionScreen("Xayrli Kun", "Siz tayyorsiz.")
            }
        }

        AnimatedVisibility(
            visible = showConsent && pagerState.currentPage == 0,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(16.dp)
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
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

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
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
                onClick = { /* Logout */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
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

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor).nestedScroll(nestedScrollConnection)) {
        Box(modifier = Modifier.size(400.dp).offset((-100).dp, (-100).dp).blur(100.dp).background(PrimaryContainer.copy(0.3f), CircleShape))

        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding()
                .graphicsLayer { translationY = overscrollOffset.value }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Care AI", color = PrimaryPurple, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 16.dp).fillMaxWidth())
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
    AuthWrapper(title = "Tinchilan\nCore Ai.", subtitle = "Step into a digital sanctuary.") {
        InputField("Full Name", "John Doe", Icons.Default.Person)
        InputField("Phone Number", "+998 90...", Icons.Default.Phone)
        InputField("Create Password", "********", Icons.Default.Lock, isPassword = true)
        PrimaryAuthButton("Ro'yxatdan o'tish", onClick = onFinishAuth)
        AuthFooter("Akkaunt bormi?", "Kirish", onClick = onNavigateToSignIn)
    }
}

@Composable
fun SignInScreen(onBack: () -> Unit, onFinishAuth: () -> Unit) {
    AuthWrapper(title = "O'zingni\nTinchilantir.", subtitle = "Hush kelibsiz, Sanctuary sizni kutmoqda.") {
        InputField("Phone Number", "+998 90...", Icons.Default.Phone)
        InputField("Password", "********", Icons.Default.Lock, isPassword = true)
        PrimaryAuthButton("Kirish", onClick = onFinishAuth)
        AuthFooter("Akkaunt yo'qmi?", "Ochish", onClick = onBack)
    }
}

@Composable
fun InputField(label: String, placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isPassword: Boolean = false) {
    var text by remember { mutableStateOf("") }
    Column {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        TextField(
            value = text, onValueChange = { text = it },
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(icon, null, tint = PrimaryPurple) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceVariant,
                unfocusedContainerColor = SurfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun PrimaryAuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(30.dp), contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(PrimaryPurple, Color(0xFFB66DFF)))), contentAlignment = Alignment.Center) {
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

// --- HOME SCREEN COMPONENTS ---
@Composable
fun SanctuaryApp(onMicClick: () -> Unit, onSettings: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericAura()
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onSettings)
            Spacer(modifier = Modifier.weight(0.5f))
            AnimatedVoiceAvatar()
            Spacer(modifier = Modifier.height(48.dp))
            GreetingSection()
            Spacer(modifier = Modifier.height(40.dp))
            SuggestionChips()
            Spacer(modifier = Modifier.weight(1f))
            ActionButtons(onMicClick = onMicClick)
            Text("YOUR PRIVATE SANCTUARY IS ALWAYS OPEN", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceVariant.copy(0.4f)), modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
fun DecompressionScreen(title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericAura()
        Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, fontSize = 36.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(subtitle, fontSize = 16.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ColorSelectionScreen(onNextPage: () -> Unit) {
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        // Fon uchun o'sha mashhur AtmosphericAura
        AtmosphericAura()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Sarlavha dizayni (Sanctuary uslubida)
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

            // 3x4 Grid - Ranglar palitrasi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.3f)) // Shaffof fon
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

            Spacer(modifier = Modifier.weight(1f))

            // API Response Text
            AnimatedVisibility(visible = apiResponse != null) {
                apiResponse?.let {
                    Text(
                        text = it,
                        fontSize = 15.sp,
                        color = Primary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .background(Color.White.copy(0.5f), CircleShape)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
            Box(modifier = Modifier.padding(bottom = 48.dp), contentAlignment = Alignment.Center) {
                if (apiResponse == null) {
                    // --- TAHLIL QILISH TUGMASI (GRADIENT BILAN) ---
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedIds.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                // API simulyatsiyasi (bu yerga haqiqiy API chaqiruvi keladi)
                                apiResponse = "Sizning tanlovingiz ichki xotirjamlikka bo'lgan ehtiyojni ko'rsatmoqda."
                                isLoading = false
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .width(220.dp) // Biroz kengaytirdik matn sig'ishi uchun
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
                    // --- KEYINGISI TUGMASI (OQ / SECONDARY USLUBDA) ---
                    Button(
                        onClick = onNextPage,
                        modifier = Modifier
                            .height(56.dp)
                            .width(200.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(0.2f)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
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

@Composable
fun ArtTherapyScreen(onNextPage: () -> Unit) {
    var selectedOutlineId by remember { mutableIntStateOf(0) }
    var showColoringCanvas by remember { mutableStateOf(false) }
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Chizilgan yo'llarni saqlash
    val paths = remember { mutableStateListOf<ColoredPath>() }
    // Hozirgi chizilayotgan nuqta (recomposition uchun state)
    var motionEvent by remember { mutableStateOf<MotionEvent?>(null) }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }
    var activeColor by remember { mutableStateOf(PrimaryPurple) }

    val outlines = listOf(
        R.drawable._947292__1_,
        R.drawable._947292__1_,
        R.drawable._947292__1_
    )

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        AtmosphericAura()

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (!showColoringCanvas) "Shaklni tanlang" else "Barmog'ingiz bilan bo'yang",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (!showColoringCanvas) {
                // --- 1. TANLASH BOSQICHI ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    outlines.forEach { resId ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(0.5f))
                                .border(1.dp, Color.White.copy(0.8f), RoundedCornerShape(24.dp))
                                .clickable {
                                    selectedOutlineId = resId
                                    showColoringCanvas = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = null,
                                modifier = Modifier.size(70.dp).padding(8.dp)
                            )
                        }
                    }
                }
            } else {
                // --- 2. BO'YASH BOSQICHI ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .border(2.dp, Color.White.copy(0.5f), RoundedCornerShape(32.dp))
                        .pointerInput(activeColor) { // Rang o'zgarganda inputni yangilash
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    // Canvasni majburan qayta chizish uchun motionEventni yangilaymiz
                                    motionEvent = MotionEvent.create(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    currentPath?.let {
                                        paths.add(ColoredPath(it, activeColor))
                                    }
                                    currentPath = null
                                    motionEvent = null
                                }
                            )
                        }
                ) {
                    val painter = painterResource(selectedOutlineId)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // 1. Bo'yoqlar (Pastki qatlam)
                        paths.forEach { coloredPath ->
                            drawPath(
                                path = coloredPath.path,
                                color = coloredPath.color,
                                style = Stroke(width = 45f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        // 2. Hozirgi chizilayotgan chiziq
                        currentPath?.let {
                            motionEvent?.let { /* faqat trigger uchun */ }
                            drawPath(
                                path = it,
                                color = activeColor,
                                style = Stroke(width = 45f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        // 3. SVG Konturi (Eng ustki qatlam - bo'yoqni berkitadi)
                        with(painter) {
                            draw(size, colorFilter = ColorFilter.tint(OnSurface.copy(0.8f)))
                        }
                    }
                }

                // Ranglar palitrasi
                Row(
                    modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val colors = listOf(Color(0xFFFFB7B7), Color(0xFFB7D7FF), Color(0xFFB7FFB7), Color(0xFFFFF4B7), PrimaryPurple, Color.DarkGray)
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (activeColor == color) 3.dp else 1.dp,
                                    color = if (activeColor == color) OnSurface else Color.White,
                                    shape = CircleShape
                                )
                                .clickable { activeColor = color }
                        )
                    }
                }
            }

            // --- BOTTOM ACTIONS ---
            if (showColoringCanvas) {
                Box(modifier = Modifier.padding(bottom = 32.dp)) {
                    if (apiResponse == null) {
                        PrimaryAuthButton(text = if (isLoading) "Tahlil..." else "Tugatish va Tahlil") {
                            isLoading = true
                            apiResponse = "Sizning rang tanlovingiz va chiziqlaringiz ichki osoyishtalikka intilishingizni ko'rsatmoqda."
                            isLoading = false
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = apiResponse!!,
                                modifier = Modifier
                                    .background(Color.White.copy(0.6f), RoundedCornerShape(20.dp))
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onNextPage,
                                modifier = Modifier.height(56.dp).width(200.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, PrimaryPurple.copy(0.3f))
                            ) {
                                Text("Keyingisi", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

class MotionTrigger(val x: Float, val y: Float)

// Recomposition uchun yordamchi class
class MotionEvent(val x: Float, val y: Float) {
    companion object {
        fun create(x: Float, y: Float) = MotionEvent(x, y)
    }
}

@Composable
fun AnimatedVoiceAvatar() {
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
        modifier = Modifier.size(350.dp) // Umumiy konteyner
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
fun ActionButtons(onMicClick: () -> Unit) {
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
        val infiniteTransition = rememberInfiniteTransition(label = "mic_glow_anim")
        val sc by infiniteTransition.animateFloat(
            initialValue = 0.8f, // Kichikroq holatdan
            targetValue = 1.3f,  // Kengroq holatgacha
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp) // Tashqi konteyner
        ) {
            // 1. GLOW (NUR) - Canvas orqali
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Skalani (sc) to'g'ridan-to'g'ri radiusga ko'paytiramiz
                val animatedRadius = (size.minDimension / 2.2f) * sc

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(red = 0.4f, alpha = 0.8f), // Markazda yorqin
                            Primary.copy(red = 0.4f, alpha = 0.8f), // O'rtacha
                            Color.Transparent          // Chegarada mutlaqo shaffof
                        ),
                        center = center,
                        radius = animatedRadius
                    ),
                    radius = animatedRadius
                )
            }

            // 2. MIKROFON TUGMASI
            Box(
                modifier = Modifier
                    .size(80.dp) // Tugma o'lchamini biroz mosladik
                    .clickable { onMicClick() }
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Primary, PrimaryContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        SecondaryButton(Icons.Default.Keyboard)
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
fun GreetingSection() {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
            append("How are you feeling today?")
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
        Text(
            text = annotatedString,
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "I'm here to listen and hold space for you.",
            fontSize = 16.sp,
            color = OnSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun SuggestionChips() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Feeling overwhelmed", "Need peace").forEach { text ->
            Surface(
                color = Color.White.copy(alpha = 0.4f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                onClick = {}
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
