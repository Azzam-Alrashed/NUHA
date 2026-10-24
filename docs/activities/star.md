# «تتبع النجوم الهادئة» (`star`)

Status: draft, written by the developer from the SRS and the built activity (milestone 3). Not reviewed by an educator; every Arabic line below is draft (`@DraftArabic` in code).

## Learning objective
Keep a finger or pen with a slowly moving target for a whole path.

## Prerequisites
Can hold a touch on the screen.

## Successful behaviour
The star moves only while the child touches it; lifting pauses it, with no time pressure. A run counts when at least 60% of contact time was inside the ring (PLACEHOLDER `StarInTargetMin`).

## Likely confounds
Motor control rather than sustained attention; screen size; a resting hand.

## Examples
Instruction «ضع إصبعك على النجمة وامشِ معها داخل الدائرة.»; praise «أحسنت! مشيت مع النجمة حتى النهاية.»; model «انظر، نمشي مع النجمة ببطء ونبقى داخل الدائرة.» Paths: gentle wave, arch, loop.

## Accessibility alternative
Not yet defined: continuous touch is the task.

## Difficulty factors
Ring radius 80 → 64 dp, then run time 9 → 7 s, then the loop path added.

## What is recorded
Detail `inTargetMs;contactMs;lifts;radiusDp;durationMs;share`. Never an attention score.

## Transfer question
For the educator to set.
