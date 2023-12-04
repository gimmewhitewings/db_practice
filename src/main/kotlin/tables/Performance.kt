package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Performance : Table() {
    val performanceId: Column<String> = varchar(name = "performance_id", length = 20)
    override val primaryKey: PrimaryKey = PrimaryKey(performanceId, name = "performance_id")

    val date: Column<String> = varchar(name = "date", length = 20)
    val time: Column<String> = varchar(name = "time", length = 20)
    val performanceName: Column<String> = varchar(name = "performance_name", length = 20)
    val clubName: Column<String> = varchar(name = "club_name", length = 20).references(StandUpClub.clubName)
    val comedianName: Column<String> = varchar(name = "comedian_name", length = 20).references(Comedian.comedianName)
}
