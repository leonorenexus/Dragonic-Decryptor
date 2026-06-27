-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.dragonic.decryptor.domain.model.** { *; }
-keep class com.dragonic.decryptor.data.db.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
