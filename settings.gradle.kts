pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // নিচের লাইনটি নতুন যোগ করা হয়েছে PDF লাইব্রেরি ডাউনলোড করার জন্য
        maven { url = uri("https://jitpack.io") } 
    }
}

rootProject.name = "Allahor Zikir"
include(":app")
