rootProject.name = "tidy"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
val IS_CI = System.getenv("CI")

pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
  }
}

plugins {
  id("com.gradle.enterprise") version("3.16.2")
}

gradleEnterprise {
  projectId = "com.greenmiststudios.tidy"

  // buildCache {
  //   remote {
  //     isEnabled = !IS_CI.isNullOrEmpty()
  //     isPush = System.getProperty("\$GITHUB_REF_NAME") == "main"
  //   }
  //   local {
  //     directory = file("$rootDir/.gradle/build-cache")
  //   }
  // }

  buildScan {
    if (!IS_CI.isNullOrEmpty()) {
      publishAlways()
      tag("CI")
    } else {
      publishOnFailure()
      tag("Local")
    }

    isUploadInBackground = IS_CI.isNullOrEmpty()
    termsOfServiceAgree = "yes"
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
  }
}

include(":common")
include(":composeApp")