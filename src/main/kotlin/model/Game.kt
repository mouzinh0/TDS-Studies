package model

class Game(
    val gameId: String,
    gameStateData: GameStateData? = null
) {
    var board = Array(BOARD_DIM) { Array<Piece?>(BOARD_DIM) { null } }
    var turn: Piece = Piece.WHITE
    var gameState: GameState = GameState.IN_PROGRESS
    var moveCount: Int = 0
    var playerAssignments: MutableMap<Piece, Boolean> =
        mutableMapOf(Piece.WHITE to false, Piece.BLACK to false)

    init {
        if (gameStateData != null) {
            // Load from data
            this.board = gameStateData.board
            this.turn = gameStateData.turn
            this.moveCount = gameStateData.moveCount
            this.gameState = gameStateData.gameState
            this.playerAssignments = gameStateData.playerAssignments.toMutableMap()
        } else {
            // New game
            initializeBoard()
        }
    }

    private fun initializeBoard() {
        // Place black pieces on rows 0..2 with alternating starting columns
        for (row in 0 until 3) {
            for (col in if (row % 2 != 0) 0 until BOARD_DIM step 2 else 1 until BOARD_DIM step 2) {
                board[row][col] = Piece.BLACK
            }
        }

        // Place white pieces on rows 5..7 with alternating starting columns
        for (row in BOARD_DIM - 3 until BOARD_DIM) {
            for (col in if (row % 2 != 0) 0 until BOARD_DIM step 2 else 1 until BOARD_DIM step 2) {
                board[row][col] = Piece.WHITE
            }
        }
    }

    fun toGameStateData(): GameStateData =
        GameStateData(
            gameId = this.gameId,
            board = this.board,
            turn = this.turn,
            playerAssignments = this.playerAssignments,
            moveCount = this.moveCount,
            gameState = this.gameState
        )

    // Check for end game
    private fun checkGameOver(): Boolean {
        val whitePieces = board.flatten().count { it?.color() == Piece.WHITE }
        val blackPieces = board.flatten().count { it?.color() == Piece.BLACK }

        gameState = when {
            whitePieces == 0 -> GameState.BLACK_WIN
            blackPieces == 0 -> GameState.WHITE_WIN
            else -> GameState.IN_PROGRESS
        }
        return gameState != GameState.IN_PROGRESS
    }

    // Make a move from -> to
    fun makeMove(from: Square, to: Square): String {
        if (gameState != GameState.IN_PROGRESS) {
            return "Game is over."
        }

        val piece = board[from.row.index][from.column.index]
            ?: return "No piece at $from"

        if (piece.color() != turn.color()) {
            return "It's $turn's turn."
        }

        // Check if capture is mandatory
        if (mandatoryCaptureExists() && !isCaptureMove(from, to, piece)) {
            return "Capture is mandatory!"
        }

        // Check if the move is valid
        if (!isValidMove(from, to, piece)) {
            return "Invalid move."
        }

        // Execute move
        board[to.row.index][to.column.index] = piece
        board[from.row.index][from.column.index] = null
        moveCount++

        // If capture, remove the jumped piece
        if (isCaptureMove(from, to, piece)) {
            val capturedRow = (from.row.index + to.row.index) / 2
            val capturedCol = (from.column.index + to.column.index) / 2
            board[capturedRow][capturedCol] = null
            moveCount++
        }

        // Promotion
        if (to.row.index == 0 && piece == Piece.WHITE) {
            board[to.row.index][to.column.index] = Piece.WHITE_QUEEN
        } else if (to.row.index == BOARD_DIM - 1 && piece == Piece.BLACK) {
            board[to.row.index][to.column.index] = Piece.BLACK_QUEEN
        }

        changeTurn()
        val gameOver = checkGameOver()
        return if (gameOver) "Game Over: ${gameState}" else "Move successful."
    }

    private fun changeTurn() {
        turn = if (turn == Piece.WHITE) Piece.BLACK else Piece.WHITE
    }

    private fun isCaptureMove(from: Square, to: Square, piece: Piece): Boolean {
        val rowDiff = kotlin.math.abs(from.row.index - to.row.index)
        val colDiff = kotlin.math.abs(from.column.index - to.column.index)
        return rowDiff == 2 && colDiff == 2
    }

    private fun mandatoryCaptureExists(): Boolean {
        // Check all squares
        for (r in 0 until BOARD_DIM) {
            for (c in 0 until BOARD_DIM) {
                val piece = board[r][c] ?: continue
                if (piece.color() == turn.color()) {
                    val square = Square(Row(r), Column(c))
                    if (isCapturePossible(square, piece)) return true
                }
            }
        }
        return false
    }

    private fun isCapturePossible(square: Square, piece: Piece): Boolean {
        // For each valid move, check if it is a capture
        val validMoves = calculateValidMoves(square, piece)
        return validMoves.any { move ->
            isCaptureMove(square, move, piece)
        }
    }

    private fun isValidMove(from: Square, to: Square, piece: Piece): Boolean {
        val validMoves = calculateValidMoves(from, piece)
        return to in validMoves
    }

    fun calculateValidMoves(from: Square, piece: Piece): Set<Square> {
        val moves = mutableSetOf<Square>()
        val directions = if (piece == Piece.WHITE || piece == Piece.WHITE_QUEEN) listOf(-1) else listOf(1)

        // Additional directions if queen
        val rowOffsets = if (piece == Piece.WHITE_QUEEN || piece == Piece.BLACK_QUEEN)
            listOf(-1, 1) else directions
        val colOffsets = listOf(-1, 1)

        if (piece == Piece.WHITE_QUEEN || piece == Piece.BLACK_QUEEN) {
            // Queen: diagonal moves in all directions
            for (rowOffset in rowOffsets) {
                for (colOffset in colOffsets) {
                    var targetRow = from.row.index + rowOffset
                    var targetCol = from.column.index + colOffset
                    while (targetRow in 0 until BOARD_DIM && targetCol in 0 until BOARD_DIM) {
                        if (board[targetRow][targetCol] == null) {
                            moves.add(Square(Row(targetRow), Column(targetCol)))
                        } else {
                            // If it's an enemy piece, maybe we can capture
                            if (board[targetRow][targetCol]?.color() != piece.color()) {
                                val jumpRow = targetRow + rowOffset
                                val jumpCol = targetCol + colOffset
                                if (jumpRow in 0 until BOARD_DIM && jumpCol in 0 until BOARD_DIM &&
                                    board[jumpRow][jumpCol] == null
                                ) {
                                    moves.add(Square(Row(jumpRow), Column(jumpCol)))
                                }
                            }
                            break
                        }
                        targetRow += rowOffset
                        targetCol += colOffset
                    }
                }
            }
        } else {
            // Regular piece
            for (rowOffset in rowOffsets) {
                for (colOffset in colOffsets) {
                    val targetRow = from.row.index + rowOffset
                    val targetCol = from.column.index + colOffset
                    if (targetRow in 0 until BOARD_DIM && targetCol in 0 until BOARD_DIM) {
                        if (board[targetRow][targetCol] == null) {
                            // Normal move
                            moves.add(Square(Row(targetRow), Column(targetCol)))
                        } else {
                            // Maybe capture
                            if (board[targetRow][targetCol]?.color() != piece.color()) {
                                val jumpRow = targetRow + rowOffset
                                val jumpCol = targetCol + colOffset
                                if (jumpRow in 0 until BOARD_DIM && jumpCol in 0 until BOARD_DIM &&
                                    board[jumpRow][jumpCol] == null
                                ) {
                                    moves.add(Square(Row(jumpRow), Column(jumpCol)))
                                }
                            }
                        }
                    }
                }
            }
        }
        return moves
    }
}
