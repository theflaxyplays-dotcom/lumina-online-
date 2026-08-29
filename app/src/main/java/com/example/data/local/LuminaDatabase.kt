package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_history")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val text: String,
    val persona: String,
    val actionJson: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "macro_routines")
data class MacroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val stepsJson: String,
    val variableNamesJson: String,
    val isPrebuilt: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "whatsapp_replies")
data class WhatsAppReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val incomingMessage: String,
    val autoRepliedText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatEntity): Long

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}

@Dao
interface MacroDao {
    @Query("SELECT * FROM macro_routines ORDER BY id DESC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macro_routines WHERE id = :id LIMIT 1")
    suspend fun getMacroById(id: Long): MacroEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity): Long

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)
}

@Dao
interface WhatsAppReplyDao {
    @Query("SELECT * FROM whatsapp_replies ORDER BY timestamp DESC")
    fun getAllReplies(): Flow<List<WhatsAppReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: WhatsAppReplyEntity): Long

    @Query("DELETE FROM whatsapp_replies")
    suspend fun clearReplies()
}

@Database(
    entities = [ChatEntity::class, MacroEntity::class, WhatsAppReplyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LuminaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun macroDao(): MacroDao
    abstract fun whatsAppReplyDao(): WhatsAppReplyDao

    companion object {
        @Volatile
        private var INSTANCE: LuminaDatabase? = null

        fun getDatabase(context: Context): LuminaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LuminaDatabase::class.java,
                    "lumina_os_master.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
