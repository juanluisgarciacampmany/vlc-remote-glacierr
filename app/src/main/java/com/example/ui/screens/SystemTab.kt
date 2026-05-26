package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun SystemTab(
    viewModel: RemoteControlViewModel,
    modifier: Modifier = Modifier
) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    // Confirmation dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingCommand by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }
    var dialogAccentColor by remember { mutableStateOf(GlassTheme.GlowCyan) }

    // Disconnected Screen
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
                    text = "Para poder ver la telemetría del sistema y apagar/suspender el PC, debes establecer enlace primero desde la pestaña de Conexión.",
                    fontSize = 13.sp,
                    color = GlassTheme.MutedGray,
                    lineHeight = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper metrics cards
        Text(
            text = "TELEMETRÍA EN TIEMPO REAL",
            fontSize = 12.sp,
            color = GlassTheme.MutedGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        // Clean Connection and Station Details Card instead of CPU/RAM gauges
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(GlassTheme.GlowCyan.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassTheme.GlowCyan.copy(alpha = 0.12f))
                        .border(1.dp, GlassTheme.GlowCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DesktopWindows,
                        contentDescription = null,
                        tint = GlassTheme.GlowCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "EQUIPO VINCULADO",
                        fontSize = 11.sp,
                        color = GlassTheme.GlowCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Windows 11 Workstation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Línea de control remoto activa",
                        fontSize = 11.sp,
                        color = GlassTheme.MutedGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Action Grid layout
        Text(
            text = "ACCIONES DE ENERGÍA Y CONTROL",
            fontSize = 12.sp,
            color = GlassTheme.MutedGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        // Grid contents
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Suspend Row Action
                PowerActionRow(
                    title = "Suspender Ordenador",
                    description = "Pone la sesión del PC en modo de bajo consumo",
                    icon = Icons.Default.Bedtime,
                    accentColor = GlassTheme.GlowViolet,
                    onClick = {
                        pendingCommand = "suspend"
                        dialogTitle = "¿Suspender el PC?"
                        dialogDescription = "¿Estás seguro de que quieres poner tu ordenador en modo de suspensión o reposo?"
                        dialogAccentColor = GlassTheme.GlowViolet
                        showConfirmDialog = true
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // Restart Row Action
                PowerActionRow(
                    title = "Reiniciar Ordenador",
                    description = "Cierra todo y vuelve a iniciar el PC",
                    icon = Icons.Default.RestartAlt,
                    accentColor = GlassTheme.GlowCyan,
                    onClick = {
                        pendingCommand = "restart"
                        dialogTitle = "¿Reiniciar el PC?"
                        dialogDescription = "Se cerrarán todos los programas abiertos. Asegúrate de haber guardado tu trabajo."
                        dialogAccentColor = GlassTheme.GlowCyan
                        showConfirmDialog = true
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // Shutdown Row Action
                PowerActionRow(
                    title = "Apagar Ordenador",
                    description = "Detiene los procesos y apaga el PC por completo",
                    icon = Icons.Default.PowerSettingsNew,
                    accentColor = GlassTheme.GlowCrimson,
                    onClick = {
                        pendingCommand = "shutdown"
                        dialogTitle = "¿Apagar el PC?"
                        dialogDescription = "Esta acción cerrará el sistema por completo. La conexión remota finalizará inmediatamente."
                        dialogAccentColor = GlassTheme.GlowCrimson
                        showConfirmDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Glassmorphic Custom-style Dialog overlay matching layout rules
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                GlassButton(
                    onClick = {
                        viewModel.sendSystemCommand(pendingCommand)
                        showConfirmDialog = false
                    },
                    accentColor = dialogAccentColor
                ) {
                    Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                GlassButton(
                    onClick = { showConfirmDialog = false },
                    accentColor = Color.White
                ) {
                    Text("Cancelar", color = Color.White)
                }
            },
            icon = {
                Icon(
                    imageVector = when(pendingCommand) {
                        "shutdown" -> Icons.Default.PowerSettingsNew
                        "restart" -> Icons.Default.RestartAlt
                        else -> Icons.Default.Bedtime
                    },
                    contentDescription = null,
                    tint = dialogAccentColor,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = dialogTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = dialogDescription,
                    color = GlassTheme.SemiWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = Color(0xFF141526),
            tonalElevation = 8.dp,
            modifier = Modifier
                .border(1.5.dp, dialogAccentColor.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
        )
    }
}

@Composable
fun PowerActionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = GlassTheme.MutedGray
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = GlassTheme.MutedGray.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
