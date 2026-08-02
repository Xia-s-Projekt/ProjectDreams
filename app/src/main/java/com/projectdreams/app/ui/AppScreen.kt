package com.projectdreams.app.ui

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.heightIn
import androidx.compose.animation.core.animateFloat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.platform.LocalContext
import com.projectdreams.app.data.InstalledAppInfo

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.ScrollState
import com.projectdreams.app.ui.theme.BouncyButton
import com.projectdreams.app.ui.theme.BouncyCard
import com.projectdreams.app.ui.theme.BouncyIconButton
import com.projectdreams.app.ui.theme.BouncyOutlinedButton
import com.projectdreams.app.ui.theme.BouncySwitch
import com.projectdreams.app.ui.theme.BouncyTextButton
import com.projectdreams.app.ui.theme.SquircleCard
import com.projectdreams.app.ui.theme.WavyProgressIndicator
import com.projectdreams.app.ui.theme.bouncyPress
import com.projectdreams.app.ui.theme.AbsoluteSmoothCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Image
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Edit
import com.projectdreams.app.ui.theme.MorphingLoadingIndicator
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.aurora.gplayapi.data.models.App
import com.projectdreams.app.data.Region
import com.projectdreams.app.data.Game
import com.projectdreams.app.data.SettingsRepository
import com.projectdreams.app.data.install.InstallManager
import com.projectdreams.app.data.model.ResumeInfo
import com.projectdreams.app.ui.theme.ProjectDreamsTheme
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private enum class Screen { Main, GameManager, Settings, CheckUpdates, AddGame }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    viewModel: AppViewModel = viewModel(),
    resumeDownload: Boolean = false
) {
    ProjectDreamsTheme {
        val onboarded by viewModel.onboarded.collectAsStateWithLifecycle()
        if (!onboarded) {
            SetupGate(viewModel)
            return@ProjectDreamsTheme
        }

        LaunchedEffect(Unit) {
            if (resumeDownload) viewModel.requestResume()
        }

        var screen by remember { mutableStateOf(Screen.Main) }
        BackHandler(enabled = screen != Screen.Main) {
            screen = if (screen == Screen.CheckUpdates) Screen.Settings else Screen.Main
        }
        
        var isBottomBarVisible by remember { mutableStateOf(true) }
        LaunchedEffect(screen) {
            isBottomBarVisible = true
        }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
                    if (available.y < -5f) {
                        isBottomBarVisible = false
                    } else if (available.y > 5f) {
                        isBottomBarVisible = true
                    }
                    return androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }
        
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).nestedScroll(nestedScrollConnection)) {
            androidx.compose.animation.AnimatedContent(
                targetState = screen,
                label = "ScreenTransition",
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) {
                        androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right
                    }
                    slideIntoContainer(
                        towards = direction,
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = 800f,
                            dampingRatio = 0.9f
                        )
                    ).togetherWith(
                        slideOutOfContainer(
                            towards = direction,
                            animationSpec = androidx.compose.animation.core.spring(
                                stiffness = 800f,
                                dampingRatio = 0.9f
                            )
                        )
                    )
                }
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.Main -> MainContent(viewModel)
                    Screen.GameManager -> GameManagerScreen(viewModel, onNavigate = { screen = it })
                    Screen.Settings -> SettingsScreen(
                        viewModel,
                        onOpenCheckUpdates = { screen = Screen.CheckUpdates },
                        onNavigateToAddGame = { screen = Screen.AddGame }
                    )
                    Screen.CheckUpdates -> CheckUpdatesScreen(
                        viewModel,
                        onBack = { screen = Screen.Settings }
                    )
                    Screen.AddGame -> AddGameScreen(
                        viewModel,
                        onBack = { screen = Screen.Settings }
                    )
                }
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it * 2 }),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it * 2 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingNavBar(
                    currentScreen = screen,
                    onNavigate = { screen = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(viewModel: AppViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val installState by viewModel.installState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val region by viewModel.region.collectAsStateWithLifecycle()
    val game by viewModel.game.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    val showAppTitle by remember { derivedStateOf { scrollState.value > 250 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    androidx.compose.animation.AnimatedContent(
                        targetState = showAppTitle && uiState is AppUiState.Ready,
                        label = "AppTitle"
                    ) { showApp ->
                        if (showApp) {
                            val app = (uiState as AppUiState.Ready).app
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = app.iconArtwork?.url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(32.dp).clip(AbsoluteSmoothCornerShape(8.dp, 60)).background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(app.displayName, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Text("ProjectDreams", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                actions = {
                    val currentApp = (uiState as? AppUiState.Ready)?.app
                    val gameDetails by viewModel.gameDetails.collectAsStateWithLifecycle()
    val allGames by viewModel.allGames.collectAsStateWithLifecycle()
                    GameDropdown(
                        game = game,
                        allGames = allGames,
                        appIconUrl = currentApp?.iconArtwork?.url,
                        gameDetails = gameDetails,
                        onSelect = viewModel::setGame
                    )
                    Spacer(Modifier.width(8.dp))
                    RegionDropdown(region = region, onSelect = viewModel::setRegion)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadApp() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is AppUiState.Loading -> LoadingView()
                is AppUiState.Error -> {
                    if (state.message.contains("not found", ignoreCase = true) || state.message.contains("404", ignoreCase = true) || state.message.contains("DF-DFERH-01", ignoreCase = true)) {
                        RegionUnavailableView(onRetry = { viewModel.loadApp() })
                    } else {
                        ErrorView(state.message) { viewModel.loadApp() }
                    }
                }
                is AppUiState.Ready -> AppDetailView(
                    state = state,
                    installState = installState,
                    viewModel = viewModel,
                    scrollState = scrollState
                )
            }
        }
    }
}

/** Inline region selector next to the app title; picking one reloads instantly. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionDropdown(region: Region, onSelect: (Region) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        BouncyTextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .clip(AbsoluteSmoothCornerShape(8.dp, 60))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = region.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        if (expanded) {
            ModalBottomSheet(
                onDismissRequest = { expanded = false },
                sheetState = rememberModalBottomSheetState(),
                shape = AbsoluteSmoothCornerShape(32.dp, 60)
            ) {
                androidx.compose.foundation.layout.Column(Modifier.padding(bottom = 24.dp)) {
                    Region.entries.forEach { option ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expanded = false
                                    onSelect(option)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (option == region) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                            } else {
                                Spacer(Modifier.width(40.dp))
                            }
                            Text(
                                option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (option == region) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupGate(viewModel: AppViewModel) {
    val rootAvailable by viewModel.rootAvailable.collectAsStateWithLifecycle()
    val shizukuAvailable by viewModel.shizukuAvailable.collectAsStateWithLifecycle()
    val deleteAfterInstall by viewModel.deleteAfterInstall.collectAsStateWithLifecycle()

    SetupScreen(
        rootAvailable = rootAvailable,
        shizukuAvailable = shizukuAvailable,
        deleteAfterInstall = deleteAfterInstall,
        onSelectMode = { viewModel.selectInstallMode(it) },
        onRequestShizuku = viewModel::requestShizukuPermission,
        onRecheckRoot = { viewModel.refreshPrivilegeStatus(forceRootRecheck = true) },
        onDeleteAfterInstallChanged = viewModel::setDeleteAfterInstall,
        onFinish = { viewModel.completeSetup(it) }
    )
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MorphingLoadingIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    val isOffline = message.contains("Unable to resolve host", ignoreCase = true) || message.contains("No address associated", ignoreCase = true)
    val displayMessage = if (isOffline) "No internet connection. Please check your network and try again." else message
    val icon = if (isOffline) Icons.Filled.WifiOff else Icons.Filled.Info
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        BouncyButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun RegionUnavailableView(onRetry: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer { rotationZ = rotation }
                .clip(AbsoluteSmoothCornerShape(36.dp, 60))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer { rotationZ = -rotation }, // Counter-rotate icon
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Region Unavailable",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "This application is not available in the currently selected region. Please switch regions from the top menu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        BouncyButton(
            onClick = onRetry
        ) {
            Text("Refresh", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailView(
    state: AppUiState.Ready,
    installState: InstallUiState,
    viewModel: AppViewModel,
    scrollState: ScrollState
) {
    val app = state.app
    var descriptionExpanded by remember { mutableStateOf(false) }
    var changelogExpanded by remember { mutableStateOf(false) }
    var showUninstallDialog by remember { mutableStateOf(false) }
    var showInstallConfirm by remember { mutableStateOf(false) }
    var fullscreenScreenshot by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }

    val rootAvailable by viewModel.rootAvailable.collectAsStateWithLifecycle()
    val shizukuAvailable by viewModel.shizukuAvailable.collectAsStateWithLifecycle()
    val activeMode by viewModel.activeMode.collectAsStateWithLifecycle()
    val confirmInstallMethod by viewModel.confirmInstallMethod.collectAsStateWithLifecycle()
    val resumeInfo by viewModel.resumeInfo.collectAsStateWithLifecycle()
    val fixSourceBusy by viewModel.fixSourceBusy.collectAsStateWithLifecycle()

    val resumeSubtext = resumeInfo?.let { info ->
        if (!info.hasPartial || info.isComplete) {
            null
        } else {
            buildString {
                if (info.fileCount > 0) append("${info.doneFiles} of ${info.fileCount} files downloaded")
                if (info.bytesOnDisk > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(viewModel.formatSize(info.bytesOnDisk))
                }
            }.ifEmpty { null }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(16.dp))

        // App header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = app.iconArtwork?.url,
                contentDescription = app.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(96.dp)
                    .clip(AbsoluteSmoothCornerShape(20.dp, 60))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = app.developerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(16.dp))
            BouncyIconButton(
                onClick = viewModel::openPlayStore,
                modifier = Modifier
                    .size(48.dp)
                    .clip(AbsoluteSmoothCornerShape(14.dp, 60))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Filled.OpenInNew,
                    contentDescription = "View in Play Store",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Install / Update / Open / Uninstall
        InstallAction(
            state = state,
            installState = installState,
            resumeInfo = resumeInfo,
            resumeSubtext = resumeSubtext,
            viewModel = viewModel,
            onInstall = {
                if (confirmInstallMethod) showInstallConfirm = true else viewModel.installOrUpdate()
            },
            onOpen = viewModel::openApp,
            onUninstall = { showUninstallDialog = true },
            onCancel = viewModel::cancelDownload,
            onDismissFailure = viewModel::dismissFailure
        )

        if (state.isInstalled && !state.installedByPlayStore) {
            Spacer(Modifier.height(12.dp))
            InstallSourceWarningSquircleCard(
                source = state.installSource,
                canFix = rootAvailable || shizukuAvailable,
                busy = fixSourceBusy,
                error = viewModel.fixSourceError.collectAsStateWithLifecycle().value,
                onFix = viewModel::fixInstallSource
            )
        }

        Spacer(Modifier.height(20.dp))

        // Meta chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            MetaChip("v${app.versionName}")
            MetaChip(viewModel.formatSize(app.size))
            MetaChip(viewModel.formatDate(app.updatedOn))
        }


        Spacer(Modifier.height(24.dp))

        // Changelog
        if (app.changes.isNotBlank()) {
            SectionTitle("What's new")
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = AbsoluteSmoothCornerShape(16.dp, 60),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = formatRichText(app.changes),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (changelogExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!changelogExpanded) {
                        Spacer(Modifier.height(8.dp))
                        BouncyTextButton(
                            onClick = { changelogExpanded = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Show more")
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // Screenshots
        if (app.screenshots.isNotEmpty()) {
            SectionTitle("Screenshots")
            Spacer(Modifier.height(12.dp))
            val urls = app.screenshots.map { it.url }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(urls.size) { index ->
                    AsyncImage(
                        model = urls[index],
                        contentDescription = "Screenshot ${index + 1}",
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier
                            .height(300.dp)
                            .width(140.dp)
                            .clip(AbsoluteSmoothCornerShape(20.dp, 60))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { fullscreenScreenshot = urls to index }
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // Description
        if (app.description.isNotBlank()) {
            SectionTitle("About this app")
            Spacer(Modifier.height(12.dp))
            Text(
                text = formatRichText(app.description),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (descriptionExpanded) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            if (!descriptionExpanded) {
                Spacer(Modifier.height(4.dp))
                BouncyTextButton(onClick = { descriptionExpanded = true }) {
                    Text("Show more")
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Xia Projekt",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
    }

    if (showUninstallDialog) {
        AlertDialog(
            onDismissRequest = { showUninstallDialog = false },
            shape = AbsoluteSmoothCornerShape(24.dp, 60),
            icon = {
                androidx.compose.material3.Surface(
                    shape = AbsoluteSmoothCornerShape(16.dp, 60),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Uninstall ${app.displayName}?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "This app will be removed from your device. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BouncyOutlinedButton(
                        onClick = { showUninstallDialog = false },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = AbsoluteSmoothCornerShape(14.dp, 60)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                    BouncyButton(
                        onClick = {
                            showUninstallDialog = false
                            viewModel.uninstall()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = AbsoluteSmoothCornerShape(14.dp, 60),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Uninstall", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {}
        )
    }

    if (showInstallConfirm) {
        InstallMethodConfirmDialog(
            rootAvailable = rootAvailable,
            shizukuAvailable = shizukuAvailable,
            activeMode = activeMode,
            onSelectMode = viewModel::selectInstallMode,
            onDontAskAgain = viewModel::setConfirmInstallMethod,
            onInstall = {
                showInstallConfirm = false
                viewModel.installOrUpdate()
            },
            onDismiss = { showInstallConfirm = false }
        )
    }

    fullscreenScreenshot?.let { (urls, index) ->
        FullscreenScreenshotsDialog(
            urls = urls,
            initialIndex = index,
            onDismiss = { fullscreenScreenshot = null }
        )
    }
}

/** Asks which install method to use, with an optional "don't ask again". */
/** Full-screen failure dialog with reason, expandable logs, retry, and copy. */
@Composable
private fun InstallFailedDialog(
    state: InstallUiState.Failed,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    var showLogs by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    val isNetwork = state.isNetworkError
    val themeColor = if (isNetwork) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val onThemeColor = if (isNetwork) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onError
    val containerColor = if (isNetwork) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
    val onContainerColor = if (isNetwork) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer

    AlertDialog(
        onDismissRequest = {},
        shape = AbsoluteSmoothCornerShape(24.dp, 60),
        icon = {
            androidx.compose.material3.Surface(
                shape = AbsoluteSmoothCornerShape(16.dp, 60),
                color = containerColor,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = onContainerColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                if (isNetwork) "No Internet Connection" else "Download Failed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isNetwork) {
                    Text(
                        text = "Check your internet connection and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Don't worry — your download is saved and will continue from where it left off.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Something went wrong during the installation. It's not your fault!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Reason Card
                androidx.compose.material3.Surface(
                    shape = AbsoluteSmoothCornerShape(12.dp, 60),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Error Details",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BouncyTextButton(onClick = { showLogs = !showLogs }) {
                        Text(if (showLogs) "Hide Logs" else "Show Logs")
                    }
                    BouncyTextButton(onClick = {
                        clipboard.setText(AnnotatedString("${state.message}\n\n${state.logs}"))
                    }) {
                        Text("Copy Logs")
                    }
                }
                
                if (showLogs) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.logs,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(AbsoluteSmoothCornerShape(12.dp, 60))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BouncyOutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = AbsoluteSmoothCornerShape(14.dp, 60)
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurface)
                }
                BouncyButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = AbsoluteSmoothCornerShape(14.dp, 60),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = themeColor,
                        contentColor = onThemeColor
                    )
                ) {
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun InstallMethodConfirmDialog(
    rootAvailable: Boolean,
    shizukuAvailable: Boolean,
    activeMode: InstallManager.Mode,
    onSelectMode: (InstallManager.Mode) -> Unit,
    onDontAskAgain: (Boolean) -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    var dontAskAgain by remember { mutableStateOf(false) }
    var pendingMode by remember { mutableStateOf(activeMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AbsoluteSmoothCornerShape(24.dp, 60),
        title = { 
            Text(
                "Installation Method", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                Spacer(Modifier.height(8.dp))
                MethodRadioRow(
                    label = "Root",
                    available = rootAvailable,
                    selected = pendingMode == InstallManager.Mode.ROOT,
                    onClick = {
                        pendingMode = InstallManager.Mode.ROOT
                        onSelectMode(InstallManager.Mode.ROOT)
                    }
                )
                MethodRadioRow(
                    label = "Shizuku",
                    available = shizukuAvailable,
                    selected = pendingMode == InstallManager.Mode.SHIZUKU,
                    onClick = {
                        pendingMode = InstallManager.Mode.SHIZUKU
                        onSelectMode(InstallManager.Mode.SHIZUKU)
                    }
                )
                Spacer(Modifier.height(16.dp))
                androidx.compose.material3.Surface(
                    onClick = { dontAskAgain = !dontAskAgain },
                    shape = AbsoluteSmoothCornerShape(16.dp, 60),
                    color = if (dontAskAgain) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't ask again",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (dontAskAgain) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.material3.Switch(
                            checked = dontAskAgain,
                            onCheckedChange = { dontAskAgain = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            BouncyTextButton(onClick = {
                onDontAskAgain(dontAskAgain)
                onInstall()
            }) {
                Text("Install", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            BouncyTextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun MethodRadioRow(
    label: String,
    available: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        available -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        available -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    
    val statusText = if (available) "Granted" else "Not Granted"
    val statusColor = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = AbsoluteSmoothCornerShape(16.dp, 60),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                androidx.compose.material3.Surface(
                    color = if (selected) contentColor.copy(alpha = 0.1f) else statusColor.copy(alpha = 0.1f),
                    shape = AbsoluteSmoothCornerShape(6.dp, 60)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) contentColor.copy(alpha = 0.9f) else statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = contentColor
                )
            }
        }
    }
}

/** Fullscreen screenshot viewer: swipe left/right, pinch-zoom, tap or X to close. */
@Composable
private fun FullscreenScreenshotsDialog(
    urls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, urls.size - 1),
        pageCount = { urls.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableScreenshot(url = urls[page], onTap = onDismiss)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${urls.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(AbsoluteSmoothCornerShape(20.dp, 60))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            BouncyIconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ZoomableScreenshot(url: String, onTap: () -> Unit) {
    var scale by remember(url) { mutableStateOf(1f) }
    var offset by remember(url) { mutableStateOf(Offset.Zero) }
    val zoomed = scale > 1f
    val currentZoomed by rememberUpdatedState(zoomed)

    val painter = rememberAsyncImagePainter(model = url)
    var imageBox by remember(url) { mutableStateOf(IntSize.Zero) }
    val intrinsic = painter.intrinsicSize
    val fitSize = if (intrinsic.isSpecified && imageBox != IntSize.Zero) {
        val s = min(imageBox.width / intrinsic.width, imageBox.height / intrinsic.height)
        Size(intrinsic.width * s, intrinsic.height * s)
    } else {
        Size(imageBox.width.toFloat(), imageBox.height.toFloat())
    }
    val maxPanX = max(0f, (fitSize.width * scale - imageBox.width) / 2f)
    val maxPanY = max(0f, (fitSize.height * scale - imageBox.height) / 2f)

    fun clampOffset(x: Float, y: Float) =
        Offset(x.coerceIn(-maxPanX, maxPanX), y.coerceIn(-maxPanY, maxPanY))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .onSizeChanged { imageBox = it }
            .pointerInput(url) {
                detectTapGestures(
                    onTap = { if (!currentZoomed) onTap() },
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2.5f
                        offset = Offset.Zero
                    }
                )
            }
    ) {
        Image(
            painter = painter,
            contentDescription = "Screenshot",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(zoomed) {
                    // When zoomed, pan with bounds so the image can never slide
                    // off into blank canvas, and hand horizontal drags at the
                    // edges over to the pager so swiping pages to the next
                    // screenshot instead of consuming them.
                    if (!zoomed) return@pointerInput
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            val zoomChange = event.calculateZoom()
                            var consume = false
                            if (zoomChange != 1f && changes.size >= 2) {
                                val centroid = event.calculateCentroid()
                                val center = Offset(
                                    imageBox.width / 2f,
                                    imageBox.height / 2f
                                )
                                val newScale = (scale * zoomChange).coerceIn(1f, 6f)
                                offset = clampOffset(
                                    centroid.x - center.x -
                                        (centroid.x - center.x - offset.x) * (newScale / scale),
                                    centroid.y - center.y -
                                        (centroid.y - center.y - offset.y) * (newScale / scale)
                                )
                                if (newScale == 1f) offset = Offset.Zero
                                scale = newScale
                                consume = true
                            } else if (changes.size == 1) {
                                val pan = changes.first().positionChange()
                                val newOffset = clampOffset(offset.x + pan.x, offset.y + pan.y)
                                val pinnedAtXBound =
                                    newOffset.x == offset.x && abs(pan.x) > 0f
                                // Pure horizontal drag against the horizontal edge:
                                // let the pager take it so the view pages over.
                                if (pinnedAtXBound && abs(pan.x) >= abs(pan.y)) {
                                    consume = false
                                } else {
                                    consume = newOffset != offset
                                    offset = newOffset
                                }
                            }
                            if (consume) {
                                changes.forEach { if (it.positionChanged()) it.consumePositionChange() }
                            }
                        } while (changes.any { it.pressed })
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

/** Converts Play Store HTML (br tags, entities) into plain, readable text. */
private fun formatRichText(raw: String): String {
    var text = raw
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</p>"), "\n\n")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace("&hellip;", "…")
        .replace("&mdash;", "—")
        .replace("&ndash;", "–")
        .replace("&ldquo;", "\"")
        .replace("&rdquo;", "\"")
        .replace("&amp;", "&")
        .replace(Regex("<[^>]*>"), "")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
    return text
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MetaChip(text: String) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = AbsoluteSmoothCornerShape(8.dp, 60),
        modifier = Modifier.height(32.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InstallSourceWarningSquircleCard(
    source: String?,
    canFix: Boolean,
    busy: Boolean,
    error: String?,
    onFix: () -> Unit
) {
    SquircleCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Not installed from Play Store",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Installed by ${source ?: "an unknown source"}. Some apps check their " +
                    "installer and may show warnings or refuse to run. Fixing reuses the " +
                    "already-downloaded files — no re-download.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            BouncyButton(
                onClick = onFix,
                enabled = canFix && !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = AbsoluteSmoothCornerShape(14.dp, 60)
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Fixing…")
                } else {
                    Text("Fix install source")
                }
            }
            if (!canFix) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Requires root or Shizuku permission to fix.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun InstallAction(
    state: AppUiState.Ready,
    installState: InstallUiState,
    resumeInfo: ResumeInfo?,
    resumeSubtext: String?,
    viewModel: AppViewModel,
    onInstall: () -> Unit,
    onOpen: () -> Unit,
    onUninstall: () -> Unit,
    onCancel: () -> Unit,
    onDismissFailure: () -> Unit
) {
    when (installState) {
        is InstallUiState.Idle -> {
            val canResume = resumeInfo != null && resumeInfo.hasPartial &&
                !resumeInfo.isComplete && !state.isUpToDate
            if (state.isUpToDate) {
                BouncyButton(
                    onClick = onOpen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = AbsoluteSmoothCornerShape(16.dp, 60)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            } else {
                var showDeleteConfirm by remember { mutableStateOf(false) }

                if (canResume) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BouncyOutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = AbsoluteSmoothCornerShape(16.dp, 60),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        BouncyButton(
                            onClick = onInstall,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = AbsoluteSmoothCornerShape(16.dp, 60)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            shape = AbsoluteSmoothCornerShape(24.dp, 60),
                            icon = {
                                androidx.compose.material3.Surface(
                                    shape = AbsoluteSmoothCornerShape(16.dp, 60),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            },
                            title = {
                                Text(
                                    text = "Delete Downloaded Files?",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            },
                            text = {
                                Text(
                                    text = "This will permanently remove all partially downloaded files for this app. You will need to start the download from the beginning.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BouncyOutlinedButton(
                                        onClick = { showDeleteConfirm = false },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = AbsoluteSmoothCornerShape(14.dp, 60)
                                    ) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    BouncyButton(
                                        onClick = {
                                            showDeleteConfirm = false
                                            viewModel.clearDownloads()
                                        },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = AbsoluteSmoothCornerShape(14.dp, 60),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        )
                                    ) {
                                        Text("Delete", fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            dismissButton = {}
                        )
                    }
                } else {
                    BouncyButton(
                        onClick = onInstall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = AbsoluteSmoothCornerShape(16.dp, 60)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (state.isInstalled) "Update" else "Install",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (canResume && resumeSubtext != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = resumeSubtext,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (state.isInstalled) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Installed version ${state.installedVersionName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (state.isInstalled) {
                Spacer(Modifier.height(8.dp))
                BouncyOutlinedButton(
                    onClick = onUninstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = AbsoluteSmoothCornerShape(14.dp, 60),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Uninstall")
                }
            }
        }

        is InstallUiState.Preparing -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MorphingLoadingIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(12.dp))
                Text("Preparing…")
            }
        }

        is InstallUiState.Downloading -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                WavyProgressIndicator(
                    progress = { installState.progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = installState.status ?: "Downloading…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (installState.detail.bytesPerSecond > 0f) {
                            Spacer(Modifier.height(2.dp))
                            val eta = installState.detail.etaSeconds
                                ?.let { viewModel.formatEta(it) }
                            Text(
                                text = if (eta != null) {
                                    "${viewModel.formatSpeed(installState.detail.bytesPerSecond)} · $eta"
                                } else {
                                    viewModel.formatSpeed(installState.detail.bytesPerSecond)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${(installState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    BouncyTextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        }

        is InstallUiState.Installing,
        is InstallUiState.Done -> {
            val finishing = installState is InstallUiState.Done
            val justInstalled = (installState as? InstallUiState.Done)?.justInstalled == true

            // One shared Animatable so the bar and layout persist seamlessly across the
            // Installing → Done transition: it ramps 0→90% while `pm install` runs, then
            // smoothly ramps 90→100% as the install finishes, holds briefly at 100%,
            // and only then swaps to the Open/Uninstall buttons.
            val progress = remember { Animatable(0f) }
            var showButtons by remember { mutableStateOf(false) }

            LaunchedEffect(installState) {
                when {
                    justInstalled -> {
                        progress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                        )
                        delay(250)
                        showButtons = true
                    }
                    finishing -> {
                        progress.snapTo(1f)
                        showButtons = true
                    }
                }
            }
            if (!finishing) {
                LaunchedEffect(Unit) {
                    progress.animateTo(
                        targetValue = 0.9f,
                        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
                    )
                }
            }

            if (showButtons) {
                if (state.isInstalled) {
                    BouncyButton(
                        onClick = onOpen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = AbsoluteSmoothCornerShape(16.dp, 60)
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Installed — Open", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    BouncyOutlinedButton(
                        onClick = onUninstall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = AbsoluteSmoothCornerShape(14.dp, 60),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Uninstall")
                    }
                } else {
                    BouncyButton(
                        onClick = onInstall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = AbsoluteSmoothCornerShape(16.dp, 60)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Install",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    WavyProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Installing…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(progress.value * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        is InstallUiState.Failed -> InstallFailedDialog(
            installState,
            onDismiss = onDismissFailure,
            onRetry = {
                onDismissFailure()
                onInstall()
            }
        )
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
    iconContentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    BouncyCard(
        onClick = onClick,
        shape = AbsoluteSmoothCornerShape(20.dp, 60),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Surface(
                shape = AbsoluteSmoothCornerShape(14.dp, 60),
                color = iconContainerColor,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconContentColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (action != null) {
                Spacer(Modifier.width(12.dp))
                action()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    PreferenceRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        iconContainerColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        iconContentColor = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { onCheckedChange(!checked) },
        action = {
            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
private fun InstallMethodPreference(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    available: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onAction: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    
    val statusText = if (available) "Granted" else "Not Granted"
    val statusColor = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    BouncyCard(
        onClick = onClick,
        shape = AbsoluteSmoothCornerShape(20.dp, 60),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Surface(
                    shape = AbsoluteSmoothCornerShape(14.dp, 60),
                    color = if (selected) contentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
                }
                androidx.compose.material3.Surface(
                    color = if (selected) contentColor.copy(alpha = 0.1f) else statusColor.copy(alpha = 0.1f),
                    shape = AbsoluteSmoothCornerShape(8.dp, 60)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) contentColor.copy(alpha = 0.9f) else statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (!available && onAction != null && actionLabel != null) {
                Spacer(Modifier.height(12.dp))
                BouncyButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = AbsoluteSmoothCornerShape(14.dp, 60),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    viewModel: AppViewModel,
    onOpenCheckUpdates: () -> Unit,
    onNavigateToAddGame: () -> Unit
) {
    val rootAvailable by viewModel.rootAvailable.collectAsStateWithLifecycle()
    val shizukuAvailable by viewModel.shizukuAvailable.collectAsStateWithLifecycle()
    val activeMode by viewModel.activeMode.collectAsStateWithLifecycle()
    val deleteAfterInstall by viewModel.deleteAfterInstall.collectAsStateWithLifecycle()
    var showClearDownloadsDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        fontWeight = FontWeight.Bold, 
                        style = if (scrollBehavior.state.collapsedFraction < 0.5f) MaterialTheme.typography.displayMedium else MaterialTheme.typography.titleLarge
                    ) 
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                modifier = Modifier.hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        blurRadius = 16.dp
                    )
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(padding),
        ) {
            SectionTitle("Game Configuration")
            Spacer(Modifier.height(8.dp))
            PreferenceRow(
                title = "Add Game",
                subtitle = "Search and configure a new game to manage",
                icon = Icons.Filled.Add,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onNavigateToAddGame
            )
            
            Spacer(Modifier.height(24.dp))
            SectionTitle("Installation Method")
            Spacer(Modifier.height(8.dp))
            InstallMethodPreference(
                label = "Root",
                subtitle = "Highest success rate, requires unlocked bootloader.",
                icon = Icons.Filled.Lock,
                available = rootAvailable,
                selected = activeMode == InstallManager.Mode.ROOT,
                onClick = {
                    viewModel.selectInstallMode(InstallManager.Mode.ROOT)
                    viewModel.refreshPrivilegeStatus(forceRootRecheck = true)
                },
                onAction = { viewModel.refreshPrivilegeStatus(forceRootRecheck = true) },
                actionLabel = "Re-check root access"
            )
            InstallMethodPreference(
                label = "Shizuku",
                subtitle = "Secure local ADB installation method.",
                icon = Icons.Filled.Terminal,
                available = shizukuAvailable,
                selected = activeMode == InstallManager.Mode.SHIZUKU,
                onClick = {
                    viewModel.selectInstallMode(InstallManager.Mode.SHIZUKU)
                    viewModel.refreshPrivilegeStatus()
                },
                onAction = viewModel::requestShizukuPermission,
                actionLabel = "Grant Shizuku access"
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle("Updates & Sync")
            Spacer(Modifier.height(8.dp))
            PreferenceRow(
                title = "Check for updates",
                subtitle = "Interval, notifications, auto update",
                icon = Icons.Filled.Refresh,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onOpenCheckUpdates,
                action = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle("Storage Management")
            Spacer(Modifier.height(8.dp))
            SettingsSwitchRow(
                title = "Delete downloaded files",
                subtitle = "Frees ~350 MB after app installation",
                icon = Icons.Filled.Delete,
                checked = deleteAfterInstall,
                onCheckedChange = viewModel::setDeleteAfterInstall
            )
            PreferenceRow(
                title = "Clear cached downloads",
                subtitle = "Deletes all downloaded APKs manually",
                icon = Icons.Filled.Delete,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                iconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = { showClearDownloadsDialog = true }
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle("Battery & Background")
            Spacer(Modifier.height(8.dp))
            BackgroundKillerCard()

            Spacer(Modifier.height(32.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Disclaimer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "ProjectDreams is an independent, open-source downloader. It is not " +
                    "affiliated with, endorsed by, or connected to any game publisher or developer " +
                    "in any way. All games and related intellectual property are owned by their respective rights holders, and " +
                    "this project makes no revenue from it. We do not save, host, or distribute game assets; everything is " +
                    "fetched directly from Google Play servers to your device. All trademarks belong to their " +
                    "respective owners.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Xia Projekt",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
        }

        if (showClearDownloadsDialog) {
            AlertDialog(
                onDismissRequest = { showClearDownloadsDialog = false },
                shape = AbsoluteSmoothCornerShape(24.dp, 60),
                icon = {
                    androidx.compose.material3.Surface(
                        shape = AbsoluteSmoothCornerShape(16.dp, 60),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = { Text("Clear downloaded files?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently remove all downloaded APKs.") },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BouncyOutlinedButton(
                            onClick = { showClearDownloadsDialog = false },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = AbsoluteSmoothCornerShape(14.dp, 60)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                        }
                        BouncyButton(
                            onClick = {
                                showClearDownloadsDialog = false
                                viewModel.clearDownloads()
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = AbsoluteSmoothCornerShape(14.dp, 60),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Clear", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {}
            )
        }
    }
}

/** Configures how (and how often) the app checks Play for updates. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CheckUpdatesScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val updateNotifications by viewModel.updateNotifications.collectAsStateWithLifecycle()
    val autoUpdate by viewModel.autoUpdate.collectAsStateWithLifecycle()
    
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // the state is already updated if they toggle, or we can just ensure they gave permission.
    }
    val updateIntervalHours by viewModel.updateIntervalHours.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Check for updates", 
                        fontWeight = FontWeight.Bold,
                        style = if (scrollBehavior.state.collapsedFraction < 0.5f) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    BouncyIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                modifier = Modifier.hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        blurRadius = 16.dp
                    )
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(padding),
        ) {
            SectionTitle("How often to check")
            Spacer(Modifier.height(8.dp))
            IntervalDropdownPreference(
                currentHours = updateIntervalHours,
                onSelect = { viewModel.setUpdateIntervalHours(it) }
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle("When an update is found")
            Spacer(Modifier.height(8.dp))
            SettingsSwitchRow(
                title = "Update notifications",
                subtitle = "Be notified when a new version is available",
                icon = Icons.Filled.Notifications,
                checked = updateNotifications,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    viewModel.setUpdateNotifications(enabled)
                }
            )
            SettingsSwitchRow(
                title = "Auto update",
                subtitle = "Download and install updates automatically (needs root or Shizuku)",
                icon = Icons.Filled.CloudDownload,
                checked = autoUpdate,
                onCheckedChange = viewModel::setAutoUpdate
            )

            Spacer(Modifier.height(24.dp))
            BouncyButton(
                onClick = viewModel::checkNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = AbsoluteSmoothCornerShape(14.dp, 60)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Check now")
            }

            Spacer(Modifier.height(24.dp))
        }
    }


}

/** "every hour" / "every day" / "every N days" / "every N hours" phrasing for a duration. */
private fun formatEveryHours(hours: Long): String = when (hours) {
    1L -> "every hour"
    else -> {
        val days = hours / 24
        val remainder = hours % 24
        when {
            remainder == 0L && days == 1L -> "every day"
            remainder == 0L -> "every $days days"
            else -> "every $hours hours"
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalDropdownPreference(
    currentHours: Long,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val currentLabel = PRESET_INTERVALS.find { it.first == currentHours }?.second ?: "$currentHours hours"

    PreferenceRow(
        title = "Check Frequency",
        subtitle = "How often the app checks for new updates",
        icon = Icons.Filled.Refresh,
        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
        iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        action = {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Text(currentLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        onClick = { expanded = true }
    )

    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            sheetState = rememberModalBottomSheetState(),
            shape = AbsoluteSmoothCornerShape(32.dp, 60)
        ) {
            androidx.compose.foundation.layout.Column(Modifier.padding(bottom = 24.dp)) {
                PRESET_INTERVALS.forEach { (hours, label) ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = false
                                onSelect(hours)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hours == currentHours) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                        } else {
                            Spacer(Modifier.width(40.dp))
                        }
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (hours == currentHours) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (hours == currentHours) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private val PRESET_INTERVALS = listOf(
    1L to "1 hour",
    3L to "3 hours",
    6L to "6 hours",
    12L to "12 hours",
    24L to "Every day",
    168L to "Every week",
    720L to "Every month"
)

@Composable
private fun FloatingNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = AbsoluteSmoothCornerShape(999.dp, 60),
        modifier = modifier.padding(bottom = 24.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NavBarItem(
                icon = Icons.Filled.Home,
                label = "Home",
                selected = currentScreen == Screen.Main,
                onClick = { onNavigate(Screen.Main) }
            )
            NavBarItem(
                icon = Icons.Filled.Apps,
                label = "Library",
                selected = currentScreen == Screen.GameManager,
                onClick = { onNavigate(Screen.GameManager) }
            )
            NavBarItem(
                icon = Icons.Filled.Settings,
                label = "Settings",
                selected = currentScreen == Screen.Settings || currentScreen == Screen.CheckUpdates,
                onClick = { onNavigate(Screen.Settings) }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    
    Row(
        modifier = Modifier
            .clip(AbsoluteSmoothCornerShape(999.dp, 60))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = contentColor)
        androidx.compose.animation.AnimatedVisibility(visible = selected) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, color = contentColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameDropdown(
    game: Game,
    allGames: List<Game>,
    appIconUrl: String?, 
    gameDetails: Map<Game, com.aurora.gplayapi.data.models.App>, 
    onSelect: (Game) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        BouncyTextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .clip(AbsoluteSmoothCornerShape(8.dp, 60))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (appIconUrl != null) {
                AsyncImage(
                    model = appIconUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(20.dp).clip(AbsoluteSmoothCornerShape(4.dp, 60))
                )
                Spacer(Modifier.width(4.dp))
            }
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        if (expanded) {
            ModalBottomSheet(
                onDismissRequest = { expanded = false },
                sheetState = rememberModalBottomSheetState(),
                shape = AbsoluteSmoothCornerShape(32.dp, 60)
            ) {
                androidx.compose.foundation.layout.Column(Modifier.padding(bottom = 24.dp)) {
                    Text(
                        "Select Game",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                    allGames.forEach { option ->
                        val detail = gameDetails[option]
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    expanded = false
                                }
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            if (detail?.iconArtwork?.url != null) {
                                AsyncImage(
                                    model = detail.iconArtwork!!.url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(40.dp).clip(AbsoluteSmoothCornerShape(10.dp, 60)).background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(Modifier.width(16.dp))
                            }
                            Text(
                                text = detail?.displayName ?: option.fallbackName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (option == game) FontWeight.Bold else FontWeight.Normal,
                                color = if (option == game) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameManagerScreen(viewModel: AppViewModel, onNavigate: (Screen) -> Unit) {
    val gameDetails by viewModel.gameDetails.collectAsStateWithLifecycle()
    val allGames by viewModel.allGames.collectAsStateWithLifecycle()
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    
    var libraryItems by remember { mutableStateOf<List<Triple<com.projectdreams.app.data.Game, Boolean, Boolean>>>(emptyList()) }
    var packageToUninstall by remember { mutableStateOf<String?>(null) }
    var appNameToUninstall by remember { mutableStateOf<String>("") }
    var gameToRemove by remember { mutableStateOf<com.projectdreams.app.data.Game?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var installFilter by remember { mutableStateOf("All") }
    var regionFilter by remember { mutableStateOf("All") }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedGameForDetails by remember { mutableStateOf<Triple<com.projectdreams.app.data.Game, Boolean, Boolean>?>(null) }
    
    // FAB Scroll animation state
    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }
    var isScrollingUp by remember { mutableStateOf(true) }
    
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                isScrollingUp = if (index != previousIndex) {
                    index < previousIndex
                } else {
                    offset <= previousScrollOffset
                }
                previousIndex = index
                previousScrollOffset = offset
            }
    }
    
    LaunchedEffect(allGames, gameDetails, searchQuery, installFilter, regionFilter) {
        var filteredList = allGames.map { game ->
            val glInstalled = game.glPackage.isNotBlank() && InstalledAppInfo.installedVersion(context, game.glPackage) != null
            val jpInstalled = game.jpPackage.isNotBlank() && InstalledAppInfo.installedVersion(context, game.jpPackage) != null
            Triple(game, glInstalled, jpInstalled)
        }.filter { item ->
            val (game, glInstalled, jpInstalled) = item
            
            val matchSearch = if (searchQuery.isBlank()) true else {
                val displayName = gameDetails[game]?.displayName ?: game.fallbackName
                displayName.contains(searchQuery, ignoreCase = true) || 
                game.glPackage.contains(searchQuery, ignoreCase = true) ||
                game.jpPackage.contains(searchQuery, ignoreCase = true)
            }
            
            val matchRegion = when (regionFilter) {
                "GL" -> game.glPackage.isNotBlank()
                "JP" -> game.jpPackage.isNotBlank()
                else -> true
            }
            
            val matchInstall = when (installFilter) {
                "Installed" -> when (regionFilter) {
                    "GL" -> glInstalled
                    "JP" -> jpInstalled
                    else -> glInstalled || jpInstalled
                }
                "Uninstalled" -> when (regionFilter) {
                    "GL" -> !glInstalled
                    "JP" -> !jpInstalled
                    else -> !glInstalled && !jpInstalled
                }
                else -> true
            }
            
            matchSearch && matchInstall && matchRegion
        }
        
        filteredList = filteredList.sortedWith(compareBy({ !(it.second || it.third) }, { gameDetails[it.first]?.displayName ?: it.first.fallbackName }))
        libraryItems = filteredList
    }

    val hazeState = remember { dev.chrisbanes.haze.HazeState() }
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val colorPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "colorPhase"
    )
    val accentColor = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, colorPhase)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            androidx.compose.animation.AnimatedVisibility(
                visible = isScrollingUp || scrollState.firstVisibleItemIndex == 0,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 16.dp, bottom = 80.dp)
                ) {
                    val editRotation by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isEditMode) 360f else 0f, animationSpec = androidx.compose.animation.core.spring(stiffness = 800f, dampingRatio = 0.9f))
                    androidx.compose.material3.FloatingActionButton(
                        onClick = { isEditMode = !isEditMode },
                        containerColor = if (isEditMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isEditMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = AbsoluteSmoothCornerShape(16.dp, 60),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(modifier = Modifier.graphicsLayer { rotationZ = editRotation }) {
                            Icon(if (isEditMode) Icons.Filled.Close else Icons.Filled.Edit, contentDescription = "Edit Mode")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.FloatingActionButton(
                        onClick = { onNavigate(Screen.AddGame) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = AbsoluteSmoothCornerShape(20.dp, 60)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Game")
                    }
                }
            }
        },
        topBar = {
            LargeTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Library", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineLarge) 
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                modifier = Modifier.hazeChild(
                    state = hazeState,
                    style = dev.chrisbanes.haze.HazeStyle(
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), 
                        blurRadius = 16.dp
                    )
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search & Filter Header
                Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search library...") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        singleLine = true,
                        shape = AbsoluteSmoothCornerShape(16.dp, 60),
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Library Filters",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Region Filter Button
                        val regionIcon = when(regionFilter) {
                            "All" -> Icons.Filled.Public
                            "GL" -> Icons.Filled.Language
                            "JP" -> Icons.Filled.Translate
                            else -> Icons.Filled.Public
                        }
                        val regionTooltip = when(regionFilter) {
                            "All" -> "All Regions"
                            "GL" -> "Global Region"
                            "JP" -> "Japan Region"
                            else -> "All Regions"
                        }
                        
                        androidx.compose.material3.FilledTonalIconButton(
                            onClick = { 
                                regionFilter = when(regionFilter) {
                                    "All" -> "GL"
                                    "GL" -> "JP"
                                    else -> "All"
                                }
                            },
                            shape = AbsoluteSmoothCornerShape(
                                androidx.compose.foundation.shape.CornerSize(26.dp),
                                androidx.compose.foundation.shape.CornerSize(8.dp),
                                androidx.compose.foundation.shape.CornerSize(8.dp),
                                androidx.compose.foundation.shape.CornerSize(26.dp),
                                60
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(regionIcon, contentDescription = regionTooltip)
                        }
                        
                        Spacer(Modifier.width(4.dp))
                        
                        // Install Filter Button
                        val installIcon = when(installFilter) {
                            "All" -> Icons.Filled.Dataset
                            "Installed" -> Icons.Filled.PhoneAndroid
                            "Uninstalled" -> Icons.Filled.Cloud
                            else -> Icons.Filled.Dataset
                        }
                        val installTooltip = when(installFilter) {
                            "All" -> "All Storage"
                            "Installed" -> "Installed Apps"
                            "Uninstalled" -> "Not Installed"
                            else -> "All Storage"
                        }
                        
                        androidx.compose.material3.FilledTonalIconButton(
                            onClick = { 
                                installFilter = when(installFilter) {
                                    "All" -> "Installed"
                                    "Installed" -> "Uninstalled"
                                    else -> "All"
                                }
                            },
                            shape = AbsoluteSmoothCornerShape(
                                androidx.compose.foundation.shape.CornerSize(8.dp),
                                androidx.compose.foundation.shape.CornerSize(26.dp),
                                androidx.compose.foundation.shape.CornerSize(26.dp),
                                androidx.compose.foundation.shape.CornerSize(8.dp),
                                60
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(installIcon, contentDescription = installTooltip)
                        }
                    }
                }
            
                if (libraryItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No games found.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        state = scrollState,
                        contentPadding = PaddingValues(bottom = 140.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        items(libraryItems.size) { index ->
                            val item = libraryItems[index]
                            val (game, glInstalled, jpInstalled) = item
                            val isAnyInstalled = glInstalled || jpInstalled
                            val detail = gameDetails[game]
                            val displayName = detail?.displayName ?: game.fallbackName
                            
                            val statusText = if (glInstalled && jpInstalled) {
                                "Installed: Both"
                            } else if (glInstalled) {
                                "Installed: GL"
                            } else if (jpInstalled) {
                                "Installed: JP"
                            } else {
                                "Not Installed"
                            }
                            
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AbsoluteSmoothCornerShape(20.dp, 60))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isAnyInstalled) 0.8f else 0.4f))
                                    .clickable {
                                        if (!isEditMode) {
                                            selectedGameForDetails = item
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (detail?.iconArtwork?.url != null) {
                                    AsyncImage(
                                        model = detail.iconArtwork!!.url,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp).clip(AbsoluteSmoothCornerShape(14.dp, 60)),
                                        alpha = if (isAnyInstalled) 1f else 0.5f
                                    )
                                    Spacer(Modifier.width(16.dp))
                                } else {
                                    androidx.compose.material3.Surface(modifier = Modifier.size(56.dp), shape = AbsoluteSmoothCornerShape(14.dp, 60), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isAnyInstalled) 1f else 0.5f)) {
                                        Icon(Icons.Filled.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(12.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAnyInstalled) 1f else 0.5f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isAnyInstalled) 1f else 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isEditMode,
                                    enter = androidx.compose.animation.expandHorizontally() + androidx.compose.animation.fadeIn(),
                                    exit = androidx.compose.animation.shrinkHorizontally() + androidx.compose.animation.fadeOut()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        if (glInstalled) {
                                            BouncyIconButton(
                                                onClick = {
                                                    packageToUninstall = game.glPackage
                                                    appNameToUninstall = displayName + " (GL)"
                                                },
                                                modifier = Modifier.size(38.dp).clip(AbsoluteSmoothCornerShape(10.dp, 60)).background(MaterialTheme.colorScheme.errorContainer)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Uninstall GL", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        if (jpInstalled) {
                                            BouncyIconButton(
                                                onClick = {
                                                    packageToUninstall = game.jpPackage
                                                    appNameToUninstall = displayName + " (JP)"
                                                },
                                                modifier = Modifier.size(38.dp).clip(AbsoluteSmoothCornerShape(10.dp, 60)).background(MaterialTheme.colorScheme.errorContainer)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Uninstall JP", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        BouncyIconButton(
                                            onClick = { 
                                                viewModel.gameToEdit = game
                                                onNavigate(Screen.AddGame) 
                                            },
                                            modifier = Modifier.size(38.dp).clip(AbsoluteSmoothCornerShape(10.dp, 60)).background(MaterialTheme.colorScheme.secondaryContainer)
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                                        }
                                        BouncyIconButton(
                                            onClick = { gameToRemove = game },
                                            modifier = Modifier.size(38.dp).clip(AbsoluteSmoothCornerShape(10.dp, 60)).background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (gameToRemove != null) {
            AlertDialog(
                onDismissRequest = { gameToRemove = null },
                shape = AbsoluteSmoothCornerShape(24.dp, 60),
                icon = {
                    androidx.compose.material3.Surface(
                        shape = AbsoluteSmoothCornerShape(16.dp, 60),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = { Text("Stop Tracking Game?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                text = { Text("This will remove the game from your library list. It will not uninstall the app from your device.", style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BouncyOutlinedButton(
                            onClick = { gameToRemove = null },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = AbsoluteSmoothCornerShape(14.dp, 60)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                        }
                        BouncyButton(
                            onClick = {
                                val g = gameToRemove
                                gameToRemove = null
                                if (g != null) {
                                    viewModel.removeGame(g)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = AbsoluteSmoothCornerShape(14.dp, 60),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Remove", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {}
            )
        }
        
        if (packageToUninstall != null) {
            AlertDialog(
                onDismissRequest = { packageToUninstall = null },
                shape = AbsoluteSmoothCornerShape(24.dp, 60),
                icon = {
                    androidx.compose.material3.Surface(
                        shape = AbsoluteSmoothCornerShape(16.dp, 60),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Uninstall $appNameToUninstall?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "This app will be removed from your device. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BouncyOutlinedButton(
                            onClick = { packageToUninstall = null },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = AbsoluteSmoothCornerShape(14.dp, 60)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                        }
                        BouncyButton(
                            onClick = {
                                val pkg = packageToUninstall
                                packageToUninstall = null
                                if (pkg != null) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DELETE)
                                    intent.data = android.net.Uri.parse("package:$pkg")
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = AbsoluteSmoothCornerShape(14.dp, 60),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Uninstall", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {}
            )
        }
    }
}


