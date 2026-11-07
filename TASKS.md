# TASKS — Petunjuk Implementasi AI Coding Agent (Siap Tempel)

Dokumen ini berisi daftar instruksi teknis berfase lengkap dengan prompt markdown siap-tempel ke AI coding agent (Claude Code, Codex, Antigravity, dll). Seluruh prompt mencakup target file, arsitektur native Rust, binary standalone yt-dlp on-device, strategi download dual-format (M4A + MP3 backup), lirik tersinkronisasi, cover playlist kustom/grid 2x2, serta batasan ketat Material 3 Expressive (zero gradient, zero shadow, radius kapsul 9999px, palet 4 warna: `#2E1E1F`, `#E30B5C`, `#F8395A`, `#EEB3B5`).

---

## FASE 1: Mesin Audio Rust Native & Invisible Failover

### Task 1.1: Inisialisasi Crate Rust Audio Core & Symphonia
- **Deskripsi**: Membangun workspace crate Rust dengan decoder Symphonia yang mendukung file M4A (AAC) dan MP3 serta output hardware AAudio.
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Buat direktori crate Rust `rust-audio-core` di dalam root proyek Android menggunakan `cargo-ndk`.

1. Konfigurasi `Cargo.toml`:
   [package]
   name = "rust_audio_core"
   version = "0.1.0"
   edition = "2021"

   [lib]
   crate-type = ["cdylib", "staticlib"]

   [dependencies]
   symphonia = { version = "0.5.4", features = ["all"] }
   cpal = { version = "0.15.3", features = ["oboe-shared-stdcxx"] }
   uniffi = { version = "0.28.0", features = ["tokio"] }
   thiserror = "1.0"
   parking_lot = "0.12"
   crossbeam-channel = "0.5"

2. Di `src/decoder.rs`:
   - Bangun struct `AudioTrackSource` yang menerima path M4A dan path opsional MP3 backup.
   - Implementasikan fungsi pemutaran yang memprioritaskan M4A.
   - Jika saat inisialisasi pembacaan atau decoding M4A menemui error (`Error::DecodeError` atau header korup), buat mekanisme **invisible fallback** yang seketika mengalihkan decoder ke path file backup MP3 tanpa memutus alur playback.
3. Di `src/sink.rs`:
   - Buka stream low-latency via `cpal` (Oboe backend di Android).
   - Pastikan zero dynamic allocation di dalam callback pemrosesan sample audio.
4. Pastikan `cargo check` lulus tanpa error.
```

### Task 1.2: UniFFI Bridge & Timestamp Broadcaster untuk Lirik
- **Deskripsi**: Menghasilkan wrapper Kotlin untuk kontrol playback dan broadcasting posisi milidetik untuk sinkronisasi lirik.
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Konfigurasi UniFFI pada crate `rust_audio_core` untuk menghasilkan antarmuka Kotlin yang presisi.

1. Deklarasikan interface di `src/audio_engine.udl`:
   - Enum `PlaybackState`: `IDLE`, `BUFFERING`, `PLAYING`, `PAUSED`, `COMPLETED`, `ERROR`.
   - Interface `AudioEngineController`:
     - `fun loadTrackWithFallback(m4aPath: String, backupMp3Path: String?)`
     - `fun play()`
     - `fun pause()`
     - `fun seekTo(positionMs: Long)`
     - `fun getPositionMs(): Long`
     - `fun getDurationMs(): Long`
   - Callback interface `AudioEventListener`:
     - `fun onPlaybackTick(currentPositionMs: Long)`
     - `fun onStateChanged(state: PlaybackState)`
     - `fun onFallbackTriggered(reason: String)`
2. Jalankan background tick loop di Rust setiap 50ms saat status `PLAYING` yang memanggil `onPlaybackTick` ke listener Kotlin untuk menggerakkan lirik sinkron.
3. Konfigurasi Gradle plugin `org.mozilla.rust-android-gradle` untuk build `.so` ke `app/src/main/jniLibs/` (`arm64-v8a`, `armeabi-v7a`, `x86_64`).
```

---

## FASE 2: Standalone On-Device yt-dlp & Dual-Format Downloader

### Task 2.1: Standalone yt-dlp Sandbox & Lazy Initialization
- **Deskripsi**: Mengintegrasikan `youtubedl-android` untuk menjalankan binary yt-dlp murni di dalam perangkat tanpa API pihak ketiga.
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Integrasikan binary native yt-dlp ke aplikasi Android RustVibe secara standalone.

1. Tambahkan dependensi Jitpack di `build.gradle.kts`:
   implementation("com.github.yausername:youtubedl-android:0.13.2")
   // JANGAN tambahkan youtubedl-android-ffmpeg karena resolusi gagal di jitpack

2. Buat singleton manager `YtDlpStandaloneManager.kt` di package `data.remote`:
   - Lakukan inisialisasi LAZY di IO Coroutine:
     ```kotlin
     suspend fun ensureReady(context: Context) = withContext(Dispatchers.IO) {
         if (!isInitialized) {
             try {
                 YoutubeDL.getInstance().init(context.applicationContext)
                 isInitialized = true
             } catch (e: Exception) {
                 Log.e("YtDlp", "Init failed", e)
             }
         }
     }
     ```
   - Implementasikan fungsi pencarian metadata:
     `suspend fun searchSongs(query: String): List<TrackMetadata>`
     Ambil field: `id`, `title`, `uploader` (sebagai nama produser/artis), `duration`, dan `thumbnail`.
     Gunakan parameter CLI: `--dump-json --default-search "ytsearch10" --skip-download --flat-playlist --extractor-args "youtube:player_client=android,web" --no-check-certificates`.
```

### Task 2.2: Dual-Format Downloader (M4A + MP3 Backup) & Lirik Scraper
- **Deskripsi**: Mengunduh audio M4A, audio MP3 backup, cover resolusi tinggi, dan file lirik bertanda waktu secara atomik.
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Bangun fungsi unduhan komprehensif di `YtDlpStandaloneManager.kt`.

1. Fungsi `downloadFullTrackPackage(videoId: String, cacheDir: File): DownloadResult`:
   - Unduh File M4A (Prioritas Utama):
     `yt-dlp -f "bestaudio[ext=m4a]" -o "${cacheDir}/audio/${videoId}.m4a" "<url>"`
   - Unduh File MP3 Backup (Cadangan Tersembunyi):
     `yt-dlp -f "bestaudio[ext=mp3]/bestaudio" --extract-audio --audio-format mp3 -o "${cacheDir}/audio/${videoId}_backup.mp3" "<url>"`
   - Unduh Cover Album (Thumbnail):
     `yt-dlp --write-thumbnail --skip-download -o "${cacheDir}/thumbnails/${videoId}" "<url>"`
   - Unduh Lirik Tersinkronisasi:
     `yt-dlp --write-subs --write-auto-subs --sub-langs "id,en.*,all" --sub-format "lrc/vtt" --skip-download -o "${cacheDir}/lyrics/${videoId}" "<url>"`
2. Kembalikan data class:
   ```kotlin
   data class DownloadResult(
       val trackId: String,
       val m4aFile: File,
       val backupMp3File: File?,
       val thumbnailFile: File,
       val lyricsFile: File?,
       val hasSyncedLyrics: Boolean
   )
   ```
3. Pastikan di antarmuka UI lagu HANYA ditampilkan sebagai satu item berekstensi M4A. File MP3 murni disimpan di storage sebagai fallback.
```

---
## FASE 3: Database Room, Cover 2x2 & Playlist Management

### Task 3.1: Skema Database Room Dual-Path & Policy Append
- **Deskripsi**: Merancang entitas dan DAO database SQLite yang menyimpan path M4A, path MP3 backup, cover, produser, dan urutan playlist.
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Implementasikan Room Database di package `data.local`.

1. Entitas `TrackEntity`:
   - `id: String` (Primary Key, YouTube ID)
   - `title: String`
   - `producer: String` (Nama produser/channel)
   - `durationSeconds: Int`
   - `thumbnailPath: String`
   - `m4aFilePath: String`
   - `backupMp3FilePath: String?`
   - `lyricsFilePath: String?`
   - `hasSyncedLyrics: Boolean`
   - `isEphemeral: Boolean`
   - `createdAt: Long`

2. Entitas `PlaylistEntity`:
   - `id: String` (Primary Key)
   - `name: String`
   - `customCoverPath: String?` (null jika memakai auto 2x2 grid)
   - `createdAt: Long`
   - `updatedAt: Long`

3. Entitas `PlaylistTrackCrossRef`:
   - `playlistId: String`, `trackId: String`, `position: Int`, `addedAt: Long`

4. Di `PlaylistDao`:
   - Query append lagu baru selalu ke posisi paling belakang:
     ```kotlin
     @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :pId")
     suspend fun getMaxPosition(pId: String): Int?

     @Transaction
     suspend fun appendTrack(playlistId: String, trackId: String) {
         val nextPos = (getMaxPosition(playlistId) ?: -1) + 1
         insertCrossRef(PlaylistTrackCrossRef(playlistId, trackId, nextPos, System.currentTimeMillis()))
     }
     ```
   - Query 4 cover pertama untuk grid 2x2:
     ```kotlin
     @Query("""
         SELECT t.thumbnailPath FROM tracks t 
         INNER JOIN playlist_tracks pt ON t.id = pt.trackId 
         WHERE pt.playlistId = :pId 
         ORDER BY pt.position ASC LIMIT 4
     """)
     fun getFirstFourThumbnails(pId: String): Flow<List<String>>
     ```
```

### Task 3.2: Dynamic 2x2 Grid Cover & Custom Image Picker (Compose)
- **Deskripsi**: Membangun komponen antarmuka cover playlist yang menampilkan gambar kustom atau kolase 2x2 dari 4 lagu teratas.
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Bangun komponen Jetpack Compose `PlaylistCoverCard.kt` di package `ui.components`.

1. Spesifikasi Visual (Strict Zero-Shadow & M3 Expressive):
   - Ukuran: 64dp x 64dp (atau 120dp x 120dp pada header detail).
   - Corner Radius: 16dp (`RoundedCornerShape(16.dp)`).
   - Border: Solid 1dp `#603E42` (Outline).
   - DILARANG menggunakan shadow atau gradasi.

2. Logika Render Cover:
   - Jika `customCoverPath != null`: Render gambar kustom tersebut secara penuh menggunakan Coil `AsyncImage`.
   - Jika `customCoverPath == null`:
     - Ambil list `thumbnails` (maksimal 4).
     - Jika size == 4: Bagi area menjadi grid 2x2 (2 baris x 2 kolom). Setiap sel berukuran 32dp x 32dp memuat thumbnail lagu 1, 2, 3, dan 4 dengan pembatas garis 1dp `#603E42`.
     - Jika size antara 1 s/d 3: Render thumbnail lagu pertama memenuhi kartu.
     - Jika size == 0: Tampilkan ikon flat music note di atas warna `#382426`.
3. Tambahkan tombol aksi kapsul 9999px "Ubah Sampul" di layar detail playlist yang membuka photo picker Android PhotoPicker API untuk memilih gambar kustom dari galeri.
```

### Task 3.3: Layar Playlist Detail & Drag-and-Drop Reordering Track (Compose)
- **Deskripsi**: Membangun tampilan daftar lagu dalam playlist yang mendukung gestur drag-and-drop reorder secara visual dan persisten ke database Room.
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Bangun layar `PlaylistDetailScreen.kt` di package `ui.playlist` dengan dukungan drag-and-drop reordering.

1. Aturan Tampilan & Token (M3 Expressive Zero-Shadow):
   - Background Layar: `#2E1E1F` (Coffee Bean).
   - Item Kartu Track: Background `#382426`, Corner 16dp (`RoundedCornerShape(16.dp)`), Border 1dp `#603E42`.
   - DILARANG menggunakan drop shadow atau elevation saat item di-drag!
   - Highlight Dragging: Item yang sedang digeser diberi outline tegas solid 2dp `#F8395A` (Hot Fuchsia) dan sedikit scale up (1.02x).
   - Drag Handle: Ikon 6 titik vertikal di sisi kanan kartu dengan warna `#EEB3B5` (Cotton Rose).

2. Logika Drag-and-Drop Reordering:
   - Gunakan Compose LazyColumn dengan state reorderable list (`org.burnoutcrew.composereorderable:reorderable` atau Compose native dragging state).
   - Saat drag berlangsung, perbarui posisi item di memori (`mutableStateListOf`) agar animasi pergeseran lagu mulus (smooth 60fps).
   - Saat gestur drag dilepas (`onDragEnd`):
     Panggil fungsi ViewModel `reorderTracks(playlistId, updatedTrackList)` yang mengeksekusi batch transaksi SQLite:
     ```sql
     UPDATE playlist_tracks SET position = :newPosition WHERE playlist_id = :pId AND track_id = :tId;
     ```
3. Verifikasi Policy Penambahan Lagu (Append to End):
   - Tambahkan tombol Floating Action Button Kapsul 9999px di pojok kanan bawah: background `#E30B5C` (Raspberry Red), teks putih "+ Tambah Lagu".
   - Jika playlist sudah berisi misal 3 lagu (urutan 0, 1, 2), maka lagu baru yang ditambahkan WAJIB otomatis mendapatkan indeks urutan ke-4 (posisi = 3, paling belakang).
```

---

## FASE 4: Synced Lyrics, Full Player & FigCraft Automation

### Task 4.1: Synchronized Karaoke Lyrics Screen (Jetpack Compose)
- **Deskripsi**: Membangun tampilan lirik berjalan real-time dengan penanda baris aktif dan lompatan waktu saat baris ditekan (tap-to-seek).
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
Bangun layar `SyncedLyricsStage.kt` di package `ui.player`.

1. Parser LRC (`LrcParser.kt`):
   - Baca baris format `[mm:ss.xx] Teks lirik`.
   - Konversi tag waktu ke milidetik integer: `timeMs = (minutes * 60 + seconds) * 1000 + millis`.
   - Kembalikan `List<LyricLine(timeMs: Long, text: String)>`.

2. Antarmuka Compose:
   - Sambungkan state `currentPlaybackMs` dari UniFFI listener (`onPlaybackTick`).
   - Cari baris aktif menggunakan binary search atau filter: baris dengan `timeMs <= currentPlaybackMs` terakhir.
   - Styling Baris Aktif:
     - Teks: `Plus Jakarta Sans Bold`, 18sp, warna `#FFD9DF` di atas container kapsul/kotak 16dp `#6B192A`.
     - Skala teks membesar halus saat aktif.
   - Styling Baris Tidak Aktif:
     - Teks: `Plus Jakarta Sans Regular`, 14sp, warna `#EEB3B5` (Cotton Rose) dengan alpha 0.6.
   - Auto-Scroll: Gunakan `LaunchedEffect(activeLineIndex)` dengan `lazyListState.animateScrollToItem(index, scrollOffset)`.
   - Tap-to-Seek: Saat baris ditekan, panggil `audioEngine.seekTo(lyricLine.timeMs)`.
3. Pastikan ZERO shadow dan ZERO gradient di seluruh panggung lirik.
```

### Task 4.2: Floating Player, Bottom Navigation Bar & FigCraft Automation
- **Deskripsi**: Mini player kapsul mengambang, bilah navigasi bawah 4 tab, dan skrip automasi kontrol socket FigCraft (port 7979).
- **Prioritas**: High
- **Prompt Siap-Tempel**:
```markdown
1. Bangun `ExpressiveBottomNavigation.kt` & `ExpressiveFloatingPlayer.kt`:
   - Bottom Navigation Bar: Docked di dasar layar (tinggi 72dp, cornerRadius top 24dp), background `#382426`, ZERO BORDER. Memuat 4 tab: Beranda, Cari, Playlist, Profil. Indikator aktif berupa kapsul pill 9999px `#E30B5C` (Raspberry Red).
   - Floating Player: Melayang tepat di atas bottom navigation bar (tinggi 58dp, shape `RoundedCornerShape(9999.dp)`), background `#452E30`, ZERO BORDER, ZERO SHADOW.
   - Thumbnail bulat 42dp, marquee judul + nama produser (`#EEB3B5`), tombol Play bulat 42dp `#E30B5C`.

2. Buat skrip Python `scripts/figcraft_generate_ui.py` yang berkomunikasi ke `figcraft --control 7979`:
   - Buka dokumen baru jika belum ada (`file.new`).
   - Buat 7 Frame Android (360x800) latar `#2E1E1F` dengan vektor SVG murni (zero emoji) dan ZERO BORDER:
     - Frame 1: Home Screen (Semua) + 2x2 Grid Cover + Mini Player + Bottom Nav (Beranda).
     - Frame 2: Search Screen + Triage Sheet to-the-point + Bottom Nav (Cari).
     - Frame 3: Fullscreen Player + Synced Karaoke Lyrics Stage.
     - Frame 4: Playlist Detail + Drag Reorder active state + Bottom Nav (Playlist).
     - Frame 5: Koleksi Playlist Hub (2 kolom grid playlist + tombol "+ Buat Baru" 9999px).
     - Frame 6: Pustaka Lagu Offline M4A (Storage Banner + M4A file list).
     - Frame 7: Profil & Pengaturan Audio Rust (AAudio, format M4A, cache cleaner).
   - Pastikan ZERO BORDER pada seluruh elemen, NO AI-SLOP badges, dan semua ikon adalah vektor SVG.
   - Eksekusi `view.zoomFit` agar seluruh mockup terlihat proporsional di canvas.
```

---

## 3. Urutan Eksekusi Berfase (Implementation Roadmap)

1. **Fase 1 (Rust Audio Engine & Failover)**: Crate `rust_audio_core`, decoder Symphonia M4A/MP3, invisible fallback logic, dan UniFFI tick loop.
2. **Fase 2 (Standalone yt-dlp & Dual-Downloader)**: Integrasi sandbox `youtubedl-android`, download paket 4 aset (M4A + MP3 backup + cover + lirik), dan sheet triage.
3. **Fase 3 (Database, Drag Reorder & Cover 2x2)**: Room Database dual-path, policy append-to-end (`position = MAX + 1`), komponen cover playlist kustom vs grid 2x2, serta implementasi gestur drag-and-drop reorder di Jetpack Compose (Task 3.1, 3.2, 3.3).
4. **Fase 4 (Synced Lyrics & FigCraft Canvas)**: Parser LRC, layar lirik karaoke dengan tap-to-seek, floating player 9999px, dan eksekusi visual canvas FigCraft.
