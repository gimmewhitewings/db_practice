package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object FoodOrder : Table(name = "food_order") {
    val orderId: Column<Int> = integer(name = "order_id").autoIncrement()
    override val primaryKey: PrimaryKey = PrimaryKey(orderId, name = "order_id")
    val clientId: Column<Int> = integer(name = "client_id").references(Client.clientId)
    val itemNumber: Column<Int> = integer(name = "item_number")
}
