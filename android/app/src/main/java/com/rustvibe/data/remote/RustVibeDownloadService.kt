package com.rustvibe.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rustvibe.MainActivity
import com.rustvibe.data.local.PlaylistEntity
import com.rustvibe.data.local.RustVibeDatabase
import com.rustvibe.data.local.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class RustVibeDownloadService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var downloadJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RustVibe::DownloadWakeLock").apply {
            setReferenceCounted(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val playlistName = intent.getStringExtra(EXTRA_PLAYLIST_NAME) ?: "Playlist Baru"
                val playlistId = intent.getStringExtra(EXTRA_PLAYLIST_ID)
                @Suppress("UNCHECKED_CAST", "DEPRECATION")
                val songs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_SONG_LIST, ArrayList::class.java) as? ArrayList<SongSearchResult> ?: arrayListOf()
                } else {
                    intent.getSerializableExtra(EXTRA_SONG_LIST) as? ArrayList<SongSearchResult> ?: arrayListOf()
                }
                startPlaylistDownload(playlistName, songs, playlistId)
            }
            ACTION_CANCEL_DOWNLOAD -> {
                cancelCurrentDownload()
            }
        }
        return START_NOT_STICKY
    }

    private fun startPlaylistDownload(
        playlistName: String,
        songs: List<SongSearchResult>,
        existingPlaylistId: String?
    ) {
        downloadJob?.cancel()

        // Acquire WakeLock for up to 60 minutes
        try {
            wakeLock?.acquire(60 * 60 * 1000L)
            Log.d(TAG, "Acquired WakeLock for background playlist download")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire WakeLock", e)
        }

        val initialNotif = buildNotification(
            title = "Mengunduh Playlist '$playlistName'",
            content = "Menyiapkan ${songs.size} lagu...",
            progress = 0,
            maxProgress = songs.size,
            indeterminate = true
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else 0
            startForeground(NOTIFICATION_ID, initialNotif, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, initialNotif)
        }

        downloadJob = serviceScope.launch {
            val db = RustVibeDatabase.getInstance(applicationContext)
            val playlistDao = db.playlistDao()

            val targetPlaylistId = if (!existingPlaylistId.isNullOrEmpty()) {
                existingPlaylistId
            } else {
                val newId = UUID.randomUUID().toString()
                playlistDao.insertPlaylist(PlaylistEntity(id = newId, name = playlistName))
                newId
            }

            val total = songs.size
            var successCount = 0
            val failedTracks = mutableListOf<SongSearchResult>()

            _downloadProgress.value = DownloadProgressState(
                isDownloading = true,
                title = playlistName,
                progress = 0f,
                percent = 0,
                speedText = "0/$total Lagu",
                statusText = "Memulai unduhan latar belakang..."
            )

            songs.forEachIndexed { index, song ->
                if (!isActive) return@forEachIndexed

                val baseFraction = index.toFloat() / total.toFloat()
                _downloadProgress.value = DownloadProgressState(
                    isDownloading = true,
                    trackId = song.id,
                    title = song.title,
                    progress = baseFraction,
                    percent = (baseFraction * 100).toInt(),
                    speedText = "Lagu ${index + 1}/$total",
                    statusText = "Menyimpan ke playlist '$playlistName'"
                )

                updateNotification(
                    title = "Mengunduh: $playlistName",
                    content = "Lagu ${index + 1}/$total: ${song.title}",
                    progress = index,
                    maxProgress = total,
                    indeterminate = false,
                    subText = "${((index.toFloat() / total.toFloat()) * 100).toInt()}%"
                )

                try {
                    val pkg = YtDlpStandaloneManager.downloadDualPackage(applicationContext, song) { frac, status ->
                        val subFraction = (index.toFloat() + frac) / total.toFloat()
                        _downloadProgress.value = DownloadProgressState(
                            isDownloading = true,
                            trackId = song.id,
                            title = song.title,
                            progress = subFraction,
                            percent = (subFraction * 100).toInt(),
                            speedText = status,
                            statusText = "Lagu ${index + 1}/$total: $status"
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
                    playlistDao.appendTrackToPlaylist(targetPlaylistId, trackEntity.id)
                    successCount++
                    Log.d(TAG, "Saved track in background: ${song.title} (${index + 1}/$total)")
                } catch (e: Exception) {
                    failedTracks.add(song)
                    Log.e(TAG, "Error downloading ${song.title} in background", e)
                }

                // Small polite delay between downloads to prevent YouTube IP rate limits
                delay(350)
            }

            _downloadProgress.value = null

            if (failedTracks.isNotEmpty()) {
                _downloadResultState.value = DownloadResultDialogState(
                    playlistId = targetPlaylistId,
                    playlistName = playlistName,
                    successCount = successCount,
                    failedCount = failedTracks.size,
                    failedSongs = failedTracks
                )

                updateNotification(
                    title = "Unduhan Selesai Sebagian",
                    content = "$successCount lagu tersimpan, ${failedTracks.size} gagal",
                    progress = total,
                    maxProgress = total,
                    indeterminate = false
                )
            } else {
                _downloadProgress.value = DownloadProgressState(
                    isDownloading = false,
                    title = "Playlist '$playlistName' Selesai!",
                    progress = 1.0f,
                    percent = 100,
                    speedText = "Selesai",
                    statusText = "Semua $successCount lagu berhasil disimpan ke playlist"
                )

                updateNotification(
                    title = "Unduhan Selesai",
                    content = "Semua $successCount lagu berhasil disimpan ke '$playlistName'",
                    progress = total,
                    maxProgress = total,
                    indeterminate = false
                )
            }

            // Release wake lock and gracefully finish foreground service
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing WakeLock", e)
            }

            delay(4000)
            _downloadProgress.value = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun cancelCurrentDownload() {
        downloadJob?.cancel()
        _downloadProgress.value = null
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing WakeLock", e)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RustVibe Unduhan",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi progres unduhan playlist dan lagu di latar belakang"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        content: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean,
        subText: String? = null
    ): android.app.Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setProgress(maxProgress, progress, indeterminate)
            .setContentIntent(createPendingIntent())
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Batalkan",
                createCancelPendingIntent()
            )

        if (!subText.isNullOrEmpty()) {
            builder.setSubText(subText)
        }
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
        return builder.build()
    }

    private fun updateNotification(
        title: String,
        content: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean,
        subText: String? = null
    ) {
        val notif = buildNotification(title, content, progress, maxProgress, indeterminate, subText)
        notificationManager.notify(NOTIFICATION_ID, notif)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createCancelPendingIntent(): PendingIntent {
        val intent = Intent(this, RustVibeDownloadService::class.java).apply {
            action = ACTION_CANCEL_DOWNLOAD
        }
        return PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing WakeLock in onDestroy", e)
        }
    }

    companion object {
        private const val TAG = "RustVibeDownloadSvc"
        private const val CHANNEL_ID = "rustvibe_download_channel_v2"
        private const val NOTIFICATION_ID = 1002

        const val ACTION_START_DOWNLOAD = "com.rustvibe.action.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.rustvibe.action.CANCEL_DOWNLOAD"

        const val EXTRA_PLAYLIST_NAME = "extra_playlist_name"
        const val EXTRA_PLAYLIST_ID = "extra_playlist_id"
        const val EXTRA_SONG_LIST = "extra_song_list"

        private val _downloadProgress = MutableStateFlow<DownloadProgressState?>(null)
        val downloadProgress = _downloadProgress.asStateFlow()

        private val _downloadResultState = MutableStateFlow<DownloadResultDialogState?>(null)
        val downloadResultState = _downloadResultState.asStateFlow()

        fun clearResultState() {
            _downloadResultState.value = null
        }

        fun startDownload(
            context: Context,
            playlistName: String,
            songs: List<SongSearchResult>,
            playlistId: String? = null
        ) {
            val intent = Intent(context, RustVibeDownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_PLAYLIST_NAME, playlistName)
                putExtra(EXTRA_PLAYLIST_ID, playlistId)
                putExtra(EXTRA_SONG_LIST, ArrayList(songs))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun retryFailed(
            context: Context,
            playlistId: String,
            playlistName: String,
            failedSongs: List<SongSearchResult>
        ) {
            _downloadResultState.value = null
            startDownload(
                context = context,
                playlistName = playlistName,
                songs = failedSongs,
                playlistId = playlistId
            )
        }

        fun cancelDownload(context: Context) {
            val intent = Intent(context, RustVibeDownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            context.startService(intent)
        }
    }
}
