package model

class CommandHandler {
    private val games = mutableMapOf<String, Game>()
    private var currentGameId: String? = null // Track the active game ID


    fun executeCommand(command: String) {
        val parts = command.split(" ")
        val action = parts.getOrNull(0)

        when (action) {
            "start" -> {
                val gameId = parts.getOrNull(1)
                if (gameId == null) {
                    println("Error: A game ID is required to start the game.")
                } else {
                    startGame(gameId)
                }
            }
            "play" -> {
                if (currentGameId == null) {
                    println("Error: Start a game first with the 'start' command.")
                } else {
                    val from = parts.getOrNull(1)
                    val to = parts.getOrNull(2)
                    if (from == null || to == null) {
                        println("Error: Please provide both 'from' and 'to' positions for the play command.")
                    } else {
                        playMove(from, to)
                    }
                }
            }
            "grid" -> {
                if (currentGameId == null) {
                    println("Error: Start a game first with the 'start' command.")
                } else {
                    showGrid()
                }
            }
            "refresh" -> {
                if (currentGameId == null) {
                    println("Error: Start a game first with the 'start' command.")
                } else {
                    refreshGame()
                }
            }
            "exit" -> exitGame()
            else -> println("Unknown command. Available commands are: start, play, grid, refresh, exit.")
        }
    }


    private fun startGame(gameId: String) {
        val game = games[gameId] ?: Game(gameId).also { games[gameId] = it }
        currentGameId = gameId
        game.displayBoard()
    }

    private fun playMove(from: String?, to: String?) {
        if (from == null || to == null) {
            println("Please provide both 'from' and 'to' positions.")
            return
        }
        val game = games.values.firstOrNull { it.turn != null } ?: run {
            println("No active game found.")
            return
        }

        val fromSquare = from.toSquareOrNull()
        val toSquare = to.toSquareOrNull()
        if (fromSquare == null || toSquare == null) {
            println("Invalid move format. Use coordinates like '4a' and '5b'.")
            return
        }

        val resultMessage = game.makeMove(fromSquare, toSquare)
        println(resultMessage)
    }


    private fun showGrid() {
        currentGame()?.displayBoard() ?: println("No active game found.")
    }

    private fun refreshGame() {
        currentGame()?.displayBoard() ?: println("No active game found.")
    }

    private fun exitGame() {
        println("Exiting game.")
        System.exit(0)
    }

    private fun currentGame(): Game? {
        return currentGameId?.let { games[it] }
    }
}
