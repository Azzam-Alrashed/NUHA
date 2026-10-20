# Legacy App Reference: لوحتي الذكية

Explored on **20 October 2026**. This document records the legacy Android app as a reference for the NUHA project. It is not a specification or acceptance checklist for the replacement.

The project owner confirmed that this APK is the old version and that the new version will use **Kotlin**, with substantial attention to UI/UX. Legacy features, game counts, layouts, and technical choices are not automatically requirements for the replacement.

## 1. Sources and evidence boundaries

- [Legacy APK](legacy-app/louhati-v2.4.0.apk): package inspection and hands-on exploration.
- [Original project report](<‎التقرير الأول_ تقييم خطة المشروع_٠٩٢٥٣٤.pdf>): a nine-page project plan, not proof of completed hardware or demonstrated outcomes.
- Project-owner clarifications: the intended nine-game, screen-and-stylus design and the decision to replace the legacy app with Kotlin.

Evidence labels used throughout:

| Label | Meaning |
|---|---|
| **Observed/tested** | Seen in the installed app or APK, or exercised with a confirmed result. Seeing a control does not prove its entire function works. |
| **Displayed but unverified** | A capability described by the interface whose underlying behavior was not independently verified. |
| **Intended design** | Direction supplied in the project report or by the owner; not necessarily implemented. |
| **Unknown** | Not established by the exploration. |

Unless explicitly qualified, descriptions of screen contents below are **observed/tested as visible UI**, not claims that every advertised behavior was completed successfully. Each game has a separate coverage statement.

### Project identity and scope

**Intended design:** NUHA is a smart educational table project for children with autism spectrum disorder, ADHD, and learning difficulties. The proposed system combines a touchscreen, stylus, educational activities, and a progress dashboard. Intended settings include homes, resource rooms, and rehabilitation centers.

**Observed/tested:** the app is branded **لوحتي الذكية**. It describes itself as an educational support platform, not a medical diagnostic tool.

**Unknown:** the formal naming relationship between NUHA and لوحتي الذكية, the new app's final name, whether a physical table has been built, and whether any table hardware is integrated with this APK. No medical effectiveness, diagnostic validity, or condition-specific suitability was established.

## 2. Build and exploration environment

| Item | Observed/tested detail |
|---|---|
| APK filename | `legacy-app/louhati-v2.4.0.apk` (originally supplied as `لعبه جديده.apk`) |
| Application label | لوحتي الذكية |
| Version | `2.4.0` / version code `2010` |
| Package | `com.smartchild.app` |
| Launcher activity | `com.smartchild.app.MainActivity` |
| Framework indicators | `libflutter.so`, `libapp.so`, and `assets/flutter_assets/` in the APK indicate Flutter |
| Declared minimum SDK | API 24 / Android 7.0 |
| Declared target SDK | API 36 |
| Packaged native architecture | `arm64-v8a`; other native architectures were not listed |
| Physical-device check | Successfully installed and opened on a Samsung SM-A075F; detailed exploration was on the emulator |
| Emulator | Existing Pixel Tablet profile, Android API 36 Google Play ARM64 system image, landscape, 2560 × 1600, density 320 |
| Interaction method | ADB-driven taps/swipes and UI hierarchy inspection, supplemented with emulator screenshots |

The emulator reported software graphics rendering under host memory pressure. Its performance is not evidence of performance on a physical tablet. Physical stylus input was not tested.

## 3. Entry and authentication

The welcome screen offers:

- **وضع الطفل**: access to child activities and games.
- **ولي الأمر**: child management, progress, and reports.
- A notice that the platform is educational support and is not used for medical diagnosis.

Choosing child mode while signed out led to a parent login screen. Its notice explains that the parent signs in first and then selects a child profile.

Visible login controls include two input fields, **تسجيل الدخول**, **نسيت كلمة المرور؟**, and **إنشاء حساب جديد**. The owner signed in directly. Registration, password recovery, authentication errors, and session expiry were not tested.

## 4. Parent workflows

### Navigation and dashboard

Parent bottom navigation contains **الرئيسية**, **الأطفال**, **التقارير**, **الإنجازات**, and **الإعدادات**. A menu button and **خروج** are also visible; the drawer contents and logout behavior were not explored.

With no children, the dashboard invites the parent to add a profile. With a profile, it displays a selected-child control, name, age, recommended activity area, overall progress, completed games, average accuracy, activity time, points, focus indicator, and focus-session count.

### Child profile creation

The creation form includes an avatar chooser, name field, age slider from **2 to 6**, and a ten-question parent observation questionnaire. The sample used the default age **4**. Avatar switching and other ages were not tested.

The questions cover:

1. Difficulty responding when called or spoken to.
2. Eye contact during interaction.
3. Observed language or speech delay compared with peers.
4. Difficulty attending to age-appropriate activities.
5. Movement affecting daily activities.
6. Repetition of words or phrases heard from others.
7. Attachment to one toy limiting exploration of other toys.
8. Difficulty learning practiced letters or numbers.
9. Acceptance of hugs or touch when the child wants it.
10. Use of taste to distinguish objects.

Each question offers **نعم**, **لا**, and **غير متأكد**. A remaining-question count was observed. The interface asks parents to report usual behavior rather than choose a condition or diagnosis. It also qualifies the interpretation of letter/number learning and tasting behavior.

**Observed/tested:** a profile named **NUHA test** was created with all ten answers set to “غير متأكد.” Local analysis reported insufficient indicators to favor one area and recommended varied age-appropriate activities. The saved profile showed **أنشطة نمو متنوعة**. The recommendation is an app output, not an independently validated assessment.

### Local and cloud analysis

- **Observed/tested:** local analysis ran and returned the recommendation above.
- **Displayed but unverified:** local analysis is described as not sending questionnaire answers to an external service. No network audit was performed.
- **Displayed but unverified:** optional cloud analysis uses Gemini through Firebase AI Logic after configuration and parent consent. The consent text describes sending age and questionnaire answers, excluding the child's name, and warns about free-tier data use and entering sensitive real-child information.
- The cloud option was not enabled or tested. Recommendation algorithms and difficulty rules are **unknown**.

### Managing profiles

The children page shows profile cards with age and activity area, **إضافة**, **فتح وضع الطفل**, and **خيارات الملف**. The profile menu exposes **تعديل الملف** and **حذف الملف**. Creation and entering child mode were tested; editing and deletion were not.

### Reports and achievements

The reports page includes **نسخ الملخص**, chart sections for the last seven sessions, averages over four weeks, and averages over six months, followed by game summaries and session history. Copying the summary was not tested. Only a small synthetic history existed; long-term chart aggregation was not verified.

The parent achievements page lists:

| Badge | Displayed condition |
|---|---|
| البداية الجميلة | Complete the first game |
| نجم الدقة | Achieve 80% or more |
| المستكشف | Complete three different games |

Badge display was inspected; the complete locking/unlocking behavior was not tested.

### Settings

Visible settings include account information, encouragement sounds described as audio/haptic feedback, an offline teacher-audio check, cloud synchronization, child-mode protection, and logout.

- **Observed/tested:** the audio check reported **82 of 82 clips available**.
- **Displayed but unverified:** extra downloadable audio has no configured download URL; unbundled clips cannot currently be downloaded.
- **Displayed but unverified:** cloud synchronization is enabled and uses Firestore for profiles and game results. Cross-device synchronization and offline conflict handling were not tested.
- Sound and protection settings were not toggled. Haptics and logout were not tested.

## 5. Child workflows

Child navigation contains **الرئيسية**, **ألعابي**, **الأنشطة**, **الإنجازات**, and **حسابي**.

### Home and activity selection

Home shows the child's name/age, overall progress, accuracy, focus and completion indicators, points, completed-session count, daily plan, and shortcuts. Three program cards group the catalog:

| Program | Game count |
|---|---:|
| مهارات التواصل والتفاعل | 4 |
| مهارات الانتباه وتنظيم الحركة | 3 |
| مهارات التعلم المبكر | 3 |

The **ألعابي** catalog describes games as suggested from the questionnaire. The sample's varied-development profile exposed all ten games. Filtering for other profiles was not tested.

### Daily plan

The **أنشطة اليوم** screen describes three short, balanced activities with a break between activities. For the sample profile it listed butterfly selection, emotion matching, and environment matching, preceded by a focus-test entry. Daily-plan selection logic, changes across dates, and enforced breaks were not verified.

Tapping the center of the focus entry on this page did not visibly navigate during exploration. Opening the focus challenge from child home worked. This is an inconclusive interaction to investigate, not a confirmed defect.

### Account and return to parent

**حسابي** shows the profile, age, activity area, and **العودة إلى ولي الأمر**. Returning opened **بوابة ولي الأمر**, an addition question with an answer field, cancel, and an open-parent-mode button. Answering the displayed question correctly returned to the parent dashboard. Incorrect-answer behavior and all alternative exit routes were not tested.

## 6. The ten legacy games

### Shared presentation and controls

Game headers show the game name, activity category, difficulty, and age. The sample profile's games displayed **متوسط • 4 سنوات**. Difficulty adaptation was not tested.

A shared instruction strip exposes spoken instructions, sound enable/disable, and play-music controls. Their labels were inspected; audio quality and every playback control were not verified. Several games accept direct taps even when instructions emphasize drawing or dragging.

### 1. فراشة التركيز — Butterfly selection

- **Category:** مهارات التواصل والتفاعل, despite the attention-oriented title.
- **Instruction:** observe butterflies and choose the differently colored one.
- **Observed interface:** animated butterfly field; round **1 of 7**; a child character and progress bar.
- **Input:** target selection by touch is implied by the instruction.
- **Coverage:** opened and visually inspected; one selection was attempted. The immediate screenshot did not establish advancement or scoring. No completion was verified.

### 2. توصيل المشاعر — Emotion matching

- **Category:** مهارات التواصل والتفاعل.
- **Instruction:** recognize the emotion and connect the child to the matching feeling; drawing from the character to the card is suggested.
- **Observed interface:** **4 questions**, three answer cards per question, character/expression cue, and progress bar.
- **Tested input:** direct taps on answer cards successfully advanced questions.
- **Observed correct sequence:** سعيد, حزين, غاضب, خائف. Text cues included joy, sadness, angry eyebrows, and wide eyes.
- **Coverage:** completed all four questions; positive feedback, result screen, and the matching parent report were verified.

### 3. رحلة مطابقة البيئة — Environment matching

- **Category:** مهارات التواصل والتفاعل.
- **Instruction:** drag each item to the environment it belongs to.
- **Observed labels:** مطر, بحر, حديقة, شمس.
- **Input:** drag-and-drop described by the screen.
- **Coverage:** opened and inspected. Successful drops, error feedback, round count, and completion were not tested.

### 4. مسار الأصدقاء — Friends' path

- **Category:** مهارات التواصل والتفاعل.
- **Instruction:** start at the first child and draw a safe path to the friend.
- **Controls:** **تحقق من المسار** and **مسح وإعادة الرسم**.
- **Input:** path drawing.
- **Coverage:** instructions and controls inspected; path validation, obstacle handling, and completion were not tested.

### 5. تتبع النجمة الهادئة — Calm star tracking

- **Category:** مهارات الانتباه وتنظيم الحركة.
- **Instruction:** stay near the moving star and keep the pen inside the circle.
- **Observed interface:** timed progress; the remaining-time text showed **14 seconds** when inspected. This does not establish the total duration.
- **Input:** the screen explicitly permits stylus or finger and emphasizes calm, accurate movement.
- **Coverage:** opened and inspected, then left before a result was verified. Tracking accuracy and completion behavior remain untested.

### 6. متاهة توازن الكرة — Ball balance maze

- **Category:** مهارات الانتباه وتنظيم الحركة.
- **Instruction:** draw a route from the start to the goal while avoiding a hole.
- **Controls:** **تشغيل الكرة** and **مسح المسار**.
- **Input:** draw a path, then run the ball.
- **Coverage:** opened and inspected; collision behavior, success conditions, wall-building, and a time limit were not verified.

### 7. بستان الأزهار — Flower garden

- **Category:** مهارات الانتباه وتنظيم الحركة.
- **Instruction:** touch numbers in order; the first prompt said to touch **1** only and not rush.
- **Observed interface:** large number tiles with individual **اسمع الرقم** controls. Tiles 3 and 5 were visible in the initial viewport, while the requested 1 was not.
- **Input:** number selection by touch; the catalog associates ordered selections with flower growth.
- **Coverage:** opened and inspected. The full tile set, sequence completion, timing requirements, and growth animation were not tested. Off-screen content may require scrolling; this was not established as a missing target.

### 8. رحلة الحروف العربية — Arabic letters

- **Category:** مهارات التعلم المبكر.
- **Observed interface:** **stage 1 of 28**, letter أ, example أسد with a lion symbol, and a tracing canvas.
- **Controls:** **اسمع الحرف والكلمة**, **تعرف على الحيوان**, **مسح**, and **أنهيت التتبّع**.
- **Input:** stylus or finger tracing. The screen says a recognition exercise follows tracing.
- **Coverage:** opened, scrolled to tracing controls, and attempted a stroke and advancement. Transition to recognition was not verified. Later letters, tracing validation, animal content, and completion were not tested.

### 9. نسخ الأنماط — Pattern completion

- **Category:** مهارات التعلم المبكر.
- **Instruction:** drag the appropriate shape into the blank; direct touch is also explicitly supported.
- **Observed interface:** **4 questions**, a shape sequence, blank, and circle/triangle/square choices.
- **Tested behavior:** selecting a circle completed the initial alternating circle/triangle pattern and advanced to question 2 with positive feedback.
- **Coverage:** one correct answer verified; full completion, incorrect answers, and drag input were not tested. Freehand pattern copying was not observed.

### 10. الميزان الحسابي — Arithmetic balance

- **Category:** مهارات التعلم المبكر.
- **Instruction:** complete addition by moving the correct number to the empty scale pan; long-press a number to hear it.
- **Observed interface:** **4 questions**, a scale, arithmetic prompt, and number choices.
- **Tested behavior:** tapping 2 for **1 + 1** advanced to **2 + 1**, with feedback that the scale was balanced.
- **Coverage:** one correct answer verified. Dragging, long-press audio, incorrect answers, and full completion were not tested. Handwritten number input was not observed.

## 7. Separate focus challenge

**اختبار التركيز** is a separate activity, outside the ten-game catalog. Child home calls its entry **تحدّي التركيز**.

The introduction asks the child to tap stars quickly and accurately while ignoring faint background circles and avoiding taps outside stars. It explicitly calls the result a training measure, not a medical diagnosis.

**Observed/tested:** the challenge started from home, displayed appearing stars, remaining time, correct/missed counters, and a progress bar, then reached a result screen. Automated inspection took time while the task was running; targets were missed. No meaningful child-performance measurement occurred.

The result screen displayed **6% score**, **6% accuracy**, **00:22**, **14 attempts**, **one star**, and **+10 points**, with a saved-result confirmation. The formula producing 6%, the meaning of attempts in this activity, response-time weighting, and persistence in a separately inspected parent report are not established.

## 8. Results, progress, and rewards

### Completed emotion-session evidence

| Field | Observed/tested value |
|---|---|
| Questions answered | 4 |
| Score | 100% |
| Accuracy | 100% |
| Time | 00:28 |
| Attempts on result screen | 4 |
| Stars | 5 |
| Points awarded | +15 |
| Parent history | 100% accuracy, 28 seconds, 0 incorrect attempts |
| Per-game summary | 1 play, average 100%, best 100% |

The result screen included congratulations, encouragement, a saved-progress message, **العب مرة أخرى**, and **اللعبة التالية**. Replay and next-game actions were not tested. “Attempts” on the result screen and “incorrect attempts” in history are distinct labels; they should not be treated as the same metric.

### Dashboard interpretation limits

After the emotion game, before the focus challenge, the parent dashboard showed **1/10 games completed**, **100% average accuracy**, **15 points**, **0 minutes activity time**, no focus indicator, and zero focus sessions. The child dashboard showed **64% overall progress**, **100% accuracy**, **0% focus**, and **10% completion**.

**Unknown:** the overall-progress formula, rounding of activity time, treatment of incomplete games, score-versus-accuracy distinctions, repeat-game rewards, and long-term aggregation. The 0-minute display may reflect formatting of a 28-second session, but that explanation was not verified. Overall progress must not be presented as a simple completed-games percentage.

The child achievements screen lists **البداية الجميلة**, **نجم الدقة**, and **مستكشف الألعاب**. The last label differs from **المستكشف** on the parent page.

### Test data retained

The authorized synthetic **NUHA test** profile remains in the account. One emotion session was completed and verified in parent reports; one focus session reached a screen confirming its result was saved. Other games were opened or partially attempted. No cleanup or deletion was performed. These records must not be interpreted as real-child observations.

## 9. Interaction and accessibility observations

**Observed/tested:** Arabic text and predominantly right-to-left layouts, large cards, bottom navigation, cartoon/emoji elements, progress bars, visual encouragement, touch-based answers, and scrollable pages. Some essential controls, including letter-tracing actions and result actions, extended below the initial landscape viewport. The game catalog and reports also required scrolling.

**Displayed but unverified:** physical stylus support, spoken instructions and examples, long-press number audio, play music, audio/haptic encouragement, and offline playback. The 82/82 audio check confirms the app's availability report, not that every clip was listened to or judged intelligible. The APK also contains letter/number/word recordings, game sounds, music, and other prompt assets; the check's scope should not be equated with every packaged audio asset.

**Unknown:** screen-reader usability, focus order, scalable text, contrast compliance, alternative input, color-independent answers, reduced-motion options, sensory suitability, palm rejection, stylus latency, and performance on the physical table. Some visible controls were exposed to Android UI inspection, but that is not a comprehensive accessibility test. Animated screens sometimes prevented UI hierarchy snapshots from reaching idle; screenshots were used instead.

## 10. Installation and running the legacy APK

These instructions concern the existing APK only. They are not Kotlin-project or Flutter-source setup instructions.

### Requirements and tested path

- Android device or emulator compatible with the APK's ARM64 native libraries and declared minimum API 24.
- Android SDK Platform Tools (`adb`) available on the host.
- For USB installation: unlock the phone, enable Developer options and USB debugging, use a data-capable cable, and accept the debugging authorization prompt.
- For an emulator: an installed Android Emulator and suitable ARM64 AVD/system image. An existing Pixel Tablet API 36 AVD was used; creating or downloading an AVD was not part of this walkthrough.

The following reproduces the commands used, with a placeholder instead of a private device identifier. Run from the repository root and replace `DEVICE_ID` with the intended target listed by ADB:

```sh
adb devices -l
adb -s DEVICE_ID install -r legacy-app/louhati-v2.4.0.apk
adb -s DEVICE_ID shell am start -W -n com.smartchild.app/com.smartchild.app.MainActivity
```

`-r` requests replacement of an existing installation while retaining its data. A successful compatible installation was observed; signature conflicts and downgrade handling were not tested.

To start an existing tablet AVD, substitute its actual name:

```sh
emulator -list-avds
emulator -avd TABLET_AVD_NAME -no-snapshot-save
```

Wait for Android to finish booting before installation. An early installation attempt returned “device is still booting”; retrying after boot succeeded. Use an explicit target when both phone and emulator are connected.

The APK installed and launched successfully on both tested targets. Parent sign-in is required to reach child profiles. Full offline operation, first-login network requirements, minimum-Android execution, and installation on other hardware were not tested. No dependency installation, source build, or backend-provisioning instructions are inferred from the APK.

## 11. Original design versus legacy implementation

**Intended design:** the report and owner describe nine activities using screen and stylus only, without controllers, smart mats, or external physical props. The report proposes table hardware including touchscreen, stylus, ambient LED lighting, speakers, and storage. These physical capabilities were not verified. The report's sketch is a concept illustration, not evidence of a completed device.

| Intended nine-game design | Legacy counterpart | Observed difference or limit |
|---|---|---|
| تلوين المشاعر | توصيل المشاعر | Emotion choice/connection; no verified coloring flow |
| رحلة مطابقة البيئة | رحلة مطابقة البيئة | Dragging described; successful line drawing/drop not tested |
| تتبع مسار الأشكال الاجتماعية | مسار الأصدقاء | Safe-path drawing controls; validation untested |
| تتبع النجوم الهادئة | تتبع النجمة الهادئة | Moving-target tracking; full scoring untested |
| متاهة توازن الكرة | متاهة توازن الكرة | Draw route then run ball; walls/time limit not verified |
| زرع بستان الأزهار | بستان الأزهار | Ordered numbers; required speed not verified |
| كتابة الحروف وتوصيل الكلمات | رحلة الحروف العربية | Letter tracing and advertised recognition; missing-letter words/image matching not verified |
| نسخ الأنماط الهندسية | نسخ الأنماط | Shape selection/dragging rather than observed freehand copying |
| الميزان الحسابي البصري | الميزان الحسابي | Number selection rather than observed handwriting |

The legacy app adds **فراشة التركيز** relative to that list, plus the separate focus challenge. Inclusion of either in Kotlin has not been approved merely by documenting them.

The report groups games by ASD, ADHD, and learning difficulties; the app uses skill-area categories. The report targets ages **4–10**, while the installed profile form permits **2–6**. Neither discrepancy is resolved here. Goals concerning attention, patience, motor precision, or impulse control are design intentions, not demonstrated benefits.

## 12. Open questions and Kotlin UI/UX considerations

### Unresolved product and technical questions

- What are the final project/app names and the physical table's implementation status?
- Which age range, game roster, and mechanics apply to the replacement?
- How are recommendations, difficulty, score, accuracy, focus, and overall progress calculated?
- What account creation, child-data retention/deletion, consent, and synchronization policies apply?
- What offline behavior and table/stylus hardware are required?
- Which accessibility needs will be explicitly designed for and tested?

### Observations to investigate, not confirmed defects

- Essential controls or requested targets can be outside the initial tablet viewport.
- A central tap on the daily-plan focus entry did not open it, while home entry worked.
- Overall progress was not explained by the visible completed-game count.
- Some instructions emphasize gestures even though direct answer taps work.
- Different pages use slightly different achievement names.

### Candidate redesign considerations, not implementation commitments

Consider keeping active game tasks and primary actions visible together; making available input methods explicit; using consistent labels and navigation; explaining progress metrics; providing clear feedback and parent/child boundaries; and validating Arabic layouts, accessibility, touch, and physical stylus use on the target hardware.

These are discussion inputs for the Kotlin UI/UX effort. This document does not choose a UI framework, architecture, schema, API, or roadmap.

### Coverage limitations

No complete coverage was performed for all stages, wrong answers, replay, deletion, profile editing, registration, recovery, session expiry, cloud analysis, synchronization across devices, fully offline use, or accessibility. Only the emotion game was completed and cross-checked against parent reports; the separate focus challenge also reached a result. The remaining game coverage is limited as stated individually above.
