# Rabbit Skater v3 - ProGuard Rules
# 난독화 및 최적화 설정

# ==================== Cocos2d-x ====================
-keep class org.cocos2dx.** { *; }
-keep class com.cocos.** { *; }

# ==================== AndroidX ====================
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# ==================== Google Mobile Ads ====================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }

# ==================== AppLovin ====================
-keep class com.applovin.** { *; }
-keep class com.applovin.mediation.** { *; }

# ==================== Firebase ====================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ==================== Buildbox SDK ====================
-keep class com.buildbox.** { *; }

# ==================== Vungle ====================
-keep class com.vungle.** { *; }

# ==================== In-App Billing ====================
-keep class com.secrethq.store.** { *; }
-keep class com.android.vending.billing.** { *; }

# ==================== Retain ====================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses

# ==================== Keep Custom Classes ====================
-keep class com.devbyzenith.rabbitskater.** { *; }

# ==================== Obfuscation ====================
-obfuscationfilterdict.txt

# ==================== Optimization ====================
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# ==================== Debug ====================
# -dontobfuscate  # For debugging, disable obfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
