package storage

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoClients
import model.*
import org.bson.Document
import com.mongodb.client.model.Filters.eq

object MongoGameStorage {

    private val mongoUri = "mongodb+srv://gmtemporario:PvDRcPUjUcGqFKnF@cluster0.zplgo.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"  // Adjust if needed
    private val databaseName = "checkers_db"
    private val collectionName = "checkers_games"

    private val client: MongoClient by lazy {
        MongoClients.create(mongoUri)
    }

    private val collection: MongoCollection<Document> by lazy {
        client.getDatabase(databaseName).getCollection(collectionName)
    }

    fun loadGame(gameId: String): GameStateData? {
        val doc = collection.find(eq("gameId", gameId)).firstOrNull() ?: return null

        // Convert doc to GameStateData
        val turnStr = doc.getString("turn")
        val turn = Piece.valueOf(turnStr)

        val gameStateStr = doc.getString("gameState")
        val gameState = GameState.valueOf(gameStateStr)

        val moveCount = doc.getInteger("moveCount")

        val whiteAssigned = doc.getBoolean("whiteAssigned", false)
        val blackAssigned = doc.getBoolean("blackAssigned", false)
        val playerAssignments = mapOf(
            Piece.WHITE to whiteAssigned,
            Piece.BLACK to blackAssigned
        )

        // Rebuild board
        val boardList = doc.getList("board", List::class.java)
        val board = Array(BOARD_DIM) { rowIndex ->
            Array<Piece?>(BOARD_DIM) { colIndex ->
                val symbol = boardList[rowIndex][colIndex] as String?
                when (symbol) {
                    "w" -> Piece.WHITE
                    "b" -> Piece.BLACK
                    "W" -> Piece.WHITE_QUEEN
                    "B" -> Piece.BLACK_QUEEN
                    else -> null
                }
            }
        }

        return GameStateData(
            gameId = gameId,
            board = board,
            turn = turn,
            playerAssignments = playerAssignments,
            moveCount = moveCount,
            gameState = gameState
        )
    }

    fun saveGame(state: GameStateData) {
        val doc = Document("gameId", state.gameId)
            .append("turn", state.turn.name)
            .append("gameState", state.gameState.name)
            .append("moveCount", state.moveCount)
            .append("whiteAssigned", state.playerAssignments[Piece.WHITE] ?: false)
            .append("blackAssigned", state.playerAssignments[Piece.BLACK] ?: false)

        // Convert board to a list of lists of symbols
        val boardList = state.board.map { row ->
            row.map { piece -> piece?.symbol?.toString() ?: "-" }
        }

        doc.append("board", boardList)

        // Upsert
        collection.findOneAndReplace(eq("gameId", state.gameId), doc)
            ?: collection.insertOne(doc)
    }
}
