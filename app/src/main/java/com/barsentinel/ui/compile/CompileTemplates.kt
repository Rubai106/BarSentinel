package com.barsentinel.ui.compile

import com.barsentinel.domain.compiler.Company
import com.barsentinel.domain.compiler.CompilerInput
import com.barsentinel.domain.compiler.Connectivity
import com.barsentinel.domain.compiler.Counterparty
import com.barsentinel.domain.compiler.EngagementType
import com.barsentinel.domain.compiler.Travel
import com.barsentinel.domain.compiler.VenueFamiliarity

/**
 * Starting templates (spec Flow A). A template pre-fills every answer; the
 * advocate adjusts any of them before compiling. Templates are honest
 * starting points, not presets that skip the questions: every pre-filled
 * answer stays visible and changeable.
 */
data class CompileTemplate(
    val id: String,
    val label: String,
    val description: String,
    val input: CompilerInput
)

object CompileTemplates {

    val all: List<CompileTemplate> = listOf(
        CompileTemplate(
            id = "home_visit",
            label = "Home visit",
            description = "First visit, alone, finishing after dark",
            input = CompilerInput(
                engagementType = EngagementType.HOME_VISIT,
                venueFamiliarity = VenueFamiliarity.FIRST_VISIT,
                finishesAfterDark = true,
                durationMinutes = 90,
                company = Company.ALONE,
                counterparty = Counterparty.FIRST_CONTACT,
                travel = Travel.OWN_TRANSPORT,
                connectivity = Connectivity.UNKNOWN
            )
        ),
        CompileTemplate(
            id = "client_meeting",
            label = "Client meeting",
            description = "First visit, with the client, referred",
            input = CompilerInput(
                engagementType = EngagementType.CLIENT_MEETING,
                venueFamiliarity = VenueFamiliarity.FIRST_VISIT,
                finishesAfterDark = false,
                durationMinutes = 60,
                company = Company.WITH_CLIENT_ONLY,
                counterparty = Counterparty.REFERRED,
                travel = Travel.RIDE_HAILING,
                connectivity = Connectivity.GOOD
            )
        ),
        CompileTemplate(
            id = "hearing_day",
            label = "Hearing day",
            description = "Court hearing, known venue, long day",
            input = CompilerInput(
                engagementType = EngagementType.COURT_HEARING,
                venueFamiliarity = VenueFamiliarity.KNOWN_VENUE,
                finishesAfterDark = false,
                durationMinutes = 240,
                company = Company.WITH_COLLEAGUE,
                counterparty = Counterparty.KNOWN,
                travel = Travel.OWN_TRANSPORT,
                connectivity = Connectivity.GOOD
            )
        )
    )
}
