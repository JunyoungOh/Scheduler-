# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Nearby Connections classes
-keep class com.google.android.gms.nearby.** { *; }

# Keep Room entities
-keep class com.cutener.raising.data.model.** { *; }
