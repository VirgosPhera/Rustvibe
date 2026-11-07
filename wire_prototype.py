import socket
import json
import time

class FigCraftPrototyper:
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
        return json.loads(line)

    def close(self):
        self.sock.close()

def wire_prototype_motion():
    p = FigCraftPrototyper()

    # Find the 7 top-level screens
    screens_res = p.call('node.find', {'type': 'frame'})
    screen_ids = screens_res['result']['ids']
    
    screen_map = {}
    for sid in screen_ids:
        node = p.call('node.get', {'id': sid})
        name = node.get('result', {}).get('name', '')
        screen_map[name] = sid
        print(f"Screen: {name} -> ID {sid}")

    s1 = screen_map.get("Screen 1 — Home (Semua)")
    s2 = screen_map.get("Screen 2 — Search & Triage")
    s3 = screen_map.get("Screen 3 — Full Player & Lirik")
    s4 = screen_map.get("Screen 4 — Detail Playlist")
    s5 = screen_map.get("Screen 5 — Koleksi Playlist Hub")
    s6 = screen_map.get("Screen 6 — Lagu Offline (M4A)")
    s7 = screen_map.get("Screen 7 — Profil & Pengaturan")

    # Set flow starting point at Screen 1
    flow_res = p.call('proto.flow.add', {'start': s1, 'name': 'RustVibe Prototype Flow'})
    print("Flow start set:", flow_res)

    # Helper to add interaction
    def link(source_id, dest_screen_id, transition_type="dissolve", direction=None, duration=250, easing="quick"):
        if not source_id or not dest_screen_id:
            return
        trans = {"type": transition_type}
        if direction:
            trans["direction"] = direction
        
        # clear any existing
        p.call('proto.interaction.clear', {'id': source_id})
        res = p.call('proto.interaction.add', {
            'id': source_id,
            'dest': dest_screen_id,
            'trigger': {'type': 'onClick'},
            'action': {'type': 'navigate'},
            'transition': trans,
            'easing': easing,
            'durationMs': duration
        })
        return res

    # 1. Floating Mini Players -> Screen 3 (Full Player) with slideIn TOP
    fps = p.call('node.find', {'name': 'FloatingPlayerBar'})['result']['ids']
    for fp_id in fps:
        print(f"Wiring MiniPlayer {fp_id} -> FullPlayer {s3}")
        link(fp_id, s3, transition_type="slideIn", direction="top", duration=350, easing="bouncy")

    # 2. Full Player (Screen 3) Back Button -> Screen 1 (slideOut BOTTOM)
    # The back icon is a vector layer at x: 20, y: 28 in s3
    s3_children = p.call('node.get', {'id': s3})['result'].get('children', [])
    for child in s3_children:
        # Check back icon (first vector around y:28)
        if child.get('x') == 20 and child.get('y') == 28:
            print(f"Wiring FullPlayer Back button {child['id']} -> Home {s1}")
            link(child['id'], s1, transition_type="slideOut", direction="bottom", duration=300, easing="quick")

    # 3. Screen 1: Search Bar -> Screen 2 (dissolve)
    s1_children = p.call('node.get', {'id': s1})['result'].get('children', [])
    for child in s1_children:
        if child.get('name') == 'SearchBar' or (child.get('y') == 76 and child.get('height') == 48):
            print(f"Wiring SearchBar {child['id']} -> Search {s2}")
            link(child['id'], s2, transition_type="dissolve", duration=200)
        # Playlist Card 1 -> Detail Playlist Screen 4
        if child.get('name') == 'PlaylistCard1' or (child.get('y') == 208 and child.get('x') == 16):
            print(f"Wiring PlaylistCard1 {child['id']} -> DetailPlaylist {s4}")
            link(child['id'], s4, transition_type="smartAnimate", duration=350, easing="bouncy")

    # 4. Screen 2: Triage Buttons
    s2_children = p.call('node.get', {'id': s2})['result'].get('children', [])
    for child in s2_children:
        # Option A: Save to Playlist -> Screen 4
        if child.get('name') == 'BtnSavePlaylist':
            print(f"Wiring BtnSavePlaylist {child['id']} -> Screen 4")
            link(child['id'], s4, transition_type="smartAnimate", duration=300, easing="quick")
        # Option B: Ephemeral -> Screen 3
        if child.get('name') == 'BtnEphemeral':
            print(f"Wiring BtnEphemeral {child['id']} -> Screen 3")
            link(child['id'], s3, transition_type="slideIn", direction="top", duration=350, easing="bouncy")
        # Close / Batal -> Screen 1
        if child.get('text') == 'Batal':
            print(f"Wiring Batal {child['id']} -> Screen 1")
            link(child['id'], s1, transition_type="dissolve", duration=200)

    # 5. Screen 4: Detail Playlist
    s4_children = p.call('node.get', {'id': s4})['result'].get('children', [])
    for child in s4_children:
        # Back icon at x:20, y:28 -> Screen 5 or 1
        if child.get('x') == 20 and child.get('y') == 28:
            print(f"Wiring DetailPlaylist Back {child['id']} -> Screen 5")
            link(child['id'], s5, transition_type="slideOut", direction="right", duration=250, easing="quick")
        # FAB Add Track -> Screen 2
        if child.get('name') == 'FAB_AddTrack':
            print(f"Wiring FAB_AddTrack {child['id']} -> Screen 2")
            link(child['id'], s2, transition_type="slideIn", direction="top", duration=300, easing="quick")

    # 6. Screen 5: Playlist Hub
    s5_children = p.call('node.get', {'id': s5})['result'].get('children', [])
    for child in s5_children:
        # Playlist cards to Screen 4
        if child.get('y') in [128, 300] and child.get('width') == 156:
            print(f"Wiring PlaylistHub card {child['id']} -> Screen 4")
            link(child['id'], s4, transition_type="smartAnimate", duration=300, easing="bouncy")

    # Save to native .figcraft file
    p.call('file.saveAs', {'path': '/home/amir/projects/rust-music-android/rustvibe_material3_expressive.figcraft'})
    p.close()
    print("Prototype motion graph successfully wired in FigCraft!")

if __name__ == '__main__':
    wire_prototype_motion()
