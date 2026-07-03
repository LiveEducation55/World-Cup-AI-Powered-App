package com.example.data.database

import android.content.Context
import androidx.room.*
import com.example.data.models.MatchEntity
import com.example.data.models.StandingsEntity
import com.example.data.models.ChatMessageEntity
import com.example.data.models.MatchStatus

class Converters {
    @TypeConverter
    fun fromMatchStatus(status: MatchStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMatchStatus(value: String): MatchStatus {
        return try {
            MatchStatus.valueOf(value)
        } catch (e: Exception) {
            MatchStatus.UPCOMING
        }
    }
}

@Database(entities = [MatchEntity::class, StandingsEntity::class, ChatMessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "world_cup_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
