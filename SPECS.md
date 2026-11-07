# SPECS — Spesifikasi Teknis Rinci RustVibe (Fitur F-01 s/d F-06)

Dokumen ini memuat spesifikasi teknis 4-bagian (Tujuan, Detail Implementasi, Aturan & Batasan, Kriteria Penerimaan) untuk setiap fitur utama dan sub-fiturnya. Seluruh spesifikasi mematuhi batasan Material 3 Expressive (zero gradient, zero shadow, radius kapsul 9999px, palet 4 warna: `#2E1E1F`, `#E30B5C`, `#F8395A`, `#EEB3B5`), download dual-format M4A+MP3 backup, ekstraksi produser & cover, lirik tersinkronisasi, serta cover playlist kustom / grid 2x2.

---

## F-01: Mesin Audio Rust Native & Failover

### 1. Tujuan
Menyediakan engine pemutar audio native berlatar belakang di Android tanpa overhead Garbage Collection, dengan dukungan failover otomatis yang tidak terlihat (*invisible fallback*) dari file M4A ke file MP3 cadangan bila terjadi decode error, serta broadcasting posisi playback presisi untuk sinkronisasi lirik.

### 2. Detail Implementasi
- **Sub-Fitur F-01.1 (Symphonia Decoder Core)**:
  - Menginisialisasi decoder Symphonia dengan format reader untuk M4A (AAC-LC/HE-AAC) dan MP3 (MPEG-1 Audio Layer III).
  - Membaca sample audio langsung ke ring buffer thread-safe tanpa dynamic allocation per-frame.
- **Sub-Fitur F-01.2 (AAudio Sink via Oboe)**:
  - Membuka native audio stream `Oboe` dengan `SharingMode::Exclusive`, low-latency performance mode, sample rate 44100Hz/48000Hz stereo.
- **Sub-Fitur F-01.3 (UniFFI Bridge & State Protocol)**:
  ```rust
  #[uniffi::export]
  pub fn engine_load_with_fallback(m4a_path: String, backup_mp3_path: Option<String>) -> Result<(), AudioError>;
  #[uniffi::export]
  pub fn engine_play();
  #[uniffi::export]
  pub fn engine_pause();
  #[uniffi::export]
  pub fn engine_seek(position_ms: u64);
  #[uniffi::export]
  pub fn engine_get_position_ms() -> u64;
  ```
- **Sub-Fitur F-01.4 (Seamless Invisible Failover Engine)**:
  - Saat `engine_load_with_fallback` dipanggil, engine pertama kali mencoba membuka dan memvalidasi header file M4A.
  - Jika Symphonia mengembalikan `Error::DecodeError` atau stream data M4A terputus, engine TIDAK memunculkan error dialog ke UI, melainkan langsung beralih ke `backup_mp3_path` secara instan, menyelaraskan posisi milidetik terakhir.
- **Sub-Fitur F-01.5 (High-Precision Timestamp Broadcaster)**:
  - Rust engine memancarkan event `onPlaybackTick(position_ms)` setiap interval 50ms melalui callback non-blocking ke Kotlin untuk menggerakkan scroll lirik karaoke.

### 3. Aturan & Batasan
- Failover dari M4A ke MP3 harus selesai dalam waktu < 200ms tanpa terdengar suara 'pop' atau 'click' pada speaker/headphone (gunakan micro-fade 10ms saat transisi).
- Dilarang mengalokasikan memory heap di dalam callback render audio AAudio.

### 4. Kriteria Penerimaan
- Jika file M4A sengaja dirusak (corrupted test file), lagu tetap langsung berputar mulus menggunakan file MP3 cadangannya.
- Event posisi waktu berjalan lancar dengan drift toleransi < 20ms terhadap wall-clock time.

---

## F-02: Standalone On-Device yt-dlp & Media Scraper

### 1. Tujuan
Mengekstrak file audio ganda (M4A + MP3 backup), cover album lokal, nama produser/channel, dan file lirik bertanda waktu secara langsung dari dalam perangkat Android tanpa bergantung pada proxy cloud atau API eksternal.

### 2. Detail Implementasi
- **Sub-Fitur F-02.1 (On-Device Python + yt-dlp Sandbox)**:
  - Menggunakan library `com.github.yausername:youtubedl-android:0.13.2`.
  - Binary Python runtime dan yt-dlp executable diekstrak ke private storage `/data/user/0/com.rustvibe/no_backup/youtubedl`.
  - Lazy initialization dijalankan di background thread dengan try-catch sebelum request pertama.
- **Sub-Fitur F-02.2 (Fast Search & Producer Metadata Extractor)**:
  - Menjalankan pencarian metadata:
    `yt-dlp "ytsearch10:<query>" --dump-json --skip-download --flat-playlist --extractor-args "youtube:player_client=android,web" --no-check-certificates`
  - Mengekstrak field: `id`, `title`, `uploader` (nama produser/artis), `duration`, `thumbnail`.
- **Sub-Fitur F-02.3 (Dual-Format Downloader M4A + MP3)**:
  - File Utama (M4A): `yt-dlp -f "bestaudio[ext=m4a]" -o "/data/.../audio/%(id)s.m4a" "<url>"`
  - File Cadangan (MP3): `yt-dlp -f "bestaudio[ext=mp3]/bestaudio" --extract-audio --audio-format mp3 -o "/data/.../audio/%(id)s_backup.mp3" "<url>"`
  - Keduanya diunduh secara sekuensial/paralel; hanya file M4A yang diregistrasikan ke tampilan daftar lagu pengguna.
- **Sub-Fitur F-02.4 (Cover Art & Subtitle Lyrics Fetcher)**:
  - Cover Album: Unduh thumbnail resolusi tertinggi ke `/data/.../thumbnails/%(id)s.jpg`.
  - Lirik Sinkron: Argumen `--write-subs --write-auto-subs --sub-langs "id,en.*,all" --sub-format "lrc/vtt"` untuk menyimpan lirik bertanda waktu.

### 3. Aturan & Batasan
- DILARANG menggunakan API pihak ketiga (Cobalt, Invidious, Piped). Seluruh scraping wajib berjalan lokal via binary yt-dlp.
- Gunakan fallback jika produser tidak menyediakan lirik bertanda waktu: set `has_synced_lyrics = 0` dan sembunyikan panggung lirik secara elegan.

### 4. Kriteria Penerimaan
- Berhasil mengunduh 4 aset sekaligus: file M4A, file MP3 backup, gambar cover album, dan file lirik bertanda waktu (.lrc/.vtt).
- Nama produser/artis tercatat persis sesuai metadata video.

---

## F-03: Triage Pasca-Unduh & Lirik Sinkron

### 1. Tujuan
Menyediakan dialog aksi instan begitu unduhan tuntas (menentukan penyimpanan permanen ke playlist vs dengar sekali) serta pengalaman mendengarkan dengan visualisasi lirik sinkron interaktif.

### 2. Detail Implementasi
- **Sub-Fitur F-03.1 (Modal Sheet Triage M3 Expressive)**:
  - Bottom sheet berbentuk kartu bulat `xl: 32px`, background `#382426`, border 1dp `#603E42`, zero shadow.
  - Opsi A: "Simpan ke Playlist" (Kapsul 9999px `#E30B5C` Raspberry Red).
  - Opsi B: "Dengerin Sekali" (Kapsul 9999px `#452E30` dengan teks `#EEB3B5` Cotton Rose).
- **Sub-Fitur F-03.2 (Alur Simpan ke Playlist - Append-to-End)**:
  - Memilih playlist target -> sistem mengambil `MAX(position)` saat ini -> memasukkan record baru dengan `position = MAX + 1`.
  - Kedua file (M4A dan MP3 backup) dipertahankan di storage persisten.
- **Sub-Fitur F-03.3 (Ephemeral Dual-Purge Worker)**:
  - Menandai `is_ephemeral = 1`.
  - Begitu playback lagu selesai atau di-skip: kedua file (`id.m4a` dan `id_backup.mp3`) beserta thumbnail dan lirik langsung dihapus permanen dari storage perangkat.
- **Sub-Fitur F-03.4 (Synchronized Lyrics Viewer & Tap-to-Seek)**:
  - Parser LRC membaca pasangan `[mm:ss.xx] Baris Lirik`.
  - Pada layar Full Player, baris aktif di-highlight dengan background container `#6B192A` dan teks kontras `#FFD9DF` (atau `#F8395A` Hot Fuchsia), sedangkan baris lainnya menggunakan teks lembut `#EEB3B5`.
  - Tap-to-Seek: Pengguna menekan sembarang baris lirik -> engine audio Rust langsung melompat (*seek*) ke milidetik baris tersebut.

### 3. Aturan & Batasan
- Zero shadow & zero gradient pada seluruh antarmuka lirik dan sheet triage.
- Auto-scroll lirik harus berbasis `animateScrollToItem` yang mulus dan tidak membuat UI patah-patah (*stutter-free*).

### 4. Kriteria Penerimaan
- Lirik bergulir secara otomatis sesuai detik pemutaran lagu.
- Menekan salah satu baris lirik langsung mengubah detik playback lagu ke titik waktu baris tersebut.

---

## F-04: Manajemen Playlist, Reorder & Cover 2x2

### 1. Tujuan
Menyediakan pengelolaan playlist cerdas yang mendukung kustomisasi cover (gambar pilihan pengguna atau mosaik otomatis 2x2 dari 4 lagu pertama), pengurutan lagu via drag-and-drop, dan jaminan lagu baru selalu berada di indeks paling akhir.

### 2. Detail Implementasi
- **Sub-Fitur F-04.1 (Custom Cover Picker & Dynamic 2x2 Grid)**:
  - Opsi A (Kustom): Pengguna dapat memilih gambar pribadi dari galeri perangkat -> disimpan jalurnya ke kolom `custom_cover_path`.
  - Opsi B (Auto 2x2 Mosaik): Jika `custom_cover_path` bernilai null, sistem mengambil hingga 4 thumbnail teratas dari playlist tersebut:
    - Jika terdapat 4 lagu: Grid 2 kolom x 2 baris (Thumbnail 1: Kiri-Atas, Thumbnail 2: Kanan-Atas, Thumbnail 3: Kiri-Bawah, Thumbnail 4: Kanan-Bawah).
    - Jika terdapat < 4 lagu: Merender thumbnail lagu pertama secara penuh atau ikon placeholder M3 Expressive di atas surface `#382426`.
    - Kartu cover berukuran 64x64dp dengan corner 16dp dan outline solid 1dp `#603E42`.
- **Sub-Fitur F-04.2 (Drag-and-Drop Reordering Track)**:
  - Compose LazyColumn dengan drag handle 6 titik berwarna `#EEB3B5`.
  - Feedback visual saat dragging: item yang ditarik diberi outline tegas 2dp `#F8395A` (Hot Fuchsia) tanpa drop shadow.
  - Saat drag dilepas, batch update transaksi SQLite dijalankan:
    ```sql
    UPDATE playlist_tracks SET position = :newPos WHERE playlist_id = :pId AND track_id = :tId;
    ```
- **Sub-Fitur F-04.3 (Policy Penambahan Otomatis ke Urutan Belakang)**:
  - Kasus uji: Playlist berisi lagu di indeks 0, 1, 2. Penambahan lagu baru wajib menerima indeks 3.
  - Query Room:
    ```kotlin
    @Transaction
    suspend fun appendTrackToPlaylist(playlistId: String, trackId: String) {
        val currentMax = playlistTrackDao.getMaxPosition(playlistId) ?: -1
        playlistTrackDao.insert(PlaylistTrackCrossRef(playlistId, trackId, currentMax + 1, System.currentTimeMillis()))
    }
    ```
- **Sub-Fitur F-04.4 (Export & Backup Playlist)**:
  - Ekspor skema playlist dan ID track dalam file JSON terenkripsi/terkompresi.

### 3. Aturan & Batasan
- Operasi reorder dan append wajib dibungkus dalam `@Transaction` SQLite untuk mencegah tabrakan indeks posisi.
- Grid 2x2 harus dimuat secara efisien (menggunakan Coil cache memori) tanpa me-reload bitmap dari disk berulang kali saat list di-scroll.

### 4. Kriteria Penerimaan
- Playlist tanpa gambar kustom otomatis menampilkan mosaik 2x2 yang terdiri dari 4 sampul lagu pertama.
- Memilih gambar dari galeri segera menggantikan tampilan grid 2x2 dengan gambar kustom tersebut.
- Urutan lagu berhasil diubah via drag dan tersimpan persisten.

---

## F-05: Antarmuka M3 Expressive & Canvas FigCraft

### 1. Tujuan
Mewujudkan antarmuka Android out-of-the-box yang memadukan ergonomi mobile mutakhir dengan palet 4 warna harmonis, zero gradient, zero shadow, dan radius kapsul 9999px.

### 2. Detail Implementasi
- **Sub-Fitur F-05.1 (Token System & Palet 4 Warna)**:
  - Base: `#2E1E1F` (Coffee Bean)
  - Primary CTA: `#E30B5C` (Raspberry Red)
  - Secondary Accent & Synced Line: `#F8395A` (Hot Fuchsia)
  - High Contrast Pastel / Metadata: `#EEB3B5` (Cotton Rose)
  - Surface Tonal: `#382426` (Container) & `#452E30` (Container High)
- **Sub-Fitur F-05.2 (Floating Player Bar 9999px & Bottom Navigation Bar)**:
  - Bottom Navigation Bar (tinggi 72dp, sudut atas 24dp) dengan 4 tab: Beranda, Cari, Playlist, Profil. Indikator tab aktif menggunakan kapsul 9999px `#E30B5C`.
  - Floating Player kapsul 9999px melayang presisi di atas bottom navigation bar (tinggi 58dp, background `#452E30`, zero border).
  - Menampilkan thumbnail bulat 42dp, marquee judul + nama produser, dan tombol Play/Pause bulat 42dp `#E30B5C`.
- **Sub-Fitur F-05.3 (Fullscreen Player & Synced Lyrics Sheet)**:
  - Modal sheet 32dp yang menyajikan cover album besar, slider scrubber 9999px `#E30B5C`, dan area teks lirik sinkron (zero border).
- **Sub-Fitur F-05.4 (Automasi Mockup FigCraft 7 Layar)**:
  - Skrip automasi Python mengontrol desktop app FigCraft melalui port socket 7979.
  - Membangun 7 artboard mobile (360x800) dengan vektor SVG murni (zero emoji) dan zero border.
- **Sub-Fitur F-05.5 (Material 3 Expressive Motion System)**:
  - Container Transform: Transisi mini-player kapsul 9999px mengembang mulus (expand) menjadi Fullscreen Player 32dp via `SharedTransitionLayout` (350ms, spring bouncy).
  - Synced Lyrics Smooth Scrolling: Pergeseran baris aktif menggunakan `animateScrollToItem` dengan interpolasi halus `FastOutSlowInEasing` (300ms) tanpa lag frame.
  - Drag-and-Drop Spring Physics: Item yang di-drag membesar halus 1.03x dengan warna aktif `#523639`, dan item lain bergeser secara fisik (spring physics) mengisi ruang kosong.
  - Sliding Tab Indicator: Kapsul tab aktif `#E30B5C` meluncur secara horizontal mengikuti sentuhan jempol pengguna.
  - Marquee Text Motion: Judul lagu panjang di mini-player bergerak horizontal halus 30dp/detik bila teks melebihi lebar kontainer.

### 3. Aturan & Batasan
- STRICT: DILARANG menggunakan emoji pada elemen UI (wajib vektor grafis SVG flat murni).
- STRICT: DILARANG menggunakan garis tepi / border / stroke di seluruh elemen (pure flat tonal surface).
- STRICT: DILARANG menggunakan gradasi (`Brush.linearGradient`) dan bayangan (`Modifier.shadow`, `elevation`).
- STRICT: DILARANG menyertakan elemen slogan teks AI-slop di header atau kalimat bertele-tele.
- Seluruh tombol kontrol, active tab indicator, dan search input wajib berbentuk kapsul `9999px`.

### 4. Kriteria Penerimaan
- Lulus audit desain antislop: zero gradient, zero shadow, dan radius 9999px pada seluruh kontrol aksi.

---

## F-06: Pengaturan Cache & Performa

### 1. Tujuan
Menjaga konsumsi penyimpanan ponsel tetap terkendali dengan membersihkan file ephemeral serta file cadangan yang tidak terpakai secara berkala.

### 2. Detail Implementasi
- **Sub-Fitur F-06.1 (Dual-Storage Sweeper & Orphan Cleanup)**:
  - Rutin latar belakang menyisir cache `/data/.../audio/` untuk menghapus file orphan (file fisik yang tidak tercatat di database).
- **Sub-Fitur F-06.2 (Audio Quality & Download Strategy)**:
  - Pilihan kualitas bitstream: Fast (128kbps m4a) vs High Quality (256kbps m4a).
- **Sub-Fitur F-06.3 (Foreground Media Session Service)**:
  - Service audio Android dengan `MediaSessionCompat` dan integrasi notifikasi lockscreen M3.

### 3. Aturan & Batasan
- Sweeper dilarang menghapus track yang sedang dalam status `Playing` atau `Buffering`.

### 4. Kriteria Penerimaan
- Cache storage bersih dari sisa-sisa file ephemeral setelah lagu selesai diputar.
