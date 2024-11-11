import model.Game
import model.GameState
import model.toSquare
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GameTest {

    @Test
    fun testSequenceOfMoves() {
        val game = Game("testGame")
        val moves = listOf(
            "3a" to "4b",
            "6d" to "5c",
            "4b" to "6d",
            "7e" to "5c",
            "3c" to "4b",
            "5c" to "3a",
            "2b" to "3c",
            "6b" to "5c",
            "1c" to "2b",
            "3a" to "1c",
            "3c" to "4b",
            "5c" to "3a",
            "3e" to "4d",
            "1c" to "3e",
            "1e" to "2d",
            "3e" to "1c"
        )

        // Execute each move in sequence and print board after each move
        for ((from, to) in moves) {
            val fromSquare = from.toSquare()
            val toSquare = to.toSquare()

            val result = game.makeMove(fromSquare, toSquare)
            println("Move from $from to $to: $result")

            // Optionally, assert that each move is successful
            assertEquals("Move successful.", result, "Expected move from $from to $to to be successful.")
            game.displayBoard(playerColor = null) // To visually check each move in the output
        }
    }


}
