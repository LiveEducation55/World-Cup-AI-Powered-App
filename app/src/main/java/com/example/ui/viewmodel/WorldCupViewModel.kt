package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.*
import com.example.data.repository.WorldCupRepository
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class WorldCupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorldCupRepository
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val eventListAdapter = moshi.adapter<List<MatchEvent>>(
        Types.newParameterizedType(List::class.java, MatchEvent::class.java)
    )

    // UI State Holders
    val allMatches: StateFlow<List<MatchEntity>>
    val allStandings: StateFlow<List<StandingsEntity>>
    val allChatMessages: StateFlow<List<ChatMessageEntity>>

    private val _selectedMatchId = MutableStateFlow<String?>(null)
    val selectedMatchId: StateFlow<String?> = _selectedMatchId.asStateFlow()

    private val _selectedMatch = MutableStateFlow<MatchEntity?>(null)
    val selectedMatch: StateFlow<MatchEntity?> = _selectedMatch.asStateFlow()

    private val _chatMatchContextId = MutableStateFlow<String?>(null)
    val chatMatchContextId: StateFlow<String?> = _chatMatchContextId.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WorldCupRepository(database.matchDao())

        // Wire reactive properties
        allMatches = repository.allMatches.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allStandings = repository.allStandings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allChatMessages = repository.allChatMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Sync selected match details if matches list changes or selection changes
        viewModelScope.launch {
            combine(_selectedMatchId, allMatches) { id, matches ->
                matches.find { it.id == id }
            }.collect {
                _selectedMatch.value = it
            }
        }

        // Initialize and Start simulated live updates
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            startLiveMatchSimulation()
        }
    }

    fun selectMatch(matchId: String?) {
        _selectedMatchId.value = matchId
    }

    fun selectChatMatchContext(matchId: String?) {
        _chatMatchContextId.value = matchId
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
            // Add a warm introduction message
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "AI",
                    message = "Welcome to the World Cup 2026 AI Predictor! 🏆 Ask me about standings, upcoming game analyses, match predictions, or team tactics! I can even analyze the current live matches in detail."
                )
            )
        }
    }

    fun sendChatMessage(messageText: String) {
        if (messageText.isBlank()) return

        val contextId = _chatMatchContextId.value
        viewModelScope.launch {
            // Save user message to database
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "USER",
                    message = messageText,
                    matchId = contextId
                )
            )

            _isAiTyping.value = true

            // Gather recent context for the match if applicable
            var contextPrompt: String? = null
            if (contextId != null) {
                val match = repository.getMatchById(contextId)
                if (match != null) {
                    contextPrompt = """
                        Match: ${match.teamA} (${match.teamACode}) vs ${match.teamB} (${match.teamBCode})
                        Date & Venue: ${match.matchDate} at ${match.venue}
                        Stage: ${match.stage}
                        Current Status: ${match.status} (Minute: ${match.liveMinute}', Score: ${match.scoreA}-${match.scoreB})
                        Possession: ${match.teamA} ${match.possessionA}% - ${match.possessionB}% ${match.teamB}
                        Shots: ${match.teamA} ${match.shotsA} - ${match.shotsB} ${match.teamB}
                        Fouls: ${match.teamA} ${match.foulsA} - ${match.foulsB} ${match.teamB}
                    """.trimIndent()
                }
            }

            // Map database chat messages to Gemini's format to maintain context (last 6 messages)
            val dbHistory = allChatMessages.value.takeLast(6)
            val chatHistory = dbHistory.map { msg ->
                GeminiContent(
                    parts = listOf(GeminiPart(msg.message))
                )
            }

            // Call Gemini Api on background thread
            val response = GeminiApiClient.getAiResponse(
                prompt = messageText,
                chatHistory = chatHistory,
                matchContextPrompt = contextPrompt
            )

            _isAiTyping.value = false

            // Save AI message to database
            repository.insertChatMessage(
                ChatMessageEntity(
                    sender = "AI",
                    message = response,
                    matchId = contextId
                )
            )
        }
    }

    private suspend fun startLiveMatchSimulation() {
        while (true) {
            delay(10000) // Ticks every 10 seconds for speed and interactive feedback!
            val liveGames = allMatches.value.filter { it.status == MatchStatus.LIVE }

            for (match in liveGames) {
                val nextMinute = match.liveMinute + 1
                val isFinished = nextMinute >= 90

                val timeline = try {
                    eventListAdapter.fromJson(match.timelineJson)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    mutableListOf()
                }

                var scoreA = match.scoreA
                var scoreB = match.scoreB
                var shotsA = match.shotsA
                var shotsB = match.shotsB
                var foulsA = match.foulsA
                var foulsB = match.foulsB
                var yellowCardsA = match.yellowCardsA
                var yellowCardsB = match.yellowCardsB

                // Simulated Event Probability (15% chance per tick)
                if (Random.nextFloat() < 0.15f && !isFinished) {
                    val eventType = selectRandomEventType()
                    val team = if (Random.nextBoolean()) "A" else "B"
                    val teamName = if (team == "A") match.teamA else match.teamB

                    when (eventType) {
                        "GOAL" -> {
                            if (team == "A") {
                                scoreA++
                                shotsA++
                            } else {
                                scoreB++
                                shotsB++
                            }
                            val scorer = getScorerName(teamName)
                            timeline.add(
                                MatchEvent(
                                    minute = nextMinute,
                                    type = "GOAL",
                                    team = team,
                                    player = scorer,
                                    detail = "A beautiful team goal! Fans are roaring in the stadium."
                                )
                            )
                        }
                        "YELLOW_CARD" -> {
                            if (team == "A") yellowCardsA++ else yellowCardsB++
                            val player = getPlayerName(teamName)
                            timeline.add(
                                MatchEvent(
                                    minute = nextMinute,
                                    type = "YELLOW_CARD",
                                    team = team,
                                    player = player,
                                    detail = "Tactical slide tackle stopping a dangerous counter-attack."
                                )
                            )
                        }
                        "SHOT" -> {
                            if (team == "A") shotsA++ else shotsB++
                        }
                        "FOUL" -> {
                            if (team == "A") foulsA++ else foulsB++
                        }
                    }
                }

                if (isFinished) {
                    timeline.add(
                        MatchEvent(
                            minute = 90,
                            type = "FULLTIME",
                            team = "SYSTEM",
                            player = "Match Ended",
                            detail = "Fierce competition! Final whistle blown. Score: $scoreA - $scoreB"
                        )
                    )
                }

                // Update possession dynamically (slight wiggle)
                val deltaPossession = Random.nextInt(-3, 4)
                val nextPossessionA = (match.possessionA + deltaPossession).coerceIn(30, 70)
                val nextPossessionB = 100 - nextPossessionA

                val updatedMatch = match.copy(
                    liveMinute = nextMinute,
                    status = if (isFinished) MatchStatus.FINISHED else MatchStatus.LIVE,
                    scoreA = scoreA,
                    scoreB = scoreB,
                    shotsA = shotsA,
                    shotsB = shotsB,
                    foulsA = foulsA,
                    foulsB = foulsB,
                    yellowCardsA = yellowCardsA,
                    yellowCardsB = yellowCardsB,
                    possessionA = nextPossessionA,
                    possessionB = nextPossessionB,
                    timelineJson = eventListAdapter.toJson(timeline)
                )

                repository.updateMatch(updatedMatch)
            }
        }
    }

    private fun selectRandomEventType(): String {
        val rand = Random.nextFloat()
        return when {
            rand < 0.15f -> "GOAL"
            rand < 0.45f -> "YELLOW_CARD"
            rand < 0.75f -> "SHOT"
            else -> "FOUL"
        }
    }

    private fun getScorerName(teamName: String): String {
        return when (teamName) {
            "Germany" -> listOf("Kai Havertz", "Jamal Musiala", "Florian Wirtz", "Niclas Füllkrug").random()
            "Spain" -> listOf("Alvaro Morata", "Lamine Yamal", "Nico Williams", "Dani Olmo").random()
            "Argentina" -> listOf("Lionel Messi", "Julian Alvarez", "Lautaro Martinez", "Enzo Fernandez").random()
            "England" -> listOf("Harry Kane", "Jude Bellingham", "Bukayo Saka", "Phil Foden").random()
            "United States" -> listOf("Christian Pulisic", "Folarin Balogun", "Timothy Weah").random()
            "Mexico" -> listOf("Santiago Giménez", "Henry Martín", "Luis Chávez").random()
            "Canada" -> listOf("Jonathan David", "Cyle Larin", "Alphonso Davies").random()
            else -> "Striker Pro"
        }
    }

    private fun getPlayerName(teamName: String): String {
        return when (teamName) {
            "Germany" -> listOf("Antonio Rüdiger", "Joshua Kimmich", "Robert Andrich").random()
            "Spain" -> listOf("Dani Carvajal", "Robin Le Normand", "Marc Cucurella").random()
            "Argentina" -> listOf("Cristian Romero", "Rodrigo De Paul", "Nicolas Otamendi").random()
            "England" -> listOf("Declan Rice", "John Stones", "Kyle Walker").random()
            "United States" -> listOf("Weston McKennie", "Tyler Adams", "Antonee Robinson").random()
            "Mexico" -> listOf("Edson Álvarez", "Johan Vásquez", "César Montes").random()
            "Canada" -> listOf("Alistair Johnston", "Stephen Eustáquio", "Kamal Miller").random()
            else -> "Defender Pro"
        }
    }
}
