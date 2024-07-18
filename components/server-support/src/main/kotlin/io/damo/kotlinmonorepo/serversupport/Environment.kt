package io.damo.kotlinmonorepo.serversupport

fun resolvePort(name: String, default: Int): Int = try {
    val portString = optionalEnvironmentVariable(name)
    portString?.toInt() ?: default

} catch (e: NumberFormatException) {
    throw RuntimeException("invalid port value in $name environment variable")
}

fun requiredIntEnvironmentVariable(name: String): Int =
    try {
        requiredEnvironmentVariable(name).toInt()
    } catch (e: NumberFormatException) {
        throw RuntimeException("invalid value Int value in $name environment variable")
    }

fun requiredEnvironmentVariable(name: String): String =
    System.getenv()[name] ?: throw RuntimeException("missing configuration: $name")

fun optionalEnvironmentVariable(name: String): String? =
    System.getenv()[name]
