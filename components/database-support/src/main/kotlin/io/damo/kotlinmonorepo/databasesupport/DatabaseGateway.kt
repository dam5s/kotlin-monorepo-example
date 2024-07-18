package io.damo.kotlinmonorepo.databasesupport

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import javax.sql.DataSource

open class DatabaseGateway(private val dataSource: DataSource) {

    suspend fun <T> findMany(sql: String, vararg bindings: Any, rowMapper: (ResultSet) -> T): List<T> =
        withConnection { connection ->
            connection
                .prepareStatement(sql)
                .bind(bindings)
                .executeQuery()
                .map(rowMapper)
        }

    suspend fun <T> findOne(sql: String, vararg bindings: Any, rowMapper: (ResultSet) -> T): T =
        withConnection { connection ->
            connection
                .prepareStatement(sql)
                .bind(bindings)
                .executeQuery()
                .mapFirst(rowMapper)
        }

    suspend fun <T> tryFindOne(sql: String, vararg bindings: Any, rowMapper: (ResultSet) -> T): T? =
        withConnection { connection ->
            connection
                .prepareStatement(sql)
                .bind(bindings)
                .executeQuery()
                .tryMapFirst(rowMapper)
        }

    suspend fun count(sql: String, vararg bindings: Any): Long =
        withConnection { connection ->
            connection
                .prepareStatement(sql)
                .bind(bindings)
                .executeQuery()
                .mapFirst { it.getLong(1) }
        }

    suspend fun execute(sql: String, vararg bindings: Any): Unit =
        withConnection { connection ->
            connection
                .prepareStatement(sql)
                .bind(bindings)
                .execute()
        }

    // This is overridden by TransactionalDatabaseGateway
    internal open suspend fun <T> withConnection(function: (Connection) -> T): T =
        coroutineScope {
            async { dataSource.connection.use(function) }.await()
        }

    private fun <T> ResultSet.map(mapping: (ResultSet) -> T): List<T> {
        val results = arrayListOf<T>()

        while (this.next()) {
            results.add(mapping(this))
        }

        return results
    }

    private fun <T> ResultSet.tryMapFirst(mapping: (ResultSet) -> T): T? {
        if (next()) {
            return mapping(this)
        }
        return null
    }

    private fun <T> ResultSet.mapFirst(mapping: (ResultSet) -> T): T {
        next()
        return mapping(this)
    }

    private fun PreparedStatement.bind(bindings: Array<out Any?>): PreparedStatement {
        bindings.forEachIndexed { index, value ->
            val bindingIndex = index + 1

            when (value) {
                is String -> setString(bindingIndex, value)
                is Int -> setInt(bindingIndex, value)
                is Long -> setLong(bindingIndex, value)
                is LocalDate -> setDate(bindingIndex, java.sql.Date.valueOf(value))
                is LocalDateTime -> setTimestamp(bindingIndex, java.sql.Timestamp.valueOf(value))
                null -> setString(bindingIndex, null)
                else -> throw IllegalArgumentException("Unsupported binding for value of type ${value.javaClass}")
            }
        }
        return this
    }
}
