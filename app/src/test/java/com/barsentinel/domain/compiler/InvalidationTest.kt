package com.barsentinel.domain.compiler

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The 3 pure invalidation cases of the 30-case suite (spec §17), plus the
 * R-12 buddy-change rule. Invalidation names the changed field for every
 * affected precaution, and a material change requires re-acceptance.
 */
class InvalidationTest {

    private fun base(
        type: EngagementType = EngagementType.COURT_HEARING,
        venue: VenueFamiliarity = VenueFamiliarity.KNOWN_VENUE,
        afterDark: Boolean = false,
        duration: Int = 60,
        company: Company = Company.WITH_COLLEAGUE,
        counterparty: Counterparty? = Counterparty.KNOWN,
        travel: Travel = Travel.OWN_TRANSPORT,
        connectivity: Connectivity = Connectivity.GOOD
    ) = CompilerInput(type, venue, afterDark, duration, company, counterparty, travel, connectivity)

    @Test
    fun movingEndTimeAfterDarkAddsR02AndIsMaterial() {
        val original = base()
        val changed = base(afterDark = true)
        val diff = Invalidation.diff(
            original,
            changed,
            EngagementCompiler.compile(original),
            buddyChanged = false
        )
        assertEquals(listOf("time"), diff.changedFields)
        assertTrue(diff.invalidated.isEmpty())
        assertEquals(listOf("R-02"), diff.newlyRequired.map { it.ruleId })
        assertEquals(listOf("R-09"), diff.unchanged.map { it.ruleId })
        assertTrue(diff.isMaterial)
        assertTrue(diff.reacceptanceRequired)
    }

    @Test
    fun companyChangingToAloneFiresR11AndIsMaterial() {
        val original = base(
            type = EngagementType.CHAMBER_MEETING,
            venue = VenueFamiliarity.OWN_CHAMBER,
            duration = 45
        )
        val changed = original.copy(company = Company.ALONE)
        val diff = Invalidation.diff(
            original,
            changed,
            EngagementCompiler.compile(original),
            buddyChanged = false
        )
        assertEquals(listOf("company"), diff.changedFields)
        assertEquals(listOf("R-11"), diff.newlyRequired.map { it.ruleId })
        assertTrue(diff.isMaterial)
        assertTrue(diff.reacceptanceRequired)
    }

    @Test
    fun venueBecomingKnownInvalidatesR01AndIsMaterial() {
        val original = base(
            type = EngagementType.HOME_VISIT,
            venue = VenueFamiliarity.OTHER_PREMISES,
            company = Company.ALONE
        )
        val changed = original.copy(venueFamiliarity = VenueFamiliarity.KNOWN_VENUE)
        val diff = Invalidation.diff(
            original,
            changed,
            EngagementCompiler.compile(original),
            buddyChanged = false
        )
        assertEquals(listOf("venue"), diff.changedFields)
        assertEquals(listOf("R-01"), diff.invalidated.map { it.ruleId })
        assertTrue(diff.newlyRequired.isEmpty())
        assertTrue(diff.isMaterial)
        assertTrue(diff.reacceptanceRequired)
    }

    @Test
    fun buddyChangeFiresR12AndVoidsAcceptance() {
        val original = base(
            type = EngagementType.HOME_VISIT,
            venue = VenueFamiliarity.OTHER_PREMISES,
            company = Company.ALONE
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
        val original = base()
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
