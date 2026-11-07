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

def build_screens():
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

    def add_bottom_nav(screen_id, active_tab_index=0):
        # Bottom Navigation Bar (Docked at bottom, NO BORDER, cornerRadius top 24)
        c.call("shape.rectangle", {
            "parent": screen_id, "x": 0, "y": 728, "width": 360, "height": 72,
            "name": "BottomNavigationBar",
            "props": {"fill": SURF, "cornerRadius": [24, 24, 0, 0]}
        })
        
        tabs = [
            ("🏠", "Beranda", 24),
            ("🔍", "Cari", 114),
            ("📁", "Playlist", 204),
            ("⚙️", "Profil", 294)
        ]
        
        for idx, (icon, label, x_pos) in enumerate(tabs):
            if idx == active_tab_index:
                # Active Tab: 9999px Capsule Pill in Raspberry Red (NO BORDER)
                c.call("shape.rectangle", {
                    "parent": screen_id, "x": x_pos - 8, "y": 738, "width": 64, "height": 34,
                    "name": f"ActiveTabPill_{idx}",
                    "props": {"fill": PRIMARY, "cornerRadius": 9999}
                })
                c.call("text.create", {"parent": screen_id, "x": x_pos + 8, "y": 745, "text": icon, "props": {"fontSize": 15, "fill": WHITE}})
                c.call("text.create", {"parent": screen_id, "x": x_pos + 3, "y": 776, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": SECONDARY}})
            else:
                # Inactive Tab
                c.call("text.create", {"parent": screen_id, "x": x_pos + 12, "y": 744, "text": icon, "props": {"fontSize": 16, "fill": PASTEL}})
                c.call("text.create", {"parent": screen_id, "x": x_pos + 6, "y": 776, "text": label, "props": {"fontSize": 10, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # =========================================================================
    # SCREEN 1: Home & Playlists Screen (x: 50, y: 50) - CLEAN & MINIMAL
    # =========================================================================
    s1 = c.call("frame.create", {
        "x": 50, "y": 50, "width": 360, "height": 800,
        "name": "Screen 1 — Home & Playlists",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Header: Clean Title & Profile Avatar (ZERO clutter)
    c.call("text.create", {"parent": s1, "x": 20, "y": 30, "text": "RustVibe", "props": {"fontSize": 26, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("shape.ellipse", {"parent": s1, "x": 304, "y": 28, "width": 38, "height": 38, "props": {"fill": SURF_HIGH}})
    c.call("text.create", {"parent": s1, "x": 314, "y": 38, "text": "👤", "props": {"fontSize": 15, "fill": PASTEL}})

    # Search Bar 9999px (Concise placeholder)
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 76, "width": 328, "height": 48, "name": "SearchBar", "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 34, "y": 91, "text": "🔍  Cari lagu atau artis...", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Filter Chips (NO BORDER)
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 134, "width": 75, "height": 32, "name": "Chip1", "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 32, "y": 142, "text": "Semua", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    c.call("shape.rectangle", {"parent": s1, "x": 98, "y": 134, "width": 85, "height": 32, "name": "Chip2", "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 114, "y": 142, "text": "Playlist", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    c.call("shape.rectangle", {"parent": s1, "x": 190, "y": 134, "width": 80, "height": 32, "name": "Chip3", "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 206, "y": 142, "text": "Offline", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Section 1: Playlist
    c.call("text.create", {"parent": s1, "x": 18, "y": 182, "text": "Playlist", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Card Playlist 1 (2x2 Grid Mosaic Cover - CLEAN)
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 208, "width": 156, "height": 140, "name": "PlaylistCard1", "props": {"fill": SURF, "cornerRadius": 20}})
    # 2x2 Grid box
    c.call("shape.rectangle", {"parent": s1, "x": 28, "y": 220, "width": 56, "height": 56, "name": "Grid2x2Base", "props": {"fill": BG, "cornerRadius": 12}})
    # 4 mini cells
    c.call("shape.rectangle", {"parent": s1, "x": 30, "y": 222, "width": 25, "height": 25, "props": {"fill": DARK_ACCENT, "cornerRadius": 4}})
    c.call("text.create", {"parent": s1, "x": 36, "y": 228, "text": "⚡", "props": {"fontSize": 10}})
    c.call("shape.rectangle", {"parent": s1, "x": 57, "y": 222, "width": 25, "height": 25, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 4}})
    c.call("text.create", {"parent": s1, "x": 63, "y": 228, "text": "🎸", "props": {"fontSize": 10}})
    c.call("shape.rectangle", {"parent": s1, "x": 30, "y": 249, "width": 25, "height": 25, "props": {"fill": SURF_HIGH, "cornerRadius": 4}})
    c.call("text.create", {"parent": s1, "x": 36, "y": 255, "text": "🔥", "props": {"fontSize": 10}})
    c.call("shape.rectangle", {"parent": s1, "x": 57, "y": 249, "width": 25, "height": 25, "props": {"fill": PRIMARY, "cornerRadius": 4}})
    c.call("text.create", {"parent": s1, "x": 63, "y": 255, "text": "🥁", "props": {"fontSize": 10}})

    c.call("text.create", {"parent": s1, "x": 28, "y": 288, "text": "Rock Workout", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 28, "y": 308, "text": "3 Lagu • 11 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Card Playlist 2 (Custom Single Cover - CLEAN)
    c.call("shape.rectangle", {"parent": s1, "x": 188, "y": 208, "width": 156, "height": 140, "name": "PlaylistCard2", "props": {"fill": SURF, "cornerRadius": 20}})
    c.call("shape.rectangle", {"parent": s1, "x": 200, "y": 220, "width": 56, "height": 56, "name": "CustomCover", "props": {"fill": DARK_ACCENT, "cornerRadius": 12}})
    c.call("text.create", {"parent": s1, "x": 218, "y": 236, "text": "🌙", "props": {"fontSize": 20}})

    c.call("text.create", {"parent": s1, "x": 200, "y": 288, "text": "Night Lo-Fi Drive", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 200, "y": 308, "text": "8 Lagu • 24 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Section 2: Lagu Terunduh
    c.call("text.create", {"parent": s1, "x": 18, "y": 366, "text": "Baru Diunduh", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Track item 1 (Clean: Judul + Artis/Produser, no text clutter)
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 394, "width": 328, "height": 58, "name": "Track1", "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s1, "x": 26, "y": 402, "width": 42, "height": 42, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    c.call("text.create", {"parent": s1, "x": 38, "y": 415, "text": "🎵", "props": {"fontSize": 15}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 407, "text": "Starfall", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 427, "text": "Illenium • 03:42", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s1, "x": 272, "y": 410, "width": 60, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 286, "y": 416, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Track item 2
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 460, "width": 328, "height": 58, "name": "Track2", "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s1, "x": 26, "y": 468, "width": 42, "height": 42, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 10}})
    c.call("text.create", {"parent": s1, "x": 38, "y": 481, "text": "🎧", "props": {"fontSize": 15}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 473, "text": "After Dark", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 493, "text": "Mr.Kitty • 04:18", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s1, "x": 272, "y": 476, "width": 60, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 286, "y": 482, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Track item 3
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 526, "width": 328, "height": 58, "name": "Track3", "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s1, "x": 26, "y": 534, "width": 42, "height": 42, "props": {"fill": SURF_HIGH, "cornerRadius": 10}})
    c.call("text.create", {"parent": s1, "x": 38, "y": 547, "text": "🎹", "props": {"fontSize": 15}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 539, "text": "Midnight City", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 78, "y": 559, "text": "M83 • 04:03", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s1, "x": 272, "y": 542, "width": 60, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s1, "x": 286, "y": 548, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Floating Mini Player Capsule 9999px (y: 658)
    c.call("shape.rectangle", {"parent": s1, "x": 16, "y": 658, "width": 328, "height": 58, "name": "FloatingPlayerBar", "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s1, "x": 24, "y": 666, "width": 42, "height": 42, "props": {"fill": DARK_ACCENT}})
    c.call("text.create", {"parent": s1, "x": 35, "y": 679, "text": "🎵", "props": {"fontSize": 15}})
    c.call("text.create", {"parent": s1, "x": 76, "y": 670, "text": "Starfall", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 76, "y": 690, "text": "Illenium", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.ellipse", {"parent": s1, "x": 238, "y": 666, "width": 42, "height": 42, "props": {"fill": PRIMARY}})
    c.call("text.create", {"parent": s1, "x": 253, "y": 679, "text": "▶", "props": {"fontSize": 15, "fill": WHITE}})
    c.call("text.create", {"parent": s1, "x": 298, "y": 679, "text": "⏭", "props": {"fontSize": 17, "fill": PASTEL}})

    # Bottom Nav Tab (Active: Beranda index 0)
    add_bottom_nav(s1, active_tab_index=0)


    # =========================================================================
    # SCREEN 2: Search & Triage Sheet (x: 450, y: 50) - TO THE POINT!
    # =========================================================================
    s2 = c.call("frame.create", {
        "x": 450, "y": 50, "width": 360, "height": 800,
        "name": "Screen 2 — Search & Triage Sheet",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Active Search Bar
    c.call("shape.rectangle", {"parent": s2, "x": 16, "y": 28, "width": 328, "height": 48, "name": "ActiveSearch", "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s2, "x": 34, "y": 43, "text": "Racing into the Night", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s2, "x": 312, "y": 43, "text": "✕", "props": {"fontSize": 14, "fill": PASTEL}})

    # Search Filter Chips (Concise)
    c.call("shape.rectangle", {"parent": s2, "x": 16, "y": 86, "width": 80, "height": 30, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s2, "x": 28, "y": 94, "text": "Semua", "props": {"fontSize": 11, "fill": WHITE}})
    c.call("shape.rectangle", {"parent": s2, "x": 104, "y": 86, "width": 70, "height": 30, "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s2, "x": 120, "y": 94, "text": "M4A", "props": {"fontSize": 11, "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s2, "x": 182, "y": 86, "width": 75, "height": 30, "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s2, "x": 198, "y": 94, "text": "Lirik", "props": {"fontSize": 11, "fill": PASTEL}})

    # Result Item Card (Clean)
    c.call("shape.rectangle", {"parent": s2, "x": 16, "y": 128, "width": 328, "height": 72, "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s2, "x": 28, "y": 136, "width": 56, "height": 56, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    c.call("text.create", {"parent": s2, "x": 44, "y": 154, "text": "夜", "props": {"fontSize": 20, "fill": WHITE}})
    c.call("text.create", {"parent": s2, "x": 94, "y": 144, "text": "Racing into the Night", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s2, "x": 94, "y": 166, "text": "YOASOBI • 04:21", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Dark overlay
    c.call("shape.rectangle", {"parent": s2, "x": 0, "y": 210, "width": 360, "height": 590, "props": {"fill": "#1A1012", "opacity": 0.55}})

    # POST-DOWNLOAD TRIAGE BOTTOM SHEET - STRICT TO THE POINT!
    c.call("shape.rectangle", {
        "parent": s2, "x": 0, "y": 320, "width": 360, "height": 410,
        "name": "TriageBottomSheet",
        "props": {"fill": SURF, "cornerRadius": [32, 32, 0, 0]}
    })

    # Drag pill handle
    c.call("shape.rectangle", {"parent": s2, "x": 160, "y": 332, "width": 40, "height": 4, "props": {"fill": SURF_HIGHEST, "cornerRadius": 9999}})

    # Checkmark circle badge
    c.call("shape.ellipse", {"parent": s2, "x": 154, "y": 352, "width": 52, "height": 52, "props": {"fill": DARK_ACCENT}})
    c.call("text.create", {"parent": s2, "x": 170, "y": 368, "text": "✓", "props": {"fontSize": 22, "fill": SECONDARY}})

    # Sheet Header: DIRECT & TO THE POINT (Under title directly goes to "Pilih tindakan penyimpanan:")
    c.call("text.create", {"parent": s2, "x": 75, "y": 420, "text": "Lagu Berhasil Diunduh", "props": {"fontSize": 18, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s2, "x": 78, "y": 448, "text": "Pilih tindakan penyimpanan:", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Option A: Simpan ke Playlist (Clean, prominent CTA)
    c.call("shape.rectangle", {
        "parent": s2, "x": 20, "y": 484, "width": 320, "height": 60,
        "name": "BtnSavePlaylist",
        "props": {"fill": PRIMARY, "cornerRadius": 9999}
    })
    c.call("text.create", {"parent": s2, "x": 90, "y": 504, "text": "📁  Simpan ke Playlist", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Option B: Dengerin Sekali Saja (Clean secondary action)
    c.call("shape.rectangle", {
        "parent": s2, "x": 20, "y": 556, "width": 320, "height": 60,
        "name": "BtnEphemeral",
        "props": {"fill": SURF_HIGH, "cornerRadius": 9999}
    })
    c.call("text.create", {"parent": s2, "x": 76, "y": 576, "text": "⚡  Dengerin Sekali (Lalu Hapus)", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Cancel button
    c.call("shape.rectangle", {"parent": s2, "x": 120, "y": 632, "width": 120, "height": 34, "props": {"fill": "none"}})
    c.call("text.create", {"parent": s2, "x": 145, "y": 640, "text": "Batal", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Bottom Nav Tab (Active: Cari index 1)
    add_bottom_nav(s2, active_tab_index=1)


    # =========================================================================
    # SCREEN 3: Full Player & Synced Karaoke Lyrics (x: 850, y: 50) - CLEAN
    # =========================================================================
    s3 = c.call("frame.create", {
        "x": 850, "y": 50, "width": 360, "height": 800,
        "name": "Screen 3 — Full Player & Synced Lyrics",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Top Bar (Clean)
    c.call("text.create", {"parent": s3, "x": 20, "y": 28, "text": "‹", "props": {"fontSize": 24, "fill": WHITE}})
    c.call("text.create", {"parent": s3, "x": 120, "y": 32, "text": "Rock Workout", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("text.create", {"parent": s3, "x": 320, "y": 32, "text": "⋮", "props": {"fontSize": 18, "fill": WHITE}})

    # Big Album Art Card (NO BORDER)
    c.call("shape.rectangle", {"parent": s3, "x": 90, "y": 68, "width": 180, "height": 180, "name": "BigCover", "props": {"fill": SURF, "cornerRadius": 24}})
    c.call("text.create", {"parent": s3, "x": 155, "y": 140, "text": "夜", "props": {"fontSize": 52, "fill": WHITE}})

    # Track Title & Artist (Clean)
    c.call("text.create", {"parent": s3, "x": 20, "y": 266, "text": "Racing into the Night", "props": {"fontSize": 18, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s3, "x": 20, "y": 292, "text": "YOASOBI", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Codec Badge (Concise "M4A")
    c.call("shape.rectangle", {"parent": s3, "x": 286, "y": 272, "width": 54, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s3, "x": 298, "y": 278, "text": "M4A", "props": {"fontSize": 11, "fill": SECONDARY}})

    # Progress Scrubber (Clean)
    c.call("shape.rectangle", {"parent": s3, "x": 20, "y": 324, "width": 320, "height": 6, "props": {"fill": SURF, "cornerRadius": 9999}})
    c.call("shape.rectangle", {"parent": s3, "x": 20, "y": 324, "width": 130, "height": 6, "props": {"fill": PRIMARY, "cornerRadius": 9999}})
    c.call("shape.ellipse", {"parent": s3, "x": 144, "y": 319, "width": 16, "height": 16, "props": {"fill": PRIMARY}})
    c.call("text.create", {"parent": s3, "x": 20, "y": 338, "text": "01:42", "props": {"fontSize": 11, "fill": PASTEL}})
    c.call("text.create", {"parent": s3, "x": 310, "y": 338, "text": "04:21", "props": {"fontSize": 11, "fill": PASTEL}})

    # SYNCHRONIZED KARAOKE LYRICS STAGE (CLEAN & TO THE POINT)
    c.call("shape.rectangle", {
        "parent": s3, "x": 16, "y": 366, "width": 328, "height": 270,
        "name": "LyricsStageContainer",
        "props": {"fill": SURF, "cornerRadius": 24}
    })

    # Clean Header
    c.call("text.create", {"parent": s3, "x": 32, "y": 384, "text": "Lirik", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Line 1 (Past inactive line)
    c.call("text.create", {"parent": s3, "x": 32, "y": 418, "text": "沈むように溶けてゆくように", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.5}})

    # Line 2 (CURRENT ACTIVE SINKRON LINE - Clean, NO engineer jargon!)
    c.call("shape.rectangle", {
        "parent": s3, "x": 24, "y": 450, "width": 312, "height": 52,
        "name": "ActiveLyricHighlight",
        "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 16}
    })
    c.call("text.create", {"parent": s3, "x": 36, "y": 466, "text": "二人だけの空が広がる夜に 🎵", "props": {"fontSize": 16, "fontFamily": "Plus Jakarta Sans", "fill": ACTIVE_LYRIC_TXT}})

    # Line 3 (Upcoming inactive line)
    c.call("text.create", {"parent": s3, "x": 32, "y": 522, "text": "「さよなら」だけだった", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.7}})

    # Line 4 (Upcoming inactive line 2)
    c.call("text.create", {"parent": s3, "x": 32, "y": 558, "text": "その一言で全てが分かった", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.5}})

    # Line 5 (Upcoming inactive line 3)
    c.call("text.create", {"parent": s3, "x": 32, "y": 594, "text": "日が沈み出した空と君の姿", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL, "opacity": 0.3}})

    # Main Playback Controls (Clean)
    c.call("text.create", {"parent": s3, "x": 34, "y": 674, "text": "🔀", "props": {"fontSize": 20, "fill": PASTEL}})
    c.call("text.create", {"parent": s3, "x": 96, "y": 674, "text": "⏮", "props": {"fontSize": 24, "fill": PASTEL}})
    # Play / Pause circle 64dp
    c.call("shape.ellipse", {"parent": s3, "x": 148, "y": 654, "width": 64, "height": 64, "props": {"fill": PRIMARY}})
    c.call("text.create", {"parent": s3, "x": 172, "y": 672, "text": "⏸", "props": {"fontSize": 24, "fill": WHITE}})
    c.call("text.create", {"parent": s3, "x": 242, "y": 674, "text": "⏭", "props": {"fontSize": 24, "fill": PASTEL}})
    c.call("text.create", {"parent": s3, "x": 304, "y": 674, "text": "🔁", "props": {"fontSize": 20, "fill": PASTEL}})

    # Bottom volume pill
    c.call("shape.rectangle", {"parent": s3, "x": 80, "y": 742, "width": 200, "height": 34, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s3, "x": 95, "y": 752, "text": "🔈 ━━━━━●━━━━ 🔊", "props": {"fontSize": 12, "fill": PASTEL}})


    # =========================================================================
    # SCREEN 4: Playlist Detail & Drag Reordering (x: 1250, y: 50) - CLEAN
    # =========================================================================
    s4 = c.call("frame.create", {
        "x": 1250, "y": 50, "width": 360, "height": 800,
        "name": "Screen 4 — Playlist & Drag Reorder",
        "props": {"fill": BG, "cornerRadius": 32}
    })['result']['id']

    # Header Bar
    c.call("text.create", {"parent": s4, "x": 20, "y": 28, "text": "‹", "props": {"fontSize": 24, "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 130, "y": 32, "text": "Playlist", "props": {"fontSize": 15, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 320, "y": 32, "text": "⋮", "props": {"fontSize": 18, "fill": WHITE}})

    # Playlist Hero Header (Clean)
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 66, "width": 328, "height": 100, "props": {"fill": SURF, "cornerRadius": 20}})
    
    # 2x2 Grid Cover (60x60)
    c.call("shape.rectangle", {"parent": s4, "x": 28, "y": 76, "width": 56, "height": 56, "props": {"fill": BG, "cornerRadius": 12}})
    c.call("shape.rectangle", {"parent": s4, "x": 30, "y": 78, "width": 25, "height": 25, "props": {"fill": DARK_ACCENT, "cornerRadius": 4}})
    c.call("text.create", {"parent": s4, "x": 36, "y": 84, "text": "⚡", "props": {"fontSize": 10}})
    c.call("shape.rectangle", {"parent": s4, "x": 57, "y": 78, "width": 25, "height": 25, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 4}})
    c.call("text.create", {"parent": s4, "x": 63, "y": 84, "text": "🎸", "props": {"fontSize": 10}})
    c.call("shape.rectangle", {"parent": s4, "x": 30, "y": 105, "width": 25, "height": 25, "props": {"fill": SURF_HIGH, "cornerRadius": 4}})
    c.call("text.create", {"parent": s4, "x": 36, "y": 111, "text": "🔥", "props": {"fontSize": 10}})
    c.call("shape.rectangle", {"parent": s4, "x": 57, "y": 105, "width": 25, "height": 25, "props": {"fill": PRIMARY, "cornerRadius": 4}})
    c.call("text.create", {"parent": s4, "x": 63, "y": 111, "text": "🥁", "props": {"fontSize": 10}})

    c.call("text.create", {"parent": s4, "x": 100, "y": 80, "text": "Rock Workout", "props": {"fontSize": 17, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 100, "y": 102, "text": "3 Lagu • 11 min", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Button Ubah Sampul 9999px (Clean)
    c.call("shape.rectangle", {"parent": s4, "x": 100, "y": 124, "width": 110, "height": 26, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s4, "x": 112, "y": 130, "text": "📷 Ubah Sampul", "props": {"fontSize": 10, "fill": PASTEL}})

    # Instruction banner (Concise)
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 178, "width": 328, "height": 32, "props": {"fill": SURF_HIGH, "cornerRadius": 9999}})
    c.call("text.create", {"parent": s4, "x": 78, "y": 188, "text": "⠿  Geser ikon untuk atur urutan", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # TRACK 1 (Position 1)
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 222, "width": 328, "height": 60, "name": "ItemPos1", "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s4, "x": 26, "y": 233, "width": 28, "height": 38, "props": {"fill": SURF_HIGH, "cornerRadius": 8}})
    c.call("text.create", {"parent": s4, "x": 33, "y": 245, "text": "1", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s4, "x": 62, "y": 230, "width": 44, "height": 44, "props": {"fill": DARK_ACCENT, "cornerRadius": 10}})
    c.call("text.create", {"parent": s4, "x": 74, "y": 244, "text": "⚡", "props": {"fontSize": 16}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 233, "text": "Believer", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 253, "text": "Imagine Dragons • 03:24", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("text.create", {"parent": s4, "x": 308, "y": 243, "text": "⠿", "props": {"fontSize": 20, "fill": PASTEL}})

    # TRACK 2 (Position 2 — ACTIVE DRAGGING STATE! Clean: no filler text)
    c.call("shape.rectangle", {
        "parent": s4, "x": 16, "y": 292, "width": 328, "height": 62, "name": "ItemPos2_Dragging",
        "props": {"fill": SURF_HIGHEST, "cornerRadius": 16}
    })
    c.call("shape.rectangle", {"parent": s4, "x": 26, "y": 303, "width": 28, "height": 38, "props": {"fill": DARK_ACCENT, "cornerRadius": 8}})
    c.call("text.create", {"parent": s4, "x": 33, "y": 315, "text": "2", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": SECONDARY}})
    c.call("shape.rectangle", {"parent": s4, "x": 62, "y": 300, "width": 44, "height": 44, "props": {"fill": ACTIVE_LYRIC_BG, "cornerRadius": 10}})
    c.call("text.create", {"parent": s4, "x": 74, "y": 314, "text": "🎸", "props": {"fontSize": 16}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 303, "text": "Bones", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 323, "text": "Imagine Dragons • 02:45", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": SECONDARY}})
    c.call("text.create", {"parent": s4, "x": 308, "y": 313, "text": "⠿", "props": {"fontSize": 22, "fill": SECONDARY}})

    # TRACK 3 (Position 3)
    c.call("shape.rectangle", {"parent": s4, "x": 16, "y": 364, "width": 328, "height": 60, "name": "ItemPos3", "props": {"fill": SURF, "cornerRadius": 16}})
    c.call("shape.rectangle", {"parent": s4, "x": 26, "y": 375, "width": 28, "height": 38, "props": {"fill": SURF_HIGH, "cornerRadius": 8}})
    c.call("text.create", {"parent": s4, "x": 33, "y": 387, "text": "3", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("shape.rectangle", {"parent": s4, "x": 62, "y": 372, "width": 44, "height": 44, "props": {"fill": SURF_HIGH, "cornerRadius": 10}})
    c.call("text.create", {"parent": s4, "x": 74, "y": 386, "text": "🔥", "props": {"fontSize": 16}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 375, "text": "Enemy", "props": {"fontSize": 14, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})
    c.call("text.create", {"parent": s4, "x": 116, "y": 395, "text": "Imagine Dragons • 02:53", "props": {"fontSize": 11, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})
    c.call("text.create", {"parent": s4, "x": 308, "y": 385, "text": "⠿", "props": {"fontSize": 20, "fill": PASTEL}})

    # APPEND-TO-END VISUAL GUIDE CARD (Slot Posisi #4 - CLEAN & DIRECT!)
    c.call("shape.rectangle", {
        "parent": s4, "x": 16, "y": 436, "width": 328, "height": 52, "name": "AppendPolicyCard",
        "props": {"fill": SURF_HIGH, "cornerRadius": 16}
    })
    c.call("text.create", {"parent": s4, "x": 42, "y": 454, "text": "＋  Lagu baru otomatis masuk ke urutan #4", "props": {"fontSize": 12, "fontFamily": "Plus Jakarta Sans", "fill": PASTEL}})

    # Floating Action Button 9999px (FAB: "+ Tambah Lagu")
    c.call("shape.rectangle", {
        "parent": s4, "x": 105, "y": 660, "width": 150, "height": 48,
        "name": "FAB_AddTrack",
        "props": {"fill": PRIMARY, "cornerRadius": 9999}
    })
    c.call("text.create", {"parent": s4, "x": 128, "y": 675, "text": "➕  Tambah Lagu", "props": {"fontSize": 13, "fontFamily": "Plus Jakarta Sans", "fill": WHITE}})

    # Bottom Nav Tab (Active: Playlist index 2)
    add_bottom_nav(s4, active_tab_index=2)

    # Zoom fit & Export
    c.call("view.zoomFit")

    print("Exporting Screen 1 (Clean text)...")
    c.call("file.export", {"ids": [s1], "path": "/home/amir/projects/rust-music-android/screen1_home.png", "format": "png", "scale": 2})
    print("Exporting Screen 2 (To the point!)...")
    c.call("file.export", {"ids": [s2], "path": "/home/amir/projects/rust-music-android/screen2_search_triage.png", "format": "png", "scale": 2})
    print("Exporting Screen 3 (Clean player)...")
    c.call("file.export", {"ids": [s3], "path": "/home/amir/projects/rust-music-android/screen3_full_player.png", "format": "png", "scale": 2})
    print("Exporting Screen 4 (Clean playlist)...")
    c.call("file.export", {"ids": [s4], "path": "/home/amir/projects/rust-music-android/screen4_playlist_reorder.png", "format": "png", "scale": 2})

    # Save native FigCraft design file
    c.call("file.saveAs", {"path": "/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft"})

    c.close()
    print("Screens updated with clean, direct, to-the-point text!")

if __name__ == "__main__":
    build_screens()
