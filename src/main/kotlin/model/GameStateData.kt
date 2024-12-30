package model

// Data class for storing game info in the database
data class GameStateData (
    val gameId: String,
    val board: Array<Array<Piece?>>,
    val turn: Piece,
    val playerAssignments: Map<Piece, Boolean>,
    val moveCount: Int,
    val gameState: GameState
)
