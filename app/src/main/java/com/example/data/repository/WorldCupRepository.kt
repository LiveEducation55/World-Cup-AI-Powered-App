package com.example.data.repository

import com.example.data.database.MatchDao
import com.example.data.models.MatchEntity
import com.example.data.models.StandingsEntity
import com.example.data.models.ChatMessageEntity
import com.example.data.models.MatchStatus
import com.example.data.models.MatchEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WorldCupRepository(private val matchDao: MatchDao) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val eventListAdapter = moshi.adapter<List<MatchEvent>>(
        Types.newParameterizedType(List::class.java, MatchEvent::class.java)
    )

    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatches()
    val liveMatches: Flow<List<MatchEntity>> = matchDao.getLiveMatches()
    val allStandings: Flow<List<StandingsEntity>> = matchDao.getAllStandings()
    val allChatMessages: Flow<List<ChatMessageEntity>> = matchDao.getAllChatMessages()

    fun getChatMessagesForMatch(matchId: String): Flow<List<ChatMessageEntity>> {
        return matchDao.getChatMessagesForMatch(matchId)
    }

    suspend fun getMatchById(matchId: String): MatchEntity? {
        return matchDao.getMatchById(matchId)
    }

    suspend fun updateMatch(match: MatchEntity) {
        matchDao.updateMatch(match)
    }

    suspend fun insertChatMessage(message: ChatMessageEntity): Long {
        return matchDao.insertChatMessage(message)
    }

    suspend fun clearChatHistory() {
        matchDao.clearChatHistory()
    }

    suspend fun seedDatabaseIfEmpty() {
        val existingMatches = allMatches.first()
        if (existingMatches.isNotEmpty()) return

        // 1. Seed Standings
        val standings = listOf(
            // Group A
            StandingsEntity(groupName = "Group A", teamName = "Mexico", teamCode = "MEX", flag = "🇲🇽", played = 3, won = 2, drawn = 1, lost = 0, goalsFor = 6, goalsAgainst = 2, points = 7),
            StandingsEntity(groupName = "Group A", teamName = "Ecuador", teamCode = "ECU", flag = "🇪🇨", played = 3, won = 2, drawn = 0, lost = 1, goalsFor = 5, goalsAgainst = 3, points = 6),
            StandingsEntity(groupName = "Group A", teamName = "New Zealand", teamCode = "NZL", flag = "🇳🇿", played = 3, won = 0, drawn = 2, lost = 1, goalsFor = 2, goalsAgainst = 4, points = 2),
            StandingsEntity(groupName = "Group A", teamName = "Cameroon", teamCode = "CMR", flag = "🇨🇲", played = 3, won = 0, drawn = 1, lost = 2, goalsFor = 1, goalsAgainst = 5, points = 1),

            // Group B
            StandingsEntity(groupName = "Group B", teamName = "Canada", teamCode = "CAN", flag = "🇨🇦", played = 3, won = 1, drawn = 2, lost = 0, goalsFor = 4, goalsAgainst = 3, points = 5),
            StandingsEntity(groupName = "Group B", teamName = "Sweden", teamCode = "SWE", flag = "🇸🇪", played = 3, won = 1, drawn = 1, lost = 1, goalsFor = 4, goalsAgainst = 4, points = 4),
            StandingsEntity(groupName = "Group B", teamName = "Egypt", teamCode = "EGY", flag = "🇪🇬", played = 3, won = 1, drawn = 1, lost = 1, goalsFor = 3, goalsAgainst = 3, points = 4),
            StandingsEntity(groupName = "Group B", teamName = "Honduras", teamCode = "HON", flag = "🇭🇳", played = 3, won = 0, drawn = 2, lost = 1, goalsFor = 2, goalsAgainst = 4, points = 2),

            // Group C
            StandingsEntity(groupName = "Group C", teamName = "United States", teamCode = "USA", flag = "🇺🇸", played = 3, won = 2, drawn = 1, lost = 0, goalsFor = 7, goalsAgainst = 2, points = 7),
            StandingsEntity(groupName = "Group C", teamName = "Australia", teamCode = "AUS", flag = "🇦🇺", played = 3, won = 1, drawn = 1, lost = 1, goalsFor = 4, goalsAgainst = 5, points = 4),
            StandingsEntity(groupName = "Group C", teamName = "Ghana", teamCode = "GHA", flag = "🇬🇭", played = 3, won = 1, drawn = 0, lost = 2, goalsFor = 3, goalsAgainst = 5, points = 3),
            StandingsEntity(groupName = "Group C", teamName = "Austria", teamCode = "AUT", flag = "🇦🇹", played = 3, won = 0, drawn = 2, lost = 1, goalsFor = 2, goalsAgainst = 4, points = 2),

            // Group D
            StandingsEntity(groupName = "Group D", teamName = "Argentina", teamCode = "ARG", flag = "🇦🇷", played = 3, won = 3, drawn = 0, lost = 0, goalsFor = 8, goalsAgainst = 1, points = 9),
            StandingsEntity(groupName = "Group D", teamName = "Morocco", teamCode = "MAR", flag = "🇲🇦", played = 3, won = 2, drawn = 0, lost = 1, goalsFor = 5, goalsAgainst = 3, points = 6),
            StandingsEntity(groupName = "Group D", teamName = "Czechia", teamCode = "CZE", flag = "🇨🇿", played = 3, won = 0, drawn = 1, lost = 2, goalsFor = 2, goalsAgainst = 6, points = 1),
            StandingsEntity(groupName = "Group D", teamName = "Nigeria", teamCode = "NGA", flag = "🇳🇬", played = 3, won = 0, drawn = 1, lost = 2, goalsFor = 1, goalsAgainst = 6, points = 1)
        )
        matchDao.insertStandings(standings)

        // 2. Seed Matches
        val finishedEvents1 = listOf(
            MatchEvent(1, "KICKOFF", "SYSTEM", "Match Started"),
            MatchEvent(12, "GOAL", "A", "Santiago Giménez", "Clinical finish in the box"),
            MatchEvent(45, "HALFTIME", "SYSTEM", "Halftime"),
            MatchEvent(67, "GOAL", "A", "Henry Martín", "Bullet header from a corner"),
            MatchEvent(88, "GOAL", "B", "Enner Valencia", "Penalty kick converted"),
            MatchEvent(90, "FULLTIME", "SYSTEM", "Fulltime whistle blown")
        )

        val finishedEvents2 = listOf(
            MatchEvent(1, "KICKOFF", "SYSTEM", "Match Started"),
            MatchEvent(8, "GOAL", "A", "Christian Pulisic", "Amazing curly strike into the top corner"),
            MatchEvent(22, "GOAL", "A", "Folarin Balogun", "Tapped in from close range"),
            MatchEvent(45, "HALFTIME", "SYSTEM", "Halftime"),
            MatchEvent(55, "YELLOW_CARD", "B", "Harry Souttar", "Tactical foul"),
            MatchEvent(71, "GOAL", "B", "Mitchell Duke", "Header off a set piece"),
            MatchEvent(85, "GOAL", "A", "Timothy Weah", "Powerful drill shot from outside the box"),
            MatchEvent(90, "FULLTIME", "SYSTEM", "USA advances with style!")
        )

        val liveEventsGermanySpain = listOf(
            MatchEvent(1, "KICKOFF", "SYSTEM", "Match Started"),
            MatchEvent(28, "GOAL", "A", "Florian Wirtz", "Stunning volley assisted by Musiala"),
            MatchEvent(42, "YELLOW_CARD", "B", "Rodri", "Cynical pull back on Musiala"),
            MatchEvent(45, "HALFTIME", "SYSTEM", "Halftime: Germany 1-0 Spain"),
            MatchEvent(58, "GOAL", "B", "Lamine Yamal", "Incredible solo dribble and curling left-foot strike")
        )

        val liveEventsArgentinaEngland = listOf(
            MatchEvent(1, "KICKOFF", "SYSTEM", "Match Started"),
            MatchEvent(5, "YELLOW_CARD", "A", "Cristian Romero", "Late slide tackle on Kane")
        )

        val matches = listOf(
            // Past Matches
            MatchEntity(
                id = "m1",
                teamA = "Mexico", teamB = "Ecuador", teamACode = "MEX", teamBCode = "ECU",
                teamAFlag = "🇲🇽", teamBFlag = "🇪🇨", scoreA = 2, scoreB = 1,
                matchTime = "18:00", matchDate = "June 11, 2026",
                status = MatchStatus.FINISHED, stage = "Group Stage",
                venue = "Estadio Azteca, Mexico City", groupName = "Group A",
                possessionA = 52, possessionB = 48, shotsA = 12, shotsB = 9,
                foulsA = 14, foulsB = 12, yellowCardsA = 1, yellowCardsB = 2,
                timelineJson = eventListAdapter.toJson(finishedEvents1)
            ),
            MatchEntity(
                id = "m2",
                teamA = "United States", teamB = "Australia", teamACode = "USA", teamBCode = "AUS",
                teamAFlag = "🇺🇸", teamBFlag = "🇦🇺", scoreA = 3, scoreB = 1,
                matchTime = "19:00", matchDate = "June 12, 2026",
                status = MatchStatus.FINISHED, stage = "Group Stage",
                venue = "SoFi Stadium, Los Angeles", groupName = "Group C",
                possessionA = 58, possessionB = 42, shotsA = 16, shotsB = 6,
                foulsA = 9, foulsB = 15, yellowCardsA = 0, yellowCardsB = 3,
                timelineJson = eventListAdapter.toJson(finishedEvents2)
            ),
            MatchEntity(
                id = "m3",
                teamA = "United States", teamB = "Morocco", teamACode = "USA", teamBCode = "MAR",
                teamAFlag = "🇺🇸", teamBFlag = "🇲🇦", scoreA = 2, scoreB = 1,
                matchTime = "17:00", matchDate = "June 29, 2026",
                status = MatchStatus.FINISHED, stage = "Round of 32",
                venue = "Mercedes-Benz Stadium, Atlanta", groupName = "",
                possessionA = 49, possessionB = 51, shotsA = 10, shotsB = 11,
                foulsA = 12, foulsB = 14, yellowCardsA = 2, yellowCardsB = 1
            ),
            MatchEntity(
                id = "m4",
                teamA = "Brazil", teamB = "Senegal", teamACode = "BRA", teamBCode = "SEN",
                teamAFlag = "🇧🇷", teamBFlag = "🇸🇳", scoreA = 3, scoreB = 1,
                matchTime = "20:00", matchDate = "June 30, 2026",
                status = MatchStatus.FINISHED, stage = "Round of 32",
                venue = "BC Place, Vancouver", groupName = "",
                possessionA = 62, possessionB = 38, shotsA = 18, shotsB = 8,
                foulsA = 8, foulsB = 16, yellowCardsA = 1, yellowCardsB = 4
            ),

            // Live Matches (Playing Today, July 2, 2026)
            MatchEntity(
                id = "m5",
                teamA = "Germany", teamB = "Spain", teamACode = "GER", teamBCode = "ESP",
                teamAFlag = "🇩🇪", teamBFlag = "🇪🇸", scoreA = 1, scoreB = 1,
                matchTime = "14:00", matchDate = "July 2, 2026",
                status = MatchStatus.LIVE, stage = "Round of 16",
                venue = "Mercedes-Benz Stadium, Atlanta", groupName = "",
                liveMinute = 64, possessionA = 47, possessionB = 53, shotsA = 9, shotsB = 11,
                foulsA = 11, foulsB = 8, yellowCardsA = 1, yellowCardsB = 2,
                timelineJson = eventListAdapter.toJson(liveEventsGermanySpain)
            ),
            MatchEntity(
                id = "m6",
                teamA = "Argentina", teamB = "England", teamACode = "ARG", teamBCode = "ENG",
                teamAFlag = "🇦🇷", teamBFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿", scoreA = 0, scoreB = 0,
                matchTime = "18:00", matchDate = "July 2, 2026",
                status = MatchStatus.LIVE, stage = "Round of 16",
                venue = "SoFi Stadium, Los Angeles", groupName = "",
                liveMinute = 12, possessionA = 51, possessionB = 49, shotsA = 2, shotsB = 1,
                foulsA = 4, foulsB = 3, yellowCardsA = 1, yellowCardsB = 0,
                timelineJson = eventListAdapter.toJson(liveEventsArgentinaEngland)
            ),

            // Upcoming Knockouts
            MatchEntity(
                id = "m7",
                teamA = "Canada", teamB = "Netherlands", teamACode = "CAN", teamBCode = "NED",
                teamAFlag = "🇨🇦", teamBFlag = "🇳🇱", scoreA = 0, scoreB = 0,
                matchTime = "16:00", matchDate = "July 3, 2026",
                status = MatchStatus.UPCOMING, stage = "Round of 16",
                venue = "BMO Field, Toronto", groupName = ""
            ),
            MatchEntity(
                id = "m8",
                teamA = "Brazil", teamB = "Uruguay", teamACode = "BRA", teamBCode = "URU",
                teamAFlag = "🇧🇷", teamBFlag = "🇺🇾", scoreA = 0, scoreB = 0,
                matchTime = "19:00", matchDate = "July 4, 2026",
                status = MatchStatus.UPCOMING, stage = "Round of 16",
                venue = "Lumen Field, Seattle", groupName = ""
            ),
            MatchEntity(
                id = "m9",
                teamA = "Mexico", teamB = "Portugal", teamACode = "MEX", teamBCode = "POR",
                teamAFlag = "🇲🇽", teamBFlag = "🇵🇹", scoreA = 0, scoreB = 0,
                matchTime = "18:00", matchDate = "July 5, 2026",
                status = MatchStatus.UPCOMING, stage = "Round of 16",
                venue = "Estadio Azteca, Mexico City", groupName = ""
            ),
            MatchEntity(
                id = "m10",
                teamA = "United States", teamB = "TBD", teamACode = "USA", teamBCode = "TBD",
                teamAFlag = "🇺🇸", teamBFlag = "🏳️", scoreA = 0, scoreB = 0,
                matchTime = "17:00", matchDate = "July 9, 2026",
                status = MatchStatus.UPCOMING, stage = "Quarter-finals",
                venue = "Hard Rock Stadium, Miami", groupName = ""
            ),
            MatchEntity(
                id = "m11",
                teamA = "TBD", teamB = "TBD", teamACode = "TBD", teamBCode = "TBD",
                teamAFlag = "🏳️", teamBFlag = "🏳️", scoreA = 0, scoreB = 0,
                matchTime = "19:00", matchDate = "July 19, 2026",
                status = MatchStatus.UPCOMING, stage = "Final",
                venue = "MetLife Stadium, East Rutherford, NJ", groupName = ""
            )
        )
        matchDao.insertMatches(matches)
    }
}
