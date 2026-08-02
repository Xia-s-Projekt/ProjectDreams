package com.projectdreams.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aurora.gplayapi.data.models.App
import com.projectdreams.app.data.Game
import com.projectdreams.app.data.Region
import com.projectdreams.app.ui.theme.BouncyButton
import com.projectdreams.app.ui.theme.BouncyIconButton
import com.projectdreams.app.ui.theme.BouncyOutlinedButton
import com.projectdreams.app.ui.theme.AbsoluteSmoothCornerShape
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun MorphingLoadingIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val infiniteTransition = rememberInfiniteTransition(label = "morph")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ), label = "rot"
    )
    val corner by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "corner"
    )
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotation }
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(corner.toInt()))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .graphicsLayer { rotationZ = -rotation * 2 }
                .clip(androidx.compose.foundation.shape.RoundedCornerShape((60 - corner).toInt()))
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }

    var step by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    
    var searchGlobal by remember { mutableStateOf(true) }
    var searchJapan by remember { mutableStateOf(true) }

    var glApp by remember { mutableStateOf<App?>(null) }
    var jpApp by remember { mutableStateOf<App?>(null) }
    var glPackage by remember { mutableStateOf("") }
    var jpPackage by remember { mutableStateOf("") }

    var searchResultsGL by remember { mutableStateOf<List<App>>(emptyList()) }
    var searchResultsJP by remember { mutableStateOf<List<App>>(emptyList()) }

    var selectedRegionForEdit by remember { mutableStateOf<String?>(null) } // "GL" or "JP"
    var sheetMode by remember { mutableStateOf("menu") } // "menu", "list", "search", "manual"

    // Consume gameToEdit on start
    LaunchedEffect(Unit) {
        val edit = viewModel.gameToEdit
        if (edit != null) {
            glPackage = edit.glPackage
            jpPackage = edit.jpPackage
            query = edit.fallbackName
            step = 1
            viewModel.gameToEdit = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BouncyIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                modifier = Modifier.hazeChild(state = hazeState, style = dev.chrisbanes.haze.HazeStyle(blurRadius = 15.dp, tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)))
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().haze(state = hazeState).padding(padding)) {
            AnimatedContent(
                targetState = step,
                label = "AddGameWizard",
                transitionSpec = {
                    val direction = if (targetState > initialState) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right
                    slideIntoContainer(direction, spring(stiffness = 800f, dampingRatio = 0.9f)) togetherWith slideOutOfContainer(direction, spring(stiffness = 800f, dampingRatio = 0.9f))
                }
            ) { currentStep ->
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    when (currentStep) {
                        0 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(Modifier.height(32.dp))
                                Surface(
                                    shape = AbsoluteSmoothCornerShape(32.dp, 60),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text("Search for a game", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(12.dp))
                                Text("Enter the title or exact package name to automatically fetch region matches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(32.dp))
                                
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it; searchError = null },
                                    label = { Text("Game Title or Package Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = AbsoluteSmoothCornerShape(20.dp, 60),
                                    isError = searchError != null,
                                    supportingText = { if (searchError != null) Text(searchError!!) }
                                )
                                
                                Spacer(Modifier.height(24.dp))
                                
                                // Gap filling toggles
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(AbsoluteSmoothCornerShape(16.dp, 60)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Global Region", fontWeight = FontWeight.Bold)
                                        Text("Fetch metadata for Global", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(checked = searchGlobal, onCheckedChange = { searchGlobal = it })
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(AbsoluteSmoothCornerShape(16.dp, 60)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Japan Region", fontWeight = FontWeight.Bold)
                                        Text("Fetch metadata for Japan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(checked = searchJapan, onCheckedChange = { searchJapan = it })
                                }
                                
                                Spacer(Modifier.height(48.dp))
                                BouncyButton(
                                    onClick = {
                                        if (query.isNotBlank() && (searchGlobal || searchJapan)) {
                                            isSearching = true
                                            searchError = null
                                            coroutineScope.launch {
                                                try {
                                                    val glDeferred = if (searchGlobal) async { viewModel.searchApps(query.trim(), Region.GLOBAL) } else null
                                                    val jpDeferred = if (searchJapan) async { viewModel.searchApps(query.trim(), Region.JAPAN) } else null
                                                    
                                                    searchResultsGL = glDeferred?.await() ?: emptyList()
                                                    searchResultsJP = jpDeferred?.await() ?: emptyList()
                                                    
                                                    glApp = searchResultsGL.firstOrNull()
                                                    jpApp = searchResultsJP.firstOrNull()
                                                    if (searchGlobal) glPackage = glApp?.packageName ?: ""
                                                    if (searchJapan) jpPackage = jpApp?.packageName ?: ""
                                                    
                                                    isSearching = false
                                                    step = 1
                                                } catch (e: Exception) {
                                                    isSearching = false
                                                    if (e is java.net.UnknownHostException || e.message?.contains("Unable to resolve host") == true) {
                                                        searchError = "Failed to search: No internet connection."
                                                    } else {
                                                        searchError = "Search failed: ${e.message}"
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    enabled = query.isNotBlank() && !isSearching && (searchGlobal || searchJapan),
                                    shape = AbsoluteSmoothCornerShape(24.dp, 60)
                                ) {
                                    AnimatedContent(targetState = isSearching, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "") { searching ->
                                        if (searching) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                MorphingLoadingIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                                Spacer(Modifier.width(16.dp))
                                                Text("Searching...", fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Text("Search Play Store", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(64.dp))
                            }
                        }
                        
                        1 -> {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                Spacer(Modifier.height(32.dp))
                                Text("Region Setup", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(12.dp))
                                Text("Review the matches below. Tap any region to edit its configuration manually or select from alternatives.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(32.dp))
                                
                                // Global Card
                                RegionCard(
                                    regionName = "Global",
                                    app = glApp,
                                    pkg = glPackage,
                                    onClick = { selectedRegionForEdit = "GL"; sheetMode = "menu" }
                                )
                                Spacer(Modifier.height(16.dp))
                                // Japan Card
                                RegionCard(
                                    regionName = "Japan",
                                    app = jpApp,
                                    pkg = jpPackage,
                                    onClick = { selectedRegionForEdit = "JP"; sheetMode = "menu" }
                                )
                                
                                Spacer(Modifier.height(48.dp))
                                val fallback = glApp?.displayName ?: jpApp?.displayName ?: query
                                val gameId = glPackage.substringAfterLast(".").takeIf { it.isNotBlank() } ?: jpPackage.substringAfterLast(".")
                                
                                BouncyButton(
                                    onClick = {
                                        viewModel.addGameConfig(Game(gameId, glPackage, jpPackage, fallback))
                                        viewModel.loadAllGames()
                                        onBack()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    enabled = glPackage.isNotBlank() || jpPackage.isNotBlank(),
                                    shape = AbsoluteSmoothCornerShape(24.dp, 60)
                                ) {
                                    Text("Save Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(Modifier.height(64.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // --- Bottom Sheet for Region Editing ---
        if (selectedRegionForEdit != null) {
            val isGl = selectedRegionForEdit == "GL"
            val regionName = if (isGl) "Global" else "Japan"
            ModalBottomSheet(
                onDismissRequest = { selectedRegionForEdit = null },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = AbsoluteSmoothCornerShape(
                    topStart = androidx.compose.foundation.shape.CornerSize(32.dp),
                    topEnd = androidx.compose.foundation.shape.CornerSize(32.dp),
                    bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp),
                    bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                    smoothness = 60
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    AnimatedContent(
                        targetState = sheetMode,
                        transitionSpec = {
                            val dir = if (targetState == "menu") AnimatedContentTransitionScope.SlideDirection.Right else AnimatedContentTransitionScope.SlideDirection.Left
                            slideIntoContainer(dir, spring(stiffness = 800f, dampingRatio = 0.9f)) togetherWith slideOutOfContainer(dir, spring(stiffness = 800f, dampingRatio = 0.9f))
                        }, label = ""
                    ) { mode ->
                        when (mode) {
                            "menu" -> {
                                Column {
                                    Text("Edit $regionName Region", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(24.dp))
                                    
                                    BottomSheetOption(
                                        icon = Icons.Filled.List,
                                        title = "Select from previous results",
                                        subtitle = "Pick from the list of fetched apps",
                                        onClick = { sheetMode = "list" }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    BottomSheetOption(
                                        icon = Icons.Filled.Search,
                                        title = "Search Again",
                                        subtitle = "Perform a new search for this region",
                                        onClick = { sheetMode = "search" }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    BottomSheetOption(
                                        icon = Icons.Filled.Edit,
                                        title = "Manual Entry",
                                        subtitle = "Type the exact package name",
                                        onClick = { sheetMode = "manual" }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    BottomSheetOption(
                                        icon = Icons.Filled.Delete,
                                        title = "Clear Entry",
                                        subtitle = "Remove this region from the configuration",
                                        onClick = {
                                            if (isGl) { glApp = null; glPackage = "" } else { jpApp = null; jpPackage = "" }
                                            selectedRegionForEdit = null
                                        },
                                        isDestructive = true
                                    )
                                    Spacer(Modifier.height(48.dp))
                                }
                            }
                            "list" -> {
                                val results = if (isGl) searchResultsGL else searchResultsJP
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        BouncyIconButton(onClick = { sheetMode = "menu" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                                        Spacer(Modifier.width(8.dp))
                                        Text("Select App", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    if (results.isEmpty()) {
                                        Text("No results found previously.", modifier = Modifier.padding(16.dp))
                                    } else {
                                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                                            items(results.size) { i ->
                                                val app = results[i]
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(AbsoluteSmoothCornerShape(16.dp, 60)).clickable {
                                                        if (isGl) { glApp = app; glPackage = app.packageName } else { jpApp = app; jpPackage = app.packageName }
                                                        selectedRegionForEdit = null
                                                    },
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = AbsoluteSmoothCornerShape(16.dp, 60)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                                                        if (app.iconArtwork?.url != null) {
                                                            AsyncImage(model = app.iconArtwork!!.url, contentDescription = null, modifier = Modifier.size(56.dp).clip(AbsoluteSmoothCornerShape(14.dp, 60)))
                                                            Spacer(Modifier.width(16.dp))
                                                        }
                                                        Column {
                                                            Text(app.displayName ?: "", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                                                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }
                                                }
                                            }
                                            item { Spacer(Modifier.height(48.dp)) }
                                        }
                                    }
                                }
                            }
                            "search" -> {
                                var localQuery by remember { mutableStateOf("") }
                                var isLocalSearching by remember { mutableStateOf(false) }
                                var localError by remember { mutableStateOf<String?>(null) }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        BouncyIconButton(onClick = { sheetMode = "menu" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                                        Spacer(Modifier.width(8.dp))
                                        Text("Search $regionName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = localQuery,
                                        onValueChange = { localQuery = it; localError = null },
                                        label = { Text("Game Title or Package") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = AbsoluteSmoothCornerShape(20.dp, 60),
                                        isError = localError != null,
                                        supportingText = { if (localError != null) Text(localError!!) }
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    BouncyButton(
                                        onClick = {
                                            if (localQuery.isNotBlank()) {
                                                isLocalSearching = true
                                                localError = null
                                                coroutineScope.launch {
                                                    try {
                                                        val res = viewModel.searchApps(localQuery.trim(), if(isGl) Region.GLOBAL else Region.JAPAN)
                                                        if (isGl) { searchResultsGL = res; glApp = res.firstOrNull(); glPackage = glApp?.packageName ?: "" }
                                                        else { searchResultsJP = res; jpApp = res.firstOrNull(); jpPackage = jpApp?.packageName ?: "" }
                                                        selectedRegionForEdit = null
                                                    } catch (e: Exception) {
                                                        if (e is java.net.UnknownHostException || e.message?.contains("Unable to resolve host") == true) {
                                                            localError = "Failed to search: No internet connection."
                                                        } else {
                                                            localError = "Error: ${e.message}"
                                                        }
                                                    }
                                                    isLocalSearching = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(64.dp),
                                        shape = AbsoluteSmoothCornerShape(24.dp, 60),
                                        enabled = localQuery.isNotBlank() && !isLocalSearching
                                    ) {
                                        AnimatedContent(targetState = isLocalSearching, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "") { searching ->
                                            if (searching) MorphingLoadingIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                            else Text("Search", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.height(48.dp))
                                }
                            }
                            "manual" -> {
                                var localPkg by remember { mutableStateOf("") }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        BouncyIconButton(onClick = { sheetMode = "menu" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                                        Spacer(Modifier.width(8.dp))
                                        Text("Manual Entry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text("Enter the exact package name. No validation will be performed.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = localPkg,
                                        onValueChange = { localPkg = it },
                                        label = { Text("Package Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = AbsoluteSmoothCornerShape(20.dp, 60)
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    BouncyButton(
                                        onClick = {
                                            if (localPkg.isNotBlank()) {
                                                if (isGl) { glApp = null; glPackage = localPkg.trim() } else { jpApp = null; jpPackage = localPkg.trim() }
                                                selectedRegionForEdit = null
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(64.dp),
                                        shape = AbsoluteSmoothCornerShape(24.dp, 60),
                                        enabled = localPkg.isNotBlank()
                                    ) {
                                        Text("Apply", fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(48.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegionCard(regionName: String, app: App?, pkg: String, onClick: () -> Unit) {
    Surface(
        shape = AbsoluteSmoothCornerShape(32.dp, 60),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (pkg.isNotBlank()) 1f else 0.5f),
        modifier = Modifier.fillMaxWidth().clip(AbsoluteSmoothCornerShape(32.dp, 60)).clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(regionName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(20.dp))
            if (pkg.isBlank()) {
                Text("Not configured", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (app?.iconArtwork?.url != null) {
                        AsyncImage(model = app.iconArtwork!!.url, contentDescription = null, modifier = Modifier.size(64.dp).clip(AbsoluteSmoothCornerShape(18.dp, 60)))
                        Spacer(Modifier.width(20.dp))
                    } else {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = AbsoluteSmoothCornerShape(18.dp, 60),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Filled.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(16.dp))
                        }
                        Spacer(Modifier.width(20.dp))
                    }
                    Column {
                        Text(app?.displayName ?: "Manual Entry", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(pkg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(AbsoluteSmoothCornerShape(20.dp, 60)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = AbsoluteSmoothCornerShape(16.dp, 60),
            color = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.width(20.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
