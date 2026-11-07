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
    "profile": "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z",
    "music": "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z",
    "github": "M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0 1 12 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0 0 22 12.017C22 6.484 17.522 2 12 2z",
    "youtube": "M21.582 6.186a2.806 2.806 0 0 0-1.974-1.987C17.868 3.75 12 3.75 12 3.75s-5.868 0-7.608.449A2.806 2.806 0 0 0 2.418 6.186C2 7.935 2 12 2 12s0 4.065.418 5.814a2.806 2.806 0 0 0 1.974 1.987c1.74.449 7.608.449 7.608.449s5.868 0 7.608-.449a2.806 2.806 0 0 0 1.974-1.987C22 16.065 22 12 22 12s0-4.065-.418-5.814zM10 15.464V8.536L16 12l-6 3.464z",
    "moon": "M9.37 5.51A7.35 7.35 0 0 0 9.1 7.5c0 4.08 3.32 7.4 7.4 7.4.68 0 1.35-.09 1.99-.27A9.014 9.014 0 0 1 12 21c-4.97 0-9-4.03-9-9 0-3.93 2.51-7.27 6.37-8.49z",
    "sun": "M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1z",
    "bolt": "M11 21h-1l1-7H7.5c-.88 0-.33-.75-.31-.78C8.48 10.94 10.42 7.54 13.01 3h1l-1 7h3.5c.49 0 .56.33.47.51l-5.98 10.49z",
    "trash": "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
    "open_link": "M19 19H5V5h7V3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z"
}

def build_profile_social_artboards():
    c = FigCraftBuilder()

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

    # =========================================================================
    # 1. PORTRAIT PROFILE SCREEN WITH GITHUB & YOUTUBE SHORTCUTS (420 x 936 dp)
    # =========================================================================
    p1 = c.call("frame.create", {
        "x": 50, "y": 3600, "width": 420, "height": 936,
        "name": "Portrait — Profil dengan Shortcut GitHub & YouTube",
        "props": {"fill": DARK["bg"], "cornerRadius": 36}
    })['result']['id']

    c.call("text.create", {"parent": p1, "x": 20, "y": 48, "text": "Profil & Pengaturan", "props": {"fontSize": 24, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})

    # User Profile Card
    c.call("shape.rectangle", {"parent": p1, "x": 20, "y": 92, "width": 380, "height": 72, "props": {"fill": DARK["surf"], "cornerRadius": 20}})
    c.call("shape.ellipse", {"parent": p1, "x": 36, "y": 104, "width": 48, "height": 48, "props": {"fill": DARK["accent"]}})
    add_icon(p1, "profile", 48, 116, size=24, color=DARK["secondary"])
    c.call("text.create", {"parent": p1, "x": 98, "y": 108, "text": "Only-One-Kind", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p1, "x": 98, "y": 130, "text": "RustVibe Standalone • Zero Ads", "props": {"fontSize": 12, "fill": DARK["text_sub"]}})

    # Tampilan & Tema Card
    c.call("text.create", {"parent": p1, "x": 20, "y": 182, "text": "Tampilan & Tema", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})
    c.call("shape.rectangle", {"parent": p1, "x": 20, "y": 206, "width": 380, "height": 108, "props": {"fill": DARK["surf"], "cornerRadius": 20}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 220, "text": "Tema Gelap", "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 240, "text": "Coffee Bean • Deep Crimson", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # 3 Pills
    c.call("shape.rectangle", {"parent": p1, "x": 36, "y": 266, "width": 96, "height": 32, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p1, "x": 58, "y": 274, "text": "Gelap", "props": {"fontSize": 11, "fill": WHITE, "fontWeight": 700}})

    c.call("shape.rectangle", {"parent": p1, "x": 140, "y": 266, "width": 96, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p1, "x": 162, "y": 274, "text": "Terang", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    c.call("shape.rectangle", {"parent": p1, "x": 244, "y": 266, "width": 142, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p1, "x": 258, "y": 274, "text": "⚡ Aksen Android", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Mesin Audio Card
    c.call("text.create", {"parent": p1, "x": 20, "y": 332, "text": "Mesin Audio Rust", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})
    c.call("shape.rectangle", {"parent": p1, "x": 20, "y": 356, "width": 380, "height": 56, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 366, "text": "Driver Audio", "props": {"fontSize": 14, "fill": WHITE}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 386, "text": "AAudio Low-Latency", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": p1, "x": 320, "y": 368, "width": 64, "height": 28, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p1, "x": 332, "y": 374, "text": "Aktif", "props": {"fontSize": 11, "fill": DARK["primary"], "fontWeight": 700}})

    # Penyimpanan & Cache
    c.call("text.create", {"parent": p1, "x": 20, "y": 430, "text": "Penyimpanan & Cache", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})
    c.call("shape.rectangle", {"parent": p1, "x": 20, "y": 454, "width": 380, "height": 56, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 464, "text": "Kapasitas Cache", "props": {"fontSize": 14, "fill": WHITE}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 484, "text": "18 MB dari 6 Lagu", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": p1, "x": 290, "y": 466, "width": 94, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    add_icon(p1, "trash", 302, 473, size=18, color=DARK["text_sub"])
    c.call("text.create", {"parent": p1, "x": 326, "y": 475, "text": "Bersihkan", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Info Aplikasi Card
    c.call("text.create", {"parent": p1, "x": 20, "y": 528, "text": "Tentang & Developer", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})
    c.call("shape.rectangle", {"parent": p1, "x": 20, "y": 552, "width": 380, "height": 58, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 562, "text": "RustVibe v1.0.0", "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p1, "x": 36, "y": 582, "text": "Native Rust Audio Engine • Zero Lag", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # =========================================================================
    # NEW REQUESTED SHORTCUTS: GITHUB REPO & YOUTUBE CHANNEL BUTTONS (9999px)
    # =========================================================================
    c.call("text.create", {"parent": p1, "x": 20, "y": 628, "text": "Tautan Proyek & Media", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})

    # 1. GitHub Repo Button (Full Pill 9999px, Height 54)
    c.call("shape.rectangle", {"parent": p1, "x": 20, "y": 654, "width": 380, "height": 54, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    # GitHub Octocat Icon on Circle
    c.call("shape.ellipse", {"parent": p1, "x": 28, "y": 661, "width": 40, "height": 40, "props": {"fill": DARK["surf_high"]}})
    add_icon(p1, "github", 36, 669, size=24, color=WHITE)
    c.call("text.create", {"parent": p1, "x": 78, "y": 663, "text": "GitHub Repository", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p1, "x": 78, "y": 682, "text": "github.com/Only-One-Kind/rustvibe-music", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    add_icon(p1, "open_link", 366, 671, size=20, color=DARK["primary"])

    # 2. YouTube Channel Button (Full Pill 9999px, Height 54)
    c.call("shape.rectangle", {"parent": p1, "x": 20, "y": 718, "width": 380, "height": 54, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    # YouTube Red Accent Badge Circle
    c.call("shape.ellipse", {"parent": p1, "x": 28, "y": 725, "width": 40, "height": 40, "props": {"fill": "#FF0000"}})
    add_icon(p1, "youtube", 36, 733, size=24, color=WHITE)
    c.call("text.create", {"parent": p1, "x": 78, "y": 727, "text": "Channel YouTube Resmi", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p1, "x": 78, "y": 746, "text": "Kunjungi Channel & Playlist Official", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    add_icon(p1, "open_link", 366, 735, size=20, color=DARK["primary"])

    # Bottom Nav Bar Portrait
    c.call("shape.rectangle", {"parent": p1, "x": 0, "y": 864, "width": 420, "height": 72, "props": {"fill": DARK["surf"], "cornerRadius": [24, 24, 0, 0]}})
    c.call("shape.rectangle", {"parent": p1, "x": 334, "y": 874, "width": 64, "height": 34, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    add_icon(p1, "profile", 354, 879, size=24, color=WHITE)


    # =========================================================================
    # 2. LANDSCAPE PROFILE WITH GITHUB & YOUTUBE CARDS (936 x 420 dp)
    # =========================================================================
    p2 = c.call("frame.create", {
        "x": 550, "y": 3600, "width": 936, "height": 420,
        "name": "Landscape — Profil dengan Shortcut GitHub & YouTube (2-Column)",
        "props": {"fill": DARK["bg"], "cornerRadius": 28}
    })['result']['id']

    # Navigation Rail Left
    c.call("shape.rectangle", {"parent": p2, "x": 0, "y": 0, "width": 72, "height": 420, "props": {"fill": DARK["surf"], "cornerRadius": [0, 24, 24, 0]}})
    c.call("shape.ellipse", {"parent": p2, "x": 18, "y": 18, "width": 36, "height": 36, "props": {"fill": DARK["primary"]}})
    add_icon(p2, "music", 24, 24, size=24, color=WHITE)
    # Active Profile Pill
    c.call("shape.rectangle", {"parent": p2, "x": 10, "y": 284, "width": 52, "height": 34, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    add_icon(p2, "profile", 24, 289, size=24, color=WHITE)
    c.call("text.create", {"parent": p2, "x": 14, "y": 322, "text": "Profil", "props": {"fontSize": 10, "fill": DARK["primary"], "fontWeight": 700}})

    # Header Top
    c.call("text.create", {"parent": p2, "x": 92, "y": 18, "text": "Profil & Pengaturan", "props": {"fontSize": 22, "fontFamily": "Plus Jakarta Sans", "fill": WHITE, "fontWeight": 700}})

    # Two Column Layout in Landscape
    # Column 1 (Left, Width 400): User, Theme, Engine
    c.call("shape.rectangle", {"parent": p2, "x": 92, "y": 52, "width": 400, "height": 66, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("shape.ellipse", {"parent": p2, "x": 106, "y": 61, "width": 48, "height": 48, "props": {"fill": DARK["accent"]}})
    add_icon(p2, "profile", 118, 73, size=24, color=DARK["secondary"])
    c.call("text.create", {"parent": p2, "x": 168, "y": 66, "text": "Only-One-Kind", "props": {"fontSize": 15, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p2, "x": 168, "y": 88, "text": "RustVibe Standalone • Zero Ads", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Theme Card
    c.call("shape.rectangle", {"parent": p2, "x": 92, "y": 128, "width": 400, "height": 104, "props": {"fill": DARK["surf"], "cornerRadius": 18}})
    c.call("text.create", {"parent": p2, "x": 110, "y": 140, "text": "Tampilan & Tema", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p2, "x": 110, "y": 160, "text": "Coffee Bean • Deep Crimson", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    # 3 Pills
    c.call("shape.rectangle", {"parent": p2, "x": 106, "y": 184, "width": 84, "height": 32, "props": {"fill": DARK["primary"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p2, "x": 126, "y": 192, "text": "Gelap", "props": {"fontSize": 11, "fill": WHITE, "fontWeight": 700}})
    c.call("shape.rectangle", {"parent": p2, "x": 198, "y": 184, "width": 84, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p2, "x": 218, "y": 192, "text": "Terang", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": p2, "x": 290, "y": 184, "width": 186, "height": 32, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p2, "x": 306, "y": 192, "text": "⚡ Aksen Android 15-16", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Audio Engine
    c.call("shape.rectangle", {"parent": p2, "x": 92, "y": 242, "width": 400, "height": 56, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("text.create", {"parent": p2, "x": 108, "y": 252, "text": "Driver Audio Native Rust", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p2, "x": 108, "y": 272, "text": "AAudio Low-Latency • Symphonia Core", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": p2, "x": 420, "y": 254, "width": 58, "height": 26, "props": {"fill": DARK["accent"], "cornerRadius": 6}})
    c.call("text.create", {"parent": p2, "x": 430, "y": 258, "text": "AKTIF", "props": {"fontSize": 10, "fill": DARK["primary"], "fontWeight": 700}})

    # Column 2 (Right, Width 408): Social Shortcuts & App Info
    c.call("text.create", {"parent": p2, "x": 508, "y": 52, "text": "Tautan Proyek & Media Resmi", "props": {"fontSize": 14, "fill": WHITE, "fontWeight": 700}})

    # GitHub Card 9999px (Landscape)
    c.call("shape.rectangle", {"parent": p2, "x": 508, "y": 76, "width": 408, "height": 52, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": p2, "x": 516, "y": 82, "width": 40, "height": 40, "props": {"fill": DARK["surf_high"]}})
    add_icon(p2, "github", 524, 90, size=24, color=WHITE)
    c.call("text.create", {"parent": p2, "x": 566, "y": 85, "text": "GitHub Repository", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p2, "x": 566, "y": 103, "text": "github.com/Only-One-Kind/rustvibe-music", "props": {"fontSize": 10, "fill": DARK["text_sub"]}})
    add_icon(p2, "open_link", 880, 92, size=18, color=DARK["primary"])

    # YouTube Card 9999px (Landscape)
    c.call("shape.rectangle", {"parent": p2, "x": 508, "y": 136, "width": 408, "height": 52, "props": {"fill": DARK["surf"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": p2, "x": 516, "y": 142, "width": 40, "height": 40, "props": {"fill": "#FF0000"}})
    add_icon(p2, "youtube", 524, 150, size=24, color=WHITE)
    c.call("text.create", {"parent": p2, "x": 566, "y": 145, "text": "Channel YouTube Resmi", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p2, "x": 566, "y": 163, "text": "Kunjungi Channel & Playlist Official", "props": {"fontSize": 10, "fill": DARK["text_sub"]}})
    add_icon(p2, "open_link", 880, 152, size=18, color=DARK["primary"])

    # Storage Info
    c.call("shape.rectangle", {"parent": p2, "x": 508, "y": 198, "width": 408, "height": 52, "props": {"fill": DARK["surf"], "cornerRadius": 16}})
    c.call("text.create", {"parent": p2, "x": 524, "y": 207, "text": "Kapasitas Cache Lagu", "props": {"fontSize": 13, "fill": WHITE, "fontWeight": 700}})
    c.call("text.create", {"parent": p2, "x": 524, "y": 227, "text": "18 MB Terpakai • 6 Lagu", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.rectangle", {"parent": p2, "x": 816, "y": 208, "width": 88, "height": 30, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("text.create", {"parent": p2, "x": 830, "y": 214, "text": "Bersihkan", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})

    # Bottom Player Landscape
    c.call("shape.rectangle", {"parent": p2, "x": 92, "y": 352, "width": 824, "height": 54, "props": {"fill": DARK["surf_high"], "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": p2, "x": 100, "y": 359, "width": 40, "height": 40, "props": {"fill": DARK["accent"]}})
    add_icon(p2, "music", 110, 369, size=20, color=DARK["secondary"])
    c.call("text.create", {"parent": p2, "x": 150, "y": 363, "text": "MALU MALU", "props": {"fontSize": 13, "fill": WHITE}})
    c.call("text.create", {"parent": p2, "x": 150, "y": 381, "text": "dia", "props": {"fontSize": 11, "fill": DARK["text_sub"]}})
    c.call("shape.ellipse", {"parent": p2, "x": 860, "y": 360, "width": 38, "height": 38, "props": {"fill": DARK["primary"]}})
    add_icon(p2, "pause", 870, 370, size=18, color=WHITE)

    # Export PNGs
    exports = [
        (p1, "figcraft_profile_portrait_social.png"),
        (p2, "figcraft_profile_landscape_social.png")
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
    print("Profile Social Shortcuts Artboards generated and exported successfully!")

if __name__ == "__main__":
    build_profile_social_artboards()
