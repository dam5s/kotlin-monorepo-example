package io.damo.kotlinmonorepo.greetingsserver.greetingsservice

import io.damo.kotlinmonorepo.databasesupport.DatabaseGateway
import javax.sql.DataSource

data class GreetingRecord(val id: String, val text: String)

interface GreetingsDataGateway {
    suspend fun tryFind(): GreetingRecord?
}

class DbGreetingsDataGateway(dataSource: DataSource) : GreetingsDataGateway {
    private val db = DatabaseGateway(dataSource)

    override suspend fun tryFind(): GreetingRecord? =
        db.tryFindOne("select id, text from greetings order by random() limit 1") {
            GreetingRecord(
                id = it.getString("id"),
                text = it.getString("text"),
            )
        }
}
