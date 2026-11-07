import socket
import json
import time

class FigCraftBuilder:
    def __init__(self, host='127.0.0.1', port=7979):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((host, port))
        self.reader = self.sock.makefile('r', encoding='utf-8')
        self._id = 0

    def call(self, command, params=None):
        self._id += 1
        payload = {
            "id": self._id,
            "method": "engine.execute",
            "params": {
                "command": command,
                "params": params or {}
            }
        }
        self.sock.sendall((json.dumps(payload) + '\n').encode('utf-8'))
        line = self.reader.readline()
        if not line:
            return None
        res = json.loads(line)
        if not res.get('ok'):
            print(f"Warn: {command} -> {res}")
        return res

    def close(self):
        self.sock.close()

# SVG VECTOR PATH CATALOG
ICONS = {
    "home": "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z",
    "search": "M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
    "playlist": "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z",
    "profile": "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z",
    "music": "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z",
    "play": "M8 5v14l11-7z",
    "pause": "M6 19h4V5H6v14zm8-14v14h4V5h-4z",
    "next": "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z",
    "prev": "M6 6h2v12H6zm3.5 6l8.5 6V6z",
    "download": "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z",
    "check": "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    "close": "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z",
    "sun": "M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1z",
    "moon": "M9.37 5.51A7.35 7.35 0 0 0 9.1 7.5c0 4.08 3.32 7.4 7.4 7.4.68 0 1.35-.09 1.99-.27A9.014 9.014 0 0 1 12 21c-4.97 0-9-4.03-9-9 0-3.93 2.51-7.27 6.37-8.49z",
    "back": "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z",
    "plus": "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",
    "bolt": "M11 21h-1l1-7H7.5c-.88 0-.33-.75-.31-.78C8.48 10.94 10.42 7.54 13.01 3h1l-1 7h3.5c.49 0 .56.33.47.51l-5.98 10.49z",
    "trash": "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
    "drag": "M11 18c0 1.1-.9 2-2 2s-2-.9-2-2 .9-2 2-2 2 .9 2 2zm-2-8c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0-6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm6 4c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
}

def build_complete_landscape_suite():
    c = FigCraftBuilder()

    SCREEN_W = 936
    SCREEN_H = 420
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
        "accent": "#52170B"
    }

    def add_icon(parent_id, icon_name, x, y, size=24, color=WHITE):
        path = ICONS.get(icon_name, ICONS["music"])
        return c.call("vector.create", {
            "parent": parent_id,
            "x": x, "y": y,
            "width": size, "height": size,
            "path": path,
            "props": {"fill": color}
        })

    def add_nav_rail(screen_id, active_tab_index=1):
        c.call("shape.rectangle", {
            "parent": screen_id, "x": 0, "y": 0, "width": 72, "height": SCREEN_H,
            "name": "NavigationRail",
            "props": {"fill": DARK["surf"], "cornerRadius": [0, 24, 24, 0]}
        })

        # Top Badge
        c.call("shape.ellipse", {"parent": screen_id, "x": 18, "y": 18, "width": 36, "height": 36, "props": {"fill": DARK["primary"]}})
        add_icon(screen_id, "music", 24, 24, size=24, color=WHITE)

        tabs = [
            ("home", "Beranda"),
            ("search", "Cari"),
            ("playlist", "Playlist"),
            ("profile", "Profil")
        ]
        start_y = 80
        for idx, (icon_key, label) in enumerate(tabs):
            item_y = start_y + (idx * 68)
            if idx == active_tab_index:
                c.call("shape.rectangle", {
                    "parent": screen_id, "x": 10, "y": item_y, "width": 52, "height": 34,
                    "name": f"ActiveRailPill_{idx}",
                    "props": {"fill": DARK["primary"], "cornerRadius": 9999}
                })
                add_icon(screen_id, icon_key, 24, item_y + 5, size=24, color=WHITE)
                c.call("text.create", {"parent": screen_id, "x": 14, "y": item_y + 38, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": DARK["primary"]}})
            else:
                add_icon(screen_id, icon_key, 24, item_y + 5, size=24, color=DARK["text_sub"])
                c.call("text.create", {"parent": screen_id, "x": 14, "y": item_y + 38, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_sub"]}})

        # Theme switch button at bottom
        c.call("shape.ellipse", {"parent": screen_id, "x": 18, "y": SCREEN_H - 46, "width": 36, "height": 36, "props": {"fill": DARK["surf_high"]}})
        add_icon(screen_id, "moon", 26, SCREEN_H - 38, size=20, color=DARK["text_sub"])


    # =========================================================================
    # ARTBOARD 1: LANDSCAPE CARI (SEARCH) DENGAN PROGRESS DOWNLOAD PERSENTASE
    # =========================================================================
    s_search = c.call("frame.create", {
        "x": 50, "y": 3100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape 1 — Cari & Download Progress Bar (%)",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(s_search, active_tab_index=1)

    # Top Search Bar (X: 92, Width 824)
    c.call("shape.rectangle", {"parent": s_search, "x": 92, "y": 16, "width": 824, "height": 46, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    add_icon(s_search, "search", 108, 28, size=22, color=DARK["text_sub"])
    c.call("text.create", {"parent": s_search, "x": 142, "y": 29, "text": "dia - MALU MALU", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    add_icon(s_search, "close", 880, 27, size=22, color=DARK["text_sub"])

    # Two-Column Search Results & Live Download Progress
    # Left Column: Search Results List (Width 400)
    c.call("text.create", {"parent": s_search, "x": 92, "y": 74, "text": "Hasil Pencarian", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_sub"]}})

    # Item 1: MALU MALU (Currently Downloading!)
    c.call("shape.rectangle", {"parent": s_search, "x": 92, "y": 98, "width": 400, "height": 64, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s_search, "x": 102, "y": 106, "width": 48, "height": 48, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
    add_icon(s_search, "music", 114, 118, size=24, color=DARK["secondary"])
    c.call("text.create", {"parent": s_search, "x": 160, "y": 112, "text": "MALU MALU", "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_search, "x": 160, "y": 132, "text": "dia • 03:26", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    # Downloading Spinner Pill
    c.call("shape.rectangle", {"parent": s_search, "x": 390, "y": 114, "width": 90, "height": 32, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_search, "x": 408, "y": 121, "text": "68%", "props": {"fontSize": 12, "fill": WHITE, "fontWeight": 700}})

    # Item 2
    c.call("shape.rectangle", {"parent": s_search, "x": 92, "y": 170, "width": 400, "height": 64, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s_search, "x": 102, "y": 178, "width": 48, "height": 48, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
    add_icon(s_search, "music", 114, 190, size=24, color=DARK["secondary"])
    c.call("text.create", {"parent": s_search, "x": 160, "y": 184, "text": "MALU MALU (MV)", "props": {"fontSize": 14, "fill": WHITE}})
    c.call("text.create", {"parent": s_search, "x": 160, "y": 204, "text": "dia & INDAHKUS • 03:30", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    add_icon(s_search, "download", 450, 188, size=22, color=DARK["primary"])

    # Item 3
    c.call("shape.rectangle", {"parent": s_search, "x": 92, "y": 242, "width": 400, "height": 64, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s_search, "x": 102, "y": 250, "width": 48, "height": 48, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
    add_icon(s_search, "music", 114, 262, size=24, color=DARK["secondary"])
    c.call("text.create", {"parent": s_search, "x": 160, "y": 256, "text": "Silhouette", "props": {"fontSize": 14, "fill": WHITE}})
    c.call("text.create", {"parent": s_search, "x": 160, "y": 276, "text": "KANA-BOON • 04:00", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    add_icon(s_search, "download", 450, 260, size=22, color=DARK["primary"])

    # Right Column: DEDICATED LIVE DOWNLOAD PROGRESS CARD (Width 408)
    c.call("text.create", {"parent": s_search, "x": 508, "y": 74, "text": "Status Unduhan Berjalan", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": s_search, "x": 508, "y": 98, "width": 408, "height": 208, "props": {"fill": DARK["surf"], "cornerRadius": 20}})

    # Header inside card
    c.call("text.create", {"parent": s_search, "x": 530, "y": 120, "text": "Mengunduh Aliran Audio...", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_search, "x": 530, "y": 144, "text": "dia - MALU MALU • Format M4A Asli", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    # Large Linear Progress Bar (9999px Full Pill)
    c.call("shape.rectangle", {"parent": s_search, "x": 530, "y": 176, "width": 364, "height": 14, "props": {"fill": DARK["surf_highest"], "cornerRadius": 9999}})
    # 68% Filled
    c.call("shape.rectangle", {"parent": s_search, "x": 530, "y": 176, "width": 248, "height": 14, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})

    # Progress Info & Percentage
    c.call("text.create", {"parent": s_search, "x": 530, "y": 202, "text": "68% Selesai (2.3 MB / 3.4 MB)", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": DARK["primary"], "fontWeight": 700}})
    c.call("text.create", {"parent": s_search, "x": 780, "y": 202, "text": "Kecepatan 1.8 MB/s", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Step Checkmarks
    c.call("text.create", {"parent": s_search, "x": 530, "y": 236, "text": "✓ Sampul HD Terunduh  •  ✓ Lirik LRCLIB Sinkron OK", "props": {"fontSize": 11, "fill": "#4CAF50"}})

    # Bottom Landscape Floating Player
    c.call("shape.rectangle", {"parent": s_search, "x": 92, "y": 352, "width": 824, "height": 54, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s_search, "x": 100, "y": 359, "width": 40, "height": 40, "props": {"fill": DARK["accent"]}})
    add_icon(s_search, "music", 110, 369, size=20, color=DARK["secondary"])
    c.call("text.create", {"parent": s_search, "x": 150, "y": 363, "text": "MALU MALU", "props": {"fontSize": 13, "fill": WHITE}})
    c.call("text.create", {"parent": s_search, "x": 150, "y": 381, "text": "dia", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.ellipse", {"parent": s_search, "x": 860, "y": 360, "width": 38, "height": 38, "props": {"fill": DARK["primary"]}})
    add_icon(s_search, "pause", 870, 370, size=18, color=WHITE)


    # =========================================================================
    # ARTBOARD 2: MODAL DOWNLOAD 1 PLAYLIST YOUTUBE LENGKAP (RENAME REQUEST)
    # =========================================================================
    s_playlist_modal = c.call("frame.create", {
        "x": 1050, "y": 3100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape 2 — Modal Download 1 Playlist (Rename Flow)",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(s_playlist_modal, active_tab_index=1)

    # Scrim Background
    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 72, "y": 0, "width": 864, "height": SCREEN_H, "props": {"fill": "#1A1012", "opacity": 0.85}})

    # Center Modal Card (Width 560, Height 360)
    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 224, "y": 30, "width": 560, "height": 360, "props": {"fill": DARK["surf"], "cornerRadius": 24}})

    # Modal Header
    add_icon(s_playlist_modal, "playlist", 250, 50, size=26, color=DARK["primary"])
    c.call("text.create", {"parent": s_playlist_modal, "x": 286, "y": 52, "text": "Unduh 1 Playlist Lengkap", "props": {"fontSize": 18, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 286, "y": 76, "text": "Terdeteksi URL Playlist YouTube • 8 Lagu Ditemukan", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    # Playlist Name Input Section (Pilihan Ikuti Nama Asli / Ganti Nama Kustom)
    c.call("text.create", {"parent": s_playlist_modal, "x": 250, "y": 114, "text": "Nama Playlist di RustVibe:", "props": {"fontSize": 12, "fill": DARK["text_sub"], "fontWeight": 700}})
    
    # Input Field 9999px (Editable)
    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 250, "y": 136, "width": 508, "height": 48, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 270, "y": 150, "text": "Japanese Rock Anime Hits (Official)", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    # Edit / Rename Indicator Icon
    add_icon(s_playlist_modal, "bolt", 720, 148, size=20, color=DARK["secondary"])

    # Quick Name Options Pills
    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 250, "y": 194, "width": 146, "height": 28, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 264, "y": 200, "text": "✓ Ikuti Nama Asli", "props": {"fontSize": 11, "fill": WHITE, "fontWeight": 700}})

    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 404, "y": 194, "width": 140, "height": 28, "props": {"fill": DARK["surf_highest"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 418, "y": 200, "text": "✎ Ganti Nama Kustom", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Batch Track Preview Card (Height 60)
    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 250, "y": 236, "width": 508, "height": 62, "props": {"fill": DARK["surf_high"], "cornerRadius": 14}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 266, "y": 248, "text": "Termasuk: Silhouette, Gurenge, Blue Bird, Idol + 4 lainnya", "props": {"fontSize": 12, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 266, "y": 270, "text": "Otomatis mengunduh cover HD & lirik sinkron milidetik", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Action Buttons: Batal & Unduh Semua 9999px
    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 250, "y": 320, "width": 140, "height": 44, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 300, "y": 332, "text": "Batal", "props": {"fontSize": 13, "fill": DARK["text_sub"]}})

    c.call("shape.rectangle", {"parent": s_playlist_modal, "x": 404, "y": 320, "width": 354, "height": 44, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_playlist_modal, "x": 480, "y": 332, "text": "Mulai Unduh Semua (8 Lagu)", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})


    # =========================================================================
    # ARTBOARD 3: LANDSCAPE KOLEKSI PLAYLIST (GRID 4-KOLOM PROPORSIONAL)
    # =========================================================================
    s_hub = c.call("frame.create", {
        "x": 2050, "y": 3100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape 3 — Koleksi Playlist Hub (4-Column Grid)",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(s_hub, active_tab_index=2)

    # Header Row
    c.call("text.create", {"parent": s_hub, "x": 92, "y": 20, "text": "Koleksi Playlist", "props": {"fontSize": 22, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})
    
    # "+ Buat Baru" Button 9999px
    c.call("shape.rectangle", {"parent": s_hub, "x": 780, "y": 16, "width": 136, "height": 36, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    add_icon(s_hub, "plus", 792, 23, size=18, color=WHITE)
    c.call("text.create", {"parent": s_hub, "x": 818, "y": 25, "text": "Buat Baru", "props": {"fontSize": 12, "fill": WHITE, "fontWeight": 700}})

    # 4-Column Playlist Grid (Width 194 each, gap 16)
    playlists_data = [
        ("Lofi Santai", "4 Lagu • 14 min", 92, 70),
        ("Rock Workout", "2 Lagu • 7 min", 302, 70),
        ("Anime OST", "8 Lagu • 28 min", 512, 70),
        ("Favorit", "12 Lagu • 44 min", 722, 70),
        ("Chill Vibes", "5 Lagu • 18 min", 92, 206),
        ("Night Drive", "6 Lagu • 21 min", 302, 206),
        ("Indie Akustik", "3 Lagu • 11 min", 512, 206),
        ("Baru Diunduh", "15 Lagu • 52 min", 722, 206),
    ]
    for name, sub, px, py in playlists_data:
        c.call("shape.rectangle", {"parent": s_hub, "x": px, "y": py, "width": 194, "height": 120, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
        c.call("shape.rectangle", {"parent": s_hub, "x": px + 12, "y": py + 12, "width": 46, "height": 46, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
        add_icon(s_hub, "music", px + 23, py + 23, size=22, color=DARK["secondary"])
        c.call("text.create", {"parent": s_hub, "x": px + 12, "y": py + 68, "text": name, "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
        c.call("text.create", {"parent": s_hub, "x": px + 12, "y": py + 88, "text": sub, "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Floating Player Bottom
    c.call("shape.rectangle", {"parent": s_hub, "x": 92, "y": 352, "width": 824, "height": 54, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s_hub, "x": 100, "y": 359, "width": 40, "height": 40, "props": {"fill": DARK["accent"]}})
    add_icon(s_hub, "music", 110, 369, size=20, color=DARK["secondary"])
    c.call("text.create", {"parent": s_hub, "x": 150, "y": 363, "text": "MALU MALU", "props": {"fontSize": 13, "fill": WHITE}})
    c.call("text.create", {"parent": s_hub, "x": 150, "y": 381, "text": "dia", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.ellipse", {"parent": s_hub, "x": 860, "y": 360, "width": 38, "height": 38, "props": {"fill": DARK["primary"]}})
    add_icon(s_hub, "pause", 870, 370, size=18, color=WHITE)


    # =========================================================================
    # ARTBOARD 4: LANDSCAPE PLAYLIST DETAIL SCREEN (DUAL-PANE REORDER)
    # =========================================================================
    s_detail = c.call("frame.create", {
        "x": 3050, "y": 3100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape 4 — Detail Playlist & Reorder",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(s_detail, active_tab_index=2)

    # Split Dual-Pane: Left Pane Playlist Info & Actions (Width 320)
    c.call("shape.rectangle", {"parent": s_detail, "x": 92, "y": 18, "width": 320, "height": 318, "props": {"fill": DARK["surf"], "cornerRadius": 20}})
    
    # Back button top left
    c.call("shape.ellipse", {"parent": s_detail, "x": 106, "y": 30, "width": 34, "height": 34, "props": {"fill": DARK["surf_high"]}})
    add_icon(s_detail, "back", 113, 37, size=18, color=WHITE)
    c.call("text.create", {"parent": s_detail, "x": 150, "y": 38, "text": "Detail Playlist", "props": {"fontSize": 14, "fill": DARK["text_sub"]}})

    # Playlist Big Cover & Title
    c.call("shape.rectangle", {"parent": s_detail, "x": 106, "y": 78, "width": 110, "height": 110, "props": {"fill": DARK["accent"], "cornerRadius": 16}})
    add_icon(s_detail, "music", 136, 108, size=50, color=DARK["secondary"])

    c.call("text.create", {"parent": s_detail, "x": 106, "y": 204, "text": "Japanese Rock Hits", "props": {"fontSize": 17, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_detail, "x": 106, "y": 228, "text": "8 Lagu • 28 Menit • Playlist Kustom", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    # "+ Tambah Lagu" Action 9999px
    c.call("shape.rectangle", {"parent": s_detail, "x": 106, "y": 264, "width": 290, "height": 44, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    add_icon(s_detail, "plus", 180, 276, size=18, color=WHITE)
    c.call("text.create", {"parent": s_detail, "x": 206, "y": 277, "text": "Tambah Lagu", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})

    # Right Pane: Reorderable Track List (Width 488)
    c.call("text.create", {"parent": s_detail, "x": 428, "y": 22, "text": "Daftar Lagu (Geser ⠿ untuk atur urutan)", "props": {"fontSize": 13, "fill": DARK["text_sub"]}})

    track_rows = [
        ("1", "MALU MALU", "dia • 03:26", 428, 48),
        ("2", "Silhouette", "KANA-BOON • 04:00", 428, 116),
        ("3", "Seven", "Jung Kook • 03:04", 428, 184),
        ("4", "Gurenge", "LiSA • 03:56", 428, 252),
    ]
    for pos, t, a, rx, ry in track_rows:
        c.call("shape.rectangle", {"parent": s_detail, "x": rx, "y": ry, "width": 488, "height": 60, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
        c.call("text.create", {"parent": s_detail, "x": rx + 16, "y": ry + 22, "text": pos, "props": {"fontSize": 13, "fill": DARK["text_sub"], "fontWeight": 700}})
        c.call("shape.rectangle", {"parent": s_detail, "x": rx + 40, "y": ry + 8, "width": 44, "height": 44, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
        add_icon(s_detail, "music", rx + 52, ry + 20, size=20, color=DARK["secondary"])
        c.call("text.create", {"parent": s_detail, "x": rx + 96, "y": ry + 12, "text": t, "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})
        c.call("text.create", {"parent": s_detail, "x": rx + 96, "y": ry + 32, "text": a, "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
        # Drag Handle Icon 6-dots
        add_icon(s_detail, "drag", rx + 448, ry + 18, size=24, color=DARK["text_sub"])

    # Floating Player Bottom
    c.call("shape.rectangle", {"parent": s_detail, "x": 92, "y": 352, "width": 824, "height": 54, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s_detail, "x": 100, "y": 359, "width": 40, "height": 40, "props": {"fill": DARK["accent"]}})
    add_icon(s_detail, "music", 110, 369, size=20, color=DARK["secondary"])
    c.call("text.create", {"parent": s_detail, "x": 150, "y": 363, "text": "MALU MALU", "props": {"fontSize": 13, "fill": WHITE}})
    c.call("text.create", {"parent": s_detail, "x": 150, "y": 381, "text": "dia", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.ellipse", {"parent": s_detail, "x": 860, "y": 360, "width": 38, "height": 38, "props": {"fill": DARK["primary"]}})
    add_icon(s_detail, "pause", 870, 370, size=18, color=WHITE)


    # =========================================================================
    # ARTBOARD 5: LANDSCAPE PROFIL & PENGATURAN (2-COLUMN CARDS)
    # =========================================================================
    s_profile = c.call("frame.create", {
        "x": 4050, "y": 3100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape 5 — Profil & Pengaturan (2-Column)",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(s_profile, active_tab_index=3)

    c.call("text.create", {"parent": s_profile, "x": 92, "y": 18, "text": "Profil & Pengaturan", "props": {"fontSize": 22, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})

    # Column 1 (Left, Width 400): User Card & Theme Switcher
    # User Profile Card
    c.call("shape.rectangle", {"parent": s_profile, "x": 92, "y": 52, "width": 400, "height": 70, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("shape.ellipse", {"parent": s_profile, "x": 108, "y": 63, "width": 48, "height": 48, "props": {"fill": DARK["accent"]}})
    add_icon(s_profile, "profile", 120, 75, size=24, color=DARK["secondary"])
    c.call("text.create", {"parent": s_profile, "x": 170, "y": 68, "text": "Only-One-Kind", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_profile, "x": 170, "y": 90, "text": "RustVibe Standalone • Zero Ads", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Theme Card
    c.call("shape.rectangle", {"parent": s_profile, "x": 92, "y": 134, "width": 400, "height": 108, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("text.create", {"parent": s_profile, "x": 110, "y": 148, "text": "Tampilan & Tema", "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_profile, "x": 110, "y": 168, "text": "Pilih skema warna Material 3 Expressive", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # 3 Pills
    c.call("shape.rectangle", {"parent": s_profile, "x": 106, "y": 194, "width": 84, "height": 32, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_profile, "x": 126, "y": 202, "text": "Gelap", "props": {"fontSize": 11, "fill": WHITE, "fontWeight": 700}})

    c.call("shape.rectangle", {"parent": s_profile, "x": 198, "y": 194, "width": 84, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_profile, "x": 218, "y": 202, "text": "Terang", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    c.call("shape.rectangle", {"parent": s_profile, "x": 290, "y": 194, "width": 186, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_profile, "x": 306, "y": 202, "text": "⚡ Aksen Android 15-16", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Audio Engine Card
    c.call("shape.rectangle", {"parent": s_profile, "x": 92, "y": 254, "width": 400, "height": 84, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("text.create", {"parent": s_profile, "x": 110, "y": 268, "text": "Mesin Audio Rust (Symphonia / AAudio)", "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_profile, "x": 110, "y": 290, "text": "M4A Primer • Backup MP3 Otomatis • Zero Lag", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": s_profile, "x": 110, "y": 310, "width": 64, "height": 20, "props": {"fill": DARK["accent"], "cornerRadius": 6}})
    c.call("text.create", {"parent": s_profile, "x": 120, "y": 313, "text": "AKTIF", "props": {"fontSize": 10, "fill": DARK["primary"], "fontWeight": 700}})

    # Column 2 (Right, Width 408): Storage & About
    c.call("shape.rectangle", {"parent": s_profile, "x": 508, "y": 52, "width": 408, "height": 136, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("text.create", {"parent": s_profile, "x": 528, "y": 68, "text": "Penyimpanan & Cache Lagu", "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_profile, "x": 528, "y": 90, "text": "Kapasitas saat ini: 18 MB dari 6 Lagu Tersimpan", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": s_profile, "x": 528, "y": 120, "width": 140, "height": 44, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    add_icon(s_profile, "trash", 544, 132, size=18, color=DARK["text_sub"])
    c.call("text.create", {"parent": s_profile, "x": 570, "y": 134, "text": "Bersihkan", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    # About App Card
    c.call("shape.rectangle", {"parent": s_profile, "x": 508, "y": 200, "width": 408, "height": 138, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("text.create", {"parent": s_profile, "x": 528, "y": 216, "text": "RustVibe v1.0.0", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": s_profile, "x": 528, "y": 238, "text": "Arsitektur Audio Native Rust & Jetpack Compose M3", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("text.create", {"parent": s_profile, "x": 528, "y": 258, "text": "Dirancang khusus viewport 20:9 Redmi Note 10 Pro", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": s_profile, "x": 528, "y": 286, "width": 160, "height": 34, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": s_profile, "x": 544, "y": 296, "text": "Dokumentasi Lengkap", "props": {"fontSize": 11, "fill": DARK["primary"], "fontWeight": 700}})

    # Floating Player Bottom
    c.call("shape.rectangle", {"parent": s_profile, "x": 92, "y": 352, "width": 824, "height": 54, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s_profile, "x": 100, "y": 359, "width": 40, "height": 40, "props": {"fill": DARK["accent"]}})
    add_icon(s_profile, "music", 110, 369, size=20, color=DARK["secondary"])
    c.call("text.create", {"parent": s_profile, "x": 150, "y": 363, "text": "MALU MALU", "props": {"fontSize": 13, "fill": WHITE}})
    c.call("text.create", {"parent": s_profile, "x": 150, "y": 381, "text": "dia", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.ellipse", {"parent": s_profile, "x": 860, "y": 360, "width": 38, "height": 38, "props": {"fill": DARK["primary"]}})
    add_icon(s_profile, "pause", 870, 370, size=18, color=WHITE)

    # Export PNGs
    exports = [
        (s_search, "figcraft_landscape_search_progress.png"),
        (s_playlist_modal, "figcraft_playlist_download_modal.png"),
        (s_hub, "figcraft_landscape_playlist_hub.png"),
        (s_detail, "figcraft_landscape_playlist_detail.png"),
        (s_profile, "figcraft_landscape_profile.png")
    ]
    for sid, fname in exports:
        print(f"Exporting {fname}...")
        c.call("file.export", {
            "ids": [sid],
            "path": f"/home/amir/projects/rust-music-android/{fname}",
            "format": "png",
            "scale": 1
        })

    c.call("file.saveAs", {"path": "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft"})
    c.close()
    print("Complete Landscape & Playlist Suite (5 artboards) generated and exported!")

if __name__ == "__main__":
    build_complete_landscape_suite()
