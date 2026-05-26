package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTheme
import com.example.ui.viewmodel.ConnectionStatus
import com.example.ui.viewmodel.RemoteControlViewModel

@Composable
fun MediaTab(
    viewModel: RemoteControlViewModel,
    modifier: Modifier = Modifier
) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    // Connection checker
    if (connectionStatus != ConnectionStatus.CONNECTED) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = GlassTheme.GlowCrimson,
                    modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "PC Desconectado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Para poder utilizar el Control de Medios, debes establecer enlace con tu ordenador primero desde la pestaña de Conexión.",
                    fontSize = 13.sp,
                    color = GlassTheme.MutedGray,
                    lineHeight = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        return
    }

    val isVlcRunning = telemetry?.vlcRunning ?: false

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Large status viewport card
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (isVlcRunning) GlassTheme.GlowCyan.copy(alpha = 0.15f)
                            else GlassTheme.MutedGray.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVlcRunning) Icons.AutoMirrored.Filled.FeaturedVideo else Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = if (isVlcRunning) GlassTheme.GlowCyan else GlassTheme.MutedGray,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isVlcRunning) "VLC Media Player" else "Sin Reproducción Activa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isVlcRunning) "Vídeo Detectado en el PC 🟢" else "Abre un archivo de vídeo en la pestaña 'Explorador' para comenzar la reproducción remota",
                    fontSize = 12.sp,
                    color = if (isVlcRunning) GlassTheme.GlowCyan else GlassTheme.MutedGray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // Circular playback center deck
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Seek backward -10s
                IconButton(
                    onClick = { viewModel.sendMediaControl("seek_backward") },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Retroceder 10s",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Centered large glowing Play/Pause
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GlassTheme.GlowCyan.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            )
                        )
                        .clickable { viewModel.sendMediaControl("play_pause") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow, // represents dynamic toggle ⏯️
                        contentDescription = "Reproducir / Pausar",
                        tint = GlassTheme.GlowCyan,
                        modifier = Modifier.size(52.dp)
                    )
                    // Nested secondary Pause icon as layout finish hint
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        modifier = zoomHeaderRatioPadding(),
                        tint = GlassTheme.GlowCyan.copy(alpha = 0.35f)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Seek forward +10s
                IconButton(
                    onClick = { viewModel.sendMediaControl("seek_forward") },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Avanzar 10s",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Action controls toolbar
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "AJUSTES DE REPRODUCCIÓN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTheme.MutedGray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Adjust volume down
                GlassButton(
                    onClick = { viewModel.sendMediaControl("volume_down") },
                    accentColor = Color.White,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vol -", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Adjust volume up
                GlassButton(
                    onClick = { viewModel.sendMediaControl("volume_up") },
                    accentColor = GlassTheme.GlowCyan,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vol +", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Fullscreen toggler
                GlassButton(
                    onClick = { viewModel.sendMediaControl("toggle_fullscreen") },
                    accentColor = GlassTheme.GlowViolet,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pantalla", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

// Visual layout helper for double overlapping overlay indicators
@Composable
private fun zoomHeaderRatioPadding() = Modifier
    .size(68.dp)
    .padding(start = 28.dp)
