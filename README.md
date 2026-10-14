# Sporz — Game Master assistant (Android)

> *« L'assistant du maître du jeu. Distribue les rôles, suis chaque nuit, compte les votes. »*

Sporz is an Android companion app for **[Sporz – Original Outbreak](https://fr.wikipedia.org/wiki/Sporz_Original_Outbreak)**, a hidden-role social deduction game (in the family of *Loup-Garou / Werewolf*) set on a spaceship where a mutant outbreak spreads among the crew.

The app replaces the Game Master's (MJ) paper notes: it deals roles and genomes, guides the night calls, records every action, counts votes and detects the end of the game. The whole game runs on the MJ's phone: players have no phone in hand, the MJ announces each role in private and enters every action and vote.

- UI reference: [`design.html`](design.html) (open it in a browser)

---

## Table of contents

1. [Decisions](#1-decisions)
2. [The game in a nutshell](#2-the-game-in-a-nutshell)
3. [What the app does](#3-what-the-app-does)
4. [Screens (from `design.html`)](#4-screens-from-designhtml)
5. [Architecture](#5-architecture)
6. [Project structure](#6-project-structure)
7. [Tickets / roadmap](#7-tickets--roadmap)
8. [Building](#8-building)

---

## 1. Decisions

The decisions below are the reference for every ticket. If one has to change, update this table first. Numbers are kept stable; gaps are decisions that were dropped with online play.

### Game rules

| # | Topic | Decision |
|---|-------|----------|
| DEC-1 | **Paralysis** | Every night, besides mutating or killing, the mutants paralyse one living, non-mutated player, who does not play for the rest of the night (R-NIGHT-22). Not announced at dawn; the spy can see it. |
| DEC-2 | **Doctors** | The doctors are awake together and decide together. They heal one player per living, non-mutated doctor, all different (nobody is healed twice in a night, no doctor who is awake can be healed), **or** they agree on one kill. If they disagree, the MJ decides. |
| DEC-3 | **Genomes of special roles** | The base mutant is always *host*, doctors are always *normal*. The other genomes are drawn at random among the remaining players, whatever their role. |
| DEC-4 | **Mutated players with a power** | A mutated player **wakes up with the mutants** and **loses their role power** (they are no longer called at their role's step). If the doctors heal them, they leave the mutants and **get their power back**. |
| DEC-5 | **Vote ties & captain** | A tie ejects nobody. With the captain variant, the captain breaks ties. The captain is elected on Day 1 and passes the role on when they die. |
| DEC-6 | **Number of players** | Min 7, max 20. The *Suggérer* button uses a suggestion table per player count (written in ticket A1). |
| DEC-7 | **Traitor & win conditions** | The traitor wins with the mutants, but does not block the healthy camp: the healthy win as soon as no mutated player is alive, even if the traitor is. The mutants win when every living player is mutated or the traitor. |
| DEC-22 | **Vote** | Relative majority with no threshold: the option (a player or blank) with the most votes wins. A tie ejects nobody (DEC-5). |
| DEC-23 | **Traitor** | The traitor is a healthy astronaut (no power, never wakes up at night) who wants the mutants to win; neither side knows who the other is. The mutants can mutate them; a mutated traitor cannot be healed. |
| DEC-24 | **Hacker** | The hacked player still receives their own report; the hacker gets a copy. |

### Product

| # | Topic | Decision |
|---|-------|----------|
| DEC-8 | **Language** | French first. Every string goes in `strings.xml`, so translating later is easy. |
| DEC-12 | **History** | Kept locally on the MJ phone only (Room). Nothing leaves the phone. |
| DEC-13 | **Rematch** | Same players, new composition. |

### Technical

| # | Topic | Decision |
|---|-------|----------|
| DEC-25 | **Themes** | Two themes, following the system setting. **Light** = `design.html` as is (light screens, navy secret areas). **Dark** = everything dark, secret areas (role card, night action) darker still so they keep standing out. |
| DEC-14 | **UI toolkit** | **Jetpack Compose** + Navigation Compose, single activity, from the first line of code. Hilt for dependency injection. |
| DEC-15 | **Rules engine** | The rules live in `:engine`, pure Kotlin with no Android dependency, so they are unit-tested on the JVM. |
| DEC-19 | **Single phone** | The game runs entirely on the MJ phone via `:engine`. No server, no network permission. |
| DEC-21 | **Package name** | `io.github.cyprienj.sporz`, set when the project is created (ticket A2). |

---

## 2. The game in a nutshell

A spaceship crew. Among them, one **base mutant** wants to turn everyone into mutants. The game alternates **nights** and **days**.

**Two camps**

- **Healthy (sains)** — win when every mutant has been killed or healed.
- **Mutants** — win when every healthy player has been mutated or killed.

**Genomes** (drawn secretly, independent from the role)

| Genome | Effect |
|---|---|
| Host (*hôte*) | Once mutated, can never be healed. The base mutant is always a host. |
| Normal | Can be mutated, healed, and mutated again. |
| Resistant (*résistant*) | Cannot be mutated. A mutation attempt fails but still uses the mutants' action. |

**Roles**

| Role | Camp | Night power |
|---|---|---|
| Mutant de base | Mutant | With all mutants: mutate **or** kill one player, **and** paralyse one player for the night (DEC-1). A mutated player loses their own role power and only acts with the mutants (DEC-4). |
| Médecin (×2 by default) | Healthy | Heal a mutated player (unless host) **or** kill a player. |
| Psychologue | Healthy | Learns whether a player is mutated or healthy. |
| Généticien | Healthy | Learns a player's genome. |
| Informaticien | Healthy | Learns the number of mutants on board. |
| Hacker | Healthy | Steals the report of the psychologist, geneticist or IT (and the spy in a variant). |
| Espion | Healthy | Learns what happened to a player during the night (mutated, paralysed, healed, analysed…). |
| Indic | Healthy | Learns whether a player is the traitor or loyal. |
| Enquêteur | Healthy | Learns what was done to a player during the previous night, without the outcome (mutation, paralysis, heal, inspections). |
| Traître | Healthy, wins with the mutants | No power, never wakes up. Can be mutated; once mutated, cannot be healed. Optional. |
| Astronaute | Healthy | No power. Fills the remaining seats. |

**Night order** (default, configurable): Mutants → Médecins → Informaticien → Psychologue → Généticien → Indic → Enquêteur → Hacker → Espion.

The complete, numbered rules used by the app are in **[`docs/rules.md`](docs/rules.md)**.

**Day:** the MJ announces deaths, players debate (no group larger than 4), then a **secret vote** ejects the player with the most votes, with no minimum (blank votes allowed; if blank comes first or there is a tie, nobody is ejected). Optionally the ejected player's role and state are revealed.

**Variants** (composition screen): hacker can hack the spy · reveal role and state on elimination · blank vote allowed · elect a captain (vote counts 1.5).

---

## 3. What the app does

One phone, held by the MJ. Names are typed in and roles are announced privately by the MJ. The MJ **never plays**: they host the game.

### Settings (screen *Paramètres*)

- **Night** — guided call (text for the MJ to read)
- **Flow** — debate timer (0–20 min) · night call order

### MJ features

- Lobby: add, rename and remove players.
- **Composition**: role counters (base mutant locked to 1, astronauts auto-fill), number of host / resistant genomes, variants, *Suggérer* button, total must match the number of players.
- **Distribution**: random draw, then a *Révéler → C'est annoncé* flow for each player; an announced role can be shown again before the first night.
- **Night**: step-by-step wizard (who wakes up, who to target, heal/kill choice, paralysis), the result of each step, then *Terminer la nuit*. A step whose holder is paralysed, mutated or secretly dead is still called for show (R-NIGHT-3).
- **Day**: summary of what to announce, MJ-only details, vote entry, tally, close/cancel vote, debate timer.
- **Player board** (*Équipage*): role, genome, state of every player, filters (alive, mutated, ejected).
- **Journal**: chronological log of every night and day.
- **End of game**: winner, all revelations, *replay with the same crew*, full journal.

### Common

- **Role guide** (roles / genomes / game rules tabs).
- **Recent games** history (local).

---

## 4. Screens (from `design.html`)

| # | Screen | Notes |
|---|---|---|
| 1 | Accueil | *Créer une partie* / resume / rules / recent games |
| 2 | Lobby | step 1/2, add / rename / remove players |
| 3 | Composition des rôles | step 2/2, roles, genomes, variants, suggest, draw |
| 4 | Distribution des rôles | reveal each role in private, start night 1 |
| 5 | Paramètres | guided call, timer, call order |
| 6 | Guide des rôles | roles / genomes / game |
| 7 | Nuit | current step, results, upcoming roles, tabs *Phase / Joueurs / Journal* |
| 8 | Jour et vote | announcements, vote entry, tally, close vote |
| 9 | Tableau des joueurs | filters, role/genome/state |
| 10 | Journal | events per phase |
| 11 | Fin de partie | winner, revelations, replay |

**Visual identity:** dark navy `#12202B`, off‑white `#F4F5F1` / `#EEF0EB`, orange accent `#C2410C` (light) / `#FDBA74` (dark), red mutant `#B42318`, teal healthy `#0F766E`, blue resistant `#1D4ED8`. Fonts: **Barlow** (text) and **Barlow Condensed** (uppercase titles). Phone frame 390 × 844.

---

## 5. Architecture

```
┌──────────────── Android app ────────────────┐
│ ui (Compose)  ── MJ screens                  │
│ viewmodels                                   │
│ data: GameSession (setup + running game,     │
│       saved to a file after every change)    │
│ Room: history of finished games              │
└──────────────────────────────────────────────┘
          :engine — pure Kotlin rules, state machine, win detection
```

- **`:engine`** (pure Kotlin, no Android): game state, night step machine, action resolution, report generation, vote counting, win detection. Fully unit-tested.
- **`:app`**: Android UI. Every screen works on `GameSession`, which runs the game through `:engine`.

---

## 6. Project structure

```
sporz/
├── .github/workflows/ci.yml  # build + unit tests + lint
├── settings.gradle.kts       # includes :app, :engine
├── gradle/libs.versions.toml # single version catalog
├── engine/                   # pure Kotlin: rules, state machine, win detection (JUnit 5)
├── app/                      # Android app (Compose, Room)
│   └── …/ui/theme, ui/components  # design system (A4)
├── docs/
│   ├── rules.md              # final rules (ticket A1)
│   └── licenses/             # third-party licences (Barlow fonts: SIL OFL 1.1)
└── design.html               # UI reference
```

Naming follows the design everywhere (code, strings, JSON): *Mutant de base* = `baseMutant`, *Médecin* = `doctor`, *Astronaute* = `astronaut`, genomes `host / normal / resistant`, states `healthy / mutated / dead`.

---

## 7. Tickets / roadmap

Tickets are grouped in epics, in a suggested order. **Size**: S ≈ ½ day, M ≈ 1–2 days, L ≈ 3–5 days. *Depends* lists blocking tickets.

### Milestone 1 — MVP (single phone)

#### EPIC A — Foundations

| ID | Ticket | Size | Depends |
|---|---|---|---|
| A1 | ✅ Write the final rules in [`docs/rules.md`](docs/rules.md) from the decisions DEC-1 to DEC-7, including the role suggestion table per player count (7 to 20). | S | — |
| A2 | ✅ Create the project from scratch: `git` repository, Gradle multi-module build (`:app`, `:engine`), version catalog, package `io.github.cyprienj.sporz` (DEC-21), min SDK, `.gitignore`, CI (GitHub Actions: build + unit tests + lint). | M | — |
| A3 | ✅ Set up the Android app skeleton: **Jetpack Compose**, Navigation Compose, single activity, dependency injection (Hilt), empty screens for every route of the design. | M | A2 |
| A4 | ✅ **Design system**: colors, typography (Barlow / Barlow Condensed bundled), components (primary/secondary card buttons, stepper −/+, toggle, chips, player row with avatar initial, state badge, bottom tab bar *Phase / Joueurs / Journal*, "secret" surfaces), light and dark themes (DEC-25). | M | A3 |
| A5 | ✅ Set up the `:engine` module (pure Kotlin, kotlinx.serialization, JUnit 5). | S | A2 |

#### EPIC B — Rules engine (`:engine`)

| ID | Ticket | Size | Depends |
|---|---|---|---|
| B1 | ✅ Domain model aligned with the design: `Role`, `Genome`, `Camp`, `PlayerState` (healthy / mutated, plus an `alive` flag: dead players keep their state), `Player`, `GameSettings`, `Composition`, `Variants`. | S | A5 |
| B2 | ✅ Composition validation + **suggestion table** per player count (DEC-6). Astronauts auto-fill. | S | B1 |
| B3 | ✅ Random **deal** of roles and genomes (base mutant = host, constraints from DEC-3), seedable RNG for tests. | S | B1 |
| B4 | ✅ **Night state machine**: ordered steps, skip dead/absent roles and mutated power holders (DEC-4), configurable call order. | M | B1 |
| B5 | ✅ Night **action resolution**: mutation (host/resistant rules, a mutated player loses their power, a healed one gets it back — DEC-4), doctors heal/kill (DEC-2), kill, order of resolution, deaths. | M | B4 |
| B6 | ✅ **Reports**: psychologist, geneticist, IT, hacker (incl. hack-spy variant), spy (what happened to the target). | M | B5 |
| B7 | ✅ **Day & vote**: eligible voters/targets, blank votes, relative majority (DEC-22), ties, captain variant (DEC-5), reveal-on-elimination variant. | M | B1 |
| B8 | ✅ **Win detection** (incl. traitor, DEC-7) after each night and each vote. | S | B5, B7 |
| B9 | ✅ **Game state & journal**: `GameState` drives a whole game with commands (night steps, captain, vote, abort), keeps the reports given to each player, records every action/result/vote as typed journal events (public / MJ-only) and saves/restores as JSON. | M | B5, B7, B8 |
| B10 | ✅ Exhaustive unit tests: full scripted games (e.g. the scenario of the design: Hugo ejected J1, Chloé mutated then healed N2…). | M | B8 |

#### EPIC C — MJ app

| ID | Ticket | Size | Depends |
|---|---|---|---|
| C1 | ✅ Screen **Accueil**. | S | A4 |
| C3 | ✅ Screen **Lobby**: add / rename / remove players. | S | C1 |
| C4 | ✅ Screen **Composition des rôles** (+ genomes, variants, *Suggérer*, total check). | M | B2, C3 |
| C5 | ✅ Screen **Distribution** flow *Révéler → Masquer → C'est annoncé*, progress bar, *Lancer la nuit 1*. | M | B3, C4 |
| C6 | ✅ Screen **Paramètres** (guided call + debate timer + call order). | M | A4 |
| C7 | ✅ Screen **MJ · Nuit**: current step, guided call text, target picker, heal/kill choice, upcoming roles. | L | B4–B6 |
| C8 | ✅ Screen **MJ · Jour et vote**: announcement, MJ-only details, manual vote entry, tally, close/cancel, debate timer. Show "9 vivants" without the "majorité : 5 voix" of the design (no threshold, DEC-22). | M | B7 |
| C9 | ✅ Screen **Tableau des joueurs** with filters. | S | B1 |
| C10 | ✅ Screen **Journal**. | S | B9 |
| C11 | ✅ Screen **Fin de partie** + *Rejouer avec le même équipage*. Note: the sample data of `design.html` ends on day 4 with Dorian killed night 3, which the rules cannot produce (the last mutant is ejected day 2, R-WIN-1); use data from a real `GameState`. | S | B8 |
| C12 | ✅ **Guide des rôles** (3 tabs, expandable cards). | S | A4 |
| C13 | ✅ Persist the running game (process death / phone call must not lose the game) + **Parties récentes** history (Room database). Home offers "Reprendre la partie" (not in design.html); creating or replaying a game while one is in progress asks first, and a played game is kept as interrupted. | M | B9 |
| C14 | ✅ Keep screen awake during a game, back-button confirmation, accessibility pass (TalkBack, font scaling, contrast). | S | C7 |

**Milestone 1 done when:** a full game can be played from start to end with one phone, with the rules of `docs/rules.md`.

### Milestone 2 — Polish & release

| ID | Ticket | Size | Depends |
|---|---|---|---|
| F2 | English translation, once the French version is stable (DEC-8). | S | — |
| F3 | Optional rules variants not in v1. | M | B10 |
| F4 | App icon, splash screen, store listing, privacy policy (no personal data leaves the phone), Play Store release (internal → open testing). | M | A2 |
| F5 | Crash reporting & analytics (opt-in). | S | F4 |

---

## 8. Building

Requirements: Android Studio (or just the Android SDK with platform 36). Gradle runs on **JDK 21**, pinned in `gradle/gradle-daemon-jvm.properties`. If no JDK 21 is installed, Gradle downloads one. The app runs on **Android 8.0 (API 26)** and above.

If you build outside Android Studio, create `local.properties` with `sdk.dir=/path/to/Android/Sdk`.

```bash
./gradlew assembleDebug        # build the app
./gradlew test                 # unit tests
./gradlew lintDebug            # lint (report in app/build/reports/)
./gradlew connectedDebugAndroidTest   # UI tests, needs a running emulator or device
```

CI (`.github/workflows/ci.yml`) runs the first three commands on every push to `master` and on every pull request. The UI tests are not in CI yet: they need an emulator.
