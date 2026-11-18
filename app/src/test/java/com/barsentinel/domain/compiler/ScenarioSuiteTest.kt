package com.barsentinel.domain.compiler

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The 30-case scenario suite (spec §17, Gate G1): 27 compile cases here plus
 * 3 invalidation cases in [InvalidationTest]. Composition per the spec: 6
 * standard court/chamber, 6 home-visit combinations, 4 after-dark, 4
 * connectivity, 4 counterparty, 3 travel, 3 invalidation. Every case states
 * the expected rule ids, tier and interval. A failing case blocks release.
 * Results run against ruleset [EngagementCompiler.RULESET_VERSION].
 */
class ScenarioSuiteTest {

    private data class Case(
        val id: String,
        val input: CompilerInput,
        val expectedRules: List<String>,
        val expectedTier: RiskTier,
        val expectedInterval: Int
    )

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

    private val cases = listOf(
        // 6 standard court / chamber cases.
        Case("C01", base(), listOf("R-09"), RiskTier.STANDARD, 30),
        Case(
            "C02",
            base(duration = 120, company = Company.GROUP, counterparty = Counterparty.REFERRED),
            listOf("R-09"), RiskTier.STANDARD, 30
        ),
        Case(
            "C03",
            base(type = EngagementType.CHAMBER_MEETING, venue = VenueFamiliarity.OWN_CHAMBER, duration = 45),
            emptyList(), RiskTier.STANDARD, 30
        ),
        Case(
            "C04",
            base(
                type = EngagementType.CHAMBER_MEETING,
                venue = VenueFamiliarity.OWN_CHAMBER,
                duration = 90,
                company = Company.WITH_CLIENT_ONLY
            ),
            emptyList(), RiskTier.STANDARD, 30
        ),
        Case("C05", base(duration = 240), listOf("R-09", "R-10"), RiskTier.STANDARD, 30),
        Case("C06", base(company = Company.ALONE), listOf("R-09"), RiskTier.STANDARD, 30),

        // 6 home-visit combinations.
        Case(
            "H01",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                company = Company.ALONE,
                counterparty = Counterparty.FIRST_CONTACT
            ),
            listOf("R-03", "R-04"), RiskTier.ELEVATED, 30
        ),
        Case(
            "H02",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.OTHER_PREMISES,
                company = Company.ALONE
            ),
            listOf("R-01"), RiskTier.ELEVATED, 30
        ),
        Case(
            "H03",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                afterDark = true,
                company = Company.ALONE,
                counterparty = Counterparty.REFERRED
            ),
            listOf("R-02", "R-03", "R-07"), RiskTier.HIGH, 20
        ),
        Case(
            "H04",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.OTHER_PREMISES,
                afterDark = true,
                duration = 90,
                company = Company.ALONE,
                counterparty = Counterparty.FIRST_CONTACT
            ),
            listOf("R-01", "R-02", "R-04", "R-07"), RiskTier.HIGH, 20
        ),
        Case(
            "H05",
            base(type = EngagementType.HOME_VISIT),
            emptyList(), RiskTier.STANDARD, 30
        ),
        Case(
            "H06",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                duration = 200,
                counterparty = Counterparty.FIRST_CONTACT
            ),
            listOf("R-03", "R-04", "R-10"), RiskTier.STANDARD, 30
        ),

        // 4 after-dark variations.
        Case(
            "D01",
            base(type = EngagementType.CLIENT_MEETING, afterDark = true),
            listOf("R-02"), RiskTier.ELEVATED, 20
        ),
        Case(
            "D02",
            base(
                type = EngagementType.SITE_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                afterDark = true,
                duration = 120,
                counterparty = Counterparty.REFERRED,
                travel = Travel.RIDE_HAILING
            ),
            listOf("R-02", "R-03"), RiskTier.ELEVATED, 20
        ),
        Case(
            "D03",
            base(
                type = EngagementType.CHAMBER_MEETING,
                venue = VenueFamiliarity.OWN_CHAMBER,
                afterDark = true,
                duration = 45,
                company = Company.ALONE
            ),
            listOf("R-02"), RiskTier.ELEVATED, 20
        ),
        Case(
            "D04",
            base(
                type = EngagementType.OTHER,
                venue = VenueFamiliarity.OTHER_PREMISES,
                afterDark = true,
                company = Company.WITH_CLIENT_ONLY,
                travel = Travel.PUBLIC_TRANSPORT
            ),
            listOf("R-02"), RiskTier.ELEVATED, 20
        ),

        // 4 connectivity variations.
        Case(
            "N01",
            base(type = EngagementType.CLIENT_MEETING, connectivity = Connectivity.POOR),
            listOf("R-08"), RiskTier.STANDARD, 20
        ),
        Case(
            "N02",
            base(
                type = EngagementType.SITE_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                duration = 90,
                counterparty = Counterparty.REFERRED,
                connectivity = Connectivity.UNKNOWN
            ),
            listOf("R-03", "R-08"), RiskTier.STANDARD, 20
        ),
        Case(
            "N03",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.OTHER_PREMISES,
                company = Company.ALONE,
                connectivity = Connectivity.POOR
            ),
            listOf("R-01", "R-08"), RiskTier.ELEVATED, 20
        ),
        Case(
            "N04",
            base(connectivity = Connectivity.UNKNOWN),
            listOf("R-08", "R-09"), RiskTier.STANDARD, 20
        ),

        // 4 counterparty variations.
        Case(
            "P01",
            base(type = EngagementType.CLIENT_MEETING, counterparty = Counterparty.FIRST_CONTACT),
            listOf("R-04"), RiskTier.STANDARD, 30
        ),
        Case(
            "P02",
            base(type = EngagementType.CLIENT_MEETING, counterparty = Counterparty.PREVIOUSLY_DIFFICULT),
            listOf("R-05"), RiskTier.ELEVATED, 15
        ),
        Case(
            "P03",
            base(
                type = EngagementType.CLIENT_MEETING,
                venue = VenueFamiliarity.OTHER_PREMISES,
                company = Company.ALONE,
                counterparty = Counterparty.PREVIOUSLY_DIFFICULT
            ),
            listOf("R-01", "R-05"), RiskTier.ELEVATED, 15
        ),
        Case(
            "P04",
            base(
                type = EngagementType.CLIENT_MEETING,
                venue = VenueFamiliarity.FIRST_VISIT,
                company = Company.ALONE,
                counterparty = Counterparty.FIRST_CONTACT
            ),
            listOf("R-03", "R-04"), RiskTier.ELEVATED, 30
        ),

        // 3 travel variations.
        Case(
            "T01",
            base(
                type = EngagementType.SITE_VISIT,
                duration = 120,
                travel = Travel.ARRANGED_BY_COUNTERPARTY
            ),
            listOf("R-06"), RiskTier.ELEVATED, 30
        ),
        Case(
            "T02",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                counterparty = Counterparty.REFERRED,
                travel = Travel.ARRANGED_BY_COUNTERPARTY
            ),
            listOf("R-03", "R-06"), RiskTier.ELEVATED, 30
        ),
        Case(
            "T03",
            base(
                type = EngagementType.CLIENT_MEETING,
                afterDark = true,
                counterparty = Counterparty.PREVIOUSLY_DIFFICULT,
                travel = Travel.ARRANGED_BY_COUNTERPARTY,
                connectivity = Connectivity.POOR
            ),
            listOf("R-02", "R-05", "R-06", "R-08"), RiskTier.ELEVATED, 15
        )
    )

    @Test
    fun suiteHas27CompileCases() {
        assertEquals(27, cases.size)
    }

    @Test
    fun allCasesProduceExpectedRulesTierAndInterval() {
        cases.forEach { case ->
            val result = EngagementCompiler.compile(case.input)
            assertEquals(
                "Case ${case.id}: rule ids",
                case.expectedRules,
                result.precautions.map { it.ruleId }
            )
            assertEquals("Case ${case.id}: tier", case.expectedTier, result.tier)
            assertEquals("Case ${case.id}: interval", case.expectedInterval, result.intervalMinutes)
            assertEquals("Case ${case.id}: ruleset", EngagementCompiler.RULESET_VERSION, result.rulesetVersion)
            result.precautions.forEach { precaution ->
                assertTrue("Case ${case.id}: ${precaution.ruleId} needs trigger text", precaution.triggerText.isNotBlank())
                assertTrue("Case ${case.id}: ${precaution.ruleId} needs an action", precaution.action.isNotBlank())
            }
        }
    }

    @Test
    fun conflictingIntervalsResolveToStrictestWithVisibleNote() {
        val input = base(
            type = EngagementType.CLIENT_MEETING,
            afterDark = true,
            counterparty = Counterparty.PREVIOUSLY_DIFFICULT,
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
        val input = base(
            type = EngagementType.HOME_VISIT,
            venue = VenueFamiliarity.FIRST_VISIT,
            afterDark = true,
            company = Company.ALONE,
            counterparty = Counterparty.FIRST_CONTACT
        )
        assertEquals(EngagementCompiler.compile(input), EngagementCompiler.compile(input))
    }

    @Test
    fun unansweredCounterpartyIsAValidInput() {
        val input = base(type = EngagementType.CLIENT_MEETING, counterparty = null)
        val result = EngagementCompiler.compile(input)
        assertTrue(result.precautions.none { it.ruleId == "R-04" || it.ruleId == "R-05" })
    }
}
