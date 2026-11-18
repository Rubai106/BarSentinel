# Architecture

Spec §14. This document describes the system as specified; the "Built" column
says what exists in this repository today.

## Layers

| Layer | Choice | Built |
|---|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose, single activity | Yes — Home, privacy centre, simulated-buddy shell, placeholders |
| Architecture | MVVM, unidirectional data flow; domain use-cases for Compile, Propose, CheckIn, Escalate, VerifyChain | Partial — compiler + chain are pure domain code; use-cases land with their phases |
| Local store | Room (envelopes, timeline, vault metadata, outbox), DataStore (preferences) | Room schema (13 entities) yes; DataStore Phase 7 |
| Background | Foreground service (session location only); WorkManager (outbox drain, sync, retries); exact alarms only for local pre-due reminders | No — Phases 4–5 |
| Location | Fused Location Provider, adaptive sampling by tier and battery | No — Phase 5 |
| Push / SMS | Firebase Cloud Messaging; structured SMS with delivery-state tracking | No — Phase 4 |
| Security | Android Keystore for local keys; EncryptedFile / encrypted Room fields for vault metadata; biometric gate for the vault | No — Phase 6 |
| DI / async | Explicit construction root (`di/AppContainer`) in Phase 1; Hilt from Phase 3 | AppContainer yes |

### Why no DI framework in Phase 1

Phase 1 has one constructed object (the database). A DI framework would add
build complexity before there is anything to inject. `AppContainer` keeps
construction visible in one file; Hilt replaces it when the backend, scheduler
client and repositories arrive in Phase 3. This is a recorded, deliberate
deviation from the spec's stack table, not an oversight.

## System context — the scheduler owns the deadline

```
ADVOCATE PHONE            MINIMAL BACKEND            BUDDY PHONE
Compose UI · Room ·       Firebase Auth ·            Accept / decline ·
Compiler                  Firestore                  Watching
Foreground location       Escalation scheduler       Escalation ·
service                   Escrow · FCM · SMS         Acknowledge
WorkManager outbox ·                               Location only
Vault                    after release
```

The non-negotiable rule (spec §8): escalation Steps 1–4 must not depend on
the advocate's phone being alive. The deadline, grace and step windows are
stored with the backend and the buddy at acceptance time, and the backend
scheduler drives escalation. A timer that lives only on the advocate's device
fails Gate G3.

## Backend — deliberately minimal (Phase 3)

- **Auth:** phone-based Firebase Authentication. No professional verification
  is performed or implied.
- **Mirror store:** Firestore holds envelope metadata, acceptance, deadlines
  and escrowed fixes — the minimum for buddy-side escalation — under security
  rules that give the buddy status-only access until a release event.
- **Scheduler:** a scheduled cloud function evaluates due envelopes and drives
  escalation steps, sending FCM and triggering SMS. Its decisions are written
  back as timeline events.
- **No web dashboard.** Pilot review uses exported bundles and in-app views.

## Escrow model

- Precise location is sampled only while an envelope is Active (or during
  SOS), by a foreground service with a persistent, honest notification.
- During a healthy envelope the buddy sees heartbeat and status only.
- Only the latest fix is retained. It is released at escalation Step 2 or on
  SOS, and the release is a timeline event the advocate can review.
- On safe closure the escrowed location is deleted from the backend and the
  deletion is logged.
- Sampling adapts to tier and battery (e.g. 5 min at High, 10–15 min at
  Standard), pausing precise fixes when stationary with low battery; the
  active profile is shown on the envelope screen.

## Transport ladder

Data (Firestore/FCM) → structured SMS → local queue, with one per-message
state machine integrated into the timeline. The UI never displays "sent" for
a message that was only queued. See spec §9.

## Offline-first

Room is the on-device source of truth; the UI always reads locally. Create,
compile, check in, extend, close, capture evidence and view the plan all work
offline. Conflicts resolve by version, never by overwrite. An offline device
cannot guarantee delivery when neither data nor cellular service is available
— that sentence appears on the envelope screen whenever the device has no
connection.

## Simulated-buddy mode

A clearly labelled simulation replaces the buddy and scheduler on one device:
short intervals, playing the buddy, simulating a dead advocate phone. Every
simulated screen carries a SIMULATION banner, simulated events are marked in
the timeline, and simulation data never mixes with real envelopes. This is
what makes Gates G3, G6 and G10 testable by a marker alone.

## Permissions discipline

Location is requested in context at first envelope activation; background
location is never requested (foreground service instead). SMS permission is
requested only when the ladder reaches SMS. Denied permissions degrade
gracefully with a clear statement of reduced protection — never a dead end.
Phase 1 requests no permissions.
