package com.example.careai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = isAllowedToScroll || pagerState.currentPage > 0
        ) { page ->
            when (page) {
                0 -> SanctuaryApp(onMicClick = { showConsent = true })
                1 -> DecompressionScreen("Chuqur Nafas", "O'pkalaringizni tozalang va xotirjamlikni his qiling.")
                2 -> DecompressionScreen("Tinchlik Ovozi", "Atrofdagi shovqinlarni unuting.")
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
fun SanctuaryApp(onMicClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericAura()
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar()
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
fun AnimatedVoiceAvatar() {
    val infiniteTransition = rememberInfiniteTransition("")
    val ts by infiniteTransition.animateFloat(40f, 65f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "")
    val bs by infiniteTransition.animateFloat(55f, 45f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "")
    val te by infiniteTransition.animateFloat(60f, 35f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), "")
    val be by infiniteTransition.animateFloat(45f, 55f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), "")
    val shape = RoundedCornerShape(ts.toInt(), te.toInt(), be.toInt(), bs.toInt())
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(350.dp)) {
        Box(modifier = Modifier.size(350.dp).blur(45.dp).background(PrimaryContainer.copy(0.2f), CircleShape))
        Box(modifier = Modifier.size(220.dp).clip(shape).background(Brush.linearGradient(listOf(PrimaryContainer, Color(0xFFD4E4FA)))).border(1.5.dp, Color.White.copy(0.5f), shape))
    }
}

@Composable
fun ActionButtons(onMicClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        SecondaryButton(Icons.Default.PhoneAndroid)
        Spacer(modifier = Modifier.width(32.dp))
        val sc by rememberInfiniteTransition("").animateFloat(1f, 1.25f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), "")
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Box(modifier = Modifier.size(90.dp).graphicsLayer { clip = false; scaleX = sc; scaleY = sc }.blur(5.dp).background(Primary.copy(0.2f), CircleShape))
            Box(modifier = Modifier.size(88.dp).clickable{
                onMicClick()
            }.clip(CircleShape).background(Brush.linearGradient(listOf(Primary, PrimaryContainer))), Alignment.Center) {
                Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(32.dp))
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
fun TopBar() {
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
        IconButton(onClick = {}) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = OnSurfaceVariant)
        }
    }
}

@Composable
fun GreetingSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
        Text(
            text = "How are you feeling today?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
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