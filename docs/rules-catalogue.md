# Rules catalogue

Spec §16. Ruleset version **1.0.0**. Each rule is a unit test and a
scenario-suite case. Wording shown to users is the plain-language trigger;
the condition is the implementable form. Growth is by versioned change, never
by silent edit. Removing or weakening a rule requires a major ruleset version
bump and a scenario-suite update in the same change.

A rule may only use compiler inputs (spec §7). No rule may infer risk from a
person's name, community, religion, gender or neighbourhood reputation. Every
rule names its precaution, its level and its trigger text at authoring time.
Conflicting rules resolve to the stricter precaution, and the conflict is
visible in the output.

| ID | Condition | Precaution (level) |
|---|---|---|
| R-01 | Venue = other person's home/office AND company = alone | Assign a buddy and require acceptance before start (Required) |
| R-02 | Expected end after dark | Shorten check-in interval; confirm return transport before start (Required) |
| R-03 | Venue familiarity = first visit | Share venue area with buddy in the envelope; save the venue contact number offline (Recommended) |
| R-04 | Counterparty = first contact | Meet with a colleague present or choose a public / known venue (Recommended); if alone at a private venue, escalate tier |
| R-05 | Counterparty = previously difficult | Require buddy acceptance + second contact; set shortest interval (Required) |
| R-06 | Travel = arranged by counterparty | Arrange independent return transport; record it in the envelope (Required) |
| R-07 | Home visit AND alone AND after dark (combined) | Enable session location escrow; buddy acceptance mandatory; tier = High (Required) |
| R-08 | Connectivity = poor or unknown | Pre-load SMS ladder; shorten interval; download venue pack / contacts offline (Recommended) |
| R-09 | Engagement type = hearing AND venue = court complex | Standard tier defaults; buddy optional unless another rule fires (Consider: notify a contact of expected end) |
| R-10 | Duration over 3 hours | Add a mid-engagement check-in regardless of interval (Recommended) |
| R-11 | Company changes to alone after compilation (invalidation) | Void R-09 relaxation; re-apply alone rules; require re-confirmation (Required) |
| R-12 | Buddy changed after acceptance (invalidation) | Void acceptance; envelope returns to Proposed until the new buddy accepts (Required) |

## Implementation notes

- **R-09 mapping:** the compiler's venue input is familiarity, not venue
  category. A court complex is represented as a hearing at a known venue.
  This mapping is a judgement call, recorded here so a reviewer can challenge
  it.
- **Intervals:** default 30 minutes. R-02 and R-08 propose 20; R-05 proposes
  15. The strictest wins; the override is named in the output (e.g. "Interval
  set to 15 minutes by R-05, overriding the 30-minute default").
- **Tiers:** High if R-07 fires; Elevated if any Required precaution fires or
  R-04 escalates the tier; otherwise Standard. The tier summarises the
  precautions — it is never a prediction that something will happen.
- **R-11/R-12** fire only through the invalidation diff
  (`domain/compiler/Invalidation.kt`), never during a first compile.

## Version history

| Version | Change |
|---|---|
| 1.0.0 | Initial catalogue: R-01 to R-12 as specified. 30-case suite green. |
