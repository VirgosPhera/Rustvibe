package com.rustvibe.audio

import java.io.File
import java.util.regex.Pattern

data class LyricLine(
    val timeMs: Long,
    val text: String
)

object LyricsParser {
    // Regex for standard LRC timestamps: [mm:ss.xx] or [mm:ss:xx] or [mm:ss]
    private val LRC_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\](.*)")

    fun parse(file: File?): List<LyricLine> {
        if (file == null || !file.exists()) return emptyList()
        return parseText(file.readText())
    }

    fun parseText(content: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        try {
            content.lineSequence().forEach { line ->
                val trimmed = line.trim()
                val matcher = LRC_PATTERN.matcher(trimmed)
                if (matcher.find()) {
                    val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                    val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                    val fractionStr = matcher.group(3) ?: "0"
                    val millis = when (fractionStr.length) {
                        1 -> fractionStr.toLong() * 100
                        2 -> fractionStr.toLong() * 10
                        else -> fractionStr.take(3).toLong()
                    }
                    val totalMs = (minutes * 60 + seconds) * 1000 + millis
                    val text = matcher.group(4)?.trim().orEmpty()

                    if (text.isNotEmpty()) {
                        lines.add(LyricLine(timeMs = totalMs, text = text))
                    }
                }
            }

            // Fallback for plain text lyrics without timestamps
            if (lines.isEmpty() && content.isNotBlank()) {
                var currentMs = 0L
                content.lineSequence().forEach { raw ->
                    val t = raw.trim()
                    if (t.isNotEmpty() && !t.startsWith("[ti:") && !t.startsWith("[ar:") && !t.startsWith("[al:") && !t.startsWith("[by:")) {
                        lines.add(LyricLine(timeMs = currentMs, text = t))
                        currentMs += 4000L // 4 seconds per line
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lines.sortedBy { it.timeMs }
    }

    /**
     * Binary search to find the active line index given the current playback millisecond.
     * Returns -1 if playback is before the first line.
     */
    fun findActiveLineIndex(lyrics: List<LyricLine>, currentMs: Long): Int {
        if (lyrics.isEmpty() || currentMs < lyrics.first().timeMs) return -1

        var low = 0
        var high = lyrics.size - 1
        var bestIndex = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lyrics[mid].timeMs <= currentMs) {
                bestIndex = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return bestIndex
    }
}
