package com.barsentinel.ui.home

import androidx.lifecycle.ViewModel
import com.barsentinel.data.demo.DemoData
import com.barsentinel.data.demo.DemoEnvelope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Home states from spec §11: no envelope, proposed-not-live, live,
 * escalating, offline banner. Phase 1 demonstrates the two states that
 * decide the design — no envelope and live — driven by synthetic demo
 * data. The remaining states arrive with the envelope lifecycle (Phase 3)
 * and the escalation engine (Phase 4); they are not faked here.
 */
sealed interface HomeUiState {
    data object NoEnvelope : HomeUiState
    data class Live(val envelope: DemoEnvelope) : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.NoEnvelope)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** Demo preview switch. Removed when real envelopes land in Phase 3. */
    fun previewLive() {
        _state.value = HomeUiState.Live(DemoData.liveEnvelope)
    }

    fun previewEmpty() {
        _state.value = HomeUiState.NoEnvelope
    }
}
