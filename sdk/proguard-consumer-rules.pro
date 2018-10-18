# Keep meta-info (used by libs that use reflection: Jackson, Gson, Retrofit)
-keepattributes *Annotation*,Signature

# Jackson references JDK-specific code
-dontwarn com.fasterxml.jackson.databind.ext.DOMSerializer
-dontwarn com.fasterxml.jackson.databind.util.TokenBuffer$Parser

# Required for proper work of Jackson-annotations
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames interface com.fasterxml.jackson.** { *; }
-keepclassmembers enum org.codehaus.jackson.annotate.** { *; }

# Firebase use reflections
-keep class com.firebase.client.android.AndroidPlatform {
  <init>(...);
}

-keep class * {
    public private *;
}