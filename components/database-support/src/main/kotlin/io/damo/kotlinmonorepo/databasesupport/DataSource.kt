package io.damo.kotlinmonorepo.databasesupport

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

data class DatabaseConnectionOptions(val url: String, val username: String?, val password: String?)

fun createDataSource(options: DatabaseConnectionOptions): DataSource =
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = options.url
            username = options.username
            password = options.password
            validate()
        }
    )
