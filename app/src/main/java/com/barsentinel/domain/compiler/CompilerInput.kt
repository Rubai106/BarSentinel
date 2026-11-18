package com.barsentinel.domain.compiler

/**
 * Compiler inputs (spec §7). Every field is an answer from the context
 * assessment — the engine may only use these inputs. No rule may infer risk
 * from a person's name, community, religion, gender or neighbourhood
 * reputation (spec §16). [counterparty] is optional: null means the advocate
 * did not answer, which is itself a valid, deterministic input state.
 */
enum class EngagementType {
    COURT_HEARING, CLIENT_MEETING, HOME_VISIT, CHAMBER_MEETING, SITE_VISIT, OTHER
}

enum class VenueFamiliarity {
    OWN_CHAMBER, KNOWN_VENUE, FIRST_VISIT, OTHER_PREMISES
}

enum class Company {
    ALONE, WITH_COLLEAGUE, WITH_CLIENT_ONLY, GROUP
}

enum class Counterparty {
    KNOWN, REFERRED, FIRST_CONTACT, PREVIOUSLY_DIFFICULT
}

enum class Travel {
    OWN_TRANSPORT, RIDE_HAILING, PUBLIC_TRANSPORT, ON_FOOT, ARRANGED_BY_COUNTERPARTY
}

enum class Connectivity {
    GOOD, POOR, UNKNOWN
}

data class CompilerInput(
    val engagementType: EngagementType,
    val venueFamiliarity: VenueFamiliarity,
    /** Derived from the end time, confirmed by the advocate — never asked separately. */
    val finishesAfterDark: Boolean,
    val durationMinutes: Int,
    val company: Company,
    val counterparty: Counterparty?,
    val travel: Travel,
    val connectivity: Connectivity
)

enum class PrecautionLevel { REQUIRED, RECOMMENDED, CONSIDER }

/**
 * A precaution is data, not just UI text (spec §13): the rule ID and the
 * trigger wording are stored with the envelope so the explanation survives
 * export and re-verification.
 */
data class Precaution(
    val ruleId: String,
    val level: PrecautionLevel,
    val action: String,
    val triggerText: String
)

enum class RiskTier { STANDARD, ELEVATED, HIGH }

data class CompileResult(
    val precautions: List<Precaution>,
    val tier: RiskTier,
    val rulesetVersion: String,
    /** Check-in interval in minutes, after conflict resolution. */
    val intervalMinutes: Int,
    /** Visible record of which rule set the interval when rules conflicted. */
    val conflictNotes: List<String>
)
