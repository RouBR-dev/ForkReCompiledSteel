import java.util.Properties

plugins {
    id("com.android.application")
}

val repoRoot: File = rootProject.projectDir

// ---- Caminhos externos (local.properties ou -P na linha de comando) --------
val localProps = Properties().apply {
    val f = File(repoRoot, "local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun cfg(key: String, default: String): String =
    (findProperty(key) as String?) ?: localProps.getProperty(key) ?: default

val home: String = System.getProperty("user.home")
val rexAndroidPrefix = File(cfg("rex.android.prefix", "$home/android/rexglue-android-arm64"))
val rexHostRexglue = cfg("rex.host.rexglue", "$home/rexglue/linux-amd64/bin/rexglue")
val sdlJavaDir = File(
    cfg("rex.sdl.java", "$home/android/rexglue-sdk/thirdparty/sdl3/android-project/app/src/main/java")
)

// ---- Versão: lida de src/realsteel_version.h --------------------------------
val versionHeaderText = File(repoRoot, "src/realsteel_version.h").readText()
val rsVersion: String = Regex("""kVersion\s*=\s*"([^"]+)"""")
    .find(versionHeaderText)!!.groupValues[1]
val versionMatch = Regex("""^(\d+)\.(\d+)\.(\d+)(?:\s+.*)?$""").matchEntire(rsVersion)
    ?: throw GradleException("Invalid version in realsteel_version.h: $rsVersion")
val calculatedVersionCode = versionMatch.groupValues[1].toInt() * 10000 +
    versionMatch.groupValues[2].toInt() * 100 + versionMatch.groupValues[3].toInt()
val versionNum = Regex("""kAndroidVersionCode\s*=\s*(\d+)""")
    .find(versionHeaderText)?.groupValues?.get(1)?.toInt() ?: calculatedVersionCode

// BuildConfigVersion.java (build_apk.sh gerava isto na mão)
val genJavaDir = layout.buildDirectory.dir("generated/rsversion/java").get().asFile
run {
    val f = File(genJavaDir, "com/recompiledsteel/app/BuildConfigVersion.java")
    f.parentFile.mkdirs()
    f.writeText(
        "package com.recompiledsteel.app;\n" +
        "final class BuildConfigVersion { static final String NAME = \"$rsVersion\"; " +
        "private BuildConfigVersion() {} }\n"
    )
}

android {
    namespace = "com.recompiledsteel.app"
    compileSdk = 35
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "com.recompiledsteel.app"
        minSdk = 30
        targetSdk = 35
        versionCode = versionNum
        versionName = rsVersion

        ndk { abiFilters += "arm64-v8a" }

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-ffile-prefix-map=$repoRoot=.")
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PLATFORM=android-30",
                    "-DCMAKE_PREFIX_PATH=${rexAndroidPrefix.absolutePath}",
                    "-DCMAKE_FIND_ROOT_PATH=${rexAndroidPrefix.absolutePath}",
                    "-DRS_HOST_REXGLUE=$rexHostRexglue",
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = File(repoRoot, "CMakeLists.txt")
            version = "3.25.0+"
        }
    }

    sourceSets["main"].apply {
        manifest.srcFile(File(repoRoot, "android/AndroidManifest.xml"))
        res.srcDir(File(repoRoot, "android/res"))
        java.srcDirs(File(repoRoot, "android/java"), genJavaDir, sdlJavaDir)
        // librexruntime.so (SDL3 + runtime do ReXGlue) vem do SDK Android.
        jniLibs.srcDir(File(rexAndroidPrefix, "lib-apk"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isJniDebuggable = false
            // Chave de debug: serve para instalar por adb / sideload.
            signingConfig = signingConfigs.getByName("debug")
            ndk { debugSymbolLevel = "NONE" }
        }
    }

    packaging {
        jniLibs {
            // O jogo e o SDL fazem dlopen de bibliotecas pelo caminho em
            // nativeLibraryDir; elas precisam estar extraídas no disco.
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint { abortOnError = false }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
}

// ---- Copia librexruntime.so (e só ela) para um jniLibs limpo ---------------
val stageRexRuntime by tasks.registering(Copy::class) {
    val src = File(rexAndroidPrefix, "lib/librexruntime.so")
    from(src)
    into(File(rexAndroidPrefix, "lib-apk/arm64-v8a"))
    doFirst {
        if (!src.exists()) {
            throw GradleException(
                "librexruntime.so não encontrada em ${src.absolutePath}.\n" +
                "Rode android/build_sdk.sh primeiro (veja COMO_COMPILAR_ANDROID.md) ou ajuste " +
                "rex.android.prefix em local.properties."
            )
        }
    }
}

val checkInputs by tasks.registering {
    doLast {
        val missing = mutableListOf<String>()
        if (!File(repoRoot, "work/game/default.xex").exists())
            missing += "work/game/default.xex (seus arquivos do jogo - passo 3 do guia)"
        if (!File(rexAndroidPrefix, "lib/cmake/rexglue/rexglueConfig.cmake").exists())
            missing += "SDK ReXGlue Android em ${rexAndroidPrefix} (android/build_sdk.sh)"
        if (!File(sdlJavaDir, "org/libsdl/app").exists())
            missing += "fontes Java do SDL em $sdlJavaDir (criados pelo build_sdk.sh)"
        if (!File(repoRoot, "generated/shader_cache.cpp").exists())
            missing += "generated/shader_cache.cpp (tools/shaders/setup_xenosrecomp.py)"
        if (missing.isNotEmpty())
            throw GradleException("Faltam pré-requisitos:\n - " + missing.joinToString("\n - "))
    }
}

tasks.named("preBuild") { dependsOn(checkInputs, stageRexRuntime) }
