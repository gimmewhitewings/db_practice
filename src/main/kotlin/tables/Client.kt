package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Client : Table() {
    val clientId: Column<Int> = integer("client_id").autoIncrement()
    override val primaryKey: PrimaryKey = PrimaryKey(clientId, name = "client_id")

    val clientName: Column<String> = char(name = "client_name", length = 18)
}
