import os
import sys
import subprocess
import socket
import secrets
import threading
import time
import platform
from typing import Optional, List
from fastapi import FastAPI, Header, HTTPException, Query, Depends, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import psutil
import pyautogui

# Generate a security PIN on startup
SECURITY_PIN = "".join(secrets.choice("0123456789") for _ in range(6))

app = FastAPI(
    title="PC Remote Control Server",
    description="Backend para el control remoto de PC (VLC, files y sistema)",
    version="1.0.0"
)

# Enable CORS for convenience
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Dependency to check authorization
def verify_pin(x_auth_token: Optional[str] = Header(None, alias="X-Auth-Token")):
    if not x_auth_token or x_auth_token != SECURITY_PIN:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="PIN de seguridad inválido o no proporcionado"
        )
    return x_auth_token

class ControlRequest(BaseModel):
    action: str  # play_pause, volume_up, volume_down, seek_forward, seek_backward, toggle_fullscreen

class SystemCommandRequest(BaseModel):
    command: str  # shutdown, restart, suspend

class FileOpenRequest(BaseModel):
    filepath: str

# Helper to find Windows logical drives
def get_windows_drives() -> List[str]:
    drives = []
    if platform.system() == "Windows":
        import string
        # Check standard drive letters A-Z
        for letter in string.ascii_uppercase:
            drive = f"{letter}:\\"
            if os.path.exists(drive):
                drives.append(drive)
    else:
        # Fallback for Linux/macOS testing
        drives.append("/")
    return drives

@app.get("/")
def get_index():
    return {
        "status": "online",
        "name": "PC Remote Control Server",
        "os": platform.system(),
        "node": platform.node(),
        "arch": platform.machine()
    }

@app.post("/auth")
def authenticate(token_req: dict):
    # Verification endpoint
    pin = token_req.get("pin", "")
    if pin == SECURITY_PIN:
        return {"authenticated": True, "token": SECURITY_PIN}
    raise HTTPException(
        status_code=401,
        detail="PIN incorrecto"
    )

@app.get("/system/telemetry", dependencies=[Depends(verify_pin)])
def get_telemetry():
    cpu_percent = psutil.cpu_percent(interval=0.1)
    ram = psutil.virtual_memory()
    disk = psutil.disk_usage('/')
    
    # Try to see if VLC is running
    vlc_running = False
    for proc in psutil.process_iter(['name']):
        if proc.info['name'] and 'vlc' in proc.info['name'].lower():
            vlc_running = True
            break
            
    return {
        "cpu_usage_percent": cpu_percent,
        "ram_used_gb": round(ram.used / (1024**3), 2),
        "ram_total_gb": round(ram.total / (1024**3), 2),
        "ram_usage_percent": ram.percent,
        "disk_usage_percent": disk.percent,
        "disk_free_gb": round(disk.free / (1024**3), 2),
        "vlc_running": vlc_running
    }

@app.get("/explorer/browse", dependencies=[Depends(verify_pin)])
def browse_files(path: str = Query("")):
    # If path is empty, return logical drives in Windows
    if not path or path.strip() == "":
        drives = get_windows_drives()
        items = []
        for d in drives:
            items.append({
                "name": d,
                "path": d,
                "is_dir": True,
                "size_bytes": 0,
                "extension": ""
            })
        return {"current_path": "", "items": items}

    # Clean and check directory existence
    if not os.path.exists(path):
        raise HTTPException(status_code=404, detail="Directorio no encontrado")
    
    if not os.path.isdir(path):
        raise HTTPException(status_code=400, detail="La ruta proporcionada no es un directorio")
    
    try:
        items = []
        # Support parent path helper
        parent_path = os.path.dirname(os.path.normpath(path))
        if parent_path == path: # we are at root
            parent_path = ""
            
        for entry in os.scandir(path):
            try:
                is_dir = entry.is_dir()
                stat = entry.stat()
                size = stat.st_size if not is_dir else 0
                ext = "" if is_dir else os.path.splitext(entry.name)[1].lower()
                
                items.append({
                    "name": entry.name,
                    "path": entry.path,
                    "is_dir": is_dir,
                    "size_bytes": size,
                    "extension": ext
                })
            except Exception:
                # Skip files we don't have permissions to read
                continue
                
        # Sort items: directory first, then alphabetically
        items.sort(key=lambda x: (not x["is_dir"], x["name"].lower()))
        
        return {
            "current_path": path,
            "parent_path": parent_path,
            "items": items
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# VLC Control and file opening
@app.post("/vlc/open", dependencies=[Depends(verify_pin)])
def open_file_vlc(req: FileOpenRequest):
    filepath = req.filepath
    if not os.path.exists(filepath):
        raise HTTPException(status_code=404, detail="Archivo no encontrado")
        
    try:
        # Standard VLC paths in Windows
        vlc_executable = None
        standard_paths = [
            r"C:\Program Files\VideoLAN\VLC\vlc.exe",
            r"C:\Program Files (x86)\VideoLAN\VLC\vlc.exe"
        ]
        
        for p in standard_paths:
            if os.path.exists(p):
                vlc_executable = p
                break
                
        if vlc_executable:
            # Open the file directly with VLC in fullscreen
            subprocess.Popen([vlc_executable, "--fullscreen", filepath])
            return {"status": "success", "message": f"Abierto con VLC: {os.path.basename(filepath)}"}
        else:
            # Fallback to default system player if VLC is not found in standard paths
            if platform.system() == "Windows":
                os.startfile(filepath)
            else:
                subprocess.Popen(["xdg-open", filepath])
            return {"status": "fallback", "message": f"VLC no encontrado en rutas estándar. Abriendo con reproductor predeterminado."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"No se pudo abrir el archivo: {str(e)}")

@app.post("/vlc/control", dependencies=[Depends(verify_pin)])
def control_vlc(req: ControlRequest):
    action = req.action.lower()
    
    # We will simulate VLC shortcuts. PyAutoGUI is great for this!
    # Common VLC Windows Shortcuts:
    # Space: Play / Pause
    # Ctrl + Up: Volume Up
    # Ctrl + Down: Volume Down
    # Left / Right: Seek backward / seek forward
    # f: Toggle full screen
    
    try:
        if action == "play_pause":
            pyautogui.press('space')
        elif action == "volume_up":
            # VLC uses Ctrl + Up
            pyautogui.hotkey('ctrl', 'up')
        elif action == "volume_down":
            # VLC uses Ctrl + Down
            pyautogui.hotkey('ctrl', 'down')
        elif action == "seek_forward":
            # VLC uses Shift + Right or Ctrl + Right or just Right
            pyautogui.press('right')
        elif action == "seek_backward":
            # VLC uses Shift + Left or Ctrl + Left or just Left
            pyautogui.press('left')
        elif action == "toggle_fullscreen":
            # VLC uses f
            pyautogui.press('f')
        else:
            raise HTTPException(status_code=400, detail="Acción no soportada")
            
        return {"status": "success", "action": action}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error ejecutando acción de control: {str(e)}")

# System Actions
def delayed_shutdown(command: str):
    time.sleep(2)  # Give time for the HTTP response to be delivered to the client
    if command == "shutdown":
        if platform.system() == "Windows":
            os.system("shutdown /s /t 1")
        elif platform.system() == "Linux":
            os.system("shutdown -h now")
        elif platform.system() == "Darwin":
            os.system("osascript -e 'tell app \"System Events\" to shut down'")
    elif command == "restart":
        if platform.system() == "Windows":
            os.system("shutdown /r /t 1")
        elif platform.system() == "Linux":
            os.system("shutdown -r now")
        elif platform.system() == "Darwin":
            os.system("osascript -e 'tell app \"System Events\" to restart'")
    elif command == "suspend":
        if platform.system() == "Windows":
            # Use rundll32 for suspending Windows
            os.system("rundll32.exe powrprof.dll,SetSuspendState 0,1,0")
        elif platform.system() == "Linux":
            os.system("systemctl suspend")
        elif platform.system() == "Darwin":
            os.system("osascript -e 'tell app \"System Events\" to sleep'")

@app.post("/system/command", dependencies=[Depends(verify_pin)])
def execute_system_command(req: SystemCommandRequest):
    cmd = req.command.lower()
    if cmd not in ["shutdown", "restart", "suspend"]:
        raise HTTPException(status_code=400, detail="Comando del sistema no soportado")
        
    # Execute in a separate thread to allow FastAPI to return HTTP state before freezing/sleeping
    t = threading.Thread(target=delayed_shutdown, args=(cmd,))
    t.start()
    
    return {"status": "success", "message": f"Comando '{cmd}' programado para ejecutarse en 2 segundos."}

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # doesn't even have to be reachable, this is just to select local interface
        s.connect(('10.255.255.255', 1))
        IP = s.getsockname()[0]
    except Exception:
        IP = '127.0.0.1'
    finally:
        s.close()
    return IP

if __name__ == "__main__":
    import uvicorn
    local_ip = get_local_ip()
    
    print("=" * 60)
    print("      SERVIDOR DE CONTROL REMOTO PC INICIADO")
    print("=" * 60)
    print(f" Sistema Operativo:  {platform.system()} {platform.release()}")
    print(f" IP Local Principal: {local_ip}")
    print(f" Puerto de Escucha:  8000")
    print("\n   " + "*" * 35)
    print(f"   *  PIN DE SEGURIDAD:  {SECURITY_PIN}  *")
    print("   " + "*" * 35)
    print("\n Introduce esta IP y el PIN en tu aplicación de Android para conectarte.")
    print("=" * 60)
    
    uvicorn.run(app, host="0.0.0.0", port=8000)
