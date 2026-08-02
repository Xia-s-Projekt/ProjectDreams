package com.projectdreams.app.ui
import androidx.compose.ui.unit.em

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectdreams.app.data.install.InstallManager
import com.projectdreams.app.ui.theme.AbsoluteSmoothCornerShape
import com.projectdreams.app.ui.theme.BouncyButton
import com.projectdreams.app.ui.theme.bouncyPress
import com.projectdreams.app.ui.theme.BouncyCard
import com.projectdreams.app.ui.theme.BouncySwitch
import com.projectdreams.app.ui.theme.BouncyTextButton
import com.projectdreams.app.ui.theme.AppTypography
import kotlinx.coroutines.launch

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
    var notificationsEnabled by remember { mutableStateOf(false) }
    var disclaimerAccepted by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
    }

    val navigateToPage: (Int) -> Unit = { targetPage ->
        scope.launch {
            val boundedPage = targetPage.coerceIn(0, PAGE_COUNT - 1)
            pagerState.animateScrollToPage(boundedPage)
        }
    }

    BackHandler {
        if (pagerState.currentPage > 0) {
            navigateToPage(pagerState.currentPage - 1)
        }
    }

    val isNextButtonEnabled = when (pagerState.currentPage) {
        1 -> selectedMode != null
        3 -> disclaimerAccepted
        else -> true
    }

    CompositionLocalProvider(LocalOverscrollFactory provides null) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            bottomBar = {
            SetupBottomBar(
                pagerState = pagerState,
                isNextButtonEnabled = isNextButtonEnabled,
                isFinishButtonEnabled = disclaimerAccepted && selectedMode != null,
                onNextClicked = {
                    navigateToPage(pagerState.currentPage + 1)
                },
                onFinishClicked = {
                    val mode = selectedMode ?: return@SetupBottomBar
                    onFinish(mode)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize().statusBarsPadding()
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                val scale = 1f - (0.15f * absOffset)
                val alpha = 1f - (0.5f * absOffset)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
        }
    }
}

@Composable
fun SetupBottomBar(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    onNextClicked: () -> Unit,
    onFinishClicked: () -> Unit,
    isNextButtonEnabled: Boolean,
    isFinishButtonEnabled: Boolean
) {
    val morphAnimationSpec = tween<Float>(durationMillis = 600, easing = FastOutSlowInEasing)
    val rotationAnimationSpec = tween<Float>(durationMillis = 900, easing = FastOutSlowInEasing)

    val targetShapeValues = when (pagerState.currentPage % 3) {
        0 -> listOf(50f, 50f, 50f, 50f)
        1 -> listOf(26f, 26f, 26f, 26f)
        else -> listOf(18f, 50f, 18f, 50f)
    }

    val animatedTopStart by animateFloatAsState(targetShapeValues[0], morphAnimationSpec, label = "TopStart")
    val animatedTopEnd by animateFloatAsState(targetShapeValues[1], morphAnimationSpec, label = "TopEnd")
    val animatedBottomStart by animateFloatAsState(targetShapeValues[2], morphAnimationSpec, label = "BottomStart")
    val animatedBottomEnd by animateFloatAsState(targetShapeValues[3], morphAnimationSpec, label = "BottomEnd")

    val animatedRotation by animateFloatAsState(
        targetValue = pagerState.currentPage * 360f,
        animationSpec = rotationAnimationSpec,
        label = "Rotation"
    )

    val shape = AbsoluteSmoothCornerShape(
        topStart = CornerSize(36.dp),
        topEnd = CornerSize(36.dp),
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp),
        smoothness = 0
    )

    Surface(
        modifier = modifier.shadow(elevation = 8.dp, shape = shape, clip = true),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = shape
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = pagerState.currentPage,
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "StepTextAnimation"
                ) { targetPage ->
                    if (targetPage == 0) {
                        Text(
                            text = "Let's go",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Text(
                            text = "Step $targetPage of ${pagerState.pageCount - 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                val isLastPage = pagerState.currentPage == pagerState.pageCount - 1
                val isPrimaryButtonEnabled = if (isLastPage) isFinishButtonEnabled else isNextButtonEnabled
                val containerColor = if (!isPrimaryButtonEnabled) {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
                val contentColor = if (!isPrimaryButtonEnabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }

                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        if (isPrimaryButtonEnabled) {
                            if (isLastPage) onFinishClicked() else onNextClicked()
                        }
                    },
                    shape = AbsoluteSmoothCornerShape(
                        topStart = CornerSize(animatedTopStart.toInt().dp),
                        topEnd = CornerSize(animatedTopEnd.toInt().dp),
                        bottomEnd = CornerSize(animatedBottomEnd.toInt().dp),
                        bottomStart = CornerSize(animatedBottomStart.toInt().dp),
                        smoothness = 0
                    ),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    containerColor = containerColor,
                    contentColor = contentColor,
                    modifier = Modifier.rotate(animatedRotation)
                ) {
                    AnimatedContent(
                        modifier = Modifier.rotate(-animatedRotation),
                        targetState = pagerState.currentPage < pagerState.pageCount - 1,
                        transitionSpec = {
                            ContentTransform(
                                targetContentEnter = fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.9f, animationSpec = tween(220, delayMillis = 90)),
                                initialContentExit = fadeOut(animationSpec = tween(90)) + scaleOut(targetScale = 0.9f, animationSpec = tween(90))
                            ).using(SizeTransform(clip = false))
                        },
                        label = "AnimatedFabIcon"
                    ) { isNextPage ->
                        if (isNextPage) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next")
                        } else {
                            if (isFinishButtonEnabled) {
                                Icon(Icons.Rounded.Check, contentDescription = "Finish")
                            } else {
                                Icon(Icons.Rounded.Close, contentDescription = "Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Welcome to",
                style = AppTypography.displayLarge.copy(
                    fontSize = 42.sp,
                    lineHeight = 1.1.em
                )
            )
            Text(
                text = "Project Dreams",
                style = AppTypography.displayLarge.copy(
                    fontSize = 46.sp,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 1.1.em
                )
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(AbsoluteSmoothCornerShape(20.dp, 60))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { translationY = floatOffset },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Everyone's favorite rhythm game.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
    }
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
    Spacer(Modifier.height(32.dp))
    Text(
        text = "Installation Method",
        style = AppTypography.displayMedium.copy(fontSize = 32.sp),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Choose how apps get installed. You can change this later in Settings.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))

    SetupMethodCard(
        title = "Root access",
        subtitle = "Installs via a root shell. Recommended on rooted devices.",
        icon = Icons.Filled.Security,
        available = rootAvailable,
        selected = selectedMode == InstallManager.Mode.ROOT,
        onClick = { onSelectMode(InstallManager.Mode.ROOT) },
        trailing = {
            if (selectedMode == InstallManager.Mode.ROOT && !rootAvailable) {
                BouncyTextButton(onClick = onRecheckRoot) {
                    Text("Re-check")
                }
            }
        }
    )
    Spacer(Modifier.height(16.dp))

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
        onClick = {
            onSelectMode(InstallManager.Mode.SHIZUKU)
            if (!shizukuAvailable) {
                onRequestShizuku()
            }
        },
        trailing = {
            if (selectedMode == InstallManager.Mode.SHIZUKU && !shizukuAvailable) {
                BouncyTextButton(onClick = onRequestShizuku) {
                    Text("Grant")
                }
            }
        }
    )

    androidx.compose.animation.AnimatedVisibility(
        visible = selectedMode == InstallManager.Mode.ROOT && !rootAvailable,
        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
    ) {
        Column {
            Spacer(Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = AbsoluteSmoothCornerShape(16.dp, 60),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = "Root is not granted to this app yet. Install a root manager and grant access, or pick Shizuku instead.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundPage(
    notificationsEnabled: Boolean,
    deleteAfterInstall: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onDeleteAfterInstallChanged: (Boolean) -> Unit
) {
    Text(
        text = "Behavior Options",
        style = AppTypography.displayMedium.copy(fontSize = 32.sp),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Tune how ProjectDreams behaves in the background.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))

    BouncyCard(
        onClick = { onNotificationsChanged(!notificationsEnabled) },
        shape = AbsoluteSmoothCornerShape(16.dp, 60),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = AbsoluteSmoothCornerShape(16.dp, 60),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "New version alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ProjectDreams checks the Play Store regularly for updates",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.size(14.dp))
            BouncySwitch(checked = notificationsEnabled, onCheckedChange = onNotificationsChanged)
        }
    }

    Spacer(Modifier.height(12.dp))

    BouncyCard(
        onClick = { onDeleteAfterInstallChanged(!deleteAfterInstall) },
        shape = AbsoluteSmoothCornerShape(16.dp, 60),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = AbsoluteSmoothCornerShape(16.dp, 60),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Delete after install",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Frees storage space by removing downloaded APKs after installation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.size(14.dp))
            BouncySwitch(checked = deleteAfterInstall, onCheckedChange = onDeleteAfterInstallChanged)
        }
    }

    Spacer(Modifier.height(12.dp))
    BackgroundKillerCard()
}

@Composable
private fun DisclaimerPage(accepted: Boolean, onAccepted: (Boolean) -> Unit) {
    Spacer(Modifier.height(32.dp))
    Text(
        text = "One last thing",
        style = AppTypography.displayMedium.copy(fontSize = 32.sp),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
    BouncyCard(
        onClick = {},
        shape = AbsoluteSmoothCornerShape(16.dp, 60),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Important Notice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "ProjectDreams operates as an independent, community-driven client.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "• No Affiliation: We are not endorsed by, affiliated with, or connected to COVER Corp or QualiArts in any capacity.\n" +
                     "• Copyrights: hololive Dreams and all related intellectual property are entirely owned by COVER Corp and QualiArts.\n" +
                     "• Delivery: This software generates no revenue. It acts solely as a direct bridge to the official Google Play servers, hosting no proprietary assets natively.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BouncySwitch(
                    checked = accepted,
                    onCheckedChange = onAccepted
                )
                Spacer(Modifier.size(16.dp))
                Text(
                    text = "I understand and agree",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SetupMethodCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    available: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    BouncyCard(
        onClick = onClick,
        shape = AbsoluteSmoothCornerShape(16.dp, 60),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing()
        }
    }
}
