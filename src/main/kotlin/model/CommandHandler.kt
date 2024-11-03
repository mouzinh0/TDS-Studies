class CommandHandler {
    private val games = mutableMapOf<String, Game>()

    fun executeCommand(command: String) {
        val parts = command.split(" ")
        when (parts[0]) {
            "start" -> startGame(parts.getOrNull(1) ?: error("No gameId provided"))
            "play" -> playMove(parts.getOrNull(1), parts.getOrNull(2))
            "grid" -> showGrid(parts.getOrNull(1) ?: error("No gameId provided"))
            "refresh" -> refreshGame(parts.getOrNull(1) ?: error("No gameId provided"))
            "exit" -> exitGame()
            else -> println("Unknown command")
        }
    }

    private fun startGame(gameId: String) {
        val game = games[gameId] ?: Game(gameId).also { games[gameId] = it }
        game.displayBoard()
    }

    private fun playMove(from: String?, to: String?) {
        val fromSquare = from?.toSquareOrNull() ?: error("Invalid from-square")
        val toSquare = to?.toSquareOrNull() ?: error("Invalid to-square")
        val game = games.values.firstOrNull { it.turn == Piece.WHITE || it.turn == Piece.BLACK }
            ?: error("No active game")

        game.makeMove(fromSquare, toSquare)
    }

    private fun showGrid(gameId: String) {
        games[gameId]?.displayBoard() ?: error("No game with id $gameId")
    }

    private fun refreshGame(gameId: String) {
        games[gameId]?.displayBoard() ?: error("No game with id $gameId")
    }

    private fun exitGame() {
        println("Exiting game.")
        System.exit(0)
    }
}
