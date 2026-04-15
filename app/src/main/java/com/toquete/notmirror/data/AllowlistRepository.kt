package com.toquete.notmirror.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isAllowed: Boolean
)

class AllowlistRepository(
    private val dataStore: AllowlistDataStore,
    private val packageManager: PackageManager
) {

    fun getAllowlistFlow(): Flow<Set<String>> = dataStore.getAllowlistFlow()

    suspend fun setAllowed(packageName: String, allowed: Boolean) =
        dataStore.setAllowed(packageName, allowed)

    // Must be called on Dispatchers.IO
    fun getInstalledApps(allowlist: Set<String>): List<AppInfo> =
        packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map { info ->
                AppInfo(
                    packageName = info.packageName,
                    appName = packageManager.getApplicationLabel(info).toString(),
                    isAllowed = info.packageName in allowlist
                )
            }
            .sortedBy { it.appName.lowercase() }
}
