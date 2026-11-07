package com.rustvibe.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.rustvibe.MainActivity
import java.io.File

class RustVibeNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var mediaSession: MediaSession? = null

    var onPlayPause: (() -> Unit)? = null
    var onSkipNext: (() -> Unit)? = null
    var onSkipPrev: (() -> Unit)? = null

    companion object {
        const val CHANNEL_ID = "rustvibe_media_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_PAUSE = "com.rustvibe.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.rustvibe.ACTION_NEXT"
        const val ACTION_PREV = "com.rustvibe.ACTION_PREV"
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY_PAUSE -> {
                    onPlayPause?.invoke() ?: RustAudioEngineBridge.instance.play()
                }
                ACTION_NEXT -> {
                    onSkipNext?.invoke()
                }
                ACTION_PREV -> {
                    onSkipPrev?.invoke()
                }
            }
        }
    }

    init {
        createNotificationChannel()
        initMediaSession()
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREV)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pemutaran Musik RustVibe",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi pemutar musik dengan kontrol playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initMediaSession() {
        try {
            mediaSession = MediaSession(context, "RustVibeMediaSession").apply {
                setCallback(object : MediaSession.Callback() {
                    override fun onPlay() {
                        onPlayPause?.invoke() ?: RustAudioEngineBridge.instance.play()
                    }
                    override fun onPause() {
                        onPlayPause?.invoke() ?: RustAudioEngineBridge.instance.pause()
                    }
                    override fun onSkipToNext() {
                        onSkipNext?.invoke()
                    }
                    override fun onSkipToPrevious() {
                        onSkipPrev?.invoke()
                    }
                    override fun onSeekTo(pos: Long) {
                        RustAudioEngineBridge.instance.seekTo(pos)
                    }
                })
                isActive = true
            }
        } catch (e: Exception) {
            Log.e("RustVibeNotif", "Failed to init MediaSession", e)
        }
    }

    fun updateNotification(
        title: String,
        artist: String,
        thumbnailPath: String?,
        positionMs: Long,
        durationSeconds: Int,
        isPlaying: Boolean
    ) {
        val durationMs = durationSeconds.toLong() * 1000L

        // 1. Update MediaSession state & metadata with exact elapsedRealtime
        try {
            val session = mediaSession
            if (session != null) {
                val stateActions = PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_SEEK_TO

                val playbackState = PlaybackState.Builder()
                    .setActions(stateActions)
                    .setState(
                        if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                        positionMs,
                        if (isPlaying) 1.0f else 0.0f,
                        SystemClock.elapsedRealtime()
                    )
                    .build()
                session.setPlaybackState(playbackState)

                val artBitmap = if (!thumbnailPath.isNullOrEmpty() && File(thumbnailPath).exists()) {
                    try {
                        BitmapFactory.decodeFile(thumbnailPath)
                    } catch (e: Exception) { null }
                } else null

                val metadataBuilder = MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)

                if (artBitmap != null) {
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artBitmap)
                }

                session.setMetadata(metadataBuilder.build())
            }
        } catch (e: Exception) {
            Log.w("RustVibeNotif", "MediaSession update error", e)
        }

        // 2. Build Notification with interactive Actions & MediaStyle
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getBroadcast(
            context, 101, Intent(ACTION_PREV).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIntent = PendingIntent.getBroadcast(
            context, 102, Intent(ACTION_PLAY_PAUSE).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextIntent = PendingIntent.getBroadcast(
            context, 103, Intent(ACTION_NEXT).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        val curSec = (positionMs / 1000).toInt().coerceAtLeast(0)
        val maxSec = durationSeconds.coerceAtLeast(1)

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Jeda" else "Putar"

        notifBuilder
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setProgress(maxSec, curSec, false)
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_previous, "Sebelumnya", prevIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    playPauseIcon, playPauseTitle, playPauseIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_next, "Berikutnya", nextIntent
                ).build()
            )

        val session = mediaSession
        if (session != null) {
            notifBuilder.style = Notification.MediaStyle()
                .setMediaSession(session.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
        }

        if (!thumbnailPath.isNullOrEmpty() && File(thumbnailPath).exists()) {
            try {
                val bmp = BitmapFactory.decodeFile(thumbnailPath)
                if (bmp != null) {
                    notifBuilder.setLargeIcon(bmp)
                }
            } catch (e: Exception) {}
        }

        try {
            notificationManager.notify(NOTIFICATION_ID, notifBuilder.build())
        } catch (e: Exception) {
            Log.w("RustVibeNotif", "Failed to post notification", e)
        }
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        try {
            mediaSession?.isActive = false
        } catch (e: Exception) {}
    }

    fun cleanup() {
        cancelNotification()
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {}
        try {
            mediaSession?.release()
        } catch (e: Exception) {}
    }
}
