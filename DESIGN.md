---
version: alpha
name: RustVibe Expressive
description: Material 3 Expressive audio experience with flat high-contrast tonal surfaces, zero borders, zero gradients, zero shadows, full 9999px capsule ergonomics, Deep Crimson Carrot primary, and Antique White light theme.
colors:
  primary: "#DC3500"
  on-primary: "#FFFFFF"
  primary-container: "#52170B"
  on-primary-container: "#FFE0D6"
  primary-light: "#FF4500"
  secondary: "#FF6B4A"
  on-secondary: "#FFFFFF"
  secondary-container: "#FCD5C5"
  on-secondary-container: "#992900"
  tertiary: "#F8E6D2"
  on-tertiary: "#2E1E1F"
  background: "#2E1E1F"
  on-background: "#F8E6D2"
  surface: "#2E1E1F"
  on-surface: "#F8E6D2"
  surface-variant: "#3D292B"
  on-surface-variant: "#F8E6D2"
  surface-container: "#382426"
  surface-container-high: "#452E30"
  surface-container-highest: "#523639"
  background-light: "#F8E6D2"
  surface-container-light: "#EED6BF"
  surface-container-high-light: "#E4C7AC"
  surface-container-highest-light: "#D8B696"
  on-background-light: "#2E1E1F"
  on-surface-variant-light: "#634740"
  active-lyric-container: "#52170B"
  active-lyric-text: "#FFE0D6"
  active-lyric-container-light: "#FCD5C5"
  active-lyric-text-light: "#992900"
typography:
  display-lg:
    fontFamily: "Plus Jakarta Sans"
    fontSize: "2.5rem"
    fontWeight: 800
    lineHeight: 1.15
    letterSpacing: "-0.03em"
  title-lg:
    fontFamily: "Plus Jakarta Sans"
    fontSize: "1.375rem"
    fontWeight: 700
    lineHeight: 1.25
  title-md:
    fontFamily: "Plus Jakarta Sans"
    fontSize: "1.125rem"
    fontWeight: 600
    lineHeight: 1.3
  body-lg:
    fontFamily: "Plus Jakarta Sans"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
  body-md:
    fontFamily: "Plus Jakarta Sans"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.45
  label-lg:
    fontFamily: "Plus Jakarta Sans"
    fontSize: "0.875rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "0.02em"
  label-sm:
    fontFamily: "Plus Jakarta Sans"
    fontSize: "0.75rem"
    fontWeight: 600
    lineHeight: 1.2
    letterSpacing: "0.04em"
rounded:
  none: "0px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
  full: "9999px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  app-bar:
    backgroundColor: "{colors.background}"
    textColor: "{colors.on-background}"
    padding: "16px"
  app-bar-light:
    backgroundColor: "{colors.background-light}"
    textColor: "{colors.on-background-light}"
    padding: "16px"
  search-bar:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.full}"
    padding: "14px"
  search-bar-light:
    backgroundColor: "{colors.surface-container-light}"
    textColor: "{colors.on-surface-variant-light}"
    rounded: "{rounded.full}"
    padding: "14px"
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.full}"
    padding: "16px"
  button-primary-light:
    backgroundColor: "{colors.primary-light}"
    textColor: "{colors.on-background-light}"
    rounded: "{rounded.full}"
    padding: "16px"
  button-primary-hover:
    backgroundColor: "{colors.secondary}"
  chip-pill:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.tertiary}"
    rounded: "{rounded.full}"
    padding: "8px"
  chip-pill-light:
    backgroundColor: "{colors.surface-container-high-light}"
    textColor: "{colors.on-surface-variant-light}"
    rounded: "{rounded.full}"
    padding: "8px"
  card-track:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.xl}"
    padding: "16px"
  card-track-light:
    backgroundColor: "{colors.surface-container-light}"
    textColor: "{colors.on-background-light}"
    rounded: "{rounded.xl}"
    padding: "16px"
  player-bar:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.full}"
    padding: "12px"
  player-bar-light:
    backgroundColor: "{colors.surface-container-high-light}"
    textColor: "{colors.on-background-light}"
    rounded: "{rounded.full}"
    padding: "12px"
  bottom-nav:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.tertiary}"
    padding: "12px"
  bottom-nav-light:
    backgroundColor: "{colors.surface-container-light}"
    textColor: "{colors.on-surface-variant-light}"
    padding: "12px"
  lyrics-line-active:
    backgroundColor: "{colors.active-lyric-container}"
    textColor: "{colors.active-lyric-text}"
    rounded: "{rounded.md}"
    padding: "12px"
  lyrics-line-active-light:
    backgroundColor: "{colors.active-lyric-container-light}"
    textColor: "{colors.active-lyric-text-light}"
    rounded: "{rounded.md}"
    padding: "12px"
  drag-state-dark:
    backgroundColor: "{colors.surface-container-highest}"
  drag-state-light:
    backgroundColor: "{colors.surface-container-highest-light}"
---

## Overview

RustVibe menerapkan identitas visual **Material 3 Expressive** dengan fokus pada performa audio tinggi, zero-latency feedback, dan ergonomi jempol. Desain mengadopsi estetika *out-of-the-box*: kartu asimetris yang berani, panggung lirik tersinkronisasi karaoke, tata letak mosaik cover playlist 2x2, bilah navigasi bawah (bottom navigation bar) 4 tab, elemen kontrol kapsul penuh (`9999px`), serta pemisahan kontras murni lewat perbedaan tingkat warna permukaan (*tonal surface layers*) murni **TANPA BORDER / GARIS TEPI, TANPA GRADIEN, dan TANPA BAYANGAN**.

Tersedia dalam dua tema resmi:
1. **Dark Theme**: Fondasi Coffee Bean (`#2E1E1F`) dengan aksen **Deep Crimson Carrot (`#DC3500`)** yang lebih teduh, grounded, dan tidak menusuk mata.
2. **Light Theme**: Fondasi Antique White (`#F8E6D2`) dengan kontainer tonal hangat (`#EED6BF`, `#E4C7AC`) dan aksen menyala **Crimson Carrot (`#FF4500`)**.

## Colors

Palet memadukan warna terpilih yang dikunci secara harmonis:
- **Deep Crimson Carrot (`#DC3500`)**: Aksen primer tema gelap (gelap 12% dari `#FF4500`) untuk tombol play/pause utama, slider waktu aktif, active tab indicator, dan CTA primer (kontras 4.61:1 terhadap teks putih, memenuhi WCAG AA).
- **Vibrant Crimson Carrot (`#FF4500`)**: Aksen primer tema terang di atas Antique White.
- **Antique White (`#F8E6D2`)**: Fondasi tema terang (light base background) dan warna teks pastel kontras tinggi pada tema gelap (rasio 9.2:1 terhadap Coffee Bean).
- **Coffee Bean (`#2E1E1F`)**: Pondasi gelap hangat (dark espresso base) pada tema gelap, serta teks kontras utama pada tema terang.
- **Surface Tonal Steps — Dark Theme (Zero Border & Zero Shadow)**:
  - Layer 0 (Dasar Aplikasi): `#2E1E1F` (Coffee Bean)
  - Layer 1 (Search bar, List Item, Bottom Nav, 2x2 Grid): `#382426` (Surface Container)
  - Layer 2 (Kartu Aktif, Floating Player): `#452E30` (Surface Container High)
  - Layer 3 (Active Drag Item State): `#523639` (Surface Container Highest).
- **Surface Tonal Steps — Light Theme (Zero Border & Zero Shadow)**:
  - Layer 0 (Dasar Aplikasi): `#F8E6D2` (Antique White)
  - Layer 1 (Search bar, List Item, Bottom Nav): `#EED6BF` (Surface Container Light)
  - Layer 2 (Kartu Aktif, Floating Player): `#E4C7AC` (Surface Container High Light)
  - Layer 3 (Active Drag Item State): `#D8B696` (Surface Container Highest Light).

## Viewport & Responsiveness

- **Ukuran Layar Standar (Redmi Note 10 Pro)**: Didesain presisi untuk viewport hardware `420dp x 936dp` (rasio 20:9 fisik 1080x2400 pada density 410dpi).
- Memberikan ruang napas vertikal ekstra (*vertical breathing room*) sehingga Floating Player 9999px dan Bottom Navigation Bar tidak menutupi isi kartu playlist maupun daftar lagu.
- Adaptif untuk semua rasio layar Android (19.5:9, 20:9, 18:9) dengan container fleksibel `fillMaxWidth()` dan padding proporsional.

## Typography

Menggunakan typeface geometris modern **Plus Jakarta Sans** dengan bobot tegas (800 ExtraBold untuk judul display, 700 Bold untuk label aksi kapsul dan lirik aktif). Desain mengeliminasi badge AI-slop dekoratif yang tidak perlu demi tampilan yang bersih, dewasa, dan fungsional.

## Layout

- Grid 4-kolom mobile dengan margin horizontal 20dp dan gutter 14dp.
- **Bottom Navigation Bar**: Berada di posisi terbawah layar (docked, height 72dp) dengan sudut atas 24dp memuat 4 tab: Beranda, Cari, Playlist, Profil.
- **Floating Mini Player**: Melayang presisi di atas bottom navigation bar (tinggi 58dp, radius 9999px) tanpa tumpang tindih.
- **Motion & Interactions**:
  - *Container Transform*: Mini player 9999px mengembang mulus (*spring bouncy* 350ms) menjadi Fullscreen Player 32dp.
  - *Karaoke Synced Scroll*: Baris lirik bergeser otomatis dengan interpolasi halus (*FastOutSlowInEasing* 300ms).
  - *Drag-and-Drop Spring Physics*: Item yang digeser membesar 1.03x dengan warna aktif dan item lain bergeser dinamis.
  - *Sliding Tab Pill*: Indikator aktif meluncur horizontal antar-tab pada bottom navigation bar.
  - *Text Marquee*: Teks judul panjang bergulir horizontal 30dp/detik secara mulus.

## Elevation & Depth

- **ZERO BORDER**: Tidak ada stroke, outline, atau garis tepi 1px di seluruh elemen antarmuka. Pemisahan elemen 100% dicapai lewat kontras warna permukaan solid (*tonal background stepping*).
- **ZERO SHADOW**: Tidak ada bayangan `elevation`, `drop-shadow`, atau `box-shadow`.
- **ZERO GRADIENT**: Tidak ada gradasi warna di mana pun.

## Shapes

- Seluruh tombol interaktif, search bar, pill filter, chip aksi, active tab indicator, dan floating music bar menggunakan radius kapsul **`full: 9999px`**.
- Kartu track dan modal sheet menggunakan **`xl: 32px`** untuk sudut yang membulat dramatis khas Material 3 Expressive.
- Thumbnail lagu dan cover 2x2 playlist menggunakan **`md: 16px`**.

## Do's and Don'ts

- **DO** gunakan flat solid fill color untuk membedakan kontainer kartu.
- **DO** terapkan radius `9999px` pada semua tombol aksi, active tab pill, dan search bar.
- **DO** letakkan floating mini player tepat di atas bottom navigation bar.
- **DON'T** gunakan border, stroke, atau outline garis tepi pada kartu atau tombol apa pun.
- **DON'T** gunakan badge teks dekoratif AI-slop seperti "RUST AUDIO • NO ADS".
- **DON'T** gunakan gradient shader atau linear-gradient di mana pun.
- **DON'T** gunakan drop shadow, elevation blur, atau ambient shadow.
