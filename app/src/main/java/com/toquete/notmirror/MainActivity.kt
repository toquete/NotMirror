package com.toquete.notmirror

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.toquete.notmirror.data.AllowlistDataStore
import com.toquete.notmirror.data.AllowlistRepository
import com.toquete.notmirror.ui.MainViewModel
import com.toquete.notmirror.ui.SettingsScreen
import com.toquete.notmirror.ui.theme.NotMirrorTheme

class MainActivity : ComponentActivity() {

    private val viewModelFactory = viewModelFactory {
        initializer {
            val dataStore = AllowlistDataStore(applicationContext)
            val repository = AllowlistRepository(dataStore, packageManager)
            MainViewModel(repository, applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotMirrorTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission each time the user returns (e.g. from Settings)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        viewModel.refreshPermissionStatus(this)
    }
}
