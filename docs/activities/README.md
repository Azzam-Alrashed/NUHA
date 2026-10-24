# Activity sheets

SRS FR-ACT-001: before content is produced for an activity, it needs a sheet approved by the Arabic or maths educator. One file per activity, named by its id in `android/.../child/Activities.kt` (for example `emotions.md`), using the template below. Nothing here is approved yet.

| Activity | id | Sheet | Educator approval |
|---|---|---|---|
| «تلوين المشاعر» | `emotions` | not written | — |
| «رحلة مطابقة البيئة» | `environment` | not written | — |
| «تتبع مسار الأشكال الاجتماعية» | `path` | not written | — |
| «تتبع النجوم الهادئة» | `star` | not written | — |
| «متاهة توازن الكرة» | `maze` | not written | — |
| «زرع بستان الأزهار» | `garden` | not written | — |
| «كتابة الحروف وتوصيل الكلمات» | `letters` | not written | — |
| «نسخ الأنماط الهندسية» | `patterns` | not written | — |
| «الميزان الحسابي البصري» | `balance` | not written | — |

## Template

```markdown
# <Arabic name> (`<id>`)

Status: draft | approved by <name, role> on <date>

## Learning objective
One sentence: what the child learns, not what the game does.

## Prerequisites
Skills the child needs first (motor, language, reading).

## Successful behaviour
What a correct response looks like, including accepted variation (for drawing: the tolerance).

## Likely confounds
What could make a child fail for reasons other than the objective (vocabulary, motor demand, colour, audio).

## Examples
The items, with the Arabic text, voice lines and images each needs. Mark which are practice and which are new.

## Accessibility alternative
How a child who cannot drag or draw does the same task (FR-ACT-005), and what changes under muted audio.

## Difficulty factors
Each factor that can change, one at a time (FR-ACT-012), with its levels.

## What is recorded
Beyond the shared trial fields (FR-ACT-011): anything activity-specific.

## Transfer question
How we would check the skill outside the app.
```
