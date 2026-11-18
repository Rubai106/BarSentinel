# Measurements

Spec §17. **No invented numbers.** Until a measurement below has actually
been run, it reads "Not yet measured." A single fabricated metric, discovered,
destroys the credibility the other gates built. Every published table records
its method, device and date, reports median and worst case, and includes
failures — never merged into "sent".

## Escalation timing per step (Gate G8)

Method: at least 10 logged runs per step, mixed connectivity.

| Step | Median | Worst case | Failures | Status |
|---|---|---|---|---|
| 0 — pre-due reminder | Not yet measured | | | AT RISK |
| 1 — grace alert | Not yet measured | | | AT RISK |
| 2 — buddy push + location release | Not yet measured | | | AT RISK |
| 3 — structured SMS to buddy | Not yet measured | | | AT RISK |
| 4 — second contact | Not yet measured | | | AT RISK |

## Battery — session location (Gate G7)

Method: real device, 2-hour active envelope per sampling profile; device
model and battery health noted.

| Profile | % per hour | Device | Date | Status |
|---|---|---|---|---|
| High tier (5 min) | Not yet measured | | | AT RISK |
| Standard (10–15 min) | Not yet measured | | | AT RISK |

## Envelope creation time

Method: timed task test with at least 5 users, or a scripted equivalent
stated honestly as such. Median and slowest, not just the best run.

| Result | Status |
|---|---|
| Not yet measured | AT RISK |

## Delivery success by channel (Gate G6)

Method: all test messages logged by channel and state. Queued and failed are
reported, never merged into "sent".

| Channel | Delivered | Queued | Failed | Status |
|---|---|---|---|---|
| Data / push | Not yet measured | | | AT RISK |
| Structured SMS | Not yet measured | | | AT RISK |

## Chain verification (Gate G5)

Method: verify every exported test bundle; tamper one event per bundle.
Verification must pass clean bundles and fail tampered ones at the correct
event — both results published.

| Check | Status |
|---|---|
| Unit tests: clean / tampered / deleted / reordered | Passing (see `HashChainTest`) |
| Live tamper demo on exported bundles | Not yet measured |

## Compiler correctness (Gate G1)

Method: 30-case scenario suite, in CI and in-app.

| Check | Status |
|---|---|
| 30/30 unit tests (27 compile + 3 invalidation) | Passing locally; CI not yet configured |
| In-app scenario tester screen | Done — ships the same 30 cases as the unit tests |
