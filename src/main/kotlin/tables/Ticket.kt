package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Ticket : Table() {
    val ticketId: Column<Int> = integer(name = "ticket_id").autoIncrement()
    val clientId: Column<Int> = integer(name = "client_id").references(Client.clientId)
    val performanceId: Column<String> =
        varchar(name = "performance_id", length = 20).references(Performance.performanceId)

    override val primaryKey: PrimaryKey = PrimaryKey(ticketId, clientId, performanceId)

    val placeNumber: Column<Int> = integer(name = "place_number")
    val price: Column<Int> = integer(name = "price")
}
