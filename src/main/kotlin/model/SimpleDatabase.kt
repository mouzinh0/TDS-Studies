import java.io.File

fun Game.saveToFile() {
    val file = File("$gameId.txt")
    file.writeText(board.joinToString("\n") { row ->
        row.joinToString(" ") { it?.symbol?.toString() ?: "." }
    })
}

fun Game.loadFromFile() {
    val file = File("$gameId.txt")
    if (!file.exists()) return
    file.readLines().forEachIndexed { rowIndex, line ->
        line.split(" ").forEachIndexed { colIndex, symbol ->
            board[rowIndex][colIndex] = when (symbol) {
                "w" -> Piece.WHITE
                "b" -> Piece.BLACK
                "W" -> Piece.WHITE_QUEEN
                "B" -> Piece.BLACK_QUEEN
                else -> null
            }
        }
    }
}
