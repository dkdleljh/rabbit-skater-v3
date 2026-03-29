# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep AppLovin SDK classes
-keep class com.applovin.** { *; }
-keep class com.applovin.sdk.** { *; }

# Keep Google Play Services Ads classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.games.** { *; }

# Keep application model classes
-keep class com.devbyzenith.rabbitskater.** { *; }
