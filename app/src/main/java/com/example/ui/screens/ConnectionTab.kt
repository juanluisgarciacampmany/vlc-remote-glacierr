package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlassTheme
import com.example.ui.viewmodel.ConnectionStatus
import com.example.ui.viewmodel.RemoteControlViewModel

@Composable
fun ConnectionTab(
    viewModel: RemoteControlViewModel,
    modifier: Modifier = Modifier
) {
    val ip by viewModel.ipInput.collectAsState()
    val port by viewModel.portInput.collectAsState()
    val pin by viewModel.pinInput.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    val isScanning by viewModel.isScanning.collectAsState()
    val scannedIpsFound by viewModel.scannedIpsFound.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()

    var showQrDialog by remember { mutableStateOf(false) }
    var qrInputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DesktopWindows,
                contentDescription = "PC Remote",
                tint = GlassTheme.GlowCyan,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "PC REMOTE",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
        }
        
        Text(
            text = "Enlace Seguro de Red Local",
            fontSize = 14.sp,
            color = GlassTheme.MutedGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Connection Status Cards
        AnimatedVisibility(
            visible = status != ConnectionStatus.CONNECTED,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                borderBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        if (status == ConnectionStatus.NETWORK_ERROR || status == ConnectionStatus.AUTH_ERROR) 
                            GlassTheme.GlowCrimson.copy(alpha = 0.4f) 
                        else Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            ) {
                Text(
                    text = "ESTADO DE CONEXIÓN",
                    fontSize = 12.sp,
                    color = GlassTheme.MutedGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (statusText, statusColor, statusIcon) = when (status) {
                        ConnectionStatus.DISCONNECTED -> Triple(
                            "Desconectado", 
                            GlassTheme.MutedGray, 
                            Icons.Default.CloudOff
                        )
                        ConnectionStatus.CONNECTING -> Triple(
                            "Conectando...", 
                            GlassTheme.GlowCyan, 
                            Icons.Default.Refresh
                        )
                        ConnectionStatus.CONNECTED -> Triple(
                            "Conectado 🟢", 
                            GlassTheme.GlowCyan, 
                            Icons.Default.CloudQueue
                        )
                        ConnectionStatus.AUTH_ERROR -> Triple(
                            "Fallo de PIN 🔑", 
                            GlassTheme.GlowCrimson, 
                            Icons.Default.VpnKeyOff
                        )
                        ConnectionStatus.NETWORK_ERROR -> Triple(
                            "Inalcanzable 📡", 
                            GlassTheme.GlowCrimson, 
                            Icons.Default.SignalWifiStatusbarConnectedNoInternet4
                        )
                    }

                    if (status == ConnectionStatus.CONNECTING) {
                        CircularProgressIndicator(
                            color = GlassTheme.GlowCyan,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = statusText,
                            tint = statusColor,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                    }

                    Text(
                        text = statusText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                errorMsg?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                        color = GlassTheme.GlowCrimson,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        // Active Connection Information
        AnimatedVisibility(
            visible = status == ConnectionStatus.CONNECTED,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                borderBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(GlassTheme.GlowCyan.copy(alpha = 0.4f), Color.White.copy(alpha = 0.05f))
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Conectado",
                        tint = GlassTheme.GlowCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "EQUIPO VINCULADO",
                            fontSize = 11.sp,
                            color = GlassTheme.GlowCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (ip.isBlank()) "PC Remoto" else ip,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Show connection status without CPU & RAM
                telemetry?.let { tel ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CANAL SEGURO", fontSize = 11.sp, color = GlassTheme.MutedGray, fontWeight = FontWeight.Bold)
                            Text("Activo 📶", fontSize = 14.sp, color = GlassTheme.GlowCyan, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("VLC PLAYER", fontSize = 11.sp, color = GlassTheme.MutedGray, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (tel.vlcRunning) "Reproduciendo ⏯️" else "Disponible",
                                fontSize = 14.sp,
                                color = if (tel.vlcRunning) GlassTheme.GlowCyan else GlassTheme.SemiWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GlassButton(
                    onClick = { viewModel.disconnect() },
                    accentColor = GlassTheme.GlowCrimson,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "disconnect_button"
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desconectar Enlace", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Smart LAN scanner and QR entry card!
        if (status != ConnectionStatus.CONNECTED) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                borderBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(GlassTheme.GlowCyan.copy(alpha = 0.3f), Color.White.copy(alpha = 0.05f))
                )
            ) {
                Text(
                    text = "VÍNCULO ULTRA-RÁPIDO (CERO CONFIG.)",
                    fontSize = 12.sp,
                    color = GlassTheme.MutedGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        onClick = { viewModel.scanLocalNetwork() },
                        accentColor = GlassTheme.GlowCyan,
                        enabled = !isScanning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.WifiTethering, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detectar Red", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    GlassButton(
                        onClick = { 
                            qrInputText = ""
                            showQrDialog = true 
                        },
                        accentColor = GlassTheme.GlowViolet,
                        enabled = !isScanning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vincular QR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // If scanning, show Radar Sonar bar:
                if (isScanning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { scanProgress / 254f },
                            color = GlassTheme.GlowCyan,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rastreando subred de Windows 11... $scanProgress / 254 IPs",
                            fontSize = 11.sp,
                            color = GlassTheme.GlowCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Show list of found devices
                if (scannedIpsFound.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "EQUIPOS DETECTADOS EN TU LOCAL:",
                        fontSize = 10.sp,
                        color = GlassTheme.MutedGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        scannedIpsFound.forEach { discoveredIp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, GlassTheme.GlowCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.updateIpInput(discoveredIp)
                                        val portNum = port.toIntOrNull() ?: 8000
                                        viewModel.connectPC(discoveredIp, portNum, pin)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DesktopWindows,
                                        contentDescription = null,
                                        tint = GlassTheme.GlowCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("PC Windows 11", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(discoveredIp, fontSize = 11.sp, color = GlassTheme.MutedGray)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GlassTheme.GlowCyan.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("CONECTAR", fontSize = 10.sp, color = GlassTheme.GlowCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Configuration Form (Hidden or faded down when connected to prevent modification errors)
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "CONFIGURACIÓN",
                fontSize = 12.sp,
                color = GlassTheme.MutedGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            GlassTextField(
                value = ip,
                onValueChange = { viewModel.updateIpInput(it) },
                label = "DIRECCIÓN IP",
                placeholder = "ej. 192.168.1.45",
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = GlassTheme.GlowCyan) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                testTag = "ip_input",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassTextField(
                    value = port,
                    onValueChange = { viewModel.updatePortInput(it) },
                    label = "PUERTO",
                    placeholder = "8000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "port_input",
                    modifier = Modifier.weight(0.4f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                GlassTextField(
                    value = pin,
                    onValueChange = { viewModel.updatePinInput(it) },
                    label = "PIN DE SEGURIDAD",
                    placeholder = "ej. 123456",
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GlassTheme.GlowCyan) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "pin_input",
                    modifier = Modifier.weight(0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GlassButton(
                onClick = { 
                    val portNum = port.toIntOrNull() ?: 8000
                    viewModel.connectPC(ip, portNum, pin) 
                },
                enabled = status != ConnectionStatus.CONNECTING,
                accentColor = GlassTheme.GlowCyan,
                modifier = Modifier.fillMaxWidth(),
                testTag = "connect_button"
            ) {
                if (status == ConnectionStatus.CONNECTING) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Estableciendo enlace...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Power, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emparejar PC", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Quick help card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = GlassTheme.GlowCyan.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Instrucciones Rápidas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Abre el servidor Python en tu PC.\n" +
                               "2. Asegúrate de estar conectado a la misma red Wi-Fi.\n" +
                               "3. Copia la IP y el Código PIN generados en el ordenador y pulsa 'Emparejar'.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = GlassTheme.MutedGray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showQrDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQrDialog = false },
            confirmButton = {
                GlassButton(
                    onClick = {
                        val parsed = viewModel.connectWithQrCode(qrInputText)
                        if (parsed) {
                            showQrDialog = false
                        }
                    },
                    accentColor = GlassTheme.GlowViolet,
                    enabled = qrInputText.isNotBlank()
                ) {
                    Text("Vincular", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                GlassButton(
                    onClick = { showQrDialog = false },
                    accentColor = Color.White.copy(alpha = 0.5f)
                ) {
                    Text("Cancelar", color = Color.White)
                }
            },
            title = {
                Text(
                    text = "VÍNCULO RÁPIDO CON QR",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escanea el código QR generado en el servidor de PC o pega su contenido aquí para conectar instantáneamente.",
                        fontSize = 12.sp,
                        color = GlassTheme.MutedGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Futuristic HUD Scanning effect Box
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(1.5.dp, GlassTheme.GlowViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Four corner neon angle brackets
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = GlassTheme.GlowViolet.copy(alpha = 0.4f),
                            modifier = Modifier.size(96.dp)
                        )
                        
                        // Sliding laser line animation or simple static glass accent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(Color.Transparent, GlassTheme.GlowViolet, Color.Transparent)
                                    )
                                )
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TextField to paste or input connection string
                    GlassTextField(
                        value = qrInputText,
                        onValueChange = { qrInputText = it },
                        label = "CÓDIGO DE CONEXIÓN O JSON",
                        placeholder = "ej. pcremote://192.168.1.45:8000?pin=123",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF121324),
            modifier = Modifier
                .border(1.5.dp, GlassTheme.GlowViolet.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
        )
    }
}
