package model

enum class Piece(var symbol: Char) {
    WHITE('w'),
    BLACK('b'),
    WHITE_QUEEN('W'),
    BLACK_QUEEN('B');


// Helper to get piece color quickly
fun color(): Piece =
    if (this == WHITE || this == WHITE_QUEEN) WHITE else BLACK
}