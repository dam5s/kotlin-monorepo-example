package io.damo.kotlinmonorepo.serversupport

import org.slf4j.Logger
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

object LoggingConfig {
    fun initialize() {
        setLogLevel(ROOT_LOGGER_NAME, Level.INFO)

        if (optionalEnvironmentVariable("ENABLE_DEBUG_LOGS") == "true") {
            setLogLevel("io.damo.kotlinmonorepo", Level.DEBUG)
        }
    }

    fun setLogLevel(name: String, level: Level) {
        LoggerFactory.getLogger(name).setLogLevel(level)
    }

    inline fun <reified T> setLogLevel(level: Level) {
        LoggerFactory.getLogger(T::class.java).setLogLevel(level)
    }
}

private typealias LogbackLogger = ch.qos.logback.classic.Logger
private typealias LogbackLevel = ch.qos.logback.classic.Level

fun Logger.setLogLevel(level: Level) {
    (this as LogbackLogger).level = level.toLogbackLevel()
}

private fun Level.toLogbackLevel(): LogbackLevel =
    when (this) {
        Level.ERROR -> LogbackLevel.ERROR
        Level.WARN -> LogbackLevel.WARN
        Level.INFO -> LogbackLevel.INFO
        Level.DEBUG -> LogbackLevel.DEBUG
        Level.TRACE -> LogbackLevel.TRACE
    }
