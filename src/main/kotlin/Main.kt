import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import tables.*
import java.math.BigDecimal

const val TIMEOUT_IN_MILLIS = 1000L
const val DATABASE_NAME = "test_schema"
const val USER = "root"
const val PASSWORD = "root"

// Объявление области видимости для выполнения задач в фоновом режиме
var tableScope = CoroutineScope(Dispatchers.IO)

fun main() = application {

    // Подключение к базе данных
    Database.connect(
        url = "jdbc:mysql://localhost:3306/${DATABASE_NAME}",
        driver = "com.mysql.cj.jdbc.Driver",
        user = USER,
        password = PASSWORD
    )

    // Массив таблиц для работы с базой данных
    val tables = arrayOf(
        Client,
        Comedian,
        FoodOrder,
        MenuItem,
        OrderItem,
        Performance,
        Poster,
        StandUpClub,
        Ticket
    )

    // Создание схемы таблиц
    transaction {
        SchemaUtils.create(tables = tables)
    }

    // Инициализация состояний для отслеживания данных из базы
    var allRowsFromCurrentTable by remember { mutableStateOf<List<ResultRow>>(emptyList()) }
    var filteredResultsFromCurrentTable by remember { mutableStateOf<List<ResultRow>>(allRowsFromCurrentTable) }
    var currentTable by remember { mutableStateOf(tables.first()) }
    var newValuesList = remember { mutableStateListOf<String>(*Array(currentTable.columns.size) { "" }) }
    var searchValuesList = remember { mutableStateListOf<String>(*Array(currentTable.columns.size) { "" }) }
    var errorsInFields = remember { mutableStateListOf<Boolean>(*Array(currentTable.columns.size) { false }) }

    var showProceduresAndFunctions by remember { mutableStateOf(false) }

    var firstFunctionOrderId by remember { mutableStateOf("") }
    var firstFunctionItemId by remember { mutableStateOf("") }
    var firstFunctionResult by remember { mutableStateOf<Int?>(null) }

    var secondFunctionParameterValue by remember { mutableStateOf("") }
    var secondResult by remember { mutableStateOf("") }

    // Запуск корутины для получения данных из базы в фоновом режиме
    var job = tableScope.launch {
        while (isActive) {
            ensureActive()
            allRowsFromCurrentTable = transaction { tables.first().selectAll().toList() }
            delay(TIMEOUT_IN_MILLIS)
        }
    }

    // Создание окна приложения
    Window(
        onCloseRequest = ::exitApplication,
        title = "Эрднеева Надежда Дмитриевна, ИКБО-06-21 - Разработка баз данных. Практическая работа"
    ) {

        MaterialTheme {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val drawerScope = rememberCoroutineScope()

            // Отображение главного окна приложения
            ModalDrawer(
                drawerContent = {
                    // Отображение списка таблиц для выбора
                    tables.forEach { table ->
                        TextButton(
                            onClick = {
                                // Остановка предыдущей корутины и запуск новой при выборе таблицы
                                tableScope.cancel()
                                tableScope = CoroutineScope(Dispatchers.IO)
                                errorsInFields.clear()
                                newValuesList.clear()
                                searchValuesList.clear()
                                currentTable = table
                                repeat(currentTable.columns.size) {
                                    errorsInFields.add(false)
                                    newValuesList.add("")
                                    searchValuesList.add("")
                                }
                                job = tableScope.launch {
                                    while (isActive) {
                                        allRowsFromCurrentTable = transaction { table.selectAll().toList() }
                                        delay(TIMEOUT_IN_MILLIS)
                                    }
                                }
                                closeDrawer(drawerScope, drawerState)
                            }
                        ) {
                            Text(text = table.tableName)
                        }
                    }
                },
                drawerState = drawerState,
            ) {
                Column {
                    // Отображение верхнего тулбара
                    TopAppBar {
                        IconButton(
                            onClick = {
                                drawerScope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                        }
                        Text(text = currentTable.tableName)
                    }
                    // Отображение блока поиска, блока добавления/обновления данных и таблицы результатов
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Row {
                            Text(text = "Procedures and functions", style = MaterialTheme.typography.h4)
                            IconButton(
                                onClick = {
                                    showProceduresAndFunctions = !showProceduresAndFunctions
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Info, null)
                            }
                        }
                        if (showProceduresAndFunctions) {
                            Column {
                                Text(text = "`get_order_item_price`(order_id int, item_id int)")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = firstFunctionOrderId,
                                        onValueChange = { firstFunctionOrderId = it },
                                        label = { Text(text = "order_id int") },
                                    )

                                    OutlinedTextField(
                                        value = firstFunctionItemId,
                                        onValueChange = { firstFunctionItemId = it },
                                        label = { Text(text = "order_id int") },
                                    )

                                    IconButton(
                                        onClick = {
                                            if (firstFunctionOrderId.toIntOrNull() != null && firstFunctionItemId.toIntOrNull() != null) {
                                                firstFunctionResult =
                                                    getOrderItemPrice(
                                                        firstFunctionOrderId.toInt(),
                                                        firstFunctionItemId.toInt()
                                                    )
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowForward, null)
                                    }

                                    if (firstFunctionResult != null) {
                                        Text("Result: $firstFunctionResult")
                                    }
                                }
                                Text(text = "`get_food_order_price`(order_id int)")
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = secondFunctionParameterValue,
                                        onValueChange = { secondFunctionParameterValue = it },
                                        label = { Text("order_id int") },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    if (secondFunctionParameterValue.toIntOrNull() != null) {
                                                        secondResult =
                                                            getFoodOrderPrice(secondFunctionParameterValue.toInt()).toString()
                                                    }
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Done, null)
                                            }
                                        }
                                    )
                                    if (secondResult.isNotEmpty()) {
                                        Text(text = secondResult)
                                    }
                                }
                            }
                        }
                        Divider()
                        Text(text = "Search", style = MaterialTheme.typography.h4)
                        SearchBlock(currentTable, searchValuesList) { newString, column, index ->
                            searchValuesList[index] = newString
                            if (searchValuesList.any { it.isNotEmpty() }) {
                                filteredResultsFromCurrentTable = allRowsFromCurrentTable
                                searchValuesList.forEachIndexed { searchValueIndex, searchValue ->
                                    val searchableValue =
                                        convertStringToColumnType(
                                            columnType = currentTable.columns[searchValueIndex].columnType,
                                            searchValue.trim()
                                        )
                                    filteredResultsFromCurrentTable = filteredResultsFromCurrentTable.filter {
                                        when (searchableValue) {
                                            is String -> it[currentTable.columns[searchValueIndex]].toString()
                                                .lowercase()
                                                .contains(searchableValue.lowercase())

                                            is Int -> it[currentTable.columns[searchValueIndex]] == searchableValue
                                            else -> {
                                                true
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Divider()

                        Text(text = "Update/Insert", style = MaterialTheme.typography.h4)
                        UpsertBlock(currentTable, newValuesList, errorsInFields)

                        Divider()

                        TableResults(
                            table = currentTable,
                            resultList = if (searchValuesList.any { it.trim().isNotEmpty() }) {
                                filteredResultsFromCurrentTable
                            } else {
                                allRowsFromCurrentTable
                            }
                        )
                    }
                }
            }
        }
    }
}

fun getFoodOrderPrice(orderId: Int): Int {
    return transaction {
        OrderItem.select { OrderItem.orderId eq orderId }.toList()
            .sumOf { getOrderItemPrice(it[OrderItem.orderId], it[OrderItem.itemId]) }
    }
}

fun getOrderItemPrice(orderId: Int, itemId: Int): Int {
    val n: Int = transaction {
        OrderItem.slice(OrderItem.number).select { (OrderItem.orderId eq orderId) and (OrderItem.itemId eq itemId) }
    }.toList().first()[OrderItem.number]
    val p: Int = transaction { MenuItem.select { MenuItem.itemId eq itemId } }
        .toList().first()[MenuItem.price]
    return p * n
}


// Закрытие бокового меню
fun closeDrawer(drawerScope: CoroutineScope, drawerState: DrawerState) {
    drawerScope.launch {
        drawerState.close()
    }
}


// Отображение результатов таблицы
@Composable
fun TableResults(table: Table, resultList: List<ResultRow>) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(table.columns.size + 1),
    ) {
        // Отображение заголовков столбцов
        for (column in table.columns) {
            item {
                Text(
                    text = column.name,
                    color = Color.Blue,
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {}

        // Отображение данных из таблицы
        resultList.forEach { resultRow ->
            table.columns.forEach { column ->
                resultRow.getOrNull(column)?.let {
                    item {
                        Text(text = it.toString(), textAlign = TextAlign.Center)
                    }
                }
            }
            // Кнопка удаления записи
            item {
                IconButton(
                    onClick = {
                        // Получение первичного ключа и удаление строки из базы данных
                        val firstPrimaryKeyColumn = table.primaryKey!!.columns.first()
                        when (firstPrimaryKeyColumn.columnType) {
                            is StringColumnType -> transaction {
                                table.deleteWhere {
                                    firstPrimaryKeyColumn as Column<String> eq resultRow[firstPrimaryKeyColumn]
                                }
                            }

                            is IntegerColumnType -> transaction {
                                table.deleteWhere {
                                    firstPrimaryKeyColumn as Column<Int> eq resultRow[firstPrimaryKeyColumn]
                                }
                            }

                            is AutoIncColumnType -> transaction {
                                table.deleteWhere {
                                    firstPrimaryKeyColumn as Column<Int> eq resultRow[firstPrimaryKeyColumn]
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, null)
                }
            }
        }

    }
}

@Composable
fun SearchBlock(
    table: Table,
    searchValues: List<String>,
    onValueChanged: (String, Column<*>, Int) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(table.columns.size),
    ) {
        table.columns.forEachIndexed { index, column ->
            item {
                OutlinedTextField(
                    value = searchValues[index],
                    onValueChange = {
                        onValueChanged(it, column, index)
                    },
                    label = { Text(text = "${column.name}: ${transaction { column.columnType.sqlType() }}") },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun UpsertBlock(table: Table, newValuesList: MutableList<String>, errorsInFields: MutableList<Boolean>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(table.columns.size),
    ) {
        table.columns.forEachIndexed { index, column ->
            item {
                OutlinedTextField(
                    value = newValuesList[index],
                    onValueChange = {
                        errorsInFields[index] = it.isNotEmpty() &&
                                column.columnType.isAutoInc &&
                                convertStringToColumnType(columnType = column.columnType, it) == null
                        newValuesList[index] = it
                    },
                    label = { Text(text = "${column.name}: ${transaction { column.columnType.sqlType() }}") },
                    isError = errorsInFields[index],
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
    Button(
        onClick = {
            transaction {
                table.upsert { statement ->
                    table.columns.forEachIndexed { index, column ->
                        if (column.columnType.isAutoInc) {
                            if (newValuesList[index].isNotEmpty()) {
                                statement[column as Column<Int>] = newValuesList[index].toInt()
                            }
                        } else {
                            val convertedValue = convertStringToColumnType(
                                columnType = column.columnType as ColumnType,
                                stringValue = newValuesList[index]
                            )
                            @Suppress("UNCHECKED_CAST")
                            when (column.columnType) {
                                is IntegerColumnType -> statement[column as Column<Int>] = convertedValue as Int
                                is LongColumnType -> statement[column as Column<Long>] = convertedValue as Long
                                is DoubleColumnType -> statement[column as Column<Double>] =
                                    convertedValue as Double

                                is VarCharColumnType -> statement[column as Column<String>] =
                                    convertedValue as String

                                is CharColumnType -> statement[column as Column<String>] = convertedValue as String
                                else -> {
                                    throw Exception("${column.columnType}")
                                }
                            }
                        }
                    }
                }
            }
        },
        enabled = errorsInFields.all { !it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Update/Insert")
    }
}

// Конвертация строки в соответствующий тип данных столбца
fun convertStringToColumnType(columnType: IColumnType, stringValue: String): Any? {
    return when (columnType) {
        is IntegerColumnType -> stringValue.toIntOrNull()
        is LongColumnType -> stringValue.toLongOrNull()
        is DoubleColumnType -> stringValue.toDoubleOrNull()
        is FloatColumnType -> stringValue.toFloatOrNull()
        is DecimalColumnType -> {
            val (precision, scale) = columnType.let { it.precision to it.scale }
            stringValue.toBigDecimalOrNull()?.setScale(scale, BigDecimal.ROUND_HALF_UP)
        }

        is AutoIncColumnType -> stringValue.toIntOrNull()
        else -> stringValue
    }
}