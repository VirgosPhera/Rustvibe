package com.rustvibe.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

/**
 * DirectYouTubeStreamExtractor
 *
 * Native pure-Kotlin extractor that bypasses YouTube botguard and precondition checks
 * by utilizing the official VISIONOS client context.
 *
 * Extracts direct high-bitrate M4A/AAC streams directly from googlevideo servers
 * without relying on outdated Python/yt-dlp binaries.
 */
object DirectYouTubeStreamExtractor {
    private const val TAG = "DirectYTExtractor"

    private const val USER_AGENT_VISIONOS =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15"

    private const val USER_AGENT_DESKTOP =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

    @Volatile
    private var cachedVisitorId: String? = null

    @Volatile
    private var cachedCookies: String? = null

    @Volatile
    private var cachedSignatureTimestamp: Int = 20760

    @Volatile
    private var lastInitTimestamp: Long = 0L

    /**
     * Initializes cookies and session visitor data from YouTube if expired or not yet loaded.
     */
    private suspend fun ensureSessionInitialized() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedVisitorId != null && cachedCookies != null && (now - lastInitTimestamp) < 3600_000L) {
            return@withContext
        }

        try {
            val url = URL("https://www.youtube.com")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", USER_AGENT_DESKTOP)
                setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                instanceFollowRedirects = true
            }

            // Extract cookies
            val cookieHeaders = conn.headerFields["Set-Cookie"]
            val cookieBuilder = StringBuilder()
            cookieHeaders?.forEach { cookie ->
                val cookieVal = cookie.substringBefore(";")
                if (cookieBuilder.isNotEmpty()) cookieBuilder.append("; ")
                cookieBuilder.append(cookieVal)
            }
            cachedCookies = cookieBuilder.toString()

            val html = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            // 1. Extract visitorData
            val visitorPattern = Pattern.compile("\"visitorData\":\"([^\"]+)\"")
            val visitorMatcher = visitorPattern.matcher(html)
            if (visitorMatcher.find()) {
                cachedVisitorId = visitorMatcher.group(1)
            }

            // 2. Extract base.js to get signatureTimestamp
            val jsPattern = Pattern.compile("(\"/s/player/[a-zA-Z0-9_-]+/player_ias\\.vflset/[a-zA-Z0-9_/-]+/base\\.js\")")
            val jsMatcher = jsPattern.matcher(html)
            val jsMatched = if (jsMatcher.find()) jsMatcher.group(1) else null
            val jsUrlStr = if (jsMatched != null) {
                "https://www.youtube.com" + jsMatched.replace("\"", "")
            } else {
                "https://www.youtube.com/s/player/a5c2aafe/player_ias.vflset/en_US/base.js"
            }

            try {
                val jsConn = (URL(jsUrlStr).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("User-Agent", USER_AGENT_DESKTOP)
                }
                val jsCode = jsConn.inputStream.bufferedReader().use { it.readText() }
                jsConn.disconnect()

                val stsPattern = Pattern.compile("signatureTimestamp:([0-9]+)")
                val stsMatcher = stsPattern.matcher(jsCode)
                if (stsMatcher.find()) {
                    stsMatcher.group(1)?.toIntOrNull()?.let {
                        cachedSignatureTimestamp = it
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse signatureTimestamp, using fallback: $cachedSignatureTimestamp", e)
            }

            lastInitTimestamp = now
            Log.d(TAG, "YouTube session initialized: visitorId=${cachedVisitorId?.take(15)}..., sts=$cachedSignatureTimestamp")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing YouTube session", e)
        }
    }

    /**
     * Resolves the direct audio download stream URL for a given video ID.
     */
    suspend fun resolveAudioStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        ensureSessionInitialized()

        try {
            val endpoint = URL("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 12000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", USER_AGENT_VISIONOS)
                setRequestProperty("X-Youtube-Client-Name", "101")
                setRequestProperty("X-Youtube-Client-Version", "1.02")
                setRequestProperty("Origin", "https://www.youtube.com")
                cachedVisitorId?.let { setRequestProperty("X-Goog-Visitor-Id", it) }
                cachedCookies?.let { setRequestProperty("Cookie", it) }
            }

            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "VISIONOS")
                        put("clientVersion", "1.02")
                        put("deviceMake", "Apple")
                        put("deviceModel", "RealityDevice17,1")
                        put("userAgent", USER_AGENT_VISIONOS)
                        put("osName", "visionOS")
                        put("osVersion", "26.5.23O471")
                        put("hl", "en")
                        put("timeZone", "UTC")
                        put("utcOffsetMinutes", 0)
                    })
                })
                put("videoId", videoId)
                put("playbackContext", JSONObject().apply {
                    put("contentPlaybackContext", JSONObject().apply {
                        put("html5Preference", "HTML5_PREF_WANTS")
                        put("signatureTimestamp", cachedSignatureTimestamp)
                    })
                })
                put("contentCheckOk", true)
                put("racyCheckOk", true)
            }

            conn.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            if (conn.responseCode != 200) {
                Log.w(TAG, "Player endpoint returned code ${conn.responseCode} for $videoId")
                return@withContext null
            }

            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val json = JSONObject(responseText)
            val streamingData = json.optJSONObject("streamingData") ?: return@withContext null
            val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats") ?: return@withContext null

            var selectedM4aUrl: String? = null
            var selectedOpusUrl: String? = null

            for (i in 0 until adaptiveFormats.length()) {
                val format = adaptiveFormats.getJSONObject(i)
                val itag = format.optInt("itag")
                val mimeType = format.optString("mimeType")
                val url = format.optString("url")

                if (url.isNotEmpty()) {
                    // itag 140 is standard M4A / AAC 128kbps
                    if (itag == 140) {
                        selectedM4aUrl = url
                        break
                    }
                    // itag 139 is M4A 48kbps fallback
                    if (itag == 139 && selectedM4aUrl == null) {
                        selectedM4aUrl = url
                    }
                    // itag 251 is WebM Opus 160kbps
                    if (itag == 251) {
                        selectedOpusUrl = url
                    }
                }
            }

            val streamUrl = selectedM4aUrl ?: selectedOpusUrl
            if (streamUrl != null) {
                Log.d(TAG, "Resolved direct audio stream for $videoId (M4A=${selectedM4aUrl != null})")
            } else {
                Log.w(TAG, "No direct audio URL with 'url' field found for $videoId in streamingData")
            }
            return@withContext streamUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving audio stream for $videoId", e)
            return@withContext null
        }
    }

    /**
     * Downloads audio directly from GoogleVideo to destination file with progressive speed and byte tracking.
     */
    suspend fun downloadDirectAudio(
        streamUrl: String,
        destinationFile: File,
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            destinationFile.parentFile?.mkdirs()
            val tempFile = File(destinationFile.absolutePath + ".downloading")
            if (tempFile.exists()) tempFile.delete()

            val url = URL(streamUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 20000
                setRequestProperty("User-Agent", USER_AGENT_VISIONOS)
                instanceFollowRedirects = true
            }

            val totalBytes = conn.contentLengthLong
            val inputStream = conn.inputStream
            val outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(32 * 1024)
            var bytesRead: Int
            var totalRead = 0L
            var lastUpdate = System.currentTimeMillis()
            var lastBytes = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastUpdate >= 250) {
                    val progress = if (totalBytes > 0) totalRead.toFloat() / totalBytes else 0.5f
                    val deltaSec = (now - lastUpdate) / 1000.0f
                    val speedMBs = if (deltaSec > 0) ((totalRead - lastBytes) / (1024.0f * 1024.0f)) / deltaSec else 0f
                    val speedText = String.format("%.1f MB/s", speedMBs)

                    onProgress(progress.coerceIn(0.05f, 0.95f), "Mengunduh Aliran Audio ($speedText)...")
                    lastUpdate = now
                    lastBytes = totalRead
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            conn.disconnect()

            if (tempFile.length() > 50_000L) {
                if (destinationFile.exists()) destinationFile.delete()
                tempFile.renameTo(destinationFile)
                Log.d(TAG, "Successfully downloaded audio direct: ${destinationFile.absolutePath} (${destinationFile.length()} bytes)")
                return@withContext true
            } else {
                tempFile.delete()
                Log.w(TAG, "Downloaded audio file too small (<50KB): ${tempFile.length()} bytes")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading audio stream directly", e)
            return@withContext false
        }
    }
}
