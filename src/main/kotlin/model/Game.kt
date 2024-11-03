package model

class Game(val gameId: String) {
    val board = Array(BOARD_DIM) { Array<Piece?>(BOARD_DIM) { null } }
    val moves: MutableList<Char> = mutableListOf(
        ' ', 'b', ' ', 'b', ' ', 'b', ' ', 'b',
        'b', ' ', 'b', ' ', 'b', ' ', 'b', ' ',
        ' ', 'b', ' ', 'b', ' ', 'b', ' ', 'b',
        '-', ' ', '-', ' ', '-', ' ', '-', ' ',
        ' ', '-', ' ', '-', ' ', '-', ' ', '-',
        'w', ' ', 'w', ' ', 'w', ' ', 'w', ' ',
        ' ', 'w', ' ', 'w', ' ', 'w', ' ', 'w',
        'w', ' ', 'w', ' ', 'w', ' ', 'w', ' '
    )
    val movesSet: Array<Array<Char>> = Array(8) { row ->
        Array(8) { col ->
            moves[row * 8 + col]
        }
    }
    var turn = Piece.WHITE
    private var gameState = GameState.IN_PROGRESS
    private var moveCount = 0

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        // Place black pieces on rows 0 to 2 with alternating starting columns
        for (row in 0 until 3) {
            for (col in if (row % 2 != 0) 0 until BOARD_DIM step 2 else 1 until BOARD_DIM step 2) {
                board[row][col] = Piece.BLACK
            }
        }

        // Place white pieces on rows 5 to 7 with alternating starting columns
        for (row in BOARD_DIM - 3 until BOARD_DIM) {
            for (col in if (row % 2 != 0) 0 until BOARD_DIM step 2 else 1 until BOARD_DIM step 2) {
                board[row][col] = Piece.WHITE
            }
        }
    }


    fun displayBoard() {
        println("Turn = ${turn.symbol}")
        println("Player = ${turn.symbol}")
        println("   a b c d e f g h")
        println(" +-----------------+")
        for (row in 0 .. 7) {
            print("${8 - row}| ") // Row label
            for (col in 0..7) {
                print("${movesSet[row][col]} ") // Display each piece or empty space
            }
            println("|${8 - row}") // Row label on the right side
        }
        println(" +-----------------+")
        println("   a b c d e f g h") // Column labels


    }

    fun makeMove(from: Square, to: Square): String {
        val piece = board[from.row.index][from.column.index]
        if (piece == null) return "No piece there."
        if (piece.color() != turn) return "It's $turn's turn!"

        // Check if a capture is mandatory
        if (mandatoryCaptureExists() && !isCaptureMove(from, to, piece)) {
            return "Capture is mandatory."
        }

        if (!isValidMove(from, to, piece)) return "Invalid move from ${from.index} to ${to.index}."

        // Execute move
        board[to.row.index][to.column.index] = piece
        board[from.row.index][from.column.index] = null
        movesSet[to.row.index][to.column.index] = piece.symbol
        movesSet[from.row.index][from.column.index] = '-'

        // Handle capturing logic
        if (isCaptureMove(from, to, piece)) {
            val capturedRow = (from.row.index + to.row.index) / 2
            val capturedCol = (from.column.index + to.column.index) / 2
            board[capturedRow][capturedCol] = null
            movesSet[capturedRow][capturedCol] = '-' // Update visual board for captured piece
        }

        if (to.row.index == 0 && piece == Piece.WHITE) {
            board[to.row.index][to.column.index] = Piece.WHITE_QUEEN
            movesSet[to.row.index][to.column.index] = 'W'
        } else if (to.row.index == BOARD_DIM - 1 && piece == Piece.BLACK) {
            board[to.row.index][to.column.index] = Piece.BLACK_QUEEN
            movesSet[to.row.index][to.column.index] = 'B'
        }

        changeTurn()
        checkGameOver()
        displayBoard()
        return "Move successful."
        }



    private fun isValidMove(from: Square, to: Square, piece: Piece): Boolean {
        val validMoves = calculateValidMoves(from, piece)
        return validMoves.contains(to)
    }

    private fun calculateValidMoves(square: Square, piece: Piece): Set<Square> {
        val moves = mutableSetOf<Square>()
        val directions = if (piece == Piece.WHITE || piece == Piece.WHITE_QUEEN) listOf(-1) else listOf(1)

        for (rowOffset in directions) {
            for (colOffset in listOf(-1, 1)) {
                val targetRow = square.row.index + rowOffset
                val targetCol = square.column.index + colOffset

                if (targetRow in 0 until BOARD_DIM && targetCol in 0 until BOARD_DIM) {
                    val targetSquare = Square(Row(targetRow), Column(targetCol))
                    if (board[targetRow][targetCol] == null) {
                        moves.add(targetSquare)
                    } else if (board[targetRow][targetCol]?.color() != piece.color()) {
                        // Check for capture possibility
                        val captureRow = targetRow + rowOffset
                        val captureCol = targetCol + colOffset
                        if (captureRow in 0 until BOARD_DIM && captureCol in 0 until BOARD_DIM && board[captureRow][captureCol] == null) {
                            moves.add(Square(Row(captureRow), Column(captureCol)))
                        }
                    }
                }
            }
        }

        return moves
    }


    private fun changeTurn() {
        turn = if (turn == Piece.WHITE) Piece.BLACK else Piece.WHITE
    }

    private fun checkGameOver() {
        val whitePieces = board.flatten().count { it?.color() == Piece.WHITE }
        val blackPieces = board.flatten().count { it?.color() == Piece.BLACK }

        gameState = when {
            whitePieces == 0 -> GameState.BLACK_WIN
            blackPieces == 0 -> GameState.WHITE_WIN
            else -> GameState.IN_PROGRESS
        }

        if (gameState != GameState.IN_PROGRESS) {
            println("Model.Game over: $gameState")
        }
    }

    private fun Piece.color() = if (this == Piece.WHITE || this == Piece.WHITE_QUEEN) Piece.WHITE else Piece.BLACK

    private fun mandatoryCaptureExists(): Boolean {
        for (row in 0 until BOARD_DIM) {
            for (col in 0 until BOARD_DIM) {
                val piece = board[row][col]
                if (piece?.color() == turn && isCapturePossible(Square(Row(row), Column(col)), piece)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isCaptureMove(from: Square, to: Square, piece: Piece): Boolean {
        val rowDiff = Math.abs(from.row.index - to.row.index)
        val colDiff = Math.abs(from.column.index - to.column.index)
        return rowDiff == 2 && colDiff == 2 // A capture move must jump two squares
    }

    private fun isCapturePossible(square: Square, piece: Piece): Boolean {
        val validMoves = calculateValidMoves(square, piece)
        return validMoves.any { target ->
            val middleRow = (square.row.index + target.row.index) / 2
            val middleCol = (square.column.index + target.column.index) / 2
            val middlePiece = board[middleRow][middleCol]
            middlePiece != null && middlePiece.color() != piece.color()
        }
    }

}

