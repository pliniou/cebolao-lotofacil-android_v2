package com.cebolao.lotofacil.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration from version 5 to 6
 * Adds performance indices to draws and user_games tables
 */
val MIGRATION_1_6 = object : Migration(1, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS draws (
                contestNumber INTEGER NOT NULL,
                numbers TEXT NOT NULL,
                date INTEGER,
                PRIMARY KEY(contestNumber)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS check_runs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ticketMask INTEGER NOT NULL,
                lotteryId TEXT NOT NULL,
                drawRange TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                metricsJSON TEXT NOT NULL,
                hitsJSON TEXT NOT NULL,
                sourceHash TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_games (
                id TEXT NOT NULL,
                lotteryId TEXT NOT NULL,
                numbersMask INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                pinned INTEGER NOT NULL,
                tags TEXT NOT NULL,
                source TEXT NOT NULL,
                seed INTEGER,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS draw_details (
                contestNumber INTEGER NOT NULL,
                nextEstimatedPrize REAL NOT NULL,
                nextContestDate TEXT,
                nextContestNumber INTEGER,
                accumulatedValue05 REAL NOT NULL,
                accumulatedValueSpecial REAL NOT NULL,
                location TEXT,
                winnersByState TEXT NOT NULL,
                prizeRates TEXT NOT NULL,
                PRIMARY KEY(contestNumber)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_draws_contestNumber ON draws(contestNumber)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_draws_date ON draws(date)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_user_games_pinned ON user_games(pinned)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_user_games_createdAt ON user_games(createdAt)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_user_games_source ON user_games(source)"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add indices to draws table
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_draws_contestNumber ON draws(contestNumber)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_draws_date ON draws(date)"
        )
        
        // Add indices to user_games table
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_user_games_pinned ON user_games(pinned)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_user_games_createdAt ON user_games(createdAt)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_user_games_source ON user_games(source)"
        )
    }
}

/**
 * Database migration from version 6 to 7
 * Adds additional performance indices to check_runs and draw_details tables
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add indices to check_runs table
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_check_runs_createdAt ON check_runs(createdAt)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_check_runs_lotteryId ON check_runs(lotteryId)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_check_runs_ticketMask ON check_runs(ticketMask)"
        )

        // Add index to draw_details table
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_draw_details_contestNumber ON draw_details(contestNumber)"
        )
    }
}

/**
 * Database migration from version 1 to 7 (for fresh installs)
 * Combines all migrations into a single migration for new database creation
 */
val MIGRATION_1_7 = object : Migration(1, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create draws table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS draws (
                contestNumber INTEGER NOT NULL,
                numbers TEXT NOT NULL,
                date INTEGER,
                PRIMARY KEY(contestNumber)
            )
            """.trimIndent()
        )
        // Create check_runs table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS check_runs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ticketMask INTEGER NOT NULL,
                lotteryId TEXT NOT NULL,
                drawRange TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                metricsJSON TEXT NOT NULL,
                hitsJSON TEXT NOT NULL,
                sourceHash TEXT NOT NULL
            )
            """.trimIndent()
        )
        // Create user_games table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_games (
                id TEXT NOT NULL,
                lotteryId TEXT NOT NULL,
                numbersMask INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                pinned INTEGER NOT NULL,
                tags TEXT NOT NULL,
                source TEXT NOT NULL,
                seed INTEGER,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        // Create draw_details table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS draw_details (
                contestNumber INTEGER NOT NULL,
                nextEstimatedPrize REAL NOT NULL,
                nextContestDate TEXT,
                nextContestNumber INTEGER,
                accumulatedValue05 REAL NOT NULL,
                accumulatedValueSpecial REAL NOT NULL,
                location TEXT,
                winnersByState TEXT NOT NULL,
                prizeRates TEXT NOT NULL,
                PRIMARY KEY(contestNumber)
            )
            """.trimIndent()
        )
        // Create indices for draws
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_draws_contestNumber ON draws(contestNumber)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_draws_date ON draws(date)")
        // Create indices for user_games
        db.execSQL("CREATE INDEX IF NOT EXISTS index_user_games_pinned ON user_games(pinned)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_user_games_createdAt ON user_games(createdAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_user_games_source ON user_games(source)")
        // Create indices for check_runs
        db.execSQL("CREATE INDEX IF NOT EXISTS index_check_runs_createdAt ON check_runs(createdAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_check_runs_lotteryId ON check_runs(lotteryId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_check_runs_ticketMask ON check_runs(ticketMask)")
        // Create index for draw_details
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_draw_details_contestNumber ON draw_details(contestNumber)")
    }
}
