# Arabic Typography, RTL Interface Design, and Saudi/Gulf Cultural Fit for NUHA

Scope: NUHA is an Arabic-only Android tablet app (landscape, touch and stylus, Jetpack Compose) for Saudi children aged 2–6 or 4–10, including autistic children, children with ADHD and children with learning difficulties. Evidence labels used below: **[EMPIRICAL]** = a peer-reviewed or experimental finding; **[GUIDELINE]** = a standards body or platform guideline; **[OPINION/INDUSTRY]** = blogs, vendors or listicles; **[WEAK]** = thin or indirect evidence. Research cut-off: October 2026. Fifteen searches and fetches were run, so several items are marked as gaps for follow-up.

## 1. Typefaces for early readers and children: Naskh vs Kufi vs simplified, legibility, size, spacing

### Takeaway
The only child-specific Arabic legibility evidence found compares "Simplified Arabic" (a simplified Naskh-derived face) against "Traditional Arabic". Simplified Arabic did better on screen at roughly 14–18 pt for ages 9–12 and gave fewer reading errors for Moroccan Grade 3 pupils. No study found compares Kufi or geometric sans styles with Naskh for young children. Arabic dyslexia work suggests spacing, vowelization and diglossia matter more than letterform. Tracking (letter-spacing) must not be applied to cursive Arabic, and tatweel should not be used as a spacing tool.

### Cited Findings
- **[EMPIRICAL]** 33 Moroccan Grade 3 pupils read text in five Arabic fonts, measuring fluency and error rate. The authors recommend Simplified Arabic for reducing reading errors in both printed and on-screen text. The full text is paywalled, so details were not verified. — [Inderscience: A survey about legible Arabic fonts for young readers](https://www.inderscience.com/filter.php?aid=109660)
- **[EMPIRICAL]** Abubaker & Lu (2012) tested Arabic Traditional and Simplified Arabic at four sizes on screen with children aged 9–12. Size 10 was "not readable" for ages 10–12. Simplified Arabic was readable, especially at sizes 14 and 18. — [Huddersfield: Optimum Font Size and Type for Students Aged 9–12](https://pure.hud.ac.uk/en/publications/the-optimum-font-size-and-type-for-students-aged-9-12-reading-ara/); [PDF](https://eprints.hud.ac.uk/id/eprint/16427/1/The_Optimum_Font_Size_and_Type_for_Students.pdf)
- **[EMPIRICAL]** Abubaker's PhD thesis covers the factors that affect on-screen legibility of Arabic e-textbooks for ages 9–13. It frames the problem as display-specific, with several interacting factors. — [Huddersfield thesis](https://eprints.hud.ac.uk/id/eprint/19509/1/thesis_Azzaa.pdf)
- **[EMPIRICAL, adults]** Chahine's Leiden dissertation (2012) used eye tracking with 72 adult readers on three Naskh interpretations (the Afandem family). It found that the complexity of word formation (ligatures and stacked forms) affects legibility, so simpler, less ligature-heavy Naskh reads more easily. Its participants were adults, not children. — [Leiden: Reading Arabic (Chahine)](https://scholarlypublications.universiteitleiden.nl/access/item%3A2937099/download)
- **[EMPIRICAL]** In a dyslexia study, 64 children with dyslexia and 64 without read eight equivalent texts that varied letterform, inter-letter spacing and inter-word spacing. The data "failed to show any effect from the letterform." The authors point to diglossia and vowelization as the factors behind Arabic dyslexia. The author and year were not confirmed from the snippet. — [ResearchGate: Does Arabic Orthography Affect Reading Performance in Children with Dyslexia?](https://www.researchgate.net/publication/337160714_Does_Arabic_Orthography_Affect_Reading_Performance_in_Children_with_Dyslexia)
- **[EMPIRICAL, small/early]** A study of "Arabolexia", a specialized dyslexia font, reports that it improved reading accuracy in children with dyslexia. — [ResearchGate: Arabolexia](https://www.researchgate.net/publication/387269553_The_Effect_of_a_Specialized_Dyslexia_Arabic_Font_Arabolexia_on_Reading_Performance_of_Children_with_Dyslexia)
- **[EMPIRICAL-qualitative]** A 2025 focus-group study gathered dyslexic readers' views on a dyslexia-friendly Arabic typeface. This is perception data, not performance data. — [Taylor & Francis 2025](https://www.tandfonline.com/doi/full/10.1080/24735132.2025.2489179)
- **[OPINION/analysis]** A 2024 visual analysis proposes letterform features for an accessible, dyslexia-friendly Arabic typeface. It discusses which features can be waived and which are mandatory for legibility. — [ResearchGate 2024](https://www.researchgate.net/publication/379581127_Informing_the_Design_of_an_Accessible_Arabic_Typeface_A_Visual_Analysis_to_Identify_Letterform_Features_of_Dyslexia-Friendly_Typefaces)
- **[EMPIRICAL, not Arabic-specific]** Eye-tracking work finds that increased crowding particularly harms phonological decoding in dyslexic children. This argues for generous word spacing and line height. — cited in the [search summary of dyslexia literature](https://www.researchgate.net/publication/337160714_Does_Arabic_Orthography_Affect_Reading_Performance_in_Children_with_Dyslexia). The original crowding paper was not retrieved.
- **[GUIDELINE]** W3C alreq says Arabic stretching "do[es] not correspond to letter-spacing in non-cursive scripts". It also says "moving two joined characters closer to or further from each other creates undesirable results", and that only gaps next to non-joining letters (for example dal and reh) can be adjusted. — [W3C Arabic & Persian Layout Requirements](https://www.w3.org/TR/alreq/)
- **[GUIDELINE]** On tatweel (U+0640), alreq says it is a fixed-width character with "the side effect of widening the letter". For controlling joining, ZWJ is "highly recommended" instead. Kashida justification can create uneven color if overused. — [W3C alreq](https://www.w3.org/TR/alreq/)

### Inferences
- Use a clear, low-ligature Naskh-style or "simplified" face for reading content. This is where the only child-relevant evidence points. Avoid heavy traditional Naskh with stacked ligatures, such as Traditional Arabic-style fonts.
- Simplified or rounded sans and Kufi-derived faces can be used for UI chrome and display text. No evidence was found that they help or hurt early decoding, so test them with real children.
- The 14–18 pt finding is for 9–12-year-olds on older desktop screens. Ages 2–10 on tablets need larger sizes. A reasonable starting point is about 24–32sp for learning words and 18–22sp for UI labels, to be confirmed by in-app testing. This is an extrapolation, not a measured result.
- Never apply `letterSpacing` to Arabic text in Compose, because it breaks joins. Raise word spacing and line height instead: tall line heights are needed anyway to fit harakat.
- Never insert tatweel for visual effect in learning words. It alters letter shapes that children are learning to recognize.
- The dyslexia evidence on letterform is mixed: one study found no effect, while Arabolexia reported gains. Do not promise "dyslexia-friendly font" benefits.

### Gaps
- No study compares Kufi or geometric sans styles with Naskh for pre-readers or Grades K–2 in Arabic.
- No child-specific Arabic x-height or size study on tablets was found.
- Full methods and effect sizes for the Moroccan and Arabolexia studies were not retrieved.
- No Saudi MoE or Madrasati official school font was identified. Saudi textbooks are commonly reported to use Naskh-style fonts with full tashkeel in early grades, but no source was found.

## 2. Open-licensed fonts for Android bundling: license, weights, harakat quality

### Takeaway
Noto Naskh Arabic is SIL OFL 1.1 and widely reported to position marks carefully, which makes it the safest default for vowelized learning text. Amiri and Scheherazade New are reported to handle harakat best but are traditional book or Quranic styles. Cairo and Tajawal are OFL modern sans faces whose diacritic support is rated only "adequate" by informal sources. Baloo Bhaijaan 2 is an OFL, rounded, child-friendly variable family. Readex Pro is a variable sans. No source independently tested harakat collisions, so NUHA must run its own rendering tests.

### Cited Findings
- **Noto Naskh Arabic** is licensed SIL OFL 1.1 and designed with careful mark positioning. — [Fontsource: Noto Naskh Arabic](https://fontsource.org/fonts/noto-naskh-arabic); [arabic-calligraphy-generator listing](https://arabic-calligraphy-generator.com/fonts/noto-naskh-arabic) **[OPINION/INDUSTRY on mark quality]**
- **Cairo and Tajawal** are OFL. A comparison table rates their diacritics "Adequate", and another source describes them as display-leaning faces with possibly "approximate" mark placement. — [The Arabic Keyboard: 10 Best Free Arabic Fonts (2026)](https://www.thearabickeyboard.com/arabic-fonts/) **[OPINION/INDUSTRY]**
- **Scheherazade New** is described as having the best harakat rendering of the fonts compared, with limited weights. **Amiri** is recommended for print and Quranic text and is among the Naskh faces that handle marks well. — [The Arabic Keyboard](https://www.thearabickeyboard.com/arabic-fonts/); [Arabify 2026](https://arabify.io/blog/best-arabic-fonts) **[OPINION/INDUSTRY]**
- **Baloo Bhaijaan 2** is the Arabic member of Ek Type's Baloo 2 project and is licensed SIL OFL 1.1 (© 2019 The Baloo 2 Project Authors). It is variable, with a weight range of 400–800 (Regular to ExtraBold). — [Google Fonts: Baloo 2 about](https://fonts.google.com/specimen/Baloo+2/about); [Fontsource: Baloo Bhaijaan 2](https://fontsource.org/fonts/baloo-bhaijaan-2/about); [Serbyte listing](https://www.serbyte.net/fonts/baloo-bhaijaan-2). Sources conflict on the exact weight count.
- **Readex Pro** has weights 200–700 and is variable on the wght and HEXP axes, with Arabic and Latin subsets. This comes from a third-party listing and the license was not verified, although it is distributed on Google Fonts (normally OFL). — [superdevpro listing](https://superdevpro.com/free-font/readex-pro) **[WEAK]**
- **[GUIDELINE]** Per alreq, multiple diacritics on one base letter should attach to that letter, and fonts usually define special shapes or positions for combinations such as shadda with a vowel. Shadda with kasra may place the kasra under the letter or between the letter and the shadda. Fonts also differ in whether vowel marks sit at a uniform height or adjust to the letter. — [W3C alreq §4.2.7–4.2.8](https://www.w3.org/TR/alreq/)
- **[GUIDELINE/INDUSTRY]** Display fonts can have approximate mark placement, so check the preview at large size. — [The Arabic Keyboard](https://www.thearabickeyboard.com/arabic-fonts/)

### Inferences
- A suggested pairing to validate: Noto Naskh Arabic (or Scheherazade New) for vowelized learning words, with Baloo Bhaijaan 2, Readex Pro or Tajawal for UI and headings.
- Before choosing, render test strings in each candidate at the target sizes in Compose on a real tablet: shadda + kasra, tanween, sukun on tall letters (لّ, كٌ), and stacked marks on lam-alef. Check that marks do not clip, which is common when `lineHeight` is tight or `includeFontPadding` is changed.
- Subset each font to Arabic and the needed digits to keep the APK small, and keep it variable where possible. OFL permits bundling and modification, but a modified font must be renamed.
- Lalezar is display-only (heavy) and Rubik is a geometric sans. Their harakat quality was not evaluated in any source found, so treat them as headline-only if used at all.

### Gaps
- No independent, systematic test of harakat collisions across Google Fonts Arabic families was found.
- Exact license text and weight lists were not verified on Google Fonts for Tajawal, Cairo, Almarai, IBM Plex Sans Arabic, Noto Kufi Arabic, Rubik or Lalezar. These are believed to be OFL (Almarai is believed to be OFL as of its Google Fonts release), but that is unconfirmed here.
- No evidence was found on which font Madrasati, iEN or Saudi MoE textbooks use.

## 3. Full diacritics (tashkeel) for beginning readers

### Takeaway
There is strong, consistent evidence that vowel diacritics support decoding accuracy for beginning and poor readers. Second graders benefit, and fully vowelized Arabic is close to phonemically transparent. Diacritics can slow skilled readers, and they are gradually removed around Grade 4. For NUHA, learning words should be fully vowelized. UI labels can be lightly vowelized, with marks only where a word is ambiguous, and should be paired with audio and icons because many users are pre-readers.

### Cited Findings
- **[EMPIRICAL]** Abu-Rabia (1997) studied 77 readers, 34 of them poor readers, on fully vowelized, partially vowelized and unvowelized text. Vowels and context aided word recognition in both poor and skilled readers. — [Springer 10.1023/A:1007906222227](https://www.doi.org/10.1023/A:1007906222227)
- **[EMPIRICAL, adults]** Abu-Rabia (1998) found that vowelization significantly affected reading accuracy for both poor and skilled adult readers across narrative, informative, poetic and Koranic texts, read correctly vowelized, unvowelized or wrongly vowelized. — [Springer](https://link.springer.com/article/10.1023/A:1025034220924)
- **[EMPIRICAL]** Abu-Rabia (2007) found clear differences between dyslexic and typical readers in Grades 3, 6, 9 and 12 on all reading measures. — [Springer 10.1007/s10936-005-9010-7](https://dx.doi.org/10.1007/s10936-005-9010-7)
- **[EMPIRICAL]** Saiegh-Haddad & Schiff (2016) and Schiff & Saiegh-Haddad (2017) found that second graders benefit from vowel diacritics, which support phonological decoding. Older readers rely more on morphological and lexical knowledge. — [Frontiers in Psychology 2020](https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2020.552315/full)
- **[EMPIRICAL]** Saiegh-Haddad found that diglossic distance still affects kindergarten and first-grade performance even with fully vowelized text. Spoken dialect differs from Standard Arabic, so transparency alone does not remove difficulty. — [Saiegh-Haddad 2003, Linguistic Distance and Initial Reading Acquisition](https://www.researchgate.net/publication/231894994_Linguistic_Distance_and_Initial_Reading_Acquisition_The_Case_of_Arabic_Diglossia)
- **[EMPIRICAL, conflicting]** Vowel diacritics tend to slow word reading, but readers do not ignore them. — [WCCFL 36: Word Reading in Arabic: Influences of Diacritics and Ambiguity](http://www.lingref.com/cpp/wccfl/36/paper3460.pdf). In another study, unvowelized words were read faster and more accurately than vowelized ones. — [Springer 2023](https://link.springer.com/article/10.1007/s10936-023-09969-w). This contradicts the general benefit and may depend on the stimuli and on reader skill.
- **[EMPIRICAL/review]** Vowelization is gradually removed around Grade 4, which shifts readers toward morphological and lexical strategies. Diglossia and orthographic complexity act multiplicatively. — [J. Psycholinguistic Research 2026 critical review](https://link.springer.com/article/10.1007/s10936-026-10214-3)
- **[OPINION/qualitative]** A 2021 paper suggests diacritics could improve reading for dyslexic students. — [IJE Vol. 9 No. 2, 2021](https://airccse.com/ije/papers/9221ije06.pdf)
- **[EMPIRICAL-corpus]** Books vary systematically in how much tashkeel they use. — [ScienceDirect: Principles of variation in the use of diacritics (taškīl)](https://www.sciencedirect.com/science/article/pii/S0388000122000225)

### Inferences
- **Learning content** (letters, words, sentences to decode) should use full, correct tashkeel. Wrong vowelization hurt accuracy in Abu-Rabia 1998, so diacritics need native-speaker QA.
- **UI chrome** is better served by icon plus audio plus a short word. Diacritics on labels such as العب or التالي are optional, so add them only where a label is ambiguous. Pre-readers rely on audio and icons anyway.
- Fully vowelized text needs extra vertical space, so set Compose `lineHeight` to about 1.6–1.8× and test for clipping.
- Diglossia matters: audio narration in Standard Arabic (فصحى) with familiar Saudi vocabulary, or optional dialect prompts for 2–4-year-olds, may reduce the distance between what children hear and read. This is an inference from Saiegh-Haddad's work.

### Gaps
- No Saudi-specific experiment on tashkeel for pre-readers or ages 4–6 was found, and nothing on the effect of tashkeel for autistic or ADHD children.
- The full 1999 Abu-Rabia Grade 2/6 comprehension results were not retrieved.

## 4. Arabic-Indic (٠١٢٣) vs Western (0123) numerals in Saudi education and apps

### Takeaway
W3C lists Saudi Arabia as an Arabic-Indic-digit region, and those digits remain traditional in Saudi print. Saudi Arabia is shifting toward Western digits: the 2023 road-sign manual mandates Western digits on new signs. No source was found confirming which digit form current Saudi primary maths textbooks use. This is the most important open question for NUHA's maths content and must be checked directly against MoE textbooks.

### Cited Findings
- **[GUIDELINE]** W3C alreq lists Arabic-Indic digits (٠–٩) as used in Eastern Arabic countries "such as Egypt, Saudi Arabia, and Iraq". European digits are used in the Maghreb. It also advises against the ambiguous terms "Eastern Arabic"/"Indic numerals". Arabic-Indic numbers use the separators ٫ (decimal) and ٬ (thousands). — [W3C alreq §6.1](https://www.w3.org/TR/alreq/)
- **[GUIDELINE]** In bidi, numbers run left to right inside RTL text. Arabic-Indic digits have bidi class AN and European digits class EN, and their weak directionality affects adjacent %, °, and currency signs. — [W3C alreq §6.1.4](https://www.w3.org/TR/alreq/)
- **[GUIDELINE/official]** The Saudi Highway Code 602 manual (2023, Vision 2030) mandates that new road signs show only Western Arabic numerals. Older signs used Eastern Arabic-Indic digits. — [Wikipedia: Road signs in Saudi Arabia](https://en.wikipedia.org/wiki/Road_signs_in_Saudi_Arabia) (secondary)
- **[WEAK]** Both systems are in concurrent use, and Western digits are "increasingly favoured … particularly in Saudi Arabia". — [Wikipedia: Eastern Arabic numerals](https://en.wikipedia.org/wiki/Eastern_Arabic_numerals). Low-authority sites say business, technology and education use Western digits, while newspapers and currency use Eastern digits. — [industryarabic.com](https://industryarabic.com/numbers-in-arabic/) (unsourced)
- **[CONTEXT]** Saudi Arabia's authorized maths curriculum is similar to McGraw-Hill's series (localized), per the TIMSS 2019 encyclopedia. The digit convention is not stated. — [TIMSS 2019 Saudi Arabia](https://timssandpirls.bc.edu/timss2019/encyclopedia/pdf/Saudi%20Arabia.pdf); [TIMSS 2023 Saudi Arabia](https://timss2023.org/wp-content/uploads/2024/10/Saudi-Arabia.pdf)
- **[CONTEXT]** A 2009 Global Voices piece records Saudi public debate over "Arabic vs Indian" numerals. — [Global Voices](https://globalvoices.org/2009/02/17/saudi-arabia-arabic-and-indian-numerals/)

### Inferences
- Make the numeral system a single app-level setting that maths content and the UI both read from. Do not hard-code either form. Choose the default after checking the current MoE Grade 1 maths book: it is widely believed to use ٠١٢٣ (Hindi digits), but that is unverified here.
- In Compose, format numbers explicitly with locale `ar-SA` and its numbering system (`-u-nu-arab` or `-u-nu-latn`). Do not rely on the device locale, because many Saudi devices are set to English, or to Arabic with Latin digits.
- Never mix digit systems on one screen, since counting tasks depend on consistent glyphs.

### Gaps
- No primary source was found on the digit form used in current (2024–2026) Saudi primary maths textbooks, Madrasati or iEN. Check the MoE textbook PDFs or Madrasati directly.
- No official 2023 MoE announcement on switching numerals was found.

## 5. RTL UI conventions: what to mirror, what not to, Android/Compose implementation

### Takeaway
Mirror anything that encodes reading or time direction: back and forward, next and previous, progress, sliders, list and carousel order, and the direction of step sequences. Do not mirror media playback controls, clocks, checkmarks, symmetric icons, icons containing Latin text or numbers, or charts or graphs whose axes follow mathematical convention. Compose mirrors layouts automatically under RTL. `Icons.AutoMirrored` and `android:autoMirrored` handle directional icons, and `LocalLayoutDirection` overrides direction for specific subtrees.

### Cited Findings
- **[GUIDELINE]** Material says the most important icons to mirror are back and forward buttons. Icons that show forward movement or a sequence of events flip in RTL. The volume slider and its sound waves move to the right in RTL. Undo and redo carry both a linear and a circular meaning, so pick one representation. — [Material Design (M1) Bidirectionality](https://m1.material.io/usability/bidirectionality.html); [M2 Bidirectionality](https://m2.material.io/go/design-bidirectionality)
- **[GUIDELINE]** Material lists media playback controls and charts among elements that are not mirrored. — summarized in the [Android/Material search results](https://m2.material.io/go/design-bidirectionality); [mdui mirror of Material bidi](https://www.mdui.org/en/design/1/usability/bidirectionality.html)
- **[GUIDELINE]** Mozilla's guidelines say not to mirror icons containing text or numbers, symmetric icons (an X close button, a bookmark star), checkmarks, or video and audio player controls. Content that appears in an LTR context should not be mirrored even under an RTL locale. — [Firefox RTL Guidelines](https://firefox-source-docs.mozilla.org/code-quality/coding-style/rtl_guidelines.html)
- **[GUIDELINE]** Wikimedia Codex also has a bidirectionality style guide with icon-flip rules. — [Wikimedia Codex Bidirectionality](https://doc.wikimedia.org/codex/main/style-guide/bidirectionality.html)
- **[GUIDELINE/platform]** `Icons.AutoMirrored` (for example `Icons.AutoMirrored.Outlined.ArrowForward`) flips automatically when the layout direction is RTL. — [Android Developers: Icons.AutoMirrored](https://developer.android.com/reference/kotlin/androidx/compose/material/icons/Icons.AutoMirrored)
- **[GUIDELINE/platform]** In Compose, `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)` excludes a subtree from mirroring. — [Kotlin Multiplatform: Compose RTL](https://kotlinlang.org/docs/multiplatform/compose-rtl.html)
- **[GUIDELINE/platform]** Android has supported RTL mirroring since 4.2 and requires `android:supportsRtl="true"`. Drawables are not flipped automatically, so set `android:autoMirrored="true"` on directional vector drawables. — [Android Developers Blog 2013](https://android-developers.googleblog.com/2013/03/native-rtl-support-in-android-42.html?hl=tr); [ProAndroidDev](https://proandroiddev.com/supporting-rtl-design-on-android-d6ef0ac31874)
- **[OPINION/INDUSTRY]** Persian guidelines consider horizontal pagination arrows semantically ambiguous. This comes from a third-party summary. — [Amsterdam design system CONTRIBUTING](https://cdn.jsdelivr.net/npm/@amsterdam/design-system-assets@2.5.0/CONTRIBUTING.md)
- **[GUIDELINE]** Numbers stay LTR inside RTL text, and in Arabic usage the percent sign is placed before the number. — [W3C alreq](https://www.w3.org/TR/alreq/)

### Inferences, NUHA-specific
- Because NUHA is Arabic-only, force `LayoutDirection.Rtl` at the root rather than inheriting it from the device locale. Otherwise a device set to English will render the app LTR.
- Child-specific "story" sequences (for example, steps of washing hands, or a train of letters) should flow right to left, matching how Saudi children read books.
- Letter-tracing paths and stroke-order animations follow Arabic writing direction, mostly right to left. Never mirror them, because they are content and not layout. Use `LayoutDirection.Ltr`-independent coordinates for stylus tracing canvases.
- Maths: number lines are debated. Some Arabic curricula run them right to left while charts follow Western convention. Verify against the Saudi textbook and keep the choice consistent.
- Keep audio play icons pointing right, the universal convention, per Material and Mozilla.

### Gaps
- The current M3 bidirectionality page could not be fetched (the M2 page returned no content). Its rules were summarized from M1/M2 mirrors.
- No evidence was found on how young Arabic-reading children interpret mirrored versus non-mirrored arrows. This is a usability-testing item.

## 6. Cultural fit for Saudi/Gulf children's content: dress, characters, family, gender, music, religion, settings; regulators

### Takeaway
No Saudi regulator guidance specific to children's apps or music was found. Gmedia (the General Authority for Media Regulation, formerly GCAM) runs age classification for films and games and issued 2025 content rules around social values and the protection of children. Conservative Saudi families commonly seek "no music" or vocal-only content, and Islamic kids' platforms market a music-off option. A research review notes a shortage of culturally and linguistically adapted Arabic apps for autistic children. The practical implications: offer music as off or optional with vocal and SFX defaults, use modest, locally recognizable dress and settings, and avoid religious missteps.

### Cited Findings
- **[GUIDELINE/official]** Gmedia (formerly GCAM) handles media age classification, which it says helps adults choose content suitable for themselves and for children in their care. — [Gmedia: Age classification](https://gmedia.gov.sa/en/age-classification); [Wikipedia: General Authority of Media Regulation](https://en.wikipedia.org/wiki/General_Authority_of_Media_Regulation)
- **[GUIDELINE/official, secondary report]** Game age tiers are 3+, 7+, 12+, 16+, 18+ and 21+, with 21+ added in 2025. — [salmangul.com GCC gaming regulation](https://salmangul.com/gaming-regulation-age-rating-gcc/) **[WEAK, secondary]**
- **[GUIDELINE, secondary report]** Gmedia's September 2025 content rules aim to protect children from exploitation and to safeguard "national identity and social values". They prohibit inappropriate language and bar filming children or domestic workers in everyday content. — [Lexis Middle East](https://www.lexis.ae/2025/09/26/ksa-media-regulator-issues-new-content-guidelines/); [Soul of Saudi](https://soulofsaudi.com/saudi-arabia-media-rules-2025/)
- **[GUIDELINE]** The film clearance and classification mechanism was announced in 2023. — [Al Arabiya 2023](https://english.alarabiya.net/life-style/entertainment/2023/08/02/Saudi-audio-visual-body-unveils-mechanism-for-clearing-cinematic-films); [BroadcastPro ME](https://www.broadcastprome.com/news/saudi-arabias-gcam-reveals-mechanism-for-clearing-and-categorising-films/)
- **[EMPIRICAL-review]** A review of educational apps for autistic children in Arab countries found few existing Arabic-language educational apps for this group. It calls for linguistically and culturally sensitive applications. — [ScienceDirect review](https://www.sciencedirect.com/org/science/article/pii/S1550187624000143)
- **[MARKET EVIDENCE]** Several Saudi-developed Google Play apps offer nasheeds marketed as vocals only, with "no beats or duff". — [Google Play: Islamic Nasheeds Without Music](https://play.google.com/store/apps/details?id=com.Sosy.nashnotiqaa); [Nasheeds No Music](https://play.google.com/store/apps/details?id=com.Sosy.nashidnomus&hl=en_GB)
- **[MARKET EVIDENCE]** Kahf Kids lists a "no-music option" for stricter homes. Miraj Stories' music can be switched off. Durioo+ targets families who prefer vocal-only nasheeds. — [Kahf Kids](https://kahfkids.com/); [Miraj Stories blog](https://www.mirajstories.com/blog/best-islamic-apps-for-kids); [Buruj Academy](https://burujacademy.com/blog/the-best-islamic-apps-for-kids/)
- **[OPINION/religious]** Voice-only nasheeds are widely regarded as permissible, while instrumental music falls under scholarly disagreement. — [Deenback](https://www.deenback.com/blog/how-to-avoid-listening-to-music-in-islam)
- **[CONTEXT]** Gulf Arabic is a dialect group spanning Kuwait, eastern Saudi Arabia, Bahrain, Qatar, the UAE and Oman, with high mutual intelligibility. Najdi and Hijazi Saudi dialects differ from it. — [Adelphi University: Arab-American language evaluation guide](https://home.adelphi.edu/~nslplab/PDFS/BilingualServicestoASHA_WholeResource.pdf)
- **[OPINION/INDUSTRY]** Cultural localization covers imagery, colour, references and religious sensitivity, beyond translation. — [TekRevol blog](https://www.tekrevol.com/blogs/arabic-rtl-app-development-dubai/) (vendor)
- **[EMPIRICAL]** Arabic localized mobile games show recurring cultural and technical problems. — [Cogent Arts & Humanities 2024](https://www.tandfonline.com/doi/full/10.1080/23311983.2024.2307645); [PRISMA scoping review](https://immi.se/index.php/intercultural/article/view/10.36923.jicc.v26i3.1528)
- **[CONTEXT]** A study of digital reading among children in Saudi Arabia exists (details not retrieved). — [ResearchGate](https://www.researchgate.net/publication/373184926_Digital_Reading_among_Children_in_Saudi_Arabia)

### Inferences (design recommendations, not evidence)
- **Audio default:** ship with background music OFF, or a parent choice at first run (موسيقى / بدون موسيقى), with voice, nature sounds and SFX as defaults. This also helps autistic and ADHD children with sensory load. Vocal-only jingles are a safe middle ground.
- **Characters:** locally recognizable dress, such as a thobe and shemagh/ghutra for men and boys, and an abaya and hijab for adult women. Young girls are commonly shown with uncovered hair in Saudi children's media, but modest clothing is the safe default. Use animal or neutral mascots to sidestep gender and dress questions entirely. Family scenes should centre parents, grandparents and siblings, and avoid romantic content and mixed adult socializing.
- **Religious sensitivity:** never place Quranic text, Allah's name or verses on things that are tapped, dragged, erased, stepped on or thrown away in a game. Avoid pigs, dogs as indoor pets, alcohol and crosses or other religious symbols. Avoid idol-like statues. Treat prayer, Ramadan and Eid with respect and accuracy.
- **Local settings and objects:** dates (تمر), dallah coffee pot, majlis, camel, falcon, palm tree, desert, Saudi cities, souq, mosque, Saudi riyal (﷼, with the new 2025 riyal symbol), and Saudi foods such as kabsa.
- **Language:** use Fusha for the reading content, plus optional Saudi-dialect voice prompts for the youngest users, given the diglossia evidence in section 3.
- **Regulatory:** NUHA likely falls outside Gmedia film and game classification unless it is published as a game. Monitor Gmedia, PDPL and NDMO data rules for children's data. This is outside the scope of these notes.

### Gaps
- No official GCAM/Gmedia or MoE "digital content standards" document for children's educational apps was retrieved. The MoE/NCEL (National Center for e-Learning) has e-content standards, but they were not found in this pass.
- No survey data was found on what share of Saudi parents avoid music for children.
- No peer-reviewed study on cultural localization of children's apps in the Gulf specifically was found.
- The guidance on depicting girls and women in Saudi children's media is unsourced convention and needs review by Saudi parents and educators.

## 7. Examples: how Arabic children's apps handle type and culture

### Takeaway
No case studies or documentation were found in this research pass on the typography or cultural choices of Lamsa, Abjadiyat, Adam wa Mishmish, Qatar Foundation apps, Madrasati or iEN. Market evidence from Islamic kids' apps shows a "no music" toggle is a recognized pattern.

### Cited Findings
- Music-off and vocal-only options exist in Kahf Kids, Miraj Stories and Durioo+. — [Kahf Kids](https://kahfkids.com/); [Miraj Stories](https://www.mirajstories.com/blog/best-islamic-apps-for-kids)
- A vendor blog describes an Alif Play project for Arabic-speaking children. This is unverified marketing. — [TekRevol](https://www.tekrevol.com/blogs/arabic-rtl-app-development-dubai/)

### Inferences
- A hands-on audit of Lamsa, Abjadiyat (from Alef Education), Adam wa Mishmish, and Madrasati/iEN screenshots is recommended. Check their typefaces, harakat use, digit form, mirroring, and how they depict characters.

### Gaps
- No primary sources were retrieved on any of the named apps' type or culture choices. This needs a dedicated follow-up search or a manual app audit.
