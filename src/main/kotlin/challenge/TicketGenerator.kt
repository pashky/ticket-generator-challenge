package challenge

import kotlin.random.Random

typealias TicketRow = List<Int?>

class Ticket(val rows: List<TicketRow>) {
    private fun Int.format(s: String) = s.format(this)
    override fun toString(): String = rows.joinToString("\n") { row ->
        row.joinToString(" ") { it?.format("%-2d") ?: "  " }
    }
}

class TicketGenerator(val random: Random = Random) {
    inner class TicketBuilder {
        private val columns: Array<MutableList<Int?>> = Array(9) { mutableListOf<Int?>() }
        private fun total() = columns.sumBy { it.size }

        fun isFull() = total() == 15
        fun isColFull(col: Int) = columns[col].size == 3

        fun canAddTo(col: Int) = columns[col].size < 3 && !isFull()
        fun addTo(col: Int, number: Int) = columns[col].add(number)

        fun colsOfSize(predicate: (size: Int) -> Boolean) =
                columns.asSequence().withIndex().filter { predicate(it.value.size) }.map { it.index }.toList()

        fun removeAt(col: Int): Int = columns[col].removeAt(random.nextInt(columns[col].size)) ?: 0

        fun build(): Ticket {
            columns.forEach { it.sortWith(nullsLast()) }

            colsOfSize { it < 3 }.shuffled(random).asSequence().take(4).forEach { columns[it].add(0, null) }
            val toShift = columns.count { it.size > 1 } - 5
            colsOfSize { it == 2 }.shuffled(random).asSequence().take(toShift).forEach { columns[it].add(1, null) }

            columns.forEach { while (it.size < 3) it.add(null) }
            return Ticket((0..2).map { rowNum -> columns.map { it[rowNum] } })
        }
    }

    private fun <T> List<T>.getRandom(random: Random): T = get(random.nextInt(size))

    fun generate(equalSplit: Boolean = false): List<Ticket> {
        val columns =
                if (equalSplit) (1..90).chunked(10)
                else listOf(1..9, 10..19, 20..29, 30..39, 40..49, 50..59, 60..69, 70..79, 80..90)

        val available = columns.map { c -> c.shuffled(random).toMutableList() }.withIndex()
        val ticketBuilders = List(6) { TicketBuilder() }

        ticketBuilders.forEach { ticket ->
            available.forEach { ticket.addTo(it.index, it.value.removeAt(0)) }
        }

        do {
            val leftToPlace = available.map { current ->
                if (current.value.isNotEmpty()) {
                    val nonFull = ticketBuilders.filter { it.canAddTo(current.index) }
                    if (nonFull.isNotEmpty()) {
                        nonFull.getRandom(random).addTo(current.index, current.value.removeAt(0))
                    } else {
                        val ticketWithFullCol = ticketBuilders.filterNot { it.isFull() }.getRandom(random)
                        val ticketFull = ticketBuilders.filterNot { it.isColFull(current.index) }.getRandom(random)
                        val columnToMove =
                                (ticketFull.colsOfSize { it > 1 } intersect ticketWithFullCol.colsOfSize { it < 3 }).toList().getRandom(random)
                        ticketWithFullCol.addTo(columnToMove, ticketFull.removeAt(columnToMove))
                        ticketFull.addTo(current.index, current.value.removeAt(0))
                    }
                }
                current.value.size
            }.sum()
        } while (leftToPlace > 0)

        return ticketBuilders.map { it.build() }
    }
}

fun main() {
    val strips = TicketGenerator().generate(false)
    println(strips.joinToString(separator = "\n--------------------------\n") { it.toString() })
}
