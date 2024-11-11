package model

import java.io.File


data class GameStateData(
    val board: Array<Array<Piece?>>,
    val turn: Piece?,
    val playerAssignments: Map<Piece, Boolean>,
    val moveCount: Int
)

object GameStorage {
    fun saveGame(gameId: String, game: Game) {
        val fileName = "game_$gameId.txt"
        val file = File(fileName)

        val stringBuilder = StringBuilder()
        stringBuilder.appendLine(game.turn.name)
        stringBuilder.appendLine("WHITE_ASSIGNED=${game.playerAssignments[Piece.WHITE]}")
        stringBuilder.appendLine("BLACK_ASSIGNED=${game.playerAssignments[Piece.BLACK]}")
        stringBuilder.appendLine("MOVE_COUNT=${game.moveCount}")
        for (row in game.board) {
            for (piece in row) {
                stringBuilder.append(piece?.symbol ?: '-')
            }
            stringBuilder.appendLine()
        }
        file.writeText(stringBuilder.toString())
    }

    fun loadGame(gameId: String): GameStateData? {
        val fileName = "game_$gameId.txt"
        val file = File(fileName)
        if (!file.exists()) {
            return null // Indicate that no saved game exists
        }

        val lines = file.readLines()
        if (lines.isEmpty()) {
            return null // Indicate that the file is empty or invalid
        }

        val turn = Piece.valueOf(lines[0])

        val whiteAssigned = lines[1].split("=")[1].toBoolean()
        val blackAssigned = lines[2].split("=")[1].toBoolean()
        val moveCount = lines[3].split("=")[1].toInt()

        val playerAssignments = mapOf(
            Piece.WHITE to whiteAssigned,
            Piece.BLACK to blackAssigned
        )

        val board = Array(BOARD_DIM) { rowIndex ->
            lines[rowIndex + 4].map { char ->
                when (char) {
                    'w' -> Piece.WHITE
                    'b' -> Piece.BLACK
                    'W' -> Piece.WHITE_QUEEN
                    'B' -> Piece.BLACK_QUEEN
                    else -> null
                }
            }.toTypedArray()
        }

        return GameStateData(board, turn, playerAssignments, moveCount)
    }
}

