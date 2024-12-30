package model

import storage.MongoGameStorage

class CommandHandler {
    private val games = mutableMapOf<String, Game>()
    private var currentGameId: String? = null
    private var playerColor: Piece? = null

    var onGameUpdated: ((Game) -> Unit)? = null  // callback for UI

    fun startGame(gameId: String) {
        // Attempt to load from Mongo
        val loaded = MongoGameStorage.loadGame(gameId)
        val game = if (loaded != null) {
            Game(gameId, loaded)
        } else {
            // Create new
            Game(gameId)
        }
        games[gameId] = game
        currentGameId = gameId

        // Assign color if not assigned
        if (game.playerAssignments[Piece.WHITE] != true) {
            playerColor = Piece.WHITE
            game.playerAssignments[Piece.WHITE] = true
        } else if (game.playerAssignments[Piece.BLACK] != true) {
            playerColor = Piece.BLACK
            game.playerAssignments[Piece.BLACK] = true
        }

        saveGame(game)
        onGameUpdated?.invoke(game)
    }

    fun refreshGame() {
        val game = currentGame() ?: return
        val loaded = MongoGameStorage.loadGame(game.gameId) ?: return
        val updatedGame = Game(game.gameId, loaded)
        games[game.gameId] = updatedGame
        onGameUpdated?.invoke(updatedGame)
    }

    fun makeMove(from: String, to: String) {
        val game = currentGame() ?: return
        if (playerColor != game.turn) {
            return
        }
        val fromSquare = from.toSquareOrNull() ?: return
        val toSquare = to.toSquareOrNull() ?: return
        game.makeMove(fromSquare, toSquare)
        saveGame(game)
        onGameUpdated?.invoke(game)
    }

    fun currentGame(): Game? {
        return currentGameId?.let { games[it] }
    }

    fun getPlayerColor() = playerColor

    fun saveGame(game: Game) {
        MongoGameStorage.saveGame(game.toGameStateData())
    }
}
