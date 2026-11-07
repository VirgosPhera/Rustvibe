package com.rustvibe.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustvibe.audio.EngineState
import com.rustvibe.audio.LyricLine
import com.rustvibe.audio.LyricsParser
import com.rustvibe.audio.RustAudioEngineBridge
import com.rustvibe.data.local.LocalMusicScanner
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.data.local.RustVibeDatabase
import com.rustvibe.data.local.ScannedFolderResult
import com.rustvibe.data.local.TrackEntity
import com.rustvibe.data.remote.DownloadedPackage
import com.rustvibe.data.remote.DownloadProgressState
import com.rustvibe.data.remote.OnlinePlaylistResult
import com.rustvibe.data.remote.PlaylistSearchResult
import com.rustvibe.data.remote.SongSearchResult
import com.rustvibe.data.remote.YtDlpStandaloneManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class RustVibeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RustVibeDatabase.getInstance(application)
    private val playlistDao = db.playlistDao()
    private val audioEngine = RustAudioEngineBridge.instance
    private val notificationManager = com.rustvibe.audio.RustVibeNotificationManager(application)

    // Audio Playback States from Native Engine
    val playbackState: StateFlow<EngineState> = audioEngine.state
    val currentPositionMs: StateFlow<Long> = audioEngine.positionMs
    val isUsingBackup: StateFlow<Boolean> = audioEngine.isUsingBackup

    // Current playing metadata
    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val currentQueue: StateFlow<List<TrackEntity>> = _currentQueue.asStateFlow()

    fun setQueue(tracks: List<TrackEntity>) {
        _currentQueue.value = tracks
    }

    private val _currentLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val currentLyrics: StateFlow<List<LyricLine>> = _currentLyrics.asStateFlow()

    // Search & Downloader States
    private val _searchResults = MutableStateFlow<List<SongSearchResult>>(emptyList())
    val searchResults: StateFlow<List<SongSearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _pendingDownloadedPackage = MutableStateFlow<DownloadedPackage?>(null)
    val pendingDownloadedPackage: StateFlow<DownloadedPackage?> = _pendingDownloadedPackage.asStateFlow()

    private val _detectedPlaylist = MutableStateFlow<PlaylistSearchResult?>(null)
    val detectedPlaylist: StateFlow<PlaylistSearchResult?> = _detectedPlaylist.asStateFlow()

    private val _onlinePlaylists = MutableStateFlow<List<OnlinePlaylistResult>>(emptyList())
    val onlinePlaylists: StateFlow<List<OnlinePlaylistResult>> = _onlinePlaylists.asStateFlow()

    private val _downloadProgress = MutableStateFlow<DownloadProgressState?>(null)
    val downloadProgress: StateFlow<DownloadProgressState?> = _downloadProgress.asStateFlow()

    // Local Folder Import State
    private val _scannedLocalFolder = MutableStateFlow<ScannedFolderResult?>(null)
    val scannedLocalFolder: StateFlow<ScannedFolderResult?> = _scannedLocalFolder.asStateFlow()
    private var targetImportPlaylistId: String? = null

    // Theme State: Dark Theme vs Light Theme vs Dynamic System Accent (Android 15-16)
    private val _themeMode = MutableStateFlow(com.rustvibe.ui.theme.ThemeMode.DARK)
    val themeMode: StateFlow<com.rustvibe.ui.theme.ThemeMode> = _themeMode.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun setThemeMode(mode: com.rustvibe.ui.theme.ThemeMode) {
        _themeMode.value = mode
        _isDarkTheme.value = (mode != com.rustvibe.ui.theme.ThemeMode.LIGHT)
    }

    fun toggleTheme() {
        val next = when (_themeMode.value) {
            com.rustvibe.ui.theme.ThemeMode.DARK -> com.rustvibe.ui.theme.ThemeMode.LIGHT
            com.rustvibe.ui.theme.ThemeMode.LIGHT -> com.rustvibe.ui.theme.ThemeMode.DYNAMIC_SYSTEM
            com.rustvibe.ui.theme.ThemeMode.DYNAMIC_SYSTEM -> com.rustvibe.ui.theme.ThemeMode.DARK
        }
        setThemeMode(next)
    }

    // Playlist Data Flows
    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineTracks: StateFlow<List<TrackEntity>> = playlistDao.getAllOfflineTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repairAllExistingTracks()
            seedInitialDataIfNeeded()
        }

        // Default recommended search items
        _searchResults.value = listOf(
            SongSearchResult("sr_1", "Idol", "YOASOBI", 213, ""),
            SongSearchResult("sr_2", "Gurenge", "LiSA", 236, ""),
            SongSearchResult("sr_3", "Seven", "Jung Kook", 184, ""),
            SongSearchResult("sr_4", "Silhouette", "KANA-BOON", 240, ""),
            SongSearchResult("sr_5", "Blue Bird", "Ikimonogakari", 216, "")
        )

        audioEngine.onTrackCompleted = { trackId, isEphemeral ->
            if (isEphemeral) {
                viewModelScope.launch(Dispatchers.IO) {
                    purgeTrackFiles(trackId)
                }
            } else {
                viewModelScope.launch(Dispatchers.Main) {
                    skipToNext()
                }
            }
        }

        notificationManager.onPlayPause = { togglePlayPause() }
        notificationManager.onSkipNext = { skipToNext() }
        notificationManager.onSkipPrev = { skipToPrev() }

        // Live Media Notification Synchronization
        viewModelScope.launch {
            _currentTrack.collect { track ->
                if (track != null) {
                    notificationManager.updateNotification(
                        title = track.title,
                        artist = track.producer,
                        thumbnailPath = track.thumbnailPath,
                        positionMs = currentPositionMs.value,
                        durationSeconds = track.durationSeconds,
                        isPlaying = (playbackState.value == EngineState.PLAYING)
                    )
                } else {
                    notificationManager.cancelNotification()
                }
            }
        }

        viewModelScope.launch {
            playbackState.collect { state ->
                val track = _currentTrack.value
                if (track != null) {
                    notificationManager.updateNotification(
                        title = track.title,
                        artist = track.producer,
                        thumbnailPath = track.thumbnailPath,
                        positionMs = currentPositionMs.value,
                        durationSeconds = track.durationSeconds,
                        isPlaying = (state == EngineState.PLAYING)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationManager.cleanup()
    }

    fun movePlaylistTrack(playlistId: String, fromIndex: Int, toIndex: Int, currentTracks: List<TrackEntity>) {
        if (fromIndex == toIndex || fromIndex !in currentTracks.indices || toIndex !in currentTracks.indices) return
        val list = currentTracks.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.reorderTracks(playlistId, list.map { it.id })
        }
    }

    fun reorderPlaylistTracks(playlistId: String, reorderedTrackIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.reorderTracks(playlistId, reorderedTrackIds)
        }
    }

    fun scanLocalFolder(treeUri: Uri, targetPlaylistId: String? = null) {
        targetImportPlaylistId = targetPlaylistId
        viewModelScope.launch(Dispatchers.IO) {
            val res = LocalMusicScanner.scanFolder(getApplication(), treeUri)
            if (res != null && res.tracks.isNotEmpty()) {
                _scannedLocalFolder.value = res
            } else {
                Log.w("RustVibeVM", "No supported audio tracks found in selected folder: $treeUri")
            }
        }
    }

    fun scanLocalDirectoryPath(path: String, targetPlaylistId: String? = null) {
        targetImportPlaylistId = targetPlaylistId
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(path)
            val res = LocalMusicScanner.scanDirectoryFile(getApplication(), dir)
            if (res != null && res.tracks.isNotEmpty()) {
                _scannedLocalFolder.value = res
            } else {
                Log.w("RustVibeVM", "No supported audio tracks found in directory: $path")
            }
        }
    }

    fun dismissLocalFolderDialog() {
        _scannedLocalFolder.value = null
        targetImportPlaylistId = null
    }

    fun confirmImportLocalFolder(scanned: ScannedFolderResult, chosenPlaylistName: String) {
        val targetPlaylistId = targetImportPlaylistId ?: UUID.randomUUID().toString()
        val finalName = if (chosenPlaylistName.isNotBlank()) chosenPlaylistName.trim() else scanned.folderName
        _scannedLocalFolder.value = null

        viewModelScope.launch(Dispatchers.IO) {
            _downloadProgress.value = DownloadProgressState(
                isDownloading = true,
                title = "Mengimpor Folder: $finalName",
                progress = 0f,
                percent = 0,
                speedText = "Menyiapkan...",
                statusText = "0/${scanned.tracks.size} lagu diimpor"
            )

            LocalMusicScanner.importTracksToPlaylist(
                context = getApplication(),
                tracks = scanned.tracks,
                targetPlaylistId = targetPlaylistId,
                targetPlaylistName = finalName,
                playlistDao = playlistDao
            ) { progress, status ->
                _downloadProgress.value = DownloadProgressState(
                    isDownloading = true,
                    title = "Mengimpor Folder: $finalName",
                    progress = progress,
                    percent = (progress * 100).toInt(),
                    speedText = status,
                    statusText = "${(progress * scanned.tracks.size).toInt()}/${scanned.tracks.size} lagu"
                )
            }

            _downloadProgress.value = DownloadProgressState(
                isDownloading = false,
                title = "Import Folder Berhasil!",
                progress = 1f,
                percent = 100,
                speedText = "Selesai",
                statusText = "${scanned.tracks.size} lagu lokal berhasil ditambahkan ke '$finalName'"
            )
            delay(2500)
            _downloadProgress.value = null
            targetImportPlaylistId = null
        }
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>> {
        return playlistDao.getTracksForPlaylist(playlistId)
    }

    fun getPlaylistThumbnails(playlistId: String): Flow<List<String>> {
        return playlistDao.getFirstFourThumbnails(playlistId)
    }

    fun dismissPlaylistDialog() {
        _detectedPlaylist.value = null
    }

    fun confirmDownloadPlaylist(playlistResult: PlaylistSearchResult, chosenName: String) {
        val finalName = if (chosenName.isNotBlank()) chosenName.trim() else playlistResult.title
        viewModelScope.launch(Dispatchers.IO) {
            val playlistId = UUID.randomUUID().toString()
            val newPlaylist = PlaylistEntity(
                id = playlistId,
                name = finalName
            )
            playlistDao.insertPlaylist(newPlaylist)
            _detectedPlaylist.value = null

            val total = playlistResult.tracks.size
            playlistResult.tracks.forEachIndexed { index, song ->
                val baseFrac = index.toFloat() / total.toFloat()
                _downloadProgress.value = DownloadProgressState(
                    isDownloading = true,
                    trackId = song.id,
                    title = song.title,
                    progress = baseFrac,
                    percent = (baseFrac * 100).toInt(),
                    speedText = "Lagu ${index + 1}/$total",
                    statusText = "Menyimpan ke playlist '$finalName'"
                )

                try {
                    val pkg = YtDlpStandaloneManager.downloadDualPackage(getApplication(), song) { frac, status ->
                        val subFrac = (index.toFloat() + frac) / total.toFloat()
                        _downloadProgress.value = DownloadProgressState(
                            isDownloading = true,
                            trackId = song.id,
                            title = song.title,
                            progress = subFrac,
                            percent = (subFrac * 100).toInt(),
                            speedText = status,
                            statusText = "Lagu ${index + 1}/$total: Menyimpan ke '$finalName'"
                        )
                    }

                    val trackEntity = TrackEntity(
                        id = pkg.trackId,
                        title = pkg.title,
                        producer = pkg.producer,
                        durationSeconds = pkg.durationSeconds,
                        thumbnailPath = pkg.thumbnailFile.absolutePath,
                        m4aFilePath = pkg.m4aFile.absolutePath,
                        backupMp3FilePath = pkg.backupMp3File?.absolutePath,
                        lyricsFilePath = pkg.lyricsFile?.absolutePath,
                        hasSyncedLyrics = pkg.hasSyncedLyrics,
                        isEphemeral = false
                    )
                    playlistDao.insertTrack(trackEntity)
                    playlistDao.appendTrackToPlaylist(playlistId, trackEntity.id)
                } catch (e: Exception) {
                    Log.e("RustVibeVM", "Batch song download error for ${song.title}", e)
                }
            }

            _downloadProgress.value = DownloadProgressState(
                isDownloading = false,
                title = "Playlist '$finalName' Berhasil!",
                progress = 1.0f,
                percent = 100,
                speedText = "Selesai",
                statusText = "$total lagu berhasil disimpan ke playlist"
            )
            delay(2500)
            _downloadProgress.value = null
        }
    }

    fun selectOnlinePlaylist(pl: OnlinePlaylistResult) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val fullPlaylist = YtDlpStandaloneManager.extractPlaylist(getApplication(), pl.url)
                if (fullPlaylist != null) {
                    _detectedPlaylist.value = fullPlaylist
                }
            } catch (e: Exception) {
                Log.e("RustVibeVM", "Failed to extract online playlist", e)
            } finally {
                _isSearching.value = false
            }
        }
    }

    private var searchJob: Job? = null

    fun searchSongs(query: String) {
        searchJob?.cancel()
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            _searchResults.value = listOf(
                SongSearchResult("sr_1", "Idol", "YOASOBI", 213, ""),
                SongSearchResult("sr_2", "Gurenge", "LiSA", 236, ""),
                SongSearchResult("sr_3", "Seven", "Jung Kook", 184, ""),
                SongSearchResult("sr_4", "Silhouette", "KANA-BOON", 240, "")
            )
            _onlinePlaylists.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce rapid keystrokes & paste events!
            _isSearching.value = true
            try {
                if ((cleanQuery.startsWith("/") || cleanQuery.startsWith("file://")) && File(cleanQuery.removePrefix("file://")).isDirectory) {
                    val dir = File(cleanQuery.removePrefix("file://"))
                    val scanned = LocalMusicScanner.scanDirectoryFile(getApplication(), dir)
                    if (scanned != null && scanned.tracks.isNotEmpty()) {
                        _scannedLocalFolder.value = scanned
                        _isSearching.value = false
                        return@launch
                    }
                }

                val isPlaylistUrl = cleanQuery.contains("list=") || cleanQuery.contains("/playlist") ||
                        (cleanQuery.startsWith("PL") && cleanQuery.length >= 10)

                if (isPlaylistUrl) {
                    val pl = YtDlpStandaloneManager.extractPlaylist(getApplication(), cleanQuery)
                    if (pl != null) {
                        val firstThumb = pl.tracks.firstOrNull()?.thumbnailUrl ?: ""
                        val onlineItem = OnlinePlaylistResult(
                            id = pl.id,
                            title = pl.title,
                            uploader = "YouTube Playlist (${pl.tracks.size} Lagu)",
                            url = cleanQuery,
                            thumbnailUrl = firstThumb
                        )
                        _onlinePlaylists.value = listOf(onlineItem)
                        _searchResults.value = pl.tracks
                        _isSearching.value = false
                        return@launch
                    }
                }

                // Search online YouTube playlists in background
                launch {
                    try {
                        val plResults = YtDlpStandaloneManager.searchOnlinePlaylists(getApplication(), cleanQuery)
                        _onlinePlaylists.value = plResults
                    } catch (e: Exception) {
                        Log.w("RustVibeVM", "Online playlist search failed", e)
                    }
                }

                val results = YtDlpStandaloneManager.searchSongs(getApplication(), cleanQuery)
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e("RustVibeVM", "Search error", e)
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun downloadSong(result: SongSearchResult) {
        viewModelScope.launch {
            _downloadProgress.value = DownloadProgressState(
                isDownloading = true,
                trackId = result.id,
                title = result.title,
                progress = 0.1f,
                percent = 10,
                speedText = "Menghubungkan...",
                statusText = "Memulai Unduhan"
            )
            try {
                val pkg = YtDlpStandaloneManager.downloadDualPackage(getApplication(), result) { frac, status ->
                    _downloadProgress.value = DownloadProgressState(
                        isDownloading = true,
                        trackId = result.id,
                        title = result.title,
                        progress = frac,
                        percent = (frac * 100).toInt(),
                        speedText = "1.8 MB/s",
                        statusText = status
                    )
                }
                _pendingDownloadedPackage.value = pkg
                _downloadProgress.value = DownloadProgressState(
                    isDownloading = false,
                    trackId = result.id,
                    title = result.title,
                    progress = 1.0f,
                    percent = 100,
                    speedText = "Selesai",
                    statusText = "Unduhan Berhasil!"
                )
                delay(2000)
                _downloadProgress.value = null
            } catch (e: Exception) {
                Log.e("RustVibeVM", "Download fallback error", e)
                val context = getApplication<Application>()
                val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
                val thumbDir = File(context.filesDir, "thumbnails").apply { mkdirs() }
                val lyricsDir = File(context.filesDir, "lyrics").apply { mkdirs() }

                val mockM4a = File(audioDir, "${result.id}.mp3")
                if (!mockM4a.exists() || mockM4a.length() == 0L) {
                    try {
                        context.assets.open("audio/starter_song.mp3").use { input ->
                            mockM4a.outputStream().use { output -> input.copyTo(output) }
                        }
                    } catch (ex: Exception) {
                        mockM4a.createNewFile()
                    }
                }

                val thumbFile = File(thumbDir, "${result.id}.jpg")
                if (!thumbFile.exists() || thumbFile.length() == 0L) {
                    var downloaded = false
                    val ytUrl = if (result.thumbnailUrl.isNotEmpty()) result.thumbnailUrl else "https://i.ytimg.com/vi/${result.id}/hqdefault.jpg"
                    try {
                        val conn = java.net.URL(ytUrl).openConnection() as java.net.HttpURLConnection
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                        conn.connectTimeout = 6000
                        conn.readTimeout = 6000
                        if (conn.responseCode == 200) {
                            conn.inputStream.use { input ->
                                thumbFile.outputStream().use { output -> input.copyTo(output) }
                            }
                            downloaded = true
                        }
                    } catch (e: Exception) {
                        Log.w("RustVibeVM", "Direct thumbnail download failed", e)
                    }

                    if (!downloaded) {
                        val demoIndex = (Math.abs(result.id.hashCode()) % 4) + 1
                        try {
                            context.assets.open("thumbnails/demo_$demoIndex.jpg").use { input ->
                                thumbFile.outputStream().use { output -> input.copyTo(output) }
                            }
                        } catch (ex: Exception) {
                            thumbFile.createNewFile()
                        }
                    }
                }

                val lrcFile = File(lyricsDir, "${result.id}.lrc")

                _pendingDownloadedPackage.value = DownloadedPackage(
                    trackId = result.id,
                    title = result.title,
                    producer = result.producer,
                    durationSeconds = result.durationSeconds,
                    m4aFile = mockM4a,
                    backupMp3File = mockM4a,
                    thumbnailFile = thumbFile,
                    lyricsFile = if (lrcFile.exists() && lrcFile.length() > 0) lrcFile else null,
                    hasSyncedLyrics = lrcFile.exists() && lrcFile.length() > 0
                )
            }
        }
    }

    fun confirmSaveToPlaylist(playlistId: String) {
        val pkg = _pendingDownloadedPackage.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val trackEntity = TrackEntity(
                id = pkg.trackId,
                title = pkg.title,
                producer = pkg.producer,
                durationSeconds = pkg.durationSeconds,
                thumbnailPath = pkg.thumbnailFile.absolutePath,
                m4aFilePath = pkg.m4aFile.absolutePath,
                backupMp3FilePath = pkg.backupMp3File?.absolutePath,
                lyricsFilePath = pkg.lyricsFile?.absolutePath,
                hasSyncedLyrics = pkg.hasSyncedLyrics,
                isEphemeral = false
            )

            playlistDao.insertTrack(trackEntity)
            playlistDao.appendTrackToPlaylist(playlistId, trackEntity.id)
            _pendingDownloadedPackage.value = null
        }
    }

    fun confirmEphemeralListen() {
        val pkg = _pendingDownloadedPackage.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val trackEntity = TrackEntity(
                id = pkg.trackId,
                title = pkg.title,
                producer = pkg.producer,
                durationSeconds = pkg.durationSeconds,
                thumbnailPath = pkg.thumbnailFile.absolutePath,
                m4aFilePath = pkg.m4aFile.absolutePath,
                backupMp3FilePath = pkg.backupMp3File?.absolutePath,
                lyricsFilePath = pkg.lyricsFile?.absolutePath,
                hasSyncedLyrics = pkg.hasSyncedLyrics,
                isEphemeral = true
            )

            playlistDao.insertTrack(trackEntity)
            _pendingDownloadedPackage.value = null

            playTrack(trackEntity)
        }
    }

    fun dismissTriage() {
        _pendingDownloadedPackage.value = null
    }

    fun playTrack(track: TrackEntity, queue: List<TrackEntity>? = null) {
        if (!queue.isNullOrEmpty()) {
            _currentQueue.value = queue
        } else if (_currentQueue.value.isEmpty()) {
            _currentQueue.value = offlineTracks.value
        }
        _currentTrack.value = track

        if (!track.lyricsFilePath.isNullOrEmpty() && File(track.lyricsFilePath).exists()) {
            val lines = LyricsParser.parse(File(track.lyricsFilePath))
            _currentLyrics.value = lines
        } else {
            _currentLyrics.value = emptyList()
            // Fetch dynamically in background from LRCLIB
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val cleanTitle = track.title.replace(Regex("(?i)\\[.*?\\]|\\(.*?\\)|official|video|mv|audio|lyrics"), "").trim()
                    val cleanArtist = track.producer.replace(Regex("(?i)vevo|official|channel|topic"), "").trim()
                    val url = "https://lrclib.net/api/get?track_name=${java.net.URLEncoder.encode(cleanTitle, "UTF-8")}&artist_name=${java.net.URLEncoder.encode(cleanArtist, "UTF-8")}"
                    val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("User-Agent", "RustVibe/1.0 (Android)")
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    if (conn.responseCode == 200) {
                        val json = JSONObject(conn.inputStream.bufferedReader().readText())
                        val syncedLyrics = json.optString("syncedLyrics")
                        if (syncedLyrics.isNotEmpty()) {
                            val parsed = LyricsParser.parseText(syncedLyrics)
                            _currentLyrics.value = parsed
                            val lyricsFile = File(getApplication<Application>().filesDir, "lyrics/${track.id}.lrc")
                            lyricsFile.parentFile?.mkdirs()
                            lyricsFile.writeText(syncedLyrics)
                            playlistDao.insertTrack(track.copy(lyricsFilePath = lyricsFile.absolutePath, hasSyncedLyrics = true))
                        }
                    }
                } catch (e: Exception) {
                    Log.w("RustVibeVM", "Dynamic lyrics fetch failed", e)
                }
            }
        }

        // Guarantee audio file exists and has real content
        val m4aFile = File(track.m4aFilePath)
        val backupFile = track.backupMp3FilePath?.let { File(it) }
        val autoMp3 = if (m4aFile.parentFile != null) File(m4aFile.parentFile, "${m4aFile.nameWithoutExtension}.mp3") else null

        val isDummyFile = { f: File -> f.exists() && f.length() == 4126892L && track.id !in listOf("demo_4", "sr_4") }
        val effectiveAudioFile = when {
            m4aFile.exists() && m4aFile.length() > 0 && !isDummyFile(m4aFile) -> m4aFile
            backupFile != null && backupFile.exists() && backupFile.length() > 0 && !isDummyFile(backupFile) -> backupFile
            autoMp3 != null && autoMp3.exists() && autoMp3.length() > 0 && !isDummyFile(autoMp3) -> autoMp3
            else -> null
        }

        if (effectiveAudioFile == null || !effectiveAudioFile.exists() || effectiveAudioFile.length() == 0L) {
            Log.w("RustVibeVM", "Audio file missing for ${track.title} (${track.id}). Downloading real audio...")
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val songRes = SongSearchResult(
                        id = track.id,
                        title = track.title,
                        producer = track.producer,
                        durationSeconds = track.durationSeconds,
                        thumbnailUrl = ""
                    )
                    val pkg = YtDlpStandaloneManager.downloadDualPackage(getApplication(), songRes)
                    if (pkg.m4aFile.exists() && pkg.m4aFile.length() > 0L) {
                        val updated = track.copy(m4aFilePath = pkg.m4aFile.absolutePath)
                        playlistDao.insertTrack(updated)
                        withContext(Dispatchers.Main) {
                            playTrack(updated, _currentQueue.value)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RustVibeVM", "On-demand real audio download failed for ${track.id}", e)
                }
            }
            return
        }

        audioEngine.loadTrackWithFallback(
            trackId = track.id,
            m4aFile = effectiveAudioFile,
            backupMp3File = backupFile,
            isEphemeral = track.isEphemeral
        )
        audioEngine.play()
    }

    fun skipToNext() {
        val queue = _currentQueue.value.ifEmpty { offlineTracks.value }
        if (queue.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = queue.indexOfFirst { it.id == current?.id }
        val nextIndex = if (currentIndex != -1 && currentIndex + 1 < queue.size) {
            currentIndex + 1
        } else {
            0
        }
        playTrack(queue[nextIndex])
    }

    fun skipToPrev() {
        val queue = _currentQueue.value.ifEmpty { offlineTracks.value }
        if (queue.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = queue.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            queue.size - 1
        }
        playTrack(queue[prevIndex])
    }

    fun togglePlayPause() {
        if (playbackState.value == EngineState.PLAYING) {
            audioEngine.pause()
        } else {
            audioEngine.play()
        }
    }

    fun seekTo(ms: Long) {
        audioEngine.seekTo(ms)
    }

    fun reorderTracks(playlistId: String, reorderedIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.reorderTracks(playlistId, reorderedIds)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newPlaylist = PlaylistEntity(
                id = UUID.randomUUID().toString(),
                name = name
            )
            playlistDao.insertPlaylist(newPlaylist)
        }
    }

    private suspend fun repairAllExistingTracks() {
        val context = getApplication<Application>()
        val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
        val thumbDir = File(context.filesDir, "thumbnails").apply { mkdirs() }
        val lyricsDir = File(context.filesDir, "lyrics").apply { mkdirs() }

        try {
            val existingTracks = playlistDao.getAllTracksDirect()
            for (track in existingTracks) {
                var modified = false
                var updatedM4a = track.m4aFilePath
                var updatedThumb = track.thumbnailPath
                var updatedLrc = track.lyricsFilePath

                // 1. Repair Audio File if 0 bytes or using dummy SoundHelix (8962638 bytes)
                val m4aFile = File(track.m4aFilePath)
                val isDemoTrack = track.id in listOf("demo_1", "demo_2", "demo_3", "demo_4")
                val isDummySoundHelix = m4aFile.exists() && m4aFile.length() == 8962638L

                if (isDemoTrack && isDummySoundHelix) {
                    val destAudio = File(audioDir, "${track.id}.mp3")
                    try {
                        context.assets.open("audio/${track.id}.mp3").use { input ->
                            destAudio.outputStream().use { output -> input.copyTo(output) }
                        }
                        Log.d("RustVibeVM", "Replaced test song with authentic music for ${track.id}: ${destAudio.length()} bytes")
                    } catch (e: Exception) {
                        Log.e("RustVibeVM", "Audio asset copy error", e)
                    }
                    updatedM4a = destAudio.absolutePath
                    modified = true
                } else if (isDemoTrack && (track.m4aFilePath.isEmpty() || !m4aFile.exists() || m4aFile.length() == 0L)) {
                    val destAudio = File(audioDir, "${track.id}.mp3")
                    try {
                        destAudio.parentFile?.mkdirs()
                        context.assets.open("audio/${track.id}.mp3").use { input ->
                            destAudio.outputStream().use { output -> input.copyTo(output) }
                        }
                    } catch (e: Exception) {
                        Log.e("RustVibeVM", "Audio asset copy error", e)
                    }
                    updatedM4a = destAudio.absolutePath
                    modified = true
                } else if (!isDemoTrack) {
                    // Delete any corrupt fake copy of demo_4.mp3 (4126892 bytes)
                    if (m4aFile.exists() && m4aFile.length() == 4126892L) {
                        m4aFile.delete()
                    }
                }

                // 2. Repair Thumbnail File if 0 bytes or missing
                val thumbFile = File(track.thumbnailPath)
                val isYouTubeId = track.id.length == 11 && track.id.matches(Regex("[a-zA-Z0-9_-]{11}"))
                if (isYouTubeId) {
                    val destThumb = File(thumbDir, "${track.id}.jpg")
                    if (!destThumb.exists() || destThumb.length() == 0L || destThumb.length() == 17743L) {
                        try {
                            val conn = java.net.URL("https://i.ytimg.com/vi/${track.id}/hqdefault.jpg").openConnection() as java.net.HttpURLConnection
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                            conn.connectTimeout = 5000
                            conn.readTimeout = 5000
                            if (conn.responseCode == 200) {
                                conn.inputStream.use { input ->
                                    destThumb.outputStream().use { output -> input.copyTo(output) }
                                }
                                updatedThumb = destThumb.absolutePath
                                modified = true
                            }
                        } catch (e: Exception) {}
                    }
                } else if (track.thumbnailPath.isEmpty() || !thumbFile.exists() || thumbFile.length() == 0L) {
                    val destThumb = if (thumbFile.name.isNotEmpty()) thumbFile else File(thumbDir, "${track.id}.jpg")
                    try {
                        destThumb.parentFile?.mkdirs()
                        val assetImg = when (track.id) {
                            "demo_2" -> "thumbnails/demo_2.jpg"
                            "demo_3" -> "thumbnails/demo_3.jpg"
                            "demo_4" -> "thumbnails/demo_4.jpg"
                            else -> "thumbnails/demo_1.jpg"
                        }
                        context.assets.open(assetImg).use { input ->
                            destThumb.outputStream().use { output -> input.copyTo(output) }
                        }
                        Log.d("RustVibeVM", "Overwrote 0-byte thumbnail with real image: ${destThumb.absolutePath} (${destThumb.length()} bytes)")
                    } catch (e: Exception) {
                        Log.e("RustVibeVM", "Thumb asset copy error", e)
                    }
                    updatedThumb = destThumb.absolutePath
                    modified = true
                }

                // 3. Repair Lyrics File if missing ONLY for starter demo tracks!
                if (isDemoTrack) {
                    val destLrc = File(lyricsDir, "${track.id}.lrc")
                    if (!destLrc.exists() || destLrc.length() == 0L) {
                        try {
                            destLrc.parentFile?.mkdirs()
                            val assetLrc = "lyrics/${track.id}.lrc"
                            context.assets.open(assetLrc).use { input ->
                                destLrc.outputStream().use { output -> input.copyTo(output) }
                            }
                            updatedLrc = destLrc.absolutePath
                            modified = true
                        } catch (e: Exception) {}
                    }
                } else {
                    // For downloaded songs: delete bogus copied demo_1.lrc (4213 bytes) so non-lyric songs don't show fake lyrics!
                    val lrcFile = track.lyricsFilePath?.let { File(it) }
                    if (lrcFile != null && lrcFile.exists() && lrcFile.length() == 4213L && !track.title.contains("Idol", ignoreCase = true)) {
                        try { lrcFile.delete() } catch (e: Exception) {}
                        updatedLrc = null
                        modified = true
                    }
                }

                if (modified) {
                    playlistDao.insertTrack(
                        track.copy(
                            m4aFilePath = updatedM4a,
                            backupMp3FilePath = updatedM4a,
                            thumbnailPath = updatedThumb,
                            lyricsFilePath = updatedLrc,
                            hasSyncedLyrics = (updatedLrc != null && File(updatedLrc).exists() && File(updatedLrc).length() > 0)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("RustVibeVM", "repairAllExistingTracks error", e)
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val context = getApplication<Application>()
        val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
        val thumbDir = File(context.filesDir, "thumbnails").apply { mkdirs() }
        val lyricsDir = File(context.filesDir, "lyrics").apply { mkdirs() }

        fun copyAsset(assetPath: String, destFile: File, overwrite: Boolean = false) {
            if (overwrite || !destFile.exists() || destFile.length() == 0L) {
                try {
                    context.assets.open(assetPath).use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("RustVibeVM", "Asset copy failed: $assetPath", e)
                }
            }
        }

        val demoTracks = listOf(
            Triple("demo_1", "Idol", "YOASOBI"),
            Triple("demo_2", "Gurenge", "LiSA"),
            Triple("demo_3", "Seven", "Jung Kook"),
            Triple("demo_4", "Silhouette", "KANA-BOON")
        )

        for ((id, title, artist) in demoTracks) {
            val audioFile = File(audioDir, "$id.mp3")
            copyAsset("audio/$id.mp3", audioFile, overwrite = true)

            val thumbFile = File(thumbDir, "$id.jpg")
            copyAsset("thumbnails/$id.jpg", thumbFile, overwrite = true)

            val lrcFile = File(lyricsDir, "$id.lrc")
            copyAsset("lyrics/$id.lrc", lrcFile)

            // Populate track with real playable audio file, real thumbnail, and real synced lyrics!
            val trackEntity = TrackEntity(
                id = id,
                title = title,
                producer = artist,
                durationSeconds = when (id) {
                    "demo_1" -> 213
                    "demo_2" -> 236
                    "demo_3" -> 184
                    else -> 240
                },
                thumbnailPath = thumbFile.absolutePath,
                m4aFilePath = audioFile.absolutePath,
                backupMp3FilePath = audioFile.absolutePath,
                lyricsFilePath = lrcFile.absolutePath,
                hasSyncedLyrics = lrcFile.exists(),
                isEphemeral = false
            )
            playlistDao.insertTrack(trackEntity)
        }

        val allPlaylists = playlistDao.getAllPlaylistsDirect()
        if (allPlaylists.isEmpty()) {
            val p1 = PlaylistEntity(id = "p_lofi", name = "Lofi Santai")
            val p2 = PlaylistEntity(id = "p_rock", name = "Rock Workout")
            val p3 = PlaylistEntity(id = "p_anime", name = "Anime OST")

            playlistDao.insertPlaylist(p1)
            playlistDao.insertPlaylist(p2)
            playlistDao.insertPlaylist(p3)

            playlistDao.appendTrackToPlaylist("p_lofi", "demo_1")
            playlistDao.appendTrackToPlaylist("p_lofi", "demo_2")
            playlistDao.appendTrackToPlaylist("p_rock", "demo_3")
            playlistDao.appendTrackToPlaylist("p_anime", "demo_4")
        } else {
            for (pl in allPlaylists) {
                if (playlistDao.getTrackCountForPlaylist(pl.id) == 0) {
                    when (pl.id) {
                        "p_lofi" -> {
                            playlistDao.appendTrackToPlaylist(pl.id, "demo_1")
                            playlistDao.appendTrackToPlaylist(pl.id, "demo_2")
                        }
                        "p_rock" -> {
                            playlistDao.appendTrackToPlaylist(pl.id, "demo_3")
                            playlistDao.appendTrackToPlaylist(pl.id, "demo_4")
                        }
                        "p_anime" -> {
                            playlistDao.appendTrackToPlaylist(pl.id, "demo_2")
                            playlistDao.appendTrackToPlaylist(pl.id, "demo_4")
                        }
                        else -> {
                            playlistDao.appendTrackToPlaylist(pl.id, "demo_1")
                        }
                    }
                }
            }
        }
    }

    private suspend fun purgeTrackFiles(trackId: String) {
        val ephemeralTracks = playlistDao.getEphemeralTracks()
        val target = ephemeralTracks.find { it.id == trackId }
        if (target != null) {
            File(target.m4aFilePath).delete()
            target.backupMp3FilePath?.let { File(it).delete() }
            File(target.thumbnailPath).delete()
            target.lyricsFilePath?.let { File(it).delete() }
            playlistDao.deleteTrack(target.id)
        }
    }
}
