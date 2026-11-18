package com.barsentinel.data.draft

import android.content.Context
import com.barsentinel.domain.compiler.Company
import com.barsentinel.domain.compiler.CompilerInput
import com.barsentinel.domain.compiler.Connectivity
import com.barsentinel.domain.compiler.Counterparty
import com.barsentinel.domain.compiler.EngagementType
import com.barsentinel.domain.compiler.Travel
import com.barsentinel.domain.compiler.VenueFamiliarity

/**
 * The wizard's partially answered form. Every field is nullable while the
 * user is still answering; [counterpartySet] distinguishes "prefer not to
 * say" (a deliberate null counterparty) from "not answered yet".
 */
data class DraftAnswers(
    val type: EngagementType? = null,
    val venue: VenueFamiliarity? = null,
    val afterDark: Boolean? = null,
    val durationMinutes: Int? = null,
    val company: Company? = null,
    val counterparty: Counterparty? = null,
    val counterpartySet: Boolean = false,
    val travel: Travel? = null,
    val connectivity: Connectivity? = null
) {
    /** True when every question has an answer (counterparty may be deliberately null). */
    val isComplete: Boolean
        get() = type != null && venue != null && afterDark != null && durationMinutes != null &&
            company != null && counterpartySet && travel != null && connectivity != null

    fun toCompilerInput(): CompilerInput? {
        if (!isComplete) return null
        return CompilerInput(
            engagementType = type!!,
            venueFamiliarity = venue!!,
            finishesAfterDark = afterDark!!,
            durationMinutes = durationMinutes!!,
            company = company!!,
            counterparty = counterparty,
            travel = travel!!,
            connectivity = connectivity!!
        )
    }

    companion object {
        fun from(input: CompilerInput) = DraftAnswers(
            type = input.engagementType,
            venue = input.venueFamiliarity,
            afterDark = input.finishesAfterDark,
            durationMinutes = input.durationMinutes,
            company = input.company,
            counterparty = input.counterparty,
            counterpartySet = true,
            travel = input.travel,
            connectivity = input.connectivity
        )
    }
}

/**
 * Draft autosave for the compile wizard (spec §11). Answers are written to
 * SharedPreferences on every change, so leaving the wizard — or the app
 * being killed — never loses them. This stores only the wizard's own
 * answers (no names, no case details); the draft is cleared once the plan
 * is saved to Room. Dismissals made on the review page are not part of the
 * draft: leaving the flow resets them.
 */
class CompileDraftStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): DraftAnswers = DraftAnswers(
        type = enumOrNull<EngagementType>(KEY_TYPE),
        venue = enumOrNull<VenueFamiliarity>(KEY_VENUE),
        afterDark = if (prefs.contains(KEY_AFTER_DARK)) prefs.getBoolean(KEY_AFTER_DARK, false) else null,
        durationMinutes = if (prefs.contains(KEY_DURATION)) prefs.getInt(KEY_DURATION, 0) else null,
        company = enumOrNull<Company>(KEY_COMPANY),
        counterparty = enumOrNull<Counterparty>(KEY_COUNTERPARTY),
        counterpartySet = prefs.getBoolean(KEY_COUNTERPARTY_SET, false),
        travel = enumOrNull<Travel>(KEY_TRAVEL),
        connectivity = enumOrNull<Connectivity>(KEY_CONNECTIVITY)
    )

    fun save(answers: DraftAnswers) {
        prefs.edit().apply {
            putEnum(KEY_TYPE, answers.type)
            putEnum(KEY_VENUE, answers.venue)
            if (answers.afterDark != null) putBoolean(KEY_AFTER_DARK, answers.afterDark) else remove(KEY_AFTER_DARK)
            if (answers.durationMinutes != null) putInt(KEY_DURATION, answers.durationMinutes) else remove(KEY_DURATION)
            putEnum(KEY_COMPANY, answers.company)
            putEnum(KEY_COUNTERPARTY, answers.counterparty)
            putBoolean(KEY_COUNTERPARTY_SET, answers.counterpartySet)
            putEnum(KEY_TRAVEL, answers.travel)
            putEnum(KEY_CONNECTIVITY, answers.connectivity)
            apply()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private inline fun <reified T : Enum<T>> enumOrNull(key: String): T? {
        val name = prefs.getString(key, null) ?: return null
        return runCatching { enumValueOf<T>(name) }.getOrNull()
    }

    private fun <T : Enum<T>> android.content.SharedPreferences.Editor.putEnum(key: String, value: T?) {
        if (value != null) putString(key, value.name) else remove(key)
    }

    private companion object {
        const val PREFS_NAME = "compile_draft"
        const val KEY_TYPE = "type"
        const val KEY_VENUE = "venue"
        const val KEY_AFTER_DARK = "after_dark"
        const val KEY_DURATION = "duration"
        const val KEY_COMPANY = "company"
        const val KEY_COUNTERPARTY = "counterparty"
        const val KEY_COUNTERPARTY_SET = "counterparty_set"
        const val KEY_TRAVEL = "travel"
        const val KEY_CONNECTIVITY = "connectivity"
    }
}
