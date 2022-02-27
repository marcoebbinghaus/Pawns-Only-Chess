package chess

class Board(private val player1: Player, private val player2: Player) {
    private val columns = ('a'..'h').toList()
    private val rows = (1..8).toList()
    private val cells = mutableMapOf<String, Pawn?>()
    private val pawns = mutableListOf<Pawn>()

    init {
        for (column in columns) {
            for (row in rows.asReversed()) {
                val cellID = column.toString() + row.toString()
                val cellContent = when (row) {
                    7 -> Pawn(player2, 'B', cellID)
                    2 -> Pawn(player1, 'W', cellID)
                    else -> null
                }
                if (cellContent != null) {
                    pawns.add(cellContent)
                }
                cells[cellID] = cellContent
            }
        }
    }

    fun printBoard() {
        for (row in rows.asReversed()) {
            for (column in columns) {
                printBorder(column)
            }
            println()
            for (column in columns) {
                printContent(column, row)
            }
            println()
            if (row == 1) {
                for (column in columns) {
                    printBorder(column)
                }
                println()
                for (column in columns) {
                    printColumnIDRow(column)
                }
                println()
            }
        }
    }

    private fun printColumnIDRow(column: Char) {
        when (column) {
            'a' -> print("    $column ")
            'h' -> print("  $column")
            else -> print("  $column ")
        }
    }

    private fun printBorder(column: Char) {
        when (column) {
            'a' -> print("  +---")
            'h' -> print("+---+")
            else -> print("+---")
        }
    }

    private fun printContent(column: Char, row: Int) {
        when (column) {
            'a' -> print("$row | ${contentOf(cells[column.toString() + row.toString()])} ")
            'h' -> print("| ${contentOf(cells[column.toString() + row.toString()])} |")
            else -> print("| ${contentOf(cells[column.toString() + row.toString()])} ")
        }
    }

    private fun contentOf(pawn: Pawn?): Char {
        return if (pawn is Pawn) pawn.symbol else ' '
    }

    fun executeMove(player: Player, input: String): TurnResult {
        if (input == "exit") {
            return TurnResult.EXIT
        }
        val inputInBounds = isStartAndTargetWithinBoardBounds(input)
        if (!inputInBounds) {
            println("Invalid Input")
            return TurnResult.INVALID_MOVE
        }
        val startCell = input.substring(0..1)
        val targetCell = input.substring(2)
        val movingPawn = cells[startCell]
        if (movingPawn == null || movingPawn.player != player) {
            println("No ${if (player == player1) "white" else "black"} pawn at $startCell")
            return TurnResult.INVALID_MOVE
        }
        val forwardMoveCells = fetchPossibleMoveTargetsFor(movingPawn)
        val captureCells = fetchPossibleCaptureCellsFor(movingPawn)
        return if (forwardMoveCells.contains(targetCell)) {
            executeMove(startCell, targetCell, movingPawn)
        } else if (captureCells.contains(targetCell)) {
            executeCaptureMove(startCell, targetCell, movingPawn)
        } else {
            println("Invalid Input")
            return TurnResult.INVALID_MOVE
        }
    }

    private fun isFinalRow(movingPawn: Pawn, targetCell: String): Boolean {
        return if (movingPawn.symbol == 'W') {
            targetCell.matches("[a-h]8".toRegex())
        } else {
            targetCell.matches("[a-h]1".toRegex())
        }
    }

    private fun executeMove(startCell: String, targetCell: String, movingPawn: Pawn): TurnResult {
        val pawnOnTargetCellBeforeMove = cells[targetCell]
        if (pawnOnTargetCellBeforeMove != null) {
            pawns.remove(pawnOnTargetCellBeforeMove)
        }
        cells[startCell] = null
        cells[targetCell] = movingPawn
        movingPawn.movesDone += 1
        pawns.filter { it.player == movingPawn.player }.forEach { it.wasLastMovedPawn = false }
        movingPawn.wasLastMovedPawn = true
        movingPawn.cellID = targetCell
        if (isFinalRow(movingPawn, targetCell) || hasNoMorePawns(if (movingPawn.player == player1) player2 else player1)) {
            printBoard()
            throw GameOver("${if (movingPawn.symbol == 'W') "White" else "Black"} Wins!")
        }
        return TurnResult.SUCCESSFUL_MOVE
    }

    private fun hasNoMorePawns(player: Player): Boolean {
        return pawns.none { it.player == player }
    }

    private fun executeCaptureMove(startCell: String, targetCell: String, movingPawn: Pawn): TurnResult {
        val originalContentOfTargetCell = cells[targetCell]
        if (originalContentOfTargetCell == null) {
            //was en passant
            if (movingPawn.symbol == 'B') {
                cells[targetCell[0].toString() + (targetCell[1].toString().toInt() + 1).toString()] = null
            } else {
                cells[targetCell[0].toString() + (targetCell[1].toString().toInt() - 1).toString()] = null
            }
        }
        return executeMove(startCell, targetCell, movingPawn)
    }

    private fun isStartAndTargetWithinBoardBounds(input: String) = input.matches("[a-h][1-8][a-h][1-8]".toRegex())

    private fun isCellWithinBoardBounds(input: String) = input.matches("[a-h][1-8]".toRegex())

    private fun fetchPossibleMoveTargetsFor(pawn: Pawn): List<String> {
        val startCell = pawn.cellID
        val possibleTargets = mutableListOf<String>()
        val col = startCell[0].toString()
        val row = startCell[1].toString().toInt()
        return if (pawn.symbol == 'B') {
            if (pawn.movesDone == 0) {
                val possibleTargetCell = col + (row - 2).toString()
                if (cells[possibleTargetCell] == null) {
                    possibleTargets.add(possibleTargetCell)
                }
            }
            val oneFieldMoveTarget = col + (row - 1).toString()
            if (cells[oneFieldMoveTarget] == null) {
                possibleTargets.add(oneFieldMoveTarget)
            }
            possibleTargets.toList()
        } else {
            if (pawn.movesDone == 0) {
                val possibleTargetCell = col + (row + 2).toString()
                if (cells[possibleTargetCell] == null) {
                    possibleTargets.add(possibleTargetCell)
                }
            }
            val oneFieldMoveTarget = col + (row + 1).toString()
            if (cells[oneFieldMoveTarget] == null) {
                possibleTargets.add(oneFieldMoveTarget)
            }
            possibleTargets.toList()
        }
    }

    private fun fetchPossibleCaptureCellsFor(pawn: Pawn): List<String> {
        val startCell = pawn.cellID
        val col = startCell[0]
        val row = startCell[1].toString().toInt()
        val possibleCaptureTargets = mutableListOf<String>()
        if (pawn.symbol == 'B') {
            val captureLeftCell = (col - 1).toString() + (row - 1).toString()
            val captureRightCell = (col + 1).toString() + (row - 1).toString()
            if (isCellWithinBoardBounds(captureLeftCell) && isWhitePawnOnTargetCell(captureLeftCell)) {
                possibleCaptureTargets.add(captureLeftCell)
            }
            if (isCellWithinBoardBounds(captureRightCell) && isWhitePawnOnTargetCell(captureRightCell)) {
                possibleCaptureTargets.add(captureRightCell)
            }
            return possibleCaptureTargets.toList()
        } else {
            val captureLeftCell = (col - 1).toString() + (row + 1).toString()
            val captureRightCell = (col + 1).toString() + (row + 1).toString()
            if (isCellWithinBoardBounds(captureLeftCell) && isBlackPawnOnTargetCell(captureLeftCell)) {
                possibleCaptureTargets.add(captureLeftCell)
            }
            if (isCellWithinBoardBounds(captureRightCell) && isBlackPawnOnTargetCell(captureRightCell)) {
                possibleCaptureTargets.add(captureRightCell)
            }
            return possibleCaptureTargets.toList()
        }
    }

    private fun isWhitePawnOnTargetCell(targetCell: String): Boolean {
        val pawn = cells[targetCell]
        val isRealPawnOnTargetCell = pawn is Pawn && pawn.symbol == 'W'
        if (isRealPawnOnTargetCell) {
            return true
        }
        val enPassantCellToCheck = targetCell[0].toString() + (targetCell[1].toString().toInt() + 1).toString()
        val enPassantPawn = cells[enPassantCellToCheck]
        return (enPassantPawn is Pawn && enPassantPawn.wasLastMovedPawn && enPassantPawn.symbol == 'W' && enPassantPawn.movesDone == 1)
    }

    private fun isBlackPawnOnTargetCell(targetCell: String): Boolean {
        val pawn = cells[targetCell]
        val isRealPawnOnTargetCell = pawn is Pawn && pawn.symbol == 'B'
        if (isRealPawnOnTargetCell) {
            return true
        }
        val enPassantCellToCheck = targetCell[0].toString() + (targetCell[1].toString().toInt() - 1).toString()
        val enPassantPawn = cells[enPassantCellToCheck]
        return (enPassantPawn is Pawn && enPassantPawn.wasLastMovedPawn  && enPassantPawn.symbol == 'B' && enPassantPawn.movesDone == 1)
    }

    fun nextTurnPossible(currentTurn: Player): Boolean {
        return pawns.filter { it.player == currentTurn }.any { pawn ->
            val forwardMoveCells = fetchPossibleMoveTargetsFor(pawn)
            val captureCells = fetchPossibleCaptureCellsFor(pawn)
            (forwardMoveCells.isNotEmpty() || captureCells.isNotEmpty())
        }
    }
}