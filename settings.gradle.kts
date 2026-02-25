
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "API-SICEnet"
include(":app")
project(":app").projectDir = file("basic-android-kotlin-compose-training-mars-photos-coil-starter/app")
