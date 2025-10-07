#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

#ifdef HAVE_OPENCV
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
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

        // Apply Canny to produce an edge map; this can later be uploaded to GL or returned
        cv::Mat edges;
        const double lowThresh = 50.0;
        const double highThresh = 150.0;
        cv::Canny(gray, edges, lowThresh, highThresh);

        // Optional: keep an average or simple checksum to prevent compiler optimizing away
        // and to aid in basic debugging without returning data yet.
        const cv::Scalar sumVal = cv::sum(edges);
        (void)sumVal;
    } else {
        __android_log_print(ANDROID_LOG_WARN, "EgdeApp", "NV21 buffer too small: %d < %d", (int)length, yPlaneSize);
    }
#else
    (void)length;
#endif
}