package com.barsentinel.ui.navigation

/**
 * All 14 destinations from spec §11, plus Settings. Route strings are stable:
 * deep links and the demo script (docs/demo-script.md) refer to them.
 */
object Routes {
    const val HOME = "home"
    const val ENVELOPES = "envelopes"
    const val VAULT = "vault"
    const val MORE = "more"
    const val COMPILE = "compile"
    const val PRECAUTIONS = "precautions"
    const val ENVELOPE_DETAIL = "envelope/{envelopeId}"
    const val INVALIDATION_DIFF = "invalidation/{envelopeId}"
    const val BUDDY_INCOMING = "buddy/incoming"
    const val BUDDY_WATCHING = "buddy/watching"
    const val SOS = "sos"
    const val TIMELINE = "timeline/{envelopeId}"
    const val SCENARIO_TESTER = "scenarios"
    const val PRIVACY_CENTRE = "privacy"
    const val SIMULATED_BUDDY = "simulated-buddy"
    const val SETTINGS = "settings"

    fun envelopeDetail(envelopeId: String) = "envelope/$envelopeId"
    fun invalidationDiff(envelopeId: String) = "invalidation/$envelopeId"
    fun timeline(envelopeId: String) = "timeline/$envelopeId"

    /** Destinations that show the bottom navigation bar. */
    val bottomNavRoutes = setOf(HOME, ENVELOPES, VAULT, MORE)
}
