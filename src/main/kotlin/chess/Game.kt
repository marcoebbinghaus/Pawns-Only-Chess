package chess

class Game(private val player1: Player, private val player2: Player) {
    private val board = Board(player1, player2)
    private var currentTurn = player1
    private var currentTurnResult = TurnResult.INVALID_MOVE

    fun printBoard() {
        board.printBoard()
    }

    fun executeTurns() {
        do {
            println("${currentTurn.name}'s turn:")
            val input = readln()
            currentTurnResult = board.executeMove(currentTurn, input)
            if (currentTurnResult == TurnResult.SUCCESSFUL_MOVE) {
                printBoard()
                currentTurn = if (currentTurn == player1) player2 else player1
                if (!board.nextTurnPossible(currentTurn)) {
                    throw GameOver("Stalemate!")
                }
            }
        } while (currentTurnResult != TurnResult.EXIT)
    }
}