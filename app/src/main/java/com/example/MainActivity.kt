package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTheme
import com.example.ui.screens.ConnectionTab
import com.example.ui.screens.ExplorerTab
import com.example.ui.screens.MediaTab
import com.example.ui.screens.SystemTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ActiveTab
import com.example.ui.viewmodel.RemoteControlViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppScreen() {
    val viewModel: RemoteControlViewModel = viewModel()
    val currentTab by viewModel.currentTab.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    // Floating alert confirmation HUD
    LaunchedEffect(actionFeedback) {
        if (actionFeedback != null) {
            delay(2800)
            viewModel.clearFeedback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassTheme.DarkObsidian)
    ) {
        // Elegant fluid spot-glow animation Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp) // creates magnificent blurred visual blending
        ) {
            // Neon cyan focal spotlight (top right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassTheme.GlowCyan.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    radius = 350.dp.toPx()
                ),
                center = Offset(size.width * 0.85f, size.height * 0.18f)
            )

            // Neon violet focal spotlight (bottom left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassTheme.GlowViolet.copy(alpha = 0.13f),
                        Color.Transparent
                    ),
                    radius = 450.dp.toPx()
                ),
                center = Offset(size.width * 0.15f, size.height * 0.82f)
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Allow dynamic glowing Canvas underneath
            bottomBar = {
                // Glassmorphic navigation menu
                NavigationBar(
                    containerColor = Color.White.copy(alpha = 0.04f),
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.02f))
                        .testTag("bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        Triple(ActiveTab.CONNECTION, Icons.Default.CloudQueue, "Enlace"),
                        Triple(ActiveTab.EXPLORER, Icons.Default.FolderOpen, "Explorar"),
                        Triple(ActiveTab.MEDIA, Icons.Default.PlayCircle, "Medios"),
                        Triple(ActiveTab.SYSTEM, Icons.Default.DeveloperBoard, "Sistema")
                    )

                    tabs.forEach { (tab, icon, label) ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) GlassTheme.GlowCyan else GlassTheme.MutedGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else GlassTheme.MutedGray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GlassTheme.GlowCyan.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(innerPadding)
            ) {
                // Render corresponding tab body
                Crossfade(
                    targetState = currentTab, 
                    animationSpec = tween(durationMillis = 300),
                    modifier = Modifier.fillMaxSize()
                ) { targetState ->
                    when (targetState) {
                        ActiveTab.CONNECTION -> ConnectionTab(viewModel = viewModel)
                        ActiveTab.EXPLORER -> ExplorerTab(viewModel = viewModel)
                        ActiveTab.MEDIA -> MediaTab(viewModel = viewModel)
                        ActiveTab.SYSTEM -> SystemTab(viewModel = viewModel)
                    }
                }

                // HUD Animated Feedbacks over-view
                AnimatedVisibility(
                    visible = actionFeedback != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    actionFeedback?.let { msg ->
                        GlassCard(
                            shape = RoundedCornerShape(16.dp),
                            backgroundBrush = Brush.verticalGradient(
                                listOf(GlassTheme.GlowCyan.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                            ),
                            borderBrush = Brush.verticalGradient(
                                listOf(GlassTheme.GlowCyan.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GlassTheme.GlowCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
