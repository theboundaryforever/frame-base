pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        maven ("https://maven.aliyun.com/repository/central")
        maven ("https://maven.aliyun.com/nexus/content/groups/public/")
        maven ("https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven ("https://maven.aliyun.com/nexus/content/repositories/google")
        maven ("https://maven.aliyun.com/nexus/content/repositories/gradle-plugin")
        maven ("https://mirrors.tencent.com/nexus/repository/maven-public/")

        maven ("https://jitpack.io")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        maven("https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven("https://maven.aliyun.com/nexus/content/repositories/google")
        maven("https://maven.aliyun.com/nexus/content/repositories/gradle-plugin")
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        flatDir {
            dirs("${rootDir}/app/libs")
        }

    }
}

rootProject.name = "yuehai"

rootProject.name = "yoppo-frame"
include(":app")
include("frame:coroutine")
include("frame:network")
include("frame:util")
include("frame:data")
include("frame:mvvm")
include("frame:media")
include("frame:base")
include("frame:sound")