/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class hypermedia_video_OpenCV */

#ifndef _Included_hypermedia_video_OpenCV
#define _Included_hypermedia_video_OpenCV
#ifdef __cplusplus
extern "C" {
#endif
#undef hypermedia_video_OpenCV_SOURCE
#define hypermedia_video_OpenCV_SOURCE 0L
#undef hypermedia_video_OpenCV_BUFFER
#define hypermedia_video_OpenCV_BUFFER 1L
#undef hypermedia_video_OpenCV_MEMORY
#define hypermedia_video_OpenCV_MEMORY 2L
#undef hypermedia_video_OpenCV_RGB
#define hypermedia_video_OpenCV_RGB 1L
#undef hypermedia_video_OpenCV_GRAY
#define hypermedia_video_OpenCV_GRAY 12L
#undef hypermedia_video_OpenCV_THRESH_BINARY
#define hypermedia_video_OpenCV_THRESH_BINARY 0L
#undef hypermedia_video_OpenCV_THRESH_BINARY_INV
#define hypermedia_video_OpenCV_THRESH_BINARY_INV 1L
#undef hypermedia_video_OpenCV_THRESH_TRUNC
#define hypermedia_video_OpenCV_THRESH_TRUNC 2L
#undef hypermedia_video_OpenCV_THRESH_TOZERO
#define hypermedia_video_OpenCV_THRESH_TOZERO 3L
#undef hypermedia_video_OpenCV_THRESH_TOZERO_INV
#define hypermedia_video_OpenCV_THRESH_TOZERO_INV 4L
#undef hypermedia_video_OpenCV_THRESH_OTSU
#define hypermedia_video_OpenCV_THRESH_OTSU 8L
#undef hypermedia_video_OpenCV_FLIP_VERTICAL
#define hypermedia_video_OpenCV_FLIP_VERTICAL 0L
#undef hypermedia_video_OpenCV_FLIP_HORIZONTAL
#define hypermedia_video_OpenCV_FLIP_HORIZONTAL 1L
#undef hypermedia_video_OpenCV_FLIP_BOTH
#define hypermedia_video_OpenCV_FLIP_BOTH -1L
#undef hypermedia_video_OpenCV_INTER_NN
#define hypermedia_video_OpenCV_INTER_NN 0L
#undef hypermedia_video_OpenCV_INTER_LINEAR
#define hypermedia_video_OpenCV_INTER_LINEAR 1L
#undef hypermedia_video_OpenCV_INTER_CUBIC
#define hypermedia_video_OpenCV_INTER_CUBIC 2L
#undef hypermedia_video_OpenCV_INTER_AREA
#define hypermedia_video_OpenCV_INTER_AREA 3L
#undef hypermedia_video_OpenCV_MAX_VERTICES
#define hypermedia_video_OpenCV_MAX_VERTICES 1024L
#undef hypermedia_video_OpenCV_BLUR
#define hypermedia_video_OpenCV_BLUR 1L
#undef hypermedia_video_OpenCV_GAUSSIAN
#define hypermedia_video_OpenCV_GAUSSIAN 2L
#undef hypermedia_video_OpenCV_MEDIAN
#define hypermedia_video_OpenCV_MEDIAN 3L
#undef hypermedia_video_OpenCV_BILATERAL
#define hypermedia_video_OpenCV_BILATERAL 4L
#undef hypermedia_video_OpenCV_MOVIE_MILLISECONDS
#define hypermedia_video_OpenCV_MOVIE_MILLISECONDS 0L
#undef hypermedia_video_OpenCV_MOVIE_FRAMES
#define hypermedia_video_OpenCV_MOVIE_FRAMES 1L
#undef hypermedia_video_OpenCV_MOVIE_RATIO
#define hypermedia_video_OpenCV_MOVIE_RATIO 2L
#undef hypermedia_video_OpenCV_MOVIE_WIDTH
#define hypermedia_video_OpenCV_MOVIE_WIDTH 3L
#undef hypermedia_video_OpenCV_MOVIE_HEIGHT
#define hypermedia_video_OpenCV_MOVIE_HEIGHT 4L
#undef hypermedia_video_OpenCV_MOVIE_FPS
#define hypermedia_video_OpenCV_MOVIE_FPS 5L
#undef hypermedia_video_OpenCV_MOVIE_FRAME_COUNT
#define hypermedia_video_OpenCV_MOVIE_FRAME_COUNT 7L
#undef hypermedia_video_OpenCV_MOVIE_FORMAT
#define hypermedia_video_OpenCV_MOVIE_FORMAT 8L
#undef hypermedia_video_OpenCV_MOVIE_MODE
#define hypermedia_video_OpenCV_MOVIE_MODE 9L
#undef hypermedia_video_OpenCV_MOVIE_BRIGHTNESS
#define hypermedia_video_OpenCV_MOVIE_BRIGHTNESS 10L
#undef hypermedia_video_OpenCV_MOVIE_CONTRAST
#define hypermedia_video_OpenCV_MOVIE_CONTRAST 11L
#undef hypermedia_video_OpenCV_MOVIE_SATURATION
#define hypermedia_video_OpenCV_MOVIE_SATURATION 12L
#undef hypermedia_video_OpenCV_MOVIE_HUE
#define hypermedia_video_OpenCV_MOVIE_HUE 13L
#undef hypermedia_video_OpenCV_MOVIE_GAIN
#define hypermedia_video_OpenCV_MOVIE_GAIN 14L
#undef hypermedia_video_OpenCV_MOVIE_CONVERT_RGB
#define hypermedia_video_OpenCV_MOVIE_CONVERT_RGB 15L
#undef hypermedia_video_OpenCV_HAAR_DO_CANNY_PRUNING
#define hypermedia_video_OpenCV_HAAR_DO_CANNY_PRUNING 1L
#undef hypermedia_video_OpenCV_HAAR_SCALE_IMAGE
#define hypermedia_video_OpenCV_HAAR_SCALE_IMAGE 2L
#undef hypermedia_video_OpenCV_HAAR_FIND_BIGGEST_OBJECT
#define hypermedia_video_OpenCV_HAAR_FIND_BIGGEST_OBJECT 4L
#undef hypermedia_video_OpenCV_HAAR_DO_ROUGH_SEARCH
#define hypermedia_video_OpenCV_HAAR_DO_ROUGH_SEARCH 8L
/*
 * Class:     hypermedia_video_OpenCV
 * Method:    dispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_dispose
  (JNIEnv *, jobject);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    instantiate
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_instantiate
  (JNIEnv *, jobject);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    allocate
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_allocate
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    remember
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_remember
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    convert
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_convert
  (JNIEnv *, jobject, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    absDiff
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_absDiff
  (JNIEnv *, jobject);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    flip
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_flip
  (JNIEnv *, jobject, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    threshold
 * Signature: (FFI)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_threshold
  (JNIEnv *, jobject, jfloat, jfloat, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    brightnessContrast
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_brightnessContrast
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    invert
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_invert
  (JNIEnv *, jobject);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    blur
 * Signature: (IIIFF)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_blur
  (JNIEnv *, jobject, jint, jint, jint, jfloat, jfloat);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    interpolation
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_interpolation
  (JNIEnv *, jobject, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    copy
 * Signature: (Ljava/lang/String;IIIIIIII)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_copy__Ljava_lang_String_2IIIIIIII
  (JNIEnv *, jobject, jstring, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    copy
 * Signature: ([IIIIIIIIII)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_copy___3IIIIIIIIII
  (JNIEnv *, jobject, jintArray, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    copy
 * Signature: (Lhypermedia/video/OpenCV;II)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_copy__Lhypermedia_video_OpenCV_2II
  (JNIEnv *, jobject, jobject, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    restore
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_restore
  (JNIEnv *, jobject, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    pixels
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_hypermedia_video_OpenCV_pixels
  (JNIEnv *, jobject, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    pixels
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_hypermedia_video_OpenCV_RGBpixels
  (JNIEnv *, jobject, jint);


/*
 * Class:     hypermedia_video_OpenCV
 * Method:    loadImage
 * Signature: (Ljava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_loadImage
  (JNIEnv *, jobject, jstring, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    blobs
 * Signature: (IIIZI)[Lhypermedia/video/Blob;
 */
JNIEXPORT jobjectArray JNICALL Java_hypermedia_video_OpenCV_blobs
  (JNIEnv *, jobject, jint, jint, jint, jboolean, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    capture
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_capture
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    movie
 * Signature: (Ljava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_movie
  (JNIEnv *, jobject, jstring, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    read
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_read
  (JNIEnv *, jobject);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    property
 * Signature: (I)F
 */
JNIEXPORT jfloat JNICALL Java_hypermedia_video_OpenCV_property
  (JNIEnv *, jobject, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    jump
 * Signature: (FI)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_jump
  (JNIEnv *, jobject, jfloat, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    ROI
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_ROI
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    cascade
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_hypermedia_video_OpenCV_cascade
  (JNIEnv *, jobject, jstring);

/*
 * Class:     hypermedia_video_OpenCV
 * Method:    detect
 * Signature: (FIIII)[Ljava/awt/Rectangle;
 */
JNIEXPORT jobjectArray JNICALL Java_hypermedia_video_OpenCV_detect
  (JNIEnv *, jobject, jfloat, jint, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
