class Game(val gameId: String) {
    val board = Array(BOARD_DIM) { Array<Piece?>(BOARD_DIM) { null } }
    var turn = Piece.WHITE
    private var gameState = GameState.IN_PROGRESS
    private var moveCount = 0

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        // Set up black pieces on rows 0 to 2
        for (row in 0 until 3)
            for (col in row % 2 until BOARD_DIM step 2)
                board[row][col] = Piece.BLACK
        // Set up white pieces on rows 5 to 7
        for (row in BOARD_DIM - 3 until BOARD_DIM)
            for (col in row % 2 until BOARD_DIM step 2)
                board[row][col] = Piece.WHITE
    }

    fun displayBoard() {
        println("  +---------------+  Turn = ${turn.symbol}")
        for (row in BOARD_DIM - 1 downTo 0) {
            print("${row + 1} |")
            for (col in 0 until BOARD_DIM) {
                val piece = board[row][col]?.symbol ?: if ((row + col) % 2 == 0) ' ' else '-'
                print(" $piece ")
            }
            println("|")
        }
        println("  +---------------+")
        println("   a b c d e f g h")
    }

    fun makeMove(from: Square, to: Square): Boolean {
        val piece = board[from.row.index][from.column.index] ?: return error("No piece at $from")
        if (piece.color() != turn) return error("It's $turn's turn!")

        if (!isValidMove(from, to, piece)) return false

        // Execute move
        board[to.row.index][to.column.index] = piece
        board[from.row.index][from.column.index] = null

        // Check if the piece should be promoted to queen
        if (to.row.index == 0 && piece == Piece.WHITE) board[to.row.index][to.column.index] = Piece.WHITE_QUEEN
        if (to.row.index == BOARD_DIM - 1 && piece == Piece.BLACK) board[to.row.index][to.column.index] = Piece.BLACK_QUEEN

        if (canCapture(to, piece)) {
            displayBoard() // Show updated board after capture
            return true
        }

        // Change turns and check game state
        changeTurn()
        checkGameOver()
        displayBoard()
        return true
    }

    private fun isValidMove(from: Square, to: Square, piece: Piece): Boolean {
        val validMoves = calculateValidMoves(from, piece)
        return if (validMoves.contains(to)) true else error("Invalid move")
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

    private fun canCapture(square: Square, piece: Piece): Boolean {
        val validMoves = calculateValidMoves(square, piece)
        return validMoves.any { target ->
            val middleRow = (square.row.index + target.row.index) / 2
            val middleCol = (square.column.index + target.column.index) / 2
            val middlePiece = board[middleRow][middleCol]
            middlePiece != null && middlePiece.color() != piece.color()
        }
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
            println("Game over: $gameState")
        }
    }

    private fun Piece.color() = if (this == Piece.WHITE || this == Piece.WHITE_QUEEN) Piece.WHITE else Piece.BLACK
}

