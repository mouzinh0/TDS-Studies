data class Square private constructor(val row: Row, val column: Column) {
    val index: Int
        get() = row.index * BOARD_DIM + column.index % BOARD_DIM

    // Sum is even(white) or odd(black)
    val black: Boolean
        get() = (row.index + column.index) % 2 != 0

    override fun toString(): String = "${row.digit}${column.symbol}"

    companion object {
        val values: List<Square> = Row.values.flatMap { row ->
            Column.values.map { col -> Square(row, col) }
        }
        operator fun invoke(row: Row, column: Column): Square {
            // Procura o Square correto na lista `values` usando o índice
            return values.first { it.row == row && it.column == column }
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
