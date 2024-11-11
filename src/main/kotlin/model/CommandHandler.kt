package model

import java.io.File

class CommandHandler {
    private val games = mutableMapOf<String, Game>()
    private var currentGameId: String? = null // Track the active game ID
    private var playerColor: Piece? = null




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
        val gameFile = File("game_$gameId.txt")
        val game: Game

        if (gameFile.exists()) {
            val gameStateData = GameStorage.loadGame(gameId)
            game = if (gameStateData != null) {
                games[gameId] ?: Game(gameId, gameStateData)
            } else {
                // If gameStateData is null, initialize a new game
                Game(gameId)
            }
            games[gameId] = game
        } else {
            // No existing game, create a new one
            game = Game(gameId)
            games[gameId] = game
        }

        currentGameId = gameId

        // Assign player color
        if (game.playerAssignments[Piece.WHITE] != true) {
            playerColor = Piece.WHITE
            game.playerAssignments[Piece.WHITE] = true
            println("You are playing as white.")
        } else if (game.playerAssignments[Piece.BLACK] != true) {
            playerColor = Piece.BLACK
            game.playerAssignments[Piece.BLACK] = true
            println("You are playing as black.")
        } else {
            // Game is full or needs to create a new one based on your logic
            println("Game is full or needs to create a new one.")
            return
        }

        game.saveGame()
        game.displayBoard(playerColor)
    }




    private fun playMove(from: String, to: String) {
        val game = currentGame() ?: run {
            println("No active game found.")
            return
        }

        if (playerColor != game.turn) {
            println("It's not your turn.")
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
        game.displayBoard(playerColor)

        if (game.gameState != GameState.IN_PROGRESS) {
            println("Game over: ${game.gameState}")
            exitGame()
        }
    }



    private fun showGrid() {
        currentGame()?.displayBoard(playerColor) ?: println("No active game found.")
    }

    private fun refreshGame() {
        val game = currentGame()
        if (game == null) {
            println("No active game found.")
            return
        }

        val newGameState = GameStorage.loadGame(game.gameId)
        if (newGameState != null) {
            game.updateState(newGameState)
        }

        if (playerColor != game.turn) {
            println("It's not your turn.")
        } else {
            println("It's your turn!")
        }
        game.displayBoard(playerColor)
    }

        //val game = games[currentGameId] ?: Game(currentGameId!!).also { games[currentGameId!!] = it }
        //currentGame()?.displayBoard() ?: println("No active game found.")
        //currentGame()?.let { currentGame()?.let { it1 -> loadGame(it.gameId, it1.board) } }
        //loadGame(currentGameId!!, game.board)
        //currentGame()?.displayBoard() ?: println("No active game found.")


     private fun exitGame() {
        println("Exiting game.")
        System.exit(0)
    }

    private fun currentGame(): Game? {
        return currentGameId?.let { games[it] }
    }
}
