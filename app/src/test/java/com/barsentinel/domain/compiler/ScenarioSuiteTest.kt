package com.barsentinel.domain.compiler

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Gate G1 at unit-test level. The cases live in [ScenarioSuite] (main
 * source) so the in-app Scenario Tester runs exactly these cases. This test
 * asserts the suite passes 30/30 against the shipped ruleset, plus the
 * engine properties the suite alone does not cover.
 */
class ScenarioSuiteTest {

    @Test
    fun suiteHas30Cases() {
        assertEquals(27, ScenarioSuite.compileCases.size)
        assertEquals(3, ScenarioSuite.invalidationCases.size)
        assertEquals(30, ScenarioSuite.totalCases)
    }

    @Test
    fun all30CasesPass() {
        val outcomes = ScenarioSuite.runAll()
        val failures = outcomes.filter { !it.passed }
        assertTrue(
            "Failing cases: ${failures.joinToString { "${it.id} (${it.detail})" }}",
            failures.isEmpty()
        )
    }

    @Test
    fun conflictingIntervalsResolveToStrictestWithVisibleNote() {
        val input = CompilerInput(
            engagementType = EngagementType.CLIENT_MEETING,
            venueFamiliarity = VenueFamiliarity.KNOWN_VENUE,
            finishesAfterDark = true,
            durationMinutes = 60,
            company = Company.WITH_COLLEAGUE,
            counterparty = Counterparty.PREVIOUSLY_DIFFICULT,
            travel = Travel.OWN_TRANSPORT,
            connectivity = Connectivity.POOR
        )
        val result = EngagementCompiler.compile(input)
        assertEquals(15, result.intervalMinutes)
        assertEquals(1, result.conflictNotes.size)
        assertTrue(result.conflictNotes.single().contains("R-05"))
        assertTrue(result.conflictNotes.single().contains("15 minutes"))
    }

    @Test
    fun compilationIsDeterministic() {
        val input = CompilerInput(
            engagementType = EngagementType.HOME_VISIT,
            venueFamiliarity = VenueFamiliarity.FIRST_VISIT,
            finishesAfterDark = true,
            durationMinutes = 60,
            company = Company.ALONE,
            counterparty = Counterparty.FIRST_CONTACT,
            travel = Travel.OWN_TRANSPORT,
            connectivity = Connectivity.GOOD
        )
        assertEquals(EngagementCompiler.compile(input), EngagementCompiler.compile(input))
    }

    @Test
    fun unansweredCounterpartyIsAValidInput() {
        val input = CompilerInput(
            engagementType = EngagementType.CLIENT_MEETING,
            venueFamiliarity = VenueFamiliarity.KNOWN_VENUE,
            finishesAfterDark = false,
            durationMinutes = 60,
            company = Company.WITH_COLLEAGUE,
            counterparty = null,
            travel = Travel.OWN_TRANSPORT,
            connectivity = Connectivity.GOOD
        )
        val result = EngagementCompiler.compile(input)
        assertTrue(result.precautions.none { it.ruleId == "R-04" || it.ruleId == "R-05" })
    }
}
