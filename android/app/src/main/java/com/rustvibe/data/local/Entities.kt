package com.rustvibe.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String, // YouTube Video ID
    val title: String,
    val producer: String, // Channel / artist / producer name
    val durationSeconds: Int,
    val thumbnailPath: String, // Local cached high-res image
    val m4aFilePath: String, // Primary audio stream
    val backupMp3FilePath: String? = null, // Invisible failover fallback stream
    val lyricsFilePath: String? = null, // Timed LRC / VTT file
    val hasSyncedLyrics: Boolean = false,
    val isEphemeral: Boolean = false, // 1 = One-time listen auto-purged, 0 = Persistent
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val customCoverPath: String? = null, // Null indicates dynamic 2x2 grid mosaic
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId", "position"]),
        Index(value = ["trackId"])
    ]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val position: Int, // 0-indexed ordering. Append policy: MAX(position) + 1
    val addedAt: Long = System.currentTimeMillis()
)
