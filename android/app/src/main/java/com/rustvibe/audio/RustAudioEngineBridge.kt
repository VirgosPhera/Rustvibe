package com.rustvibe.audio

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class EngineState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

class RustAudioEngineBridge private constructor() {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var tickJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _state = MutableStateFlow(EngineState.IDLE)
    val state: StateFlow<EngineState> = _state.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _isUsingBackup = MutableStateFlow(false)
    val isUsingBackup: StateFlow<Boolean> = _isUsingBackup.asStateFlow()

    var onTrackCompleted: ((trackId: String, isEphemeral: Boolean) -> Unit)? = null
    private var currentTrackId: String? = null
    private var currentIsEphemeral: Boolean = false
    private var activeAudioFile: File? = null

    init {
        try {
            System.loadLibrary("rust_audio_core")
            Log.d("RustAudioEngine", "Native librust_audio_core loaded.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w("RustAudioEngine", "Using hardware AAudio/MediaPlayer fallback", e)
        }
    }

    fun loadTrackWithFallback(
        trackId: String,
        m4aFile: File,
        backupMp3File: File?,
        isEphemeral: Boolean
    ): Boolean {
        stop()
        currentTrackId = trackId
        currentIsEphemeral = isEphemeral
        _positionMs.value = 0L

        val primaryExists = m4aFile.exists() && m4aFile.length() > 0
        val backupExists = backupMp3File != null && backupMp3File.exists() && backupMp3File.length() > 0
        val autoMp3 = if (m4aFile.parentFile != null) File(m4aFile.parentFile, "${m4aFile.nameWithoutExtension}.mp3") else null
        val autoMp3Exists = autoMp3 != null && autoMp3.exists() && autoMp3.length() > 0
        val autoBackupMp3 = if (m4aFile.parentFile != null) File(m4aFile.parentFile, "${m4aFile.nameWithoutExtension}_backup.mp3") else null
        val autoBackupMp3Exists = autoBackupMp3 != null && autoBackupMp3.exists() && autoBackupMp3.length() > 0

        activeAudioFile = when {
            primaryExists -> {
                _isUsingBackup.value = false
                m4aFile
            }
            backupExists -> {
                Log.w("RustAudioEngine", "M4A missing/unreadable. Invisible failover to MP3 backup.")
                _isUsingBackup.value = true
                backupMp3File
            }
            autoMp3Exists -> {
                Log.w("RustAudioEngine", "Auto-discovered MP3 sibling: ${autoMp3!!.name}")
                _isUsingBackup.value = true
                autoMp3
            }
            autoBackupMp3Exists -> {
                Log.w("RustAudioEngine", "Auto-discovered _backup.mp3 sibling: ${autoBackupMp3!!.name}")
                _isUsingBackup.value = true
                autoBackupMp3
            }
            else -> {
                Log.e("RustAudioEngine", "No valid audio file found for track $trackId at ${m4aFile.absolutePath}")
                _isUsingBackup.value = false
                null
            }
        }

        if (activeAudioFile != null) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(activeAudioFile!!.absolutePath)
                    prepare()
                    setOnCompletionListener {
                        triggerCompletion()
                    }
                }
                Log.d("RustAudioEngine", "MediaPlayer prepared successfully: ${activeAudioFile!!.absolutePath}")
            } catch (e: Exception) {
                Log.e("RustAudioEngine", "MediaPlayer prep failed, falling back to backup", e)
                if (backupExists && activeAudioFile != backupMp3File) {
                    try {
                        activeAudioFile = backupMp3File
                        _isUsingBackup.value = true
                        mediaPlayer = MediaPlayer().apply {
                            setAudioAttributes(
                                android.media.AudioAttributes.Builder()
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )
                            setDataSource(backupMp3File!!.absolutePath)
                            prepare()
                            setOnCompletionListener {
                                triggerCompletion()
                            }
                        }
                    } catch (e2: Exception) {
                        Log.e("RustAudioEngine", "Backup MP3 also failed", e2)
                        activeAudioFile = null
                    }
                } else {
                    activeAudioFile = null
                }
            }
        }

        _state.value = EngineState.PAUSED
        return true
    }

    fun play() {
        if (_state.value == EngineState.PLAYING) return
        _state.value = EngineState.PLAYING

        if (mediaPlayer != null) {
            try {
                mediaPlayer?.start()
            } catch (e: Exception) {
                Log.e("RustAudioEngine", "MediaPlayer start error", e)
            }
        }
        startTickLoop()
    }

    fun pause() {
        if (_state.value == EngineState.PLAYING) {
            _state.value = EngineState.PAUSED
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                Log.e("RustAudioEngine", "MediaPlayer pause error", e)
            }
            stopTickLoop()
        }
    }

    fun stop() {
        _state.value = EngineState.IDLE
        _positionMs.value = 0L
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        stopTickLoop()
    }

    fun seekTo(targetMs: Long) {
        _positionMs.value = targetMs
        mediaPlayer?.seekTo(targetMs.toInt())
    }

    private fun startTickLoop() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive && _state.value == EngineState.PLAYING) {
                delay(50)
                try {
                    val mp = mediaPlayer
                    if (mp != null && mp.isPlaying) {
                        _positionMs.value = mp.currentPosition.toLong()
                    } else {
                        _positionMs.value += 50
                    }
                } catch (e: Exception) {
                    // Safe guard against MediaPlayer state transition races
                }
            }
        }
    }

    private fun stopTickLoop() {
        tickJob?.cancel()
        tickJob = null
    }

    fun triggerCompletion() {
        _state.value = EngineState.COMPLETED
        stop()
        val tid = currentTrackId
        if (tid != null) {
            onTrackCompleted?.invoke(tid, currentIsEphemeral)
        }
    }

    companion object {
        val instance: RustAudioEngineBridge by lazy { RustAudioEngineBridge() }
    }
}
