# EgdeApp

Android + OpenCV (C++) + OpenGL ES + Web viewer RnD challenge implementation.

## Features
- Camera2 preview on TextureView
- ImageReader pipeline → JNI C++ `processFrame`
- OpenCV Canny edges (optional, enabled when OpenCV is provided)
- OpenGL ES 2.0 renderer to display processed frames (RGBA texture)
- Toggle between raw preview and processed output
- FPS counter; Save Sample (PNG) to Pictures/EgdeApp
- Web viewer (TypeScript) showing a static processed frame and stats

## Architecture
- Java/Kotlin (Android): Camera2 + TextureView, ImageReader, UI, GLSurfaceView
- JNI (C++): Receives NV21 bytes, uses OpenCV to produce edges, exposes RGBA via JNI
- OpenGL ES: `GlRenderer` uploads RGBA bytes as texture and renders fullscreen quad
- Web (TypeScript): minimal page + stats, built with `tsc`

Frame flow: Camera2 (YUV_420_888) → NV21 bytes → JNI `processFrame` → OpenCV Canny → RGBA cache → GL `getLastProcessedRgba()` → texture → screen

## Android Setup
1. Requirements: Android Studio (with NDK), Android SDK 24+.
2. OpenCV (optional):
   - Install OpenCV Android SDK or AAR.
   - Set `OpenCV_DIR` for CMake to find OpenCV. Example in `local.properties`:

     ```
     opencv.dir=/absolute/path/to/OpenCV-android-sdk/sdk/native/jni
     ```

   - Or export `OpenCV_DIR` in your environment before build.
3. Build/Run: connect a device and run from Android Studio, or:

   ```bash
   ./gradlew :app:assembleDebug
   ```

Notes:
- If OpenCV is not present, app runs with no-op processing and GL may show blank processed view.
- Grant camera permission at first launch.

## Web Viewer
```bash
cd web
npm install
npm run build
npx http-server -c-1 -p 5173
```
Open `http://localhost:5173` in a browser.

To show a sample processed frame, copy a PNG from device `Pictures/EgdeApp` into `web/` as `sample_processed.jpg` (or update `index.html`).

## Screenshots / GIF
- Add screenshots of raw vs processed and the web viewer.

## License
MIT


