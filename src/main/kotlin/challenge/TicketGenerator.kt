package challenge

import kotlin.random.Random

typealias StripRow = List<Int?>

class Strip(val rows: List<StripRow>) {
    private fun Int.format(s: String) = s.format(this)
    override fun toString(): String = rows.joinToString("\n") { row ->
        row.joinToString(" ") { it?.format("%-2d") ?: "  " }
    }
}

class TicketGenerator(val random: Random = Random) {
    inner class StripBuilder {
        private val columns: Array<MutableList<Int?>> = Array(9) { mutableListOf<Int?>() }
        private fun total() = columns.sumBy { it.size }

        fun isFull() = total() == 15
        fun isColFull(col: Int) = columns[col].size == 3
        fun canAddTo(col: Int) = columns[col].size < 3 && !isFull()
        fun addTo(col: Int, number: Int) = columns[col].add(number)

        fun colsOfSize(predicate: (size: Int) -> Boolean) =
                columns.asSequence().withIndex().filter { predicate(it.value.size) }.map { it.index }.toList()

        fun removeAt(col: Int): Int = columns[col].removeAt(random.nextInt(columns[col].size)) ?: 0

        fun build(): Strip {
            columns.forEach { it.sortWith(nullsLast()) }

            colsOfSize { it < 3 }.shuffled(random).asSequence().take(4).forEach { columns[it].add(0, null) }
            val toShift = columns.count { it.size > 1 } - 5
            colsOfSize { it == 2 }.shuffled(random).asSequence().take(toShift).forEach { columns[it].add(1, null) }

            columns.forEach { while (it.size < 3) it.add(null) }
            return Strip((0..2).map { rowNum -> columns.map { it[rowNum] } })
        }
    }

    private fun <T> List<T>.getRandom(random: Random): T = get(random.nextInt(size))

    fun generate(equalSplit: Boolean = false): List<Strip> {
        val columns =
                if (equalSplit) (1..90).chunked(10)
                else listOf(1..9, 10..19, 20..29, 30..39, 40..49, 50..59, 60..69, 70..79, 80..90)

        val available = columns.map { c -> c.shuffled(random).toMutableList() }.withIndex()
        val stripBuilders = List(6) { StripBuilder() }

        stripBuilders.forEach { strip ->
            available.forEach { strip.addTo(it.index, it.value.removeAt(0)) }
        }

        do {
            val haveLeftToPlace = available.map { currentCol ->
                if (currentCol.value.isNotEmpty()) {
                    val nonFull = stripBuilders.filter { it.canAddTo(currentCol.index) }
                    if (nonFull.isNotEmpty()) {
                        nonFull.getRandom(random).addTo(currentCol.index, currentCol.value.removeAt(0))
                    } else {
                        val stripWithFullCol = stripBuilders.filterNot { it.isFull() }.getRandom(random)
                        val stripFull = stripBuilders.filterNot { it.isColFull(currentCol.index) }.getRandom(random)
                        val colToMove =
                                (stripFull.colsOfSize { it > 1 } intersect stripWithFullCol.colsOfSize { it < 3 }).toList().getRandom(random)
                        stripWithFullCol.addTo(colToMove, stripFull.removeAt(colToMove))
                        stripFull.addTo(currentCol.index, currentCol.value.removeAt(0))
                    }
                }
                currentCol.value.size
            }.sum()
        } while (haveLeftToPlace > 0)

        return stripBuilders.map { it.build() }
    }

}

fun main() {
    val strips = TicketGenerator().generate(false)
    println(strips.joinToString(separator = "\n--------------------------\n") { it.toString() })
}
