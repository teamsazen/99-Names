# Nothing in the app is reflected on, so R8 (full mode) may move and rename freely.
-allowaccessmodification
-repackageclasses
# Drop debug and verbose log calls from release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
-dontwarn org.jetbrains.annotations.**
