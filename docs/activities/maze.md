# «متاهة توازن الكرة» (`maze`)

Status: draft, written by the developer from the SRS and the built activity (milestone 3). Not reviewed by an educator; every Arabic line below is draft (`@DraftArabic` in code).

## Learning objective
Plan a route for a ball that avoids holes.

## Prerequisites
Can draw a line; understands start and goal.

## Successful behaviour
Mechanic is PLACEHOLDER OI-10 (`MazeMechanic = "route"`): the child draws the ball's route from the ball to the star, then presses «شغّل الكرة». The ball rolls along the drawing (deterministic, replayable) and falls into the first hole the route crosses.

## Likely confounds
Drawing precision; the run button as an extra step.

## Examples
Instruction «ارسم طريق الكرة إلى النجمة، ثم شغّل الكرة.»; praise «أحسنت! وصلت الكرة إلى النجمة.»; model «انظر، نرسم طريقًا يمرّ بجانب الحفرة ولا يدخلها.»; invalid route «ارسم طريقًا يبدأ من الكرة وينتهي عند النجمة.»

## Accessibility alternative
Not yet defined; depends on OI-10.

## Difficulty factors
Holes 1 → 2 → 3.

## What is recorded
Route strokes; `misses` = falls; detail `falls;invalidRoutes;mechanic`.

## Transfer question
For the educator to set.
