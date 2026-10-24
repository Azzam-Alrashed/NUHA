# «زرع بستان الأزهار» (`garden`)

Status: draft, written by the developer from the SRS and the built activity (milestone 3). Not reviewed by an educator; every Arabic line below is draft (`@DraftArabic` in code).

## Learning objective
Follow number order 1, 2, 3 … by tapping.

## Prerequisites
Recognises the numerals in play (numeral style is PLACEHOLDER OI-02, Western digits for now).

## Successful behaviour
Taps the seeds in order; each correct tap grows a flower. A wrong number counts as a wrong answer; skipping ahead is also counted as a skip.

## Likely confounds
Numeral recognition versus counting; seed positions (fixed places, shuffled numbers).

## Examples
Instruction «ازرع الأزهار بالترتيب: ابدأ بالرقم 1.»; praise «أحسنت! نمت كل الأزهار بالترتيب.»; model «نبدأ بالرقم الأصغر، ثم الذي بعده.»

## Accessibility alternative
Tap only. No timing: a timed mode exists only as a separate choice (PLACEHOLDER `GardenTimed = false`).

## Difficulty factors
Seeds 3 → 4 → 5.

## What is recorded
Detail `taps=<order, x for wrong>;skips`.

## Transfer question
For the educator to set.
