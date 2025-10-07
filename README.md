<div align="center">

# EgdeApp

Android + OpenCV (C++) + OpenGL ES + Web viewer — RnD Intern Challenge

</div>

## Overview
EgdeApp is a minimal end‑to‑end pipeline:
- Android Camera2 preview → frames to native C++ via JNI → OpenCV processing (Canny/Grayscale) → OpenGL ES texture rendering → optional TypeScript web viewer for a sample frame.

Repository structure:
- `app/` — Android app (Java) + NDK C++ (`src/main/cpp`)
- `jni/` — placeholder for native code (kept for guideline compatibility)
- `gl/` — placeholder for GL classes (renderer lives in `app/`)
- `web/` — TypeScript web viewer (tsc build)
- `docs/` — screenshots/samples for README (add your captured images here)

## Implemented Features
- Camera2 preview on a `TextureView`
- `ImageReader` captures YUV_420_888; converted to NV21 and sent through JNI
- Native C++ `processFrame(byte[], w, h)` uses OpenCV (if available)
  - Y plane → grayscale
  - Optional Canny edge detection (thresholds tuned for performance)
- OpenGL ES 2.0 renderer (`GLSurfaceView` + `GlRenderer`) displays processed RGBA frames
- UI: toggle Raw/Processed, FPS counter, Save Sample (PNG to `Pictures/EgdeApp`)
- Web viewer (`web/`): static sample image + overlayed stats, built with TypeScript

## Architecture
Data flow:
1) Camera2 (YUV_420_888) → 2) NV21 bytes → 3) JNI `processFrame` → 4) OpenCV (Grayscale/Canny) → 5) RGBA cache in native → 6) Java pulls RGBA via `getLastProcessedRgba()` → 7) OpenGL uploads to texture → 8) Display.

Threads:
- Camera2 runs on Camera service threads
- `ImageReader` callback runs on a `HandlerThread` (background)
- GL rendering runs on the GL thread managed by `GLSurfaceView`

Performance tweaks (enabled):
- Preview scaled to 960×540 (try 640×480 for lower‑end devices)
- Process every 2nd frame (configurable)
- Reusable NV21 buffer to avoid allocations
- Higher Canny thresholds to reduce work/noise

## Android Setup
Prereqs
- Android Studio (latest) with Android SDK 24+ and NDK installed
- A physical Android device recommended (emulators often lack full camera support)

OpenCV for Android (optional but recommended)
1) Download the Android SDK: `OpenCV-android-sdk`
2) Set path in `local.properties` (already supported):
   ```
   opencv.dir=/absolute/path/to/OpenCV-android-sdk/sdk/native/jni
   ```
   The project also sets a fallback in `app/src/main/cpp/CMakeLists.txt` for local development.

Build & Install
```bash
./gradlew :app:assembleDebug     # build
./gradlew :app:installDebug      # install to connected device
adb shell am start -n com.example.myapplication/.MainActivity
```
On first launch, grant Camera permission. Tap “Processed” to switch the view. Tap “Save Sample” to export a PNG to `Pictures/EgdeApp`.

## Web Viewer
```bash
cd web
npm install
npm run build
npx http-server -c-1 -p 5173
```
Open `http://localhost:5173`. Place a sample processed image at `web/sample_processed.jpg` (copy from your device’s `Pictures/EgdeApp`) to visualize in the page.

## Sample Output
Add your captured sample(s) to `docs/` and they will appear below. For example, after tapping “Save Sample”, copy the PNG from your phone to `docs/sample_edges.png` and commit it.

![Processed Canny edges](docs/sample_edges.png)

If the image above doesn’t render on GitHub, ensure the file exists at `docs/sample_edges.png` in the repository.

## Troubleshooting
- Processed view is blank: ensure OpenCV Android SDK is present and `opencv.dir` is set; rebuild.
- Low FPS: reduce preview to 640×480; increase frame skip; confirm device is not in power‑save mode.
- ADB install blocked: enable “Install via USB”/USB debugging in Developer Options; remove work profile/MDM.

## Roadmap / Optional Enhancements
- GPU path: sample external OES texture and do Sobel/Canny in fragment shader
- Mock HTTP/WebSocket server to stream frames and stats to the web viewer
- Add unit/instrumentation tests; more robust lifecycle handling

## License
MIT


