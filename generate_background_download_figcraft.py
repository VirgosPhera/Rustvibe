import json
import subprocess
import zipfile

def get_next_id():
    with zipfile.ZipFile("/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft") as z:
        doc = json.loads(z.read("document.json").decode())
        return doc.get("next_id", 2040)

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
    "download": "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z",
    "close": "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z",
    "check": "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    "music": "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z",
    "shield": "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"
}

WHITE = "#FFFFFF"
DARK = {
    "bg": "#120B0C",           # Ultra Dark Android Lockscreen / System Shade
    "system_shade": "#221617",  # System shade background
    "card_bg": "#2E1E1F",       # Coffee Bean container
    "card_surf": "#382426",     # Surface Container
    "surf_high": "#452E30",    # Surface Container High
    "surf_highest": "#523639", # Surface Container Highest
    "primary": "#DC3500",      # Deep Crimson Carrot
    "secondary": "#E65100",    # Warm Carrot
    "text_main": WHITE,
    "text_sub": "#F8E6D2",
    "badge_bg": "#1B3B22",
    "badge_fg": "#78DC8B"
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

def add_text(parent_id, text, x, y, size=14, weight=400, color=WHITE):
    add_cmd("text.create", {
        "parent": parent_id,
        "x": x, "y": y,
        "text": text,
        "props": {
            "fontSize": size,
            "fontWeight": weight,
            "fill": color,
            "fontFamily": "Plus Jakarta Sans"
        }
    })

# ==============================================================================
# ARTBOARD 40: PORTRAIT — NOTIFIKASI SISTEM ANDROID (BACKGROUND DOWNLOAD)
# ==============================================================================
p40 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Portrait — Notifikasi Sistem Android (Background Download Ongoing)",
        "x": 2000,
        "y": 6200,
        "width": 420,
        "height": 936,
        "props": {"fill": DARK["bg"]}
    }
})

# Status Bar
add_text(p40, "09:41", 24, 14, size=13, weight=700, color=WHITE)
add_text(p40, "4G  100%", 340, 14, size=12, weight=700, color=WHITE)

# System Lockscreen / Notification Shade Header
add_text(p40, "Notifikasi Sistem", 24, 60, size=22, weight=800, color=WHITE)
add_text(p40, "Latar Belakang Tetap Berjalan Walau Layar Mati", 24, 90, size=12, weight=400, color=DARK["text_sub"])

# Main Foreground Notification Card (Material 3 Expressive Elevated Pill)
notif_y = 130
add_rect(p40, 16, notif_y, 388, 220, DARK["card_bg"], corner=28)

# Notification Header: App Icon + App Name + Timestamp
add_rect(p40, 32, notif_y + 20, 28, 28, DARK["primary"], corner=9999)
add_icon(p40, "download", 38, notif_y + 26, size=16, color=WHITE)
add_text(p40, "RustVibe • Unduhan Musik Aktif", 68, notif_y + 24, size=12, weight=700, color=DARK["text_sub"])
add_text(p40, "sekarang", 336, notif_y + 24, size=11, weight=400, color=DARK["text_sub"])

# Notification Title & Subtitle
add_text(p40, "Mengunduh: YJCA (Yang Jersey Club Aja)", 32, notif_y + 60, size=15, weight=700, color=WHITE)
add_text(p40, "Lagu 14 dari 26 • 2.4 MB/s (Stabil)", 32, notif_y + 84, size=13, weight=400, color=DARK["text_sub"])

# Progress Bar Container & Active Bar
add_rect(p40, 32, notif_y + 112, 356, 8, DARK["surf_highest"], corner=9999)
add_rect(p40, 32, notif_y + 112, 192, 8, DARK["primary"], corner=9999) # 54% progress

# Progress Percentage & Track Details
add_text(p40, "54%", 32, notif_y + 128, size=12, weight=700, color=DARK["primary"])
add_text(p40, "Sedang unduh: YOASOBI - Idol (Jersey Club)", 70, notif_y + 128, size=11, weight=400, color=DARK["text_sub"])

# Notification Action Buttons (Pill 9999px)
add_rect(p40, 32, notif_y + 158, 140, 38, DARK["surf_high"], corner=9999)
add_text(p40, "Buka Aplikasi", 62, notif_y + 169, size=12, weight=700, color=WHITE)

add_rect(p40, 184, notif_y + 158, 140, 38, "#52170B", corner=9999)
add_text(p40, "Hentikan", 226, notif_y + 169, size=12, weight=700, color="#FF897D")

# Security & Background Persistence Guarantee Card
card2_y = 370
add_rect(p40, 16, card2_y, 388, 180, DARK["card_surf"], corner=24)
add_rect(p40, 32, card2_y + 20, 36, 36, DARK["badge_bg"], corner=9999)
add_icon(p40, "shield", 40, card2_y + 28, size=20, color=DARK["badge_fg"])
add_text(p40, "Garansi Background Service", 80, card2_y + 22, size=16, weight=700, color=WHITE)
add_text(p40, "Android Foreground Service & WakeLock", 80, card2_y + 42, size=12, weight=400, color=DARK["badge_fg"])

add_text(p40, "• Download TIDAK AKAN mati saat keluar dari aplikasi", 32, card2_y + 75, size=12, weight=400, color=DARK["text_sub"])
add_text(p40, "• Layar HP bisa dikunci / dimatikan tanpa putus", 32, card2_y + 100, size=12, weight=400, color=DARK["text_sub"])
add_text(p40, "• Bebas buka WhatsApp, game, YouTube, atau sosmed", 32, card2_y + 125, size=12, weight=400, color=DARK["text_sub"])
add_text(p40, "• Auto-save per lagu langsung tersimpan di SQLite Room", 32, card2_y + 150, size=12, weight=700, color=WHITE)

# ==============================================================================
# ARTBOARD 41: LANDSCAPE — NOTIFIKASI & STATUS DUAL-PANE (936x420)
# ==============================================================================
p41 = alloc_id()
commands.append({
    "command": "frame.create",
    "params": {
        "name": "Landscape — Notifikasi Sistem & Floating Download Banner",
        "x": 2000,
        "y": 7200,
        "width": 936,
        "height": 420,
        "props": {"fill": DARK["bg"]}
    }
})

# Left Pane: Notification Tray Item (420px)
add_rect(p41, 24, 24, 420, 240, DARK["card_bg"], corner=28)
add_rect(p41, 44, 44, 28, 28, DARK["primary"], corner=9999)
add_icon(p41, "download", 50, 50, size=16, color=WHITE)
add_text(p41, "RustVibe • Foreground Service", 82, 48, size=12, weight=700, color=DARK["text_sub"])
add_text(p41, "Mengunduh 26 Lagu di Latar Belakang", 44, 86, size=16, weight=800, color=WHITE)
add_text(p41, "Lagu 14/26: YOASOBI - Idol • 2.4 MB/s", 44, 114, size=13, weight=400, color=DARK["text_sub"])
add_rect(p41, 44, 142, 380, 8, DARK["surf_highest"], corner=9999)
add_rect(p41, 44, 142, 205, 8, DARK["primary"], corner=9999)
add_text(p41, "54% Selesai — HP Boleh Dimatikan", 44, 160, size=12, weight=700, color=DARK["primary"])
add_rect(p41, 44, 190, 130, 36, DARK["surf_high"], corner=9999)
add_text(p41, "Buka RustVibe", 66, 200, size=12, weight=700, color=WHITE)

# Right Pane: Real-time In-App Floating Sync Card (440px)
add_rect(p41, 470, 24, 440, 370, DARK["card_surf"], corner=28)
add_text(p41, "Sinkronisasi State Aplikasi & SQLite", 496, 48, size=18, weight=800, color=WHITE)
add_text(p41, "Ketika aplikasi dibuka kembali, semua lagu otomatis muncul", 496, 76, size=12, weight=400, color=DARK["text_sub"])

add_rect(p41, 496, 110, 388, 56, DARK["surf_high"], corner=18)
add_text(p41, "13 Lagu Telah Tersimpan di Penyimpanan Internal", 516, 122, size=13, weight=700, color=WHITE)
add_text(p41, "Format M4A 128kbps AAC • ID3 & Album Art Utuh", 516, 142, size=11, weight=400, color=DARK["badge_fg"])

add_rect(p41, 496, 178, 388, 56, DARK["surf_high"], corner=18)
add_text(p41, "1 Lagu Sedang Berjalan: YOASOBI - Idol", 516, 190, size=13, weight=700, color=WHITE)
add_text(p41, "DirectYouTubeStreamExtractor (VISIONOS + ANDROID_VR)", 516, 210, size=11, weight=700, color=DARK["primary"])

add_rect(p41, 496, 246, 388, 56, DARK["surf_high"], corner=18)
add_text(p41, "12 Lagu Antrean di Memori Background Service", 516, 258, size=13, weight=700, color=WHITE)
add_text(p41, "Otomatis lanjut tanpa campur tangan pengguna", 516, 278, size=11, weight=400, color=DARK["text_sub"])

add_rect(p41, 496, 320, 388, 48, DARK["primary"], corner=9999)
add_text(p41, "Semua Proses Terlindungi oleh WakeLock Sistem", 540, 336, size=13, weight=700, color=WHITE)

# Save script
script_path = "/home/amir/projects/rust-music-android/add_background_download_figcraft.json"
with open(script_path, "w") as f:
    json.dump(commands, f, indent=2)

print(f"Generated {len(commands)} commands. Starting ID: {START_ID}, Ending ID: {current_id}")
