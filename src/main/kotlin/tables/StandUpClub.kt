package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object StandUpClub : Table(name = "stand_up_club") {
    val clubName: Column<String> = varchar(name = "club_name", length = 20)
    override val primaryKey: PrimaryKey = PrimaryKey(clubName, name = "club_name")

    val openingHours: Column<String> = varchar(name = "opening_hours", length = 20)
    val address: Column<String> = varchar(name = "address", length = 20)
}
