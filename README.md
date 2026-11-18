# BarSentinel

**A safety envelope you can audit, not a panic button you have to trust.**

BarSentinel is a native Android app (Kotlin, Jetpack Compose) for advocates
doing off-site engagements in Bangladesh. Before an engagement, it compiles
the context into explained precautions, binds them into a time-limited
envelope a named buddy must accept, escalates from the buddy side if a
check-in is missed — even if the advocate's phone is dead — releases precise
location only at that moment, and records every event in a hash-chained
timeline that can be re-verified offline.

Course: CSE489 — Android Application Development, BRAC University.

## Limits first

Read these before any claim about what the app does:

- BarSentinel **cannot guarantee an emergency-service response**. SOS notifies
  the buddy and offers the 999 dialler; the response is theirs, not the app's.
- With **no data signal and no cellular service** anywhere in the chain,
  delivery cannot be guaranteed. The app says so on screen.
- The hash chain proves the record **has not changed since it was written**.
  It does not prove the events themselves were true, and it does not
  establish court admissibility.
- **No identity is verified.** The app does not check Bar Council membership
  or any professional credential.
- No single mechanism here is claimed as a world first. Server-side timers
  and escrowed location release have commercial precedent (Then-Go, Beacon
  Safe). The claim is the combination plus the proof. See
  `docs/architecture.md` and the full specification in `docs/`.

## Status

Built to the BarSentinel specification v1.0 (the spec PDF is held outside
this repo; the diagrams in `docs/` are the wireframes and schema). Phases
follow spec §18; the current phase is marked below.

| Phase | Deliverable | Status |
|---|---|---|
| 1. Foundations | Skeleton, Room schema, navigation, design system, privacy centre, simulated-buddy harness | **Done — this build** |
| 2. Compiler | Rules engine, catalogue v1.0.0, wizard, precautions review, scenario suite | **Done — wizard, precautions review, scenario tester, draft save** |
| 3. Envelope + buddy | Lifecycle, versioned acceptance, backend mirror, scheduler skeleton | Not started |
| 4. Escalation | Check-ins, grace, steps 0–4, transport ladder, stand-down | Not started |
| 5. Location escrow | Foreground service, adaptive sampling, status-only buddy view, release + deletion | Not started |
| 6. Timeline + vault | Hash chain UI, export + offline verify, evidence capture, access log | Chain engine + tests done; UI pending |
| 7. Invalidation + polish | Diff UI, offline states, Bangla pass, accessibility pass | Not started (the what-would-change preview shipped early, inside the review screen) |
| 8. Showcase freeze | Demo build, script rehearsal, case study, measurements published | Not started |

### Evidence gates (spec §2)

A gate stays **AT RISK** until its evidence exists. Nothing here is assumed.

| Gate | Status |
|---|---|
| G1 Compiler correctness — 30/30 scenario suite | Suite passes locally (27 compile + 3 invalidation cases); the in-app tester ships the same cases; CI not yet configured |
| G2 Invalidation works | Engine + tests done; UI pending |
| G3 Dead-phone escalation | Not yet measured |
| G4 Location escrow honoured | Not yet measured |
| G5 Hash chain verifies | Unit tests pass (clean, tampered, deleted, reordered); live tamper demo pending |
| G6 Offline honesty | Not yet measured |
| G7 Battery measured | Not yet measured |
| G8 Escalation timing measured | Not yet measured |
| G9 Accessibility checked | Not yet measured |
| G10 Demo reproducible | Not yet measured |

All measurements live in `docs/measurements.md`. Until a measurement has
actually been run, it reads "Not yet measured."

## Build

Requirements: Android Studio (Ladybug or newer) or JDK 17 + Android SDK
(API 35). The project uses the Gradle wrapper.

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest   # scenario suite + chain tests
```

Note: `gradle/wrapper/gradle-wrapper.jar` is not committed (binary). Android
Studio regenerates it automatically on first open; on the command line, run
`gradle wrapper --gradle-version 8.7` once to generate it.

## Layout

```
app/            Compose UI, ViewModels, Room, domain (compiler, chain)
docs/           Specification, diagrams, architecture, rules catalogue,
                threat model, measurements, demo script
backend/        Scheduler design (Phase 3) — no function yet
```

Package map inside `app/`: `ui/` (theme, navigation, screens, components),
`domain/` (compiler, chain — pure Kotlin, tested), `data/` (Room entities,
DAOs, demo data), `di/` (explicit construction root).

## Traceability

BarSentinel traces to Freelancer project 40800233, *Advocate SOS Safety App
Development* (₹12,500–37,500 INR; shown Open in the supplied screenshot,
Closed when opened live). It traces to the job's problem and core safety
requirements — SOS, trusted contacts, check-ins, location, incident records —
while deliberately delivering a native-Android, reduced-scope interpretation.
The compiler, escrow and hash chain are the student's contributions; the job
did not ask for them.

Differentiation statement (spec §4): "Existing safety and lone-worker apps
monitor a person during an activity. BarSentinel reasons about a legal
engagement before it starts, invalidates its own precautions when the context
changes, requires a named buddy to accept a versioned plan, escalates from the
buddy side, escrows location until failure, and produces a hash-chained
timeline that can be re-verified offline."

## Design

Material 3 Expressive base, deep-navy data screens, glass cards, warm gold
accent — approved from a 21-reference design study (see the design boards in
the project history). The Home screen ships in both states: no live envelope,
and envelope live with the countdown ring. All demo content is synthetic.

## Honest claims

What may and may never be said about this product is fixed in the spec §20
claims matrix and the `Say / Never say` copy rules (§12). The short version:
claim the combination and the proof, never a guarantee, never admissibility,
never endorsement.
