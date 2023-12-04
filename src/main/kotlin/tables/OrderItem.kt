package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object OrderItem : Table(name = "order_item") {
    val itemId: Column<Int> = integer(name = "item_id").references(MenuItem.itemId)
    val orderId: Column<Int> = integer(name = "order_id").references(FoodOrder.orderId)
    override val primaryKey: PrimaryKey = PrimaryKey(itemId, orderId)

    val number: Column<Int> = integer(name = "number")
}
