import socket
import json

class FigCraftClient:
    def __init__(self, host='127.0.0.1', port=7979):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((host, port))
        self.reader = self.sock.makefile('r', encoding='utf-8')
        self._id = 0

    def execute(self, command, params=None):
        self._id += 1
        payload = {
            "id": self._id,
            "method": "engine.execute",
            "params": {
                "command": command,
                "params": params or {}
            }
        }
        raw = json.dumps(payload) + '\n'
        self.sock.sendall(raw.encode('utf-8'))
        line = self.reader.readline()
        if not line:
            return None
        return json.loads(line)

    def close(self):
        self.sock.close()

if __name__ == '__main__':
    client = FigCraftClient()
    print("New file:", client.execute("file.new"))
    client.close()
