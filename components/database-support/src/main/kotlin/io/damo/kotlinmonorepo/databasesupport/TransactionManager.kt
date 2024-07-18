package io.damo.kotlinmonorepo.databasesupport

import kotlinx.coroutines.withContext
import java.sql.Connection
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface TransactionManager {
    suspend fun <T> withTransaction(function: suspend () -> T): T
}

internal class InternalTransactionManager(private val dataSource: DataSource): TransactionManager {

    internal suspend fun getConnection(): Connection? =
        coroutineContext[ConnectionContext.Key]?.connection

    override suspend fun <T> withTransaction(function: suspend () -> T): T =
        allocateConnection().use { connection ->
            withContext(ConnectionContext(connection)) {
                connection.autoCommit = false

                try {
                    val result = function()
                    connection.commit()
                    result

                } catch (t: Throwable) {
                    connection.rollback()
                    throw t
                } finally {
                    connection.autoCommit = true
                }
            }
        }

    private suspend fun allocateConnection(): Connection {
        if (getConnection() != null) {
            throw IllegalStateException("Tried to start a transaction when one is already in progress")
        }

        return dataSource.connection
    }
}

private class ConnectionContext(val connection: Connection): CoroutineContext.Element {

    object Key: CoroutineContext.Key<ConnectionContext>

    override val key: CoroutineContext.Key<*> = Key
}
