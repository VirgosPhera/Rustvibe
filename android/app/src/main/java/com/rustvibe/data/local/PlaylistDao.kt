package com.rustvibe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylistsDirect(): List<PlaylistEntity>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getTrackCountForPlaylist(playlistId: String): Int

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.position ASC
    """)
    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>>

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: String): Int?

    @Query("""
        SELECT t.thumbnailPath FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.position ASC
        LIMIT 4
    """)
    fun getFirstFourThumbnails(playlistId: String): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE isEphemeral = 0 ORDER BY createdAt DESC")
    fun getAllOfflineTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksDirect(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE isEphemeral = 1")
    suspend fun getEphemeralTracks(): List<TrackEntity>

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun deletePlaylistTracks(playlistId: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId IN (:playlistIds)")
    suspend fun deletePlaylistTracksBatch(playlistIds: List<String>)

    @Query("DELETE FROM playlists WHERE id IN (:playlistIds)")
    suspend fun deletePlaylistsBatch(playlistIds: List<String>)

    @Transaction
    suspend fun deletePlaylists(playlistIds: List<String>) {
        deletePlaylistTracksBatch(playlistIds)
        deletePlaylistsBatch(playlistIds)
    }

    @Query("UPDATE playlist_tracks SET position = :newPos WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun updateTrackPosition(playlistId: String, trackId: String, newPos: Int)

    @Query("UPDATE playlists SET customCoverPath = :coverPath, updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updateCustomCover(playlistId: String, coverPath: String?, timestamp: Long = System.currentTimeMillis())

    /**
     * Strict Policy: Any track added to an existing playlist is guaranteed to be appended
     * at the very end of the playlist (position = MAX(position) + 1).
     */
    @Transaction
    suspend fun appendTrackToPlaylist(playlistId: String, trackId: String) {
        val currentMax = getMaxPosition(playlistId) ?: -1
        val nextPosition = currentMax + 1
        insertCrossRef(PlaylistTrackCrossRef(playlistId, trackId, nextPosition))
        updatePlaylistTimestamp(playlistId, System.currentTimeMillis())
    }

    /**
     * Batch update for smooth drag-and-drop reordering.
     */
    @Transaction
    suspend fun reorderTracks(playlistId: String, orderedTrackIds: List<String>) {
        orderedTrackIds.forEachIndexed { index, trackId ->
            updateTrackPosition(playlistId, trackId, index)
        }
        updatePlaylistTimestamp(playlistId, System.currentTimeMillis())
    }

    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistTimestamp(playlistId: String, timestamp: Long)
}
