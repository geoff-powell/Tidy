import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose)
  alias(libs.plugins.spotless)
}

kotlin {
  js(IR) {
    browser {
      useCommonJs()
      binaries.executable()
    }
  }

  androidTarget {
    compilations.all {
      kotlinOptions {
        jvmTarget = "11"
      }
    }
  }

  jvm("desktop") {
    compilations.all {
      kotlinOptions.jvmTarget = "17"
    }
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    val desktopMain by getting

    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
    }
    commonMain.dependencies {
      implementation(libs.koin.compose)

      @OptIn(ExperimentalComposeLibrary::class)
      implementation(compose.components.resources)
      implementation(projects.common)
    }
    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
    }
    iosMain.dependencies {
    }
    jsMain.dependencies {
      api(compose.html.core)
      implementation(libs.sqldelight.driver.web)
      implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
      implementation(npm("sql.js", "1.8.0"))
      implementation(devNpm("copy-webpack-plugin", "9.1.0"))
    }
  }
}

android {
  namespace = "com.greenmiststudios.tidy"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  sourceSets["main"].res.srcDirs("src/androidMain/res")
  sourceSets["main"].resources.srcDirs("src/commonMain/resources")

  defaultConfig {
    applicationId = "com.greenmiststudios.tidy"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = project.properties["tidy.version_code"]?.toString()?.toInt() ?: 1
    versionName = project.properties["tidy.version_name"]?.toString()
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  dependencies {
    debugImplementation(libs.compose.ui.tooling)
  }
}
dependencies {
  implementation(project(mapOf("path" to ":common")))
}

compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.greenmiststudios.tidy"
      packageVersion = project.properties["tidy.version_name"]?.toString()
    }
  }
}

compose.experimental {
  web.application {}
}

afterEvaluate {
  rootProject.extensions.configure<NodeJsRootExtension> {
    versions.webpackDevServer.version = "4.0.0"
    versions.webpackCli.version = "4.10.0"
  }
}