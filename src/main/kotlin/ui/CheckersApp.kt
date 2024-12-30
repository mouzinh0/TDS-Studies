package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import model.*
import model.CommandHandler

@Composable
@Preview
fun CheckersApp(
    onExit: () -> Unit, // <-- 1) Accept an `onExit` callback
) {
    val handler = remember { CommandHandler() }
    var currentGame by remember { mutableStateOf<Game?>(null) }
    var gameId by remember { mutableStateOf("") }

    // Options
    var showTargets by remember { mutableStateOf(false) }
    var autoRefresh by remember { mutableStateOf(false) }

    // For "target squares" highlight
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var validMoves by remember { mutableStateOf(setOf<Square>()) }

    // Setup callback for CommandHandler -> UI
    LaunchedEffect(handler) {
        handler.onGameUpdated = { updatedGame ->
            currentGame = updatedGame
            // If a square was selected, recalc valid moves
            selectedSquare?.let { sq ->
                val piece = updatedGame.board[sq.row.index][sq.column.index]
                if (piece != null && piece.color() == updatedGame.turn.color()) {
                    validMoves = updatedGame.calculateValidMoves(sq, piece)
                } else {
                    validMoves = emptySet()
                }
            }
        }
    }

    // Auto Refresh every 5 seconds if enabled
    LaunchedEffect(autoRefresh) {
        while (this.isActive) {
            if (autoRefresh) {
                // Refresh game
                handler.refreshGame()
            }
            delay(5000)
        }
    }

    // Top-level window
    // 2) Use `onCloseRequest = onExit`
    Window(
        onCloseRequest = onExit,
        title = "Compose Checkers"
    ) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar {
                        Text("Checkers", modifier = Modifier.padding(8.dp))
                    }
                },
                drawerContent = null,
                drawerGesturesEnabled = false
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Menus in a row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Game Menu
                        var expandedGameMenu by remember { mutableStateOf(false) }
                        Box {
                            Button(onClick = { expandedGameMenu = true }) {
                                Text("Game")
                            }
                            DropdownMenu(
                                expanded = expandedGameMenu,
                                onDismissRequest = { expandedGameMenu = false }
                            ) {
                                DropdownMenuItem(onClick = {
                                    expandedGameMenu = false
                                    // Start
                                    if (gameId.isNotBlank()) {
                                        handler.startGame(gameId)
                                    }
                                }) {
                                    Text("Start")
                                }
                                DropdownMenuItem(onClick = {
                                    expandedGameMenu = false
                                    // Refresh
                                    handler.refreshGame()
                                }) {
                                    Text("Refresh")
                                }
                                DropdownMenuItem(onClick = {
                                    expandedGameMenu = false
                                    // Exit
                                    onExit()   // <-- 3) Call the lambda
                                }) {
                                    Text("Exit")
                                }
                            }
                        }

                        // Options Menu
                        var expandedOptionsMenu by remember { mutableStateOf(false) }
                        Box {
                            Button(onClick = { expandedOptionsMenu = true }) {
                                Text("Options")
                            }
                            DropdownMenu(
                                expanded = expandedOptionsMenu,
                                onDismissRequest = { expandedOptionsMenu = false }
                            ) {
                                // Show Targets
                                DropdownMenuItem(onClick = {
                                    showTargets = !showTargets
                                }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = showTargets,
                                            onCheckedChange = { showTargets = it }
                                        )
                                        Text("Show Targets")
                                    }
                                }
                                // Auto Refresh
                                DropdownMenuItem(onClick = {
                                    autoRefresh = !autoRefresh
                                }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = autoRefresh,
                                            onCheckedChange = { autoRefresh = it }
                                        )
                                        Text("Auto Refresh")
                                    }
                                }
                            }
                        }

                        // Input field for game ID
                        OutlinedTextField(
                            value = gameId,
                            onValueChange = { gameId = it },
                            label = { Text("Game ID") },
                            modifier = Modifier.width(120.dp)
                        )
                    }

                    // Game Info
                    Row(modifier = Modifier.padding(8.dp)) {
                        Text("Current Game: ${currentGame?.gameId ?: "None"}", modifier = Modifier.padding(end = 16.dp))
                        Text("Turn: ${currentGame?.turn?.symbol ?: '-'}", modifier = Modifier.padding(end = 16.dp))
                        Text("State: ${currentGame?.gameState ?: GameState.IN_PROGRESS}", modifier = Modifier.padding(end = 16.dp))
                        val playerColor = handler.getPlayerColor()
                        Text("Player Color: ${playerColor?.symbol ?: '-'}")
                    }

                    // The Board
                    BoardView(
                        game = currentGame,
                        showTargets = showTargets,
                        selectedSquare = selectedSquare,
                        validMoves = validMoves,
                        onSquareClick = { square ->
                            val g = currentGame ?: return@BoardView

                            // If there's a selected square + valid moves, attempt move
                            if (selectedSquare != null && square in validMoves) {
                                val fromStr = selectedSquare.toString()
                                val toStr = square.toString()
                                handler.makeMove(fromStr, toStr)
                                selectedSquare = null
                                validMoves = emptySet()
                            } else {
                                // Otherwise, select/deselect
                                val piece = g.board[square.row.index][square.column.index]
                                if (
                                    piece != null &&
                                    piece.color() == g.turn.color() &&
                                    piece.color() == handler.getPlayerColor()?.color()
                                ) {
                                    // Recalc valid moves
                                    selectedSquare = square
                                    validMoves = g.calculateValidMoves(square, piece)
                                } else {
                                    selectedSquare = null
                                    validMoves = emptySet()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BoardView(
    game: Game?,
    showTargets: Boolean,
    selectedSquare: Square?,
    validMoves: Set<Square>,
    onSquareClick: (Square) -> Unit
) {
    val cellSize = 50.dp

    if (game == null) {
        Text("No game loaded", modifier = Modifier.padding(16.dp))
        return
    }

    Column {
        // Columns label
        Row {
            Spacer(Modifier.width(cellSize))
            for (colIndex in 0 until BOARD_DIM) {
                val c = 'a' + colIndex
                Box(Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                    Text("$c")
                }
            }
        }

        // Board rows
        for (rowIndex in 0 until BOARD_DIM) {
            Row {
                // Row label on left
                Box(Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                    Text("${BOARD_DIM - rowIndex}")
                }

                for (colIndex in 0 until BOARD_DIM) {
                    val square = Square(Row(rowIndex), Column(colIndex))
                    val piece = game.board[rowIndex][colIndex]
                    val isSelected = (square == selectedSquare)
                    val isValid = (square in validMoves)
                    val color = if (square.black) Color.Gray else Color.White
                    val borderColor = when {
                        isSelected -> Color.Green
                        showTargets && isValid -> Color.Red
                        else -> Color.Black
                    }
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .border(BorderStroke(2.dp, borderColor))
                            .background(color)
                            .clickable {
                                onSquareClick(square)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (piece != null) {
                            Text(piece.symbol.toString(), fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    // For Preview, we can pass a no-op for onExit
    CheckersApp(onExit = {})
}

fun main() = application {
    // 1) Provide the `exitApplication()` from this scope
    CheckersApp(
        onExit = ::exitApplication
    )
}
