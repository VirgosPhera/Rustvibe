import json
import subprocess
import zipfile

def get_next_id():
    with zipfile.ZipFile("/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft") as z:
        doc = json.loads(z.read("document.json").decode())
        return doc.get("next_id", 1954)

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
    "playlist": "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"
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
# ARTBOARD 38: LANDSCAPE — PLAYLIST HUB MODE HAPUS SELEKTIF (MULTI-SELECT)
# =========================================================================
p1 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Landscape — Playlist Hub Mode Hapus Selektif (4-Column)",
        "x": 50, "y": 6200,
        "width": 936, "height": 420,
        "props": {"fill": DARK["bg"]}
    }
})

# Left Navigation Rail (72dp) with Selection Actions
add_rect(p1, 0, 0, 72, 420, DARK["surf_high"])
add_icon(p1, "close", 24, 28, size=24, color=WHITE)
add_icon(p1, "playlist", 24, 180, size=24, color=DARK["primary"])
add_icon(p1, "trash", 24, 340, size=24, color=DARK["badge_error_fg"])

# Top App Header Bar
add_rect(p1, 92, 16, 824, 52, DARK["surf"], corner=16)
add_text(p1, 112, 28, "Mode Hapus Playlist", size=15, color=WHITE, weight=700)
add_text(p1, 276, 30, "• 2 Playlist Dipilih (Ketuk untuk menandai)", size=12, color=DARK["secondary"], weight=700)

# "Pilih Semua" Pill Button in Top Bar
add_rect(p1, 620, 24, 105, 36, DARK["surf_highest"], corner=9999)
add_text(p1, 638, 33, "Pilih Semua", size=11, color=WHITE, weight=700)

# Action Pill "Hapus (2)" in Top Bar
add_rect(p1, 736, 24, 164, 36, DARK["badge_error_bg"], corner=9999)
add_icon(p1, "trash", 746, 32, size=18, color=DARK["badge_error_fg"])
add_text(p1, 770, 33, "Hapus 2 Playlist", size=11, color=DARK["badge_error_fg"], weight=700)

# 4-Column Playlist Grid (8 Playlists)
playlists_landscape = [
    ("Jersey_Local_Test", "3 Lagu • Lokal", True),      # Selected
    ("Japanese Rock", "8 Lagu • YouTube", False),
    ("Rock Workout", "15 Lagu • YouTube", True),        # Selected
    ("Lofi Santai", "12 Lagu • YouTube", False),
    ("Anime OST", "20 Lagu • YouTube", False),
    ("Playlist Baru", "0 Lagu • Kustom", False),
    ("DJ Jersey Club", "6 Lagu • YouTube", False),
    ("Bass Booster", "10 Lagu • Lokal", False)
]

col_w = 194
col_gap = 16
start_x = 92
start_y = 80
row_h = 150

for idx, (pl_name, pl_desc, is_sel) in enumerate(playlists_landscape):
    col = idx % 4
    row = idx // 4
    x = start_x + col * (col_w + col_gap)
    y = start_y + row * (row_h + 12)

    card_bg = DARK["selection_overlay"] if is_sel else DARK["surf"]
    add_rect(p1, x, y, col_w, row_h, card_bg, corner=18)

    # Artwork box
    add_rect(p1, x + 10, y + 10, col_w - 20, 84, DARK["surf_highest"], corner=12)
    add_icon(p1, "playlist", x + (col_w // 2) - 12, y + 38, size=24, color=WHITE)

    # Checkbox Badge
    if is_sel:
        add_rect(p1, x + col_w - 36, y + 14, 24, 24, DARK["check_bg"], corner=9999)
        add_icon(p1, "check", x + col_w - 32, y + 18, size=16, color=WHITE)
    else:
        add_rect(p1, x + col_w - 36, y + 14, 24, 24, DARK["uncheck_bg"], corner=9999)

    add_text(p1, x + 12, y + 102, pl_name[:16], size=12, color=WHITE, weight=700)
    add_text(p1, x + 12, y + 122, pl_desc, size=10, color=DARK["text_sub"])


# =========================================================================
# ARTBOARD 39: LANDSCAPE — DIALOG KONFIRMASI HAPUS PLAYLIST TERPILIH
# =========================================================================
p2 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Landscape — Dialog Konfirmasi Hapus Playlist Terpilih",
        "x": 1050, "y": 6200,
        "width": 936, "height": 420,
        "props": {"fill": "#180F10"}
    }
})

# Background silhouette
add_rect(p2, 72, 20, 844, 380, DARK["bg"], corner=24)

# Center Landscape Modal Dialog Card (680x300 dp)
add_rect(p2, 128, 60, 680, 300, DARK["surf"], corner=28)

# Left Column in Modal: Hero Icon & Info
add_rect(p2, 156, 88, 56, 56, DARK["badge_error_bg"], corner=9999)
add_icon(p2, "trash", 172, 104, size=24, color=DARK["badge_error_fg"])

add_text(p2, 228, 92, "Hapus 2 Playlist Terpilih?", size=18, color=WHITE, weight=700)
add_text(p2, 228, 118, "Playlist ini akan dihapus dari daftar putar aplikasi.", size=12, color=DARK["text_sub"])

# Selected List Box (Dual-column list inside modal)
add_rect(p2, 156, 160, 624, 76, DARK["surf_high"], corner=16)
add_text(p2, 176, 174, "• Jersey_Local_Test (3 lagu)", size=12, color=WHITE, weight=700)
add_text(p2, 176, 196, "• Rock Workout (15 lagu)", size=12, color=WHITE, weight=700)

add_text(p2, 440, 174, "✓ File audio di memori HP tetap aman.", size=11, color=DARK["badge_success_fg"], weight=700)
add_text(p2, 440, 196, "Tindakan ini tidak dapat dibatalkan.", size=11, color=DARK["badge_error_fg"])

# Action Buttons: Full Pill 9999px (Horizontal row at bottom of dialog)
# Button 1: Hapus Sekarang
add_rect(p2, 452, 252, 328, 48, DARK["primary"], corner=9999)
add_icon(p2, "trash", 516, 266, size=18, color=WHITE)
add_text(p2, 542, 267, "Hapus 2 Playlist Sekarang", size=13, color=WHITE, weight=700)

# Button 2: Batal
add_rect(p2, 156, 252, 280, 48, DARK["surf_high"], corner=9999)
add_text(p2, 280, 267, "Batal", size=13, color=WHITE, weight=700)

with open("/tmp/landscape_delete_script.json", "w") as f:
    json.dump(commands, f, indent=2)

print(f"Generated {len(commands)} commands. Running via figcraft-cli...")
run_res = subprocess.run([
    "figcraft-cli", "run",
    "--in", "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft",
    "--out", "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft",
    "/tmp/landscape_delete_script.json"
], capture_output=True, text=True)

if run_res.returncode != 0:
    print("Run error:", run_res.stderr)
else:
    print("Run succeeded! Exporting landscape artboards...")

# Export PNGs
exports = [
    (p1, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_landscape_playlist_delete_multiselect.png"),
    (p2, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_landscape_playlist_delete_confirm.png")
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
