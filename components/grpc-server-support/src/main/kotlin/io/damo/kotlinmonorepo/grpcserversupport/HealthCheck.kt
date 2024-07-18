package io.damo.kotlinmonorepo.grpcserversupport

import org.slf4j.LoggerFactory
import javax.sql.DataSource

interface HealthCheck {
    suspend fun run(): Boolean
}

internal class FunctionHealthCheck(private val function: () -> Boolean) : HealthCheck {
    override suspend fun run() = function()
}

class DatabaseHealthCheck(private val dataSource: DataSource) : HealthCheck {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun run(): Boolean {
        try {
            dataSource.connection.use {
                it.prepareStatement("select 1").execute()
                logger.debug("Health check with database succeeded")
                return true
            }
        } catch (e: Exception) {
            logger.error("Health check with database failed", e)
            return false
        }
    }
}
