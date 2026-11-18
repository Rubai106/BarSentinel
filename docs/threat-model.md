# Threat model

Spec §15. Assets, threats, controls — and the residual risk, stated plainly.
The residual-risk column is the part most student projects omit, and the part
professionals look for first. This document is kept current: if a control
changes, this table changes in the same release.

| Asset | Threat | Control | Residual risk — say it |
|---|---|---|---|
| Escrowed location | Buddy, second contact or backend reader sees location during a healthy envelope | Status-only buddy access enforced by backend rules; release only on Step 2 / SOS; release logged; deletion on closure | The backend operator can technically access escrow data — mitigated by minimisation and logging, not eliminated |
| Timeline integrity | Record altered after an incident | Hash chain; offline re-verification; export format documented | Chain proves no change since writing; it does not prove events were true when written |
| Evidence files | File replaced or edited after capture | SHA-256 at capture; append-only vault; access log | A staged photo hashes perfectly — integrity is not authenticity |
| Account | Phone theft / account takeover leading to false SOS or location release | Device credential for the app; biometric gate for vault; SOS requires an unlocked, deliberate hold | A coerced unlock is out of scope — no duress feature is claimed |
| Buddy duty | Buddy never agreed, or agreed to a different plan | Versioned acceptance with timestamp; material edits void acceptance | A buddy can still ignore an alert — escalation to a second contact is the mitigation, not a guarantee |
| Availability | No signal, dead battery, killed app | Buddy-side scheduler; transport ladder; offline queue with honest states | With no data and no cellular service anywhere in the chain, delivery cannot be guaranteed — stated in-app |

## Privacy commitments (enforced, testable)

- No continuous or background tracking outside an active envelope or SOS.
- No location history is built. Only the latest escrowed fix exists during a
  healthy envelope.
- No client or case data is required, solicited or analysed.
- No advertising SDKs, no third-party analytics on safety events, no sale or
  sharing of data.
- Data export and deletion are available to the owner in-app, with
  consequences stated before confirmation.
- The privacy centre reflects the actual system: if a behaviour changes, the
  screen changes in the same release.
