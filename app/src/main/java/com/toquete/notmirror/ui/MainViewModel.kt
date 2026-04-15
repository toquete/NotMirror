package com.toquete.notmirror.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Build
import android.text.format.Formatter
import android.net.wifi.WifiManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toquete.notmirror.data.AppInfo
import com.toquete.notmirror.data.AllowlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.Inet4Address

data class UiState(
    val deviceIp: String = "",
    val serverPort: Int = 8765,
    val apps: List<AppInfo> = emptyList(),
    val isListenerPermissionGranted: Boolean = false
)

class MainViewModel(
    private val repository: AllowlistRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadDeviceIp()
        observeAllowlist()
    }

    private fun loadDeviceIp() {
        val ip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val lp: LinkProperties? = cm.getLinkProperties(cm.activeNetwork)
            lp?.linkAddresses
                ?.firstOrNull { it.address is Inet4Address }
                ?.address?.hostAddress ?: "unknown"
        } else {
            @Suppress("DEPRECATION")
            val wm = appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        }
        _uiState.update { it.copy(deviceIp = ip) }
    }

    private fun observeAllowlist() {
        viewModelScope.launch {
            repository.getAllowlistFlow().collect { allowlist ->
                val apps = if (_uiState.value.apps.isEmpty()) {
                    // First load: fetch installed apps on IO
                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                        repository.getInstalledApps(allowlist)
                    }
                } else {
                    // Subsequent updates: just flip the isAllowed flag
                    _uiState.value.apps.map { it.copy(isAllowed = it.packageName in allowlist) }
                }
                _uiState.update { it.copy(apps = apps) }
            }
        }
    }

    fun toggleAppAllowlist(packageName: String, allowed: Boolean) {
        viewModelScope.launch { repository.setAllowed(packageName, allowed) }
    }

    fun refreshPermissionStatus(context: Context) {
        val granted = NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)
        _uiState.update { it.copy(isListenerPermissionGranted = granted) }
    }
}
