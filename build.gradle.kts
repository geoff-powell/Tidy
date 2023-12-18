import org.jetbrains.compose.ComposeExtension

group = "com.greenmiststudios"
version = "1.0-SNAPSHOT"

allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

plugins {
  kotlin("jvm") version libs.versions.kotlin.get() apply false
  kotlin("multiplatform") version libs.versions.kotlin.get() apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.sqldelight) apply false
}
