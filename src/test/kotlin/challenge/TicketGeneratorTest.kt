package challenge

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketGeneratorTest {
    private fun Ticket.isValid() = rows.all { row -> row.count { it != null } == 5 }
            && (0..8).all { x -> (0..2).count { y -> rows[y][x] != null } in 1..3 }

    private fun Ticket.allNumbers() = rows.asSequence().flatMap { row -> row.asSequence().filterNotNull() }.toList()

    @Test fun testGeneratedTicketsAreValid() {
        val random = Random
        for (i in 1..100000) {
            for(strip in arrayOf(TicketGenerator(random).generate(), TicketGenerator(random).generate(true))) {
                assertTrue(strip.all { it.isValid() })
                assertEquals((1..90).toList(), strip.flatMap { it.allNumbers() }.sorted())
            }
        }
    }
}
