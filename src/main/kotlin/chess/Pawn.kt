package chess

class Pawn(val player: Player, val symbol: Char, var cellID: String) {
    var movesDone = 0
    var wasLastMovedPawn = false
}