package testing.kotlinmonorepo.greetingsserver

import io.damo.kotlinmonorepo.databasesupport.DatabaseConnectionOptions
import io.damo.kotlinmonorepo.databasesupport.TransactionalDatabaseGateway
import io.damo.kotlinmonorepo.databasesupport.createDataSource
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionManagerTest {

    private val databaseOptions =
        DatabaseConnectionOptions(
            url = "jdbc:postgresql://localhost/greetings_test",
            username = "monorepodev",
            password = "monorepodev",
        )

    private val db = TransactionalDatabaseGateway(createDataSource(databaseOptions))
    private val transactions = db.transactions

    @BeforeTest
    fun setup() = runBlocking {
        db.execute("truncate greetings")
    }

    @Test
    fun testTransactionRollback() = runBlocking {
        try {
            transactions.withTransaction {
                db.execute("insert into greetings (text) values ('Bonjour')")

                val count = db.count("select count(1) from greetings")
                assertEquals(1, count)

                throw IllegalStateException("This will force the rollback")
            }
        } catch (ignored: IllegalStateException) {
        }

        val count = db.count("select count(1) from greetings")
        assertEquals(0, count)
    }
}
