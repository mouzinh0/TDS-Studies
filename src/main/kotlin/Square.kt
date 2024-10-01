data class Square(val row: Row, val column: Column) {
    val index: Int
        get() = row.index * BOARD_DIM + column.index % BOARD_DIM

    val black: Boolean
        get() = (row.index + column.index) % 2 != 0

    override fun toString(): String = "${row.digit}${column.symbol}"

    companion object {
        val values: List<Square>
            get() = Row.values.flatMap { row ->
                Column.values.map { col -> Square(row, col) }
            }
    }
}

fun String.toSquareOrNull(): Square? {
    if (length != 2) return null
    val row = this[0].toRowOrNull()
    val col = this[1].toColumnOrNull()
    return if (row != null && col != null) Square(row, col) else null
}

fun String.toSquare(): Square = toSquareOrNull() ?: throw IllegalArgumentException("Invalid square format")
