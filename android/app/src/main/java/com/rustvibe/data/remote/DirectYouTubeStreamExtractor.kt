package com.rustvibe.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
 * Multi-client Native Kotlin extractor that bypasses YouTube botguard and precondition checks
 * by utilizing official VISIONOS and ANDROID_VR client contexts with session refresh.
 *
 * Extracts direct high-bitrate M4A/AAC/Opus streams directly from googlevideo servers
 * without relying on outdated Python/yt-dlp binaries.
 */
object DirectYouTubeStreamExtractor {
    private const val TAG = "DirectYTExtractor"

    private const val USER_AGENT_VISIONOS =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15"

    private const val USER_AGENT_ANDROID_VR =
        "Mozilla/5.0 (Linux; Android 12; Quest 2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

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
     * Initializes cookies and session visitor data from YouTube if expired or forced.
     */
    suspend fun ensureSessionInitialized(forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedVisitorId != null && cachedCookies != null && (now - lastInitTimestamp) < 3600_000L) {
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
            val visitorPattern = Pattern.compile("\"visitorData\":\\s*\"([^\"]+)\"")
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

    private data class ClientConfig(
        val name: String,
        val version: String,
        val clientNumber: String,
        val userAgent: String,
        val deviceMake: String,
        val deviceModel: String,
        val osName: String,
        val osVersion: String
    )

    private val SUPPORTED_CLIENTS = listOf(
        ClientConfig(
            name = "VISIONOS",
            version = "1.02",
            clientNumber = "101",
            userAgent = USER_AGENT_VISIONOS,
            deviceMake = "Apple",
            deviceModel = "RealityDevice17,1",
            osName = "visionOS",
            osVersion = "26.5.23O471"
        ),
        ClientConfig(
            name = "ANDROID_VR",
            version = "1.59.21",
            clientNumber = "28",
            userAgent = USER_AGENT_ANDROID_VR,
            deviceMake = "Oculus",
            deviceModel = "Quest 2",
            osName = "Android",
            osVersion = "12"
        )
    )

    /**
     * Resolves the direct audio download stream URL for a given video ID with multi-client fallback.
     */
    suspend fun resolveAudioStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        ensureSessionInitialized()

        for (client in SUPPORTED_CLIENTS) {
            val url = queryPlayerWithClient(videoId, client)
            if (!url.isNullOrEmpty()) {
                return@withContext url
            }
        }

        // If both clients failed, force refresh visitor session once and retry primary client
        Log.w(TAG, "All clients failed for $videoId. Refreshing visitor session and retrying...")
        ensureSessionInitialized(forceRefresh = true)
        val retryUrl = queryPlayerWithClient(videoId, SUPPORTED_CLIENTS[0])
        return@withContext retryUrl
    }

    private suspend fun queryPlayerWithClient(videoId: String, client: ClientConfig): String? = withContext(Dispatchers.IO) {
        try {
            val endpoint = URL("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 12000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", client.userAgent)
                setRequestProperty("X-YouTube-Client-Name", client.clientNumber)
                setRequestProperty("X-YouTube-Client-Version", client.version)
                setRequestProperty("Origin", "https://www.youtube.com")
                cachedVisitorId?.let { setRequestProperty("X-Goog-Visitor-Id", it) }
                cachedCookies?.let { setRequestProperty("Cookie", it) }
            }

            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", client.name)
                        put("clientVersion", client.version)
                        put("deviceMake", client.deviceMake)
                        put("deviceModel", client.deviceModel)
                        put("userAgent", client.userAgent)
                        put("osName", client.osName)
                        put("osVersion", client.osVersion)
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
                Log.w(TAG, "Player endpoint returned code ${conn.responseCode} with client ${client.name} for $videoId")
                return@withContext null
            }

            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val json = JSONObject(responseText)
            val playability = json.optJSONObject("playabilityStatus")
            val status = playability?.optString("status")

            if (status != "OK") {
                Log.w(TAG, "Client ${client.name} playability status=$status reason=${playability?.optString("reason")} for $videoId")
                return@withContext null
            }

            val streamingData = json.optJSONObject("streamingData") ?: return@withContext null
            val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats") ?: return@withContext null

            var urlM4a140: String? = null
            var urlOpus251: String? = null
            var urlM4a139: String? = null
            var urlOpus250: String? = null
            var urlAnyAudio: String? = null

            for (i in 0 until adaptiveFormats.length()) {
                val format = adaptiveFormats.getJSONObject(i)
                val itag = format.optInt("itag")
                val mimeType = format.optString("mimeType")
                val directUrl = format.optString("url")

                if (directUrl.isNotEmpty()) {
                    when (itag) {
                        140 -> urlM4a140 = directUrl
                        251 -> urlOpus251 = directUrl
                        139 -> urlM4a139 = directUrl
                        250 -> urlOpus250 = directUrl
                        else -> {
                            if (mimeType.contains("audio") && urlAnyAudio == null) {
                                urlAnyAudio = directUrl
                            }
                        }
                    }
                }
            }

            val selected = urlM4a140 ?: urlOpus251 ?: urlM4a139 ?: urlOpus250 ?: urlAnyAudio
            if (selected != null) {
                Log.d(TAG, "Resolved audio stream with ${client.name} for $videoId (M4A140=${urlM4a140 != null})")
            }
            return@withContext selected
        } catch (e: Exception) {
            Log.e(TAG, "Error querying player API with client ${client.name} for $videoId", e)
            return@withContext null
        }
    }

    /**
     * Downloads audio directly from GoogleVideo to destination file with progressive speed and byte tracking.
     * Includes automatic 1-time retry if network socket terminates prematurely.
     */
    suspend fun downloadDirectAudio(
        streamUrl: String,
        destinationFile: File,
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        destinationFile.parentFile?.mkdirs()
        val tempFile = File(destinationFile.absolutePath + ".downloading")

        for (attempt in 1..2) {
            try {
                if (tempFile.exists()) tempFile.delete()

                val url = URL(streamUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 25000
                    setRequestProperty("User-Agent", USER_AGENT_VISIONOS)
                    instanceFollowRedirects = true
                }

                if (conn.responseCode !in 200..299) {
                    Log.w(TAG, "Stream download returned HTTP ${conn.responseCode} on attempt $attempt")
                    if (attempt < 2) {
                        delay(500)
                        continue
                    }
                    return@withContext false
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
                    Log.d(TAG, "Successfully downloaded direct audio: ${destinationFile.absolutePath} (${destinationFile.length()} bytes)")
                    return@withContext true
                } else {
                    tempFile.delete()
                    Log.w(TAG, "File too small (<50KB): ${tempFile.length()} bytes on attempt $attempt")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Download attempt $attempt failed: ${e.message}")
                if (attempt < 2) {
                    delay(800)
                }
            }
        }

        if (tempFile.exists()) tempFile.delete()
        return@withContext false
    }
}
