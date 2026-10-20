# Interaction design, layout and components for NUHA (child + adult UI)

Scope: touchscreen/stylus interaction for young children (2–6 / 4–10), including autism, ADHD and learning difficulties; parent/educator UI; platform policy; Jetpack Compose mapping. NUHA context: Arabic-only RTL, landscape tablet / smart table, touch + stylus, 72dp minimum targets, fixed layout (instruction strip top / task middle / action bar bottom, no scrolling during tasks).

Evidence labels used below: **[EMP]** empirical study (sample/ages given where known), **[GUIDE]** official guideline/policy, **[PRAC]** practitioner/opinion/secondary source, **[WEAK]** weak or unverified evidence.

Research-process note: the two key primary PDFs (Anthony et al. IJHCS 2019 MTAGIC synthesis; Anthony et al. JPUC 2013) were downloaded but could not be text-extracted in this environment, so their numeric recommendations are NOT in these notes; only their abstracts/metadata are. The Vatavu et al. 2015 paper (ages 3–6) full text was not reachable either. Numbers for these appear only in Gaps.

---

## 1. Touch target size, spacing, gestures, stylus vs finger

### Takeaway
Empirical work consistently shows children (especially under ~6) miss small targets far more than adults, improve sharply between 3 and 6, and handle single tap best; double-tap, long-press, pinch/multi-touch and sustained drags are unreliable under ~5. Official minimums (48dp Material, 24 CSS px WCAG AA) are adult baselines and far too small for preschoolers, so NUHA's 72dp is a reasonable floor but likely should go larger (and be generously spaced) for ages 2–4; no verified source gives a child-specific mm figure.

### Cited Findings
- [EMP] Anthony et al. (2013, ages 7–10 vs adults): children miss onscreen targets significantly more than adults, especially smaller targets; targets tested were 100/60/40/20 px squares (CHI 2012 precursor). — [Anthony et al. JPUC 2013 PDF](https://lisa-anthony.com/wp-content/uploads/2013/04/anthony-et-al-jpuc2013.pdf); [Brown & Anthony CHI 2012](https://lisa-anthony.com/wp-content/uploads/2012/03/brown-and-anthony-chi2012eist.pdf)
- [EMP] MTAGIC 5-year synthesis (Anthony et al., IJHCS vol. 128, 2019): six studies, >180 participants (116 children), on how children touch targets and gesture vs adults; paper ends with a set of cross-study design recommendations (not extracted here). — [UF INIT lab abstract](https://init.cise.ufl.edu/?p=2686); [preprint PDF](https://init.cise.ufl.edu/wp-content/uploads/sites/378/2019/03/anthony-et-al-IJHCS2019-MTAGIC-final-preprint.pdf)
- [EMP] Vatavu, Cramariuc & Schipor (2015), "Touch interaction for children aged 3 to 6 years: Experimental findings and relationship to motor skills," IJHCS 74:54–76, doi:10.1016/j.ijhcs.2014.10.007; cited by Woodward et al. (CHI 2016) as showing children's tapping ability improves significantly between ages 3 and 6. — [Woodward et al. CHI 2016](https://init.cise.ufl.edu/wp-content/uploads/sites/378/2017/05/Woodward-et-al-CHI2016_0.pdf); [scholarhub record](https://scholarhub.vn/publication/:slug/c9d9c904-dd34-4e07-864a-dd650132390a)
- [EMP] Vatavu, Anthony & Brown (INTERACT 2015): touch measurements alone classify child (3–6) vs adult with 86.5% accuracy (119 participants, 89 aged 3–6) — i.e., young children's touch is measurably different from adults'. — [Lisa Anthony blog](https://lisa-anthony.com/2015/06/05/interact-2015-paper-accepted-on-detecting-child-users-from-touch-input/)
- [EMP] Woodward et al. (CHI 2016) studied how interface complexity (more/denser targets) affects children's touchscreen interactions. — [Woodward et al. CHI 2016](https://init.cise.ufl.edu/wp-content/uploads/sites/378/2017/05/Woodward-et-al-CHI2016_0.pdf); [Academia.edu copy](https://www.academia.edu/57068346/Characterizing_How_Interface_Complexity_Affects_Childrens_Touchscreen_Interactions)
- [EMP] Woodward et al. (AVI 2020, ages 5–10): Fitts' law with nominal target widths modelled children's finger pointing better than the FFitts variant. — [Woodward et al. AVI 2020](https://init.cise.ufl.edu/wp-content/uploads/sites/378/2021/05/woodward-et-al-AVI2020.pdf)
- [EMP] Pre-kindergarten (ages 2–3) multi-touch gesture study (IJHCS 73:37–51, 2015, UPV Valencia group): double tap, long press, scale-down and two-finger rotation caused cognitive/precision problems; one-finger rotation and two-finger scale-up were performed about as well as tap and drag. — [RiuNet UPV record](https://riunet.upv.es/handle/10251/64752?show=full); [hebis record "Multi-touch gestures for pre-kindergarten children"](https://ubmr.hds.hebis.de/EdsRecord/edscal%252Cedscal.28891981)
- [EMP, WEAK — thesis] 4- vs 5-year-olds on iPad (point-and-touch, drag-and-drop, zoom, rotate): 5-year-olds outperformed 4-year-olds in all four; drag-and-drop design must account for target size. — [MNSU thesis 2014](https://cornerstone.lib.mnsu.edu/etds/354)
- [EMP] Children (older, CHI 2011, point-and-click on PC) already expected drag-and-drop; deviating from that expectation caused usability problems. — [TU/e record](https://research.tue.nl/en/publications/children-may-expect-drag-and-drop-instead-of-point-and-click/)
- [PRAC] Children under ~5 often lose contact mid-drag; recommends "tap-and-tap" (tap item, then tap destination) as alternative; children notice activation slowly → held-over touches and accidental double taps. — [uxdesign.cc "Design Considerations for Little Fingers"](https://uxdesign.cc/design-considerations-for-little-fingers-ad2a19ed3816)
- [GUIDE/EMP-synthesis] Sesame Workshop (2012; synthesizes >50 internal studies): tap is the most intuitive gesture; pinching, flicking, tilting/shaking, multi-touch and double tap are harder; register taps on touch-down for immediate feedback; support partial completion of tracing/dragging because kids lift fingers. — [Joan Ganz Cooney Center summary](https://joanganzcooneycenter.org/?p=19607); [The Register coverage](https://www.theregister.co.uk/2012/12/20/sesame_street_touch_apps_best_practice/); [Licensing International](https://licensinginternational.org/news/sesame-workshop-shares-tips-for-creating-touch-screen-content-for-preschoolers)
- [EMP, WEAK — secondary] NN/g children's UX research (ages 3–12; split 3–5, 6–8, 9–12): 3–5-year-olds favour tapping and swiping; dragging/scrolling/complex coordination comes ~9+. Sample "125 children, US/Israel/China" is only from a secondary blog. — [UserTesting summary](https://www.usertesting.com/blog/ux-for-kids); [Skillbox (RU) summary](https://skillbox.ru/media/design/uxdizayn-dlya-detey/)
- [GUIDE] WCAG 2.2 SC 2.5.8 Target Size (Minimum, AA): ≥24×24 CSS px, or undersized targets spaced so a 24px circle on each doesn't intersect another; exceptions: equivalent, inline, user-agent, essential. 2.5.5 (Enhanced, stricter) recommended for important controls. — [W3C Understanding 2.5.8](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html)
- [GUIDE] Android/Material: interactive elements ≥48dp; Compose Material components pad to 48dp only when they have a click handler; small clickables get an expanded touch area beyond bounds — so set explicit sizeIn() to avoid overlapping touch areas between neighbours. — [Android Compose accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- [EMP, WEAK] Multimedia University study, 103 preschoolers, Fitts' law game on tablet vs all-in-one multi-touch PC: tablet screen size suited preschoolers best (no target dimensions in abstract). — [MMU repository](https://shdl.mmu.edu.my/11249/)
- [EMP] Stylus vs finger — FittsFarm (Cassidy, Read, MacKenzie, INTERACT 2019, 28 children; ages not confirmed in excerpt): drag-and-drop with stylus ~9% higher throughput; error rate finger 12.6% vs stylus 9.3%; movement time n.s.; authors suggest low-cost styluses for schools. — [IFIP DL](https://dl.ifip.org/IFIP-LNCS/hal-02553903v1); [UCLan repository](https://clok.uclan.ac.uk/30143); [MacKenzie page](https://www.yorku.ca/mack/interact2019.html)
- [EMP, WEAK — seen only via repost] Preschool handwriting (41–65 months, 8 weeks; paper/pencil vs tablet-finger vs tablet-stylus): finger group wrote more letters correctly than stylus group; stylus ≈ paper. Original journal article not verified. — [repost thread](https://forums.freebsd.org/goto/post?id=429147); [Research Connections record](https://researchconnections.org/childcare/resources/32955)

### Inferences
- Keep 72dp as an absolute floor for secondary controls; for primary task targets for ages 2–5, use substantially larger (e.g., 96–120dp+, roughly 15–20mm on a typical tablet) with ≥16–24dp gaps — this is extrapolation from "children miss small targets much more" + Sesame/NN/g, not a verified figure.
- Gesture vocabulary for child mode: single tap (fire on press/touch-down with immediate visual+audio feedback); simple drag only with forgiving drop zones, snap-to-target, and tap-tap fallback; no double-tap, long-press, pinch, multi-finger, or flick as required interactions. Debounce rapid repeat taps (holdover).
- Tracing/drawing with stylus should accept partial strokes, tolerate lifts, and use palm rejection (Android stylus `MotionEvent.TOOL_TYPE_STYLUS`; filter `TOOL_TYPE_FINGER`/large-contact touches while stylus is active — technical detail from author knowledge, verify). Stylus evidence is mixed: better precision for older kids on drag, but preschool handwriting results favoured finger → offer both, don't require stylus for under-5s.
- WCAG 2.5.7 (Dragging Movements) — any drag must have a single-pointer non-drag alternative — aligns with the tap-tap recommendation (2.5.7 text itself not fetched; from author knowledge).

### Gaps
- Exact numeric recommendations (target mm, edge padding, miss rates by age) from Anthony et al. 2013/2019 and Vatavu et al. 2015 could not be extracted (PDF text extraction failed / paywall). Writer should not cite specific mm values from these papers without checking.
- No verified study for children ≤2.5 years on target size.
- No study found on palm rejection or stylus grip in 2–4-year-olds specifically; no research on large "smart table" form factors (horizontal surfaces) for young children was retrieved.

---

## 2. Layout density, predictability, navigation depth (incl. autism/ADHD/cognitive)

### Takeaway
Guidance converges on few choices per screen, consistent placement, shallow navigation and audio-led instructions; TEACCH-style structure/predictability is widely used for autism but its evidence base as a package is rated weak, so treat it as a sensible design rationale rather than proven efficacy.

### Cited Findings
- [EMP] Interface complexity (number/density of targets) affects children's touch performance (Woodward et al., CHI 2016). — [Woodward et al. CHI 2016](https://init.cise.ufl.edu/wp-content/uploads/sites/378/2017/05/Woodward-et-al-CHI2016_0.pdf)
- [GUIDE] Sesame: avoid hotspots along the bottom edge and in lower-left/right corners because preschoolers rest wrists/palms there and trigger accidental actions/exits; design for landscape (preschoolers hold tablets landscape). — [Cooney Center](https://joanganzcooneycenter.org/?p=19607); [The Register](https://www.theregister.co.uk/2012/12/20/sesame_street_touch_apps_best_practice/)
- [GUIDE] Sesame: greet with a character/friendly adult; give instructions up front by voice with visual reinforcement; use time-outs that re-prompt what to do next. — [The Register](https://www.theregister.co.uk/2012/12/20/sesame_street_touch_apps_best_practice/); [Intel/MCV summary](https://mcvuk.com/development-news/creating-great-apps-for-children)
- [EMP-synthesis] Soni et al. (IDC 2019) review of 31 papers → 57 design recommendations for children's touchscreens, mostly mapped to Piaget's pre-operational stage (2–7). — [Soni et al. IDC 2019 talk](https://init.cise.ufl.edu/wp-content/uploads/sites/775/2019/06/soni-et-al-idc2019-talk.pdf)
- [GUIDE] TEACCH structured teaching = physical structure, visual schedules, work systems, visual structure, aimed at predictability with flexible routines. — [NDIS/Autism CRC TEACCH review](https://dataresearch.ndis.gov.au/research-and-evaluation/early-interventions-and-high-volume-cohorts/evidence-review-early-interventions-children-autism/teacch-interventions); [Indiana IIDC visual schedules](https://www.iidc.indiana.edu/doc/resources/structured-teaching-strategies-visual-schedules.pdf); [Reading Rockets](https://www.readingrockets.org/topics/autism-spectrum-disorder/articles/structured-teaching-strategies-students-autism-spectrum)
- [EMP-synthesis, evidence caution] Autism CRC (2020) rated TEACCH evidence weak (null effect on social-communication in the one included review; no evidence for most outcomes); a 2014 synthesis found none of the four components met evidence-based-practice criteria on their own. — [NDIS/Autism CRC](https://ndis.gov.au/about-us/research-and-evaluation/early-childhood-interventions-research/autism-crc-early-intervention-report/teacch-interventions); [Autism CRC](https://www.autismcrc.com.au/node/64441)
- [GUIDE, non-normative] W3C COGA "Making Content Usable" (Working Group Note, supplemental to WCAG): use familiar icons/terms/patterns; clear layout with visual cues, headings and regions; easy navigation; design to prevent mistakes. — [W3C COGA](https://www.w3.org/TR/coga-usable); [UK DfE COGA summary](https://accessibility.education.gov.uk/guidelines/coga)

### Inferences
- NUHA's fixed three-band layout (instruction / task / action bar) matches predictability principles. But Sesame's wrist-rest finding argues against putting child-tappable controls in the bottom corners/edge: keep the bottom bar, but place its child-facing buttons inset from the edge (e.g., ≥ ~24–32dp bottom margin, avoid extreme corners) and put destructive/exit controls nowhere in child reach. On a horizontal smart table, the "bottom edge" is wherever the child's forearms rest — the nearest edge — so this matters more.
- Choices per task: 2–4 options for 2–4-year-olds, up to ~4–6 for older children; one task per screen; no scrolling (already NUHA policy). These counts are design judgement, not found in a verified source.
- Navigation depth for pre-readers: ≤2 levels in child mode (home → activity), always same "home" position; visual schedule/progress strip ("first-then") supports autistic children's predictability and ADHD task completion.
- Warn of transitions (audio/visual cue before screen change), avoid surprise pop-ups; this aligns with COGA and TEACCH rationale but no NUHA-specific evidence.

### Gaps
- No peer-reviewed studies retrieved on touchscreen implementations of visual schedules or on optimal number of choices per screen for preschoolers; no ADHD-specific app layout study retrieved.

---

## 3. Iconography for pre-readers

### Takeaway
Pre-readers need icons that are concrete/familiar, paired with spoken audio labels (and text for adults/older kids); abstract icons and text-only labels should be avoided. Evidence here is mostly guideline-level.

### Cited Findings
- [GUIDE] Provide dialogue and visual reinforcement for children who can't read; offer narration on/off in books. — [Cooney Center](https://joanganzcooneycenter.org/?p=19607)
- [GUIDE] COGA: use icons, symbols and design patterns already familiar to users; clear visual cues. — [W3C COGA](https://www.w3.org/TR/coga-usable)
- [GUIDE] Apple: for pre-literate children, consider a voiceover prompt telling kids they need to involve their parent (at gates). — [Apple Kids apps](https://developer.apple.com/app-store/kids-apps/)
- [GUIDE] Compose: Image/Icon have no automatic description — pass localized contentDescription (null for decorative). — [Android Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)

### Inferences
- Every child-facing control = large concrete pictogram + Arabic text label (for parents/older kids) + tap-to-hear Arabic audio label. Avoid abstract glyphs (hamburger, gear, share) in child mode. Mirror directional icons for RTL (back/next arrows) but do not mirror non-directional icons.
- Use one consistent "character guide" voice for instructions; repeat instruction button (speaker icon) in a fixed spot in the instruction strip.

### Gaps
- No empirical study retrieved comparing icon-only vs icon+audio recognition in 2–6-year-olds, nor on Arabic-speaking children's icon recognition.

---

## 4. Preventing accidental exits/actions; parental gates; store policies

### Takeaway
Apple requires parental gates (adult-level tasks) for link-outs, purchases and other distractions in Kids Category apps; Google Play's Families policy focuses on ads, data and SDKs and does not (in the text retrieved) mandate a parental gate. There is no independent research on gate effectiveness; math gates are weak against older children (8+).

### Cited Findings
- [GUIDE] Apple App Review 1.3: Kids Category apps must not include links out, purchasing opportunities or other distractions unless in a designated area behind a parental gate; no third-party analytics/ads; must not send PII/device info to third parties. — [App Review Guidelines](https://developer.apple.com/app-store/review/guidelines/)
- [GUIDE] Apple 5.1.4(b): the parental gate is not the same as obtaining verifiable parental consent under privacy laws. — [App Review Guidelines](https://developer.apple.com/app-store/review/guidelines/)
- [GUIDE] Apple: parental gates are "adult-level tasks" (examples: math, answering a question); age bands 5 and under, 6–8, 9–11. — [Apple Kids apps](https://developer.apple.com/app-store/kids-apps/)
- [GUIDE] Google Play Families: child-only apps must not transmit persistent identifiers (AAID, IMEI, MAC, SSID…) or request precise location; only Families-approved SDKs; ads (if any) from self-certified SDKs, no interest-based ads, no ads at launch, rewarded ads closable after 5s, one placement per page; social features need adult controls; mixed-audience apps need a neutral age screen; Teacher Approved is opt-in for compliant apps. — [Play Families Policy](https://support.google.com/googleplay/android-developer/answer/9893335?hl=en); [Families Self-Certified Ads SDK](https://support.google.com/googleplay/android-developer/answer/12918983)
- [GUIDE] Families policy expansion prohibiting anonymous chat apps from targeting children, effective 26 Aug 2026 (per search summary; not re-verified on page). — [Play Families Policy](https://support.google.com/googleplay/android-developer/answer/9893335?hl=en)
- [PRAC] Common gates: math question, press-and-hold, multi-finger swipe; critics: children stumble through simple math by random tapping, 8–12-year-olds solve them easily; one vendor combined math + timed drag and reports (anecdotally) no child bypass in a special-needs app. — [Art & Logic](https://artandlogic.com/gate-external-links-in-kids-apps/); [Solar2D forum](https://forums.solar2d.com/t/apps-rejected-because-of-no-parental-gate/318280); [HTKUltimateParentalGate](https://github.com/carabina/htkultimateparentalgate)
- [GUIDE] Sesame: bottom corners are where kids rest wrists and accidentally exit apps. — [The Register](https://www.theregister.co.uk/2012/12/20/sesame_street_touch_apps_best_practice/)

### Inferences
- NUHA (Android, likely Play Families): put parent area, settings, exit and any external link behind a gate. For ages 4–10, a numeric-question gate is insufficient; prefer e.g. press-and-hold (≥3 s) a small inconspicuous control + a randomized multi-step task with Arabic-numeral instructions in text (not voiced), or PIN set by parent. Note a long-press gate is itself something young children rarely do intentionally (Section 1), which is why it's useful.
- On a dedicated smart table, use Android screen pinning / Lock Task (kiosk) mode to prevent leaving the app (Android device-owner Lock Task — author knowledge, verify for the target device).
- Don't voice the gate's answer; do voice "ask a grown-up" (Apple suggestion).

### Gaps
- No academic study found measuring children's bypass rates for parental gates, nor ethics/privacy research on gates; Play policy text retrieved did not mention gates explicitly.

---

## 5. Separate child vs adult (parent/educator) UI

### Takeaway
Sources recommend designing for joint engagement and providing parent tips/progress tracking, but retrieved material gave no detailed dashboard design patterns from leading apps; the adult area should be a distinct, conventional Material UI behind the gate.

### Cited Findings
- [GUIDE] Sesame: design with parents/caregivers in mind (joint media engagement increases learning); provide parent tips; consider progress trackers; let parents turn off distracting hotspots. — [Cooney Center](https://joanganzcooneycenter.org/?p=19607)
- [GUIDE] Apple: adult sections still must not send PII/device data to third parties without explicit parental consent. — [Apple Kids apps](https://developer.apple.com/app-store/kids-apps/)

### Inferences
- Child mode: custom large-target, full-bleed, high-affordance, audio-led components, no text-dependent navigation. Adult mode: standard Material 3 (48dp targets, NavigationRail in landscape, text-first, denser information, charts/tables), visually distinct (different color scheme/header) so adults and children recognise the mode at a glance.
- Adult reports: lead with plain-language summary ("what your child practised this week"), then per-skill progress over time, then raw session detail; avoid ranking children or deficit framing (judgement, not sourced).

### Gaps
- Did not retrieve primary sources on how Khan Academy Kids, PBS KIDS, Sago Mini, Lingokids, etc. structure parent/teacher dashboards; NN/g and PBS KIDS design guideline documents were not reached directly.

---

## 6. Official guidance index & Jetpack Compose mapping

### Takeaway
Use Material 3 as the base for the adult UI and as infrastructure (theming, semantics) for the child UI, but build custom child components with larger minimum sizes, touch-down feedback and no reliance on gestures beyond tap.

### Cited Findings
- [GUIDE] Material/Android 48dp minimum; Compose expands small clickables' touch area; set sizeIn explicitly to avoid overlap; lift click handling to parent rows for selection controls; contentDescription required on Icon/Image; clickable merges descendant semantics; onClickLabel customises TalkBack hint. — [Android Compose accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- [GUIDE] WCAG 2.2 2.5.8 (24px AA) baseline; 2.5.5 stricter for important controls. — [W3C](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html)
- [GUIDE] COGA note is supplemental, not required for WCAG conformance. — [W3C COGA](https://www.w3.org/TR/coga-usable)

### Inferences (author knowledge — verify against current API docs before relying)
- `LocalMinimumInteractiveComponentSize` (Material3 ≥1.2; replaced `LocalMinimumInteractiveComponentEnforcement`) can be provided as 72.dp (or more) at the child-mode root so all M3 interactive components and `Modifier.minimumInteractiveComponentSize()` enforce it; adult mode keeps 48.dp.
- Custom `MaterialTheme(colorScheme = …, typography = …, shapes = …)` per mode: child mode with large rounded shapes, larger Arabic-friendly typography (e.g., a child-legible Arabic font), high-contrast but not saturated palette; adult mode a calmer standard scheme. `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` is automatic with Arabic locale.
- Reduced motion: read `Settings.Global.ANIMATOR_DURATION_SCALE` (0 = animations off) and expose an in-app "calm mode" toggle in the parent area for sensory-sensitive children.
- Adaptive: `currentWindowAdaptiveInfo().windowSizeClass` — landscape tablet is Expanded width; lock child mode to landscape; use NavigationRail/list-detail for adult dashboards.
- Use `pointerInput { detectTapGestures(onPress = …) }` to give feedback on touch-down (Sesame), and consider triggering the action on press for child taps with debounce to prevent holdover double-activation.

### Gaps
- Material 3 large-screen/adaptive and accessibility pages on m3.material.io, Apple HIG kids page (404 at the fetched URL), PBS KIDS guidelines and NN/g "Children's UX" report were not fetched directly in this pass; Compose API names above are from model knowledge, not verified sources.
