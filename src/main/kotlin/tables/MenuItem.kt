package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object MenuItem : Table(name = "menu_item") {
    val itemId: Column<Int> = integer("item_id").autoIncrement()
    override val primaryKey: PrimaryKey = PrimaryKey(itemId, name = "item_id")

    val itemName: Column<String> = varchar("item_name", length = 20)
    val price: Column<Int> = integer("price")
}
