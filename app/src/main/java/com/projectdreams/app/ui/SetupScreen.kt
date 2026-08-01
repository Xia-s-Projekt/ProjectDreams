package com.projectdreams.app.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.projectdreams.app.ui.theme.AbsoluteSmoothCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.projectdreams.app.data.install.InstallManager
import kotlinx.coroutines.launch

/** Number of pages in the setup wizard. */
private const val PAGE_COUNT = 4

@Composable
fun SetupScreen(
    rootAvailable: Boolean,
    shizukuAvailable: Boolean,
    deleteAfterInstall: Boolean,
    onSelectMode: (InstallManager.Mode) -> Unit,
    onRequestShizuku: () -> Unit,
    onRecheckRoot: () -> Unit,
    onDeleteAfterInstallChanged: (Boolean) -> Unit,
    onFinish: (InstallManager.Mode) -> Unit
) {
    var selectedMode by remember { mutableStateOf<InstallManager.Mode?>(null) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var disclaimerAccepted by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        PageDots(pagerState.currentPage, PAGE_COUNT)
        Spacer(Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (page) {
                    0 -> WelcomePage()
                    1 -> MethodPage(
                        rootAvailable = rootAvailable,
                        shizukuAvailable = shizukuAvailable,
                        selectedMode = selectedMode,
                        onSelectMode = { selectedMode = it },
                        onRequestShizuku = onRequestShizuku,
                        onRecheckRoot = onRecheckRoot
                    )
                    2 -> BackgroundPage(
                        notificationsEnabled = notificationsEnabled,
                        deleteAfterInstall = deleteAfterInstall,
                        onNotificationsChanged = { enabled ->
                            notificationsEnabled = enabled
                            if (enabled) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onDeleteAfterInstallChanged = onDeleteAfterInstallChanged
                    )
                    3 -> DisclaimerPage(accepted = disclaimerAccepted) { disclaimerAccepted = it }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage > 0) {
                TextButton(onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }) {
                    Text("Back")
                }
            }
            Spacer(Modifier.weight(1f))
            val lastPage = pagerState.currentPage == PAGE_COUNT - 1
            val canAdvance = when (pagerState.currentPage) {
                1 -> selectedMode != null
                3 -> disclaimerAccepted
                else -> true
            }
            Button(
                onClick = {
                    if (lastPage) {
                        val mode = selectedMode ?: return@Button
                        onFinish(mode)
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = canAdvance,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (lastPage) "Get started" else "Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (lastPage) {
                    Spacer(Modifier.size(8.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun PageDots(current: Int, count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == current) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    Spacer(Modifier.height(24.dp))
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(AbsoluteSmoothCornerShape(32.dp, 60))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Android,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    
    Spacer(Modifier.height(32.dp))
    Text(
        text = "Welcome to ProjectDreams",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = "Download and update hololive Dreams straight from the Google Play store, " +
            "without opening the Play Store itself.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = "You'll be asked a few quick questions to set things up.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun MethodPage(
    rootAvailable: Boolean,
    shizukuAvailable: Boolean,
    selectedMode: InstallManager.Mode?,
    onSelectMode: (InstallManager.Mode) -> Unit,
    onRequestShizuku: () -> Unit,
    onRecheckRoot: () -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Choose how apps get installed",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = "You can change this later in Settings.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))

    SetupMethodCard(
        title = "Root access",
        subtitle = "Installs via a root shell. Recommended on rooted devices.",
        icon = Icons.Filled.Security,
        available = rootAvailable,
        selected = selectedMode == InstallManager.Mode.ROOT,
        onClick = { onSelectMode(InstallManager.Mode.ROOT) },
        trailing = {
            if (selectedMode == InstallManager.Mode.ROOT && !rootAvailable) {
                TextButton(onClick = onRecheckRoot) {
                    Text("Re-check")
                }
            }
        }
    )
    Spacer(Modifier.height(12.dp))

    SetupMethodCard(
        title = "Shizuku",
        subtitle = if (shizukuAvailable) {
            "Installs through the Shizuku service."
        } else {
            "Shizuku is not running or not granted yet. Tap “Grant” below."
        },
        icon = Icons.Filled.Android,
        available = shizukuAvailable,
        selected = selectedMode == InstallManager.Mode.SHIZUKU,
        onClick = { onSelectMode(InstallManager.Mode.SHIZUKU) },
        trailing = {
            if (selectedMode == InstallManager.Mode.SHIZUKU && !shizukuAvailable) {
                TextButton(onClick = onRequestShizuku) {
                    Text("Grant")
                }
            }
        }
    )

    if (selectedMode == InstallManager.Mode.ROOT && !rootAvailable) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Root is not granted to this app yet. Install a root manager and grant " +
                "access, or pick Shizuku instead.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BackgroundPage(
    notificationsEnabled: Boolean,
    deleteAfterInstall: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onDeleteAfterInstallChanged: (Boolean) -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Tune how ProjectDreams behaves",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = null)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Be notified about new versions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ProjectDreams checks the Play Store regularly for updates",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChanged
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Delete downloads after installing",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Frees ~350 MB per install by removing the downloaded APKs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = deleteAfterInstall,
                onCheckedChange = onDeleteAfterInstallChanged
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        text = "Stay running in the background",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    BackgroundKillerCard()
}

@Composable
private fun DisclaimerPage(accepted: Boolean, onAccepted: (Boolean) -> Unit) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = "One last thing",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Disclaimer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "ProjectDreams is an independent, fan-made downloader and is NOT " +
                    "affiliated with, endorsed by, or connected to COVER Corp or QualiArts " +
                    "in any way, shape or form.\n\n" +
                    "hololive Dreams is 100% owned by COVER Corp and QualiArts, and all " +
                    "rights to the game and its content belong to them. This project makes " +
                    "no revenue or profit from it, and hosts no copyrighted content — the " +
                    "app is downloaded directly from the official Google Play servers.\n\n" +
                    "All trademarks, logos and names are the property of their respective owners.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Switch(
                checked = accepted,
                onCheckedChange = onAccepted,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "I understand and agree",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SetupMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    available: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing()
        }
    }
}
