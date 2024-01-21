# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile



#http://stackoverflow.com/questions/35321742/android-proguard-most-aggressive-optimizations
-optimizationpasses 2
-allowaccessmodification
-repackageclasses ''
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

#-optimizations code/simplification/arithmetic
#-optimizations code/simplification/cast
#-optimizations field/*
#-optimizations class/merging/*


#jogl
-dontwarn jogamp.opengl.**
-dontwarn com.jogamp.opengl.**
-dontwarn com.jogamp.common.util.awt.**
-dontwarn com.jogamp.nativewindow.**



#######################################required to deploy JOGL on play store!
#gluegen-rt-android.jar
-keep class jogamp.common.os.android.AndroidUtilsImpl { *; }

#joal-android.jar
-keep class com.jogamp.openal.** { *; }
-keep class jogamp.openal.** { *; }

#jogl-all-android.jar
-keep class com.jogamp.nativewindow.egl.EGLGraphicsDevice { *; }
-keep class com.jogamp.opengl.egl.** { *; }


-keep class jogamp.graph.font.typecast.TypecastFontConstructor { *; }
-keep class jogamp.graph.curve.opengl.shader.** { *; }

-keep class jogamp.newt.driver.** { *; }
-keep class jogamp.opengl.** { *; }