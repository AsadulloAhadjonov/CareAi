package com.example.careai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.text.input.KeyboardCapitalization
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

val PrimaryPurple = Color(0xFF8720DE)
val PrimaryContainer = Color(0xFFD9B0FF)
val BackgroundColor = Color(0xFFFAF8FF)
val TextGray = Color(0xFF5C5F6A)
val SurfaceVariant = Color(0xFFF3F3FC)
val SecondaryPink = Color(0xFFFFD8E7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareAiTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "oyna_a") {
                    composable("oyna_a") {
                        MindCareRegistrationScreen(onNavigateToB = { navController.navigate("oyna_b") }, homeScreen = {navController.navigate("home_screen")})
                    }
                    composable("oyna_b") {
                        SignIn(onBack = { navController.popBackStack() }, homeScreen = {navController.navigate("home_screen")})
                    }
                    composable("home_screen") {
                        HomeScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(){
    Text(text = "Home Screen")
}

@Composable
fun SignIn(onBack: () -> Unit, homeScreen: () -> Unit) {
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()
    val overscrollOffset = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollOffset.value > 0 && available.y < 0) {
                    val consumed = available.y * 0.5f // Qanchalik qiyin tortilishi (resist)
                    coroutineScope.launch {
                        overscrollOffset.snapTo((overscrollOffset.value + consumed).coerceAtLeast(0f))
                    }
                    return Offset(0f, available.y)
                }
                if (overscrollOffset.value < 0 && available.y > 0) {
                    val consumed = available.y * 0.5f
                    coroutineScope.launch {
                        overscrollOffset.snapTo((overscrollOffset.value + consumed).coerceAtMost(0f))
                    }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f) {
                    coroutineScope.launch {
                        // available.y * 0.2f - tortish kuchi (resistance)
                        overscrollOffset.snapTo(overscrollOffset.value + available.y * 0.2f)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                coroutineScope.launch {
                    overscrollOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy, // Qanchalik "sakrashi"
                            stiffness = Spring.StiffnessLow // Qanchalik tez qaytishi
                        )
                    )
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(modifier = Modifier
            .size(400.dp)
            .offset(x = (-100).dp, y = (-100).dp)
            .blur(100.dp)
            .background(PrimaryContainer.copy(alpha = 0.3f), CircleShape)
        )
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(400.dp)
            .offset(x = 100.dp, y = 100.dp)
            .blur(100.dp)
            .background(SecondaryPink.copy(alpha = 0.3f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                // Overscroll effektini shu Column'ga qo'llaymiz (Tepaga/Pastga surilish)
                .graphicsLayer {
                    translationY = overscrollOffset.value
                }
                // Vertikal skrollni yoqish
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Care AI",
                    color = PrimaryPurple,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = buildAnnotatedString {
                    append("O'zingni\n")
                    withStyle(style = SpanStyle(color = PrimaryPurple, fontStyle = FontStyle.Italic)) {
                        append("Tinchilantir.")
                    }
                },
                fontSize = 48.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Step into a digital sanctuary designed for your emotional well-being.",
                color = TextGray,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    InputField(
                        label = "Phone Number",
                        placeholder = "+998 90 123 45 67",
                        icon = Icons.Default.Phone,
                        value = phone,
                        onValueChange = { phone = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    InputField(
                        label = "Create Password",
                        placeholder = "Min. 8 characters",
                        icon = Icons.Default.Lock,
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Your emotional data is encrypted and private. We never sell your personal reflections.",
                            fontSize = 11.sp,
                            color = TextGray,
                            lineHeight = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            homeScreen()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(PrimaryPurple, Color(0xFFB66DFF)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Kirish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Akkaunt yo'qmi ",
                    fontSize = 14.sp,
                    color = TextGray
                )
                TextButton(
                    onClick = {
                        onBack()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Ochish",
                        color = PrimaryPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
                Text("  OR CONTINUE WITH  ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                SocialIcon(Icons.Default.AlternateEmail)
                SocialIcon(Icons.Default.PhoneAndroid)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "BY CREATING AN ACCOUNT, YOU AGREE TO OUR\nTERMS OF SERVICE & PRIVACY POLICY",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 14.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindCareRegistrationScreen(onNavigateToB: () -> Unit, homeScreen: () -> Unit) {

    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()
    val overscrollOffset = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollOffset.value > 0 && available.y < 0) {
                    val consumed = available.y * 0.5f // Qanchalik qiyin tortilishi (resist)
                    coroutineScope.launch {
                        overscrollOffset.snapTo((overscrollOffset.value + consumed).coerceAtLeast(0f))
                    }
                    return Offset(0f, available.y)
                }
                if (overscrollOffset.value < 0 && available.y > 0) {
                    val consumed = available.y * 0.5f
                    coroutineScope.launch {
                        overscrollOffset.snapTo((overscrollOffset.value + consumed).coerceAtMost(0f))
                    }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y != 0f) {
                    coroutineScope.launch {
                        overscrollOffset.snapTo(overscrollOffset.value + available.y * 0.2f)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                coroutineScope.launch {
                    overscrollOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy, // Qanchalik "sakrashi"
                            stiffness = Spring.StiffnessLow // Qanchalik tez qaytishi
                        )
                    )
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(modifier = Modifier
            .size(400.dp)
            .offset(x = (-100).dp, y = (-100).dp)
            .blur(100.dp)
            .background(PrimaryContainer.copy(alpha = 0.3f), CircleShape)
        )
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(400.dp)
            .offset(x = 100.dp, y = 100.dp)
            .blur(100.dp)
            .background(SecondaryPink.copy(alpha = 0.3f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .graphicsLayer {
                    translationY = overscrollOffset.value
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Care AI",
                    color = PrimaryPurple,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = buildAnnotatedString {
                    append("Tinchilan\n")
                    withStyle(style = SpanStyle(color = PrimaryPurple, fontStyle = FontStyle.Italic)) {
                        append("Core Ai.")
                    }
                },
                fontSize = 48.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Step into a digital sanctuary designed for your emotional well-being.",
                color = TextGray,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    InputField(
                        label = "How should we call you?",
                        placeholder = "Your name",
                        icon = Icons.Default.Person,
                        value = name,
                        onValueChange = { name = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        )
                    )

                    InputField(
                        label = "Phone Number",
                        placeholder = "+998 90 123 45 67",
                        icon = Icons.Default.Phone,
                        value = phone,
                        onValueChange = { phone = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    InputField(
                        label = "Create Password",
                        placeholder = "Min. 8 characters",
                        icon = Icons.Default.Lock,
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Your emotional data is encrypted and private. We never sell your personal reflections.",
                            fontSize = 11.sp,
                            color = TextGray,
                            lineHeight = 16.sp
                        )
                    }

                    // CTA Button
                    Button(
                        onClick = {
                            homeScreen()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(PrimaryPurple, Color(0xFFB66DFF)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Yaratish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have a space? ",
                    fontSize = 14.sp,
                    color = TextGray
                )
                TextButton(
                    onClick = {
                        onNavigateToB()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign In",
                        color = PrimaryPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
                Text("  OR CONTINUE WITH  ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Divider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                SocialIcon(Icons.Default.AlternateEmail)
                SocialIcon(Icons.Default.PhoneAndroid)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "BY CREATING AN ACCOUNT, YOU AGREE TO OUR\nTERMS OF SERVICE & PRIVACY POLICY",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 14.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isPassword: Boolean = false
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray,
            modifier = Modifier.padding(start = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.LightGray) },

            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"

                        Icon(imageVector = image, contentDescription = description, tint = Color.LightGray)
                    }
                }
            } else null,

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceVariant,
                unfocusedContainerColor = SurfaceVariant,
                disabledContainerColor = SurfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),

            keyboardOptions = if (isPassword) {
                KeyboardOptions(keyboardType = KeyboardType.Password)
            } else {
                keyboardOptions
            },

            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
                   },
            singleLine = true
        )
    }
}

@Composable
fun SocialIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.size(60.dp),
        shape = CircleShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Black)
        }
    }
}
