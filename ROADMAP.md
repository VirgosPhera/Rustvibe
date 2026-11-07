# ROADMAP — Pohon Fitur & Rencana Pengembangan RustVibe

Struktur fitur RustVibe disusun menggunakan format kerangka fitur *ngodingpakeai* dengan penekanan pada arsitektur audio Rust, pipeline yt-dlp native on-device, strategi unduhan dual-format (M4A + MP3 backup), lirik tersinkronisasi, cover playlist kustom / grid 2x2, serta antarmuka Material 3 Expressive tanpa gradien dan tanpa bayangan (zero-shadow).

---

## 1. Ringkasan Eksekutif Fitur

| ID | Fitur Utama | Ikon | Prioritas | Sub-Fitur |
|---|---|---|---|---|
| F-01 | Mesin Audio Rust Native & Failover | `box` | **High** | 5 sub-fitur |
| F-02 | Standalone On-Device yt-dlp & Media Scraper | `search` | **High** | 4 sub-fitur |
| F-03 | Triage Pasca-Unduh & Lirik Sinkron | `file` | **High** | 4 sub-fitur |
| F-04 | Manajemen Playlist Dinamis, Drag-and-Drop Reorder & Cover 2x2 | `dashboard` | **High** | 4 sub-fitur |
| F-05 | Antarmuka M3 Expressive & Canvas FigCraft | `chart` | **Medium** | 4 sub-fitur |
| F-06 | Pengaturan Cache & Performa | `settings` | **Medium** | 3 sub-fitur |

---

## 2. Pohon Fitur Rinci

### F-01: Mesin Audio Rust Native & Failover (`box` — High)
Mesin audio inti berbasis Rust yang berjalan di background Android tanpa overhead GC, dilengkapi mekanisme invisible failover ke MP3.
- **F-01.1: Rust Core Symphonia Decoder** (`box` — High): Pipeline demuxing dan decoding audio M4A (AAC) / MP3 langsung ke PCM buffer.
- **F-01.2: Android AAudio/Oboe Hardware Sink** (`settings` — High): Output stream audio langsung ke perangkat keras audio Android via AAudio.
- **F-01.3: UniFFI Kotlin-Rust Bridge** (`shield` — High): Layer interface type-safe antara ViewModel Compose dan runtime native audio Rust.
- **F-01.4: Seamless Invisible Failover Engine** (`shield` — High): Deteksi error otomatis saat M4A korup/gagal decode dan pengalihan instan ke file backup MP3 tanpa interupsi user.
- **F-01.5: High-Precision Timestamp Broadcaster** (`dashboard` — High): Pengiriman event posisi playback milidetik setiap 50ms untuk sinkronisasi lirik yang presisi.

### F-02: Standalone On-Device yt-dlp & Media Scraper (`search` — High)
Ekstraksi media lokal sepenuhnya di dalam perangkat Android tanpa bergantung pada server proxy atau API pihak ketiga.
- **F-02.1: On-Device Python + yt-dlp Sandbox** (`box` — High): Bundling binary native `youtubedl-android` dengan lazy init yang terisolasi di internal storage aplikasi.
- **F-02.2: Fast Search & Producer Metadata Extractor** (`search` — High): Pencarian judul, durasi, nama produser/channel, dan URL thumbnail via JSON dump.
- **F-02.3: Dual-Format Downloader (M4A + MP3)** (`export` — High): Mengunduh track primer format M4A dan track sekunder format MP3 sebagai cadangan tersembunyi.
- **F-02.4: Cover Art & Subtitle Lyrics Fetcher** (`file` — High): Pengunduhan thumbnail resolusi tinggi ke local file dan ekstraksi subtitle lirik bertanda waktu (.lrc / .vtt).

### F-03: Triage Pasca-Unduh & Lirik Sinkron (`file` — High)
Alur pengambilan keputusan penyimpanan setelah unduhan selesai dan antarmuka lirik real-time.
- **F-03.1: Modal Sheet Triage M3 Expressive** (`file` — High): Tampilan modal kapsul `9999px` flat dengan 2 opsi: Simpan ke Playlist atau Dengar Sekali.
- **F-03.2: Alur Simpan ke Playlist (Append-to-End)** (`dashboard` — High): Penempatan lagu baru selalu di indeks terbawah playlist (`position = MAX + 1`).
- **F-03.3: Ephemeral Dual-Purge Worker** (`shield` — High): Pemutaran satu kali di mana kedua file (M4A & MP3) otomatis dimusnahkan saat selesai diputar.
- **F-03.4: Synchronized Lyrics Viewer & Tap-to-Seek** (`chat` — High): Tampilan lirik berjalan per baris dengan highlight Hot Fuchsia (`#F8395A`) dan lompatan playback saat baris ditekan.

### F-04: Manajemen Playlist Dinamis, Drag-and-Drop Reorder & Cover 2x2 (`dashboard` — High)
Pengelolaan koleksi lagu lokal dengan visual cover cerdas dan pengurutan ulang yang intuitif.
- **F-04.1: Custom Cover Picker & Dynamic 2x2 Grid** (`file` — High): Dukungan unggah gambar kustom dari galeri ATAU perakitan otomatis mosaik grid 2x2 dari 4 lagu pertama.
- **F-04.2: Drag-and-Drop Reordering Track** (`dashboard` — High): Gestur drag-handle untuk memindahkan urutan lagu secara visual dan persisten.
- **F-04.3: Policy Penambahan Otomatis ke Urutan Belakang** (`box` — High): Penjaminan posisi indeks paling akhir setiap kali lagu baru ditambahkan.
- **F-04.4: Export & Backup Playlist Lokal** (`export` — Low): Backup daftar playlist dan relasi lagu ke format JSON terkompresi.

### F-05: Antarmuka M3 Expressive & Canvas FigCraft (`chart` — Medium)
Desain antarmuka out-of-the-box yang memadukan 4 warna palet resmi, zero gradient, zero shadow, dan radius kapsul 9999px.
- **F-05.1: Token System & Palet 4 Warna** (`chart` — High): Implementasi token Coffee Bean, Raspberry Red, Hot Fuchsia, dan Cotton Rose.
- **F-05.2: Floating Player Bar 9999px** (`box` — High): Mini-player kapsul mengambang dengan flat surface container `#452E30` tanpa drop shadow.
- **F-05.3: Fullscreen Player & Synced Lyrics Sheet** (`chat` — High): Tampilan pemutar penuh dengan cover besar 32dp dan panggung lirik tersinkronisasi.
- **F-05.4: Automasi Mockup FigCraft** (`chart` — Medium): Skrip automasi socket FigCraft untuk menyusun wireframe 3 layar utama.

### F-06: Pengaturan Cache & Performa (`settings` — Medium)
Optimasi penyimpanan lokal dan konsumsi baterai perangkat.
- **F-06.1: Dual-Storage Sweeper & Orphan Cleanup** (`shield` — Medium): Pembersihan berkala untuk file ephemeral kedaluwarsa dan verifikasi integritas backup MP3.
- **F-06.2: Audio Quality & Download Strategy** (`settings` — Low): Konfigurasi kompresi audio dan pembatasan download pada jaringan seluler.
- **F-06.3: Foreground Media Session Service** (`box` — High): Service background Android dengan notifikasi media sistem yang sinkron dengan state Rust.
