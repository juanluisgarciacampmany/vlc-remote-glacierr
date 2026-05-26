package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTheme
import com.example.ui.viewmodel.ConnectionStatus
import com.example.ui.viewmodel.RemoteControlViewModel
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun ExplorerTab(
    viewModel: RemoteControlViewModel,
    modifier: Modifier = Modifier
) {
    val currentPath by viewModel.currentPath.collectAsState()
    val parentPath by viewModel.parentPath.collectAsState()
    val fileItems by viewModel.fileItems.collectAsState()
    val isLoading by viewModel.isLoadingFiles.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    // Verification check: if not connected, show error screen
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
                    text = "Por favor, conecta un PC primero en la pestaña de Conexión para poder explorar tus archivos de vídeo.",
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
    ) {
        // Upper Navigation Info Bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = GlassTheme.GlowCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = currentPath.ifBlank { "Unidades del PC" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { viewModel.loadFiles(currentPath) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        tint = GlassTheme.GlowCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Parent navigation action row
        AnimatedVisibility(
            visible = parentPath != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { parentPath?.let { viewModel.loadFiles(it) } }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Subir de nivel",
                    tint = GlassTheme.GlowCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = ".. / Carpeta Anterior",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTheme.GlowCyan
                )
            }
        }

        // List loading state or table view
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GlassTheme.GlowCyan)
            }
        } else {
            if (fileItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = GlassTheme.MutedGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Este directorio está vacío",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = GlassTheme.MutedGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(fileItems) { item ->
                        FileItemRow(
                            item = item,
                            onClick = {
                                if (item.isDir) {
                                    viewModel.loadFiles(item.path)
                                } else {
                                    if (isVideoFile(item.extension)) {
                                        viewModel.openFileOnPC(item.path)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileItemRow(
    item: FileItem,
    onClick: () -> Unit
) {
    val isVideo = isVideoFile(item.extension)
    val testTag = if (item.isDir) "dir_${item.name}" else "file_${item.name}"

    // Design layout row using a semi translucent mini rounded card
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        backgroundBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                if (isVideo) GlassTheme.GlowCyan.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.04f),
                Color.White.copy(alpha = 0.01f)
            )
        ),
        borderBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                if (isVideo) GlassTheme.GlowCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.02f)
            )
        ),
        testTag = testTag
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon layout matching directory vs media file vs document file
            val (icon, color) = when {
                item.isDir -> Pair(Icons.Default.Folder, Color(0xFFFFA726)) // Amber-Orange directory
                isVideo -> Pair(Icons.Default.Movie, GlassTheme.GlowCyan) // Bright neon cyan video file
                else -> Pair(Icons.AutoMirrored.Filled.InsertDriveFile, GlassTheme.MutedGray) // Common doc file
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!item.isDir && item.sizeBytes > 0) {
                    Text(
                        text = formatFileSize(item.sizeBytes),
                        color = GlassTheme.MutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (item.isDir) {
                    Text(
                        text = "Carpeta de archivos",
                        color = GlassTheme.MutedGray.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Secondary visual action arrow
            if (item.isDir) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = GlassTheme.MutedGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            } else if (isVideo) {
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = "Reproducir en PC",
                    tint = GlassTheme.GlowCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Checks if extension matches common Windows playable video codecs
 */
fun isVideoFile(ext: String): Boolean {
    val videoExtensions = listOf(
        ".mp4", ".mkv", ".avi", ".mov", ".flv", ".wmv", 
        ".webm", ".3gp", ".m4v", ".mpg", ".mpeg", ".ts"
    )
    return videoExtensions.contains(ext.lowercase())
}

/**
 * Fast clean human-readable directory sizing Converter
 */
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}
