package com.rustvibe.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ScannedLocalTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val uri: Uri,
    val fileName: String,
    val extension: String,
    val sizeBytes: Long,
    val embeddedArtBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ScannedLocalTrack
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
}

data class ScannedFolderResult(
    val folderName: String,
    val folderUri: Uri,
    val tracks: List<ScannedLocalTrack>
)

object LocalMusicScanner {
    private const val TAG = "LocalMusicScanner"
    private val SUPPORTED_AUDIO_EXTENSIONS = setOf(
        "mp3", "m4a", "aac", "flac", "wav", "ogg", "opus", "wma"
    )

    suspend fun scanFolder(context: Context, treeUri: Uri): ScannedFolderResult? = withContext(Dispatchers.IO) {
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext null
            val folderName = rootDoc.name ?: "Folder Musik Lokal"

            val tracks = mutableListOf<ScannedLocalTrack>()
            val mmr = MediaMetadataRetriever()

            fun scanDir(dir: DocumentFile) {
                val files = dir.listFiles()
                for (doc in files) {
                    if (doc.isDirectory) {
                        scanDir(doc)
                    } else if (doc.isFile) {
                        val name = doc.name ?: continue
                        val ext = name.substringAfterLast('.', "").lowercase()
                        if (ext in SUPPORTED_AUDIO_EXTENSIONS) {
                            try {
                                context.contentResolver.openFileDescriptor(doc.uri, "r")?.use { pfd ->
                                    mmr.setDataSource(pfd.fileDescriptor)

                                    val titleTag = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                    val artistTag = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                        ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                                    val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                    val durSec = (durationStr?.toLongOrNull() ?: 0L) / 1000L

                                    val finalTitle = if (!titleTag.isNullOrBlank()) titleTag.trim() else name.substringBeforeLast('.')
                                    val finalArtist = if (!artistTag.isNullOrBlank()) artistTag.trim() else "Artis Lokal"
                                    val embeddedArt = mmr.embeddedPicture

                                    tracks.add(
                                        ScannedLocalTrack(
                                            id = "loc_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                                            title = finalTitle,
                                            artist = finalArtist,
                                            durationSeconds = if (durSec > 0) durSec.toInt() else 180,
                                            uri = doc.uri,
                                            fileName = name,
                                            extension = ext,
                                            sizeBytes = doc.length(),
                                            embeddedArtBytes = embeddedArt
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to read metadata for $name", e)
                                tracks.add(
                                    ScannedLocalTrack(
                                        id = "loc_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                                        title = name.substringBeforeLast('.'),
                                        artist = "Artis Lokal",
                                        durationSeconds = 180,
                                        uri = doc.uri,
                                        fileName = name,
                                        extension = ext,
                                        sizeBytes = doc.length(),
                                        embeddedArtBytes = null
                                    )
                                )
                            }
                        }
                    }
                }
            }

            scanDir(rootDoc)
            try { mmr.release() } catch (e: Exception) {}

            return@withContext ScannedFolderResult(
                folderName = folderName,
                folderUri = treeUri,
                tracks = tracks
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning folder: $treeUri", e)
            null
        }
    }

    suspend fun scanDirectoryFile(context: Context, dir: File): ScannedFolderResult? = withContext(Dispatchers.IO) {
        if (!dir.exists() || !dir.isDirectory) return@withContext null
        try {
            val tracks = mutableListOf<ScannedLocalTrack>()
            val mmr = MediaMetadataRetriever()

            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val name = file.name
                    val ext = name.substringAfterLast('.', "").lowercase()
                    if (ext in SUPPORTED_AUDIO_EXTENSIONS) {
                        try {
                            mmr.setDataSource(file.absolutePath)
                            val titleTag = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                            val artistTag = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            val durSec = (durationStr?.toLongOrNull() ?: 0L) / 1000L

                            val finalTitle = if (!titleTag.isNullOrBlank()) titleTag.trim() else name.substringBeforeLast('.')
                            val finalArtist = if (!artistTag.isNullOrBlank()) artistTag.trim() else "Artis Lokal"
                            val embeddedArt = mmr.embeddedPicture

                            tracks.add(
                                ScannedLocalTrack(
                                    id = "loc_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                                    title = finalTitle,
                                    artist = finalArtist,
                                    durationSeconds = if (durSec > 0) durSec.toInt() else 180,
                                    uri = Uri.fromFile(file),
                                    fileName = name,
                                    extension = ext,
                                    sizeBytes = file.length(),
                                    embeddedArtBytes = embeddedArt
                                )
                            )
                        } catch (e: Exception) {
                            tracks.add(
                                ScannedLocalTrack(
                                    id = "loc_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                                    title = name.substringBeforeLast('.'),
                                    artist = "Artis Lokal",
                                    durationSeconds = 180,
                                    uri = Uri.fromFile(file),
                                    fileName = name,
                                    extension = ext,
                                    sizeBytes = file.length(),
                                    embeddedArtBytes = null
                                )
                            )
                        }
                    }
                }
            }
            try { mmr.release() } catch (e: Exception) {}

            return@withContext ScannedFolderResult(
                folderName = dir.name,
                folderUri = Uri.fromFile(dir),
                tracks = tracks
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning file directory: ${dir.absolutePath}", e)
            null
        }
    }

    suspend fun importTracksToPlaylist(
        context: Context,
        tracks: List<ScannedLocalTrack>,
        targetPlaylistId: String,
        targetPlaylistName: String,
        playlistDao: PlaylistDao,
        onProgress: (Float, String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
        val thumbDir = File(context.filesDir, "thumbnails").apply { mkdirs() }

        // Ensure playlist exists in DB
        val existingPlaylist = playlistDao.getPlaylistById(targetPlaylistId)
        if (existingPlaylist == null) {
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    id = targetPlaylistId,
                    name = targetPlaylistName
                )
            )
        }

        val total = tracks.size
        tracks.forEachIndexed { index, track ->
            val progress = (index.toFloat() + 0.5f) / total.toFloat()
            onProgress(progress, "Menyalin ${index + 1}/$total: ${track.title}")

            // 1. Copy audio file to internal storage
            val destAudio = File(audioDir, "${track.id}.${track.extension}")
            try {
                val stream = context.contentResolver.openInputStream(track.uri)
                    ?: if (track.uri.path != null && File(track.uri.path!!).exists()) File(track.uri.path!!).inputStream() else null
                stream?.use { input ->
                    FileOutputStream(destAudio).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy audio for ${track.title}", e)
            }

            // 2. Save embedded picture if present
            var thumbPath: String? = null
            if (track.embeddedArtBytes != null && track.embeddedArtBytes.isNotEmpty()) {
                val destThumb = File(thumbDir, "${track.id}.jpg")
                try {
                    destThumb.outputStream().use { it.write(track.embeddedArtBytes) }
                    thumbPath = destThumb.absolutePath
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to write embedded art for ${track.title}", e)
                }
            }

            // 3. Create TrackEntity and insert
            val trackEntity = TrackEntity(
                id = track.id,
                title = track.title,
                producer = track.artist,
                durationSeconds = track.durationSeconds,
                thumbnailPath = thumbPath ?: "",
                m4aFilePath = destAudio.absolutePath,
                backupMp3FilePath = if (track.extension == "mp3") destAudio.absolutePath else null,
                lyricsFilePath = null,
                hasSyncedLyrics = false,
                isEphemeral = false
            )
            playlistDao.insertTrack(trackEntity)
            playlistDao.appendTrackToPlaylist(targetPlaylistId, trackEntity.id)
        }
        onProgress(1.0f, "Selesai mengimpor $total lagu!")
    }
}
