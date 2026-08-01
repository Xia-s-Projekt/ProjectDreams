package com.projectdreams.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.projectdreams.app.data.DownloadNotifier
import com.projectdreams.app.ui.AppScreen
import com.projectdreams.app.ui.AppViewModel
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Shizuku permission granted", Toast.LENGTH_SHORT).show()
                viewModel.refreshInstallAvailability()
            } else {
                Toast.makeText(this, "Shizuku permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (intent.getBooleanExtra(DownloadNotifier.EXTRA_RESUME, false)) {
            viewModel.requestResume()
        }

        setContent {
            AppScreen(viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(DownloadNotifier.EXTRA_RESUME, false)) {
            viewModel.requestResume()
        }
    }

    override fun onResume() {
        super.onResume()
        // Picks up grants/revocations made in the Shizuku app, or a Shizuku
        // service that was started after the permission dialog was dismissed.
        viewModel.refreshInstallAvailability()
    }
}
