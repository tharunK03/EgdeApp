#include <jni.h>
#include <string>
#include <vector>

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
    // NV21 to grayscale (Y plane)
    const int yPlaneSize = width * height;
    if (yPlaneSize <= length) {
        cv::Mat yMat(height, width, CV_8UC1, buffer.data());
        cv::Mat edges;
        cv::Canny(yMat, edges, 50, 150);
        // For now, we don't return the processed data; rendering will be added later.
    }
#endif
}