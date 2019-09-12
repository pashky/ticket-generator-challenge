package challenge

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketGeneratorTest {
    private fun Strip.isValid() = rows.all { row -> row.count { it != null } == 5 }
            && (0..8).all { x -> (0..2).count { y -> rows[y][x] != null } in 1..3 }

    private fun Strip.allNumbers() = rows.asSequence().flatMap { row -> row.asSequence().filterNotNull() }.toList()

    @Test fun testGeneratedTicketsAreValid() {
        val random = Random
        for (i in 1..100000) {
            val strips = TicketGenerator(random).generate()
            assertTrue(strips.all { strip -> strip.isValid() })
            assertEquals((1..90).toList(), strips.flatMap { strip -> strip.allNumbers() }.sorted())
        }
    }
}
