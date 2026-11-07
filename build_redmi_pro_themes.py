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

def build_all_themes():
    c = FigCraftBuilder()
    c.call("file.new")
    time.sleep(0.2)

    # EXACT REDMI NOTE 10 PRO VIEWPORT (1080x2400 Physical, 410 DPI -> 420dp x 936dp, 20:9 ratio)
    SCREEN_W = 420
    SCREEN_H = 936
    WHITE = "#FFFFFF"

    # Dark Theme Palette (Deepened Crimson Carrot #DC3500)
    DARK = {
        "bg": "#2E1E1F",           # Coffee Bean
        "surf": "#382426",         # Surface Container
        "surf_high": "#452E30",    # Surface Container High
        "surf_highest": "#523639", # Surface Container Highest
        "primary": "#DC3500",      # Deep Crimson Carrot (Darkened to harmonize with Coffee Bean)
        "secondary": "#E65100",    # Deep Amber / Warm Carrot Accent
        "text_main": WHITE,        # Main text
        "text_sub": "#F8E6D2",     # Antique White pastel
        "accent": "#52170B",       # Deep Molten Container
        "lyric_bg": "#52170B",
        "lyric_text": "#FFE0D6"
    }

    # Light Theme Palette (Base: Antique White #F8E6D2, Vibrant Crimson Carrot #FF4500)
    LIGHT = {
        "bg": "#F8E6D2",           # Antique White
        "surf": "#EED6BF",         # Surface Container Light
        "surf_high": "#E4C7AC",    # Surface Container High Light
        "surf_highest": "#D8B696", # Surface Container Highest Light
        "primary": "#FF4500",      # Original Vibrant Crimson Carrot (User confirmed perfect here)
        "secondary": "#E03E00",    # Deeper Carrot Accent
        "text_main": "#2E1E1F",    # Coffee Bean (High contrast dark text)
        "text_sub": "#634740",     # Warm Charcoal
        "accent": "#FCD5C5",       # Soft carrot tint container
        "lyric_bg": "#FCD5C5",
        "lyric_text": "#992900"
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

    def add_bottom_nav(screen_id, palette, active_tab_index=0):
        nav_y = SCREEN_H - 72
        c.call("shape.rectangle", {
            "parent": screen_id, "x": 0, "y": nav_y, "width": SCREEN_W, "height": 72,
            "name": "BottomNavigationBar",
            "props": {"fill": palette["surf"], "cornerRadius": [24, 24, 0, 0]}
        })
        # 4 evenly spaced tabs across 420dp width
        tabs = [
            ("home", "Beranda", 32),
            ("search", "Cari", 136),
            ("playlist", "Playlist", 240),
            ("profile", "Profil", 344)
        ]
        for idx, (icon_key, label, x_pos) in enumerate(tabs):
            if idx == active_tab_index:
                c.call("shape.rectangle", {
                    "parent": screen_id, "x": x_pos - 10, "y": nav_y + 10, "width": 68, "height": 34,
                    "name": f"ActiveTabPill_{idx}",
                    "props": {"fill": palette["primary"], "cornerRadius": 9999}
                })
                add_icon(screen_id, icon_key, x_pos + 14, y=nav_y + 16, size=20, color=WHITE)
                c.call("text.create", {"parent": screen_id, "x": x_pos + 4, "y": nav_y + 48, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": palette["primary"]}})
            else:
                add_icon(screen_id, icon_key, x_pos + 14, y=nav_y + 16, size=20, color=palette["text_sub"])
                c.call("text.create", {"parent": screen_id, "x": x_pos + 7, "y": nav_y + 48, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": palette["text_sub"]}})

    def add_floating_player(screen_id, palette, track_title="Starfall", artist="Illenium"):
        fp_y = SCREEN_H - 72 - 58 - 12 # 794
        c.call("shape.rectangle", {"parent": screen_id, "x": 20, "y": fp_y, "width": 380, "height": 58, "name": "FloatingPlayerBar", "props": {"fill": palette["surf_high"], "cornerRadius": 9999}})
        c.call("shape.ellipse", {"parent": screen_id, "x": 28, "y": fp_y + 8, "width": 42, "height": 42, "props": {"fill": palette["accent"]}})
        add_icon(screen_id, "music", 39, fp_y + 19, size=20, color=palette["secondary"])
        c.call("text.create", {"parent": screen_id, "x": 82, "y": fp_y + 12, "text": track_title, "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": palette["text_main"]}})
        c.call("text.create", {"parent": screen_id, "x": 82, "y": fp_y + 32, "text": artist, "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": palette["text_sub"]}})
        # Controls right
        c.call("shape.ellipse", {"parent": screen_id, "x": 290, "y": fp_y + 8, "width": 42, "height": 42, "props": {"fill": palette["primary"]}})
        add_icon(screen_id, "play", 303, fp_y + 20, size=18, color=WHITE)
        add_icon(screen_id, "next", 348, fp_y + 20, size=20, color=palette["text_sub"])

    def render_suite(prefix_name, base_y, pal):
        # 1. SCREEN 1: Home
        s1 = c.call("frame.create", {"x": 50, "y": base_y, "width": SCREEN_W, "height": SCREEN_H, "name": f"{prefix_name} 1 — Home", "props": {"fill": pal["bg"], "cornerRadius": 36}})['result']['id']
        c.call("text.create", {"parent": s1, "x": 20, "y": 44, "text": "RustVibe", "props": {"fontSize": 28, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("shape.ellipse", {"parent": s1, "x": 360, "y": 42, "width": 40, "height": 40, "props": {"fill": pal["surf_high"]}})
        add_icon(s1, "profile", 370, 52, size=20, color=pal["text_sub"])

        # Search Bar 9999px (Width 380)
        c.call("shape.rectangle", {"parent": s1, "x": 20, "y": 98, "width": 380, "height": 50, "props": {"fill": pal["surf"], "cornerRadius": 9999}})
        add_icon(s1, "search", 36, 113, size=20, color=pal["text_sub"])
        c.call("text.create", {"parent": s1, "x": 68, "y": 114, "text": "Cari lagu atau artis...", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})

        # Filter Chips
        c.call("shape.rectangle", {"parent": s1, "x": 20, "y": 162, "width": 80, "height": 34, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s1, "x": 36, "y": 170, "text": "Semua", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
        c.call("shape.rectangle", {"parent": s1, "x": 108, "y": 162, "width": 90, "height": 34, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s1, "x": 124, "y": 170, "text": "Playlist", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s1, "x": 206, "y": 162, "width": 85, "height": 34, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s1, "x": 222, "y": 170, "text": "Offline", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})

        # Playlist Section
        c.call("text.create", {"parent": s1, "x": 20, "y": 218, "text": "Playlist", "props": {"fontSize": 17, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        # 2-column cards (width 183 each, gap 14)
        c.call("shape.rectangle", {"parent": s1, "x": 20, "y": 246, "width": 183, "height": 146, "props": {"fill": pal["surf"], "cornerRadius": 20}})
        c.call("shape.rectangle", {"parent": s1, "x": 34, "y": 258, "width": 60, "height": 60, "props": {"fill": pal["bg"], "cornerRadius": 12}})
        c.call("shape.rectangle", {"parent": s1, "x": 36, "y": 260, "width": 27, "height": 27, "props": {"fill": pal["accent"], "cornerRadius": 4}})
        add_icon(s1, "bolt", 42, 266, size=15, color=pal["secondary"])
        c.call("shape.rectangle", {"parent": s1, "x": 65, "y": 260, "width": 27, "height": 27, "props": {"fill": pal["lyric_bg"], "cornerRadius": 4}})
        add_icon(s1, "guitar", 71, 266, size=15, color=pal["primary"])
        c.call("shape.rectangle", {"parent": s1, "x": 36, "y": 289, "width": 27, "height": 27, "props": {"fill": pal["surf_high"], "cornerRadius": 4}})
        add_icon(s1, "fire", 42, 295, size=15, color=pal["text_sub"])
        c.call("shape.rectangle", {"parent": s1, "x": 65, "y": 289, "width": 27, "height": 27, "props": {"fill": pal["primary"], "cornerRadius": 4}})
        add_icon(s1, "drum", 71, 295, size=15, color=WHITE)
        c.call("text.create", {"parent": s1, "x": 34, "y": 328, "text": "Rock Workout", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s1, "x": 34, "y": 348, "text": "3 Lagu • 11 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})

        # Card 2: Custom Cover
        c.call("shape.rectangle", {"parent": s1, "x": 217, "y": 246, "width": 183, "height": 146, "props": {"fill": pal["surf"], "cornerRadius": 20}})
        c.call("shape.rectangle", {"parent": s1, "x": 231, "y": 258, "width": 60, "height": 60, "props": {"fill": pal["accent"], "cornerRadius": 12}})
        add_icon(s1, "moon", 248, 273, size=26, color=pal["secondary"])
        c.call("text.create", {"parent": s1, "x": 231, "y": 328, "text": "Night Lo-Fi Drive", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s1, "x": 231, "y": 348, "text": "8 Lagu • 24 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})

        # Section 2: Baru Diunduh
        c.call("text.create", {"parent": s1, "x": 20, "y": 414, "text": "Baru Diunduh", "props": {"fontSize": 17, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        tracks = [
            ("Starfall", "Illenium • 03:42", "music"),
            ("After Dark", "Mr.Kitty • 04:18", "headphone"),
            ("Midnight City", "M83 • 04:03", "piano"),
            ("Believer", "Imagine Dragons • 03:24", "bolt")
        ]
        for t_idx, (t_title, t_sub, t_ico) in enumerate(tracks):
            ty = 444 + t_idx * 70
            c.call("shape.rectangle", {"parent": s1, "x": 20, "y": ty, "width": 380, "height": 62, "props": {"fill": pal["surf"], "cornerRadius": 18}})
            c.call("shape.rectangle", {"parent": s1, "x": 30, "y": ty + 9, "width": 44, "height": 44, "props": {"fill": pal["accent"], "cornerRadius": 12}})
            add_icon(s1, t_ico, 42, ty + 21, size=20, color=pal["secondary"])
            c.call("text.create", {"parent": s1, "x": 86, "y": ty + 14, "text": t_title, "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
            c.call("text.create", {"parent": s1, "x": 86, "y": ty + 36, "text": t_sub, "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
            c.call("shape.rectangle", {"parent": s1, "x": 326, "y": ty + 18, "width": 64, "height": 26, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
            c.call("text.create", {"parent": s1, "x": 342, "y": ty + 24, "text": "M4A", "props": {"fontSize": 11, "fill": pal["secondary"]}})

        add_floating_player(s1, pal)
        add_bottom_nav(s1, pal, active_tab_index=0)

        # 2. SCREEN 2: Search & Triage
        s2 = c.call("frame.create", {"x": 520, "y": base_y, "width": SCREEN_W, "height": SCREEN_H, "name": f"{prefix_name} 2 — Search & Triage", "props": {"fill": pal["bg"], "cornerRadius": 36}})['result']['id']
        c.call("shape.rectangle", {"parent": s2, "x": 20, "y": 44, "width": 380, "height": 50, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        add_icon(s2, "search", 36, 59, size=20, color=pal["secondary"])
        c.call("text.create", {"parent": s2, "x": 68, "y": 60, "text": "Racing into the Night", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        add_icon(s2, "close", 366, 59, size=20, color=pal["text_sub"])
        # Filters
        c.call("shape.rectangle", {"parent": s2, "x": 20, "y": 108, "width": 85, "height": 34, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s2, "x": 36, "y": 116, "text": "Semua", "props": {"fontSize": 12, "fill": WHITE}})
        c.call("shape.rectangle", {"parent": s2, "x": 115, "y": 108, "width": 75, "height": 34, "props": {"fill": pal["surf"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s2, "x": 133, "y": 116, "text": "M4A", "props": {"fontSize": 12, "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s2, "x": 200, "y": 108, "width": 80, "height": 34, "props": {"fill": pal["surf"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s2, "x": 218, "y": 116, "text": "Lirik", "props": {"fontSize": 12, "fill": pal["text_sub"]}})
        # Result item
        c.call("shape.rectangle", {"parent": s2, "x": 20, "y": 156, "width": 380, "height": 76, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        c.call("shape.rectangle", {"parent": s2, "x": 32, "y": 166, "width": 56, "height": 56, "props": {"fill": pal["accent"], "cornerRadius": 12}})
        c.call("text.create", {"parent": s2, "x": 49, "y": 184, "text": "夜", "props": {"fontSize": 20, "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s2, "x": 100, "y": 174, "text": "Racing into the Night", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s2, "x": 100, "y": 196, "text": "YOASOBI • 04:21", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        # Dark overlay
        c.call("shape.rectangle", {"parent": s2, "x": 0, "y": 246, "width": SCREEN_W, "height": SCREEN_H - 246, "props": {"fill": "#000000", "opacity": 0.45}})
        # Triage Sheet
        c.call("shape.rectangle", {"parent": s2, "x": 0, "y": 420, "width": SCREEN_W, "height": SCREEN_H - 420 - 72, "props": {"fill": pal["surf"], "cornerRadius": [32, 32, 0, 0]}})
        c.call("shape.rectangle", {"parent": s2, "x": 190, "y": 432, "width": 40, "height": 4, "props": {"fill": pal["surf_highest"], "cornerRadius": 9999}})
        c.call("shape.ellipse", {"parent": s2, "x": 184, "y": 454, "width": 52, "height": 52, "props": {"fill": pal["accent"]}})
        add_icon(s2, "check", 198, 468, size=24, color=pal["secondary"])
        c.call("text.create", {"parent": s2, "x": 105, "y": 522, "text": "Lagu Berhasil Diunduh", "props": {"fontSize": 19, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s2, "x": 108, "y": 552, "text": "Pilih tindakan penyimpanan:", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        # Option A
        c.call("shape.rectangle", {"parent": s2, "x": 24, "y": 594, "width": 372, "height": 62, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        add_icon(s2, "playlist", 108, 614, size=22, color=WHITE)
        c.call("text.create", {"parent": s2, "x": 140, "y": 616, "text": "Simpan ke Playlist", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
        # Option B
        c.call("shape.rectangle", {"parent": s2, "x": 24, "y": 670, "width": 372, "height": 62, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        add_icon(s2, "bolt", 88, 690, size=22, color=pal["secondary"])
        c.call("text.create", {"parent": s2, "x": 120, "y": 692, "text": "Dengerin Sekali (Lalu Hapus)", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("text.create", {"parent": s2, "x": 192, "y": 760, "text": "Batal", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        add_bottom_nav(s2, pal, active_tab_index=1)

        # 3. SCREEN 3: Full Player
        s3 = c.call("frame.create", {"x": 990, "y": base_y, "width": SCREEN_W, "height": SCREEN_H, "name": f"{prefix_name} 3 — Full Player", "props": {"fill": pal["bg"], "cornerRadius": 36}})['result']['id']
        add_icon(s3, "back", 20, 44, size=24, color=pal["text_main"])
        c.call("text.create", {"parent": s3, "x": 160, "y": 48, "text": "Rock Workout", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        add_icon(s3, "more", 376, 44, size=24, color=pal["text_main"])
        # Cover 200x200
        c.call("shape.rectangle", {"parent": s3, "x": 110, "y": 94, "width": 200, "height": 200, "props": {"fill": pal["surf"], "cornerRadius": 28}})
        c.call("text.create", {"parent": s3, "x": 182, "y": 174, "text": "夜", "props": {"fontSize": 60, "fill": pal["text_main"]}})
        # Title
        c.call("text.create", {"parent": s3, "x": 20, "y": 314, "text": "Racing into the Night", "props": {"fontSize": 20, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s3, "x": 20, "y": 342, "text": "YOASOBI", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s3, "x": 344, "y": 320, "width": 56, "height": 28, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s3, "x": 357, "y": 326, "text": "M4A", "props": {"fontSize": 11, "fill": pal["secondary"]}})
        # Scrubber
        c.call("shape.rectangle", {"parent": s3, "x": 20, "y": 380, "width": 380, "height": 6, "props": {"fill": pal["surf"], "cornerRadius": 9999}})
        c.call("shape.rectangle", {"parent": s3, "x": 20, "y": 380, "width": 160, "height": 6, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        c.call("shape.ellipse", {"parent": s3, "x": 172, "y": 374, "width": 18, "height": 18, "props": {"fill": pal["primary"]}})
        c.call("text.create", {"parent": s3, "x": 20, "y": 396, "text": "01:42", "props": {"fontSize": 12, "fill": pal["text_sub"]}})
        c.call("text.create", {"parent": s3, "x": 366, "y": 396, "text": "04:21", "props": {"fontSize": 12, "fill": pal["text_sub"]}})
        # Lyrics Stage (Height 310)
        c.call("shape.rectangle", {"parent": s3, "x": 20, "y": 426, "width": 380, "height": 310, "props": {"fill": pal["surf"], "cornerRadius": 26}})
        c.call("text.create", {"parent": s3, "x": 36, "y": 446, "text": "Lirik", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("text.create", {"parent": s3, "x": 36, "y": 482, "text": "沈むように溶けてゆくように", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"], "opacity": 0.5}})
        c.call("shape.rectangle", {"parent": s3, "x": 28, "y": 516, "width": 364, "height": 56, "props": {"fill": pal["lyric_bg"], "cornerRadius": 16}})
        c.call("text.create", {"parent": s3, "x": 42, "y": 534, "text": "二人だけの空が広がる夜に", "props": {"fontSize": 17, "fontFamily": "Plus Jakarta Sans", "fill": pal["lyric_text"]}})
        add_icon(s3, "music", 354, 532, size=20, color=pal["secondary"])
        c.call("text.create", {"parent": s3, "x": 36, "y": 594, "text": "「さよなら」だけだった", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"], "opacity": 0.7}})
        c.call("text.create", {"parent": s3, "x": 36, "y": 634, "text": "その一言で全てが分かった", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"], "opacity": 0.5}})
        c.call("text.create", {"parent": s3, "x": 36, "y": 674, "text": "日が沈み出した空と君の姿", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"], "opacity": 0.3}})
        # Controls
        add_icon(s3, "shuffle", 44, 760, size=24, color=pal["text_sub"])
        add_icon(s3, "prev", 114, 758, size=28, color=pal["text_sub"])
        c.call("shape.ellipse", {"parent": s3, "x": 178, "y": 742, "width": 64, "height": 64, "props": {"fill": pal["primary"]}})
        add_icon(s3, "pause", 198, 760, size=26, color=WHITE)
        add_icon(s3, "next", 278, 758, size=28, color=pal["text_sub"])
        add_icon(s3, "repeat", 352, 760, size=24, color=pal["text_sub"])
        # Volume
        c.call("shape.rectangle", {"parent": s3, "x": 80, "y": 834, "width": 260, "height": 36, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        add_icon(s3, "volume", 96, 842, size=20, color=pal["text_sub"])
        c.call("shape.rectangle", {"parent": s3, "x": 130, "y": 850, "width": 150, "height": 6, "props": {"fill": pal["surf"], "cornerRadius": 9999}})
        c.call("shape.rectangle", {"parent": s3, "x": 130, "y": 850, "width": 95, "height": 6, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        add_icon(s3, "volume", 298, 842, size=20, color=pal["text_sub"])

        # 4. SCREEN 4: Detail Playlist
        s4 = c.call("frame.create", {"x": 1460, "y": base_y, "width": SCREEN_W, "height": SCREEN_H, "name": f"{prefix_name} 4 — Detail Playlist", "props": {"fill": pal["bg"], "cornerRadius": 36}})['result']['id']
        add_icon(s4, "back", 20, 44, size=24, color=pal["text_main"])
        c.call("text.create", {"parent": s4, "x": 160, "y": 48, "text": "Playlist", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        add_icon(s4, "more", 376, 44, size=24, color=pal["text_main"])
        # Hero card
        c.call("shape.rectangle", {"parent": s4, "x": 20, "y": 92, "width": 380, "height": 110, "props": {"fill": pal["surf"], "cornerRadius": 22}})
        c.call("shape.rectangle", {"parent": s4, "x": 34, "y": 104, "width": 64, "height": 64, "props": {"fill": pal["bg"], "cornerRadius": 14}})
        c.call("shape.rectangle", {"parent": s4, "x": 36, "y": 106, "width": 29, "height": 29, "props": {"fill": pal["accent"], "cornerRadius": 4}})
        add_icon(s4, "bolt", 43, 112, size=15, color=pal["secondary"])
        c.call("shape.rectangle", {"parent": s4, "x": 67, "y": 106, "width": 29, "height": 29, "props": {"fill": pal["lyric_bg"], "cornerRadius": 4}})
        add_icon(s4, "guitar", 73, 112, size=15, color=pal["primary"])
        c.call("shape.rectangle", {"parent": s4, "x": 36, "y": 139, "width": 29, "height": 29, "props": {"fill": pal["surf_high"], "cornerRadius": 4}})
        add_icon(s4, "fire", 43, 145, size=15, color=pal["text_sub"])
        c.call("shape.rectangle", {"parent": s4, "x": 67, "y": 139, "width": 29, "height": 29, "props": {"fill": pal["primary"], "cornerRadius": 4}})
        add_icon(s4, "drum", 73, 145, size=15, color=WHITE)
        c.call("text.create", {"parent": s4, "x": 116, "y": 108, "text": "Rock Workout", "props": {"fontSize": 18, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s4, "x": 116, "y": 132, "text": "3 Lagu • 11 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s4, "x": 116, "y": 158, "width": 126, "height": 28, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        add_icon(s4, "camera", 126, 164, size=16, color=pal["text_sub"])
        c.call("text.create", {"parent": s4, "x": 148, "y": 166, "text": "Ubah Sampul", "props": {"fontSize": 11, "fill": pal["text_sub"]}})
        # Banner
        c.call("shape.rectangle", {"parent": s4, "x": 20, "y": 218, "width": 380, "height": 34, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        add_icon(s4, "drag", 112, 226, size=18, color=pal["text_sub"])
        c.call("text.create", {"parent": s4, "x": 136, "y": 228, "text": "Geser ikon untuk atur urutan", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        # Items
        items_reorder = [
            ("1", "Believer", "Imagine Dragons • 03:24", "bolt", pal["surf"], pal["surf_high"], pal["text_sub"], pal["text_sub"]),
            ("2", "Bones", "Imagine Dragons • 02:45", "guitar", pal["surf_highest"], pal["accent"], pal["primary"], pal["primary"]),
            ("3", "Enemy", "Imagine Dragons • 02:53", "fire", pal["surf"], pal["surf_high"], pal["text_sub"], pal["text_sub"])
        ]
        for r_idx, (num, title, artist_str, ico, card_bg, num_bg, num_col, drag_col) in enumerate(items_reorder):
            ry = 268 + r_idx * 76
            c.call("shape.rectangle", {"parent": s4, "x": 20, "y": ry, "width": 380, "height": 66, "props": {"fill": card_bg, "cornerRadius": 18}})
            c.call("shape.rectangle", {"parent": s4, "x": 32, "y": ry + 13, "width": 30, "height": 40, "props": {"fill": num_bg, "cornerRadius": 8}})
            c.call("text.create", {"parent": s4, "x": 42, "y": ry + 25, "text": num, "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": num_col}})
            c.call("shape.rectangle", {"parent": s4, "x": 72, "y": ry + 11, "width": 44, "height": 44, "props": {"fill": pal["accent"], "cornerRadius": 10}})
            add_icon(s4, ico, 84, ry + 23, size=20, color=drag_col)
            c.call("text.create", {"parent": s4, "x": 128, "y": ry + 15, "text": title, "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
            c.call("text.create", {"parent": s4, "x": 128, "y": ry + 37, "text": artist_str, "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": num_col}})
            add_icon(s4, "drag", 364, ry + 23, size=20, color=drag_col)

        # Append slot
        c.call("shape.rectangle", {"parent": s4, "x": 20, "y": 508, "width": 380, "height": 56, "props": {"fill": pal["surf_high"], "cornerRadius": 18}})
        add_icon(s4, "plus", 38, 526, size=20, color=pal["text_sub"])
        c.call("text.create", {"parent": s4, "x": 68, "y": 528, "text": "Lagu baru otomatis masuk ke urutan #4", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        # FAB
        c.call("shape.rectangle", {"parent": s4, "x": 130, "y": 786, "width": 160, "height": 50, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        add_icon(s4, "plus", 146, 801, size=20, color=WHITE)
        c.call("text.create", {"parent": s4, "x": 174, "y": 803, "text": "Tambah Lagu", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
        add_bottom_nav(s4, pal, active_tab_index=2)

        # 5. SCREEN 5: Playlist Hub
        s5 = c.call("frame.create", {"x": 1930, "y": base_y, "width": SCREEN_W, "height": SCREEN_H, "name": f"{prefix_name} 5 — Playlist Hub", "props": {"fill": pal["bg"], "cornerRadius": 36}})['result']['id']
        c.call("text.create", {"parent": s5, "x": 20, "y": 44, "text": "Koleksi Playlist", "props": {"fontSize": 26, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("shape.rectangle", {"parent": s5, "x": 256, "y": 40, "width": 144, "height": 42, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        add_icon(s5, "plus", 268, 52, size=18, color=WHITE)
        c.call("text.create", {"parent": s5, "x": 294, "y": 53, "text": "Buat Baru", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
        # Filters
        c.call("shape.rectangle", {"parent": s5, "x": 20, "y": 104, "width": 80, "height": 34, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s5, "x": 36, "y": 112, "text": "Semua", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s5, "x": 108, "y": 104, "width": 90, "height": 34, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s5, "x": 124, "y": 112, "text": "Playlist", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
        c.call("shape.rectangle", {"parent": s5, "x": 206, "y": 104, "width": 85, "height": 34, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s5, "x": 222, "y": 112, "text": "Offline", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        # 4 cards 2-columns (183 width each)
        hub_cards = [
            (20, 156, "Rock Workout", "3 Lagu • 11 min", "bolt", "guitar", "fire", "drum", True),
            (217, 156, "Night Lo-Fi Drive", "8 Lagu • 24 min", "moon", None, None, None, False),
            (20, 334, "Chill Gaming", "12 Lagu • 46 min", "headphone", "music", "bolt", "piano", True),
            (217, 334, "Japanese Focus", "5 Lagu • 19 min", "夜", None, None, None, False)
        ]
        for cx, cy, ctitle, csub, ico1, ico2, ico3, ico4, is_grid in hub_cards:
            c.call("shape.rectangle", {"parent": s5, "x": cx, "y": cy, "width": 183, "height": 164, "props": {"fill": pal["surf"], "cornerRadius": 20}})
            if is_grid:
                c.call("shape.rectangle", {"parent": s5, "x": cx + 14, "y": cy + 14, "width": 64, "height": 64, "props": {"fill": pal["bg"], "cornerRadius": 12}})
                c.call("shape.rectangle", {"parent": s5, "x": cx + 16, "y": cy + 16, "width": 29, "height": 29, "props": {"fill": pal["accent"], "cornerRadius": 4}})
                add_icon(s5, ico1, cx + 22, cy + 22, size=16, color=pal["secondary"])
                c.call("shape.rectangle", {"parent": s5, "x": cx + 47, "y": cy + 16, "width": 29, "height": 29, "props": {"fill": pal["lyric_bg"], "cornerRadius": 4}})
                add_icon(s5, ico2, cx + 53, cy + 22, size=16, color=pal["primary"])
                c.call("shape.rectangle", {"parent": s5, "x": cx + 16, "y": cy + 47, "width": 29, "height": 29, "props": {"fill": pal["surf_high"], "cornerRadius": 4}})
                add_icon(s5, ico3, cx + 22, cy + 53, size=16, color=pal["text_sub"])
                c.call("shape.rectangle", {"parent": s5, "x": cx + 47, "y": cy + 47, "width": 29, "height": 29, "props": {"fill": pal["primary"], "cornerRadius": 4}})
                add_icon(s5, ico4, cx + 53, cy + 53, size=16, color=WHITE)
            else:
                c.call("shape.rectangle", {"parent": s5, "x": cx + 14, "y": cy + 14, "width": 64, "height": 64, "props": {"fill": pal["accent"], "cornerRadius": 12}})
                if ico1 == "夜":
                    c.call("text.create", {"parent": s5, "x": cx + 36, "y": cy + 54, "text": "夜", "props": {"fontSize": 26, "fill": pal["text_main"]}})
                else:
                    add_icon(s5, ico1, cx + 34, cy + 32, size=26, color=pal["secondary"])
            c.call("text.create", {"parent": s5, "x": cx + 14, "y": cy + 96, "text": ctitle, "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
            c.call("text.create", {"parent": s5, "x": cx + 14, "y": cy + 118, "text": csub, "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})

        # Riwayat
        c.call("shape.rectangle", {"parent": s5, "x": 20, "y": 516, "width": 380, "height": 68, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        c.call("shape.rectangle", {"parent": s5, "x": 32, "y": 527, "width": 46, "height": 46, "props": {"fill": pal["surf_high"], "cornerRadius": 12}})
        add_icon(s5, "bolt", 45, 540, size=20, color=pal["secondary"])
        c.call("text.create", {"parent": s5, "x": 90, "y": 532, "text": "Riwayat Dengar Sekali", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s5, "x": 90, "y": 554, "text": "Otomatis dibersihkan • 0 MB", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        add_floating_player(s5, pal, "Bones", "Imagine Dragons")
        add_bottom_nav(s5, pal, active_tab_index=2)

        # 6. SCREEN 6: Offline M4A
        s6 = c.call("frame.create", {"x": 2400, "y": base_y, "width": SCREEN_W, "height": SCREEN_H, "name": f"{prefix_name} 6 — Lagu Offline", "props": {"fill": pal["bg"], "cornerRadius": 36}})['result']['id']
        c.call("text.create", {"parent": s6, "x": 20, "y": 44, "text": "Pustaka Offline", "props": {"fontSize": 26, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("shape.rectangle", {"parent": s6, "x": 20, "y": 98, "width": 80, "height": 34, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s6, "x": 36, "y": 106, "text": "Semua", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s6, "x": 108, "y": 98, "width": 90, "height": 34, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s6, "x": 124, "y": 106, "text": "Playlist", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s6, "x": 206, "y": 98, "width": 85, "height": 34, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s6, "x": 222, "y": 106, "text": "Offline", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
        # Storage Card
        c.call("shape.rectangle", {"parent": s6, "x": 20, "y": 150, "width": 380, "height": 78, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        add_icon(s6, "download", 36, 172, size=26, color=pal["secondary"])
        c.call("text.create", {"parent": s6, "x": 74, "y": 166, "text": "Penyimpanan Offline: 120 MB", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s6, "x": 74, "y": 188, "text": "24 Lagu M4A Siap Diputar Tanpa Kuota", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s6, "x": 74, "y": 208, "width": 306, "height": 6, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("shape.rectangle", {"parent": s6, "x": 74, "y": 208, "width": 110, "height": 6, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s6, "x": 20, "y": 248, "text": "Semua Lagu Tersimpan (24)", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        off_tracks = [
            ("Starfall", "Illenium • 7.4 MB", "music"),
            ("After Dark", "Mr.Kitty • 8.6 MB", "headphone"),
            ("Midnight City", "M83 • 8.1 MB", "piano"),
            ("Believer", "Imagine Dragons • 6.8 MB", "bolt"),
            ("Bones", "Imagine Dragons • 5.9 MB", "guitar")
        ]
        for oi, (ot, os, oc) in enumerate(off_tracks):
            oy = 278 + oi * 68
            c.call("shape.rectangle", {"parent": s6, "x": 20, "y": oy, "width": 380, "height": 60, "props": {"fill": pal["surf"], "cornerRadius": 18}})
            c.call("shape.rectangle", {"parent": s6, "x": 30, "y": oy + 9, "width": 42, "height": 42, "props": {"fill": pal["accent"], "cornerRadius": 10}})
            add_icon(s6, oc, 41, oy + 20, size=20, color=pal["secondary"])
            c.call("text.create", {"parent": s6, "x": 86, "y": oy + 14, "text": ot, "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
            c.call("text.create", {"parent": s6, "x": 86, "y": oy + 36, "text": os, "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
            add_icon(s6, "check", 360, oy + 21, size=18, color=pal["secondary"])
        add_floating_player(s6, pal, "Starfall", "Illenium")
        add_bottom_nav(s6, pal, active_tab_index=0)

        # 7. SCREEN 7: Profile & Settings
        s7 = c.call("frame.create", {"x": 2870, "y": base_y, "width": SCREEN_W, "height": SCREEN_H, "name": f"{prefix_name} 7 — Profil & Pengaturan", "props": {"fill": pal["bg"], "cornerRadius": 36}})['result']['id']
        c.call("text.create", {"parent": s7, "x": 20, "y": 44, "text": "Profil & Pengaturan", "props": {"fontSize": 26, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        # Profile card
        c.call("shape.rectangle", {"parent": s7, "x": 20, "y": 96, "width": 380, "height": 78, "props": {"fill": pal["surf"], "cornerRadius": 22}})
        c.call("shape.ellipse", {"parent": s7, "x": 34, "y": 110, "width": 50, "height": 50, "props": {"fill": pal["accent"]}})
        add_icon(s7, "profile", 47, 123, size=24, color=pal["secondary"])
        c.call("text.create", {"parent": s7, "x": 98, "y": 114, "text": "Only-One-Kind", "props": {"fontSize": 17, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s7, "x": 98, "y": 138, "text": "RustVibe Standalone • Zero Ads", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        # Section 1
        c.call("text.create", {"parent": s7, "x": 20, "y": 196, "text": "Mesin Audio Rust", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        # Item 1
        c.call("shape.rectangle", {"parent": s7, "x": 20, "y": 224, "width": 380, "height": 60, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 234, "text": "Driver Audio", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 256, "text": "AAudio Low-Latency", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s7, "x": 308, "y": 240, "width": 76, "height": 28, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s7, "x": 322, "y": 246, "text": "Aktif", "props": {"fontSize": 11, "fill": pal["secondary"]}})
        # Item 2
        c.call("shape.rectangle", {"parent": s7, "x": 20, "y": 294, "width": 380, "height": 60, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 304, "text": "Format Utama", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 326, "text": "M4A (MP3 Backup Siap)", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s7, "x": 308, "y": 310, "width": 76, "height": 28, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        c.call("text.create", {"parent": s7, "x": 320, "y": 316, "text": "256k", "props": {"fontSize": 11, "fill": pal["secondary"]}})
        # Section 2
        c.call("text.create", {"parent": s7, "x": 20, "y": 376, "text": "Penyimpanan & Cache", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("shape.rectangle", {"parent": s7, "x": 20, "y": 404, "width": 380, "height": 60, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 414, "text": "Hapus Dengar Sekali", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 436, "text": "Otomatis musnahkan cache", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s7, "x": 332, "y": 420, "width": 52, "height": 28, "props": {"fill": pal["primary"], "cornerRadius": 9999}})
        c.call("shape.ellipse", {"parent": s7, "x": 358, "y": 422, "width": 24, "height": 24, "props": {"fill": WHITE}})
        # Clear cache
        c.call("shape.rectangle", {"parent": s7, "x": 20, "y": 474, "width": 380, "height": 60, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 484, "text": "Kapasitas Cache", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 506, "text": "12 MB File Sementara", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        c.call("shape.rectangle", {"parent": s7, "x": 290, "y": 488, "width": 94, "height": 32, "props": {"fill": pal["surf_high"], "cornerRadius": 9999}})
        add_icon(s7, "trash", 300, 495, size=18, color=pal["text_sub"])
        c.call("text.create", {"parent": s7, "x": 324, "y": 496, "text": "Bersihkan", "props": {"fontSize": 11, "fill": pal["text_sub"]}})
        # Section 3
        c.call("text.create", {"parent": s7, "x": 20, "y": 556, "text": "Tentang", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("shape.rectangle", {"parent": s7, "x": 20, "y": 584, "width": 380, "height": 60, "props": {"fill": pal["surf"], "cornerRadius": 18}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 594, "text": "RustVibe v1.0.0", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_main"]}})
        c.call("text.create", {"parent": s7, "x": 36, "y": 616, "text": "Pure Rust Audio • Standalone yt-dlp", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": pal["text_sub"]}})
        add_bottom_nav(s7, pal, active_tab_index=3)

        return [
            (s1, f"{prefix_name.lower().replace(' ', '_')}_s1.png"),
            (s2, f"{prefix_name.lower().replace(' ', '_')}_s2.png"),
            (s3, f"{prefix_name.lower().replace(' ', '_')}_s3.png"),
            (s4, f"{prefix_name.lower().replace(' ', '_')}_s4.png"),
            (s5, f"{prefix_name.lower().replace(' ', '_')}_s5.png"),
            (s6, f"{prefix_name.lower().replace(' ', '_')}_s6.png"),
            (s7, f"{prefix_name.lower().replace(' ', '_')}_s7.png"),
        ]

    print("Building Dark Theme with Deepened Crimson Carrot (#DC3500) on Redmi Note 10 Pro viewport (420x936)...")
    dark_screens = render_suite("Dark", 50, DARK)

    print("Building Light Theme with Antique White (#F8E6D2) and Vibrant Crimson Carrot (#FF4500) on 420x936...")
    light_screens = render_suite("Light", 1080, LIGHT)

    c.call("view.zoomFit")

    for sid, filename in dark_screens + light_screens:
        print(f"Exporting {filename}...")
        c.call("file.export", {"ids": [sid], "path": f"/home/amir/projects/rust-music-android/{filename}", "format": "png", "scale": 1})

    c.call("file.saveAs", {"path": "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft"})
    c.close()
    print("Dual Theme (14 complete artboards) generated successfully at 420x936 (Redmi Note 10 Pro physical viewport)!")

if __name__ == "__main__":
    build_all_themes()
