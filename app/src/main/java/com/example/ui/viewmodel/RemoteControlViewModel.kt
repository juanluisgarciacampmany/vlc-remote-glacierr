package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.model.*
import com.example.data.pref.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.ConnectException
import java.net.SocketTimeoutException

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    AUTH_ERROR,
    NETWORK_ERROR
}

enum class ActiveTab {
    EXPLORER,
    MEDIA,
    SYSTEM,
    CONNECTION
}

class RemoteControlViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    // Connection Form Fields (Pre-populate with stored values)
    val ipInput = MutableStateFlow(prefsManager.ipAddress)
    val portInput = MutableStateFlow(prefsManager.port.toString())
    val pinInput = MutableStateFlow(prefsManager.pin)

    // Bottom Navigation Tab State (defaults to CONNECTION if no IP is saved, otherwise EXPLORER)
    private val _currentTab = MutableStateFlow(
        if (prefsManager.ipAddress.isEmpty()) ActiveTab.CONNECTION else ActiveTab.EXPLORER
    )
    val currentTab: StateFlow<ActiveTab> = _currentTab.asStateFlow()

    // Connection Status
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // File Explorer State
    private val _currentPath = MutableStateFlow(prefsManager.lastPath)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _parentPath = MutableStateFlow<String?>(null)
    val parentPath: StateFlow<String?> = _parentPath.asStateFlow()

    private val _fileItems = MutableStateFlow<List<FileItem>>(emptyList())
    val fileItems: StateFlow<List<FileItem>> = _fileItems.asStateFlow()

    private val _isLoadingFiles = MutableStateFlow(false)
    val isLoadingFiles: StateFlow<Boolean> = _isLoadingFiles.asStateFlow()

    // Telemetry State
    private val _telemetry = MutableStateFlow<TelemetryResponse?>(null)
    val telemetry: StateFlow<TelemetryResponse?> = _telemetry.asStateFlow()

    // Feedback notifications for actions
    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    // LAN Subnet Scanner State
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scannedIpsFound = MutableStateFlow<List<String>>(emptyList())
    val scannedIpsFound: StateFlow<List<String>> = _scannedIpsFound.asStateFlow()

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        // If we already have stored credentials, attempt silent/auto connection
        if (ipInput.value.isNotEmpty() && pinInput.value.isNotEmpty()) {
            connectPC(ipInput.value, portInput.value.toIntOrNull() ?: 8000, pinInput.value)
        }
    }

    fun selectTab(tab: ActiveTab) {
        _currentTab.value = tab
    }

    fun updateIpInput(ip: String) {
        ipInput.value = ip
    }

    fun updatePortInput(port: String) {
        portInput.value = port
    }

    fun updatePinInput(pin: String) {
        pinInput.value = pin
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun getLocalIpPrefix(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ipInterface = interfaces.nextElement()
                if (ipInterface.isLoopback || !ipInterface.isUp) continue
                val addresses = ipInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is java.net.Inet4Address && !addr.isLoopbackAddress) {
                        val host = addr.hostAddress ?: continue
                        if (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.")) {
                            val lastDot = host.lastIndexOf('.')
                            if (lastDot > 0) {
                                return host.substring(0, lastDot + 1)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Manual input backup prefix
        val manualIp = ipInput.value.trim()
        val lastDot = manualIp.lastIndexOf('.')
        if (lastDot > 0) {
            return manualIp.substring(0, lastDot + 1)
        }
        
        return "192.168.1."
    }

    fun scanLocalNetwork() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scannedIpsFound.value = emptyList()
        _scanProgress.value = 0
        
        val prefix = getLocalIpPrefix()
        val targetPort = portInput.value.toIntOrNull() ?: 8000
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val foundList = java.util.Collections.synchronizedList(mutableListOf<String>())
            val jobs = mutableListOf<Job>()
            val scanSemaphore = Semaphore(30) // limit to 30 concurrent ping socket checks
            
            for (i in 1..254) {
                val ip = "$prefix$i"
                val job = launch(kotlinx.coroutines.Dispatchers.IO) {
                    scanSemaphore.withPermit {
                        try {
                            val socket = java.net.Socket()
                            socket.connect(java.net.InetSocketAddress(ip, targetPort), 500)
                            socket.close()
                            // Found an open socket of our target port on this IP!
                            foundList.add(ip)
                            _scannedIpsFound.value = foundList.toList()
                        } catch (e: Exception) {
                            // ignore/offline
                        } finally {
                            _scanProgress.value = _scanProgress.value + 1
                        }
                    }
                }
                jobs.add(job)
            }
            
            jobs.forEach { it.join() }
            _isScanning.value = false
            
            if (foundList.isEmpty()) {
                _actionFeedback.value = "Escaneo completado. No se detectó ningún PC en $prefix*"
            } else {
                _actionFeedback.value = "¡Terminado! Se encontraron ${foundList.size} PC(s) activo(s)"
            }
        }
    }

    fun connectWithQrCode(qrText: String): Boolean {
        val trimmed = qrText.trim()
        if (trimmed.isEmpty()) return false
        
        try {
            // Case 1: JSON payload
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val ipRegex = "\"ip\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val portRegex = "\"port\"\\s*:\\s*(\\d+)".toRegex()
                val pinRegex = "\"pin\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                
                val ipMatch = ipRegex.find(trimmed)?.groupValues?.get(1)
                val portMatch = portRegex.find(trimmed)?.groupValues?.get(1)?.toIntOrNull() ?: 8000
                val pinMatch = pinRegex.find(trimmed)?.groupValues?.get(1)
                
                if (ipMatch != null && pinMatch != null) {
                    updateIpInput(ipMatch)
                    updatePortInput(portMatch.toString())
                    updatePinInput(pinMatch)
                    connectPC(ipMatch, portMatch, pinMatch)
                    return true
                }
            }
            
            // Case 2: Custom URI
            if (trimmed.startsWith("pcremote://")) {
                val cleanUri = trimmed.removePrefix("pcremote://")
                var pin: String? = null
                val pinParamIdx = cleanUri.indexOf("?pin=")
                val hostPart = if (pinParamIdx > 0) {
                    pin = cleanUri.substring(pinParamIdx + 5)
                    cleanUri.substring(0, pinParamIdx)
                } else {
                    cleanUri
                }
                
                val parts = hostPart.split(":", "/")
                if (parts.isNotEmpty()) {
                    val ip = parts[0]
                    val port = if (parts.size > 1) parts[1].toIntOrNull() ?: 8000 else 8000
                    val finalPin = pin ?: if (parts.size > 2) parts[2] else ""
                    
                    updateIpInput(ip)
                    updatePortInput(port.toString())
                    updatePinInput(finalPin)
                    connectPC(ip, port, finalPin)
                    return true
                }
            }
            
            // Case 3: Simple colon-separated format
            val colonParts = trimmed.split(":")
            if (colonParts.size == 3) {
                val ip = colonParts[0]
                val port = colonParts[1].toIntOrNull() ?: 8000
                val pin = colonParts[2]
                
                updateIpInput(ip)
                updatePortInput(port.toString())
                updatePinInput(pin)
                connectPC(ip, port, pin)
                return true
            } else if (colonParts.size == 2) {
                val ip = colonParts[0]
                val pin = colonParts[1]
                
                updateIpInput(ip)
                updatePortInput("8000")
                updatePinInput(pin)
                connectPC(ip, 8000, pin)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Authenticate and connect with the Windows PC.
     */
    fun connectPC(ip: String, port: Int, pin: String) {
        viewModelScope.launch {
            if (ip.isBlank()) {
                _errorMessage.value = "La dirección IP no puede estar vacía."
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                return@launch
            }
            if (pin.isBlank()) {
                _errorMessage.value = "El PIN de seguridad es obligatorio."
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                return@launch
            }

            _connectionStatus.value = ConnectionStatus.CONNECTING
            _errorMessage.value = null

            try {
                val service = RetrofitClient.getService(ip, port)
                val response = service.authenticate(AuthRequest(pin = pin))
                
                if (response.authenticated) {
                    // Save successful configs
                    prefsManager.ipAddress = ip
                    prefsManager.port = port
                    prefsManager.pin = pin
                    
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    _actionFeedback.value = "Conexión establecida con éxito 🟢"
                    
                    // Switch to explorer tab automatically on first connection
                    if (_currentTab.value == ActiveTab.CONNECTION) {
                        _currentTab.value = ActiveTab.EXPLORER
                    }

                    // Load explorer list and trigger telemetry
                    loadFiles(prefsManager.lastPath)
                    startTelemetryPolling(ip, port, pin)
                } else {
                    _connectionStatus.value = ConnectionStatus.AUTH_ERROR
                    _errorMessage.value = "PIN de seguridad incorrecto."
                }
            } catch (e: Exception) {
                _connectionStatus.value = ConnectionStatus.NETWORK_ERROR
                _errorMessage.value = when (e) {
                    is SocketTimeoutException -> "Tiempo de espera agotado. Verifica el firewall y puertos del PC."
                    is ConnectException -> "No se pudo conectar al servidor. ¿Está el backend encendido y en la misma red?"
                    else -> "Error de red: ${e.localizedMessage ?: "No se puede alcanzar el servidor."}"
                }
            }
        }
    }

    /**
     * Disconnects and stops updates.
     */
    fun disconnect() {
        telemetryJob?.cancel()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _telemetry.value = null
        _fileItems.value = emptyList()
        _currentPath.value = ""
        _parentPath.value = null
        _actionFeedback.value = "PC Desconectado 🔴"
    }

    /**
     * Periodically pulls telemetry reports from the server.
     */
    private fun startTelemetryPolling(ip: String, port: Int, pin: String) {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                if (_connectionStatus.value != ConnectionStatus.CONNECTED) break
                try {
                    val service = RetrofitClient.getService(ip, port)
                    val response = service.getTelemetry(pin)
                    _telemetry.value = response
                } catch (e: Exception) {
                    // Silently fail network drop in telemetry to avoid continuous error dialogs
                }
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    /**
     * Browse folder contents.
     */
    fun loadFiles(path: String) {
        val ip = prefsManager.ipAddress
        val port = prefsManager.port
        val pin = prefsManager.pin

        if (ip.isEmpty() || pin.isEmpty()) return

        viewModelScope.launch {
            _isLoadingFiles.value = true
            _errorMessage.value = null
            try {
                val service = RetrofitClient.getService(ip, port)
                val response = service.browseFiles(pin, path)
                
                _currentPath.value = response.currentPath
                _parentPath.value = response.parentPath
                _fileItems.value = response.items

                // Persist successful directory navigated
                prefsManager.lastPath = response.currentPath
            } catch (e: Exception) {
                _errorMessage.value = "Error al listar archivos: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _isLoadingFiles.value = false
            }
        }
    }

    /**
     * Play media file.
     */
    fun openFileOnPC(filePath: String) {
        val ip = prefsManager.ipAddress
        val port = prefsManager.port
        val pin = prefsManager.pin

        if (ip.isEmpty() || pin.isEmpty()) return

        viewModelScope.launch {
            try {
                val service = RetrofitClient.getService(ip, port)
                val response = service.openFile(pin, FileOpenRequest(filePath))
                _actionFeedback.value = response.message
            } catch (e: Exception) {
                _errorMessage.value = "No se pudo reproducir el archivo. Comprueba la conexión."
            }
        }
    }

    /**
     * Send play pause, seek, volume actions.
     */
    fun sendMediaControl(action: String) {
        val ip = prefsManager.ipAddress
        val port = prefsManager.port
        val pin = prefsManager.pin

        if (ip.isEmpty() || pin.isEmpty()) return

        viewModelScope.launch {
            try {
                val service = RetrofitClient.getService(ip, port)
                val response = service.controlVlc(pin, ControlRequest(action))
                // Minimalist UI: visual action flash feedback
                _actionFeedback.value = when (response.action) {
                    "play_pause" -> "Reproducir / Pausar ⏯️"
                    "volume_up" -> "Subir Volumen 🔊"
                    "volume_down" -> "Bajar Volumen 🔉"
                    "seek_forward" -> "Avanzar +10s ⏩"
                    "seek_backward" -> "Retroceder -10s ⏪"
                    "toggle_fullscreen" -> "Pantalla Completa 🖥️"
                    else -> "Control enviado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar comando: ${e.localizedMessage ?: "Fallo de conexión"}"
            }
        }
    }

    /**
     * Direct OS control commands.
     */
    fun sendSystemCommand(command: String) {
        val ip = prefsManager.ipAddress
        val port = prefsManager.port
        val pin = prefsManager.pin

        if (ip.isEmpty() || pin.isEmpty()) return

        viewModelScope.launch {
            try {
                val service = RetrofitClient.getService(ip, port)
                val response = service.executeSystemCommand(pin, SystemCommandRequest(command))
                _actionFeedback.value = response.message
                
                // If it is a shutdown, restart, or suspend, disconnect locally right away
                if (command in listOf("shutdown", "restart", "suspend")) {
                    delay(1500)
                    disconnect()
                    _currentTab.value = ActiveTab.CONNECTION
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al ejecutar '${command}' en el PC"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        telemetryJob?.cancel()
    }
}
