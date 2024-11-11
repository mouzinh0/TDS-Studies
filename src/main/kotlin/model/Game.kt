package model

class Game(val gameId: String, gameStateData: GameStateData? = null) {
    // 2D array of PIECE objects
    var board = Array(BOARD_DIM) { Array<Piece?>(BOARD_DIM) { null } }

    var turn: Piece
    var gameState = GameState.IN_PROGRESS
    var moveCount: Int = 0
    var playerAssignments: MutableMap<Piece, Boolean>

    init {
        if (gameStateData?.turn != null && gameStateData.board.any { row -> row.any { it != null } }) {
            // Use the loaded game data if it's valid
            this.board = gameStateData.board
            this.turn = gameStateData.turn
            this.moveCount = gameStateData.moveCount
            this.playerAssignments = gameStateData.playerAssignments.toMutableMap()
        } else {
            // No valid game data, initialize a new game
            board = Array(BOARD_DIM) { Array<Piece?>(BOARD_DIM) { null } }
            turn = Piece.WHITE
            moveCount = 0
            playerAssignments = mutableMapOf(Piece.WHITE to false, Piece.BLACK to false)
            initializeBoard()
            saveGame()
        }
    }

    // Board with Pieces Objects in place
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

    fun updateState(gameStateData: GameStateData) {
        this.board = gameStateData.board
        this.turn = gameStateData.turn ?: Piece.WHITE
        this.moveCount = gameStateData.moveCount
        this.playerAssignments = gameStateData.playerAssignments.toMutableMap()
    }

    fun saveGame() {
        GameStorage.saveGame(gameId, this)
    }

    // Builds Visual Board
    fun displayBoard(playerColor: Piece?) {
        println("Turn = ${turn.symbol}")
        println("Player = ${playerColor?.symbol ?: "Unknown"}")
        println("   a b c d e f g h")
        println(" +-----------------+")
        for (rowIndex in 0 until BOARD_DIM) {
            val rowNumber = BOARD_DIM - rowIndex
            print("$rowNumber| ")
            for (colIndex in 0 until BOARD_DIM) {
                val piece = board[rowIndex][colIndex]
                val symbol = when {
                    piece != null -> piece.symbol
                    (rowIndex + colIndex) % 2 == 0 -> ' ' // White square
                    else -> '-' // Black square
                }
                print("$symbol ")
            }
            println("|$rowNumber")
        }
        println(" +-----------------+")
        println("   a b c d e f g h")
    }


    fun makeMove(from: Square, to: Square): String {
        val piece = board[from.row.index][from.column.index] ?: return "No piece there."
        if (piece.color() != turn) return "It's $turn's turn!"

        // Check if a capture is mandatory
        if (mandatoryCaptureExists() && !isCaptureMove(from, to, piece)) {
            return "Capture is mandatory."
        }

        if (!isValidMove(from, to, piece)) return "Invalid move from ${from.index} to ${to.index}."

        // Execute move
        board[to.row.index][to.column.index] = piece
        board[from.row.index][from.column.index] = null
        moveCount++

        // Handle capturing logic
        if (isCaptureMove(from, to, piece)) {
            val capturedRow = (from.row.index + to.row.index) / 2
            val capturedCol = (from.column.index + to.column.index) / 2
            board[capturedRow][capturedCol] = null
            moveCount++
        }

        if (to.row.index == 0 && piece == Piece.WHITE) {
            board[to.row.index][to.column.index] = Piece.WHITE_QUEEN
        } else if (to.row.index == BOARD_DIM - 1 && piece == Piece.BLACK) {
            board[to.row.index][to.column.index] = Piece.BLACK_QUEEN
        }

        changeTurn()
        val gameOver = checkGameOver()
        saveGame()
        return if (gameOver) {
            "Game Over: $gameState"
        } else {
            "Move successful."
        }    }




    private fun isValidMove(from: Square, to: Square, piece: Piece): Boolean {
        val validMoves = calculateValidMoves(from, piece)
        return validMoves.contains(to)
    }

    private fun calculateValidMoves(square: Square, piece: Piece): Set<Square> {
        val moves = mutableSetOf<Square>()
        val directions = if (piece == Piece.WHITE || piece == Piece.WHITE_QUEEN) listOf(-1) else listOf(1)

        if (piece == Piece.WHITE_QUEEN || piece == Piece.BLACK_QUEEN) {
            // Queen can move any number of squares diagonally
            for (rowOffset in listOf(-1, 1)) {
                for (colOffset in listOf(-1, 1)) {
                    var targetRow = square.row.index + rowOffset
                    var targetCol = square.column.index + colOffset
                    while (targetRow in 0 until BOARD_DIM && targetCol in 0 until BOARD_DIM) {
                        val targetSquare = Square(Row(targetRow), Column(targetCol))
                        if (board[targetRow][targetCol] == null) {
                            moves.add(targetSquare)
                        } else if (board[targetRow][targetCol]?.color() != piece.color()) {
                            // Check for a capture move
                            val captureRow = targetRow + rowOffset
                            val captureCol = targetCol + colOffset
                            if (captureRow in 0 until BOARD_DIM && captureCol in 0 until BOARD_DIM && board[captureRow][captureCol] == null) {
                                moves.add(Square(Row(captureRow), Column(captureCol)))
                            }
                            break // Stop checking in this direction after a capture or blocked piece
                        } else {
                            break // Blocked by a same-color piece
                        }
                        targetRow += rowOffset
                        targetCol += colOffset
                    }
                }
            }
        } else {
            // Regular piece movement
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
        }

        return moves
    }



    private fun isCaptureMove(from: Square, to: Square, piece: Piece): Boolean {
        val rowDiff = Math.abs(from.row.index - to.row.index)
        val colDiff = Math.abs(from.column.index - to.column.index)
        return rowDiff == 2 && colDiff == 2 // A capture move must jump two squares
    }


    // Checks for all squares if its next jumps have eatable piece with help of isCapturePossible()
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


    private fun isCapturePossible(square: Square, piece: Piece): Boolean {
        val validMoves = calculateValidMoves(square, piece)
        return validMoves.any { target ->
            val middleRow = (square.row.index + target.row.index) / 2
            val middleCol = (square.column.index + target.column.index) / 2
            val middlePiece = board[middleRow][middleCol]
            middlePiece != null && middlePiece.color() != piece.color()
        }
    }


    // Helper to changer turn
    private fun changeTurn() {
        turn = if (turn == Piece.WHITE) Piece.BLACK else Piece.WHITE
    }

    // Helper to get piece color
    private fun Piece.color() = if (this == Piece.WHITE || this == Piece.WHITE_QUEEN) Piece.WHITE else Piece.BLACK

    // Checks for end game
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


}

