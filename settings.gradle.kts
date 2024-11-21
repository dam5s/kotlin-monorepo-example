pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "KotlinMonorepo"

private fun includeSubProjectsOf(path: String) {
    val gradlePath = path.replace("/", ":")
    val children = file(path).listFiles() ?: emptyArray()

    children
        .filter { it.isDirectory }
        .map { ":${gradlePath}:${it.name}" }
        .forEach { include(it) }
}

includeSubProjectsOf("applications")
includeSubProjectsOf("components")
includeSubProjectsOf("databases")
includeSubProjectsOf("protocols")
include("protocols")
