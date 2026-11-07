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
    "shuffle": "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z",
    "repeat": "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z",
    "download": "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z",
    "check": "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    "back": "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z",
    "sun": "M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1z",
    "moon": "M9.37 5.51A7.35 7.35 0 0 0 9.1 7.5c0 4.08 3.32 7.4 7.4 7.4.68 0 1.35-.09 1.99-.27A9.014 9.014 0 0 1 12 21c-4.97 0-9-4.03-9-9 0-3.93 2.51-7.27 6.37-8.49z",
    "volume_up": "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z",
    "volume_mute": "M7 9v6h4l5 5V4L11 9H7z"
}

def build_horizontal_artboards():
    c = FigCraftBuilder()

    # REDMI NOTE 10 PRO LANDSCAPE VIEWPORT: 936dp width x 420dp height
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
        "accent": "#52170B",
        "lyric_bg": "#382426",
        "lyric_active": "#DC3500"
    }

    LIGHT = {
        "bg": "#F8E6D2",           # Antique White
        "surf": "#EED6BF",         # Surface Container Light
        "surf_high": "#E4C7AC",    # Surface Container High Light
        "surf_highest": "#D8B696", # Surface Container Highest Light
        "primary": "#FF4500",      # Vibrant Crimson Carrot
        "secondary": "#E03E00",
        "text_main": "#2E1E1F",
        "text_sub": "#634740",
        "accent": "#FCD5C5",
        "lyric_bg": "#EED6BF",
        "lyric_active": "#FF4500"
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

    def add_nav_rail(screen_id, palette, active_tab_index=0):
        # Navigation Rail: Left docked column (width 72dp, full height 420dp)
        c.call("shape.rectangle", {
            "parent": screen_id, "x": 0, "y": 0, "width": 72, "height": SCREEN_H,
            "name": "NavigationRail",
            "props": {"fill": palette["surf"], "cornerRadius": [0, 24, 24, 0]}
        })

        # Mini App Badge top
        c.call("shape.ellipse", {"parent": screen_id, "x": 18, "y": 18, "width": 36, "height": 36, "props": {"fill": palette["primary"]}})
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
                # Active pill 9999px (52x34dp)
                c.call("shape.rectangle", {
                    "parent": screen_id, "x": 10, "y": item_y, "width": 52, "height": 34,
                    "name": f"ActiveRailPill_{idx}",
                    "props": {"fill": palette["primary"], "cornerRadius": 9999}
                })
                add_icon(screen_id, icon_key, 24, item_y + 5, size=24, color=WHITE)
                c.call("text.create", {"parent": screen_id, "x": 14, "y": item_y + 38, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": palette["primary"]}})
            else:
                add_icon(screen_id, icon_key, 24, item_y + 5, size=24, color=palette["text_sub"])
                c.call("text.create", {"parent": screen_id, "x": 14, "y": item_y + 38, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": palette["text_sub"]}})

        # Theme switch button at bottom of Rail
        c.call("shape.ellipse", {"parent": screen_id, "x": 18, "y": SCREEN_H - 46, "width": 36, "height": 36, "props": {"fill": palette["surf_high"]}})
        add_icon(screen_id, "moon" if palette == DARK else "sun", 26, SCREEN_H - 38, size=20, color=palette["text_sub"])

    # =========================================================================
    # 1. LANDSCAPE ARTBOARD 1: BERANDA (HOME) - DARK THEME
    # =========================================================================
    h1 = c.call("frame.create", {
        "x": 50, "y": 2100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape Dark 1 — Beranda",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(h1, DARK, active_tab_index=0)

    # Top Header & Filters (X: 92 to 916)
    c.call("text.create", {"parent": h1, "x": 92, "y": 20, "text": "RustVibe", "props": {"fontSize": 24, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_main"]}})
    
    # Filter Chips
    c.call("shape.rectangle", {"parent": h1, "x": 220, "y": 18, "width": 78, "height": 32, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": h1, "x": 236, "y": 25, "text": "Semua", "props": {"fontSize": 12, "fill": WHITE}})

    c.call("shape.rectangle", {"parent": h1, "x": 306, "y": 18, "width": 86, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": h1, "x": 322, "y": 25, "text": "Playlist", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    c.call("shape.rectangle", {"parent": h1, "x": 400, "y": 18, "width": 80, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": h1, "x": 416, "y": 25, "text": "Offline", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    # Search Bar 9999px (Right aligned in top bar)
    c.call("shape.rectangle", {"parent": h1, "x": 500, "y": 16, "width": 416, "height": 36, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    add_icon(h1, "search", 516, 22, size=20, color=DARK["text_sub"])
    c.call("text.create", {"parent": h1, "x": 544, "y": 24, "text": "Cari lagu atau artis...", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    # Two-Column Content Layout
    # Column 1 (Left): Playlists (X: 92, Width 380)
    c.call("text.create", {"parent": h1, "x": 92, "y": 66, "text": "Playlist", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_main"]}})
    
    # Playlist Card 1
    c.call("shape.rectangle", {"parent": h1, "x": 92, "y": 92, "width": 184, "height": 136, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("shape.rectangle", {"parent": h1, "x": 104, "y": 104, "width": 56, "height": 56, "props": {"fill": DARK["accent"], "cornerRadius": 12}})
    add_icon(h1, "music", 118, 118, size=26, color=DARK["secondary"])
    c.call("text.create", {"parent": h1, "x": 104, "y": 170, "text": "Lofi Santai", "props": {"fontSize": 13, "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h1, "x": 104, "y": 190, "text": "4 Lagu • 14 min", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Playlist Card 2
    c.call("shape.rectangle", {"parent": h1, "x": 288, "y": 92, "width": 184, "height": 136, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("shape.rectangle", {"parent": h1, "x": 300, "y": 104, "width": 56, "height": 56, "props": {"fill": DARK["accent"], "cornerRadius": 12}})
    add_icon(h1, "music", 314, 118, size=26, color=DARK["primary"])
    c.call("text.create", {"parent": h1, "x": 300, "y": 170, "text": "Rock Workout", "props": {"fontSize": 13, "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h1, "x": 300, "y": 190, "text": "2 Lagu • 7 min", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Column 2 (Right): Baru Diunduh (X: 492, Width 424)
    c.call("text.create", {"parent": h1, "x": 492, "y": 66, "text": "Baru Diunduh", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_main"]}})
    
    # Track Row 1
    c.call("shape.rectangle", {"parent": h1, "x": 492, "y": 92, "width": 424, "height": 62, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": h1, "x": 502, "y": 100, "width": 46, "height": 46, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
    add_icon(h1, "music", 513, 111, size=22, color=DARK["secondary"])
    c.call("text.create", {"parent": h1, "x": 560, "y": 104, "text": "Silhouette", "props": {"fontSize": 13, "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h1, "x": 560, "y": 124, "text": "KANA-BOON • 04:00", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    # M4A badge
    c.call("shape.rectangle", {"parent": h1, "x": 820, "y": 110, "width": 38, "height": 24, "props": {"fill": DARK["accent"], "cornerRadius": 6}})
    c.call("text.create", {"parent": h1, "x": 826, "y": 114, "text": "M4A", "props": {"fontSize": 10, "fill": DARK["secondary"]}})
    add_icon(h1, "play", 880, 110, size=22, color=DARK["primary"])

    # Track Row 2
    c.call("shape.rectangle", {"parent": h1, "x": 492, "y": 160, "width": 424, "height": 62, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": h1, "x": 502, "y": 168, "width": 46, "height": 46, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
    add_icon(h1, "music", 513, 179, size=22, color=DARK["primary"])
    c.call("text.create", {"parent": h1, "x": 560, "y": 172, "text": "Seven", "props": {"fontSize": 13, "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h1, "x": 560, "y": 192, "text": "Jung Kook • 03:04", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": h1, "x": 820, "y": 178, "width": 38, "height": 24, "props": {"fill": DARK["accent"], "cornerRadius": 6}})
    c.call("text.create", {"parent": h1, "x": 826, "y": 182, "text": "M4A", "props": {"fontSize": 10, "fill": DARK["secondary"]}})
    add_icon(h1, "play", 880, 178, size=22, color=DARK["primary"])

    # Track Row 3
    c.call("shape.rectangle", {"parent": h1, "x": 492, "y": 228, "width": 424, "height": 62, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": h1, "x": 502, "y": 236, "width": 46, "height": 46, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
    add_icon(h1, "music", 513, 247, size=22, color=DARK["secondary"])
    c.call("text.create", {"parent": h1, "x": 560, "y": 240, "text": "Gurenge", "props": {"fontSize": 13, "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h1, "x": 560, "y": 260, "text": "LiSA • 03:56", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": h1, "x": 820, "y": 246, "width": 38, "height": 24, "props": {"fill": DARK["accent"], "cornerRadius": 6}})
    c.call("text.create", {"parent": h1, "x": 826, "y": 250, "text": "M4A", "props": {"fontSize": 10, "fill": DARK["secondary"]}})
    add_icon(h1, "play", 880, 246, size=22, color=DARK["primary"])

    # Bottom Landscape Floating Player Bar (X: 92, Width 824, Height 54)
    fp_y = 352
    c.call("shape.rectangle", {"parent": h1, "x": 92, "y": fp_y, "width": 824, "height": 54, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": h1, "x": 100, "y": fp_y + 7, "width": 40, "height": 40, "props": {"fill": DARK["accent"]}})
    add_icon(h1, "music", 110, fp_y + 17, size=20, color=DARK["secondary"])
    c.call("text.create", {"parent": h1, "x": 150, "y": fp_y + 11, "text": "Silhouette", "props": {"fontSize": 13, "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h1, "x": 150, "y": fp_y + 29, "text": "KANA-BOON", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    
    # Mini Progress Line Center
    c.call("shape.rectangle", {"parent": h1, "x": 280, "y": fp_y + 25, "width": 360, "height": 4, "props": {"fill": DARK["surf_highest"], "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": h1, "x": 280, "y": fp_y + 25, "width": 120, "height": 4, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})

    # Controls right
    c.call("shape.ellipse", {"parent": h1, "x": 670, "y": fp_y + 8, "width": 38, "height": 38, "props": {"fill": DARK["primary"]}})
    add_icon(h1, "pause", 680, fp_y + 18, size=18, color=WHITE)
    add_icon(h1, "next", 724, fp_y + 17, size=20, color=DARK["text_sub"])
    # Volume mini bar right
    add_icon(h1, "volume_up", 764, fp_y + 17, size=20, color=DARK["text_sub"])
    c.call("shape.rectangle", {"parent": h1, "x": 792, "y": fp_y + 24, "width": 100, "height": 6, "props": {"fill": DARK["surf_highest"], "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": h1, "x": 792, "y": fp_y + 24, "width": 65, "height": 6, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})


    # =========================================================================
    # 2. LANDSCAPE ARTBOARD 2: SEDANG DIPUTAR (PLAYER) - TWO-PANE DARK THEME
    # =========================================================================
    h2 = c.call("frame.create", {
        "x": 1050, "y": 2100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape Dark 2 — Sedang Diputar (Dual-Pane)",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']

    # Top Bar: Back Button + Header Title
    c.call("shape.ellipse", {"parent": h2, "x": 24, "y": 18, "width": 36, "height": 36, "props": {"fill": DARK["surf_high"]}})
    add_icon(h2, "back", 30, 24, size=24, color=DARK["text_sub"])
    c.call("text.create", {"parent": h2, "x": 72, "y": 22, "text": "Sedang Diputar", "props": {"fontSize": 18, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_main"]}})

    # PANE 1: Left Pane (X: 24, Width 430, Height 340) - Controls & Artwork
    # Album Art
    c.call("shape.rectangle", {"parent": h2, "x": 24, "y": 68, "width": 140, "height": 140, "props": {"fill": DARK["accent"], "cornerRadius": 18}})
    add_icon(h2, "music", 64, 108, size=60, color=DARK["secondary"])
    
    # Metadata Right of Album Art
    c.call("text.create", {"parent": h2, "x": 180, "y": 80, "text": "Silhouette", "props": {"fontSize": 20, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h2, "x": 180, "y": 108, "text": "KANA-BOON", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_sub"]}})
    # M4A Chip
    c.call("shape.rectangle", {"parent": h2, "x": 180, "y": 136, "width": 46, "height": 26, "props": {"fill": DARK["accent"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": h2, "x": 190, "y": 142, "text": "M4A", "props": {"fontSize": 10, "fill": DARK["secondary"]}})

    # Seek Bar (X: 24, Width 430)
    c.call("shape.rectangle", {"parent": h2, "x": 24, "y": 224, "width": 430, "height": 6, "props": {"fill": DARK["surf_highest"], "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": h2, "x": 24, "y": 224, "width": 90, "height": 6, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": h2, "x": 110, "y": 221, "width": 12, "height": 12, "props": {"fill": DARK["primary"]}})
    c.call("text.create", {"parent": h2, "x": 24, "y": 236, "text": "00:50", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("text.create", {"parent": h2, "x": 420, "y": 236, "text": "04:00", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Playback Controls Row (Y: 260)
    add_icon(h2, "shuffle", 40, 274, size=24, color=DARK["text_sub"])
    add_icon(h2, "prev", 120, 270, size=30, color=DARK["text_main"])
    # Main Play/Pause circle 54dp
    c.call("shape.ellipse", {"parent": h2, "x": 210, "y": 258, "width": 56, "height": 56, "props": {"fill": DARK["primary"]}})
    add_icon(h2, "pause", 226, 274, size=24, color=WHITE)
    add_icon(h2, "next", 310, 270, size=30, color=DARK["text_main"])
    add_icon(h2, "repeat", 390, 274, size=24, color=DARK["text_sub"])

    # Hardware Volume Bar at bottom of Left Pane (Y: 334, Height 50, 9999px pill)
    c.call("shape.rectangle", {"parent": h2, "x": 24, "y": 344, "width": 430, "height": 48, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    add_icon(h2, "volume_mute", 44, 356, size=22, color=DARK["text_sub"])
    # Volume Slider Track
    c.call("shape.rectangle", {"parent": h2, "x": 80, "y": 364, "width": 314, "height": 8, "props": {"fill": DARK["surf_highest"], "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": h2, "x": 80, "y": 364, "width": 210, "height": 8, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": h2, "x": 284, "y": 360, "width": 16, "height": 16, "props": {"fill": DARK["primary"]}})
    add_icon(h2, "volume_up", 412, 356, size=22, color=DARK["text_sub"])

    # PANE 2: Right Pane (X: 476, Width 436, Height 374) - Synced Karaoke Lyrics
    c.call("shape.rectangle", {"parent": h2, "x": 476, "y": 24, "width": 436, "height": 368, "props": {"fill": DARK["surf"], "cornerRadius": 24}})
    c.call("text.create", {"parent": h2, "x": 504, "y": 48, "text": "LIRIK SINKRON", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": DARK["secondary"]}})

    # Lyrics lines (Smooth vertical scrolling list)
    lyrics = [
        ("... (Intro Melodi Gitar)", False, 84),
        ("いっせーのーせで踏み込むゴーライン", True, 126),
        ("isse no se de fumikomu gourain", True, 154),
        ("僕らは何も何もまだ知らぬ", False, 196),
        ("一線越えて振り返るともうない", False, 236),
        ("僕らは何も何もまだ知らぬ", False, 276),
        ("うだってうだってうだっても", False, 316),
    ]
    for text, is_active, y_pos in lyrics:
        if is_active:
            # Active Karaoke Line Highlight Pill
            c.call("shape.rectangle", {"parent": h2, "x": 496, "y": y_pos - 6, "width": 396, "height": 32, "props": {"fill": DARK["accent"], "cornerRadius": 9999}})
            c.call("text.create", {"parent": h2, "x": 512, "y": y_pos, "text": text, "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": DARK["primary"]}})
        else:
            c.call("text.create", {"parent": h2, "x": 512, "y": y_pos, "text": text, "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": DARK["text_sub"]}})


    # =========================================================================
    # 3. LANDSCAPE ARTBOARD 3: CARI (SEARCH) - DARK THEME
    # =========================================================================
    h3 = c.call("frame.create", {
        "x": 2050, "y": 2100, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape Dark 3 — Cari (Grid)",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(h3, DARK, active_tab_index=1)

    # Wide Search Input (X: 92, Width 824)
    c.call("shape.rectangle", {"parent": h3, "x": 92, "y": 18, "width": 824, "height": 48, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    add_icon(h3, "search", 108, 30, size=24, color=DARK["text_sub"])
    c.call("text.create", {"parent": h3, "x": 144, "y": 32, "text": "Ketik judul lagu, artis, atau tempel link YouTube...", "props": {"fontSize": 14, "fill": DARK["text_sub"]}})

    # Search Results 2-Column Grid
    c.call("text.create", {"parent": h3, "x": 92, "y": 80, "text": "Hasil Pencarian", "props": {"fontSize": 15, "fill": DARK["text_main"]}})
    
    results = [
        ("Idol", "YOASOBI • 03:33", 92, 108),
        ("Gurenge", "LiSA • 03:56", 516, 108),
        ("Seven", "Jung Kook • 03:04", 92, 180),
        ("Silhouette", "KANA-BOON • 04:00", 516, 180),
        ("Blue Bird", "Ikimonogakari • 03:36", 92, 252),
        ("Magnetic", "ILLIT • 02:40", 516, 252),
    ]
    for title, sub, rx, ry in results:
        c.call("shape.rectangle", {"parent": h3, "x": rx, "y": ry, "width": 400, "height": 62, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
        c.call("shape.rectangle", {"parent": h3, "x": rx + 10, "y": ry + 8, "width": 46, "height": 46, "props": {"fill": DARK["accent"], "cornerRadius": 10}})
        add_icon(h3, "music", rx + 21, ry + 19, size=22, color=DARK["secondary"])
        c.call("text.create", {"parent": h3, "x": rx + 66, "y": ry + 12, "text": title, "props": {"fontSize": 14, "fill": DARK["text_main"]}})
        c.call("text.create", {"parent": h3, "x": rx + 66, "y": ry + 32, "text": sub, "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
        # Download Pill Button
        c.call("shape.ellipse", {"parent": h3, "x": rx + 350, "y": ry + 12, "width": 38, "height": 38, "props": {"fill": DARK["surf_high"]}})
        add_icon(h3, "download", rx + 360, ry + 22, size=18, color=DARK["primary"])

    # Bottom Landscape Floating Player
    c.call("shape.rectangle", {"parent": h3, "x": 92, "y": fp_y, "width": 824, "height": 54, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": h3, "x": 100, "y": fp_y + 7, "width": 40, "height": 40, "props": {"fill": DARK["accent"]}})
    add_icon(h3, "music", 110, fp_y + 17, size=20, color=DARK["secondary"])
    c.call("text.create", {"parent": h3, "x": 150, "y": fp_y + 11, "text": "Silhouette", "props": {"fontSize": 13, "fill": DARK["text_main"]}})
    c.call("text.create", {"parent": h3, "x": 150, "y": fp_y + 29, "text": "KANA-BOON", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.ellipse", {"parent": h3, "x": 860, "y": fp_y + 8, "width": 38, "height": 38, "props": {"fill": DARK["primary"]}})
    add_icon(h3, "pause", 870, fp_y + 18, size=18, color=WHITE)


    # =========================================================================
    # 4. LANDSCAPE ARTBOARD 4: BERANDA (HOME) - LIGHT THEME
    # =========================================================================
    h4 = c.call("frame.create", {
        "x": 50, "y": 2600, "width": SCREEN_W, "height": SCREEN_H,
        "name": "Landscape Light 1 — Beranda",
        "props": {"fill": LIGHT["bg"], "cornerRadius": 28}
    })['result']['id']
    add_nav_rail(h4, LIGHT, active_tab_index=0)

    # Top Header & Filters
    c.call("text.create", {"parent": h4, "x": 92, "y": 20, "text": "RustVibe", "props": {"fontSize": 24, "fontFamily": "Plus Jakarta Sans", "fill": LIGHT["text_main"]}})
    c.call("shape.rectangle", {"parent": h4, "x": 220, "y": 18, "width": 78, "height": 32, "props": {"fill": LIGHT["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": h4, "x": 236, "y": 25, "text": "Semua", "props": {"fontSize": 12, "fill": WHITE}})
    c.call("shape.rectangle", {"parent": h4, "x": 306, "y": 18, "width": 86, "height": 32, "props": {"fill": LIGHT["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": h4, "x": 322, "y": 25, "text": "Playlist", "props": {"fontSize": 12, "fill": LIGHT["text_sub"]}})
    c.call("shape.rectangle", {"parent": h4, "x": 400, "y": 18, "width": 80, "height": 32, "props": {"fill": LIGHT["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": h4, "x": 416, "y": 25, "text": "Offline", "props": {"fontSize": 12, "fill": LIGHT["text_sub"]}})

    # Search Bar
    c.call("shape.rectangle", {"parent": h4, "x": 500, "y": 16, "width": 416, "height": 36, "props": {"fill": LIGHT["surf"], "cornerRadius": 9999}})
    add_icon(h4, "search", 516, 22, size=20, color=LIGHT["text_sub"])
    c.call("text.create", {"parent": h4, "x": 544, "y": 24, "text": "Cari lagu atau artis...", "props": {"fontSize": 12, "fill": LIGHT["text_sub"]}})

    # Playlists
    c.call("text.create", {"parent": h4, "x": 92, "y": 66, "text": "Playlist", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": LIGHT["text_main"]}})
    c.call("shape.rectangle", {"parent": h4, "x": 92, "y": 92, "width": 184, "height": 136, "props": {"fill": LIGHT["surf"], "cornerRadius": 18}})
    c.call("shape.rectangle", {"parent": h4, "x": 104, "y": 104, "width": 56, "height": 56, "props": {"fill": LIGHT["accent"], "cornerRadius": 12}})
    add_icon(h4, "music", 118, 118, size=26, color=LIGHT["primary"])
    c.call("text.create", {"parent": h4, "x": 104, "y": 170, "text": "Lofi Santai", "props": {"fontSize": 13, "fill": LIGHT["text_main"]}})
    c.call("text.create", {"parent": h4, "x": 104, "y": 190, "text": "4 Lagu • 14 min", "props": {"fontSize": 11, "fill": LIGHT["text_sub"]}})

    c.call("shape.rectangle", {"parent": h4, "x": 288, "y": 92, "width": 184, "height": 136, "props": {"fill": LIGHT["surf"], "cornerRadius": 18}})
    c.call("shape.rectangle", {"parent": h4, "x": 300, "y": 104, "width": 56, "height": 56, "props": {"fill": LIGHT["accent"], "cornerRadius": 12}})
    add_icon(h4, "music", 314, 118, size=26, color=LIGHT["secondary"])
    c.call("text.create", {"parent": h4, "x": 300, "y": 170, "text": "Rock Workout", "props": {"fontSize": 13, "fill": LIGHT["text_main"]}})
    c.call("text.create", {"parent": h4, "x": 300, "y": 190, "text": "2 Lagu • 7 min", "props": {"fontSize": 11, "fill": LIGHT["text_sub"]}})

    # Baru Diunduh Light
    c.call("text.create", {"parent": h4, "x": 492, "y": 66, "text": "Baru Diunduh", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": LIGHT["text_main"]}})
    for idx, (t, a) in enumerate([("Silhouette", "KANA-BOON • 04:00"), ("Seven", "Jung Kook • 03:04"), ("Gurenge", "LiSA • 03:56")]):
        row_y = 92 + (idx * 68)
        c.call("shape.rectangle", {"parent": h4, "x": 492, "y": row_y, "width": 424, "height": 62, "props": {"fill": LIGHT["surf"], "cornerRadius": 16}})
        c.call("shape.rectangle", {"parent": h4, "x": 502, "y": row_y + 8, "width": 46, "height": 46, "props": {"fill": LIGHT["accent"], "cornerRadius": 10}})
        add_icon(h4, "music", 513, row_y + 19, size=22, color=LIGHT["primary"])
        c.call("text.create", {"parent": h4, "x": 560, "y": row_y + 12, "text": t, "props": {"fontSize": 13, "fill": LIGHT["text_main"]}})
        c.call("text.create", {"parent": h4, "x": 560, "y": row_y + 32, "text": a, "props": {"fontSize": 11, "fill": LIGHT["text_sub"]}})
        c.call("shape.rectangle", {"parent": h4, "x": 820, "y": row_y + 18, "width": 38, "height": 24, "props": {"fill": LIGHT["accent"], "cornerRadius": 6}})
        c.call("text.create", {"parent": h4, "x": 826, "y": row_y + 22, "text": "M4A", "props": {"fontSize": 10, "fill": LIGHT["primary"]}})
        add_icon(h4, "play", 880, row_y + 18, size=22, color=LIGHT["primary"])

    # Bottom Landscape Floating Player Light
    c.call("shape.rectangle", {"parent": h4, "x": 92, "y": fp_y, "width": 824, "height": 54, "props": {"fill": LIGHT["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": h4, "x": 100, "y": fp_y + 7, "width": 40, "height": 40, "props": {"fill": LIGHT["accent"]}})
    add_icon(h4, "music", 110, fp_y + 17, size=20, color=LIGHT["primary"])
    c.call("text.create", {"parent": h4, "x": 150, "y": fp_y + 11, "text": "Silhouette", "props": {"fontSize": 13, "fill": LIGHT["text_main"]}})
    c.call("text.create", {"parent": h4, "x": 150, "y": fp_y + 29, "text": "KANA-BOON", "props": {"fontSize": 11, "fill": LIGHT["text_sub"]}})
    c.call("shape.ellipse", {"parent": h4, "x": 860, "y": fp_y + 8, "width": 38, "height": 38, "props": {"fill": LIGHT["primary"]}})
    add_icon(h4, "pause", 870, fp_y + 18, size=18, color=WHITE)

    # Export PNGs for visual inspection
    exports = [
        (h1, "landscape_dark_home.png"),
        (h2, "landscape_dark_player.png"),
        (h3, "landscape_dark_search.png"),
        (h4, "landscape_light_home.png")
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
    print("All 4 Landscape Artboards generated and saved to FigCraft!")

if __name__ == "__main__":
    build_horizontal_artboards()
