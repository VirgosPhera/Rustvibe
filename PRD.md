# PRD — Project Requirements Document: RustVibe (Android Online Music Player)

## Overview
RustVibe adalah aplikasi pemutar musik online Android mandiri (standalone & native) berkinerja tinggi. Aplikasi memadukan mesin audio berbasis **Rust** (`symphonia` + `oboe`/AAudio) untuk pemutaran audio zero-latency bebas iklan (no ads) dengan binary **yt-dlp** lokal yang berjalan sepenuhnya secara on-device di Android tanpa server perantara pihak ketiga. Sistem menerapkan strategi download dual-format cerdas: format **M4A** diunduh sebagai format utama berkecepatan tinggi yang muncul di antarmuka pengguna, sementara format **MP3** diunduh secara bersamaan sebagai backup lokal tidak kasat mata untuk *failover* instan jika file M4A mengalami kendala decoding. Setiap lagu yang diunduh wajib menyertakan sampul album berkualitas tinggi, identitas produser/artis, serta lirik ter-sinkronisasi (synchronized lyrics) bila tersedia. Pada ranah koleksi, cover playlist mendukung kustomisasi foto pribadi atau otomatis menghasilkan mosaik grid 2x2 dari 4 lagu teratas. Seluruh tampilan dibalut dengan bahasa visual **Material 3 Expressive** tanpa gradien, tanpa bayangan, dan dengan ergonomi kapsul 9999px.

## Requirements
- **FR-01 (Standalone On-Device yt-dlp)**: Menjalankan binary Python + yt-dlp secara mandiri di dalam sandbox perangkat Android (via `youtubedl-android`), tanpa backend API perantara pihak ketiga dan bebas pembatasan kuota eksternal.
- **FR-02 (Dual-Format Download & Invisible Failover)**:
  - Unduhan mengekstrak format **M4A** (prioritas utama/tampil di UI) dan format **MP3** (backup lokal tidak kasat mata).
  - Tampilan UI hanya menampilkan satu track tunggal.
  - Jika file M4A gagal di-decode oleh mesin Rust (korup/unsupported codec), sistem secara otomatis dan instan beralih (*seamless failover*) memutar file MP3 backup tanpa memutus sesi dengar.
- **FR-03 (Metadata, Producer Info & Synchronized Lyrics)**:
  - Wajib mengekstrak: sampul album (thumbnail lokal), nama produser/channel/artis, dan file lirik bertanda waktu (LRC / VTT / timed text).
  - Saat lagu dimainkan, lirik bergerak sinkron (synchronized karaoke-style) mengikuti posisi milidetik playback dari mesin Rust.
  - Fitur tap-to-seek: Menekan baris lirik otomatis melompatkan posisi lagu ke waktu baris tersebut.
- **FR-04 (Post-Download Triage Flow)**:
  - Dialog modal sheet kapsul 9999px muncul begitu track terunduh:
    1. *Simpan ke Playlist*: Pengguna memilih playlist tujuan (atau buat baru) -> lagu otomatis di-append ke urutan paling belakang (`position = MAX + 1`).
    2. *Dengar Sekali (Ephemeral)*: Lagu dialirkan ke antrian sementara dan kedua file (M4A & MP3 backup) langsung dihapus permanen begitu playback selesai.
- **FR-05 (Manajemen Playlist Dinamis, Drag-and-Drop Reordering & Cover 2x2)**:
  - Urutan lagu dalam playlist dapat diubah-ubah secara bebas menggunakan gestur **drag-and-drop reordering** (reorderable list) yang terupdate reaktif dan tersimpan permanen di database.
  - Aturan penambahan lagu: Setiap lagu baru yang ditambahkan ke playlist yang sudah ada (misal sudah ada 3 lagu berurutan) WAJIB otomatis diletakkan di **posisi paling belakang** (`position = MAX + 1`).
  - Pembuatan, kustomisasi nama, dan penghapusan playlist lokal mandiri.
  - Cover playlist: Pengguna dapat memilih gambar kustom sendiri dari galeri, atau jika tidak diisi, aplikasi otomatis merender mosaik **grid 2x2** dari sampul 4 lagu teratas dalam playlist tersebut.
- **FR-06 (High-Performance Rust Native Audio Engine)**:
  - Decoding stream audio menggunakan Symphonia dan hardware output via AAudio / Oboe C++.
  - Zero garbage collection pause, konsumsi RAM < 45MB saat streaming.
- **NFR-01 (Zero Advertisements & Pure Offline Capability)**: Tidak ada iklan dalam bentuk apa pun.
- **NFR-02 (Strict M3 Expressive Design Rules)**:
  - Palet 4 warna: Coffee Bean (`#2E1E1F`), Raspberry Red (`#E30B5C`), Hot Fuchsia (`#F8395A`), Cotton Rose (`#EEB3B5`).
  - DILARANG menggunakan garis tepi / border (zero border — murni pemisahan warna permukaan flat tonal).
  - DILARANG menggunakan gradien warna (zero gradient).
  - DILARANG menggunakan bayangan (zero elevation/drop-shadow).
  - DILARANG menyertakan elemen teks dekoratif AI-slop (seperti badge slogan di header).
  - Navigasi utama menggunakan Bottom Navigation Bar (4 tab: Beranda, Cari, Playlist, Profil) dengan kapsul aktif 9999px.
  - Seluruh tombol kontrol, filter, search bar, active tab pill, dan mini-player berbentuk kapsul `9999px`.

## Core Features
1. **On-Device yt-dlp Extractor**: Ekstraksi lokal audio M4A + MP3 backup, thumbnail resolusi tinggi, metadata produser, dan subtitle lirik ter-sinkronisasi.
2. **Dual-Format Audio Pipeline**: Manajemen file ganda di mana M4A menjadi representasi utama di UI, dan MP3 tersimpan di background untuk failover otomatis saat terjadi decode error.
3. **Synchronized Lyrics Viewer**: Tampilan lirik real-time dengan highlight baris aktif berwarna Hot Fuchsia (`#F8395A`), auto-scroll mulus, dan tap-to-seek.
4. **Manajemen Playlist Dinamis & Drag-and-Drop Reorder**: Pengelolaan koleksi lagu dengan pengurutan ulang bebas via drag handle, jaminan penambahan lagu baru selalu masuk ke urutan paling belakang, serta cover cerdas (foto kustom atau mosaik 2x2 dari 4 lagu teratas).
5. **Pustaka Playlist & Mode Offline**:
   - Tab "Playlist" membuka katalog seluruh playlist dengan opsi buat playlist baru.
   - Tab "Offline" menampilkan dashboard file M4A yang tersimpan lokal beserta indikator kapasitas storage.
6. **Profil & Pengaturan Audio Rust**:
   - Tab "Profil" menampilkan kontrol driver audio AAudio, failover bitrate 256k, toggle pembersihan ephemeral otomatis, dan pembersih cache.

## User Flow
1. **Pencarian & Unduhan Ganda**:
   - Pengguna mencari judul/produser lagu di Search Bar 9999px.
   - yt-dlp lokal mengunduh file audio M4A (prioritas), file MP3 (backup), thumbnail album, dan lirik (.lrc / .vtt).
2. **Triage Dialog**:
   - Pengguna memilih: *Simpan ke Playlist* (masuk ke posisi terbawah playlist pilihan) ATAU *Dengar Sekali* (diputar langsung lalu dihapus total).
3. **Pemutaran & Lirik Sinkron**:
   - Lagu dimainkan lewat mesin Rust (M4A). Jika terjadi decode error pada M4A, engine secara instan mengalihkan input ke MP3 backup tanpa jeda suara yang mengganggu.
   - Pengguna swipe up floating player untuk membuka Full Player -> Lirik berjalan otomatis per baris sesuai ketukan lagu.
4. **Koleksi Playlist**:
   - Di tab Playlist, kartu playlist menampilkan gambar kustom atau grid 2x2 dari 4 lagu teratas.
   - Pengguna dapat membuka playlist dan menggeser posisi lagu sesuka hati melalui drag handle.

## Architecture
- **Presentation Layer (Kotlin + Jetpack Compose)**:
  - Material 3 Expressive UI, Dynamic 2x2 Grid Painter, Synced Lyric LazyColumn.
- **Native yt-dlp Subsystem (Standalone On-Device)**:
  - `youtubedl-android` (`com.yausername.youtubedl_android.YoutubeDL`) mengekstrak binary ke sandbox internal.
  - Menjalankan pipeline download: M4A + MP3 + Thumbnail + Subs secara atomik.
- **Bridge Layer (UniFFI / JNI)**:
  - Menghubungkan Kotlin dengan library Rust `librust_audio_engine.so`.
  - Mengirim perintah playback, deteksi decode error untuk failover ke MP3, dan mengalirkan event `position_ms` per 50ms ke UI lirik.
- **Rust Audio Core**:
  - `symphonia` untuk decoding M4A (AAC) / MP3.
  - `cpal`/`oboe` untuk transfer data PCM ke Android AudioServer via AAudio.
- **Data & Cache Layer (Room SQLite)**:
  - Menyimpan path M4A, path MP3 backup, path lirik, path thumbnail, produser, dan urutan playlist.

## Database Schema
```sql
-- Tabel Playlist
CREATE TABLE playlists (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    custom_cover_path TEXT, -- Null jika menggunakan generate otomatis 2x2 grid
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- Tabel Track
CREATE TABLE tracks (
    id TEXT PRIMARY KEY NOT NULL, -- YouTube Video ID
    title TEXT NOT NULL,
    producer TEXT NOT NULL, -- Nama producer / channel / artist
    duration_seconds INTEGER NOT NULL,
    thumbnail_path TEXT NOT NULL, -- File gambar lokal
    m4a_file_path TEXT NOT NULL, -- File audio utama
    backup_mp3_file_path TEXT, -- File audio cadangan
    lyrics_file_path TEXT, -- Path file .lrc atau .vtt (opsional)
    has_synced_lyrics INTEGER NOT NULL DEFAULT 0,
    is_ephemeral INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL
);

-- Tabel Relasi Playlist & Track
CREATE TABLE playlist_tracks (
    playlist_id TEXT NOT NULL,
    track_id TEXT NOT NULL,
    position INTEGER NOT NULL, -- 0-indexed, append = MAX(position) + 1
    added_at INTEGER NOT NULL,
    PRIMARY KEY (playlist_id, track_id),
    FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
);

CREATE INDEX idx_playlist_tracks_order ON playlist_tracks(playlist_id, position ASC);
```

## Tech Stack
- **UI & Presentation**: Android Jetpack Compose 1.7+, Kotlin 2.0+, M3 Expressive Tokens, FigCraft.
- **Downloader Engine**: `youtubedl-android` (standalone native binary yt-dlp + python runtime).
- **Audio Engine**: Rust 2021/2024 (`symphonia`, `cpal`, `oboe-rs`, `uniffi`).
- **Lyric Parser**: Custom Kotlin/Rust LRC parser (Regex + binary search timestamp interpolator).
- **Database**: Android Room SQLite Persistence Library.
- **Image Loading**: Coil Compose (dengan disk cache lokal).
