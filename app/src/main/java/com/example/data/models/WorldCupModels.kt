package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class MatchStatus {
    UPCOMING, LIVE, FINISHED
}

@JsonClass(generateAdapter = true)
data class MatchEvent(
    val minute: Int,
    val type: String, // GOAL, YELLOW_CARD, RED_CARD, SUB, KICKOFF, HALFTIME, FULLTIME
    val team: String, // "A", "B", or "SYSTEM"
    val player: String,
    val detail: String = ""
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val teamA: String,
    val teamB: String,
    val teamACode: String,
    val teamBCode: String,
    val teamAFlag: String, // Country flag emoji or custom asset name
    val teamBFlag: String,
    val scoreA: Int,
    val scoreB: Int,
    val matchTime: String, // Time representation (e.g., "18:00")
    val matchDate: String, // Date representation (e.g., "July 2, 2026")
    val status: MatchStatus,
    val stage: String, // "Group Stage", "Round of 32", "Round of 16", "Quarter-finals", "Semi-finals", "Final"
    val venue: String,
    val groupName: String, // "Group A", "Group B", etc. or "" for knockouts
    val liveMinute: Int = 0,
    val possessionA: Int = 50,
    val possessionB: Int = 50,
    val shotsA: Int = 0,
    val shotsB: Int = 0,
    val foulsA: Int = 0,
    val foulsB: Int = 0,
    val yellowCardsA: Int = 0,
    val yellowCardsB: Int = 0,
    val redCardsA: Int = 0,
    val redCardsB: Int = 0,
    val timelineJson: String = "[]" // JSON array of MatchEvent
)

@Entity(tableName = "standings")
data class StandingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val teamName: String,
    val teamCode: String,
    val flag: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val matchId: String? = null // Optional context
)
