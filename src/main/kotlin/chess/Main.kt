package chess

fun main() {
    println("Pawns-Only Chess")
    println("First Player's name:")
    val name1 = readln()
    println("Second Player's name:")
    val name2 = readln()
    val game = Game(Player(name1), Player(name2))
    game.printBoard()
    try {
        game.executeTurns()
    } catch (gameOver: GameOver) {
        println(gameOver.message)
    }
    println("Bye!")
}