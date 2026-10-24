# Make NUHA playful: Playful Soft-Flat

NUHA's new version adopts **Playful Soft-Flat**. This is the style of the owner's Pinterest moodboard: flat shapes with soft shadows, bubbly corners, chunky buttons that look pressable, shape mascots with faces, a saga-map home where lessons are stops along a road, and frequent rewards in a saturated candy palette. It replaces the calm, low-saturation teal/sand direction, now archived as [legacy/ui-style-calm.md](legacy/ui-style-calm.md). This is an owner decision, not a research finding. The legacy report found no study showing that a muted palette improves learning, so neither direction has a proven learning advantage. The new direction aims for appeal and engagement, which matter for a child choosing to come back. Some of the legacy evidence still applies, and some of it argues against parts of this style. The rules that don't depend on style are [carried forward](#carried-forward-from-the-legacy-direction) unchanged. The places where the two directions clash are listed in [Conflicts with the legacy research](#conflicts-with-the-legacy-research), each with a mitigation and a test, so the trade-offs stay visible. The client PDF `docs/client/NUHA_UI_UX_Style_Research.pdf` describes the legacy direction, not this one.

**Evidence levels used in this document.** The four levels from the legacy report still apply. **Strong** means a meta-analysis or several consistent studies. **Moderate** means one or a few studies, small samples, or indirect populations. **Guideline** means a standard, platform rule or expert consensus. **Inference** means a design judgement to test. This document adds a fifth: **Owner direction** means a choice the owner made on aesthetic or product grounds, with no supporting study. Most of the visual choices below are owner direction.

## What the style is, and what it is not

The moodboard reduces to eight ingredients.

| Ingredient | Common name | Moodboard examples |
|---|---|---|
| Flat shapes with a little depth: soft shadows, slight gradients, no outlines | Flat 2.0 / soft-flat | Nearly every image |
| Fat corner radii and pill buttons | Bubbly / squircle UI | Ladybug path, Duolingo-style screens, UI kit sheet |
| Buttons with a darker bottom edge so they look pressable | Tactile or chunky buttons | Colours & Typography sheet, UI kit sheet |
| Geometric shapes given eyes and a mouth | Shape-character mascots | Both emotion grids, the Aptivo flower, the robot kids |
| Lessons drawn as stops along a winding road | Saga map / adventure path | Ladybug, Course B2, Duolingo map, Lessons strip |
| Stars, badges, ribbons, trophies, confetti, progress bars | Gamification "juice" | Award screen, UI kit sheet, quiz leaderboard |
| Bright, saturated but soft colours | Candy palette | Colours & Typography sheet, Flat UI colour swatches |
| Rounded geometric type in heavy weights | Rounded display type | Nunito sheet, Aptivo |

Three moodboard images are **out of scope**, so the direction stays coherent. ZenZ is claymorphism and soft glass: puffy 3D objects and gradient blurs. The @uix.vikram screens are neutral minimal. Lingora uses 3D-rendered, Pixar-style mascots, which need a different and much more expensive illustration pipeline than flat vector art. NUHA's artwork stays flat vector drawn in Compose, as `ui/identity/Identity.kt` already does.

## Palette

The eight candy hues from the moodboard's Colours & Typography sheet are the starting values. Each hue has five roles:
- **fill:** the surface colour
- **lip:** the darker bottom edge and border
- **text-on-fill:** the label colour
- **deep:** a shade dark enough for normal-size white text
- **tint:** a pale background for cards and chips

The shared neutrals are **ink #1E2A4A** (a dark navy, as on the emotion-grid faces) and **paper #FFF8EE** (a warm cream, as in Aptivo). Ink on paper is 13.4:1.

Every ratio below was computed with the WCAG 2.2 relative-luminance formula. Lip and deep shades are the fill mixed toward black until they reach the threshold, and tints are the fill mixed 80% toward white.

| Hue | Fill | Lip (vs paper) | Text on fill | Deep (white text) | Tint (ink on tint) |
|---|---|---|---|---|---|
| Amethyst | #9956DE | #7340A6 (6.5:1) | white, large text only (4.43:1) | #9755DC (4.52:1) | #EBDDF8 (10.9:1) |
| Slate Blue | #7274ED | #5657B2 (5.9:1) | white, large text only (3.86:1) | #686AD8 (4.52:1) | #E3E3FB (11.2:1) |
| Summer Sky | #1FA7E1 | #1D9ACF (3.03:1) | ink (5.17:1) | #187FAB (4.50:1) | #D2EDF9 (11.6:1) |
| Downy | #6ED1CF | #519B99 (3.06:1) | ink (7.87:1) | #437F7E (4.59:1) | #E2F6F5 (12.6:1) |
| Pastel Green | #75D06A | #5AA052 (3.02:1) | ink (7.40:1) | #4A8343 (4.55:1) | #E3F6E1 (12.5:1) |
| Texas Rose | #FFB356 | #BD8440 (3.05:1) | ink (7.97:1) | #9C6D34 (4.52:1) | #FFF0DD (12.6:1) |
| Mona Lisa | #FF8B8B | #D47373 (3.06:1) | ink (6.27:1) | #AD5F5F (4.59:1) | #FFE8E8 (12.1:1) |
| Illusion | #FB96BB | #C97896 (3.01:1) | ink (6.86:1) | #A3627A (4.58:1) | #FEEAF1 (12.3:1) |

The fills of Amethyst (4.20:1) and Slate Blue (3.66:1) are already 3:1 boundaries against paper. For those two, the lip is a 25% shade, so it reads as a chunky edge. The other six fills sit at only 1.7–2.6:1 against paper. By WCAG's measure, a tile in those colours is not identifiable as a control unless its edge carries the contrast. That is why each lip in the table is at least 3:1, and why tiles in those six hues also get a 2dp edge in the lip colour, not just a bottom lip (Guideline: [W3C 1.4.11](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html)). White text on Amethyst and Slate Blue passes only at WCAG large-text size (at least 18.66px bold), which covers every child-mode button label in this document. Smaller white text uses the deep shade.

Assign one hue to each activity and keep it everywhere: tile, path stop, badge and header. Colour then works as a wayfinding cue. It must never be the only cue, because each activity also has its own pictogram and its mascot (Guideline: [W3C 1.4.1](https://www.w3.org/WAI/WCAG22/Understanding/use-of-color.html)). Reds and pinks (Mona Lisa, Illusion) are activity colours, not error colours. NUHA has no error colour (see [Rewards and celebration](#rewards-and-celebration)).

The legacy *colourful*, *muted* and *high-contrast* comfort settings stay. Under this direction *colourful* is the default and uses the table above. *Muted* desaturates each fill toward its tint while keeping the same lips. *High-contrast* uses the deep shades as fills with white text, and ink lips. Owner direction for the default; Strong rationale for keeping the settings (legacy: sensory subtypes).

## Typography

Headings, buttons and UI labels use a **rounded geometric face in heavy weights**: Black for screen titles, ExtraBold for buttons, SemiBold for secondary labels. The moodboard uses Nunito, which has no Arabic. The Arabic candidates are **Baloo Bhaijaan 2**, a rounded OFL variable family (400–800) ([Google Fonts](https://fonts.google.com/specimen/Baloo+2/about)), and **Readex Pro**, already bundled in `res/font`. Readex Pro is geometric rather than rounded, so Baloo Bhaijaan 2 matches the style better. It has not yet been tested for harakat collisions. Owner direction for the look; Inference for the face choice.

Learning content (letters, words and sentences the child is learning to read) stays in **simplified Naskh, fully vowelized**, for example Noto Naskh Arabic. The rounded display face is for the interface around the task, not the thing being learned. This rule is carried forward from the legacy report, which found that the only child-relevant Arabic legibility evidence favours simplified Naskh ([Inderscience](https://www.inderscience.com/filter.php?aid=109660); [Abubaker & Lu 2012](https://pure.hud.ac.uk/en/publications/the-optimum-font-size-and-type-for-students-aged-9-12-reading-ara/)). Moderate.

Sizes from the legacy report still apply: learning text from about 28–32sp, UI labels from about 20–24sp, and 1.6–1.8× line height wherever tashkeel appears. Heavy weights close the counters of small Arabic letters faster than Latin ones, so display text below about 24sp uses SemiBold or ExtraBold rather than Black. Inference.

## Shape and elevation

- **Corners:** 24dp on cards and tiles, a fully rounded pill on buttons, and circles for path stops and avatars. Chips and the token board use pill ends.
- **Depth:** flat colour with one soft ambient shadow (large blur, low opacity, tinted toward the hue rather than grey) and an optional top-to-bottom gradient of no more than about 6% lightness. No outlines, except the lip-colour edge required by the [palette](#palette) rule.
- **Backgrounds:** paper on task screens. Illustrated scenes are kept for the home map and celebration screens (see [Conflicts](#conflicts-with-the-legacy-research)).

Owner direction.

## Buttons and tiles

The **chunky button** is the signature component.
- It is a fill-coloured pill with a 4–6dp lip in the lip colour along the bottom edge.
- **Pressed state:** on touch-down the face drops by the lip height, so the button visibly sinks, and the tap fires at that moment. Firing on touch-down is carried forward from the legacy report ([Cooney Center](https://joanganzcooneycenter.org/?p=19607)).
- The sink must not change the button's hit area or move anything next to it.
- **Answer tiles** use the same construction: tint or fill face, lip edge, 24dp corners.
- **Selected state** changes three things at once: the lip thickens, a check badge appears in the top corner (on the leading side in RTL), and the fill shifts from tint to full fill. Selection never relies on colour alone.

Target sizes are unchanged from the legacy report:
- 72dp floor for secondary controls
- 96–120dp for primary task targets for ages 2–5
- 16–24dp gaps
- sizes computed from the available task area

Placed items leave an empty slot and never reflow. Tap-then-tap is always available as an alternative to drag. Strong for the size rationale; Inference for the exact numbers; NUHA experiment for stable targets.

## Mascot system

The cast is **shape-character mascots**: simple geometric bodies with two eyes and a mouth, drawn as flat vectors. Each activity has its own mascot in that activity's hue. A lead mascot, the flower guide already drawn in `Identity.kt`, hosts the home map, onboarding and transitions. Mascots carry the instructions and demonstrations. On a task screen, only one mascot is on screen: the activity's own, or the lead guide.

The emotion-grid images show the expression range: closed-eye calm, open-eye curiosity, worried brows, and so on. The emotion activity can therefore start with these shape faces as its simplest rung, then move up the legacy report's cartoon-to-photo ladder to real Saudi and Gulf faces ([Rosset et al. 2008](https://mijn.bsl.nl/typical-emotion-processing-for-cartoon-but-not-for-real-faces-in/547186); [Zhao et al.](https://www.researchgate.net/profile/Weihua-Zhao-12/publication/353362177_Facial_emotion_training_as_an_intervention_in_autism_spectrum_disorder_A_meta-analysis_of_randomized_controlled_trials/links/6135760638818c2eaf8574a5/Facial-emotion-training-as-an-intervention-in-autism-spectrum-disorder-A-meta-analysis-of-randomized-controlled-trials.pdf)). Shape mascots also avoid the questions of dress and gender that human mascots raise for Saudi families.

Owner direction for the cast; Moderate for faces as an emotion rung.

## Home: the adventure path

Child mode opens on a **saga map**. A winding road crosses an illustrated landscape, and each activity is a circular stop in its hue, with its pictogram, its mascot and up to three stars for progress. The road runs from the bottom right toward the top left, so it reads in RTL order. Stops have a fixed position: they never shuffle, and new content appears as new stops, never as rearranged old ones. The map scrolls only if there are more stops than fit on one screen. On the 1280×800dp table it should fit without scrolling for the nine activities.

Navigation stays two levels deep: map, then activity. A therapist can lock the map to a single stop, so a child who needs it sees one activity, not a world. The landscape is decorative, so it stays on the map and never enters a task screen. Owner direction for the map; Guideline (COGA) for depth and fixed positions.

## Rewards and celebration

The reward vocabulary is stars, badges, ribbons, trophies and confetti, with progress bars on the map. The legacy token board survives in this vocabulary as a strip of 3–5 star slots on the task screen. Each correct or helped-correct trial fills one slot, and a full strip earns a celebration and a break.

Celebration has three tiers:
- **Per trial (1.5s or less):** a star flies into its slot and the mascot nods. No confetti.
- **Per session:** a short confetti burst, a ribbon ("أحسنت!") and the stars earned. The confetti burst is the one moment of high-energy motion.
- **Per milestone:** a badge or trophy for the trophy case on the adult-visible profile.

There is no failure state. A wrong answer returns gently to its slot, the prompt ladder steps up, and the trial is logged as "correct with help". Strong but contested for informational praise over contingent rewards ([Deci, Koestner & Ryan 1999](https://depts.washington.edu/techdocs/papers/deciExtrinsicRewardsAndIntrinsicMotivation99.pdf)); Guideline/Moderate for prompting ([NCAEP](https://ncaep.fpg.unc.edu/?p=395)); Owner direction for the visual vocabulary.

**Leaderboards are not adopted**, even though the moodboard shows one. NUHA is used one child at a time in resource rooms, and the README rules out comparing children with each other. Stars and badges compare a child only with their own past sessions.

## Motion

The style asks for bounce: buttons that sink and spring back, stars that fly, a mascot that hops between path stops. Motion is short (under about 400ms for UI responses), springy, and the same every time. On task screens nothing moves idly. Motion there points at the learning target or responds to the child's touch. The confetti burst is the only effect longer than 1.5s, and it plays only on the session celebration screen. No flashing above three per second ([W3C 2.3.1](https://www.w3.org/WAI/WCAG22/Understanding/three-flashes-or-below-threshold.html)). The in-app motion setting (full, reduced, none, defaulting to the system value) carries forward. Under *reduced*, confetti becomes a static ribbon and stars fade in instead of flying. Guideline plus Owner direction.

## Sound

The style's sound is bright and short: soft pops on button presses, a rising chime when a star lands, a short fanfare on the session celebration. The legacy rules carry forward unchanged:
- Arabic voice prompts on.
- Background music off by default.
- Separate voice, effects and music controls.
- Effects normalized and soft-attack.
- No buzzer.
- Anything under speech at least 20dB quieter than the speech, or off.

Moderate/Strong (hyperacusis prevalence; noise meta-analysis); market evidence for music off.

## Carried forward from the legacy direction

These rules hold under any visual style. The evidence is in the [legacy report](legacy/ui-style-calm.md).

| Rule | Evidence level |
|---|---|
| Text at least 4.5:1 (3:1 for large text); controls and meaningful graphics at least 3:1; WCAG ratio tests for every token pair | Guideline (WCAG 1.4.3, 1.4.11) |
| Colour is never the only cue: add shape, icon, border and position | Guideline (WCAG 1.4.1) plus Moderate (reduced chromatic discrimination) |
| Full, native-speaker-checked tashkeel on all learning content; light tashkeel on UI; no letter-spacing or tatweel on Arabic | Strong (diacritics) plus Guideline (W3C alreq) |
| One app-wide numeral setting, never mixed on a screen | Guideline; default unresolved |
| Mirror sequence (back/next, progress, schedules, the path); never mirror media controls, checkmarks, letter-tracing paths or charts | Guideline (Material, Firefox RTL) |
| Targets never move during a task; placed items leave slots | NUHA experiment plus Guideline (COGA) |
| Large targets; tap fires on touch-down; drag optional with tap-tap fallback; no double-tap, long-press or pinch in child mode | Strong |
| Child buttons inset from screen edges and corners | Guideline (Sesame) |
| No flashing; in-app motion setting | Guideline (WCAG 2.3.x) |
| Voice on, music off, three audio channels, no buzzer | Moderate/Strong |
| Prompt ladder with fading, feedback within about 0.5s, informative praise | Guideline/Moderate |
| Visual schedule strip, first/then card, trial dots, closing ritual | Strong-ish |
| Adult area as a separate Material 3 theme behind a gate; the child can always pause | Guideline |
| Per-child settings for theme, motion, audio, celebration level, mascot on/off, prompt delay, target size and numerals | Strong rationale (sensory subtypes; ADHD noise split) |

## Conflicts with the legacy research

These are the places where the new direction goes against evidence the legacy report found. They are accepted trade-offs, not oversights. Each one has a mitigation that keeps the style and a test that would show whether the trade-off is costing learning.

| Legacy finding | New choice | Risk | Mitigation and test |
|---|---|---|---|
| Autistic children looked at highly saturated images differently, with larger group differences ([J Autism Dev Disord 2025](https://link.springer.com/article/10.1007/s10803-025-07100-7)); guidelines warn against bright contrasting colours ([GOV.UK](https://accessibility.blog.gov.uk/2016/09/02/dos-and-donts-on-designing-for-accessibility/)) | Saturated candy palette as the default | Hyper-responsive children may find screens uncomfortable or avoid them. The cost of over-stimulating is higher than the cost of under-stimulating. | Keep *muted* and *high-contrast* settings, and let therapists set *muted* per child at enrolment. Large surfaces stay paper; saturation lives on tiles, stops and badges. **Test:** time to disengage and distress signs, colourful vs muted, split by sensory profile. |
| Heavily decorated rooms cut kindergarten learning from 55% to 42% ([APS](https://psychologicalscience.org/news/releases/heavily-decorated-classrooms-disrupt-attention-and-learning-in-young-children.html)); seductive details g = −0.33 ([WSU](https://news.wsu.edu/2020/03/19/seductive-details-inhibit-learning/)) | Illustrated landscapes, a mascot cast | Decoration on task screens lowers learning | Illustrated scenes stay on the map and celebration screens only. Task screens use plain paper, the task objects and one mascot. Scenes stay the same across sessions, so novelty fades. **Test:** task accuracy and off-task looking with the map art vs a plain map. |
| Expected, contingent tangible rewards can undermine intrinsic motivation ([Deci, Koestner & Ryan 1999](https://depts.washington.edu/techdocs/papers/deciExtrinsicRewardsAndIntrinsicMotivation99.pdf)); the legacy report capped celebrations at 1.5s | Stars, badges, trophies, confetti | Children work for the confetti, not the task, and over-arousal makes it harder to settle for the next trial | Three celebration tiers with confetti only per session. A celebration-intensity setting (off, gentle, lively; default gentle). Praise says what was right. **Test:** trials attempted after the reward is removed, and settling time after celebrations, for ADHD and autistic children. |
| Token board, not a points economy; no leaderboards, lives or shops | Gamification vocabulary from the moodboard | A drift toward points, streaks and competition | Stars fill a bounded token board; badges are personal. Leaderboards, lives, streak penalties and shops stay out (see [Rewards](#rewards-and-celebration)). |
| One quiet guide across all activities; characters help when they carry content and hurt as side attractions ([Takacs, Swart & Bus 2015](https://journals.sagepub.com/doi/pdf/10.3102/0034654314566989)) | A cast with one mascot per activity, plus a lead guide | More characters to learn, and a character becoming the attraction | Only one mascot on a task screen. Mascots speak only at instruction and feedback moments and stay still while the child works. Adults can switch every activity to the lead guide. **Test:** task completion and off-task looking with the activity mascot, the lead guide, and no mascot. |
| No idle motion; brief, consistent feedback | Springy, bouncy motion and confetti | Vestibular discomfort; attention pulled away from the target | Bounce only in response to touch or to show the target. Confetti only per session. A reduced setting that removes both. |
| Calm default because over-stimulating a hyper-responsive child is the costlier mistake | Lively default | The default is wrong for some children on day one, before anyone adjusts it | Enrolment asks the therapist to choose a sensory starting point (lively, gentle or muted) before the child's first session. |

## Open decisions and things to test with children

The legacy open decisions still stand: the age range, the numeral convention, and Fusha vs dialect voice. The age range matters more now, because a style built from apps for ages 4–8 may overwhelm 2–3-year-olds and look babyish to 9–10-year-olds. New decisions this direction adds:

- **Rounded Arabic face.** Baloo Bhaijaan 2 vs Readex Pro, after a harakat collision test on device.
- **Mascot cast.** One mascot per activity, or fewer shared across activities. Each extra character needs design, animation and voice work.
- **Map art budget.** The landscape is the most expensive asset in the style. Decide on one landscape or one per activity group.
- **The default sensory starting point** at enrolment.

Tests to add to the legacy test list:
1. Palette acceptance by sensory profile: colourful vs muted.
2. Effect of map decoration on task accuracy.
3. Celebration intensity vs settling time.
4. Mascot presence: activity mascot vs lead guide vs none.
5. Whether the chunky pressed state helps children under 5 understand that a tap registered.
6. Contrast regression: WCAG unit tests for every token pair in all three themes before any child testing.

## Status of existing work

The current onboarding was built to the legacy direction: warm paper, the flower guide, a winding path, and a muted palette in `ui/identity/Identity.kt` and `ui/theme/`. Its structure (flower guide, path, comfort settings) already fits this direction. Its colours, button shapes and type weights will be restyled in a later change. No Kotlin has changed with this document.
