package com.nuha.app.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import com.nuha.app.Pending
import kotlinx.coroutines.flow.Flow

/**
 * One child's settings (FR-SET-002), in their own table so the profile keeps only the fields FR-PRF-007 allows.
 * Values are the same words the settings screen offers, so the database reads like the screen.
 */
@Entity(tableName = "child_settings")
data class ChildSettings(
    @PrimaryKey val childId: Long,
    /** colourful, muted or contrast (FR-SET-001). */
    val appearance: String = "colourful",
    /** full, reduced or none (FR-SET-004). */
    val motion: String = Pending.DefaultMotion,
    val voice: Boolean = true,
    val effects: Boolean = true,
    val music: Boolean = false,
    /** off, gentle or lively (FR-RWD-005). */
    val celebration: String = "gentle",
    /** The task-screen mascot: activity (its own), guide (the lead guide, FR-ACT-016) or off. */
    val mascot: String = "activity",
    /** Quiet seconds before the prompt ladder steps up by itself; 0 means never. */
    val promptDelaySeconds: Int = Pending.DefaultPromptDelaySeconds,
    /** A key of [Pending.TargetScales]. */
    val targetSize: String = "standard",
    /** western or arabic-indic (OI-02). */
    val numerals: String = Pending.DefaultNumerals.id,
    /** The one activity the map is locked to (FR-HOM-006), or null for the whole map. */
    val mapLock: String? = null,
)

@Dao
interface SettingsDao {
    @Query("SELECT * FROM child_settings WHERE childId = :childId")
    fun observe(childId: Long): Flow<ChildSettings?>
    @Query("SELECT * FROM child_settings WHERE childId = :childId")
    suspend fun get(childId: Long): ChildSettings?
    @Upsert suspend fun save(settings: ChildSettings)
    @Query("DELETE FROM child_settings WHERE childId = :childId")
    suspend fun delete(childId: Long)
}

internal object SettingsSchema {
    /** v4 → v5: per-child settings, each existing child starting from the device preferences it played with. */
    val create = listOf(
        "CREATE TABLE IF NOT EXISTS `child_settings` (`childId` INTEGER NOT NULL, `appearance` TEXT NOT NULL, `motion` TEXT NOT NULL, " +
            "`voice` INTEGER NOT NULL, `effects` INTEGER NOT NULL, `music` INTEGER NOT NULL, `celebration` TEXT NOT NULL, `mascot` TEXT NOT NULL, " +
            "`promptDelaySeconds` INTEGER NOT NULL, `targetSize` TEXT NOT NULL, `numerals` TEXT NOT NULL, `mapLock` TEXT, PRIMARY KEY(`childId`))",
        "INSERT OR IGNORE INTO `child_settings` SELECT c.`id`, COALESCE(p.`appearance`, 'colourful'), " +
            "CASE WHEN COALESCE(p.`reducedMotion`, 1) = 1 THEN 'reduced' ELSE 'full' END, COALESCE(p.`voice`, 1), COALESCE(p.`effects`, 1), " +
            "COALESCE(p.`music`, 0), 'gentle', 'activity', ${Pending.DefaultPromptDelaySeconds}, 'standard', '${Pending.DefaultNumerals.id}', NULL " +
            "FROM `child_profiles` c LEFT JOIN `app_preferences` p ON p.`id` = 1",
    )
}
