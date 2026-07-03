package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.ui.viewmodel.WorldCupViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class TabScreen {
    MATCHES, STANDINGS, AI_CHAT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldCupAppScreen(viewModel: WorldCupViewModel) {
    var currentTab by remember { mutableStateOf(TabScreen.MATCHES) }
    val selectedMatch by viewModel.selectedMatch.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 24.sp
                        )
                        Column {
                            Text(
                                text = "World Cup 2026",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Canada • USA • Mexico",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                actions = {
                    if (currentTab == TabScreen.AI_CHAT) {
                        IconButton(
                            onClick = { viewModel.clearChatHistory() },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp, start = 4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            text = "JD",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == TabScreen.MATCHES,
                    onClick = { currentTab = TabScreen.MATCHES },
                    icon = { Icon(Icons.Default.SportsSoccer, contentDescription = "Matches") },
                    label = { Text("Matches") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_matches_tab")
                )
                NavigationBarItem(
                    selected = currentTab == TabScreen.STANDINGS,
                    onClick = { currentTab = TabScreen.STANDINGS },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Standings") },
                    label = { Text("Standings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_standings_tab")
                )
                NavigationBarItem(
                    selected = currentTab == TabScreen.AI_CHAT,
                    onClick = {
                        currentTab = TabScreen.AI_CHAT
                        viewModel.selectChatMatchContext(null)
                    },
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = "AI Coach") },
                    label = { Text("AI Predictor") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_ai_chat_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching with slide animations for fluid visual experience
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut()
                },
                label = "TabTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    TabScreen.MATCHES -> MatchesTabScreen(viewModel)
                    TabScreen.STANDINGS -> StandingsTabScreen()
                    TabScreen.AI_CHAT -> AiChatTabScreen(viewModel)
                }
            }

            // Beautiful slide-up full screen detail panel when a match is clicked
            AnimatedVisibility(
                visible = selectedMatch != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                ) + fadeOut()
            ) {
                selectedMatch?.let { match ->
                    MatchDetailScreen(
                        match = match,
                        onDismiss = { viewModel.selectMatch(null) },
                        onDiscussInChat = {
                            viewModel.selectChatMatchContext(match.id)
                            currentTab = TabScreen.AI_CHAT
                            viewModel.selectMatch(null)
                            // Auto trigger contextual greet
                            viewModel.sendChatMessage("Give me a fast tactical preview and match prediction for this game.")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MatchesTabScreen(viewModel: WorldCupViewModel) {
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    var filterText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "LIVE", "UPCOMING", "FINISHED"

    val filteredMatches = remember(matches, filterText, selectedFilter) {
        matches.filter { match ->
            val matchesFilter = when (selectedFilter) {
                "LIVE" -> match.status == MatchStatus.LIVE
                "UPCOMING" -> match.status == MatchStatus.UPCOMING
                "FINISHED" -> match.status == MatchStatus.FINISHED
                else -> true
            }
            val matchesSearch = match.teamA.contains(filterText, ignoreCase = true) ||
                    match.teamB.contains(filterText, ignoreCase = true) ||
                    match.stage.contains(filterText, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            placeholder = { Text("Search countries or stages...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (filterText.isNotEmpty()) {
                    IconButton(onClick = { filterText = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("match_search_input"),
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "LIVE", "UPCOMING", "FINISHED")
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (filter == "LIVE") {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                            }
                            Text(filter)
                        }
                    },
                    modifier = Modifier.testTag("filter_chip_$filter")
                )
            }
        }

        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "No matches found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try adjusting your search filters or check back later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMatches, key = { it.id }) { match ->
                    MatchCard(match = match, onClick = { viewModel.selectMatch(match.id) })
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: MatchEntity, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("match_card_${match.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (match.status == MatchStatus.LIVE)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (match.status != MatchStatus.LIVE)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        else
            null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (match.status == MatchStatus.LIVE) 4.dp else 1.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row (Status / Date vs Stage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.status == MatchStatus.LIVE) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = alpha))
                            )
                            Text(
                                text = "LIVE • ${match.liveMinute}'",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (match.status == MatchStatus.FINISHED) "FINISHED" else "UPCOMING • ${match.matchTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "${match.stage} ${if (match.groupName.isNotEmpty()) "• ${match.groupName}" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (match.status == MatchStatus.LIVE)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Central Teams and Score representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Team A
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(16.dp))
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(match.teamAFlag, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.teamA,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score or Verses separator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    if (match.status == MatchStatus.UPCOMING) {
                        Text(
                            text = "vs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Text(
                            text = "${match.scoreA} — ${match.scoreB}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                    }
                }

                // Team B
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(16.dp))
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(match.teamBFlag, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.teamB,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Section (AI Insight / Hype Badge / Venue Details)
            if (match.status == MatchStatus.LIVE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Insight",
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AI Prediction: High action! Expected win prob for ${if (match.scoreA > match.scoreB) match.teamA else if (match.scoreB > match.scoreA) match.teamB else match.teamA} is ${(65..85).random()}%.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (match.status == MatchStatus.UPCOMING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = match.venue,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${(88..98).random()}% Hype",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            } else {
                // Finished match footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = match.venue,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Match Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StandingsTabScreen() {
    // Elegant local Standings table
    val groups = listOf(
        GroupData("Group A", listOf(
            TeamStanding("Mexico", "🇲🇽", 3, 2, 1, 0, 6, 2, 7),
            TeamStanding("Ecuador", "🇪🇨", 3, 2, 0, 1, 5, 3, 6),
            TeamStanding("New Zealand", "🇳🇿", 3, 0, 2, 1, 2, 4, 2),
            TeamStanding("Cameroon", "🇨🇲", 3, 0, 1, 2, 1, 5, 1)
        )),
        GroupData("Group B", listOf(
            TeamStanding("Canada", "🇨🇦", 3, 1, 2, 0, 4, 3, 5),
            TeamStanding("Sweden", "🇸🇪", 3, 1, 1, 1, 4, 4, 4),
            TeamStanding("Egypt", "🇪🇬", 3, 1, 1, 1, 3, 3, 4),
            TeamStanding("Honduras", "🇭🇳", 3, 0, 2, 1, 2, 4, 2)
        )),
        GroupData("Group C", listOf(
            TeamStanding("United States", "🇺🇸", 3, 2, 1, 0, 7, 2, 7),
            TeamStanding("Australia", "🇦🇺", 3, 1, 1, 1, 4, 5, 4),
            TeamStanding("Ghana", "🇬🇭", 3, 1, 0, 2, 3, 5, 3),
            TeamStanding("Austria", "🇦🇹", 3, 0, 2, 1, 2, 4, 2)
        )),
        GroupData("Group D", listOf(
            TeamStanding("Argentina", "🇦🇷", 3, 3, 0, 0, 8, 1, 9),
            TeamStanding("Morocco", "🇲🇦", 3, 2, 0, 1, 5, 3, 6),
            TeamStanding("Czechia", "🇨🇿", 3, 0, 1, 2, 2, 6, 1),
            TeamStanding("Nigeria", "🇳🇬", 3, 0, 1, 2, 1, 6, 1)
        ))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(groups) { group ->
            GroupStandingsCard(group)
        }
    }
}

data class GroupData(val name: String, val standings: List<TeamStanding>)
data class TeamStanding(
    val team: String,
    val flag: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val gf: Int,
    val ga: Int,
    val pts: Int
)

@Composable
fun GroupStandingsCard(group: GroupData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group Title
            Text(
                text = group.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEAM",
                    modifier = Modifier.weight(1.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "P",
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "GD",
                    modifier = Modifier.weight(0.6f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "PTS",
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Standings rows
            group.standings.forEachIndexed { index, team ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.8f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (index < 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.width(14.dp)
                        )
                        Text(team.flag, fontSize = 20.sp)
                        Text(
                            text = team.team,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (index < 2) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${team.played}",
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val gd = team.gf - team.ga
                    val gdStr = if (gd > 0) "+$gd" else "$gd"
                    Text(
                        text = gdStr,
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (gd > 0) Color(0xFF2E7D32) else if (gd < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${team.pts}",
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AiChatTabScreen(viewModel: WorldCupViewModel) {
    val messages by viewModel.allChatMessages.collectAsStateWithLifecycle()
    val isAiTyping by viewModel.isAiTyping.collectAsStateWithLifecycle()
    val chatMatchId by viewModel.chatMatchContextId.collectAsStateWithLifecycle()
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()

    val contextMatch = remember(chatMatchId, matches) {
        matches.find { it.id == chatMatchId }
    }

    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Auto-scroll to newest message
    LaunchedEffect(messages.size, isAiTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Match Context Banner
        if (contextMatch != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🤖", fontSize = 18.sp)
                        Text(
                            text = "AI analyzing: ${contextMatch.teamA} vs ${contextMatch.teamB}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    IconButton(
                        onClick = { viewModel.selectChatMatchContext(null) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Context",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Suggestion pills (when history is small)
        if (messages.size <= 1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose a query to analyze:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                val suggestions = listOf(
                    "Predict who will win the World Cup 2026!",
                    "Who are the key players of Germany and Spain?",
                    "Analyze Group C with USA and Argentina's bracket.",
                    "Explain the new 48-team tournament format."
                )
                suggestions.forEach { suggestion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                textInput = suggestion
                                viewModel.sendChatMessage(suggestion)
                                textInput = ""
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Conversation history
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }

            if (isAiTyping) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "AI Analyst is thinking...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Send message input bar
        Surface(
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask the AI Predictor...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    })
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("chat_send_button"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "USER"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = if (isUser) {
                RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            } else {
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    match: MatchEntity,
    onDismiss: () -> Unit,
    onDiscussInChat: () -> Unit
) {
    var detailTab by remember { mutableStateOf("STATS") } // "STATS", "TIMELINE"
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val eventListAdapter = moshi.adapter<List<MatchEvent>>(
        Types.newParameterizedType(List::class.java, MatchEvent::class.java)
    )

    val timelineEvents = remember(match.timelineJson) {
        try {
            eventListAdapter.fromJson(match.timelineJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_detail_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = { Text(text = match.stage, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_detail_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onDiscussInChat,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analyze with AI")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )

            // Dynamic Gradient background banner representing stadium energy
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Match Venue & Status
                    Text(
                        text = if (match.status == MatchStatus.LIVE) "LIVE • ${match.liveMinute}'" else match.matchDate,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (match.status == MatchStatus.LIVE) Color.Red else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Versus flags and score display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Team A
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(match.teamAFlag, fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                match.teamA,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Score Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            if (match.status == MatchStatus.UPCOMING) {
                                Text(
                                    match.matchTime,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    "${match.scoreA} - ${match.scoreB}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        // Team B
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(match.teamBFlag, fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                match.teamB,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = match.venue,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Tab bar for details
            TabRow(
                selectedTabIndex = if (detailTab == "STATS") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = detailTab == "STATS",
                    onClick = { detailTab = "STATS" },
                    text = { Text("STATS & POSSESSION") }
                )
                Tab(
                    selected = detailTab == "TIMELINE",
                    onClick = { detailTab = "TIMELINE" },
                    text = { Text("TIMELINE") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                if (detailTab == "STATS") {
                    if (match.status == MatchStatus.UPCOMING) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No stats available yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Stats and live possession will update dynamically once the game kicks off!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                // Possession Linear dual-colored bar
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${match.possessionA}% Possession", fontWeight = FontWeight.Bold)
                                        Text("${match.possessionB}% Possession", fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = match.possessionA / 100f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Match Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            // Comparison Stats Bars
                            item { ComparisonStatRow("Shots on Target", match.shotsA, match.shotsB) }
                            item { ComparisonStatRow("Fouls Committed", match.foulsA, match.foulsB) }
                            item { ComparisonStatRow("Yellow Cards", match.yellowCardsA, match.yellowCardsB) }
                            item { ComparisonStatRow("Red Cards", match.redCardsA, match.redCardsB) }
                        }
                    }
                } else {
                    // Timeline
                    if (timelineEvents.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Sports, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Waiting for kickoff",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Live match events and timeline highlights will display here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(timelineEvents) { event ->
                                TimelineRow(event = event, teamA = match.teamA, teamB = match.teamB)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonStatRow(label: String, valA: Int, valB: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$valA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            Text(text = "$valB", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
        val total = (valA + valB).coerceAtLeast(1)
        LinearProgressIndicator(
            progress = valA.toFloat() / total,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun TimelineRow(event: MatchEvent, teamA: String, teamB: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minute Box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${event.minute}'",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Event Detail Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = when (event.type) {
                    "GOAL" -> Color(0xFFE8F5E9)
                    "YELLOW_CARD" -> Color(0xFFFFFDE7)
                    "RED_CARD" -> Color(0xFFFFEBEE)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Event Icon representation
                val iconStr = when (event.type) {
                    "GOAL" -> "⚽"
                    "YELLOW_CARD" -> "🟨"
                    "RED_CARD" -> "🟥"
                    "KICKOFF" -> "🏁"
                    "HALFTIME" -> "⏸️"
                    "FULLTIME" -> "🏆"
                    else -> "🏃"
                }
                Text(iconStr, fontSize = 20.sp)

                Column {
                    Text(
                        text = event.player,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (event.type) {
                            "GOAL" -> Color(0xFF2E7D32)
                            "RED_CARD" -> Color(0xFFC62828)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (event.detail.isNotEmpty()) {
                        Text(
                            text = event.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
