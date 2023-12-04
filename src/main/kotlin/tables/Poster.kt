package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Poster : Table() {
    val performanceId: Column<String> =
        varchar(name = "performance_id", length = 20).references(Performance.performanceId)
    override val primaryKey: PrimaryKey = PrimaryKey(performanceId)

    val pictureLink: Column<String> = char(name = "picture_link", length = 18)
}
