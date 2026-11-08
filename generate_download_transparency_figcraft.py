import json
import subprocess
import zipfile

def get_next_id():
    with zipfile.ZipFile("/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft") as z:
        doc = json.loads(z.read("document.json").decode())
        return doc.get("next_id", 1638)

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
    "download": "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z",
    "check": "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    "warning": "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z",
    "refresh": "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z",
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
    "badge_progress_bg": "#4A2B00",
    "badge_progress_fg": "#FFAE42",
    "badge_wait_bg": "#382426",
    "badge_wait_fg": "#D2B48C"
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
# ARTBOARD 1: PORTRAIT — DOWNLOAD PROGRESS & STATUS
# =========================================================================
p1 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Portrait — Modal Unduh Playlist Transparan & Indikator Status",
        "x": 50, "y": 4200,
        "width": 420, "height": 936,
        "props": {"fill": DARK["bg"]}
    }
})

# Top App Bar
add_rect(p1, 0, 0, 420, 96, DARK["surf"])
add_icon(p1, "back", 24, 48, size=24, color=WHITE)
add_text(p1, 68, 50, "Unduh Playlist YouTube", size=18, color=WHITE, weight=700)
add_icon(p1, "close", 372, 48, size=24, color=WHITE)

# Playlist Info Card
add_rect(p1, 20, 116, 380, 90, DARK["surf"], corner=20)
add_rect(p1, 32, 126, 70, 70, DARK["primary"], corner=16)
add_icon(p1, "playlist", 53, 147, size=28, color=WHITE)
add_text(p1, 114, 134, "YJCA (YANG JERSEY CLUB AJA)", size=15, color=WHITE, weight=700)
add_text(p1, 114, 158, "YouTube Music • 26 Lagu Terdeteksi", size=12, color=DARK["text_sub"])
add_text(p1, 114, 178, "Format: M4A Original • Estimasi: ~82 MB", size=11, color=DARK["secondary"])

# Big Expressive Progress Section
add_rect(p1, 20, 222, 380, 114, DARK["surf_high"], corner=24)
add_text(p1, 40, 238, "Mengunduh Aliran Audio...", size=14, color=WHITE, weight=700)
add_text(p1, 310, 238, "14 / 26", size=14, color=DARK["primary"], weight=700)

# Progress Track & Fill
add_rect(p1, 40, 268, 340, 16, DARK["surf_highest"], corner=9999)
add_rect(p1, 40, 268, 183, 16, DARK["primary"], corner=9999)

# Progress Details
add_text(p1, 40, 298, "54% Selesai", size=12, color=WHITE, weight=700)
add_text(p1, 160, 298, "1.8 MB/s", size=12, color=DARK["text_sub"])
add_text(p1, 275, 298, "Sisa ~45 detik", size=12, color=DARK["text_sub"])

# Section Header
add_text(p1, 24, 356, "Live Status Unduhan per Lagu", size=15, color=WHITE, weight=700)
add_text(p1, 260, 358, "12 Sukses • 1 Gagal • 1 Proses", size=11, color=DARK["text_sub"])

tracks = [
    ("1", "Boss Up Pt2", "DJ Smallz 732", "3.4 MB", "Tersimpan", "badge_success"),
    ("2", "rrodney - Do You Remember...", "rrodney", "2.8 MB", "Tersimpan", "badge_success"),
    ("3", "Amerie - 1 thing (Jersey Remix)", "Amerie", "3.1 MB", "Tersimpan", "badge_success"),
    ("4", "Jeremih - oui (Jersey Club Mix)", "Jeremih", "—", "Gagal (403)", "badge_error"),
    ("5", "Cash Cobain - Fisher (Jersey Beat)", "Cash Cobain", "48%", "Unduh 48%", "badge_progress"),
    ("6", "Ice Spice - Deli (Jersey Edit)", "Ice Spice", "—", "Antrean #6", "badge_wait"),
    ("7", "PinkPantheress - Boy's a liar", "PinkPantheress", "—", "Antrean #7", "badge_wait"),
]

item_y = 388
for num, title, artist, meta, status_label, badge_type in tracks:
    bg_color = DARK["surf"] if badge_type != "badge_progress" else DARK["surf_high"]
    add_rect(p1, 20, item_y, 380, 56, bg_color, corner=16)

    add_text(p1, 34, item_y + 19, num, size=13, color=DARK["text_sub"], weight=700)
    add_text(p1, 62, item_y + 11, title[:26], size=13, color=WHITE, weight=700)
    add_text(p1, 62, item_y + 31, f"{artist} • {meta}", size=11, color=DARK["text_sub"])

    if badge_type == "badge_success":
        badge_bg = DARK["badge_success_bg"]
        badge_fg = DARK["badge_success_fg"]
        icon_n = "check"
    elif badge_type == "badge_error":
        badge_bg = DARK["badge_error_bg"]
        badge_fg = DARK["badge_error_fg"]
        icon_n = "warning"
    elif badge_type == "badge_progress":
        badge_bg = DARK["badge_progress_bg"]
        badge_fg = DARK["badge_progress_fg"]
        icon_n = "refresh"
    else:
        badge_bg = DARK["badge_wait_bg"]
        badge_fg = DARK["badge_wait_fg"]
        icon_n = "download"

    add_rect(p1, 275, item_y + 14, 115, 28, badge_bg, corner=9999)
    add_icon(p1, icon_n, 283, item_y + 20, size=14, color=badge_fg)
    add_text(p1, 303, item_y + 21, status_label, size=10, color=badge_fg, weight=700)

    item_y += 64

# Bottom Actions
add_rect(p1, 20, 856, 185, 52, DARK["surf_high"], corner=9999)
add_text(p1, 56, 872, "Lanjut di Latar", size=13, color=WHITE, weight=700)

add_rect(p1, 215, 856, 185, 52, DARK["badge_error_bg"], corner=9999)
add_text(p1, 258, 872, "Batalkan Unduhan", size=13, color=DARK["badge_error_fg"], weight=700)

# =========================================================================
# ARTBOARD 2: PORTRAIT — DIALOG HASIL UNDUHAN JUJUR & RETRY
# =========================================================================
p2 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Portrait — Dialog Hasil Unduhan Jujur & Retry Gagal",
        "x": 520, "y": 4200,
        "width": 420, "height": 936,
        "props": {"fill": "#180F10"}
    }
})

# Background silhouette
add_rect(p2, 20, 80, 380, 700, DARK["bg"], corner=24)

# Modal Card
add_rect(p2, 24, 210, 372, 516, DARK["surf"], corner=28)

# Top Icon & Title
add_rect(p2, 180, 234, 60, 60, DARK["primary"], corner=9999)
add_icon(p2, "check", 196, 250, size=28, color=WHITE)
add_text(p2, 90, 306, "Hasil Unduhan Playlist", size=20, color=WHITE, weight=700)
add_text(p2, 60, 334, "Playlist 'YJCA (YANG JERSEY CLUB AJA)'", size=13, color=DARK["text_sub"])

# Statistics Boxes
add_rect(p2, 44, 366, 160, 74, DARK["surf_high"], corner=20)
add_rect(p2, 56, 378, 32, 32, DARK["badge_success_bg"], corner=9999)
add_icon(p2, "check", 63, 385, size=18, color=DARK["badge_success_fg"])
add_text(p2, 96, 376, "24 Lagu", size=16, color=WHITE, weight=700)
add_text(p2, 96, 400, "Tersimpan", size=11, color=DARK["badge_success_fg"], weight=700)
add_text(p2, 96, 416, "Di DB & Storage", size=9, color=DARK["text_sub"])

add_rect(p2, 216, 366, 160, 74, DARK["surf_high"], corner=20)
add_rect(p2, 228, 378, 32, 32, DARK["badge_error_bg"], corner=9999)
add_icon(p2, "warning", 235, 385, size=18, color=DARK["badge_error_fg"])
add_text(p2, 268, 376, "2 Lagu", size=16, color=WHITE, weight=700)
add_text(p2, 268, 400, "Gagal Diunduh", size=11, color=DARK["badge_error_fg"], weight=700)
add_text(p2, 268, 416, "Bisa Coba Lagi", size=9, color=DARK["text_sub"])

# Info Banner
add_rect(p2, 44, 452, 332, 84, DARK["surf_highest"], corner=16)
add_text(p2, 56, 462, "2 Lagu Belum Masuk:", size=11, color=DARK["badge_error_fg"], weight=700)
add_text(p2, 56, 482, "• Jeremih - oui (Precondition check failed)", size=11, color=WHITE)
add_text(p2, 56, 502, "• Cash Cobain - Fisher (Stream timeout)", size=11, color=WHITE)
add_text(p2, 56, 520, "Tekan tombol di bawah untuk unduh ulang 2 lagu ini.", size=9, color=DARK["text_sub"])

# Buttons
add_rect(p2, 44, 550, 332, 48, DARK["primary"], corner=9999)
add_icon(p2, "refresh", 95, 564, size=18, color=WHITE)
add_text(p2, 122, 565, "Coba Lagi 2 Lagu yang Gagal", size=13, color=WHITE, weight=700)

add_rect(p2, 44, 608, 332, 48, DARK["surf_high"], corner=9999)
add_icon(p2, "playlist", 112, 622, size=18, color=WHITE)
add_text(p2, 138, 623, "Buka Playlist (24 Lagu)", size=13, color=WHITE, weight=700)

add_rect(p2, 136, 668, 148, 36, DARK["surf"], corner=9999)
add_text(p2, 176, 678, "Tutup Dialog", size=12, color=DARK["text_sub"], weight=700)

# =========================================================================
# ARTBOARD 3: LANDSCAPE — DUAL-PANE PROGRESS & STATUS
# =========================================================================
p3 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Landscape — Modal Unduh Playlist Transparan (Dual-Pane)",
        "x": 990, "y": 4200,
        "width": 936, "height": 420,
        "props": {"fill": DARK["bg"]}
    }
})

# Navigation Rail
add_rect(p3, 0, 0, 72, 420, DARK["surf"])
add_icon(p3, "back", 24, 28, size=24, color=WHITE)
add_icon(p3, "playlist", 24, 180, size=24, color=DARK["primary"])
add_icon(p3, "download", 24, 250, size=24, color=WHITE)

# Left Pane
add_rect(p3, 92, 20, 390, 380, DARK["surf"], corner=24)
add_text(p3, 116, 42, "Mengunduh Playlist YouTube", size=16, color=WHITE, weight=700)
add_text(p3, 116, 66, "YJCA (YANG JERSEY CLUB AJA)", size=13, color=DARK["secondary"], weight=700)
add_text(p3, 116, 86, "26 Lagu • Format M4A Asli", size=11, color=DARK["text_sub"])

add_rect(p3, 116, 116, 342, 100, DARK["surf_high"], corner=20)
add_text(p3, 134, 132, "14 dari 26 Lagu Selesai", size=14, color=WHITE, weight=700)
add_text(p3, 390, 132, "54%", size=14, color=DARK["primary"], weight=700)
add_rect(p3, 134, 158, 306, 14, DARK["surf_highest"], corner=9999)
add_rect(p3, 134, 158, 165, 14, DARK["primary"], corner=9999)
add_text(p3, 134, 184, "Kecepatan: 1.8 MB/s • Sisa ~45s", size=11, color=DARK["text_sub"])

add_rect(p3, 116, 230, 164, 54, DARK["badge_success_bg"], corner=16)
add_icon(p3, "check", 128, 248, size=18, color=DARK["badge_success_fg"])
add_text(p3, 154, 242, "12 Lagu Sukses", size=12, color=DARK["badge_success_fg"], weight=700)
add_text(p3, 154, 260, "Tersimpan di Disk", size=10, color=WHITE)

add_rect(p3, 294, 230, 164, 54, DARK["badge_error_bg"], corner=16)
add_icon(p3, "warning", 306, 248, size=18, color=DARK["badge_error_fg"])
add_text(p3, 332, 242, "1 Lagu Gagal", size=12, color=DARK["badge_error_fg"], weight=700)
add_text(p3, 332, 260, "Bisa Diulang", size=10, color=WHITE)

add_rect(p3, 116, 302, 342, 44, DARK["primary"], corner=9999)
add_icon(p3, "refresh", 170, 314, size=18, color=WHITE)
add_text(p3, 196, 315, "Coba Lagi Lagu Gagal", size=13, color=WHITE, weight=700)

add_rect(p3, 116, 356, 342, 36, DARK["surf_high"], corner=9999)
add_text(p3, 218, 365, "Lanjut di Latar Belakang", size=12, color=DARK["text_sub"], weight=700)

# Right Pane
add_rect(p3, 498, 20, 418, 380, DARK["surf"], corner=24)
add_text(p3, 520, 42, "Daftar Antrean & Status Per Lagu", size=15, color=WHITE, weight=700)

right_tracks = [
    ("1", "Boss Up Pt2", "DJ Smallz 732", "Tersimpan", "badge_success"),
    ("2", "rrodney - Do You Remember...", "rrodney", "Tersimpan", "badge_success"),
    ("3", "Amerie - 1 thing (Jersey Club)", "Amerie", "Tersimpan", "badge_success"),
    ("4", "Jeremih - oui (Jersey Club Mix)", "Jeremih", "Gagal (403)", "badge_error"),
    ("5", "Cash Cobain - Fisher (Jersey Beat)", "Cash Cobain", "Unduh 48%", "badge_progress"),
]

ry = 76
for num, title, artist, status_lbl, b_type in right_tracks:
    add_rect(p3, 516, ry, 382, 54, DARK["surf_high"], corner=14)
    add_text(p3, 528, ry + 18, num, size=12, color=DARK["text_sub"], weight=700)
    add_text(p3, 552, ry + 11, title[:26], size=13, color=WHITE, weight=700)
    add_text(p3, 552, ry + 31, artist, size=11, color=DARK["text_sub"])

    if b_type == "badge_success":
        bg = DARK["badge_success_bg"]
        fg = DARK["badge_success_fg"]
        ic = "check"
    elif b_type == "badge_error":
        bg = DARK["badge_error_bg"]
        fg = DARK["badge_error_fg"]
        ic = "warning"
    else:
        bg = DARK["badge_progress_bg"]
        fg = DARK["badge_progress_fg"]
        ic = "refresh"

    add_rect(p3, 786, ry + 13, 102, 28, bg, corner=9999)
    add_icon(p3, ic, 792, ry + 19, size=14, color=fg)
    add_text(p3, 810, ry + 20, status_lbl, size=10, color=fg, weight=700)

    ry += 62

with open("/tmp/download_transparency_script.json", "w") as f:
    json.dump(commands, f, indent=2)

print(f"Generated {len(commands)} commands. Running via figcraft-cli...")
run_res = subprocess.run([
    "figcraft-cli", "run",
    "--in", "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft",
    "--out", "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft",
    "/tmp/download_transparency_script.json"
], capture_output=True, text=True)

if run_res.returncode != 0:
    print("Run error:", run_res.stderr)
else:
    print("Run succeeded! Exporting artboards...")

# Export PNGs
exports = [
    (p1, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_download_progress_transparent.png"),
    (p2, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_download_result_retry.png"),
    (p3, "/home/amir/projects/rust-music-android/docs/screenshots/figcraft_landscape_download_progress.png")
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
