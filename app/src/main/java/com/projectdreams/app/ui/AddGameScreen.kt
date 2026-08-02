package com.projectdreams.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aurora.gplayapi.data.models.App
import com.projectdreams.app.data.Game
import com.projectdreams.app.ui.theme.BouncyButton
import com.projectdreams.app.ui.theme.BouncyIconButton
import com.projectdreams.app.ui.theme.BouncyOutlinedButton
import com.projectdreams.app.ui.theme.AbsoluteSmoothCornerShape
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
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
    
    var glApp by remember { mutableStateOf<App?>(null) }
    var jpApp by remember { mutableStateOf<App?>(null) }
    
    var glPackage by remember { mutableStateOf("") }
    var jpPackage by remember { mutableStateOf("") }
    
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<App>>(emptyList()) }
    var isPackageMode by remember { mutableStateOf(false) }

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
                        0, 6, 7 -> { // Search Screens
                            val regionLabel = when (currentStep) {
                                6 -> "Japan"
                                7 -> "Global"
                                else -> "Game"
                            }
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
                                Text("Search $regionLabel Engine", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(12.dp))
                                Text("Find the perfect match for the $regionLabel region automatically.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(32.dp))
                                
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    label = { Text(if (isPackageMode) "Exact Package Name (e.g. jp.co.craftegg.band)" else "$regionLabel Game Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = AbsoluteSmoothCornerShape(16.dp, 60)
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clip(AbsoluteSmoothCornerShape(12.dp, 60)).clickable { isPackageMode = !isPackageMode }.padding(8.dp)
                                ) {
                                    Switch(checked = isPackageMode, onCheckedChange = { isPackageMode = it })
                                    Spacer(Modifier.width(16.dp))
                                    Text("Search by exact Package Name", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                                
                                Spacer(Modifier.height(48.dp))
                                BouncyButton(
                                    onClick = {
                                        if (query.isNotBlank()) {
                                            isSearching = true
                                            step = when(currentStep) { 6 -> 3; 7 -> 1; else -> 1 }
                                            coroutineScope.launch {
                                                searchResults = try {
                                                    if (isPackageMode) listOf(viewModel.getApp(query.trim()))
                                                    else viewModel.searchApps(query.trim(), if (currentStep == 6) com.projectdreams.app.data.Region.JAPAN else com.projectdreams.app.data.Region.GLOBAL)
                                                } catch (e: Exception) { emptyList() }
                                                isSearching = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    enabled = query.isNotBlank(),
                                    shape = AbsoluteSmoothCornerShape(20.dp, 60)
                                ) {
                                    Text("Search Play Store", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                        
                        1, 3 -> { // Confirm Screens
                            val isGlobal = currentStep == 1
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                Spacer(Modifier.height(32.dp))
                                Text("${if (isGlobal) "Global" else "Japan"} Region Setup", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(24.dp))
                                
                                if (isSearching) {
                                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else if (searchResults.isEmpty()) {
                                    Surface(
                                        shape = AbsoluteSmoothCornerShape(24.dp, 60),
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("No results found.", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(16.dp))
                                            BouncyOutlinedButton(onClick = { step = if (isGlobal) 0 else 6 }, shape = AbsoluteSmoothCornerShape(12.dp, 60)) {
                                                Text("Try Again", color = MaterialTheme.colorScheme.onErrorContainer)
                                            }
                                        }
                                    }
                                } else {
                                    val topApp = searchResults.first()
                                    Text("Is this the correct ${if (isGlobal) "Global" else "Japan"} app?", fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(16.dp))
                                    
                                    Surface(
                                        shape = AbsoluteSmoothCornerShape(24.dp, 60),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                                            if (topApp.iconArtwork?.url != null) {
                                                AsyncImage(model = topApp.iconArtwork!!.url, contentDescription = null, modifier = Modifier.size(64.dp).clip(AbsoluteSmoothCornerShape(16.dp, 60)))
                                                Spacer(Modifier.width(16.dp))
                                            }
                                            Column {
                                                Text(topApp.displayName ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                                Text(topApp.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                            }
                                        }
                                    }
                                    
                                    Spacer(Modifier.height(32.dp))
                                    BouncyButton(
                                        onClick = {
                                            if (isGlobal) {
                                                glApp = topApp; glPackage = topApp.packageName; step = 3; isSearching = true
                                                coroutineScope.launch { searchResults = try { viewModel.searchApps(query.trim(), com.projectdreams.app.data.Region.JAPAN) } catch (e: Exception) { emptyList() }; isSearching = false }
                                            } else {
                                                jpApp = topApp; jpPackage = topApp.packageName; step = 5
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = AbsoluteSmoothCornerShape(20.dp, 60)
                                    ) { Text("Yes, ${if (isGlobal) "continue" else "finish"}", fontWeight = FontWeight.Bold) }
                                    
                                    Spacer(Modifier.height(12.dp))
                                    BouncyOutlinedButton(onClick = { step = if (isGlobal) 2 else 4 }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = AbsoluteSmoothCornerShape(20.dp, 60)) {
                                        Text("No, let me select from list")
                                    }
                                    
                                    Spacer(Modifier.height(12.dp))
                                    TextButton(
                                        onClick = {
                                            if (isGlobal) {
                                                glPackage = ""; step = 3; isSearching = true
                                                coroutineScope.launch { searchResults = try { viewModel.searchApps(query.trim(), com.projectdreams.app.data.Region.JAPAN) } catch (e: Exception) { emptyList() }; isSearching = false }
                                            } else {
                                                jpPackage = ""; step = 5
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp)
                                    ) { Text("Remove ${if (isGlobal) "Global" else "Japan"} Region", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                        
                        2, 4 -> { // Pick from list Screens
                            val isGlobal = currentStep == 2
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(Modifier.height(32.dp))
                                Text("Select ${if (isGlobal) "Global" else "Japan"} App", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(16.dp))
                                
                                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                                    items(searchResults.size) { i ->
                                        val app = searchResults[i]
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(AbsoluteSmoothCornerShape(16.dp, 60)).clickable {
                                                if (isGlobal) {
                                                    glApp = app; glPackage = app.packageName; step = 3; isSearching = true
                                                    coroutineScope.launch { searchResults = try { viewModel.searchApps(query.trim(), com.projectdreams.app.data.Region.JAPAN) } catch (e: Exception) { emptyList() }; isSearching = false }
                                                } else {
                                                    jpApp = app; jpPackage = app.packageName; step = 5
                                                }
                                            },
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = AbsoluteSmoothCornerShape(16.dp, 60)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                                                if (app.iconArtwork?.url != null) {
                                                    AsyncImage(model = app.iconArtwork!!.url, contentDescription = null, modifier = Modifier.size(56.dp).clip(AbsoluteSmoothCornerShape(12.dp, 60)))
                                                    Spacer(Modifier.width(16.dp))
                                                }
                                                Column {
                                                    Text(app.displayName ?: "", fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                                                    Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(Modifier.height(16.dp))
                                        TextButton(onClick = { query = ""; step = if (isGlobal) 7 else 6 }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                                            Text("Can't find your game? Search manually", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        
                        5 -> { // Save step
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                Spacer(Modifier.height(32.dp))
                                Text("Review Configuration", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(24.dp))
                                
                                val fallback = glApp?.displayName ?: jpApp?.displayName ?: query
                                val gameId = glPackage.substringAfterLast(".").takeIf { it.isNotBlank() } ?: jpPackage.substringAfterLast(".")
                                
                                Surface(
                                    shape = AbsoluteSmoothCornerShape(24.dp, 60),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text("Game Details", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(16.dp))
                                        Text("Title", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(fallback, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(12.dp))
                                        Text("Game ID", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(gameId, fontWeight = FontWeight.Medium)
                                        
                                        Spacer(Modifier.height(24.dp))
                                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                        Spacer(Modifier.height(24.dp))
                                        
                                        Text("Global Package", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(if (glPackage.isNotBlank()) glPackage else "Not configured", fontWeight = FontWeight.Medium, color = if (glPackage.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.height(12.dp))
                                        Text("Japan Package", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(if (jpPackage.isNotBlank()) jpPackage else "Not configured", fontWeight = FontWeight.Medium, color = if (jpPackage.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                                    }
                                }
                                
                                Spacer(Modifier.height(48.dp))
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
                            }
                        }
                    }
                }
            }
        }
    }
}
