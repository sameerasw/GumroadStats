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

# General attributes - Preserving signatures and annotations is critical
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations, SourceFile, LineNumberTable

# Gson rules
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class com.google.gson.stream.** { *; }
-keep @interface com.google.gson.annotations.** { *; }

# Retrofit 2 rules
-keep class retrofit2.** { *; }
-keep @interface retrofit2.http.** { *; }
-keep interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.HttpException { *; }
-keep class retrofit2.Response { *; }

# OkHttp 3 & Okio rules
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Kotlin-specific rules for reflection and suspend functions
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.coroutines.jvm.internal.ContinuationImpl { *; }
-keepclassmembers class ** {
    @kotlin.jvm.JvmField <fields>;
}

# Project Models & API - keep everything to avoid reflection failure
-keep class com.sameerasw.gumroadstats.data.model.** { *; }
-keep class com.sameerasw.gumroadstats.ui.model.** { *; }
-keep interface com.sameerasw.gumroadstats.data.api.GumroadApiService { *; }
-keep class com.sameerasw.gumroadstats.data.api.RetrofitClient { *; }
-keep class com.sameerasw.gumroadstats.data.repository.** { *; }
-keep class com.sameerasw.gumroadstats.viewmodel.** { *; }