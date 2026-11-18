# Backend — scheduler design stub

Phase 3 deliverable. Nothing here is built yet; this stub records the design
so the Android client is written against the right contract from Phase 1.

## What the backend owns

The deadline. Escalation Steps 1–4 must not depend on the advocate's phone
being alive (spec §8, Gate G3). The backend stores, at acceptance time:

- Envelope id + version, buddy id, second contact id
- Check-in deadline, grace window, step windows
- The escrowed latest fix (access-controlled, deleted on safe closure)

A scheduled cloud function evaluates due envelopes, drives the escalation
steps, sends FCM pushes, triggers structured SMS, and writes every decision
back as a timeline event.

## What the backend never owns

- A web dashboard (permanent cut, spec §5).
- Continuous location history (only the latest escrowed fix exists).
- Client or case data (never collected).

## Contract with the client (to be fixed in Phase 3)

- Firestore collections: `envelopes/{id}/versions/{v}`, `acceptances`,
  `deadlines`, `escrow` — security rules give the buddy status-only access
  until a release event.
- The scheduler function and its security rules live in this directory.
- No secrets are committed. Firebase configuration is added by the developer
  on their own machine and never pushed.

## Status

Not started. The Android client compiles and runs without any backend; the
simulated-buddy mode covers single-device testing until Phase 3 lands.
