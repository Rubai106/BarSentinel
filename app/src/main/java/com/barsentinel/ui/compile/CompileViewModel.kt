package com.barsentinel.ui.compile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.barsentinel.data.draft.CompileDraftStore
import com.barsentinel.data.draft.DraftAnswers
import com.barsentinel.data.local.CompilerDao
import com.barsentinel.data.local.CompilerInputEntity
import com.barsentinel.data.local.EnvelopeDao
import com.barsentinel.data.local.EnvelopeEntity
import com.barsentinel.data.local.PrecautionEntity
import com.barsentinel.domain.compiler.CompileResult
import com.barsentinel.domain.compiler.CompilerInput
import com.barsentinel.domain.compiler.EngagementCompiler
import com.barsentinel.di.AppContainer
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Steps of the compile flow. REVIEW is the precautions review page. */
enum class CompileStep { TYPE, VENUE, TIME, PEOPLE, TRAVEL, REVIEW }

/**
 * The compile flow's state (spec §11, Flow A). One ViewModel owns the
 * wizard steps and the review page: answers survive Back, the draft is
 * autosaved on every change, and the compiled result is recomputed from
 * the answers whenever the review page is shown.
 */
data class CompileUiState(
    val answers: DraftAnswers = DraftAnswers(),
    val step: CompileStep = CompileStep.TYPE,
    /** Dismissed precautions: rule id -> one-tap reason. */
    val dismissals: Map<String, String> = emptyMap(),
    /** The compiled plan, set when the review page is entered. */
    val result: CompileResult? = null,
    val isSaving: Boolean = false,
    val savedEnvelopeId: String? = null,
    val saveFailed: Boolean = false,
    /** True when a restored draft supplied the starting answers. */
    val restoredDraft: Boolean = false
)

class CompileViewModel(
    private val draftStore: CompileDraftStore,
    private val envelopeDao: EnvelopeDao,
    private val compilerDao: CompilerDao
) : ViewModel() {

    private val _state = MutableStateFlow(CompileUiState())
    val state: StateFlow<CompileUiState> = _state.asStateFlow()

    init {
        val draft = draftStore.load()
        val hasAnything = draft != DraftAnswers()
        if (hasAnything) {
            _state.update { it.copy(answers = draft, restoredDraft = true) }
        }
    }

    /** Whether the current step has everything it needs to continue. */
    fun canContinue(state: CompileUiState): Boolean = when (state.step) {
        CompileStep.TYPE -> state.answers.type != null
        CompileStep.VENUE -> state.answers.venue != null
        CompileStep.TIME -> state.answers.afterDark != null && state.answers.durationMinutes != null
        CompileStep.PEOPLE -> state.answers.company != null && state.answers.counterpartySet
        CompileStep.TRAVEL -> state.answers.travel != null && state.answers.connectivity != null
        CompileStep.REVIEW -> true
    }

    fun applyTemplate(template: CompileTemplate) {
        val answers = DraftAnswers.from(template.input)
        draftStore.save(answers)
        _state.update { it.copy(answers = answers, step = CompileStep.VENUE, dismissals = emptyMap(), result = null) }
    }

    fun clearAnswers() {
        draftStore.clear()
        _state.update { it.copy(answers = DraftAnswers(), step = CompileStep.TYPE, dismissals = emptyMap(), result = null, restoredDraft = false) }
    }

    fun updateAnswers(transform: (DraftAnswers) -> DraftAnswers) {
        _state.update { current ->
            val next = transform(current.answers)
            draftStore.save(next)
            current.copy(answers = next, result = null, dismissals = emptyMap())
        }
    }

    fun goTo(step: CompileStep) {
        _state.update { it.copy(step = step) }
    }

    fun next() {
        val current = _state.value
        val nextStep = when (current.step) {
            CompileStep.TYPE -> CompileStep.VENUE
            CompileStep.VENUE -> CompileStep.TIME
            CompileStep.TIME -> CompileStep.PEOPLE
            CompileStep.PEOPLE -> CompileStep.TRAVEL
            CompileStep.TRAVEL -> CompileStep.REVIEW
            CompileStep.REVIEW -> CompileStep.REVIEW
        }
        if (nextStep == CompileStep.REVIEW) {
            compile()
        }
        _state.update { it.copy(step = nextStep) }
    }

    fun back(): Boolean {
        val current = _state.value
        val previous = when (current.step) {
            CompileStep.TYPE -> return false
            CompileStep.VENUE -> CompileStep.TYPE
            CompileStep.TIME -> CompileStep.VENUE
            CompileStep.PEOPLE -> CompileStep.TIME
            CompileStep.TRAVEL -> CompileStep.PEOPLE
            CompileStep.REVIEW -> CompileStep.TRAVEL
        }
        _state.update { it.copy(step = previous) }
        return true
    }

    /** Recompiles from the current answers. Only valid once all answers exist. */
    fun compile() {
        val input = _state.value.answers.toCompilerInput() ?: return
        _state.update { it.copy(result = EngagementCompiler.compile(input)) }
    }

    fun dismissPrecaution(ruleId: String, reason: String) {
        _state.update { it.copy(dismissals = it.dismissals + (ruleId to reason)) }
    }

    fun restorePrecaution(ruleId: String) {
        _state.update { it.copy(dismissals = it.dismissals - ruleId) }
    }

    /**
     * Saves the compiled plan as a DRAFT envelope with its compiler input
     * and precautions (including dismissals with reasons). A draft is a
     * plan, not a live envelope: no buddy is involved, nothing is sent,
     * and the timeline starts when the envelope is proposed in Phase 3.
     * Times are draft placeholders (now + duration), replaced with the
     * real schedule at propose time. The owner id is a local placeholder
     * until accounts land (Milestone 2 auth work).
     */
    fun savePlan() {
        val current = _state.value
        val input = current.answers.toCompilerInput() ?: return
        val result = current.result ?: return
        if (current.isSaving || current.savedEnvelopeId != null) return
        _state.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            runCatching {
                val now = System.currentTimeMillis()
                val envelopeId = UUID.randomUUID().toString()
                envelopeDao.insert(
                    EnvelopeEntity(
                        id = envelopeId,
                        ownerId = OWNER_PLACEHOLDER,
                        version = 1,
                        status = "DRAFT",
                        type = input.engagementType.name,
                        venueCategory = input.venueFamiliarity.name,
                        areaLabel = "Area not set yet",
                        startEpochMillis = now,
                        expectedEndEpochMillis = now + input.durationMinutes * 60_000L,
                        intervalMinutes = result.intervalMinutes,
                        graceMinutes = 10,
                        tier = result.tier.name,
                        rulesetVersion = result.rulesetVersion,
                        buddyId = null,
                        secondContactId = null,
                        createdAtEpochMillis = now
                    )
                )
                compilerDao.insertInput(
                    CompilerInputEntity(
                        envelopeId = envelopeId,
                        envelopeVersion = 1,
                        engagementType = input.engagementType.name,
                        venueFamiliarity = input.venueFamiliarity.name,
                        finishesAfterDark = input.finishesAfterDark,
                        durationMinutes = input.durationMinutes,
                        company = input.company.name,
                        counterparty = input.counterparty?.name,
                        travel = input.travel.name,
                        connectivity = input.connectivity.name
                    )
                )
                compilerDao.insertPrecautions(
                    result.precautions.map { precaution ->
                        val reason = current.dismissals[precaution.ruleId]
                        PrecautionEntity(
                            envelopeId = envelopeId,
                            envelopeVersion = 1,
                            ruleId = precaution.ruleId,
                            level = precaution.level.name,
                            action = precaution.action,
                            triggerText = precaution.triggerText,
                            state = if (reason == null) "ACTIVE" else "DISMISSED",
                            stateReason = reason
                        )
                    }
                )
                envelopeId
            }.onSuccess { envelopeId ->
                draftStore.clear()
                _state.update { it.copy(isSaving = false, savedEnvelopeId = envelopeId) }
            }.onFailure {
                _state.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    companion object {
        const val OWNER_PLACEHOLDER = "local-user"
    }
}

class CompileViewModelFactory(
    private val draftStore: CompileDraftStore,
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CompileViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return CompileViewModel(draftStore, container.envelopeDao, container.compilerDao) as T
    }
}
