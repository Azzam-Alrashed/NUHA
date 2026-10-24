# NUHA onboarding

Three native Compose screens establish the first version of the visual identity:

1. **Welcome:** original flower guide, a winding path, warm paper, and Arabic introduction.
2. **Learn through play:** matching, letters, and emotions, each with an original illustration and a consistent activity colour.
3. **Comfort:** colourful, muted, and high-contrast appearance; separate voice, effects, music, and reduced-motion preferences.

These screens were built to the [legacy calm direction](research/legacy/ui-style-calm.md). NUHA's style is now [Playful Soft-Flat](research/ui-style.md). The structure carries over (flower guide, winding path, comfort settings), but the palette, button shapes and type weights will be restyled in a later change.

## Implementation boundaries

- `ui/identity/Identity.kt` owns palette tokens, vector illustrations, and the reduced-motion composition local. Artwork is drawn with Compose Canvas, not screenshots or emoji. Illustrations use content coordinates independent of RTL layout; the surrounding interface is RTL.
- Readex Pro is bundled for offline Arabic UI rendering. Its SIL Open Font License is included in `app/src/main/assets/licenses/ReadexPro-OFL.txt`. Arabic early-learning content still needs educator review before activities are built.
- Reduced motion defaults on. With it disabled, illustration entrances have a brief 240ms opacity transition; there is no idle animation. Toggle changes are static in both modes. Compose's animation clock also respects the system animation scale.
- `data/AppPreferences.kt` uses the existing Room storage technology in a separate `nuha-app.db`. The experiment's disposable `nuha-lab.db` is unchanged. A serial writer persists snapshots in interaction order; completion is shown only after the final write succeeds.
- Completion enters the existing technical lab. The lab includes an introduction/settings entry so preferences can be revisited without clearing the persisted completion flag. No home, account, caregiver dashboard, or learning game has been invented.
- Audio channels are independent stored preferences. This introduction has no narration, effects recordings, or music assets; it remains usable silently. These preferences are available to later audio-enabled activities.
- Settings and introductory copy can scroll at larger text sizes; the navigation footer stays visible. Active learning tasks have not been introduced here.

## Emulator checks

Build from `android/` with JDK 21:

```sh
./gradlew :app:assembleDebug :app:lintDebug
```

`tools/android/verify_onboarding.py` exercises visible Arabic controls, verifies the first-run/revisit flow and persistence across force-stop/relaunch, and captures all three screens at normal and 1.5× system font scale. It does not clear app data. It accepts any of the three onboarding screens or the lab as its initial state. It restores the original font scale in a `finally` block.

Screenshots are in `output/onboarding/`. Test target: existing Pixel Tablet emulator, API 36, 2560×1600 at density 320 (1280×800dp). Physical-device reach, actual stylus input, TalkBack speech, Arabic voice assets, and child/caregiver usability evaluation remain to be tested.
