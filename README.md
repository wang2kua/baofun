# BaoFun

A near-black, eye-safe touch toy for babies (~10 months). Taps make a warm dim
glow + pitched tones + a haptic blip. Includes a black-screen song mode, a
parent-voice easter egg, a hidden parent menu, anti-mistouch lockdown, and a
hard 5-minute auto-stop.

> Not a substitute for real play. Pediatric guidance (AAP/WHO) is that babies
> under ~18-24 months should avoid screens. This is designed only for the rare
> moments you need both hands — a better option than handing over a video. Keep
> sessions short; the app enforces a 5-minute cap and forces minimum brightness.

## What it does

- **Play mode (default):** tapping anywhere makes a soft pitched tone (the screen
  is an invisible 3×3 grid of notes), a short vibration, and a dim warm
  feathered glow that fades in ~2s. Every ~8-10 taps, a recorded parent voice
  clip plays (if you've recorded any).
- **Song mode:** pure black screen, loops the mp3s you bundle in `assets/songs/`.
- **Eye-safety:** pure black background, forced minimum brightness, warm
  low-saturation glow (no hard bright dot), and a hard 5-minute auto-stop that
  blacks the screen and halts all feedback until a parent restarts it.
- **Anti-mistouch:** portrait-locked, back key blocked, immersive fullscreen so
  the baby can't swipe to the status bar, notifications, or out of the app.

## Build the APK

Requires JDK 17 and the Android SDK (platform android-34, build-tools 34.0.0).

```bash
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk
```

## Install on the phone

1. On the old phone: Settings → enable "Install unknown apps" for your file manager.
2. Copy `app-debug.apk` to the phone and tap it to install.
3. Before handing it to the baby, turn on **Airplane mode / Do Not Disturb**.
   The app **cannot** block real incoming calls — Android does not allow it — so
   this is the only reliable way to prevent interruptions.

## Add baby songs (e.g. 贝乐虎)

Drop `.mp3` files into `app/src/main/assets/songs/`, then rebuild. They play on
loop in Song mode, sorted by filename. (Using songs you already own, for your
own child, is personal use.)

## Parent menu

Long-press the **top-right corner for 3 seconds**. Babies almost never hold one
fixed corner that long; adults do it easily one-handed. From the menu you can:

- Switch to **Play** or **Song** mode
- **Record** a parent voice clip (tap once to start, open the menu and tap again
  to stop) — these are the clips that play as the easter egg in Play mode
- **Restart the timer** after the 5-minute auto-stop
- **Exit** the app

The menu also shows a reminder to enable Airplane mode.

## Run tests

```bash
./gradlew test
```

The pure logic (note-zone mapping, the auto-stop timer, the easter-egg trigger)
is covered by JVM unit tests. UI and touch feedback are verified on-device.

## Project layout

- `app/src/main/java/com/baofun/app/logic/` — pure, unit-tested logic
- `app/src/main/java/com/baofun/app/{audio,system,voice}/` — thin Android wrappers
- `app/src/main/java/com/baofun/app/PlayView.kt` — black canvas + glow rendering
- `app/src/main/java/com/baofun/app/parent/` — hidden parent-menu gesture
- `app/src/main/java/com/baofun/app/MainActivity.kt` — wires it all together

Design and implementation notes live in `docs/superpowers/`.
