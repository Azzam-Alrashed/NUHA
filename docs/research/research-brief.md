# NUHA Research Brief

## 1. Decisions the research should change

**Executive finding:** NUHA should be designed as an accessible educational experience whose learning goals are explicit. A pleasant interface, higher game score, and a demonstrated improvement outside the game are different outcomes. The research does not validate NUHA as a diagnostic or therapeutic product.

**Recommended priorities**

- Define users by language, reading, motor, sensory, and assistance needs, rather than assigning a fixed interface or game solely from a diagnosis.
- Separate the educational task from the input task. A child may know the right answer but struggle to drag or draw it.
- Treat Arabic language content as a curriculum decision, not a translation task.
- Make the actual table and stylus part of usability research. A tablet emulator cannot settle reach, comfort, palm rejection, or input delay.
- Replace unexplained composite percentages with understandable activity records, assistance context, and comparable practice conditions.
- Prefer a small, verifiable local experience over introducing cloud AI before its purpose and data implications are justified.

**Evidence anchors:** W3C provides cognitive-accessibility patterns [S1]; UNICEF provides a child well-being design framework [S2]; a preschool app trial illustrates that engagement need not produce observable real-world communication gains [S3]. The recommendations above are our synthesis, not a tested NUHA intervention.

**Project boundary:** the owner confirmed Kotlin as the replacement direction. The original plan and owner describe nine screen-and-stylus activities; the legacy APK has ten games plus a focus challenge. No decision to carry over the extras is implied.

**What this report delivers:** findings, limits, game-specific hypotheses, research activities, and technical experiments. It does not fix an age range, prescribe treatment, select hardware, or claim that field research has occurred.

## 2. Method, confidence, and evidence limits

**Research date:** 20 October 2026. This is a targeted desk-research synthesis, not a systematic review or a clinical effectiveness assessment. Searches covered child-centered design, cognitive accessibility, Arabic literacy, game-learning transfer, stylus input, Android accessibility, privacy, ethics, and comparison products.

**Source selection:** primary empirical studies where available, official W3C/UNICEF/IES guidance, Saudi regulatory material, and Android platform documentation. Product-owner descriptions are used only as examples of features to examine. Academic claims below were reviewed through available abstracts; full methods and supplementary data were not independently appraised. Some document fetches failed, so retrieved official excerpts were used and interpretation was kept narrow.

**Evidence labels throughout**

| Label | What it means |
|---|---|
| Finding | Supported by the cited source, within its population and context |
| Legacy observation | Seen in the earlier APK walkthrough; not general research evidence |
| Recommendation | A proposed NUHA design response; still needs evaluation |
| Field question | Cannot be settled from desk research alone |

**Important limits:** no parent interviews, child sessions, expert review, physical table assessment, or Kotlin benchmark was conducted for this report. We do not have a representative Saudi user sample. Research on typically developing children or older children does not automatically transfer to younger children with different support needs.

**Original-report reference audit:** exact-title searches did not identify reliable matching records for the cited Smith/Brown/Davis interactive-table paper or the Johnson/Lee stylus paper. This is not proof they do not exist. Obtain DOI/publisher records before relying on them. The remaining original references were not fully verified. No original-report therapeutic claim is treated here as established evidence.

**Local context used:** LEGACY_APP_REFERENCE.md, the original project-plan PDF, and the owner's nine-game description. The report's ages 4–10 and legacy form's ages 2–6 remain unresolved.

## 3. Understand the child, not just the category

**Finding:** UNICEF's RITEC framework draws on 787 children, principally ages 8–12, across 18 countries. It organizes well-being around safety, inclusion, autonomy, emotions, competence, relationships, creativity, and identity. It is a useful design lens, not a validated prescription for NUHA's younger or neurodivergent users. [S2]

**Recommendation: research a support-needs matrix.** Include receptive language, familiarity with standard Arabic, reading readiness, ability to select versus drag versus draw, handedness, tolerance of motion/audio, preferred assistance, and ability to communicate a wish to stop. Do not turn this matrix into a diagnostic score.

**Candidate use contexts to validate**

| Context | What to investigate |
|---|---|
| Parent and child at home | Setup burden, interruptions, shared devices, internet reliability |
| Teacher or specialist with a child | Goal selection, prompts, assistance recording, session handover |
| Child using the table with supervision nearby | Recovering from mistakes, getting help, stopping, accidental exits |

**Interview prompts:** Ask adults to describe a recent activity that worked and one that failed. Ask what the child could do independently, what help was given, and what the adult needed to know afterward. Avoid asking only whether they like a proposed design.

**Field questions:** Are there children who cannot yet use a stylus comfortably? Is stylus-only a hardware vision or a hard access restriction? Can a task offer a tap alternative while retaining a separate pen-practice mode? Who owns each profile on a shared table?

**Decision output:** a small set of usage scenarios and support profiles, with explicit exclusions and unresolved needs. Confirm age range through intended curriculum and participants, not by copying the old slider.

## 4. Cognitive, sensory, and motor accessibility

**Finding:** W3C cognitive guidance emphasizes understandable purpose, familiar controls, consistent layouts, manageable steps, and reduced unnecessary complexity. It is supplementary guidance, not a certification that a product works for all users. [S1]

**Finding:** Android recommends at least 48dp touch targets for interactive elements. That is a platform baseline, not a proven ideal size for young children on a table. W3C's dragging guidance supports non-drag alternatives unless the movement itself is essential. [S4, S5]

**Recommended starting hypotheses**

- Present one active instruction with a repeatable demonstration and optional spoken version.
- Keep the task, answers, help, and pause visible together. Avoid searching or scrolling during an active timed task.
- Offer independent controls for music, speech, effects, and motion. Start with restrained animation; test preferences rather than assuming one universally calming palette.
- Use shape, position, and labels where needed so color is not the only cue. For deliberate color-discrimination tasks, explain that distinction and offer other activities rather than claiming universal access.
- Provide a stable help location and a predictable retry. Preserve work after an accidental interruption.
- Make pause/stop accessible to the child; protecting parent settings must not prevent stopping play.

**Checks:** TalkBack labels and order; large text without clipped Arabic; visible focus; contrast; muted-audio comprehension; left-handed reach; accidental palm input; adjacent target separation. WCAG 2.2 offers useful contrast, motion, and input criteria, but its web conformance model does not alone certify a native Android app. [S6]

**Legacy observation:** some activity controls and number targets were outside the initial landscape viewport. Treat this as a concrete layout-test case, not evidence that all scrolling is harmful.

## 5. Learning claims and what counts as evidence

**Finding:** Fletcher-Watson and colleagues studied 54 autistic children under six in a randomized trial. The app engaged children and parents rated it favorably, but the study found no significant group differences on the reported post-intervention parent measures or follow-up parent-child play outcome. This illustrates the gap between engagement and transfer. It does not show that all educational apps are ineffective. [S3]

**Finding:** a 2026 emotion-recognition trial abstract reports 36 autistic children ages 3–10, split into two groups, with improved emotion-test accuracy in the intervention group. Its small sample and specific intervention do not establish effects for NUHA or prove generalization to everyday interaction. [S7]

**Finding:** STARS-ADHD randomized 348 children ages 8–12 in a specific intervention/control trial. That study's population, software, procedures, and attention outcome cannot be substituted by NUHA's brief star-tapping activity. It is an example of the study effort needed for a treatment claim, not permission to make one. [S8]

**Recommendation: use an outcome ladder**

| Level | Question | Suitable evidence |
|---|---|---|
| Usability | Can the child understand and operate it? | Observation, prompts, errors, successful recovery |
| In-game performance | Does performance change in comparable rounds? | Versioned task records with assistance and difficulty |
| Learning transfer | Can the skill be used on new examples or outside the app? | Educator-designed unfamiliar tasks and follow-up |
| Clinical benefit | Does it affect a clinical outcome? | Separate qualified research and appropriate oversight |

Do not call a session percentage an attention diagnosis or combine different skills into a health-like score. Resolve what each activity teaches before optimizing rewards or measuring improvement.

## 6. Arabic literacy and early mathematics

**Finding:** a longitudinal study followed 137 Arabic-speaking first-graders into second grade. Reading was more accurate and faster for words whose spoken and standard forms were identical than for standard-only forms. This supports paying attention to language familiarity; it is not a Saudi preschool or NUHA-specific evaluation. [S9]

**Finding:** the MAWRID model treats vocalization, morphology, and spoken/standard distance as central to Arabic reading development. It is a theoretical model, not evidence that a particular tracing game works. W3C Arabic layout guidance describes contextual letter forms and bidirectional text requirements. [S10, S11]

**Recommendations for Arabic content**

- Have an Arabic early-years educator approve letter forms, examples, phoneme/letter-name recordings, word familiarity, and use of diacritics.
- Teach recognition, letter-sound association, tracing, and writing as related but distinct tasks. Tracing a silhouette does not demonstrate reading a word.
- Test isolated and connected forms when the curriculum introduces them. Do not assume a 28-screen sequence covers contextual forms.
- Choose and consistently present numerals; test mixed Arabic, numbers, symbols, punctuation, and Latin names without blindly mirroring equations.
- Use explicit stroke guidance where educationally justified, accepting reasonable variation rather than rewarding only exact pixel overlap.

**Finding:** IES preschool guidance supports intentional mathematical instruction, mathematical conversation, vocabulary work, and letter/sound knowledge. Its evidence ratings concern those instructional approaches, not NUHA's game mechanics. [S12]

**Recommendation:** connect quantity, numeral, and operation. In arithmetic, allow the child to inspect groups before selecting an answer. Test whether the child is counting, recalling, or guessing. Separate a handwriting failure from a mathematical error.

**Field question:** Which Saudi curriculum, language register, numeral convention, and educator-approved progression should govern the new version?

## 7. Game research: communication and matching

The following are **design hypotheses**, derived from the intended activities and the distinction between task performance and transfer. They require educator review and child testing; they are not validated interventions. [S3, S7, S12]

### تلوين المشاعر / legacy: توصيل المشاعر

**Task to define:** recognizing an illustrated emotion, associating a word with it, or considering a social situation. These are different goals. Start with clear examples and then vary characters/context. Avoid teaching that a face always reveals exactly what someone feels. Do not make a particular color the universal meaning of an emotion.

**Record:** first independent choice, hints, revised choice, and examples used. **Test:** can the child recognize a new illustration without memorizing answer positions? Can they explain or indicate uncertainty with support? Do not infer real-world empathy from correct taps.

### رحلة مطابقة البيئة

**Task to define:** identifying an item, categorizing it, or understanding its relationship to a setting. Some objects reasonably belong in several environments; make the question specific or accept multiple justified matches.

**Recommendation:** compare drag-and-drop with tap-item/tap-destination. Keep the language and visual vocabulary consistent. **Record:** conceptual choices separately from missed drop targets. **Test:** unfamiliar but unambiguous examples; whether failure reflects vocabulary, motor demands, or the relationship itself.

### تتبع مسار الأشكال الاجتماعية / legacy: مسار الأصدقاء

**Task to define:** route planning and motor execution. A story about two friends does not itself make a maze a measure of social skills.

**Recommendation:** show start and destination clearly; preview an example; vary route complexity separately from corridor width. **Record:** chosen route, boundary contacts, restarts, assistance, and input mode. **Test:** whether the route is understood before requiring a continuous stroke.

**Shared rule:** label the practice skill in adult reports. Keep diagnosis-based eligibility out of the child's navigation unless a qualified, justified need is established.

## 8. Game research: movement and early learning

These are **proposals for evaluation**, not claims that the games improve attention or motor ability outside the task.

| Intended game | What to investigate | Candidate task-level record |
|---|---|---|
| تتبع النجوم الهادئة | Vary target size, speed, and path separately; allow demonstration and rest. Check whether the hand obscures the target. | Time in target while contact is active; interruptions; speed and target size |
| متاهة توازن الكرة | Decide whether the child draws a route or constructs walls. Make simulation predictable and replayable. | Valid route, collisions, retries; distinguish drawing from simulation outcome |
| زرع بستان الأزهار | Decide whether the goal is sequence knowledge or timed selection. Begin without speed pressure as a hypothesis to test. | Correct sequence choices; skipped items; assistance; timing only in a defined mode |
| كتابة الحروف وتوصيل الكلمات | Separate letter formation, recognition, missing-letter completion, and word-picture matching. | Task type, accepted strokes, recognition choice, hints, word familiarity |
| نسخ الأنماط الهندسية | Pattern inference and shape drawing should be separate modes if both matter. Vary one pattern rule at a time. | Correct continuation on a new pattern; drawing record kept separate |
| الميزان الحسابي البصري | Clarify whether the balance represents equal quantities or merely decorates a multiple-choice sum. | Operation/range, correct first choice, quantity supports, assistance |

**Recommended progression experiment:** change one difficulty factor at a time. Record the active configuration so progress comparisons are interpretable. Do not automatically treat faster responses as better learning.

**Legacy extras:** butterfly selection and the separate focus challenge are not in the intended nine-game set. Evaluate their distinct educational purpose before adding them. The old focus result must not become a diagnostic baseline.

**Review output for each game:** one learning objective, prerequisite skills, successful behavior, likely confounds, examples, accessibility alternatives, and a transfer question. Arabic/math educator review is needed before content production. [S9, S12]

## 9. Feedback, rewards, and understandable reports

**Recommendation:** make feedback informative: what happened, what the child can try, and how to get help. Celebrate completion without making mistakes shameful. Let children repeat, pause, or finish without losing earned work. Explore whether points and animations help this group; do not assume more stimulation means better engagement.

**Finding:** RITEC includes agency and competence as well-being dimensions. Its relevance supports investigating meaningful choice and achievable challenge, not prescribing a particular reward schedule. [S2]

**Proposed metric definitions for discussion**

| Metric | Definition to test | Interpretation limit |
|---|---|---|
| First-choice accuracy | Correct independent first choices / scorable presented items | Needs explicit hint/assistance rules |
| Completion | Finished activities / selected activities | Not mastery; interrupted sessions are separate |
| Active practice time | Time in the task, excluding pauses | Not attention or learning by itself |
| Tracing performance | Task-specific path coverage and deviation | Depends on size, device, pen, and tolerance |
| Response time | Time from eligible prompt to valid response | Confounded by reading, movement, audio, and device delay |

**Recommendation:** show counts alongside percentages, task version, difficulty, input method, and whether help was given. Do not compare a timed stylus task with an untimed tap task as if they measure the same thing. Avoid rankings between children.

**Legacy observation:** 1/10 games completed produced 64% overall progress with no explained formula. The replacement should not inherit that metric without a defined purpose and transparent calculation.

**Parent research task:** give adults a sample report and ask what they think it means and what they would do next. A useful report might say “matched 4 of 6 unfamiliar examples, with two hints,” rather than imply a measured developmental age.

## 10. Physical table and stylus: validate before buying

**Finding:** Android exposes stylus-specific event information and offers low-latency rendering tools. Pressure, tilt, hover, palm handling, and cancellation are capabilities to inspect and test on the actual device; the presence of an API is not proof that a specific pen/screen combination provides usable data. [S13]

**Recommendation: compare candidate hardware with the same small tasks.** No fixed table dimensions, screen angle, pen pressure threshold, or session duration is justified by the desk research reviewed here.

| Test | Observe and record |
|---|---|
| Reach and posture | Can each child reach targets without sustained leaning? Check seated use and mobility needs. |
| Pen control | Grip comfort, left/right hand, dropped pen recovery, hand obscuring content |
| Palm contact | Accidental marks, rejected intentional input, multitouch cancellation |
| Drawing delay | Visible ink following the tip, jitter, gaps, performance during audio/animation |
| Physical environment | Glare, lighting, speaker position, ambient noise, cleaning, cables and stability |
| Session comfort | Voluntary stopping, position changes, fatigue signs; no forced completion |

**Acceptance approach:** agree practical thresholds after supervised observation. Use measured traces and video only with appropriate permission. Evaluate touch as an access alternative, while preserving a separate pen-practice condition if that is the educational objective.

**Legacy boundary:** the emulator established navigation and basic game behavior only. It did not establish table ergonomics, real pen latency, durable hardware safety, or suitability for children.

**Decision needed:** select a hardware candidate and a responsible hardware/ergonomics reviewer before committing to fixed game geometry. A larger screen creates more space, but also potentially longer reach distances.

## 11. Kotlin/Android feasibility findings

**Finding:** the official Ink API supports responsive freehand drawing and separates stroke data from rendering. Android accessibility components provide useful defaults, but custom game canvases need deliberate semantics and testing. Adaptive guidance bases layouts on available window space rather than a device marketing label. [S14, S4, S15]

**Recommendation: run four technical experiments before architecture lock-in.**

1. **Drawing surface:** build a short trace task using the current supported Ink approach. Check cancellation, undo, palm behavior, coordinate scaling, and saved-stroke replay on the real tablet.
2. **Accessible game surface:** expose meaningful selectable elements to accessibility services. Confirm large-text navigation, labels, and alternative selection input. A painted icon is not automatically an accessible control.
3. **Layout:** fit one complete activity at the target screen sizes, including system bars and text scaling. Test rotation/window resizing and ensure gesture input does not accidentally scroll the activity.
4. **Offline result durability:** finish a sample activity, restart the app, and interrupt connectivity. Confirm that results are retained and not duplicated when synchronization resumes.

**Finding:** Android's offline-first guidance treats local data access and synchronization strategy as data-layer concerns. Offline support needs a deliberate write path, not only cached screens. [S16]

**Recommendation:** prototype local profiles, packaged content/audio, and local session recording first; add synchronization only when its user benefit and access model are agreed. This is a proposal, not a selected production architecture.

**No premature promises:** Kotlin alone does not guarantee smoother pen input, accessibility, or correct Arabic rendering. Library versions, minimum Android support, hardware requirements, backend choice, and content licensing must be verified when implementation begins.

## 12. Privacy, consent, and ethical participation

**Finding:** Article 13 of Saudi Arabia's PDPL Implementing Regulation addresses guardians acting for people lacking full or partial legal capacity and verification of guardianship when obtaining such consent. The correct lawful basis, controller obligations, sensitive-data handling, and transfer requirements need review for the actual deployment. A parent math gate is not legal identity or guardianship verification. [S17]

**Recommendations, not a legal compliance certification**

- Inventory data before choosing a backend: child nickname, age band, preferences, task records, guardian account, and optional notes. Justify each field.
- Avoid diagnosis labels and raw behavioral questionnaires unless necessary for a defined purpose. Treat these data as potentially sensitive pending qualified review.
- Separate access protection, research permission, and optional cloud/AI processing consent. State what leaves the device, why, who receives it, and how long it remains.
- Define deletion, withdrawal, export, shared-device access, and retention before cloud launch. Review backups and logs as well as the primary database.
- Do not send real child answers to generative AI merely because the old app exposed that option. First establish necessity, evaluation, contractual terms, and data handling.

**Finding:** UNICEF's ethical-research guidance emphasizes respectful relationships and children's ongoing informed participation. Guardian permission does not remove the need to attend to the child's willingness and withdrawal. [S18]

**Proposed research safeguards:** age-appropriate explanation; guardian permission and meaningful child assent; accessible stop/help signals; no penalty for stopping; optional recordings with separate permission; minimal identifiers; protected notes; an agreed safeguarding/escalation contact. Seek institutional ethics review where applicable.

**Field question:** will the operator be a family, educational institution, or service provider? That choice changes account ownership, access, and operational responsibilities.

## 13. Comparison products and evidence quality

This is a **desk-based comparison**, not a hands-on product audit. Product pages establish advertised patterns, not effectiveness for NUHA's users.

| Reference | What the source describes | What to investigate for NUHA |
|---|---|---|
| Khan Academy Kids | Child learning paths, adult progress views, and teacher assignment/reporting features [S19] | Separate adult/child tasks; understand what parents can act on |
| Feed the Monster / Curious Reader Parents | Provider listing describes phonics/blending games and offline use after setup [S20] | Audio-led interaction, content delivery, offline entry and recovery |
| RITEC toolbox | Child-centered design dimensions and design activities [S2] | Choice, support, inclusion, and child feedback during prototyping |

**Recommendation:** a later hands-on benchmark should use the same tasks: start without adult explanation, repeat an instruction, recover from an incorrect answer, pause, resume offline, and explain a parent report. Record access barriers and task outcomes, not only screenshots or visual preferences.

**Do not copy without checking:** reward systems, assessment labels, privacy statements, account requirements, and curricula. Their context and business model may differ from a Saudi educational table.

**Evidence filter for future research:** prefer traceable publication records, clear age/sample/context, actual outcome measures, and comparison conditions. Separate a vendor's claim, a usability study, and a clinical trial. Ask whether results transfer to the target language and support needs.

**Remaining gap:** no independently inspected interactive-table product supplied sufficient evidence here to recommend a specific hardware vendor or prove table superiority over a tablet. Obtain device demonstrations and evaluate them against the physical-test matrix.

## 14. What to do next: field research and decision gates

Desk research has narrowed the questions, but cannot choose the experience on behalf of children and caregivers. The following is a **proposed formative program**, not a statistically powered effectiveness study.

### Gate A: agree on users and purpose

Interview approximately 4–6 caregivers and 3–4 relevant educators/therapists as a starting recruitment target, adjusted for access and diversity. Include Arabic early-learning and motor/access expertise. Confirm intended ages, support needs, setting, supervision, and what the nine activities should teach. These numbers are planning estimates, not evidence of representativeness.

### Gate B: review three contrasting task prototypes

Use one selection task, one drawing task, and one Arabic-learning task. Invite a small, varied group of children through an approved consent/safeguarding process. Keep sessions flexible; stop when requested or when participation is no longer comfortable. Repeat rounds of revision rather than treating one round as validation.

**Observe:** comprehension without extra explanation, assistance type, error recovery, reach, accidental inputs, voluntary engagement, and stopping. Do not require verbal think-aloud from children who find it burdensome. Offer demonstration, choice, or supported feedback.

### Gate C: validate measurement and technical behavior

Have adults interpret sample reports. Test new examples separately from practiced ones. Verify offline recovery, no duplicate results, Arabic layout, and real stylus behavior. Agree measurable release criteria from these tests; do not invent universal accuracy or session-duration thresholds.

### Gate D: approve scope before implementation expansion

Deliver support profiles, a nine-game objective matrix, Arabic content rules, tested interaction patterns, a data inventory, and an unresolved-questions log. Only then decide which legacy features to keep, change, or omit.

**Immediate owner decisions:** research access and participants; target hardware candidate; language/curriculum reviewer; age-range intent; operating setting; and whether optional touch is allowed. This report does not require a medical diagnosis from participants to study basic usability.

## 15. Source register: design and learning

All sources retrieved or searched on 20 October 2026. Source IDs in the report identify the supporting material. Recommendations remain the author's synthesis.

[S1] W3C. Making Content Usable for People with Cognitive and Learning Disabilities. Working Group Note, 2021. Official guidance; not a child-specific efficacy study. https://www.w3.org/TR/coga-usable/

[S2] UNICEF. RITEC Design Toolbox. Official research-informed design framework; principal framework sample ages 8–12, 787 children, 18 countries. https://www.unicef.org/childrightsandbusiness/workstreams/responsible-technology/online-gaming/ritec-design-toolbox

[S3] Fletcher-Watson et al. A trial of an iPad intervention targeting social communication skills in children with autism. Autism, 2016;20(7):771–782. Abstract reviewed; randomized trial, n=54, under six. DOI: 10.1177/1362361315605624. https://pubmed.ncbi.nlm.nih.gov/26503990/

[S4] Android Developers. API defaults: accessibility in Jetpack Compose. Official platform guidance. https://developer.android.com/develop/ui/compose/accessibility/api-defaults

[S5] W3C. Understanding Success Criterion 2.5.7: Dragging. Explanatory accessibility guidance. https://w3c.github.io/wcag/understanding/dragging.html

[S6] W3C. Web Content Accessibility Guidelines 2.2. Normative web standard; native-app application requires care. https://www.w3.org/TR/WCAG22/

[S7] Stepped-Care Digital Gaming Intervention Enhanced Emotion Recognition in Autistic Children: A Randomized Controlled Trial. 2026 publication record; abstract reviewed, n=36, ages 3–10. https://pubmed.ncbi.nlm.nih.gov/41934558/

[S8] Kollins et al. A novel digital intervention for actively reducing severity of paediatric ADHD (STARS-ADHD): a randomised controlled trial. Lancet Digital Health, 2020. Abstract/record reviewed; n=348, ages 8–12. https://pubmed.ncbi.nlm.nih.gov/33334505/

[S9] Asadi and Asli-Badarneh. The impact of Diglossia-Effect on Reading Acquisition Among Arabic-Speaking Children: A Longitudinal Study. Journal of Psycholinguistic Research, 2023;52:1919–1937. Abstract reviewed; n=137. https://pubmed.ncbi.nlm.nih.gov/37338651/

[S10] Saiegh-Haddad. MAWRID: A Model of Arabic Word Reading in Development. Publication record/abstract, 2017. Theoretical model. https://pubmed.ncbi.nlm.nih.gov/28715927/

## 16. Source register: implementation and safeguards

[S11] W3C. Arabic & Persian Layout Requirements. Draft Note, 2025 version surfaced during research; language-layout reference, not curriculum validation. https://www.w3.org/TR/alreq/

[S12] Institute of Education Sciences / What Works Clearinghouse. Preparing Young Children for School. Practice Guide, August 2022. Official evidence synthesis; preschool educational context. https://ies.ed.gov/ncee/wwc/practiceguide/30

[S13] Android Developers. Advanced stylus features. Official platform guidance; retrieved official excerpts used where direct page fetch failed. https://developer.android.com/develop/ui/views/touch-and-input/stylus-input/advanced-stylus-features

[S14] Android Developers. Add inking to your app with the Ink API. Official platform guidance. https://developer.android.com/develop/ui/views/touch-and-input/stylus-input/about-ink-api

[S15] Android Developers. Get started with adaptive apps. Official layout guidance. https://developer.android.com/develop/adaptive-apps/guides/get-started-with-adaptive-apps

[S16] Android Developers. Build an offline-first app. Official architecture guidance. https://developer.android.com/topic/architecture/data-layer/offline-first

[S17] Saudi Data & AI Authority. The Implementing Regulation of the Personal Data Protection Law, Article 13. Official legal text; retrieved excerpt used, not a comprehensive legal review. https://sdaia.gov.sa/en/SDAIA/about/Documents/ExecutiveRegulations.pdf

[S18] UNICEF Innocenti. Ethical Research Involving Children. Compendium, 2013; official summary and indexed compendium excerpt reviewed. https://www.unicef.org/innocenti/reports/ethical-research-involving-children

[S19] Khan Academy. Khan Academy Kids: family, teacher, and school offerings. Provider description, not independently tested for this report. https://www.khanacademy.org/kids/pricing

[S20] Curious Learning. Curious Reader Parents, Google Play listing. Provider-published product description; offline-after-setup and game descriptions, not independent efficacy evidence. https://play.google.com/store/apps/details?id=org.curiouslearningparent.app

**Local project evidence:** LEGACY_APP_REFERENCE.md (20 October 2026); the nine-page first project-plan report supplied by the owner; and the owner's nine-game description and Kotlin-replacement clarification. Local files are not embedded in this PDF. No private account identifiers or child records are included.

**Before external claims:** obtain full papers for deeper methodological appraisal, resolve the original report's unverifiable citations, and commission qualified review for curriculum, data protection, and any proposed medical claims. This research report establishes design questions and testable recommendations, not clinical or legal certification.
