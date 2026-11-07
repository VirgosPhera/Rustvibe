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

# SVG VECTOR PATH CATALOG (STANDARD CLEAN MATERIAL VECTOR ICONS)
ICONS = {
    "home": "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z",
    "search": "M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
    "playlist": "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z",
    "profile": "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z",
    "settings": "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z",
    "music": "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z",
    "play": "M8 5v14l11-7z",
    "pause": "M6 19h4V5H6v14zm8-14v14h4V5h-4z",
    "next": "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z",
    "prev": "M6 6h2v12H6zm3.5 6l8.5 6V6z",
    "shuffle": "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5l-2.04 2.04-3.13-3.13z",
    "repeat": "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z",
    "download": "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z",
    "check": "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z",
    "close": "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z",
    "drag": "M11 18c0 1.1-.9 2-2 2s-2-.9-2-2 .9-2 2-2 2 .9 2 2zm-2-8c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0-6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm6 4c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z",
    "plus": "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",
    "bolt": "M11 21h-1l1-7H7.5c-.88 0-.33-.75-.31-.78C8.48 10.94 10.42 7.54 13.01 3h1l-1 7h3.5c.49 0 .56.33.47.51l-5.98 10.49z",
    "guitar": "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z",
    "fire": "M12 23c-4.97 0-9-4.03-9-9 0-3.66 2.4-7.05 6-8.24V8c0 1.66 1.34 3 3 3s3-1.34 3-3V5.76c3.6 1.19 6 4.58 6 8.24 0 4.97-4.03 9-9 9z",
    "drum": "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z",
    "moon": "M12.3 2a10 10 0 0 0-1.9 19.8 10 10 0 0 0 11.6-11.6A10 10 0 0 0 12.3 2z",
    "headphone": "M12 3a9 9 0 0 0-9 9v7c0 1.1.9 2 2 2h4v-8H5v-1a7 7 0 0 1 14 0v1h-4v8h4c1.1 0 2-.9 2-2v-7a9 9 0 0 0-9-9z",
    "piano": "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 11h2v4h-2v-4zm-4 0h2v4h-2v-4zm-4 0h2v4H6v-4z",
    "camera": "M9 2L7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z",
    "volume": "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z",
    "back": "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z",
    "more": "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z",
    "trash": "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
}

def build_all_screens():
    c = FigCraftBuilder()
    
    # 1. Reset document
    c.call("file.new")
    time.sleep(0.2)

    # PALETTE CONSTANTS (STRICT ZERO GRADIENT, ZERO SHADOW, ZERO BORDER)
    BG = "#2E1E1F"          # Coffee Bean
    SURF = "#382426"        # Surface Container
    SURF_HIGH = "#452E30"   # Surface Container High
    SURF_HIGHEST = "#523639"# Surface Container Highest
    PRIMARY = "#E30B5C"     # Raspberry Red
    SECONDARY = "#F8395A"   # Hot Fuchsia
    PASTEL = "#EEB3B5"      # Cotton Rose
    WHITE = "#FFFFFF"
    DARK_ACCENT = "#5C1329" # Primary Container
    ACTIVE_LYRIC_BG = "#6B192A"
    ACTIVE_LYRIC_TXT = "#FFD9DF"

    def add_icon(parent_id, icon_name, x, y, size=24, color=WHITE):
        path = ICONS.get(icon_name, ICONS["music"])
        return c.call("vector.create", {
            "parent": parent_id,
            "x": x, "y": y,
            "width": size, "height": size,
            "path": path,
            "props": {"fill": color}
        })

    def add_bottom_nav(screen_id, active_tab_index=0):
        # Bottom Navigation Bar (Docked at bottom, NO BORDER, cornerRadius top 24)
        c.call("shape.rectangle", {
            "parent": screen_id, "x": 0, "y": 728, "width": 360, "height": 72,
            "name": "BottomNavigationBar",
            "props": {"fill": SURF, "cornerRadius": [24, 24, 0, 0]}
        })
        
        tabs = [
            ("home", "Beranda", 24),
            ("search", "Cari", 114),
            ("playlist", "Playlist", 204),
            ("profile", "Profil", 294)
        ]
        
        for idx, (icon_key, label, x_pos) in enumerate(tabs):
            if idx == active_tab_index:
                # Active Tab: 9999px Capsule Pill in Raspberry Red (NO BORDER)
                c.call("shape.rectangle", {
                    "parent": screen_id, "x": x_pos - 8, "y": 738, "width": 64, "height": 34,
                    "name": f"ActiveTabPill_{idx}",
                    "props": {"fill": PRIMARY, "cornerRadius": 9999}
                })
                add_icon(screen_id, icon_key, x_pos + 12, y=744, size=20, color=WHITE)
                c.call("text.create", {"parent": screen_id, "x": x_pos + 3, "y": 776, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": SECONDARY}})
            else:
                # Inactive Tab
                add_icon(screen_id, icon_key, x_pos + 12, y=744, size=20, color=PASTEL)
                c.call("text.create", {"parent": screen_id, "x": x_pos + 6, "y": 776, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    def add_floating_player(screen_id, track_title="Starfall", artist="Illenium"):
        c.call("shape.rectangle", {"parent": screen_id, "x": 16, "y": 658, "width": 328, "height": 58, "name": "FloatingPlayerBar", "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
        c.call("shape.ellipse", {"parent": screen_id, "x": 24, "y": 666, "width": 42, "height": 42, "props": {"fill": DARK_ACCENT}})
        add_icon(screen_id, "music", 35, 677, size=20, color=SECONDARY)
        c.call("text.create", {"parent": screen_id, "x": 76, "y": 670, "text": track_title, "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
        c.call("text.create", {"parent": screen_id, "x": 76, "y": 690, "text": artist, "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
        c.call("shape.ellipse", {"parent": screen_id, "x": 238, "y": 666, "width": 42, "height": 42, "props": {"fill": PRIMARY}})
        add_icon(screen_id, "play", 251, 678, size=18, color=WHITE)
        add_icon(screen_id, "next", 296, 678, size=20, color=PASTEL)


    # =========================================================================
    # SCREEN 1: Home Screen (Tab "Semua") [x: 50, y: 50]
    # =========================================================================
    s1 = c.call("frame.create", {
        "x": 50, "y": 50, "width": 360, "height": 800,
        "name": "Screen 1 — Home (Semua)",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Header
    c.call("text.create", {"parent": s1, "x": 20, "y": 30, "text": "RustVibe", "props": {"fontSize": 26, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("shape.ellipse", {"parent": s1, "x": 304, "y": 28, "width": 38, "height": 38, "props": {"fill": SURF_HIGH}})
    add_icon(s1, "profile", 313, 37, size=20, color=PASTEL)

    # Search Bar 9999px
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 76, "width": 328, "height": 48, "props": {"fill": SURF, "cornerRadius": 9999}})
    add_icon(s1, "search", 32, 90, size=20, color=PASTEL)
    c.call("text.create", {"parent": s1, "x": 60, "y": 91, "text": "Cari lagu atau artis...", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Filter Chips: Semua (Active) | Playlist | Offline
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 134, "width": 75, "height": 32, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 32, "y": 142, "text": "Semua", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    c.call("shape.rectangle", {"parent": s1, "x": 98, "y": 134, "width": 85, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 114, "y": 142, "text": "Playlist", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    c.call("shape.rectangle", {"parent": s1, "x": 190, "y": 134, "width": 80, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 206, "y": 142, "text": "Offline", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Playlist Section
    c.call("text.create", {"parent": s1, "x": 18, "y": 182, "text": "Playlist", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Card 1: Rock Workout (2x2 Grid Mosaic Cover with SVG vectors)
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 208, "width": 156, "height": 140, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s1, "x": 28, "y": 220, "width": 56, "height": 56, "props": {"fill": BG, "cornerRadius": 12}})
    # 4 grid cells with real SVG icons
    c.call("shape.rectangle", {"parent": s1, "x": 30, "y": 222, "width": 25, "height": 25, "props": {"fill": DARK_ACCENT, "cornerRadius": 4}})
    add_icon(s1, "bolt", 35, 227, size=15, color=SECONDARY)
    c.call("shape.rectangle", {"parent": s1, "x": 57, "y": 222, "width": 25, "height": 25, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 4}})
    add_icon(s1, "guitar", 62, 227, size=15, color=PASTEL)
    c.call("shape.rectangle", {"parent": s1, "x": 30, "y": 249, "width": 25, "height": 25, "props": {"fill": SURF_HIGH, "cornerRadius": 4}})
    add_icon(s1, "fire", 35, 254, size=15, color=PASTEL)
    c.call("shape.rectangle", {"parent": s1, "x": 57, "y": 249, "width": 25, "height": 25, "props": {"fill": PRIMARY, "cornerRadius": 4}})
    add_icon(s1, "drum", 62, 254, size=15, color=WHITE)

    c.call("text.create", {"parent": s1, "x": 28, "y": 288, "text": "Rock Workout", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 28, "y": 308, "text": "3 Lagu • 11 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Card 2: Night Lo-Fi Drive (Custom Cover with Moon SVG)
    c.call("shape.rectangle", {"parent": s1, "x": 188, "y": 208, "width": 156, "height": 140, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s1, "x": 200, "y": 220, "width": 56, "height": 56, "props": {"fill": DARK_ACCENT, "cornerRadius": 12}})
    add_icon(s1, "moon", 216, 234, size=24, color=SECONDARY)
    c.call("text.create", {"parent": s1, "x": 200, "y": 288, "text": "Night Lo-Fi Drive", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 200, "y": 308, "text": "8 Lagu • 24 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Section 2: Baru Diunduh
    c.call("text.create", {"parent": s1, "x": 18, "y": 366, "text": "Baru Diunduh", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Track 1
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 394, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s1, "x": 26, "y": 402, "width": 42, "height": 42, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    add_icon(s1, "music", 37, 413, size=20, color=SECONDARY)
    c.call("text.create", {"parent": s1, "x": 78, "y": 407, "text": "Starfall", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 427, "text": "Illenium • 03:42", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s1, "x": 272, "y": 410, "width": 60, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 286, "y": 416, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Track 2
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 460, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s1, "x": 26, "y": 468, "width": 42, "height": 42, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 10}})
    add_icon(s1, "headphone", 37, 479, size=20, color=PASTEL)
    c.call("text.create", {"parent": s1, "x": 78, "y": 473, "text": "After Dark", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 493, "text": "Mr.Kitty • 04:18", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s1, "x": 272, "y": 476, "width": 60, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 286, "y": 482, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Track 3
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 526, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s1, "x": 26, "y": 534, "width": 42, "height": 42, "props": {"fill": SURF_HIGH, "cornerRadius": 10}})
    add_icon(s1, "piano", 37, 545, size=20, color=PASTEL)
    c.call("text.create", {"parent": s1, "x": 78, "y": 539, "text": "Midnight City", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 559, "text": "M83 • 04:03", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s1, "x": 272, "y": 542, "width": 60, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 286, "y": 548, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Floating Player & Bottom Nav
    add_floating_player(s1)
    add_bottom_nav(s1, active_tab_index=0)


    # =========================================================================
    # SCREEN 2: Search & Post-Download Triage [x: 450, y: 50]
    # =========================================================================
    s2 = c.call("frame.create", {
        "x": 450, "y": 50, "width": 360, "height": 800,
        "name": "Screen 2 — Search & Triage",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Search Bar Active
    c.call("shape.rectangle", {"parent": s2, "x": 16, "y": 28, "width": 328, "height": 48, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    add_icon(s2, "search", 32, 42, size=20, color=SECONDARY)
    c.call("text.create", {"parent": s2, "x": 60, "y": 43, "text": "Racing into the Night", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    add_icon(s2, "close", 310, 42, size=20, color=PASTEL)

    # Filter Chips
    c.call("shape.rectangle", {"parent": s2, "x": 16, "y": 86, "width": 80, "height": 30, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s2, "x": 28, "y": 94, "text": "Semua", "props": {"fontSize": 11, "fill": WHITE}})
    c.call("shape.rectangle", {"parent": s2, "x": 104, "y": 86, "width": 70, "height": 30, "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s2, "x": 120, "y": 94, "text": "M4A", "props": {"fontSize": 11, "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s2, "x": 182, "y": 86, "width": 75, "height": 30, "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s2, "x": 198, "y": 94, "text": "Lirik", "props": {"fontSize": 11, "fill": PASTEL}})

    # Result Item Card
    c.call("shape.rectangle", {"parent": s2, "x": 16, "y": 128, "width": 328, "height": 72, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s2, "x": 28, "y": 136, "width": 56, "height": 56, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    c.call("text.create", {"parent": s2, "x": 44, "y": 154, "text": "夜", "props": {"fontSize": 20, "fill": WHITE}})
    c.call("text.create", {"parent": s2, "x": 94, "y": 144, "text": "Racing into the Night", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s2, "x": 94, "y": 166, "text": "YOASOBI • 04:21", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Dark overlay
    c.call("shape.rectangle", {"parent": s2, "x": 0, "y": 210, "width": 360, "height": 590, "props": {"fill": "#1A1012", "opacity": 0.55}})

    # Bottom Sheet Triage (32dp corner, no borders)
    c.call("shape.rectangle", {"parent": s2, "x": 0, "y": 320, "width": 360, "height": 410, "props": {"fill": SURF, "cornerRadius": [32, 32, 0, 0]}})
    c.call("shape.rectangle", {"parent": s2, "x": 160, "y": 332, "width": 40, "height": 4, "props": {"fill": SURF_HIGHEST, "cornerRadius": 9999}})

    # Check circle
    c.call("shape.ellipse", {"parent": s2, "x": 154, "y": 352, "width": 52, "height": 52, "props": {"fill": DARK_ACCENT}})
    add_icon(s2, "check", 168, 366, size=24, color=SECONDARY)

    c.call("text.create", {"parent": s2, "x": 75, "y": 420, "text": "Lagu Berhasil Diunduh", "props": {"fontSize": 18, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s2, "x": 78, "y": 448, "text": "Pilih tindakan penyimpanan:", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Option A (Save to playlist)
    c.call("shape.rectangle", {"parent": s2, "x": 20, "y": 484, "width": 320, "height": 60, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    add_icon(s2, "playlist", 82, 502, size=22, color=WHITE)
    c.call("text.create", {"parent": s2, "x": 114, "y": 504, "text": "Simpan ke Playlist", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Option B (One-time ephemeral)
    c.call("shape.rectangle", {"parent": s2, "x": 20, "y": 556, "width": 320, "height": 60, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    add_icon(s2, "bolt", 64, 574, size=22, color=SECONDARY)
    c.call("text.create", {"parent": s2, "x": 96, "y": 576, "text": "Dengerin Sekali (Lalu Hapus)", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    c.call("text.create", {"parent": s2, "x": 162, "y": 640, "text": "Batal", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    add_bottom_nav(s2, active_tab_index=1)


    # =========================================================================
    # SCREEN 3: Full Player & Karaoke Synced Lyrics [x: 850, y: 50]
    # =========================================================================
    s3 = c.call("frame.create", {
        "x": 850, "y": 50, "width": 360, "height": 800,
        "name": "Screen 3 — Full Player & Lirik",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Top Bar
    add_icon(s3, "back", 20, 28, size=22, color=WHITE)
    c.call("text.create", {"parent": s3, "x": 130, "y": 32, "text": "Rock Workout", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s3, "more", 318, 28, size=22, color=WHITE)

    # Big Album Art
    c.call("shape.rectangle", {"parent": s3, "x": 90, "y": 68, "width": 180, "height": 180, "props": {"fill": SURF, "cornerRadius": 24}})
    c.call("text.create", {"parent": s3, "x": 155, "y": 140, "text": "夜", "props": {"fontSize": 52, "fill": WHITE}})

    # Title & Artist
    c.call("text.create", {"parent": s3, "x": 20, "y": 266, "text": "Racing into the Night", "props": {"fontSize": 18, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s3, "x": 20, "y": 292, "text": "YOASOBI", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    c.call("shape.rectangle", {"parent": s3, "x": 286, "y": 272, "width": 54, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s3, "x": 298, "y": 278, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Scrubber
    c.call("shape.rectangle", {"parent": s3, "x": 20, "y": 324, "width": 320, "height": 6, "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": s3, "x": 20, "y": 324, "width": 130, "height": 6, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s3, "x": 144, "y": 319, "width": 16, "height": 16, "props": {"fill": PRIMARY}})
    c.call("text.create", {"parent": s3, "x": 20, "y": 338, "text": "01:42", "props": {"fontSize": 11, "fill": PASTEL}})
    c.call("text.create", {"parent": s3, "x": 310, "y": 338, "text": "04:21", "props": {"fontSize": 11, "fill": PASTEL}})

    # Lyrics Stage
    c.call("shape.rectangle", {"parent": s3, "x": 16, "y": 366, "width": 328, "height": 270, "props": {"fill": SURF, "cornerRadius": 24}})
    c.call("text.create", {"parent": s3, "x": 32, "y": 384, "text": "Lirik", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    c.call("text.create", {"parent": s3, "x": 32, "y": 418, "text": "沈むように溶けてゆくように", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.5}})

    # Active Lyric Line
    c.call("shape.rectangle", {"parent": s3, "x": 24, "y": 450, "width": 312, "height": 52, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 16}})
    c.call("text.create", {"parent": s3, "x": 36, "y": 466, "text": "二人だけの空が広がる夜に", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": ACTIVE_LYRIC_TXT}})
    add_icon(s3, "music", 298, 464, size=18, color=SECONDARY)

    c.call("text.create", {"parent": s3, "x": 32, "y": 522, "text": "「さよなら」だけだった", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.7}})
    c.call("text.create", {"parent": s3, "x": 32, "y": 558, "text": "その一言で全てが分かった", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.5}})
    c.call("text.create", {"parent": s3, "x": 32, "y": 594, "text": "日が沈み出した空と君の姿", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.3}})

    # Controls with real SVG icons
    add_icon(s3, "shuffle", 34, 672, size=24, color=PASTEL)
    add_icon(s3, "prev", 94, 670, size=28, color=PASTEL)
    c.call("shape.ellipse", {"parent": s3, "x": 148, "y": 654, "width": 64, "height": 64, "props": {"fill": PRIMARY}})
    add_icon(s3, "pause", 168, 672, size=26, color=WHITE)
    add_icon(s3, "next", 238, 670, size=28, color=PASTEL)
    add_icon(s3, "repeat", 302, 672, size=24, color=PASTEL)

    # Volume Bar
    c.call("shape.rectangle", {"parent": s3, "x": 60, "y": 742, "width": 240, "height": 34, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    add_icon(s3, "volume", 74, 749, size=20, color=PASTEL)
    c.call("shape.rectangle", {"parent": s3, "x": 106, "y": 756, "width": 140, "height": 6, "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": s3, "x": 106, "y": 756, "width": 90, "height": 6, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    add_icon(s3, "volume", 262, 749, size=20, color=PASTEL)


    # =========================================================================
    # SCREEN 4: Playlist Detail & Drag Reorder [x: 1250, y: 50]
    # =========================================================================
    s4 = c.call("frame.create", {
        "x": 1250, "y": 50, "width": 360, "height": 800,
        "name": "Screen 4 — Detail Playlist",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Header Bar
    add_icon(s4, "back", 20, 28, size=22, color=WHITE)
    c.call("text.create", {"parent": s4, "x": 130, "y": 32, "text": "Playlist", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    add_icon(s4, "more", 318, 28, size=22, color=WHITE)

    # Playlist Hero Header
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 66, "width": 328, "height": 100, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s4, "x": 28, "y": 76, "width": 56, "height": 56, "props": {"fill": BG, "cornerRadius": 12}})
    c.call("shape.rectangle", {"parent": s4, "x": 30, "y": 78, "width": 25, "height": 25, "props": {"fill": DARK_ACCENT, "cornerRadius": 4}})
    add_icon(s4, "bolt", 35, 83, size=15, color=SECONDARY)
    c.call("shape.rectangle", {"parent": s4, "x": 57, "y": 78, "width": 25, "height": 25, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 4}})
    add_icon(s4, "guitar", 62, 83, size=15, color=PASTEL)
    c.call("shape.rectangle", {"parent": s4, "x": 30, "y": 105, "width": 25, "height": 25, "props": {"fill": SURF_HIGH, "cornerRadius": 4}})
    add_icon(s4, "fire", 35, 110, size=15, color=PASTEL)
    c.call("shape.rectangle", {"parent": s4, "x": 57, "y": 105, "width": 25, "height": 25, "props": {"fill": PRIMARY, "cornerRadius": 4}})
    add_icon(s4, "drum", 62, 110, size=15, color=WHITE)

    c.call("text.create", {"parent": s4, "x": 100, "y": 80, "text": "Rock Workout", "props": {"fontSize": 17, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 100, "y": 102, "text": "3 Lagu • 11 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Ubah Sampul Button
    c.call("shape.rectangle", {"parent": s4, "x": 100, "y": 124, "width": 116, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    add_icon(s4, "camera", 108, 129, size=15, color=PASTEL)
    c.call("text.create", {"parent": s4, "x": 128, "y": 130, "text": "Ubah Sampul", "props": {"fontSize": 10, "fill": PASTEL}})

    # Instruction banner
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 178, "width": 328, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    add_icon(s4, "drag", 84, 184, size=18, color=PASTEL)
    c.call("text.create", {"parent": s4, "x": 108, "y": 188, "text": "Geser ikon untuk atur urutan", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Item 1
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 222, "width": 328, "height": 60, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s4, "x": 26, "y": 233, "width": 28, "height": 38, "props": {"fill": SURF_HIGH, "cornerRadius": 8}})
    c.call("text.create", {"parent": s4, "x": 33, "y": 245, "text": "1", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s4, "x": 62, "y": 230, "width": 44, "height": 44, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    add_icon(s4, "bolt", 74, 242, size=20, color=SECONDARY)
    c.call("text.create", {"parent": s4, "x": 116, "y": 233, "text": "Believer", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 253, "text": "Imagine Dragons • 03:24", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s4, "drag", 308, 242, size=20, color=PASTEL)

    # Item 2 (Active Dragging State)
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 292, "width": 328, "height": 62, "props": {"fill": SURF_HIGHEST, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s4, "x": 26, "y": 303, "width": 28, "height": 38, "props": {"fill": DARK_ACCENT, "cornerRadius": 8}})
    c.call("text.create", {"parent": s4, "x": 33, "y": 315, "text": "2", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": SECONDARY}})
    c.call("shape.rectangle", {"parent": s4, "x": 62, "y": 300, "width": 44, "height": 44, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 10}})
    add_icon(s4, "guitar", 74, 312, size=20, color=PASTEL)
    c.call("text.create", {"parent": s4, "x": 116, "y": 303, "text": "Bones", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 323, "text": "Imagine Dragons • 02:45", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": SECONDARY}})
    add_icon(s4, "drag", 308, 312, size=22, color=SECONDARY)

    # Item 3
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 364, "width": 328, "height": 60, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s4, "x": 26, "y": 375, "width": 28, "height": 38, "props": {"fill": SURF_HIGH, "cornerRadius": 8}})
    c.call("text.create", {"parent": s4, "x": 33, "y": 387, "text": "3", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s4, "x": 62, "y": 372, "width": 44, "height": 44, "props": {"fill": SURF_HIGH, "cornerRadius": 10}})
    add_icon(s4, "fire", 74, 384, size=20, color=PASTEL)
    c.call("text.create", {"parent": s4, "x": 116, "y": 375, "text": "Enemy", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 395, "text": "Imagine Dragons • 02:53", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s4, "drag", 308, 384, size=20, color=PASTEL)

    # Append Guide Slot (#4)
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 436, "width": 328, "height": 52, "props": {"fill": SURF_HIGH, "cornerRadius": 16}})
    add_icon(s4, "plus", 32, 452, size=18, color=PASTEL)
    c.call("text.create", {"parent": s4, "x": 58, "y": 454, "text": "Lagu baru otomatis masuk ke urutan #4", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # FAB "+ Tambah Lagu"
    c.call("shape.rectangle", {"parent": s4, "x": 105, "y": 660, "width": 150, "height": 48, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    add_icon(s4, "plus", 118, 674, size=20, color=WHITE)
    c.call("text.create", {"parent": s4, "x": 144, "y": 676, "text": "Tambah Lagu", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    add_bottom_nav(s4, active_tab_index=2)


    # =========================================================================
    # SCREEN 5: Koleksi Playlist Hub (Layar Pustaka Playlist) [x: 1650, y: 50]
    # =========================================================================
    s5 = c.call("frame.create", {
        "x": 1650, "y": 50, "width": 360, "height": 800,
        "name": "Screen 5 — Koleksi Playlist Hub",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Header
    c.call("text.create", {"parent": s5, "x": 20, "y": 30, "text": "Koleksi Playlist", "props": {"fontSize": 24, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    # Buat Playlist Baru CTA Kapsul 9999px
    c.call("shape.rectangle", {"parent": s5, "x": 208, "y": 28, "width": 136, "height": 38, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    add_icon(s5, "plus", 218, 38, size=18, color=WHITE)
    c.call("text.create", {"parent": s5, "x": 240, "y": 40, "text": "Buat Baru", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Filter chips
    c.call("shape.rectangle", {"parent": s5, "x": 16, "y": 80, "width": 75, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s5, "x": 32, "y": 88, "text": "Semua", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s5, "x": 98, "y": 80, "width": 85, "height": 32, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s5, "x": 114, "y": 88, "text": "Playlist", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("shape.rectangle", {"parent": s5, "x": 190, "y": 80, "width": 80, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s5, "x": 206, "y": 88, "text": "Offline", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Grid Playlist Cards (2 Columns)
    # Row 1 Left: Rock Workout (2x2 Grid)
    c.call("shape.rectangle", {"parent": s5, "x": 16, "y": 128, "width": 156, "height": 160, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s5, "x": 28, "y": 140, "width": 64, "height": 64, "props": {"fill": BG, "cornerRadius": 12}})
    c.call("shape.rectangle", {"parent": s5, "x": 30, "y": 142, "width": 29, "height": 29, "props": {"fill": DARK_ACCENT, "cornerRadius": 4}})
    add_icon(s5, "bolt", 36, 148, size=16, color=SECONDARY)
    c.call("shape.rectangle", {"parent": s5, "x": 61, "y": 142, "width": 29, "height": 29, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 4}})
    add_icon(s5, "guitar", 67, 148, size=16, color=PASTEL)
    c.call("shape.rectangle", {"parent": s5, "x": 30, "y": 173, "width": 29, "height": 29, "props": {"fill": SURF_HIGH, "cornerRadius": 4}})
    add_icon(s5, "fire", 36, 179, size=16, color=PASTEL)
    c.call("shape.rectangle", {"parent": s5, "x": 61, "y": 173, "width": 29, "height": 29, "props": {"fill": PRIMARY, "cornerRadius": 4}})
    add_icon(s5, "drum", 67, 179, size=16, color=WHITE)
    c.call("text.create", {"parent": s5, "x": 28, "y": 218, "text": "Rock Workout", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s5, "x": 28, "y": 238, "text": "3 Lagu • 11 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Row 1 Right: Night Lo-Fi (Custom Cover)
    c.call("shape.rectangle", {"parent": s5, "x": 188, "y": 128, "width": 156, "height": 160, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s5, "x": 200, "y": 140, "width": 64, "height": 64, "props": {"fill": DARK_ACCENT, "cornerRadius": 12}})
    add_icon(s5, "moon", 220, 158, size=26, color=SECONDARY)
    c.call("text.create", {"parent": s5, "x": 200, "y": 218, "text": "Night Lo-Fi Drive", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s5, "x": 200, "y": 238, "text": "8 Lagu • 24 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Row 2 Left: Chill Gaming (2x2 Grid)
    c.call("shape.rectangle", {"parent": s5, "x": 16, "y": 300, "width": 156, "height": 160, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s5, "x": 28, "y": 312, "width": 64, "height": 64, "props": {"fill": BG, "cornerRadius": 12}})
    c.call("shape.rectangle", {"parent": s5, "x": 30, "y": 314, "width": 29, "height": 29, "props": {"fill": SURF_HIGH, "cornerRadius": 4}})
    add_icon(s5, "headphone", 36, 320, size=16, color=PASTEL)
    c.call("shape.rectangle", {"parent": s5, "x": 61, "y": 314, "width": 29, "height": 29, "props": {"fill": DARK_ACCENT, "cornerRadius": 4}})
    add_icon(s5, "music", 67, 320, size=16, color=SECONDARY)
    c.call("shape.rectangle", {"parent": s5, "x": 30, "y": 345, "width": 29, "height": 29, "props": {"fill": PRIMARY, "cornerRadius": 4}})
    add_icon(s5, "bolt", 36, 351, size=16, color=WHITE)
    c.call("shape.rectangle", {"parent": s5, "x": 61, "y": 345, "width": 29, "height": 29, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 4}})
    add_icon(s5, "piano", 67, 351, size=16, color=PASTEL)
    c.call("text.create", {"parent": s5, "x": 28, "y": 390, "text": "Chill Gaming", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s5, "x": 28, "y": 410, "text": "12 Lagu • 46 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Row 2 Right: Japanese Focus (Custom Cover)
    c.call("shape.rectangle", {"parent": s5, "x": 188, "y": 300, "width": 156, "height": 160, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s5, "x": 200, "y": 312, "width": 64, "height": 64, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 12}})
    c.call("text.create", {"parent": s5, "x": 222, "y": 352, "text": "夜", "props": {"fontSize": 26, "fill": WHITE}})
    c.call("text.create", {"parent": s5, "x": 200, "y": 390, "text": "Japanese Focus", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s5, "x": 200, "y": 410, "text": "5 Lagu • 19 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Row 3 Card: Favorit Dengar Sekali (Recent Purged History)
    c.call("shape.rectangle", {"parent": s5, "x": 16, "y": 472, "width": 328, "height": 64, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s5, "x": 26, "y": 482, "width": 44, "height": 44, "props": {"fill": SURF_HIGH, "cornerRadius": 10}})
    add_icon(s5, "bolt", 38, 494, size=20, color=SECONDARY)
    c.call("text.create", {"parent": s5, "x": 80, "y": 486, "text": "Riwayat Dengar Sekali", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s5, "x": 80, "y": 506, "text": "Otomatis dibersihkan • 0 MB", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    add_floating_player(s5, "Bones", "Imagine Dragons")
    add_bottom_nav(s5, active_tab_index=2)


    # =========================================================================
    # SCREEN 6: Layar Lagu Offline / M4A Storage (Tab "Offline") [x: 2050, y: 50]
    # =========================================================================
    s6 = c.call("frame.create", {
        "x": 2050, "y": 50, "width": 360, "height": 800,
        "name": "Screen 6 — Lagu Offline (M4A)",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Header
    c.call("text.create", {"parent": s6, "x": 20, "y": 30, "text": "Pustaka Offline", "props": {"fontSize": 24, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Filter chips
    c.call("shape.rectangle", {"parent": s6, "x": 16, "y": 76, "width": 75, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s6, "x": 32, "y": 84, "text": "Semua", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s6, "x": 98, "y": 76, "width": 85, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s6, "x": 114, "y": 84, "text": "Playlist", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s6, "x": 190, "y": 76, "width": 80, "height": 32, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s6, "x": 206, "y": 84, "text": "Offline", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Storage Info Banner Card (Flat, no borders)
    c.call("shape.rectangle", {"parent": s6, "x": 16, "y": 122, "width": 328, "height": 70, "props": {"fill": SURF, "cornerRadius": 16}})
    add_icon(s6, "download", 30, 140, size=24, color=SECONDARY)
    c.call("text.create", {"parent": s6, "x": 64, "y": 136, "text": "Penyimpanan Offline: 120 MB", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s6, "x": 64, "y": 156, "text": "24 Lagu M4A Siap Diputar Tanpa Kuota", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    # Progress storage
    c.call("shape.rectangle", {"parent": s6, "x": 64, "y": 172, "width": 260, "height": 6, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": s6, "x": 64, "y": 172, "width": 90, "height": 6, "props": {"fill": PRIMARY, "cornerRadius": 9999}})

    # Offline Track List
    c.call("text.create", {"parent": s6, "x": 18, "y": 210, "text": "Semua Lagu Tersimpan (24)", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Offline item 1
    c.call("shape.rectangle", {"parent": s6, "x": 16, "y": 236, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s6, "x": 26, "y": 244, "width": 42, "height": 42, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    add_icon(s6, "music", 37, 255, size=20, color=SECONDARY)
    c.call("text.create", {"parent": s6, "x": 78, "y": 249, "text": "Starfall", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s6, "x": 78, "y": 269, "text": "Illenium • 7.4 MB", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s6, "check", 308, 254, size=18, color=SECONDARY)

    # Offline item 2
    c.call("shape.rectangle", {"parent": s6, "x": 16, "y": 302, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s6, "x": 26, "y": 310, "width": 42, "height": 42, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 10}})
    add_icon(s6, "headphone", 37, 321, size=20, color=PASTEL)
    c.call("text.create", {"parent": s6, "x": 78, "y": 315, "text": "After Dark", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s6, "x": 78, "y": 335, "text": "Mr.Kitty • 8.6 MB", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s6, "check", 308, 320, size=18, color=SECONDARY)

    # Offline item 3
    c.call("shape.rectangle", {"parent": s6, "x": 16, "y": 368, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s6, "x": 26, "y": 376, "width": 42, "height": 42, "props": {"fill": SURF_HIGH, "cornerRadius": 10}})
    add_icon(s6, "piano", 37, 387, size=20, color=PASTEL)
    c.call("text.create", {"parent": s6, "x": 78, "y": 381, "text": "Midnight City", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s6, "x": 78, "y": 401, "text": "M83 • 8.1 MB", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s6, "check", 308, 386, size=18, color=SECONDARY)

    # Offline item 4
    c.call("shape.rectangle", {"parent": s6, "x": 16, "y": 434, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s6, "x": 26, "y": 442, "width": 42, "height": 42, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    add_icon(s6, "bolt", 37, 453, size=20, color=SECONDARY)
    c.call("text.create", {"parent": s6, "x": 78, "y": 447, "text": "Believer", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s6, "x": 78, "y": 467, "text": "Imagine Dragons • 6.8 MB", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s6, "check", 308, 452, size=18, color=SECONDARY)

    # Offline item 5
    c.call("shape.rectangle", {"parent": s6, "x": 16, "y": 500, "width": 328, "height": 58, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s6, "x": 26, "y": 508, "width": 42, "height": 42, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 10}})
    add_icon(s6, "guitar", 37, 519, size=20, color=PASTEL)
    c.call("text.create", {"parent": s6, "x": 78, "y": 513, "text": "Bones", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s6, "x": 78, "y": 533, "text": "Imagine Dragons • 5.9 MB", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    add_icon(s6, "check", 308, 518, size=18, color=SECONDARY)

    add_floating_player(s6, "Starfall", "Illenium")
    add_bottom_nav(s6, active_tab_index=0)


    # =========================================================================
    # SCREEN 7: Layar Profil & Pengaturan Audio Rust [x: 2450, y: 50]
    # =========================================================================
    s7 = c.call("frame.create", {
        "x": 2450, "y": 50, "width": 360, "height": 800,
        "name": "Screen 7 — Profil & Pengaturan",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Header
    c.call("text.create", {"parent": s7, "x": 20, "y": 30, "text": "Profil & Pengaturan", "props": {"fontSize": 24, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # User Profile Card (Clean)
    c.call("shape.rectangle", {"parent": s7, "x": 16, "y": 72, "width": 328, "height": 72, "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.ellipse", {"parent": s7, "x": 30, "y": 84, "width": 48, "height": 48, "props": {"fill": DARK_ACCENT}})
    add_icon(s7, "profile", 42, 96, size=24, color=SECONDARY)
    c.call("text.create", {"parent": s7, "x": 90, "y": 88, "text": "Only-One-Kind", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s7, "x": 90, "y": 110, "text": "RustVibe Standalone • Zero Ads", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Section 1: Mesin Audio Rust
    c.call("text.create", {"parent": s7, "x": 18, "y": 164, "text": "Mesin Audio Rust", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Setting Item 1: Audio Driver
    c.call("shape.rectangle", {"parent": s7, "x": 16, "y": 188, "width": 328, "height": 56, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 198, "text": "Driver Audio", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 218, "text": "AAudio Low-Latency", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s7, "x": 256, "y": 202, "width": 72, "height": 28, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s7, "x": 268, "y": 208, "text": "Aktif", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Setting Item 2: Format & Failover
    c.call("shape.rectangle", {"parent": s7, "x": 16, "y": 252, "width": 328, "height": 56, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 262, "text": "Format Utama", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 282, "text": "M4A (MP3 Backup Siap)", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s7, "x": 256, "y": 266, "width": 72, "height": 28, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s7, "x": 266, "y": 272, "text": "256k", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Section 2: Penyimpanan & yt-dlp
    c.call("text.create", {"parent": s7, "x": 18, "y": 328, "text": "Penyimpanan & Cache", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Setting Item 3: Ephemeral Auto Purge
    c.call("shape.rectangle", {"parent": s7, "x": 16, "y": 352, "width": 328, "height": 56, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 362, "text": "Hapus Dengar Sekali", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 382, "text": "Otomatis musnahkan cache", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    # Toggle Pill on
    c.call("shape.rectangle", {"parent": s7, "x": 278, "y": 366, "width": 50, "height": 28, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s7, "x": 302, "y": 368, "width": 24, "height": 24, "props": {"fill": WHITE}})

    # Setting Item 4: Bersihkan Cache Manual
    c.call("shape.rectangle", {"parent": s7, "x": 16, "y": 416, "width": 328, "height": 56, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 426, "text": "Kapasitas Cache", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 446, "text": "12 MB File Sementara", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s7, "x": 240, "y": 428, "width": 88, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    add_icon(s7, "trash", 248, 434, size=18, color=PASTEL)
    c.call("text.create", {"parent": s7, "x": 270, "y": 436, "text": "Bersihkan", "props": {"fontSize": 11, "fill": PASTEL}})

    # Section 3: Tentang
    c.call("text.create", {"parent": s7, "x": 18, "y": 492, "text": "Tentang", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("shape.rectangle", {"parent": s7, "x": 16, "y": 516, "width": 328, "height": 56, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 526, "text": "RustVibe v1.0.0", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s7, "x": 32, "y": 546, "text": "Pure Rust Audio • Standalone yt-dlp", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Bottom Nav (Active: Profil index 3)
    add_bottom_nav(s7, active_tab_index=3)


    # Zoom fit & Export all 7 screens
    c.call("view.zoomFit")

    screens = [
        (s1, "screen1_home.png", "Screen 1 (Home)"),
        (s2, "screen2_search_triage.png", "Screen 2 (Search/Triage)"),
        (s3, "screen3_full_player.png", "Screen 3 (Full Player)"),
        (s4, "screen4_playlist_reorder.png", "Screen 4 (Playlist Reorder)"),
        (s5, "screen5_playlist_hub.png", "Screen 5 (Playlist Hub)"),
        (s6, "screen6_offline_m4a.png", "Screen 6 (Offline M4A)"),
        (s7, "screen7_profile_settings.png", "Screen 7 (Profil & Pengaturan)")
    ]

    for sid, filename, label in screens:
        print(f"Exporting {label}...")
        c.call("file.export", {"ids": [sid], "path": f"/home/amir/projects/rust-music-android/{filename}", "format": "png", "scale": 2})

    # Save native FigCraft design file
    c.call("file.saveAs", {"path": "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft"})

    c.close()
    print("All 7 screens successfully generated with pure SVG vectors and zero borders!")

if __name__ == "__main__":
    build_all_screens()
