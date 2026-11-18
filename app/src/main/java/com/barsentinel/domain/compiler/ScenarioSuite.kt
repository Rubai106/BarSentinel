package com.barsentinel.domain.compiler

/**
 * The 30-case scenario suite (spec §17, Gate G1), defined once and used in
 * two places: the unit tests and the in-app Scenario Tester screen. One
 * definition means the app can never drift from the tests.
 *
 * Composition per the spec: 6 standard court/chamber, 6 home-visit
 * combinations, 4 after-dark, 4 connectivity, 4 counterparty, 3 travel,
 * 3 invalidation. Every case states the expected rule ids, tier and
 * interval. A failing case blocks release.
 */
data class ScenarioCase(
    val id: String,
    val label: String,
    val input: CompilerInput,
    val expectedRules: List<String>,
    val expectedTier: RiskTier,
    val expectedInterval: Int
)

data class InvalidationScenario(
    val id: String,
    val label: String,
    val original: CompilerInput,
    val changed: CompilerInput,
    val buddyChanged: Boolean,
    val expectedChangedFields: List<String>,
    val expectedInvalidated: List<String>,
    val expectedNewlyRequired: List<String>,
    val expectedUnchanged: List<String>,
    val expectedMaterial: Boolean
)

data class ScenarioOutcome(
    val id: String,
    val label: String,
    val passed: Boolean,
    /** Empty when passed; expected-vs-actual detail when failed. */
    val detail: String
)

object ScenarioSuite {

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

    val compileCases: List<ScenarioCase> = listOf(
        // 6 standard court / chamber cases.
        ScenarioCase("C01", "Hearing, known venue, daytime", base(), listOf("R-09"), RiskTier.STANDARD, 30),
        ScenarioCase(
            "C02", "Hearing, group, referred counterparty",
            base(duration = 120, company = Company.GROUP, counterparty = Counterparty.REFERRED),
            listOf("R-09"), RiskTier.STANDARD, 30
        ),
        ScenarioCase(
            "C03", "Chamber meeting, own chamber",
            base(type = EngagementType.CHAMBER_MEETING, venue = VenueFamiliarity.OWN_CHAMBER, duration = 45),
            emptyList(), RiskTier.STANDARD, 30
        ),
        ScenarioCase(
            "C04", "Chamber meeting, with client only",
            base(
                type = EngagementType.CHAMBER_MEETING,
                venue = VenueFamiliarity.OWN_CHAMBER,
                duration = 90,
                company = Company.WITH_CLIENT_ONLY
            ),
            emptyList(), RiskTier.STANDARD, 30
        ),
        ScenarioCase("C05", "Hearing, 4 hours", base(duration = 240), listOf("R-09", "R-10"), RiskTier.STANDARD, 30),
        ScenarioCase("C06", "Hearing, alone, known venue", base(company = Company.ALONE), listOf("R-09"), RiskTier.STANDARD, 30),

        // 6 home-visit combinations.
        ScenarioCase(
            "H01", "Home visit, first visit, alone, first contact",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                company = Company.ALONE,
                counterparty = Counterparty.FIRST_CONTACT
            ),
            listOf("R-03", "R-04"), RiskTier.ELEVATED, 30
        ),
        ScenarioCase(
            "H02", "Home visit, other premises, alone",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.OTHER_PREMISES,
                company = Company.ALONE
            ),
            listOf("R-01"), RiskTier.ELEVATED, 30
        ),
        ScenarioCase(
            "H03", "Home visit, first visit, alone, after dark",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                afterDark = true,
                company = Company.ALONE,
                counterparty = Counterparty.REFERRED
            ),
            listOf("R-02", "R-03", "R-07"), RiskTier.HIGH, 20
        ),
        ScenarioCase(
            "H04", "Home visit, other premises, alone, after dark, first contact",
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
        ScenarioCase(
            "H05", "Home visit, known venue, with colleague",
            base(type = EngagementType.HOME_VISIT),
            emptyList(), RiskTier.STANDARD, 30
        ),
        ScenarioCase(
            "H06", "Home visit, first visit, over 3 hours, first contact",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                duration = 200,
                counterparty = Counterparty.FIRST_CONTACT
            ),
            listOf("R-03", "R-04", "R-10"), RiskTier.STANDARD, 30
        ),

        // 4 after-dark variations.
        ScenarioCase(
            "D01", "Client meeting, after dark",
            base(type = EngagementType.CLIENT_MEETING, afterDark = true),
            listOf("R-02"), RiskTier.ELEVATED, 20
        ),
        ScenarioCase(
            "D02", "Site visit, first visit, after dark",
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
        ScenarioCase(
            "D03", "Chamber meeting, alone, after dark",
            base(
                type = EngagementType.CHAMBER_MEETING,
                venue = VenueFamiliarity.OWN_CHAMBER,
                afterDark = true,
                duration = 45,
                company = Company.ALONE
            ),
            listOf("R-02"), RiskTier.ELEVATED, 20
        ),
        ScenarioCase(
            "D04", "Other engagement, other premises, after dark",
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
        ScenarioCase(
            "N01", "Client meeting, poor connectivity",
            base(type = EngagementType.CLIENT_MEETING, connectivity = Connectivity.POOR),
            listOf("R-08"), RiskTier.STANDARD, 20
        ),
        ScenarioCase(
            "N02", "Site visit, first visit, unknown connectivity",
            base(
                type = EngagementType.SITE_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                duration = 90,
                counterparty = Counterparty.REFERRED,
                connectivity = Connectivity.UNKNOWN
            ),
            listOf("R-03", "R-08"), RiskTier.STANDARD, 20
        ),
        ScenarioCase(
            "N03", "Home visit, other premises, alone, poor connectivity",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.OTHER_PREMISES,
                company = Company.ALONE,
                connectivity = Connectivity.POOR
            ),
            listOf("R-01", "R-08"), RiskTier.ELEVATED, 20
        ),
        ScenarioCase(
            "N04", "Hearing, unknown connectivity",
            base(connectivity = Connectivity.UNKNOWN),
            listOf("R-08", "R-09"), RiskTier.STANDARD, 20
        ),

        // 4 counterparty variations.
        ScenarioCase(
            "P01", "Client meeting, first contact",
            base(type = EngagementType.CLIENT_MEETING, counterparty = Counterparty.FIRST_CONTACT),
            listOf("R-04"), RiskTier.STANDARD, 30
        ),
        ScenarioCase(
            "P02", "Client meeting, previously difficult",
            base(type = EngagementType.CLIENT_MEETING, counterparty = Counterparty.PREVIOUSLY_DIFFICULT),
            listOf("R-05"), RiskTier.ELEVATED, 15
        ),
        ScenarioCase(
            "P03", "Client meeting, other premises, alone, previously difficult",
            base(
                type = EngagementType.CLIENT_MEETING,
                venue = VenueFamiliarity.OTHER_PREMISES,
                company = Company.ALONE,
                counterparty = Counterparty.PREVIOUSLY_DIFFICULT
            ),
            listOf("R-01", "R-05"), RiskTier.ELEVATED, 15
        ),
        ScenarioCase(
            "P04", "Client meeting, first visit, alone, first contact",
            base(
                type = EngagementType.CLIENT_MEETING,
                venue = VenueFamiliarity.FIRST_VISIT,
                company = Company.ALONE,
                counterparty = Counterparty.FIRST_CONTACT
            ),
            listOf("R-03", "R-04"), RiskTier.ELEVATED, 30
        ),

        // 3 travel variations.
        ScenarioCase(
            "T01", "Site visit, counterparty-arranged travel",
            base(
                type = EngagementType.SITE_VISIT,
                duration = 120,
                travel = Travel.ARRANGED_BY_COUNTERPARTY
            ),
            listOf("R-06"), RiskTier.ELEVATED, 30
        ),
        ScenarioCase(
            "T02", "Home visit, first visit, counterparty-arranged travel",
            base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.FIRST_VISIT,
                counterparty = Counterparty.REFERRED,
                travel = Travel.ARRANGED_BY_COUNTERPARTY
            ),
            listOf("R-03", "R-06"), RiskTier.ELEVATED, 30
        ),
        ScenarioCase(
            "T03", "Client meeting, after dark, difficult, arranged travel, poor signal",
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

    val invalidationCases: List<InvalidationScenario> = listOf(
        InvalidationScenario(
            "V01", "End time moves after dark",
            original = base(),
            changed = base(afterDark = true),
            buddyChanged = false,
            expectedChangedFields = listOf("time"),
            expectedInvalidated = emptyList(),
            expectedNewlyRequired = listOf("R-02"),
            expectedUnchanged = listOf("R-09"),
            expectedMaterial = true
        ),
        InvalidationScenario(
            "V02", "Company changes to alone",
            original = base(
                type = EngagementType.CHAMBER_MEETING,
                venue = VenueFamiliarity.OWN_CHAMBER,
                duration = 45
            ),
            changed = base(
                type = EngagementType.CHAMBER_MEETING,
                venue = VenueFamiliarity.OWN_CHAMBER,
                duration = 45,
                company = Company.ALONE
            ),
            buddyChanged = false,
            expectedChangedFields = listOf("company"),
            expectedInvalidated = emptyList(),
            expectedNewlyRequired = listOf("R-11"),
            expectedUnchanged = emptyList(),
            expectedMaterial = true
        ),
        InvalidationScenario(
            "V03", "Venue becomes a known venue",
            original = base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.OTHER_PREMISES,
                company = Company.ALONE
            ),
            changed = base(
                type = EngagementType.HOME_VISIT,
                venue = VenueFamiliarity.KNOWN_VENUE,
                company = Company.ALONE
            ),
            buddyChanged = false,
            expectedChangedFields = listOf("venue"),
            expectedInvalidated = listOf("R-01"),
            expectedNewlyRequired = emptyList(),
            expectedUnchanged = emptyList(),
            expectedMaterial = true
        )
    )

    /** Total cases in the suite: 27 compile + 3 invalidation = 30. */
    val totalCases: Int get() = compileCases.size + invalidationCases.size

    /** Runs every case against the shipped ruleset and returns per-case outcomes. */
    fun runAll(): List<ScenarioOutcome> {
        val outcomes = compileCases.map { runCompileCase(it) } +
            invalidationCases.map { runInvalidationCase(it) }
        return outcomes
    }

    fun runCompileCase(case: ScenarioCase): ScenarioOutcome {
        val result = EngagementCompiler.compile(case.input)
        val actualRules = result.precautions.map { it.ruleId }
        val problems = mutableListOf<String>()
        if (actualRules != case.expectedRules) {
            problems += "rules: expected ${case.expectedRules}, got $actualRules"
        }
        if (result.tier != case.expectedTier) {
            problems += "tier: expected ${case.expectedTier}, got ${result.tier}"
        }
        if (result.intervalMinutes != case.expectedInterval) {
            problems += "interval: expected ${case.expectedInterval}, got ${result.intervalMinutes}"
        }
        if (result.rulesetVersion != EngagementCompiler.RULESET_VERSION) {
            problems += "ruleset: expected ${EngagementCompiler.RULESET_VERSION}, got ${result.rulesetVersion}"
        }
        result.precautions.forEach { precaution ->
            if (precaution.triggerText.isBlank() || precaution.action.isBlank()) {
                problems += "${precaution.ruleId} is missing its explanation"
            }
        }
        return ScenarioOutcome(case.id, case.label, problems.isEmpty(), problems.joinToString("; "))
    }

    fun runInvalidationCase(case: InvalidationScenario): ScenarioOutcome {
        val diff = Invalidation.diff(
            case.original,
            case.changed,
            EngagementCompiler.compile(case.original),
            buddyChanged = case.buddyChanged
        )
        val problems = mutableListOf<String>()
        if (diff.changedFields != case.expectedChangedFields) {
            problems += "changed fields: expected ${case.expectedChangedFields}, got ${diff.changedFields}"
        }
        if (diff.invalidated.map { it.ruleId } != case.expectedInvalidated) {
            problems += "invalidated: expected ${case.expectedInvalidated}, got ${diff.invalidated.map { it.ruleId }}"
        }
        if (diff.newlyRequired.map { it.ruleId } != case.expectedNewlyRequired) {
            problems += "newly required: expected ${case.expectedNewlyRequired}, got ${diff.newlyRequired.map { it.ruleId }}"
        }
        if (diff.unchanged.map { it.ruleId } != case.expectedUnchanged) {
            problems += "unchanged: expected ${case.expectedUnchanged}, got ${diff.unchanged.map { it.ruleId }}"
        }
        if (diff.isMaterial != case.expectedMaterial) {
            problems += "material: expected ${case.expectedMaterial}, got ${diff.isMaterial}"
        }
        return ScenarioOutcome(case.id, case.label, problems.isEmpty(), problems.joinToString("; "))
    }
}
