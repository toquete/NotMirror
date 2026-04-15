package com.toquete.notmirror.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "allowlist")

class AllowlistDataStore(private val context: Context) {

    private val allowedPackagesKey = stringSetPreferencesKey("allowed_packages")

    fun getAllowlistFlow(): Flow<Set<String>> =
        context.dataStore.data.map { prefs -> prefs[allowedPackagesKey] ?: emptySet() }

    suspend fun setAllowed(packageName: String, allowed: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[allowedPackagesKey] ?: emptySet()
            prefs[allowedPackagesKey] = if (allowed) current + packageName else current - packageName
        }
    }
}
