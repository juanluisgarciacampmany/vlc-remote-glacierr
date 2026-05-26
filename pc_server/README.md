# Servidor de Control Remoto para PC 🖥️📱

Este es el servidor en Python (Backend) para emparejar tu PC con el cliente móvil Android. Permite controlar VLC, explorar tus archivos de forma remota y realizar operaciones de sistema (apagar/reiniciar/suspender) junto con telemetría de rendimiento en tiempo real (CPU/RAM).

## Requerimientos

- Python 3.8 o superior.
- Sistema Operativo Windows (con soporte parcial para MacOS/Linux para pruebas).
- Conexión a la **misma red Wi-Fi** local para tu PC y tu dispositivo Android.
- VLC Media Player instalado (recomendado).

## Instalación de Dependencias

Ejecuta el siguiente comando en tu consola/terminal para instalar las dependencias requeridas:

```bash
pip install -r requirements.txt
```

*Nota: `pyautogui` y `psutil` se utilizan para automatización de teclado y telemetría de recursos respectivamente.*

## Cómo Iniciar el Servidor

Dirígete a la carpeta `pc_server` e inicia la aplicación con:

```bash
python server.py
```

Al iniciar, verás una ventana de consola con detalles similares a:

```text
============================================================
      SERVIDOR DE CONTROL REMOTO PC INICIADO
============================================================
 Sistema Operativo:  Windows 10
 IP Local Principal: 192.168.1.45
 Puerto de Escucha:  8000

   ***********************************
   *  PIN DE SEGURIDAD:  395217       *
   ***********************************

 Introduce esta IP y el PIN en tu aplicación de Android para conectarte.
============================================================
```

## Características Técnicas

1. **Explorador de Archivos**: Si no se selecciona ninguna ruta (por defecto en la conexión), listará automáticamente las unidades del sistema (ej. `C:\`, `D:\`). Al navegar por carpetas y pulsar en un archivo de video, se abrirá instantáneamente en VLC en pantalla completa.
2. **Control de VLC**: El servidor utiliza `pyautogui` para simular atajos de teclado estándar de VLC, lo que elimina la necesidad de realizar configuraciones de red complejas dentro de VLC. Soporta: Play/Pause, Subir/Bajar volumen, Búsqueda (+/- 10s) y Pantalla completa.
3. **Control del Sistema**:
   - Apagar (`shutdown /s /t 1`)
   - Reiniciar (`shutdown /r /t 1`)
   - Suspender (utiliza API de energía `powrprof.dll`)
   - Los comandos se ejecutan en un hilo secundario por separado para asegurar que la aplicación Android reciba la confirmación de envío con éxito antes de ejecutarse.
4. **Seguridad**: Todas las peticiones HTTP (salvo la de comprobación inicial / ping) exigen la cabecera `X-Auth-Token` que coincida exactamente con el PIN autogenerado al azar en el inicio. Esto evita que personas ajenas conectadas a tu misma red Wi-Fi operen tu ordenador.
