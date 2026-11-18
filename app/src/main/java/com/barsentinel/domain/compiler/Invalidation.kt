package com.barsentinel.domain.compiler

/**
 * Context-change invalidation (spec §7). Editing venue, time, company, buddy
 * or connectivity re-runs the compiler against the stored original inputs.
 * The diff names the changed field for every affected precaution. A material
 * change — any Required precaution affected, or the buddy changed — returns
 * the envelope to PROPOSE, voids buddy acceptance, and requires
 * re-confirmation and re-acceptance. All versions are retained; nothing is
 * overwritten.
 */
object Invalidation {

    data class Diff(
        /** Field names that changed, in assessment order. */
        val changedFields: List<String>,
        /** Precautions from the original compile that no longer apply. */
        val invalidated: List<Precaution>,
        /** Precautions the new inputs require that the original did not. */
        val newlyRequired: List<Precaution>,
        /** Precautions present in both compiles. */
        val unchanged: List<Precaution>,
        /** True when a Required precaution is affected or the buddy changed. */
        val isMaterial: Boolean,
        /** True when the envelope must return to PROPOSE for re-acceptance. */
        val reacceptanceRequired: Boolean
    )

    fun diff(
        original: CompilerInput,
        changed: CompilerInput,
        originalResult: CompileResult,
        buddyChanged: Boolean
    ): Diff {
        val changedFields = mutableListOf<String>()
        if (original.engagementType != changed.engagementType) changedFields += "engagement type"
        if (original.venueFamiliarity != changed.venueFamiliarity) changedFields += "venue"
        if (original.finishesAfterDark != changed.finishesAfterDark) changedFields += "time"
        if (original.durationMinutes != changed.durationMinutes) changedFields += "duration"
        if (original.company != changed.company) changedFields += "company"
        if (original.counterparty != changed.counterparty) changedFields += "counterparty"
        if (original.travel != changed.travel) changedFields += "travel"
        if (original.connectivity != changed.connectivity) changedFields += "connectivity"
        if (buddyChanged) changedFields += "buddy"

        val newResult = EngagementCompiler.compile(changed)
        val originalByRule = originalResult.precautions.associateBy { it.ruleId }
        val newByRule = newResult.precautions.associateBy { it.ruleId }

        val invalidated = originalResult.precautions.filter { it.ruleId !in newByRule }
        val newlyRequired = newResult.precautions.filter { it.ruleId !in originalByRule }.toMutableList()
        val unchanged = originalResult.precautions.filter { it.ruleId in newByRule }

        // R-11: company changed to alone after compilation → void the R-09
        // relaxation, re-apply the alone rules, require re-confirmation.
        if (original.company != Company.ALONE && changed.company == Company.ALONE) {
            newlyRequired += Precaution(
                ruleId = "R-11",
                level = PrecautionLevel.REQUIRED,
                action = "Re-apply the alone rules and re-confirm the plan",
                triggerText = "Company changed to alone after compilation."
            )
        }

        // R-12: buddy changed after acceptance → acceptance void, back to Proposed.
        if (buddyChanged) {
            newlyRequired += Precaution(
                ruleId = "R-12",
                level = PrecautionLevel.REQUIRED,
                action = "The new buddy must accept this version before it can go live",
                triggerText = "The buddy changed after acceptance."
            )
        }

        val material = buddyChanged ||
            invalidated.any { it.level == PrecautionLevel.REQUIRED } ||
            newlyRequired.any { it.level == PrecautionLevel.REQUIRED }

        return Diff(
            changedFields = changedFields,
            invalidated = invalidated,
            newlyRequired = newlyRequired.sortedBy { it.ruleId },
            unchanged = unchanged,
            isMaterial = material,
            reacceptanceRequired = material
        )
    }
}
