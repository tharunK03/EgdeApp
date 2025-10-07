#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

#ifdef HAVE_OPENCV
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
static std::vector<unsigned char> g_lastRgba;
static int g_lastWidth = 0;
static int g_lastHeight = 0;
#endif

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myapplication_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_myapplication_MainActivity_processFrame(
        JNIEnv* env,
        jobject /* this */, jbyteArray yuvNv21, jint width, jint height) {
    if (yuvNv21 == nullptr || width <= 0 || height <= 0) return;

    jsize length = env->GetArrayLength(yuvNv21);
    std::vector<jbyte> buffer(length);
    env->GetByteArrayRegion(yuvNv21, 0, length, buffer.data());

#ifdef HAVE_OPENCV
    // Interpret NV21: Y plane first width*height bytes; interleaved VU after.
    const int yPlaneSize = width * height;
    if (yPlaneSize <= length) {
        // Grayscale is simply the Y plane
        cv::Mat gray(height, width, CV_8UC1, reinterpret_cast<unsigned char*>(buffer.data()));

        // Apply Canny to produce an edge map; stash as RGBA for GL
        cv::Mat edges;
        const double lowThresh = 50.0;
        const double highThresh = 150.0;
        cv::Canny(gray, edges, lowThresh, highThresh);

        cv::Mat lastRgba;
        cv::cvtColor(edges, lastRgba, cv::COLOR_GRAY2RGBA);
        g_lastWidth = lastRgba.cols;
        g_lastHeight = lastRgba.rows;
        g_lastRgba.assign(lastRgba.datastart, lastRgba.dataend);
    } else {
        __android_log_print(ANDROID_LOG_WARN, "EgdeApp", "NV21 buffer too small: %d < %d", (int)length, yPlaneSize);
    }
#else
    (void)length;
#endif
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_myapplication_MainActivity_getLastProcessedRgba(
        JNIEnv* env,
        jobject /* this */) {
#ifdef HAVE_OPENCV
    if (g_lastRgba.empty() || g_lastWidth <= 0 || g_lastHeight <= 0) return nullptr;
    jbyteArray out = env->NewByteArray(static_cast<jsize>(g_lastRgba.size()));
    if (!out) return nullptr;
    env->SetByteArrayRegion(out, 0, static_cast<jsize>(g_lastRgba.size()), reinterpret_cast<const jbyte*>(g_lastRgba.data()));
    return out;
#else
    return nullptr;
#endif
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_getLastProcessedWidth(
        JNIEnv* /*env*/,
        jobject /* this */) {
#ifdef HAVE_OPENCV
    return g_lastWidth;
#else
    return 0;
#endif
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_myapplication_MainActivity_getLastProcessedHeight(
        JNIEnv* /*env*/,
        jobject /* this */) {
#ifdef HAVE_OPENCV
    return g_lastHeight;
#else
    return 0;
#endif
}