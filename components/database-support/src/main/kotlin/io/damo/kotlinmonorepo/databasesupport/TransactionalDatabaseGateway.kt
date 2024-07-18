package io.damo.kotlinmonorepo.databasesupport

import java.sql.Connection
import javax.sql.DataSource

class TransactionalDatabaseGateway(dataSource: DataSource): DatabaseGateway(dataSource) {

    private val internalTransactionManager = InternalTransactionManager(dataSource)

    val transactions: TransactionManager
        get() = internalTransactionManager

    override suspend fun <T> withConnection(function: (Connection) -> T): T {
        internalTransactionManager.getConnection()?.let {
            return function(it)
        }

        return super.withConnection(function)
    }
}
