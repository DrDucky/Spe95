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

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**


-keepclassmembers class com.pomplarg.spe95.agent.data** {
  *;
}

-keepclassmembers class com.pomplarg.spe95.speoperations.data** {
  *;
}

-keepclassmembers class com.pomplarg.spe95.statistiques.data** {
  *;
}

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class com.google.firebase.** {
    *;
}
-keep class com.google.android.gms.** {
    *;
}
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

-keep public class com.github.mikephil.charting.animation.* {
    public protected *;

}