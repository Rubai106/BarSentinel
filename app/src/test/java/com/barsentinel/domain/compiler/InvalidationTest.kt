package com.barsentinel.domain.compiler

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Invalidation behaviour (spec §7). The 3 suite cases run through
 * [ScenarioSuite]; the buddy-change rule (R-12) and the no-change case are
 * covered here as additional unit tests.
 */
class InvalidationTest {

    @Test
    fun suiteInvalidationCasesPass() {
        val outcomes = ScenarioSuite.invalidationCases.map { ScenarioSuite.runInvalidationCase(it) }
        val failures = outcomes.filter { !it.passed }
        assertTrue(
            "Failing cases: ${failures.joinToString { "${it.id} (${it.detail})" }}",
            failures.isEmpty()
        )
    }

    @Test
    fun buddyChangeFiresR12AndVoidsAcceptance() {
        val original = CompilerInput(
            engagementType = EngagementType.HOME_VISIT,
            venueFamiliarity = VenueFamiliarity.OTHER_PREMISES,
            finishesAfterDark = false,
            durationMinutes = 60,
            company = Company.ALONE,
            counterparty = Counterparty.KNOWN,
            travel = Travel.OWN_TRANSPORT,
            connectivity = Connectivity.GOOD
        )
        val diff = Invalidation.diff(
            original,
            original,
            EngagementCompiler.compile(original),
            buddyChanged = true
        )
        assertEquals(listOf("buddy"), diff.changedFields)
        assertEquals(listOf("R-12"), diff.newlyRequired.map { it.ruleId })
        assertTrue(diff.isMaterial)
        assertTrue(diff.reacceptanceRequired)
    }

    @Test
    fun noChangeProducesAnEmptyImmaterialDiff() {
        val original = CompilerInput(
            engagementType = EngagementType.COURT_HEARING,
            venueFamiliarity = VenueFamiliarity.KNOWN_VENUE,
            finishesAfterDark = false,
            durationMinutes = 60,
            company = Company.WITH_COLLEAGUE,
            counterparty = Counterparty.KNOWN,
            travel = Travel.OWN_TRANSPORT,
            connectivity = Connectivity.GOOD
        )
        val diff = Invalidation.diff(
            original,
            original,
            EngagementCompiler.compile(original),
            buddyChanged = false
        )
        assertTrue(diff.changedFields.isEmpty())
        assertTrue(diff.invalidated.isEmpty())
        assertTrue(diff.newlyRequired.isEmpty())
        assertFalse(diff.isMaterial)
        assertFalse(diff.reacceptanceRequired)
    }
}
