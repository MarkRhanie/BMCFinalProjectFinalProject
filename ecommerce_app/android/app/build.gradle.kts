allprojects {
    repositories {
        google()
        mavenCentral()
        // Add these repositories for Flutter and common dependencies
        maven { url = uri("https://storage.googleapis.com/download.flutter.io") }
        maven { url = uri("https://www.jitpack.io") }
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
    
    // Add Java version compatibility
    afterEvaluate {
        // For Android projects
        project.extensions.findByType(com.android.build.gradle.BaseExtension::class.java)?.let { android ->
            android.compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
        
        // For Kotlin projects
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
}

subprojects {
    project.evaluationDependsOn(":app")
    
    // Add Android configuration for app and library modules
    plugins.withId("com.android.application") {
        configure<com.android.build.gradle.AppExtension> {
            compileSdk = 34
            defaultConfig {
                minSdk = 21
                targetSdk = 34
                versionCode = 1
                versionName = "1.0"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
    }
    
    plugins.withId("com.android.library") {
        configure<com.android.build.gradle.LibraryExtension> {
            compileSdk = 34
            defaultConfig {
                minSdk = 21
                targetSdk = 34
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

// Add dependency resolution strategy to fix version conflicts
configurations.all {
    resolutionStrategy {
        // Force specific versions if needed
        force(
            "androidx.core:core-ktx:1.12.0",
            "androidx.appcompat:appcompat:1.6.1",
            "com.google.android.material:material:1.10.0"
        )
        
        // Prefer project modules over external dependencies
        preferProjectModules()
        
        // Fail on version conflict
        failOnVersionConflict()
    }
}
