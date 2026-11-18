package com.barsentinel.ui.compile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barsentinel.BarSentinelApp
import com.barsentinel.data.draft.CompileDraftStore
import com.barsentinel.domain.compiler.Company
import com.barsentinel.domain.compiler.Connectivity
import com.barsentinel.domain.compiler.Counterparty
import com.barsentinel.domain.compiler.EngagementType
import com.barsentinel.domain.compiler.Travel
import com.barsentinel.domain.compiler.VenueFamiliarity
import com.barsentinel.ui.components.GlassColumnCard
import com.barsentinel.ui.components.GoldButton
import com.barsentinel.ui.theme.BarSentinelShapes
import com.barsentinel.ui.theme.GlassBorder
import com.barsentinel.ui.theme.GlassSurface
import com.barsentinel.ui.theme.Gold
import com.barsentinel.ui.theme.GoldBorder
import com.barsentinel.ui.theme.GoldGlass
import com.barsentinel.ui.theme.TextFaint
import com.barsentinel.ui.theme.TextMuted
import com.barsentinel.ui.theme.TextPrimary
import com.barsentinel.ui.theme.TextSecondary

/**
 * The compile wizard (spec §11, Flow A): five short steps that turn answers
 * into a compiled plan. Back preserves answers, the draft autosaves on
 * every change, and templates pre-fill without skipping any question. The
 * precautions review is the final page of this same flow.
 */
@Composable
fun CompileFlowScreen(onExit: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as BarSentinelApp
    val viewModel: CompileViewModel = viewModel(
        factory = CompileViewModelFactory(CompileDraftStore(context), app.container)
    )
    val state by viewModel.state.collectAsState()
    var templateNotice by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = state.step != CompileStep.TYPE) {
        viewModel.back()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (!viewModel.back()) onExit()
                }
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = if (state.step == CompileStep.REVIEW) "Your precautions" else "New envelope",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.weight(1f))
            if (state.step != CompileStep.REVIEW) {
                TextButton(onClick = { viewModel.clearAnswers() }) {
                    Text("Clear answers", color = TextMuted, fontSize = 13.sp)
                }
            }
        }

        if (state.step != CompileStep.REVIEW) {
            val stepIndex = state.step.ordinal
            Row(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (stepIndex + 1) / 6f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Gold,
                    trackColor = GlassSurface
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    "Step ${stepIndex + 1} of 5",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        templateNotice?.let { notice ->
            Text(
                notice,
                color = Gold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)
            )
        }
        if (state.restoredDraft && state.step == CompileStep.TYPE) {
            Text(
                "Your saved draft is loaded. Change any answer, or clear it to start fresh.",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)
            )
        }

        if (state.step == CompileStep.REVIEW) {
            PrecautionsReviewScreen(
                state = state,
                onDismiss = viewModel::dismissPrecaution,
                onRestore = viewModel::restorePrecaution,
                onSave = viewModel::savePlan,
                onEditAnswers = { viewModel.goTo(CompileStep.TRAVEL) },
                onExit = onExit
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
            ) {
                when (state.step) {
                    CompileStep.TYPE -> TypeStep(
                        selected = state.answers.type,
                        onSelect = { viewModel.updateAnswers { it.copy(type = it) } },
                        onTemplate = { template ->
                            viewModel.applyTemplate(template)
                            templateNotice = "Template applied: ${template.label}. Adjust any answer."
                        }
                    )
                    CompileStep.VENUE -> VenueStep(
                        selected = state.answers.venue,
                        onSelect = { viewModel.updateAnswers { it.copy(venue = it) } }
                    )
                    CompileStep.TIME -> TimeStep(
                        afterDark = state.answers.afterDark,
                        duration = state.answers.durationMinutes,
                        onAfterDark = { viewModel.updateAnswers { it.copy(afterDark = it) } },
                        onDuration = { viewModel.updateAnswers { it.copy(durationMinutes = it) } }
                    )
                    CompileStep.PEOPLE -> PeopleStep(
                        company = state.answers.company,
                        counterparty = state.answers.counterparty,
                        counterpartySet = state.answers.counterpartySet,
                        onCompany = { viewModel.updateAnswers { it.copy(company = it) } },
                        onCounterparty = { value ->
                            viewModel.updateAnswers { it.copy(counterparty = value, counterpartySet = true) }
                        }
                    )
                    CompileStep.TRAVEL -> TravelStep(
                        travel = state.answers.travel,
                        connectivity = state.answers.connectivity,
                        onTravel = { viewModel.updateAnswers { it.copy(travel = it) } },
                        onConnectivity = { viewModel.updateAnswers { it.copy(connectivity = it) } }
                    )
                    CompileStep.REVIEW -> Unit
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
                GoldButton(
                    text = if (state.step == CompileStep.TRAVEL) "See precautions" else "Continue",
                    enabled = viewModel.canContinue(state),
                    onClick = {
                        templateNotice = null
                        viewModel.next()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Draft autosaved. Nothing is sent or shared while you answer.",
                    color = TextFaint,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StepHeading(title: String, help: String) {
    Text(title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(modifier = Modifier.height(6.dp))
    Text(help, color = TextSecondary, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(18.dp))
}

@Composable
private fun <T> OptionList(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    descriptions: Map<T, String> = emptyMap()
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { (value, label) ->
            SelectableOption(
                label = label,
                description = descriptions[value],
                selected = value == selected,
                onClick = { onSelect(value) }
            )
        }
    }
}

@Composable
private fun SelectableOption(
    label: String,
    description: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = BarSentinelShapes.medium,
        color = if (selected) GoldGlass else GlassSurface,
        border = BorderStroke(1.dp, if (selected) GoldBorder else GlassBorder),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(description, color = TextMuted, fontSize = 13.sp)
                }
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) Gold else GlassSurface,
                    border = BorderStroke(1.dp, if (selected) Gold else GlassBorder),
                    modifier = Modifier.size(22.dp)
                ) {}
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                    ) {
                        Surface(shape = CircleShape, color = TextPrimary, modifier = Modifier.fillMaxSize()) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeStep(
    selected: EngagementType?,
    onSelect: (EngagementType) -> Unit,
    onTemplate: (CompileTemplate) -> Unit
) {
    StepHeading(
        "What kind of engagement is this?",
        "Start from a template if one fits — you can change every answer afterwards."
    )
    Text("Start from a template", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CompileTemplates.all.forEach { template ->
            GlassColumnCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(template.label, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(template.description, color = TextMuted, fontSize = 13.sp)
                    }
                    TextButton(onClick = { onTemplate(template) }) {
                        Text("Use", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    Text("Or answer from scratch", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    OptionList(
        options = listOf(
            EngagementType.COURT_HEARING to "Court hearing",
            EngagementType.CLIENT_MEETING to "Client meeting",
            EngagementType.HOME_VISIT to "Home visit",
            EngagementType.CHAMBER_MEETING to "Chamber meeting",
            EngagementType.SITE_VISIT to "Site visit",
            EngagementType.OTHER to "Something else"
        ),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun VenueStep(selected: VenueFamiliarity?, onSelect: (VenueFamiliarity) -> Unit) {
    StepHeading(
        "How well do you know the place?",
        "First visits and other people's premises change the plan."
    )
    OptionList(
        options = listOf(
            VenueFamiliarity.OWN_CHAMBER to "My own chamber",
            VenueFamiliarity.KNOWN_VENUE to "A place I know well",
            VenueFamiliarity.FIRST_VISIT to "First time here",
            VenueFamiliarity.OTHER_PREMISES to "Someone else's premises"
        ),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun TimeStep(
    afterDark: Boolean?,
    duration: Int?,
    onAfterDark: (Boolean) -> Unit,
    onDuration: (Int) -> Unit
) {
    StepHeading(
        "When does it finish, and how long?",
        "Check-ins are planned around the finish time. After-dark finishes get shorter intervals."
    )
    Text("Finish time", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    OptionList(
        options = listOf(false to "Finishes before dark", true to "Finishes after dark"),
        selected = afterDark,
        onSelect = onAfterDark
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text("How long will it take?", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    val durations = listOf(30, 60, 90, 120, 180, 240)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        durations.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { minutes ->
                    val label = if (minutes >= 60) {
                        val hours = minutes / 60
                        val remainder = minutes % 60
                        if (remainder == 0) "$hours h" else "$hours h $remainder min"
                    } else {
                        "$minutes min"
                    }
                    Surface(
                        shape = BarSentinelShapes.medium,
                        color = if (duration == minutes) GoldGlass else GlassSurface,
                        border = BorderStroke(1.dp, if (duration == minutes) GoldBorder else GlassBorder),
                        onClick = { onDuration(minutes) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            label,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 14.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeopleStep(
    company: Company?,
    counterparty: Counterparty?,
    counterpartySet: Boolean,
    onCompany: (Company) -> Unit,
    onCounterparty: (Counterparty?) -> Unit
) {
    StepHeading(
        "Who will be with you, and who is the other side?",
        "Being alone changes the plan. The second question is optional — “Prefer not to say” is a valid answer."
    )
    Text("Who will be with you?", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    OptionList(
        options = listOf(
            Company.ALONE to "Alone",
            Company.WITH_COLLEAGUE to "With a colleague",
            Company.WITH_CLIENT_ONLY to "With the client only",
            Company.GROUP to "With a group"
        ),
        selected = company,
        onSelect = onCompany
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text("Who is the other side?", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    OptionList(
        options = listOf(
            Counterparty.KNOWN to "Someone I know",
            Counterparty.REFERRED to "Referred to me",
            Counterparty.FIRST_CONTACT to "First contact",
            Counterparty.PREVIOUSLY_DIFFICULT to "Previously difficult"
        ),
        selected = if (counterpartySet) counterparty else null,
        onSelect = onCounterparty
    )
    Spacer(modifier = Modifier.height(10.dp))
    SelectableOption(
        label = "Prefer not to say",
        description = "The plan is compiled without this answer.",
        selected = counterpartySet && counterparty == null,
        onClick = { onCounterparty(null) }
    )
}

@Composable
private fun TravelStep(
    travel: Travel?,
    connectivity: Connectivity?,
    onTravel: (Travel) -> Unit,
    onConnectivity: (Connectivity) -> Unit
) {
    StepHeading(
        "How are you getting there, and what is the signal like?",
        "Travel arranged by the other side, and poor signal, both change the plan."
    )
    Text("Travel", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    OptionList(
        options = listOf(
            Travel.OWN_TRANSPORT to "My own transport",
            Travel.RIDE_HAILING to "Ride-hailing",
            Travel.PUBLIC_TRANSPORT to "Public transport",
            Travel.ON_FOOT to "On foot",
            Travel.ARRANGED_BY_COUNTERPARTY to "Arranged by the other side"
        ),
        selected = travel,
        onSelect = onTravel
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text("Phone signal at the venue", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    OptionList(
        options = listOf(
            Connectivity.GOOD to "Good signal",
            Connectivity.POOR to "Poor signal",
            Connectivity.UNKNOWN to "Not sure"
        ),
        selected = connectivity,
        onSelect = onConnectivity
    )
}
