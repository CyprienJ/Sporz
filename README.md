# Sporz — Game Master assistant (Android)

> *« L'assistant du maître du jeu. Distribue les rôles, suis chaque nuit, compte les votes. »*

Sporz is an Android companion app for **[Sporz – Original Outbreak](https://fr.wikipedia.org/wiki/Sporz_Original_Outbreak)**, a hidden-role social deduction game (in the family of *Loup-Garou / Werewolf*) set on a spaceship where a mutant outbreak spreads among the crew.

The app replaces the Game Master's (MJ) paper notes: it deals roles and genomes, guides the night calls, records every action, counts votes and detects the end of the game. Players can optionally join from their own phone to receive their role, act at night and vote secretly.

- UI reference: [`design.html`](design.html) (17 screens, open it in a browser)
- Server API contract: [`docs/openapi.yaml`](docs/openapi.yaml) (OpenAPI 3.1)

**Scope of this repository:** the Android app (and, in v2, the web player app). The backend that hosts multiplayer games is **not** part of this project: it will be built separately from `docs/openapi.yaml` and run on the owner's own server.

---

## Table of contents

1. [Decisions](#1-decisions)
2. [The game in a nutshell](#2-the-game-in-a-nutshell)
3. [What the app does](#3-what-the-app-does)
4. [Screens (from `design.html`)](#4-screens-from-designhtml)
5. [Architecture](#5-architecture)
6. [Backend API contract](#6-backend-api-contract)
7. [Project structure](#7-project-structure)
8. [Tickets / roadmap](#8-tickets--roadmap)
9. [Building](#9-building)

---

## 1. Decisions

The project starts **from a clean sheet**: no code exists yet, only this README, [`design.html`](design.html) and [`docs/openapi.yaml`](docs/openapi.yaml). The decisions below are the reference for every ticket. If one has to change, update this table first.

### Still open

| # | Question | When |
|---|----------|------|
| OPEN-1 | **Web player app stack (v2).** Compose Multiplatform for Web (reuses Kotlin code and the design system) or a classic web stack (TypeScript + React/Svelte, lighter and more mature)? | Later, before starting v2 (ticket F6). |

### Game rules

| # | Topic | Decision |
|---|-------|----------|
| DEC-1 | **Paralysis** | Not in v1. The rules engine keeps a variant flag so it can be added later (ticket F3). |
| DEC-2 | **Doctors** | Each living, non-mutated doctor heals one target, **or** all of them agree on one kill. If they disagree, the MJ decides. |
| DEC-3 | **Genomes of special roles** | The base mutant is always *host*, doctors are always *normal*. The other genomes are drawn at random among the remaining players, whatever their role. |
| DEC-4 | **Mutated players with a power** | A mutated player **wakes up with the mutants** and **loses their role power** (they are no longer called at their role's step). If the doctors heal them, they leave the mutants and **get their power back**. |
| DEC-5 | **Vote ties & captain** | A tie ejects nobody. With the captain variant, the captain breaks ties. The captain is elected on Day 1 and passes the role on when they die. |
| DEC-6 | **Number of players** | Min 7, max 20. The *Suggérer* button uses a suggestion table per player count (written in ticket A1). |
| DEC-7 | **Traitor & win conditions** | The traitor counts in the mutant camp for win detection. |
| DEC-22 | **Vote** | Relative majority with no threshold: the option (a player or blank) with the most votes wins. A tie ejects nobody (DEC-5). |
| DEC-23 | **Traitor** | The traitor never wakes up with the mutants; neither side knows who the other is. |
| DEC-24 | **Hacker** | The hacked player still receives their own report; the hacker gets a copy. |

### Product

| # | Topic | Decision |
|---|-------|----------|
| DEC-8 | **Language** | French first. Every string goes in `strings.xml`, so translating later is easy. |
| DEC-9 | **Accounts** | None. A nickname + a per-game token. |
| DEC-10 | **Player client** | Native Android app in v1. A lightweight web client (QR code → browser) is planned for v2. |
| DEC-11 | **iOS** | iPhone players are supported through the **v2 web player app** (ticket F6), not a native iOS app. Until then they join as offline players. |
| DEC-12 | **History** | Kept locally on the MJ phone only (Room). The backend forgets games 48 h after they end. |
| DEC-13 | **Rematch** | Same lobby, same code, same players, new composition. |

### Technical

| # | Topic | Decision |
|---|-------|----------|
| DEC-25 | **Themes** | Two themes, following the system setting. **Light** = `design.html` as is (light screens, navy secret areas). **Dark** = everything dark, secret areas (role card, night action) darker still so they keep standing out. |
| DEC-14 | **UI toolkit** | **Jetpack Compose** + Navigation Compose, single activity, from the first line of code. Hilt for dependency injection, Ktor client for networking. |
| DEC-15 | **Backend** | **Out of scope for this repository.** The owner builds it later from `docs/openapi.yaml`. The rules engine `:engine` is pure Kotlin with no Android dependency, so the backend can reuse it if it runs on the JVM. |
| DEC-16 | **Real-time transport** | REST for commands (see OpenAPI) + **one WebSocket per client** for push events. SSE as a fallback. No push notifications (FCM) in v1: the app is in the foreground during a game. |
| DEC-17 | **Backend persistence** | Recommendation for the backend: games kept in memory with **Redis** snapshots, so a restart does not kill running games. |
| DEC-18 | **Hosting** | The owner's own server. The app reads the backend URL from the build config (one URL per build type). |
| DEC-19 | **Authority** | In online and semi-online games the **backend** is authoritative and the MJ phone is a privileged client. In offline games the same rules run locally on the MJ phone via `:engine`. |
| DEC-20 | **MJ loses the network** | The backend pauses the game; the MJ reconnects with their token and resumes. |
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
| Mutant de base | Mutant | With all mutants: mutate **or** kill one player. A mutated player loses their own role power and only acts with the mutants (DEC-4). |
| Médecin (×2 by default) | Healthy | Heal a mutated player (unless host) **or** kill a player. |
| Psychologue | Healthy | Learns whether a player is mutated or healthy. |
| Généticien | Healthy | Learns a player's genome. |
| Informaticien | Healthy | Learns the number of mutants on board. |
| Hacker | Healthy | Steals the report of the psychologist, geneticist or IT (and the spy in a variant). |
| Espion | Healthy | Learns what happened to a player during the night (mutated, healed, analysed…). |
| Traître | Mutant (but healthy) | No power; plays for the mutants. Optional. |
| Astronaute | Healthy | No power. Fills the remaining seats. |

**Night order** (default, configurable): Mutants → Médecins → Informaticien → Psychologue → Généticien → Hacker → Espion.

The complete, numbered rules used by the app are in **[`docs/rules.md`](docs/rules.md)**.

**Day:** the MJ announces deaths, players debate (no group larger than 4), then a **secret vote** ejects the player with the most votes, with no minimum (blank votes allowed; if blank comes first or there is a tie, nobody is ejected). Optionally the ejected player's role and state are revealed.

**Variants** (composition screen): hacker can hack the spy · reveal role and state on elimination · blank vote allowed · elect a captain (double vote).

---

## 3. What the app does

### Three ways to play (chosen by the MJ — screen *Mode de partie*)

| Mode | Who has a phone | Server needed |
|---|---|---|
| **Hors ligne** (offline) | Only the MJ. Names are typed in, roles are announced privately by the MJ. | No |
| **Semi-online** | Some players join with their phone, the others are added by hand by the MJ. | Yes |
| **En ligne** (online) | Every player joins the lobby and gets their role on their screen. | Yes |

The MJ **never plays**: they host the game.

### Three levels of automation (screen *Paramètres*)

| Preset | Behaviour |
|---|---|
| **Manuel** | The phone is the MJ's notebook. Players only receive their role; everything else is said out loud. |
| **Assisté** (default) | Players act on their phone, but the MJ validates each result before it is sent. |
| **Automatique** | The app chains everything: actions, reports and votes. The MJ animates and arbitrates. |

Each preset is a set of toggles that can be customised individually:

- **Night** — night actions on phone · guided call (text for the MJ to read) · discreet vibration when it's the player's turn
- **Information** — automatic sending of reports · notify the mutated player · discoveries notebook · reminder of the action to perform
- **Votes** — vote on phone · auto-close when all living players voted · live tally visible to players
- **Privacy** — auto-hide the role screen after 10 s
- **Flow** — debate timer (0–20 min) · night call order

### MJ features

- Create a game, get a **4-character code** + **QR code** + share link.
- Lobby: see who joined, add offline players, remove players.
- **Composition**: role counters (base mutant locked to 1, astronauts auto-fill), number of host / resistant genomes, variants, *Suggérer* button, total must match the number of players.
- **Distribution**: random draw; for offline players, a *Révéler → C'est annoncé* flow; for connected players, a *Envoyé / Vu* status.
- **Night**: step-by-step wizard (who wakes up, who to target, heal/kill choice), live display of choices made on phones, manual input for offline players.
- **Day**: summary of what to announce, MJ-only details, live vote tally, manual input of offline votes, close/cancel vote.
- **Player board** (*Équipage*): role, genome, state of every player, filters (alive, mutated, offline, ejected).
- **Journal**: chronological log of every night and day.
- **End of game**: winner, all revelations, *replay with the same crew*, full journal.

### Player features (when connected)

- Join with code or QR + nickname.
- Waiting room with crew list and access to the role guide.
- **My role**: role, camp, genome, state, description, received reports (notebook), hide button.
- **Night action**: target picker when it's their turn, countdown, confirmation, result.
- **Vote**: secret, changeable until the close, blank vote.

### Common

- **Role guide** (roles / genomes / game rules tabs).
- **Recent games** history (local).

---

## 4. Screens (from `design.html`)

| # | Screen | Side | Notes |
|---|---|---|---|
| 1 | Accueil | both | *Créer une partie* / *Rejoindre une partie* / rules / recent games |
| 2 | Mode de partie | MJ | step 1/3, offline / semi-online / online |
| 3 | Lobby | MJ | step 2/3, code, QR, add offline player, crew list |
| 4 | Composition des rôles | MJ | step 3/3, roles, genomes, variants, suggest, draw |
| 5 | Distribution des rôles | MJ | reveal offline roles, track "seen" on phones, start night 1 |
| 6 | Paramètres et automatisation | MJ | presets + toggles + timer + call order |
| 7 | Rejoindre | Player | code, nickname, QR scan |
| 8 | Salle d'attente | Player | crew list, open guide, leave |
| 9 | Mon rôle | Player | role card, reports, tabs *Rôle / Joueurs / Journal* |
| 10 | Vote | Player | secret vote with blank |
| 11 | Action de nuit | Player | target picker, timer, result |
| 12 | Guide des rôles | both | roles / genomes / game |
| 13 | Nuit | MJ | mutants & doctors step, upcoming roles, tabs *Phase / Joueurs / Journal* |
| 14 | Jour et vote | MJ | announcements, tally, offline votes, close vote |
| 15 | Tableau des joueurs | MJ | filters, role/genome/state |
| 16 | Journal | MJ | events per phase |
| 17 | Fin de partie | both | winner, revelations, replay |

**Visual identity:** dark navy `#12202B`, off‑white `#F4F5F1` / `#EEF0EB`, orange accent `#C2410C` (light) / `#FDBA74` (dark), red mutant `#B42318`, teal healthy `#0F766E`, blue resistant `#1D4ED8`. Fonts: **Barlow** (text) and **Barlow Condensed** (uppercase titles). Phone frame 390 × 844.

---

## 5. Architecture

```
┌──────────────── Android app (this repo) ────────────────┐          ┌──── Backend (separate project) ────┐
│ ui (Compose)  ── MJ screens / player screens            │  HTTPS   │ implements docs/openapi.yaml       │
│ viewmodels                                              │◄────────►│ REST + WebSocket /games/{id}/events│
│ data: GameRepository                                    │   WS     │ authoritative game state           │
│   ├─ LocalGameSource   (offline mode, runs :engine)     │          │ hosted on the owner's server       │
│   ├─ RemoteGameSource  (Ktor client + WebSocket)        │          └────────────────────────────────────┘
│   └─ FakeGameServer    (tests / debug, runs :engine)    │
│ Room: history of finished games                         │
└─────────────────────────────────────────────────────────┘
          :engine — pure Kotlin rules, state machine, win detection
```

- **`:engine`** (pure Kotlin, no Android): game state, night step machine, action resolution, report generation, vote counting, win detection. Fully unit-tested. Runs the offline mode and the fake backend; can be reused by the real backend if it runs on the JVM.
- **`:app`**: Android UI + repositories. Every screen talks to `GameRepository`, so it works the same offline, against the fake backend, or against the real one.
- **Backend**: not in this repository. `docs/openapi.yaml` is the contract between the two.

**Secrets never leave the backend:** a player's client only ever receives *their own* role card and reports. The full board is only sent to the MJ token. The app must never rely on data it is not supposed to see.

---

## 6. Backend API contract

The full contract is in **[`docs/openapi.yaml`](docs/openapi.yaml)** (view it with [Swagger Editor](https://editor.swagger.io) or any OpenAPI viewer). It is maintained in this repository and is the only specification the backend needs.

### Authentication

No accounts. Each game issues opaque bearer tokens:

- `POST /games` returns a **MJ token**.
- `POST /games/{code}/join` returns a **player token**.

The token is sent as `Authorization: Bearer <token>` and is enough to reconnect after a network loss. Tokens die with the game.

### Endpoints overview

| Area | Endpoints |
|---|---|
| Game | `POST /games` · `GET /games/{code}/preview` · `GET /games/{gameId}` · `DELETE /games/{gameId}` |
| Lobby | `POST /games/{code}/join` · `POST /games/{gameId}/players/offline` · `DELETE /games/{gameId}/players/{playerId}` |
| Setup | `PUT /games/{gameId}/settings` · `PUT /games/{gameId}/composition` · `GET /games/{gameId}/composition/suggestion` · `POST /games/{gameId}/deal` · `POST /games/{gameId}/players/{playerId}/announced` |
| Player | `GET /games/{gameId}/me` · `POST /games/{gameId}/me/role-seen` |
| Night | `POST /games/{gameId}/night/start` · `GET /games/{gameId}/night` · `POST /games/{gameId}/night/actions` · `POST /games/{gameId}/night/steps/{stepId}/validate` · `POST /games/{gameId}/night/reports/{reportId}/send` |
| Day | `POST /games/{gameId}/day/vote/open` · `GET /games/{gameId}/day/vote` · `PUT /games/{gameId}/day/votes/me` · `PUT /games/{gameId}/day/votes/{playerId}` · `POST /games/{gameId}/day/vote/close` · `POST /games/{gameId}/day/vote/cancel` |
| MJ views | `GET /games/{gameId}/players` · `GET /games/{gameId}/journal` |
| End | `GET /games/{gameId}/result` · `POST /games/{gameId}/rematch` |
| Reference | `GET /roles` |
| Real time | `GET /games/{gameId}/events` (WebSocket upgrade) |

### Real-time events (WebSocket)

After connecting to `/games/{gameId}/events?token=…`, the server pushes JSON messages `{ "type": "...", "payload": {...} }`. Each client only receives what its token is allowed to see.

| Event | Sent to | Payload |
|---|---|---|
| `lobby.updated` | all | crew list |
| `game.settingsUpdated` | all | public settings |
| `role.assigned` | one player | the player's `RoleCard` |
| `role.seen` | MJ | `playerId` |
| `phase.changed` | all | `{ phase, number }` |
| `night.yourTurn` | one player | `NightStep` + deadline |
| `night.actionSubmitted` | MJ | `NightAction` |
| `night.report` | one player | `Report` |
| `night.mutated` | one player | when *notify mutated player* is on |
| `day.announcement` | all | deaths of the night |
| `vote.opened` / `vote.closed` | all | eligible targets / result |
| `vote.tally` | MJ (+ all if *live tally*) | counts only |
| `player.eliminated` | all | player + revealed role if variant on |
| `game.ended` | all | `GameResult` |
| `connection.changed` | MJ | a player connected / disconnected |

Clients must treat events as hints and re-fetch the state (`GET /games/{gameId}` or `/me`) after reconnection.

---

## 7. Project structure

Nothing exists yet; the project is created in EPIC A with this layout:

```
sporz/
├── .github/workflows/ci.yml  # build + unit tests + lint
├── settings.gradle.kts       # includes :app, :engine
├── gradle/libs.versions.toml # single version catalog
├── engine/                   # pure Kotlin: rules, state machine, win detection (JUnit 5)
├── app/                      # Android app (Compose, Room, Ktor client)
│   └── …/ui/theme, ui/components  # design system (A4); debug builds show it in « Catalogue du design »
├── web/                      # v2: web player app (stack: OPEN-1)
├── docs/
│   ├── openapi.yaml          # API contract
│   ├── rules.md              # final rules (ticket A1)
│   └── licenses/             # third-party licences (Barlow fonts: SIL OFL 1.1)
└── design.html               # UI reference
```

Naming follows the design and the API everywhere (code, strings, JSON): *Mutant de base* = `baseMutant`, *Médecin* = `doctor`, *Astronaute* = `astronaut`, genomes `host / normal / resistant`, states `healthy / mutated / dead`.

---

## 8. Tickets / roadmap

Tickets are grouped in epics, in a suggested order. **Size**: S ≈ ½ day, M ≈ 1–2 days, L ≈ 3–5 days. *Depends* lists blocking tickets.

### Milestone 1 — Offline MVP (single phone, no server)

#### EPIC A — Foundations

| ID | Ticket | Size | Depends |
|---|---|---|---|
| A1 | ✅ Write the final rules in [`docs/rules.md`](docs/rules.md) from the decisions DEC-1 to DEC-7, including the role suggestion table per player count (7 to 20). | S | — |
| A2 | ✅ Create the project from scratch: `git` repository, Gradle multi-module build (`:app`, `:engine`), version catalog, package `io.github.cyprienj.sporz` (DEC-21), backend URL per build type (DEC-18), min SDK, `.gitignore`, CI (GitHub Actions: build + unit tests + lint). | M | — |
| A3 | ✅ Set up the Android app skeleton: **Jetpack Compose**, Navigation Compose, single activity, dependency injection (Hilt), empty screens for every route of the design. | M | A2 |
| A4 | ✅ **Design system**: colors, typography (Barlow / Barlow Condensed bundled), components (primary/secondary card buttons, stepper −/+, toggle, chips, player row with avatar initial, state badge, bottom tab bar *Phase / Joueurs / Journal*, "secret" surfaces), light and dark themes (DEC-25). | M | A3 |
| A5 | Set up the `:engine` module (pure Kotlin, kotlinx.serialization, JUnit 5). | S | A2 |

#### EPIC B — Rules engine (`:engine`)

| ID | Ticket | Size | Depends |
|---|---|---|---|
| B1 | Domain model aligned with the design: `Role`, `Genome`, `Camp`, `PlayerState` (healthy / mutated / dead), `Player`, `GameSettings`, `Composition`, `Variants`. | S | A5 |
| B2 | Composition validation + **suggestion table** per player count (DEC-6). Astronauts auto-fill. | S | B1 |
| B3 | Random **deal** of roles and genomes (base mutant = host, constraints from DEC-3), seedable RNG for tests. | S | B1 |
| B4 | **Night state machine**: ordered steps, skip dead/absent roles and mutated power holders (DEC-4), configurable call order. | M | B1 |
| B5 | Night **action resolution**: mutation (host/resistant rules, a mutated player loses their power, a healed one gets it back — DEC-4), doctors heal/kill (DEC-2), kill, order of resolution, deaths. | M | B4 |
| B6 | **Reports**: psychologist, geneticist, IT, hacker (incl. hack-spy variant), spy (what happened to the target). | M | B5 |
| B7 | **Day & vote**: eligible voters/targets, blank votes, relative majority (DEC-22), ties, captain variant (DEC-5), reveal-on-elimination variant. | M | B1 |
| B8 | **Win detection** (incl. traitor, DEC-7) after each night and each vote. | S | B5, B7 |
| B9 | **Journal**: every action/result/vote recorded as typed events; serialisable (JSON) game state for save/restore. | S | B5, B7 |
| B10 | Exhaustive unit tests: full scripted games (e.g. the scenario of the design: Hugo ejected J1, Chloé mutated then healed N2…). | M | B8 |

#### EPIC C — Offline MJ app

| ID | Ticket | Size | Depends |
|---|---|---|---|
| C1 | Screen **Accueil**. | S | A4 |
| C2 | Screen **Mode de partie** (only *Hors ligne* enabled until Milestone 2). | S | A4 |
| C3 | Screen **Lobby (offline)**: add / rename / remove players. | S | C2 |
| C4 | Screen **Composition des rôles** (+ genomes, variants, *Suggérer*, total check). | M | B2, C3 |
| C5 | Screen **Distribution** offline flow *Révéler → Masquer → C'est annoncé*, progress bar, *Lancer la nuit 1*. | M | B3, C4 |
| C6 | Screen **Paramètres** (presets + toggles + debate timer + call order); only *Manuel* meaningful offline. | M | A4 |
| C7 | Screen **MJ · Nuit**: current step, guided call text, target picker, heal/kill choice, upcoming roles. | L | B4–B6 |
| C8 | Screen **MJ · Jour et vote**: announcement, MJ-only details, manual vote entry, tally, close/cancel, debate timer. Show "9 vivants" without the "majorité : 5 voix" of the design (no threshold, DEC-22). | M | B7 |
| C9 | Screen **Tableau des joueurs** with filters. | S | B1 |
| C10 | Screen **Journal**. | S | B9 |
| C11 | Screen **Fin de partie** + *Rejouer avec le même équipage*. | S | B8 |
| C12 | **Guide des rôles** (3 tabs, expandable cards). | S | A4 |
| C13 | Persist the running game (process death / phone call must not lose the game) + **Parties récentes** history (Room database). | M | B9 |
| C14 | Keep screen awake during a game, back-button confirmation, accessibility pass (TalkBack, font scaling, contrast). | S | C7 |

**Milestone 1 done when:** a full game can be played from start to end with one phone, with the rules of `docs/rules.md`.

### Milestone 2 — Online play (Android client)

The backend is built outside this project. These tickets let the app be developed and tested before it exists, then plugged into it.

#### EPIC D — API contract & mock backend

| ID | Ticket | Size | Depends |
|---|---|---|---|
| D1 | Finalise `docs/openapi.yaml`: review with the backend owner, add one schema per WebSocket event payload, error codes, examples. | M | A1 |
| D2 | **REST mock** for manual testing: [Prism](https://stoplight.io/open-source/prism) serving `docs/openapi.yaml`, plus a debug setting in the app to point at any backend URL. | S | D1 |
| D3 | **`FakeGameServer`**: in-process implementation of the API and events built on `:engine`, used in unit/UI tests and debug builds to simulate a full online game (one MJ + several players) on one device. | L | B10, D1 |
| D4 | **Contract tests**: check that every request the app sends and every response it parses matches `docs/openapi.yaml`. | S | E1 |
| D5 | **End-to-end test against the real backend** once available: a 10-player semi-online game on real phones, in each automation preset. | M | E10, *backend* |

#### EPIC E — Android online client

| ID | Ticket | Size | Depends |
|---|---|---|---|
| E1 | `RemoteGameSource`: Ktor client generated from the OpenAPI, WebSocket client, token storage, reconnection & state re-sync. | M | D1, D3 |
| E2 | `GameRepository` abstraction so every MJ screen works on local *or* remote state. | M | E1, C13 |
| E3 | MJ **Lobby online**: code, QR code generation, share link (Android sharesheet), live crew list. | M | E2 |
| E4 | Player **Rejoindre**: code input, nickname, **QR scanner** (CameraX + ML Kit), deep link `sporz://join/{code}` / App Links. | M | E1 |
| E5 | Player **Salle d'attente**. | S | E4 |
| E6 | Player **Mon rôle** (role card, auto-hide after 10 s, reports notebook) and *Envoyé / Vu* on MJ distribution screen. | M | E5 |
| E7 | Player **Action de nuit** (your-turn vibration, countdown, confirm, result). | M | E6, D3 |
| E8 | Player **Vote** (secret, changeable until close, blank). | S | E6, D3 |
| E9 | MJ night/day screens: show choices made on phones, validate & send reports (*Assisté*), auto mode (*Automatique*). | L | E2, D3 |
| E10 | **Semi-online**: mixed lobby, per-player input mode in night & vote screens ("Bastien hors ligne : saisie par le MJ"). | M | E9 |
| E11 | Player tabs *Joueurs* and *Journal* (public information only) + end-of-game screen for players. | S | E6 |
| E12 | Error & network states: lost connection banner, MJ disconnected → game paused, kicked, game deleted. | M | E1 |

**Milestone 2 done when:** a 10-player semi-online game (7 phones + 3 offline) can be played in each automation preset against `FakeGameServer`, then against the real backend (D5).

### Milestone 3 — Polish & release

| ID | Ticket | Size | Depends |
|---|---|---|---|
| F1 | Rematch with the same crew (`POST /rematch`) online. | S | E10 |
| F2 | English translation, once the French version is stable (DEC-8). | S | — |
| F3 | Optional rules variants not in v1 (paralysis DEC-1, etc.). | M | B10 |
| F4 | App icon, splash screen, store listing, privacy policy (nicknames only, no personal data), Play Store release (internal → open testing). | M | A2 |
| F5 | Crash reporting & analytics (opt-in). | S | F4 |
| F6 | **v2** — Web player app in `web/` (join, waiting room, my role, night action, vote), also used by iPhone players (DEC-10, DEC-11). Stack decided by OPEN-1. Served next to the backend so the QR code can open it directly. | L | D1, *backend* |

---

## 9. Building

Requirements: Android Studio (or just the Android SDK with platform 36). Gradle runs on **JDK 21**, pinned in `gradle/gradle-daemon-jvm.properties`. If no JDK 21 is installed, Gradle downloads one. The app runs on **Android 8.0 (API 26)** and above.

If you build outside Android Studio, create `local.properties` with `sdk.dir=/path/to/Android/Sdk`.

```bash
./gradlew assembleDebug        # build the app
./gradlew test                 # unit tests
./gradlew lintDebug            # lint (report in app/build/reports/)
./gradlew connectedDebugAndroidTest   # UI tests, needs a running emulator or device
```

CI (`.github/workflows/ci.yml`) runs the first three commands on every push to `master` and on every pull request. The UI tests are not in CI yet: they need an emulator.

**Backend URL** (DEC-18): the app reads `BuildConfig.BACKEND_URL`, set from `gradle.properties`:

| Build type | Property | Default |
|---|---|---|
| debug (`io.github.cyprienj.sporz.debug`) | `sporz.backendUrl.debug` | `http://10.0.2.2:4010`, the Prism mock on the host machine as seen from the emulator |
| release | `sporz.backendUrl.release` | placeholder `https://api.sporz.example/v1` |

Put your real server address in `~/.gradle/gradle.properties` (or pass `-Psporz.backendUrl.release=…`) so it does not end up in the repository.

Mock backend for manual testing (ticket D2):

```bash
npx @stoplight/prism-cli mock docs/openapi.yaml   # http://127.0.0.1:4010
```
