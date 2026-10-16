# VSOFT release rules
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep class com.vsoft.app.data.model.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn org.jetbrains.annotations.**
