# Sporz — rules implemented by the app

This is the reference for the rules engine (`:engine`) and for the backend. It is based on [Sporz – Original Outbreak](https://fr.wikipedia.org/wiki/Sporz_Original_Outbreak), the decisions DEC-1 to DEC-7 of the [README](../README.md#1-decisions) and the screens of [`design.html`](../design.html).

Every rule has an id (`R-…`) so tickets, code comments and tests can point to it.

---

## 1. Vocabulary

| Term | Meaning |
|---|---|
| **MJ** | Game Master (*maître du jeu*). Runs the game, never plays. |
| **Player** | A crew member. *Living* or *dead* (dead includes ejected). |
| **State** | `healthy` (*sain*) or `mutated` (*muté*). Dead players keep the state they had when they died. |
| **Genome** | `host` (*hôte*), `normal` or `resistant` (*résistant*). Fixed for the whole game. |
| **Role** | The card dealt at setup (*Médecin*, *Psychologue*…). Fixed for the whole game. |
| **Mutant camp** | Living mutated players + the traitor (a healthy player who wins with the mutants, R-SET-12). |
| **Healthy camp** | Every other living player. |
| **Round** | Night *n* followed by Day *n*. The game starts with Night 1. |

---

## 2. Setup

### 2.1 Players

- **R-SET-1** A game has **7 to 20 players**, not counting the MJ (DEC-6).
- **R-SET-2** Player names are unique within a game (case-insensitive, trimmed).

### 2.2 Roles

| Role | Code | Count | Camp at start |
|---|---|---|---|
| Mutant de base | `baseMutant` | exactly 1 | mutant (mutated) |
| Médecin | `doctor` | 1 to 3 (default 2) | healthy |
| Psychologue | `psychologist` | 0 or 1 | healthy |
| Généticien | `geneticist` | 0 or 1 | healthy |
| Informaticien | `computerScientist` | 0 or 1 | healthy |
| Hacker | `hacker` | 0 or 1 | healthy |
| Espion | `spy` | 0 or 1 | healthy |
| Traître | `traitor` | 0 or 1 | healthy, wins with the mutant camp |
| Astronaute | `astronaut` | the rest (computed) | healthy |

- **R-SET-3** The number of astronauts is always *players − all other roles*. A composition is valid only if this number is ≥ 0.
- **R-SET-4** A hacker with nothing to hack is useless: the composition screen shows a warning (not an error) if there is a hacker but no psychologist, geneticist or computer scientist (or spy, with the *hacker can hack the spy* variant).

### 2.3 Genomes

- **R-SET-5** The base mutant is always **host** (DEC-3).
- **R-SET-6** Doctors are always **normal** (DEC-3).
- **R-SET-7** The MJ chooses a number of **extra hosts** and of **resistants**. They are drawn at random among the players who are neither base mutant nor doctor, whatever their role. Everybody else is **normal**.
- **R-SET-8** *extra hosts + resistants* ≤ *players − 1 − doctors*.

### 2.4 Deal

- **R-SET-9** Roles are shuffled and dealt at random, then genomes are drawn (R-SET-5 to 7). The random generator is seedable so tests are reproducible.
- **R-SET-10** At the start, the base mutant is `mutated`, everybody else is `healthy`.
- **R-SET-11** Each player learns secretly their **role, genome and state**. Mutants do not learn who the other mutants are at this point; they discover each other when they wake up together (R-NIGHT-6).
- **R-SET-12** **Traitor** (DEC-23): the traitor is a healthy astronaut — no power, never woken at night — who wins with the mutants (DEC-7). They do not know who the mutants are, and the mutants do not know who the traitor is. The mutants can mutate the traitor, who then wakes up with them; **a mutated traitor cannot be healed** (R-NIGHT-13).

### 2.5 Suggested compositions (*Suggérer* button)

A starting point, to be tuned after playtests. *Hosts* = extra hosts on top of the base mutant.

| Players | Doctors | Psy | Gen | Info | Hacker | Spy | Traitor | Astronauts | Hosts | Resistants |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 7  | 2 | 1 | 0 | 1 | 0 | 0 | 0 | 2  | 0 | 1 |
| 8  | 2 | 1 | 1 | 1 | 0 | 0 | 0 | 2  | 0 | 1 |
| 9  | 2 | 1 | 1 | 1 | 1 | 0 | 0 | 2  | 1 | 1 |
| 10 | 2 | 1 | 1 | 1 | 1 | 1 | 0 | 2  | 1 | 1 |
| 11 | 2 | 1 | 1 | 1 | 1 | 1 | 0 | 3  | 1 | 1 |
| 12 | 2 | 1 | 1 | 1 | 1 | 1 | 0 | 4  | 1 | 1 |
| 13 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 4  | 1 | 2 |
| 14 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 5  | 1 | 2 |
| 15 | 2 | 1 | 1 | 1 | 1 | 1 | 1 | 6  | 2 | 2 |
| 16 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 6  | 2 | 2 |
| 17 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 7  | 2 | 2 |
| 18 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 8  | 2 | 3 |
| 19 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 9  | 2 | 3 |
| 20 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 10 | 2 | 3 |

The base mutant (1) is always included. Default variants: see §6.

---

## 3. Night

### 3.1 General

- **R-NIGHT-1** Everybody closes their eyes. The MJ wakes the steps one by one, in this default order (configurable in *Paramètres*):
  1. Mutants
  2. Médecins
  3. Informaticien
  4. Psychologue
  5. Généticien
  6. Hacker
  7. Espion
- **R-NIGHT-2** **Effects apply immediately**, in step order. A player killed at step 1 does not act at later steps; a player mutated at step 1 loses their power for the rest of the night unless healed at step 2 (R-NIGHT-4).
- **R-NIGHT-3** A step is **skipped** when nobody can play it: the role is not in the game, or its holder is dead or mutated. In *manual* mode the MJ may still pretend to call it, so players cannot deduce anything from silence (the guided call text says so).
- **R-NIGHT-4** **A mutated player loses their role power and wakes up with the mutants. If healed, they get their power back** (DEC-4). A power holder who is mutated *and* healed in the same night acts normally at their step.
- **R-NIGHT-5** Players cannot target themselves, except where stated. Dead players cannot be targeted.

### 3.2 Mutants

- **R-NIGHT-6** All living mutated players wake up together (the base mutant alone on Night 1). They choose **one** action:
  - **mutate** one living healthy player, **or**
  - **kill** one living healthy player,

  **and** they paralyse one player (R-NIGHT-22).
- **R-NIGHT-7** Mutants cannot target a mutated player. They can target the traitor (they may not know who it is).
- **R-NIGHT-8** Mutation outcome by genome:
  - `normal` or `host` → the target becomes `mutated`;
  - `resistant` → nothing happens, but the mutants' action is used up.
- **R-NIGHT-9** If the mutants disagree, the MJ decides.
- **R-NIGHT-22** **Paralysis** (DEC-1): with their action, the mutants choose one living, non-mutated player to paralyse, other than the player they target (if that player is the only one left, nobody is paralysed). The paralysed player is not woken for the rest of the night: their step is played by the other holders of the role (one doctor left heals one player) or skipped if nobody else can play it (R-NIGHT-3); a hacker copying a skipped role gets "nothing". The paralysis is never announced and ends with the night.
- **R-NIGHT-10** The mutated player is told secretly that they are now a mutant: by the MJ (touch on the shoulder) or on their phone if *notify mutated player* is on. They wake up with the mutants from the **next** night.

### 3.3 Doctors

- **R-NIGHT-11** All living, non-mutated doctors wake up together (DEC-2). They choose **either**:
  - **heal**: they decide together whom to heal: one living player per doctor, all different (a player cannot be healed twice in the same night; awake doctors are healthy and see each other, so they cannot heal themselves or one another; a doctor who does not wake up — mutated or paralysed — can be healed by the others. If fewer players can be healed than there are doctors, they heal as many as they can), **or**
  - **kill**: all doctors agree on one living player to kill.
- **R-NIGHT-12** If the doctors disagree, the MJ decides.
- **R-NIGHT-13** Heal outcome:
  - `mutated` and not `host` → becomes `healthy` (and recovers their power, R-NIGHT-4);
  - `mutated` and `host` → nothing happens;
  - `mutated` traitor → nothing happens (R-SET-12);
  - `healthy` → nothing happens.
- **R-NIGHT-14** Kill works on any genome and any state.

### 3.4 Information roles

Each one designates a living player (except the computer scientist) and receives a **report**. Reports are computed at the moment of the step, so they include the effects of the mutants and doctors of the same night.

- **R-NIGHT-15** **Informaticien**: learns the number of living `mutated` players. The traitor is not counted unless mutated.
- **R-NIGHT-16** **Psychologue**: learns whether the target is `mutated` or `healthy`. The traitor shows as `healthy` unless mutated.
- **R-NIGHT-17** **Généticien**: learns the target's genome.
- **R-NIGHT-18** **Hacker**: chooses one role among psychologist, geneticist, computer scientist (and spy with the variant). They receive a **copy** of that role's report of the night. If that role produced no report (absent, dead, mutated), the hacker receives "nothing". The hacked player still receives their own report and is not told they were hacked (DEC-24).
- **R-NIGHT-19** **Espion**: designates a player and learns every action that targeted them this night, with its outcome — mutated / mutation failed / killed by mutants / paralysed / healed / heal failed / killed by doctors / analysed by the psychologist / analysed by the geneticist. With the *hacker can hack the spy* variant, the spy report is computed after the spy's step and the hacker's copy is delivered at the end of the night.

### 3.5 End of night

- **R-NIGHT-20** The MJ checks the victory conditions (§5).
- **R-NIGHT-21** At dawn, the MJ announces **who died**, without saying how. Mutations and heals are never announced. If the *reveal* variant is on, the role and state of each dead player are revealed.

---

## 4. Day

### 4.1 Debate

- **R-DAY-1** Everybody opens their eyes. Players debate freely; groups of more than 4 players are not allowed (table rule, not enforced by the app).
- **R-DAY-2** Dead players no longer speak or vote.
- **R-DAY-3** Optional debate timer (0 to 20 min); when it runs out the app alerts the MJ, who opens the vote.

### 4.2 Captain (variant, DEC-5)

- **R-DAY-4** On Day 1, before the first vote, living players elect a captain by secret vote. Most votes wins; on a tie the MJ draws at random among the tied players.
- **R-DAY-5** The captain's vote counts **1.5**, and the captain breaks ties (R-DAY-10).
- **R-DAY-6** When the captain dies (night or vote), they immediately name a living successor.

### 4.3 Vote

- **R-DAY-7** Every living player votes **secretly** for one living player, or **blank** if the variant is on. A player may vote for themself. They can change their vote until the MJ closes it.
- **R-DAY-8** A living player who has not voted when the vote closes **abstains** (not counted).
- **R-DAY-9** **Relative majority, no threshold** (DEC-22): the option with the most votes wins, however few votes it has.
  - a player → that player is **ejected**;
  - blank → nobody is ejected.
- **R-DAY-10** On a tie for first place, nobody is ejected — unless the captain variant is on, in which case the captain chooses among the tied options (DEC-5).
- **R-DAY-11** The ejected player is dead. With the *reveal* variant (default on) their role and state are revealed.
- **R-DAY-12** The MJ checks the victory conditions (§5), then starts the next night.

---

## 5. Victory

Checked after the end of each night (R-NIGHT-20) and after each vote (R-DAY-12).

- **R-WIN-1** **Healthy camp wins** when no living player is mutated: every mutated player is dead or healed. A living, non-mutated traitor does not prevent it (DEC-7); a mutated traitor counts as a mutant.
- **R-WIN-2** **Mutant camp wins** when every living player is in the mutant camp (mutated players and the traitor).
- **R-WIN-3** If nobody is alive, the game ends with no winner.
- **R-WIN-4** The MJ may end the game at any time (abort, no winner).

---

## 6. Variants

| Variant | Default | Rules |
|---|---|---|
| Hacker can hack the spy | off | R-NIGHT-18, R-NIGHT-19 |
| Reveal role and state on death | on | R-NIGHT-21, R-DAY-11 |
| Blank vote allowed | on | R-DAY-7, R-DAY-9 |
| Captain (vote counts 1.5) | off | R-DAY-4 to R-DAY-6, R-DAY-10 |

---

## 7. Examples (used as engine tests)

**E-1 — mutated then healed (design, Night 2).** Mutants mutate Chloé (normal); doctors heal Chloé → Chloé is healthy again. Nobody died: the MJ announces "Personne n'est mort cette nuit." The spy targeting Chloé learns "mutated by the mutants, healed".

**E-2 — mutated power holder.** Mutants mutate the psychologist (normal) and nobody heals them → step 4 is skipped this night; from Night 3 the psychologist wakes up with the mutants. If healed on a later night, they play step 4 again.

**E-3 — resistant.** Mutants try to mutate Farid (resistant) → nothing happens, no other mutation this night. The spy targeting Farid learns "mutation failed".

**E-4 — host.** Inès (host) is mutated, doctors heal her → she stays mutated.

**E-5 — vote.** 9 living players, votes Gaëlle 4, Chloé 1, blank 1, 3 abstentions → Gaëlle is ejected (relative majority, R-DAY-9).

**E-6 — traitor.** Living: 3 healthy players, 1 mutant, 1 traitor. The 3 healthy ones eject the mutant → the healthy camp wins, although the traitor is still alive (DEC-7).

**E-7 — paralysis.** Night 1: the mutants kill Hugo and paralyse Bastien (doctor). Emma is the only doctor woken and heals one player. Paralysing the psychologist instead skips step 4; the hacker who chose the psychologist gets "nothing". The spy targeting the paralysed player learns "paralysed".

**E-8 — mutated traitor.** The mutants mutate Tom (traitor); the doctors heal Tom → nothing happens, Tom stays mutated and wakes up with the mutants from the next night.
