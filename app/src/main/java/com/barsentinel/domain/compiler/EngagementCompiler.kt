package com.barsentinel.domain.compiler

/**
 * The deterministic, versioned rules engine (spec §7, §16). Same inputs plus
 * the same ruleset version always give the same output — no model, no
 * network call. Every precaution cites the exact condition that triggered
 * it, and every rule is covered by the 30-case scenario suite
 * (app/src/test/.../ScenarioSuiteTest.kt, Gate G1).
 *
 * Ruleset 1.0.0 ships R-01 to R-10 as compile-time rules. R-11 and R-12 are
 * invalidation rules: they fire only when an input changes after compilation
 * and live in [Invalidation].
 */
object EngagementCompiler {

    const val RULESET_VERSION = "1.0.0"
    const val DEFAULT_INTERVAL_MINUTES = 30

    fun compile(input: CompilerInput): CompileResult {
        val precautions = mutableListOf<Precaution>()
        // (ruleId, intervalMinutes) candidates; the strictest wins.
        val intervalCandidates = mutableListOf<Pair<String, Int>>()
        var tierBump = false
        var highTier = false

        // R-01: other person's premises + alone → buddy acceptance required.
        if (input.venueFamiliarity == VenueFamiliarity.OTHER_PREMISES &&
            input.company == Company.ALONE
        ) {
            precautions += Precaution(
                ruleId = "R-01",
                level = PrecautionLevel.REQUIRED,
                action = "Assign a buddy and require acceptance before start",
                triggerText = "This engagement is at another person's home or office, alone."
            )
        }

        // R-02: finishes after dark → shorter interval, return transport confirmed.
        if (input.finishesAfterDark) {
            precautions += Precaution(
                ruleId = "R-02",
                level = PrecautionLevel.REQUIRED,
                action = "Shorten the check-in interval; confirm return transport before start",
                triggerText = "The engagement finishes after dark."
            )
            intervalCandidates += "R-02" to 20
        }

        // R-03: first visit → share the area, save the venue contact offline.
        if (input.venueFamiliarity == VenueFamiliarity.FIRST_VISIT) {
            precautions += Precaution(
                ruleId = "R-03",
                level = PrecautionLevel.RECOMMENDED,
                action = "Share the venue area with your buddy in the envelope; save the venue contact number offline",
                triggerText = "This is a first visit to this venue."
            )
        }

        // R-04: first contact → colleague present or a public/known venue.
        // Alone at a private venue escalates the tier.
        if (input.counterparty == Counterparty.FIRST_CONTACT) {
            precautions += Precaution(
                ruleId = "R-04",
                level = PrecautionLevel.RECOMMENDED,
                action = "Meet with a colleague present or choose a public or known venue",
                triggerText = "The counterparty is a first contact."
            )
            if (input.company == Company.ALONE &&
                input.venueFamiliarity in setOf(
                    VenueFamiliarity.FIRST_VISIT,
                    VenueFamiliarity.OTHER_PREMISES
                )
            ) {
                tierBump = true
            }
        }

        // R-05: previously difficult → buddy + second contact, shortest interval.
        if (input.counterparty == Counterparty.PREVIOUSLY_DIFFICULT) {
            precautions += Precaution(
                ruleId = "R-05",
                level = PrecautionLevel.REQUIRED,
                action = "Require buddy acceptance plus a second contact; set the shortest interval",
                triggerText = "The counterparty has previously been difficult."
            )
            intervalCandidates += "R-05" to 15
        }

        // R-06: counterparty-arranged travel → independent return transport.
        if (input.travel == Travel.ARRANGED_BY_COUNTERPARTY) {
            precautions += Precaution(
                ruleId = "R-06",
                level = PrecautionLevel.REQUIRED,
                action = "Arrange independent return transport; record it in the envelope",
                triggerText = "Travel is arranged by the counterparty."
            )
        }

        // R-07: home visit + alone + after dark → escrow on, tier High.
        if (input.engagementType == EngagementType.HOME_VISIT &&
            input.company == Company.ALONE &&
            input.finishesAfterDark
        ) {
            precautions += Precaution(
                ruleId = "R-07",
                level = PrecautionLevel.REQUIRED,
                action = "Enable session location escrow; buddy acceptance mandatory",
                triggerText = "A home visit, alone, finishing after dark."
            )
            highTier = true
        }

        // R-08: poor or unknown connectivity → SMS ladder pre-loaded, shorter interval.
        if (input.connectivity == Connectivity.POOR || input.connectivity == Connectivity.UNKNOWN) {
            precautions += Precaution(
                ruleId = "R-08",
                level = PrecautionLevel.RECOMMENDED,
                action = "Pre-load the SMS ladder; shorten the interval; download venue contacts offline",
                triggerText = "Connectivity at the venue is ${input.connectivity.name.lowercase()}."
            )
            intervalCandidates += "R-08" to 20
        }

        // R-09: hearing at a known venue (a court complex is a known venue) →
        // standard defaults; buddy optional unless another rule fires.
        if (input.engagementType == EngagementType.COURT_HEARING &&
            input.venueFamiliarity == VenueFamiliarity.KNOWN_VENUE
        ) {
            precautions += Precaution(
                ruleId = "R-09",
                level = PrecautionLevel.CONSIDER,
                action = "Notify a contact of your expected end time",
                triggerText = "A hearing at a known venue."
            )
        }

        // R-10: over 3 hours → mid-engagement check-in regardless of interval.
        if (input.durationMinutes > 180) {
            precautions += Precaution(
                ruleId = "R-10",
                level = PrecautionLevel.RECOMMENDED,
                action = "Add a mid-engagement check-in regardless of interval",
                triggerText = "The engagement lasts over 3 hours."
            )
        }

        // Conflict resolution: the strictest interval wins, and the override
        // is visible in the output (spec §16).
        val conflictNotes = mutableListOf<String>()
        var interval = DEFAULT_INTERVAL_MINUTES
        if (intervalCandidates.isNotEmpty()) {
            val winner = intervalCandidates.minByOrNull { it.second }!!
            interval = winner.second
            val overridden = intervalCandidates
                .filter { it.second > winner.second }
                .map { it.first }
            val note = buildString {
                append("Interval set to $interval minutes by ${winner.first}")
                if (overridden.isNotEmpty()) {
                    append(", overriding the ${intervalCandidates.first { it.first in overridden }.second} minutes from ${overridden.joinToString(", ")}")
                } else {
                    append(", overriding the $DEFAULT_INTERVAL_MINUTES-minute default")
                }
            }
            conflictNotes += note
        }

        val tier = when {
            highTier -> RiskTier.HIGH
            tierBump || precautions.any { it.level == PrecautionLevel.REQUIRED } -> RiskTier.ELEVATED
            else -> RiskTier.STANDARD
        }

        return CompileResult(
            precautions = precautions.sortedBy { it.ruleId },
            tier = tier,
            rulesetVersion = RULESET_VERSION,
            intervalMinutes = interval,
            conflictNotes = conflictNotes
        )
    }
}
