package model

import java.io.File

//fun Game.saveToFile() {
//    val file = File("$gameId.txt")
//    file.writeText(board.joinToString("\n") { row ->
//        row.joinToString(" ") { it?.symbol?.toString() ?: "." }
//    })
//}
//
//fun Game.loadFromFile() {
//    val file = File("$gameId.txt")
//    if (!file.exists()) return
//    file.readLines().forEachIndexed { rowIndex, line ->
//        line.split(" ").forEachIndexed { colIndex, symbol ->
//            board[rowIndex][colIndex] = when (symbol) {
//                "w" -> Piece.WHITE
//                "b" -> Piece.BLACK
//                "W" -> Piece.WHITE_QUEEN
//                "B" -> Piece.BLACK_QUEEN
//                else -> null
//            }
//        }
//    }
//}


fun loadGame(gameId: String): Pair<Array<Array<Piece?>>, Piece> {
    val file = File("$gameId.txt")
    val board = Array(8) { arrayOfNulls<Piece>(8) }
    var turn = Piece.WHITE // Default if file does not specify

    if (file.exists()) {
        val lines = file.readLines()
        turn = Piece.valueOf(lines.first()) // Read the turn at the top
        lines.drop(1).forEachIndexed { row, line ->
            line.split(" ").forEachIndexed { col, pieceSymbol ->
                board[row][col] = when (pieceSymbol) {
                    "b" -> Piece.BLACK
                    "w" -> Piece.WHITE
                    "B" -> Piece.BLACK_QUEEN
                    "W" -> Piece.WHITE_QUEEN
                    else -> null
                }
            }
        }
    }
    return board to turn
}


fun saveGame(gameId: String, board: Array<Array<Piece?>>, turn: Piece) {
    val file = File("$gameId.txt")
    file.createNewFile()

    file.printWriter().use { writer ->
        board.forEach { row ->
            val line = row.joinToString(" ") { piece ->
                when (piece) {
                    Piece.BLACK -> "b"
                    Piece.WHITE -> "w"
                    Piece.BLACK_QUEEN -> "B"
                    Piece.WHITE_QUEEN -> "W"
                    null -> "-"
                }
            }
            writer.println(line)
        }
        writer.println(if (turn == Piece.WHITE) "WHITE" else "BLACK")
    }
}
