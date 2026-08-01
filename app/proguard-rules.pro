-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

-keep class com.aurora.gplayapi.** { *; }
-keep class com.google.protobuf.** { *; }

-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.projectdreams.app.**$$serializer { *; }
-keepclassmembers class com.projectdreams.app.** {
    *** Companion;
    *** Companion$*;
}
-keepclasseswithmembers class com.projectdreams.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
