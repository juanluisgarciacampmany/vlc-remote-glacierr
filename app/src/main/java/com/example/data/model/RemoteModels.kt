package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthRequest(
    @Json(name = "pin") val pin: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "authenticated") val authenticated: Boolean,
    @Json(name = "token") val token: String
)

@JsonClass(generateAdapter = true)
data class TelemetryResponse(
    @Json(name = "cpu_usage_percent") val cpuUsagePercent: Double,
    @Json(name = "ram_used_gb") val ramUsedGb: Double,
    @Json(name = "ram_total_gb") val ramTotalGb: Double,
    @Json(name = "ram_usage_percent") val ramUsagePercent: Double,
    @Json(name = "disk_usage_percent") val diskUsagePercent: Double,
    @Json(name = "disk_free_gb") val diskFreeGb: Double,
    @Json(name = "vlc_running") val vlcRunning: Boolean
)

@JsonClass(generateAdapter = true)
data class FileItem(
    @Json(name = "name") val name: String,
    @Json(name = "path") val path: String,
    @Json(name = "is_dir") val isDir: Boolean,
    @Json(name = "size_bytes") val sizeBytes: Long,
    @Json(name = "extension") val extension: String
)

@JsonClass(generateAdapter = true)
data class BrowseResponse(
    @Json(name = "current_path") val currentPath: String,
    @Json(name = "parent_path") val parentPath: String?,
    @Json(name = "items") val items: List<FileItem>
)

@JsonClass(generateAdapter = true)
data class FileOpenRequest(
    @Json(name = "filepath") val filepath: String
)

@JsonClass(generateAdapter = true)
data class ControlRequest(
    @Json(name = "action") val action: String
)

@JsonClass(generateAdapter = true)
data class ControlResponse(
    @Json(name = "status") val status: String,
    @Json(name = "action") val action: String
)

@JsonClass(generateAdapter = true)
data class SystemCommandRequest(
    @Json(name = "command") val command: String
)

@JsonClass(generateAdapter = true)
data class ActionResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String
)
