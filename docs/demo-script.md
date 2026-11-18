# Demo script — 90 seconds

Spec §19. The APK must match this script exactly. Until a step is built, it
is marked pending — the script is not rehearsed against a build that cannot
do it.

| Time | Beat | What the viewer sees | Status |
|---|---|---|---|
| 0:00 | Compile | Create a home-visit envelope: first visit, alone, finishing after dark. Three precautions, each with its stated reason and rule ID. | Engine built + tested; wizard UI pending (Phase 2) |
| 0:20 | Invalidate | Move the end time later. The diff shows which precautions fell, what is newly required, and the envelope dropping back to not-live. | Engine built + tested; diff UI pending (Phase 7) |
| 0:35 | Bind | Buddy accepts the revised version on the second phone (or simulated buddy). LIVE state; the buddy's status-only view — no map. | Pending (Phases 3–4) |
| 0:50 | Fail | Miss the check-in (shortened demo interval; advocate phone into airplane mode on camera). Buddy-side escalation fires anyway. Location appears on the buddy screen only now. | Pending (Phases 4–5) |
| 1:10 | Prove | Advocate checks in late; ladder stands down. Export the bundle, verify the chain — then tamper with one event in a copy and verify again: it fails at exactly that event. | Chain engine built + tested; export UI pending (Phase 6) |
| 1:25 | Limits | Close on the privacy / limits screen: what the app does not promise. This ending is deliberate — it is the trust signal. | Privacy centre built (Phase 1) |

## Rehearsal rules

- All demo content is synthetic: invented names, areas and engagements. No
  real client, venue or person appears.
- Shortened demo intervals are stated on screen as demo settings.
- The simulated-buddy path is labelled SIMULATION on every simulated screen.
- If any step cannot be performed on the shipped APK, the script is cut to
  what the APK does — never the other way round.
