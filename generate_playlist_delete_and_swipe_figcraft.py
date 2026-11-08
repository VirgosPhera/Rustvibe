import json
import subprocess
import zipfile

def get_next_id():
    with zipfile.ZipFile("/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft") as z:
        doc = json.loads(z.read("document.json").decode())
        return doc.get("next_id", 1809)

START_ID = get_next_id()
current_id = START_ID

def alloc_id():
    global current_id
    cid = current_id
    current_id += 1
    return cid

commands = []

def add_cmd(command, params):
    alloc_id()
    commands.append({"command": command, "params": params})

# SVG VECTOR PATH CATALOG
ICONS = {
    "back": "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z",
    "close": "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z",
    "check": "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    "trash": "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
    "music": "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z",
    "playlist": "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z",
    "play": "M8 5v14l11-7z",
    "pause": "M6 19h4V5H6v14zm8-14v14h4V5h-4z",
    "next": "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z",
    "prev": "M6 6h2v12H6zm3.5 6l8.5 6V6z",
    "shuffle": "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z",
    "repeat": "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z",
    "swipe_left": "M15.41 16.59L10.83 12l4.58-4.59L14 6l-6 6 6 6 1.41-1.41z",
    "swipe_right": "M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z",
    "more": "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
}

WHITE = "#FFFFFF"
DARK = {
    "bg": "#2E1E1F",           # Coffee Bean
    "surf": "#382426",         # Surface Container
    "surf_high": "#452E30",    # Surface Container High
    "surf_highest": "#523639", # Surface Container Highest
    "primary": "#DC3500",      # Deep Crimson Carrot
    "secondary": "#E65100",    # Warm Carrot Accent
    "text_main": WHITE,
    "text_sub": "#F8E6D2",
    "badge_success_bg": "#1B3B22",
    "badge_success_fg": "#78DC8B",
    "badge_error_bg": "#52170B",
    "badge_error_fg": "#FF897D",
    "selection_overlay": "#52170B",
    "check_bg": "#DC3500",
    "uncheck_bg": "#452E30"
}

def add_icon(parent_id, icon_name, x, y, size=24, color=WHITE):
    path = ICONS.get(icon_name, ICONS["music"])
    add_cmd("vector.create", {
        "parent": parent_id,
        "x": x, "y": y,
        "width": size, "height": size,
        "path": path,
        "props": {"fill": color}
    })

def add_rect(parent_id, x, y, w, h, fill_color, corner=0):
    corners = corner if isinstance(corner, list) else [corner, corner, corner, corner]
    add_cmd("shape.rectangle", {
        "parent": parent_id,
        "x": x, "y": y,
        "width": w, "height": h,
        "props": {"fill": fill_color, "cornerRadius": corners}
    })

def add_text(parent_id, x, y, text, size=14, color=WHITE, weight=400):
    add_cmd("text.create", {
        "parent": parent_id,
        "x": x, "y": y,
        "text": text,
        "props": {
            "fontSize": size,
            "fill": color,
            "fontWeight": weight,
            "fontFamily": "Plus Jakarta Sans"
        }
    })

# =========================================================================
# ARTBOARD 34: PORTRAIT — PLAYLIST HUB (MODE HAPUS SELEKTIF & MULTI-SELECT)
# =========================================================================
p1 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Portrait — Playlist Hub Mode Hapus Selektif (Multi-Select)",
        "x": 50, "y": 5200,
        "width": 420, "height": 936,
        "props": {"fill": DARK["bg"]}
    }
})

# Contextual Selection App Bar (Full Pill & Action Bar)
add_rect(p1, 0, 0, 420, 96, DARK["surf_high"])
add_icon(p1, "close", 24, 48, size=24, color=WHITE)
add_text(p1, 68, 48, "2 Playlist Dipilih", size=17, color=WHITE, weight=700)
add_text(p1, 68, 70, "Ketuk playlist untuk memilih", size=11, color=DARK["text_sub"])

# "Pilih Semua" Pill Button in Top Bar
add_rect(p1, 305, 46, 95, 34, DARK["surf_highest"], corner=9999)
add_text(p1, 320, 56, "Pilih Semua", size=11, color=WHITE, weight=700)

# Section Header
add_text(p1, 24, 116, "Pilih Playlist yang Ingin Dihapus", size=15, color=WHITE, weight=700)
add_text(p1, 280, 118, "Total: 6 Playlist", size=11, color=DARK["text_sub"])

# Playlist 2-Column Grid (Cards with Selection Badges)
playlists = [
    ("Jersey_Local_Test", "3 Lagu • Lokal", True),      # Selected
    ("Japanese Rock Hits", "8 Lagu • YouTube", False),  # Not selected
    ("Rock Workout", "15 Lagu • YouTube", True),        # Selected
    ("Lofi Santai", "12 Lagu • YouTube", False),        # Not selected
    ("Anime OST", "20 Lagu • YouTube", False),          # Not selected
    ("Playlist Baru", "0 Lagu • Kustom", False)         # Not selected
]

grid_y = 146
for i in range(0, len(playlists), 2):
    row_y = grid_y + (i // 2) * 190

    # Column 1
    name1, desc1, sel1 = playlists[i]
    card_bg1 = DARK["selection_overlay"] if sel1 else DARK["surf"]
    add_rect(p1, 20, row_y, 182, 176, card_bg1, corner=20)
    add_rect(p1, 32, row_y + 12, 158, 100, DARK["surf_highest"], corner=14)
    add_icon(p1, "playlist", 96, row_y + 47, size=30, color=WHITE)

    # Checkbox Badge
    if sel1:
        add_rect(p1, 150, row_y + 20, 30, 30, DARK["check_bg"], corner=9999)
        add_icon(p1, "check", 156, row_y + 26, size=18, color=WHITE)
    else:
        add_rect(p1, 150, row_y + 20, 30, 30, DARK["uncheck_bg"], corner=9999)

    add_text(p1, 32, row_y + 122, name1[:18], size=13, color=WHITE, weight=700)
    add_text(p1, 32, row_y + 142, desc1, size=11, color=DARK["text_sub"])

    # Column 2
    if i + 1 < len(playlists):
        name2, desc2, sel2 = playlists[i + 1]
        card_bg2 = DARK["selection_overlay"] if sel2 else DARK["surf"]
        add_rect(p1, 218, row_y, 182, 176, card_bg2, corner=20)
        add_rect(p1, 230, row_y + 12, 158, 100, DARK["surf_highest"], corner=14)
        add_icon(p1, "playlist", 294, row_y + 47, size=30, color=WHITE)

        if sel2:
            add_rect(p1, 348, row_y + 20, 30, 30, DARK["check_bg"], corner=9999)
            add_icon(p1, "check", 354, row_y + 26, size=18, color=WHITE)
        else:
            add_rect(p1, 348, row_y + 20, 30, 30, DARK["uncheck_bg"], corner=9999)

        add_text(p1, 230, row_y + 122, name2[:18], size=13, color=WHITE, weight=700)
        add_text(p1, 230, row_y + 142, desc2, size=11, color=DARK["text_sub"])

# Bottom Floating Destructive Action Bar (Full Pill 9999px)
add_rect(p1, 20, 846, 380, 60, DARK["badge_error_bg"], corner=9999)
add_icon(p1, "trash", 125, 864, size=22, color=DARK["badge_error_fg"])
add_text(p1, 158, 866, "Hapus 2 Playlist Terpilih", size=14, color=DARK["badge_error_fg"], weight=700)


# =========================================================================
# ARTBOARD 35: PORTRAIT — DIALOG KONFIRMASI HAPUS PLAYLIST
# =========================================================================
p2 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Portrait — Dialog Konfirmasi Hapus Playlist Terpilih",
        "x": 520, "y": 5200,
        "width": 420, "height": 936,
        "props": {"fill": "#180F10"}
    }
})

# Background silhouette
add_rect(p2, 20, 80, 380, 700, DARK["bg"], corner=24)

# Confirmation Modal Dialog Card
add_rect(p2, 24, 250, 372, 436, DARK["surf"], corner=28)

# Trash Hero Icon Badge
add_rect(p2, 180, 274, 60, 60, DARK["badge_error_bg"], corner=9999)
add_icon(p2, "trash", 198, 292, size=24, color=DARK["badge_error_fg"])

add_text(p2, 106, 348, "Hapus 2 Playlist?", size=20, color=WHITE, weight=700)
add_text(p2, 54, 376, "Playlist berikut akan dihapus secara permanen:", size=12, color=DARK["text_sub"])

# Playlist preview list
add_rect(p2, 44, 404, 332, 94, DARK["surf_high"], corner=16)
add_text(p2, 58, 418, "• Jersey_Local_Test (3 lagu)", size=12, color=WHITE, weight=700)
add_text(p2, 58, 442, "• Rock Workout (15 lagu)", size=12, color=WHITE, weight=700)
add_text(p2, 58, 468, "File audio fisik di HP TIDAK akan ikut terhapus.", size=10, color=DARK["badge_success_fg"])

# Action Buttons: Full Pill 9999px
# Button 1: Hapus Permanen (Destructive Red Pill)
add_rect(p2, 44, 520, 332, 48, DARK["primary"], corner=9999)
add_icon(p2, "trash", 112, 534, size=18, color=WHITE)
add_text(p2, 138, 535, "Hapus 2 Playlist Sekarang", size=13, color=WHITE, weight=700)

# Button 2: Batal
add_rect(p2, 44, 580, 332, 48, DARK["surf_high"], corner=9999)
add_text(p2, 190, 595, "Batal", size=13, color=WHITE, weight=700)


# =========================================================================
# ARTBOARD 36: PORTRAIT — SCREEN 3 FULL PLAYER (SWIPE COVER GESTURE CAROUSEL)
# =========================================================================
p3 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Portrait — Screen 3 Full Player (Cover Gesture Swipe Kiri-Kanan)",
        "x": 990, "y": 5200,
        "width": 420, "height": 936,
        "props": {"fill": DARK["bg"]}
    }
})

# Top Bar
add_rect(p3, 0, 0, 420, 96, DARK["surf"])
add_icon(p3, "back", 24, 48, size=24, color=WHITE)
add_text(p3, 136, 44, "SEDANG DIPUTAR", size=11, color=DARK["secondary"], weight=700)
add_text(p3, 114, 62, "YJCA (YANG JERSEY CLUB)", size=14, color=WHITE, weight=700)
add_icon(p3, "more", 372, 48, size=24, color=WHITE)

# SWIPE CAROUSEL CONTAINER (Central Cover + Sneak Peek Left & Right)
# Left Track Peek (Previous Song Preview)
add_rect(p3, -60, 136, 120, 260, DARK["surf_high"], corner=24)
add_rect(p3, -50, 146, 100, 240, DARK["surf_highest"], corner=18)

# Center Active Track Cover (Big 280x280)
add_rect(p3, 70, 126, 280, 280, DARK["surf_high"], corner=28)
add_rect(p3, 80, 136, 260, 260, DARK["primary"], corner=24)
add_icon(p3, "music", 186, 242, size=48, color=WHITE)

# Right Track Peek (Next Song Preview)
add_rect(p3, 360, 136, 120, 260, DARK["surf_high"], corner=24)
add_rect(p3, 370, 146, 100, 240, DARK["surf_highest"], corner=18)

# Swipe Hint Overlay / Tactile Indicator Pill (Full Pill 9999px)
add_rect(p3, 110, 420, 200, 32, DARK["surf_highest"], corner=9999)
add_icon(p3, "swipe_left", 118, 426, size=18, color=DARK["secondary"])
add_text(p3, 142, 429, "Geser Cover Lagu", size=11, color=WHITE, weight=700)
add_icon(p3, "swipe_right", 284, 426, size=18, color=DARK["secondary"])

# Queue Pagination Indicator Dots (Carousel Dots)
add_rect(p3, 185, 466, 8, 8, DARK["surf_highest"], corner=9999)
add_rect(p3, 198, 466, 24, 8, DARK["primary"], corner=9999) # Active song wide pill dot
add_rect(p3, 227, 466, 8, 8, DARK["surf_highest"], corner=9999)

# Song Title & Artist
add_text(p3, 28, 496, "Boss Up Pt2 #jerseyclub", size=20, color=WHITE, weight=700)
add_text(p3, 28, 526, "DJ Smallz 732 • Salako Producer", size=13, color=DARK["text_sub"])

# Seekbar (Progress Track & Scrubber Full Pill)
add_rect(p3, 28, 568, 364, 8, DARK["surf_highest"], corner=9999)
add_rect(p3, 28, 568, 146, 8, DARK["primary"], corner=9999)
add_rect(p3, 166, 564, 16, 16, WHITE, corner=9999) # Thumb

# Time Labels
add_text(p3, 28, 586, "01:24", size=11, color=DARK["text_sub"])
add_text(p3, 356, 586, "03:30", size=11, color=DARK["text_sub"])

# Playback Media Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
add_icon(p3, "shuffle", 36, 638, size=22, color=DARK["secondary"])

# Prev Button (Pill Container)
add_rect(p3, 90, 624, 52, 52, DARK["surf_high"], corner=9999)
add_icon(p3, "prev", 104, 638, size=24, color=WHITE)

# Big Play/Pause Main Button (Primary 68x68 Full Pill)
add_rect(p3, 176, 616, 68, 68, DARK["primary"], corner=9999)
add_icon(p3, "pause", 196, 636, size=28, color=WHITE)

# Next Button (Pill Container)
add_rect(p3, 278, 624, 52, 52, DARK["surf_high"], corner=9999)
add_icon(p3, "next", 292, 638, size=24, color=WHITE)

add_icon(p3, "repeat", 362, 638, size=22, color=DARK["text_sub"])

# Hardware Volume Slider Bar
add_rect(p3, 28, 712, 364, 52, DARK["surf"], corner=9999)
add_icon(p3, "music", 44, 726, size=22, color=DARK["text_sub"])
add_rect(p3, 80, 734, 250, 8, DARK["surf_highest"], corner=9999)
add_rect(p3, 80, 734, 180, 8, DARK["primary"], corner=9999)
add_text(p3, 344, 730, "72%", size=12, color=WHITE, weight=700)

# Quick Navigation Lyrics Preview Button (Full Pill 9999px)
add_rect(p3, 120, 800, 180, 44, DARK["surf_high"], corner=9999)
add_text(p3, 142, 814, "Lihat Lirik Karaoke", size=12, color=WHITE, weight=700)


# =========================================================================
# ARTBOARD 37: LANDSCAPE — DUAL-PANE (SWIPE COVER & SYNCED LYRICS)
# =========================================================================
p4 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Landscape — Screen 3 Full Player (Swipe Cover & Synced Lyrics)",
        "x": 1460, "y": 5200,
        "width": 936, "height": 420,
        "props": {"fill": DARK["bg"]}
    }
})

# Navigation Rail
add_rect(p4, 0, 0, 72, 420, DARK["surf"])
add_icon(p4, "back", 24, 28, size=24, color=WHITE)
add_icon(p4, "music", 24, 196, size=24, color=DARK["primary"])

# LEFT PANE: Swipeable Cover & Compact Controls (380dp)
add_rect(p4, 92, 20, 390, 380, DARK["surf"], corner=24)

# Swipeable Cover Preview
add_rect(p4, 112, 36, 170, 170, DARK["primary"], corner=20)
add_icon(p4, "music", 175, 99, size=44, color=WHITE)

# Swipe Hint badge
add_rect(p4, 112, 214, 170, 26, DARK["surf_high"], corner=9999)
add_text(p4, 134, 221, "‹ Geser Ganti Lagu ›", size=10, color=DARK["text_sub"], weight=700)

# Track Info
add_text(p4, 298, 48, "Boss Up Pt2", size=17, color=WHITE, weight=700)
add_text(p4, 298, 72, "DJ Smallz 732", size=12, color=DARK["text_sub"])
add_text(p4, 298, 92, "YJCA Playlist", size=11, color=DARK["secondary"])

# Compact Controls in Left Pane
add_rect(p4, 298, 130, 44, 44, DARK["surf_high"], corner=9999)
add_icon(p4, "prev", 308, 140, size=22, color=WHITE)

add_rect(p4, 350, 126, 52, 52, DARK["primary"], corner=9999)
add_icon(p4, "pause", 366, 142, size=20, color=WHITE)

add_rect(p4, 410, 130, 44, 44, DARK["surf_high"], corner=9999)
add_icon(p4, "next", 420, 140, size=22, color=WHITE)

# Seekbar
add_rect(p4, 112, 262, 350, 8, DARK["surf_highest"], corner=9999)
add_rect(p4, 112, 262, 140, 8, DARK["primary"], corner=9999)
add_text(p4, 112, 278, "01:24", size=10, color=DARK["text_sub"])
add_text(p4, 432, 278, "03:30", size=10, color=DARK["text_sub"])

# Volume Bar
add_rect(p4, 112, 320, 350, 46, DARK["surf_high"], corner=9999)
add_icon(p4, "music", 126, 332, size=20, color=DARK["text_sub"])
add_rect(p4, 156, 339, 240, 8, DARK["surf_highest"], corner=9999)
add_rect(p4, 156, 339, 170, 8, DARK["primary"], corner=9999)
add_text(p4, 410, 335, "72%", size=11, color=WHITE, weight=700)

# RIGHT PANE: Synced Karaoke Lyrics (420dp)
add_rect(p4, 502, 20, 414, 380, DARK["surf"], corner=24)
add_text(p4, 524, 40, "Lirik Sinkron (LRCLIB)", size=15, color=WHITE, weight=700)
add_text(p4, 790, 42, "Real-time", size=11, color=DARK["secondary"])

lyrics = [
    ("Yeah, we back on the block again", False),
    ("Turn the bass up, let the jersey bounce", False),
    ("Salako on the beat, you know how it go", True), # Active
    ("Left, right, step it out, don't stop now", False),
    ("Keep the rhythm rollin' till the morning light", False),
    ("Boss up, yeah you know we came to win", False)
]

ly = 76
for text, active in lyrics:
    if active:
        add_rect(p4, 520, ly, 378, 48, DARK["primary"], corner=16)
        add_text(p4, 536, ly + 14, text, size=13, color=WHITE, weight=700)
    else:
        add_text(p4, 536, ly + 14, text, size=12, color=DARK["text_sub"])
    ly += 50

with open("/tmp/playlist_delete_swipe_script.json", "w") as f:
    json.dump(commands, f, indent=2)

print(f"Generated {len(commands)} commands. Running via figcraft-cli...")
run_res = subprocess.run([
    "figcraft-cli", "run",
    "--in", "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft",
    "--out", "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft",
    "/tmp/playlist_delete_swipe_script.json"
], capture_output=True, text=True)

if run_res.returncode != 0:
    print("Run error:", run_res.stderr)
else:
    print("Run succeeded! Exporting artboards...")

# Export PNGs
exports = [
    (p1, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_playlist_delete_multiselect.png"),
    (p2, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_playlist_delete_confirm_dialog.png"),
    (p3, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_player_cover_swipe_carousel.png"),
    (p4, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_landscape_player_swipe_lyrics.png")
]
for node_id, out_path in exports:
    exp_res = subprocess.run([
        "figcraft-cli", "export",
        "--node", str(node_id),
        "--out", out_path,
        "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft"
    ], capture_output=True, text=True)
    print(f"Export {out_path}: {exp_res.stdout.strip()} {exp_res.stderr.strip()}")

print("Complete!")
