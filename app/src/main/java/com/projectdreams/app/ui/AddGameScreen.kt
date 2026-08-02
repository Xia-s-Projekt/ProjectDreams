package com.projectdreams.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    var glApp by remember { mutableStateOf<App?>(null) }
    var jpApp by remember { mutableStateOf<App?>(null) }
    var glPackage by remember { mutableStateOf("") }
    var jpPackage by remember { mutableStateOf("") }

    var searchResultsGL by remember { mutableStateOf<List<App>>(emptyList()) }
    var searchResultsJP by remember { mutableStateOf<List<App>>(emptyList()) }

    var selectedRegionForEdit by remember { mutableStateOf<String?>(null) } // "GL" or "JP"
    var showListSelection by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Game", fontWeight = FontWeight.Bold) },
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
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text("Search for a game", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(12.dp))
                                Text("Enter the title or exact package name to automatically fetch both Global and Japan region matches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(32.dp))
                                
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    label = { Text("Game Title or Package Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = AbsoluteSmoothCornerShape(16.dp, 60)
                                )
                                
                                Spacer(Modifier.height(48.dp))
                                BouncyButton(
                                    onClick = {
                                        if (query.isNotBlank()) {
                                            isSearching = true
                                            coroutineScope.launch {
                                                val glDeferred = async { try { viewModel.searchApps(query.trim(), Region.GLOBAL) } catch (e: Exception) { emptyList() } }
                                                val jpDeferred = async { try { viewModel.searchApps(query.trim(), Region.JAPAN) } catch (e: Exception) { emptyList() } }
                                                
                                                searchResultsGL = glDeferred.await()
                                                searchResultsJP = jpDeferred.await()
                                                
                                                glApp = searchResultsGL.firstOrNull()
                                                jpApp = searchResultsJP.firstOrNull()
                                                glPackage = glApp?.packageName ?: ""
                                                jpPackage = jpApp?.packageName ?: ""
                                                
                                                isSearching = false
                                                step = 1
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = query.isNotBlank() && !isSearching,
                                    shape = AbsoluteSmoothCornerShape(20.dp, 60)
                                ) {
                                    if (isSearching) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                    } else {
                                        Text("Search Play Store", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                        
                        1 -> {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                Spacer(Modifier.height(32.dp))
                                Text("Region Setup", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(12.dp))
                                Text("Review the automatic matches below. Tap any region to edit its configuration manually.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(24.dp))
                                
                                // Global Card
                                RegionCard(
                                    regionName = "Global",
                                    app = glApp,
                                    pkg = glPackage,
                                    onClick = { selectedRegionForEdit = "GL" }
                                )
                                Spacer(Modifier.height(16.dp))
                                // Japan Card
                                RegionCard(
                                    regionName = "Japan",
                                    app = jpApp,
                                    pkg = jpPackage,
                                    onClick = { selectedRegionForEdit = "JP" }
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
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = glPackage.isNotBlank() || jpPackage.isNotBlank(),
                                    shape = AbsoluteSmoothCornerShape(20.dp, 60)
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
        
        // --- Dialogs / Modals for Region Editing ---
        
        if (selectedRegionForEdit != null && !showListSelection && !showSearchDialog && !showManualDialog) {
            val isGl = selectedRegionForEdit == "GL"
            val regionName = if (isGl) "Global" else "Japan"
            ModalBottomSheet(
                onDismissRequest = { selectedRegionForEdit = null },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = AbsoluteSmoothCornerShape(
                    topStart = androidx.compose.foundation.shape.CornerSize(28.dp),
                    topEnd = androidx.compose.foundation.shape.CornerSize(28.dp),
                    bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp),
                    bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                    smoothness = 60
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Edit $regionName Region", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    
                    BottomSheetOption(
                        icon = Icons.Filled.List,
                        title = "Select from previous results",
                        subtitle = "Pick from the list of fetched apps",
                        onClick = { showListSelection = true }
                    )
                    Spacer(Modifier.height(12.dp))
                    BottomSheetOption(
                        icon = Icons.Filled.Search,
                        title = "Search Again",
                        subtitle = "Perform a new search for this region",
                        onClick = { showSearchDialog = true }
                    )
                    Spacer(Modifier.height(12.dp))
                    BottomSheetOption(
                        icon = Icons.Filled.Edit,
                        title = "Manual Entry",
                        subtitle = "Type the exact package name",
                        onClick = { showManualDialog = true }
                    )
                    Spacer(Modifier.height(12.dp))
                    BottomSheetOption(
                        icon = Icons.Filled.Delete,
                        title = "Clear Entry",
                        subtitle = "Remove this region from the game configuration",
                        onClick = {
                            if (isGl) { glApp = null; glPackage = "" } else { jpApp = null; jpPackage = "" }
                            selectedRegionForEdit = null
                        },
                        isDestructive = true
                    )
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
        
        if (showListSelection && selectedRegionForEdit != null) {
            val isGl = selectedRegionForEdit == "GL"
            val results = if (isGl) searchResultsGL else searchResultsJP
            AlertDialog(
                onDismissRequest = { showListSelection = false },
                shape = AbsoluteSmoothCornerShape(24.dp, 60),
                title = { Text("Select App", fontWeight = FontWeight.Bold) },
                text = {
                    if (results.isEmpty()) {
                        Text("No results found previously.")
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(results.size) { i ->
                                val app = results[i]
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(AbsoluteSmoothCornerShape(12.dp, 60)).clickable {
                                        if (isGl) { glApp = app; glPackage = app.packageName } else { jpApp = app; jpPackage = app.packageName }
                                        showListSelection = false
                                        selectedRegionForEdit = null
                                    },
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                        if (app.iconArtwork?.url != null) {
                                            AsyncImage(model = app.iconArtwork!!.url, contentDescription = null, modifier = Modifier.size(48.dp).clip(AbsoluteSmoothCornerShape(10.dp, 60)))
                                            Spacer(Modifier.width(12.dp))
                                        }
                                        Column {
                                            Text(app.displayName ?: "", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    BouncyOutlinedButton(onClick = { showListSelection = false }, shape = AbsoluteSmoothCornerShape(12.dp, 60)) { Text("Cancel") }
                }
            )
        }
        
        if (showSearchDialog && selectedRegionForEdit != null) {
            var localQuery by remember { mutableStateOf("") }
            var isLocalSearching by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                shape = AbsoluteSmoothCornerShape(24.dp, 60),
                title = { Text("Search Region", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = localQuery,
                            onValueChange = { localQuery = it },
                            label = { Text("Game Title or Package") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = AbsoluteSmoothCornerShape(16.dp, 60)
                        )
                    }
                },
                confirmButton = {
                    BouncyButton(
                        onClick = {
                            if (localQuery.isNotBlank()) {
                                isLocalSearching = true
                                val isGl = selectedRegionForEdit == "GL"
                                coroutineScope.launch {
                                    try {
                                        val res = viewModel.searchApps(localQuery.trim(), if(isGl) Region.GLOBAL else Region.JAPAN)
                                        if (isGl) { searchResultsGL = res; glApp = res.firstOrNull(); glPackage = glApp?.packageName ?: "" }
                                        else { searchResultsJP = res; jpApp = res.firstOrNull(); jpPackage = jpApp?.packageName ?: "" }
                                    } catch (e: Exception) {}
                                    isLocalSearching = false
                                    showSearchDialog = false
                                    selectedRegionForEdit = null
                                }
                            }
                        },
                        shape = AbsoluteSmoothCornerShape(12.dp, 60),
                        enabled = localQuery.isNotBlank() && !isLocalSearching
                    ) {
                        Text("Search")
                    }
                },
                dismissButton = {
                    BouncyOutlinedButton(onClick = { showSearchDialog = false }, shape = AbsoluteSmoothCornerShape(12.dp, 60)) { Text("Cancel") }
                }
            )
        }
        
        if (showManualDialog && selectedRegionForEdit != null) {
            var localPkg by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showManualDialog = false },
                shape = AbsoluteSmoothCornerShape(24.dp, 60),
                title = { Text("Manual Entry", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter the exact package name. No validation will be performed.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = localPkg,
                            onValueChange = { localPkg = it },
                            label = { Text("Package Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = AbsoluteSmoothCornerShape(16.dp, 60)
                        )
                    }
                },
                confirmButton = {
                    BouncyButton(
                        onClick = {
                            if (localPkg.isNotBlank()) {
                                val isGl = selectedRegionForEdit == "GL"
                                if (isGl) { glApp = null; glPackage = localPkg.trim() } else { jpApp = null; jpPackage = localPkg.trim() }
                                showManualDialog = false
                                selectedRegionForEdit = null
                            }
                        },
                        shape = AbsoluteSmoothCornerShape(12.dp, 60),
                        enabled = localPkg.isNotBlank()
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    BouncyOutlinedButton(onClick = { showManualDialog = false }, shape = AbsoluteSmoothCornerShape(12.dp, 60)) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun RegionCard(regionName: String, app: App?, pkg: String, onClick: () -> Unit) {
    Surface(
        shape = AbsoluteSmoothCornerShape(24.dp, 60),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (pkg.isNotBlank()) 1f else 0.5f),
        modifier = Modifier.fillMaxWidth().clip(AbsoluteSmoothCornerShape(24.dp, 60)).clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(regionName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            if (pkg.isBlank()) {
                Text("Not configured", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (app?.iconArtwork?.url != null) {
                        AsyncImage(model = app.iconArtwork!!.url, contentDescription = null, modifier = Modifier.size(56.dp).clip(AbsoluteSmoothCornerShape(14.dp, 60)))
                        Spacer(Modifier.width(16.dp))
                    } else {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = AbsoluteSmoothCornerShape(14.dp, 60),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Filled.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(16.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                    }
                    Column {
                        Text(app?.displayName ?: "Manual Entry", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(2.dp))
                        Text(pkg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(AbsoluteSmoothCornerShape(16.dp, 60)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = AbsoluteSmoothCornerShape(12.dp, 60),
            color = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
