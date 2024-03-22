plugins {
  kotlin("multiplatform")
  id("com.android.library")
  alias(libs.plugins.compose)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.spotless)
}

group = "com.greenmiststudios.tidy"
version = "${project.properties["tidy.version_name"]?.toString()}-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {

  androidTarget {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  js(IR) {
    browser()
  }

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(compose.runtime)
        api(compose.ui)
        api(compose.foundation)
        api(compose.materialIconsExtended)
        api(compose.material)
        api(compose.material3)
        api(libs.voyager.navigator)
        api(libs.voyager.tab.navigator)
        api(libs.voyager.bottom.sheet.navigator)
        api(libs.koin.core)
        api(libs.koin.compose)
        api(libs.uuid)
        api(libs.sqldelight.coroutines.extensions)

        implementation(libs.molecule.runtime)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }

    val androidMain by getting {
      dependencies {
        api(libs.androidx.appcompat)
        api(libs.androidx.core)
        api(libs.kotlinx.coroutines.android)
        api(libs.koin.android)
        api(libs.koin.androidx.compose)

        implementation(libs.sqldelight.driver.android)
      }
    }

    nativeMain.dependencies {
      implementation(libs.sqldelight.driver.native)
    }

    val jvmMain by getting {
      dependencies {
        api(compose.preview)
        implementation(libs.sqldelight.driver.jvm)
      }
    }

    val jsMain by getting {
      dependencies {
        api(compose.html.core)
        implementation(libs.sqldelight.driver.web)
        implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
        implementation(npm("sql.js", "1.8.0"))
        implementation(devNpm("copy-webpack-plugin", "9.1.0"))
      }
    }

    iosMain.dependencies {
      // Fixes sqldelight 2.0.1 issue with ios compilation
      // https://github.com/cashapp/sqldelight/issues/4888
      implementation("co.touchlab:stately-common:2.0.7")
    }

    // val wasmJsMain by getting {
    //     dependencies {
    //         implementation(libs.ktor.core.wasm)
    //     }
    // }
  }

  explicitApi()
}

android {
  namespace = "com.greenmiststudios.tidy.common"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

sqldelight {
  databases {
    create("Database") {
      packageName.set("com.greenmiststudios.tidy")
    }
    linkSqlite = true
  }
}