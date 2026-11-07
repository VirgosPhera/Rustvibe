# RustVibe 🎵🦀

Pemutar musik mandiri Android (open-source & bebas iklan) yang menggabungkan mesin audio native **Rust** dengan antarmuka **Material 3 Expressive** di Jetpack Compose.

> *"Aplikasi ini gw bangun dalam waktu **2 hari intensif (6 - 7 November 2026)** dari nol sampai APK siap pakai di HP fisik (Redmi Note 10 Pro / Android 16). Alasan utamanya simpel: capek sama aplikasi streaming resmi yang makin hari makin berat, banyak iklan, lirik harus bayar langganan, dan lag kalau multitasking."* — **Amir (Only-One-Kind / VirgosPhera)**

---

## 🇮🇩 Bahasa Indonesia

### Kenapa RustVibe?
Kebanyakan aplikasi pemutar musik pihak ketiga di Android cuma pembungkus webview atau pakai MediaPlayer bawaan yang lambat. RustVibe dibangun dengan arsitektur berbeda:
1. **Core Audio Engine ditulis pakai Rust**: Menggunakan crate `symphonia` untuk decoding M4A (AAC) dan MP3 langsung di layer native tanpa terkena jeda Garbage Collector Java/Kotlin.
2. **Tanpa API Berbayar / Tanpa Proxy Server**: yt-dlp berjalan mandiri langsung di dalam sistem Android HP kamu untuk menarik aliran audio M4A langsung dari YouTube dan lirik sinkron milidetik dari LRCLIB.
3. **100% Offline-First**: Begitu lagu atau playlist kamu unduh, semua tersimpan lokal di SQLite Room. Kamu bisa dengerin musik kapan saja tanpa kuota internet.

---

### Fitur-Fitur Utama

- **Unduh 1 Playlist Lengkap Sekaligus**:
  - Cukup paste link playlist YouTube / YouTube Music (misal: `https://music.youtube.com/playlist?list=...`) atau ketik judul playlist di kolom pencarian.
  - Muncul dialog konfirmasi: kamu bisa memilih **✓ Ikuti Nama Asli** dari YouTube atau **✎ Ganti Nama Kustom** sebelum mulai unduh semua lagu.
- **Indikator Progres Unduhan Real-Time**:
  - Ada progress bar linear dan angka persentase (misal: `68% Selesai`, kecepatan KB/s, status lagu ke-X dari total).
  - Tombol unduh di samping lagu berubah jadi pill persentase aktif saat proses berlangsung.
- **Lirik Karaoke Tersinkronisasi (Tap-to-Seek)**:
  - Lirik sinkron milidetik (.lrc) ter-scroll otomatis mengikuti ketukan lagu.
  - Klik baris lirik mana saja untuk langsung melompatkan posisi lagu (*tap-to-seek*).
  - Bisa ditutup/dibuka lewat tombol `Tutup Lirik` / `Buka Lirik`.
- **Navigasi Tombol Prev (`⏮`) & Next (`⏭`) Penuh**:
  - Tombol sebelum dan sesudah di samping tombol Play berfungsi aktif mengontrol antrean lagu di playlist.
  - Floating mini player di bawah layar juga dilengkapi tombol skip next instan.
  - Otomatis memutar lagu berikutnya (*auto-play next*) saat lagu selesai.
- **Tampilan Ergonomis Landscape & Portrait**:
  - **Portrait**: Tata letak vertikal nyaman dioperasikan satu tangan.
  - **Landscape**: Navigasi samping ramping (*Navigation Rail* 72dp), layout dua kolom di Beranda/Cari, dan *Dual-Pane Player* (kontrol & cover di kiri, panggung lirik di kanan).
- **Desain Material 3 Expressive Murni**:
  - **Zero Border**: Pemisahan visual mengandalkan kontras warna container (*tonal stepping*), tanpa garis pinggir kaku.
  - **Zero Shadow**: Tidak menggunakan bayangan/elevation palsu.
  - **Zero Gradient**: Warna solid dan bersih.
  - **Full Pill Shape (`9999px`)**: Semua tombol interaktif, search bar, filter chip, dan volume slider berbentuk kapsul membulat.
- **Tiga Pilihan Tema**:
  1. *Tema Gelap*: Coffee Bean (`#2E1E1F`) + Deep Crimson Carrot (`#DC3500`).
  2. *Tema Terang*: Antique White (`#F8E6D2`) + Vibrant Crimson Carrot (`#FF4500`).
  3. *Aksen Android Dinamis*: Menyesuaikan palet warna wallpaper Android 15 & 16 (Monet System).
- **Kontrol Volume Hardware + Label Persentase**:
  - Slider volume kapsul responsif dengan indikator angka tebal (`0%` - `100%`).
- **Integrasi Notifikasi Media Android**:
  - Dukungan `MediaSession` resmi dengan seekbar progres interaktif dan tombol aksi notifikasi.

---

### Cara Build & Pasang Sendiri

#### Kebutuhan Sistem:
- JDK 17 atau lebih baru
- Android SDK (Platform 34/35) & ADB
- Rust toolchain (`cargo`, `rustc`) target `aarch64-linux-android` (opsional jika compile core Rust)

#### Langkah Kompilasi:
```bash
# 1. Clone repository
git clone https://github.com/VirgosPhera/Rustvibe.git
cd Rustvibe/android

# 2. Build Debug APK langsung via terminal
./gradlew assembleDebug

# 3. Pasang ke HP Android via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🇬🇧 English

### Why RustVibe?
Most mobile music streaming apps today are bloated, packed with unskippable ads, locked behind paywalled lyrics, or rely on heavy webviews that drain your battery and RAM. 

RustVibe is engineered from the ground up to solve that:
- **Rust Native Audio Engine**: Built with the `symphonia` audio crate for instant, high-fidelity AAC/M4A and MP3 decoding without JVM garbage collection spikes.
- **Standalone On-Device yt-dlp**: Extracts and downloads YouTube Music streams, album artwork, and synced LRC lyrics directly on your phone without third-party middleman servers.
- **Pure Material 3 Expressive**: Zero gradient, zero shadow, zero borders, and full 9999px pill ergonomics designed for modern Android devices.

### Key Highlights
- **1-Click Full Playlist Downloader**: Batch download 20+ tracks from any YouTube/YouTube Music playlist with real-time numeric percentage and speed trackers. Choose between original YouTube title or custom naming.
- **Synced Karaoke Lyrics**: Millisecond-accurate auto-scrolling lyrics with tap-to-seek functionality.
- **Adaptive Ergonomics**: Seamlessly switches between Portrait and Landscape layout (72dp left Navigation Rail, dual-pane player with controls on the left and full lyrics stage on the right).
- **Full MediaSession Integration**: Android status bar and lockscreen playback scrubber with Previous, Play/Pause, and Next transport controls.
- **Hardware Volume Pill**: Full capsule volume bar with live percentage badge (`0%` - `100%`).
- **3 Color Themes**: Dark Theme (Coffee Bean + Crimson), Light Theme (Antique White + Crimson), and Android 15/16 Monet Dynamic System Accent.

---

## Struktur Folder Proyek / Project Structure

```text
rust-music-android/
├── DESIGN.md                                 # Material 3 Expressive Token Specification
├── PRD.md                                    # Complete Product Requirements Document
├── ROADMAP.md                                # Development Roadmap & Milestones
├── SPECS.md                                  # Audio Engine & Architecture Specs
├── TASKS.md                                  # Step-by-step verification log
├── rust-audio-core/                          # Rust Native Audio Crate
│   ├── Cargo.toml
│   └── src/
│       ├── lib.rs                            # JNI Exports & Audio Pipelines
│       ├── decoder.rs                        # Symphonia M4A/MP3 Decoder
│       ├── engine.rs                         # State machine & sample playback
│       └── error.rs                          # Error handling
└── android/                                  # Android Application (Jetpack Compose)
    └── app/
        ├── build.gradle.kts                  # Android dependencies (Room, yt-dlp, Coil)
        └── src/main/
            ├── AndroidManifest.xml
            └── java/com/rustvibe/
                ├── MainActivity.kt           # Edge-to-edge Navigation & Root Activity
                ├── audio/
                │   ├── LyricsParser.kt       # Fast LRC parser & timestamp mapper
                │   ├── RustAudioEngineBridge.kt # Audio bridge & JNI bindings
                │   └── RustVibeNotificationManager.kt # Android MediaSession & Notification
                ├── data/
                │   ├── local/                # Room SQLite Database & DAOs
                │   └── remote/               # Standalone yt-dlp downloader & HTTP scraper
                └── ui/
                    ├── RustVibeViewModel.kt  # Unified reactive state management
                    ├── theme/                # Color, Shape, and Expressive Theme tokens
                    ├── components/           # Full pill components, dialogs, nav bars
                    └── screens/              # HomeScreen, SearchScreen, PlayerScreen, etc.
```

---

## Kontak & Kontribusi / Social Links

- **GitHub Repository**: [https://github.com/VirgosPhera/Rustvibe](https://github.com/VirgosPhera/Rustvibe)  
  *Kasih bintangnya dong biar makin populer nih aplikasi, Makasih! ⭐*
- **YouTube Channel**: [@lampulolhst](https://www.youtube.com/@lampulolhst)
- **Lead Developer**: Amir (**Only-One-Kind** / **VirgosPhera**)
- **License**: MIT License
