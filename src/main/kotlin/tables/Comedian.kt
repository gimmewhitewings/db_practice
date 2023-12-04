package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Comedian : Table() {
    val comedianName: Column<String> = varchar("comedian_name", length = 20)
    override val primaryKey: PrimaryKey = PrimaryKey(comedianName, name = "comedian_name")

    val specialization: Column<String> = varchar(name = "specialization", length = 20)
    val performance_material: Column<String> = varchar(name = "performance_material", length = 20)
}
