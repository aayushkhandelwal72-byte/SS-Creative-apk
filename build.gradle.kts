// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

// Automatically create debug keystore if absent on CI environments (e.g. GitHub Actions)
val debugKeystore = file("${rootDir}/debug.keystore")
if (!debugKeystore.exists()) {
  try {
    ProcessBuilder(
      "keytool", "-genkey", "-v",
      "-keystore", debugKeystore.absolutePath,
      "-storepass", "android",
      "-alias", "androiddebugkey",
      "-keypass", "android",
      "-keyalg", "RSA",
      "-keysize", "2048",
      "-validity", "10000",
      "-dname", "CN=Android Debug,O=Android,C=US"
    ).redirectErrorStream(true).start().waitFor()
  } catch (e: Exception) {
    logger.warn("Could not generate debug.keystore: ${e.message}")
  }
}
