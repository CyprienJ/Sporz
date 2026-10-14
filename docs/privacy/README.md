# Sporz — privacy policy: content brief

Everything needed to write the public privacy policy page of the Sporz Android
app ("Politique de confidentialité"), required by Google Play. The page itself is
built in another project; this file is the source of truth for **what it must
say**.

- §1 lists the requirements the page must meet.
- §2 lists the facts about the app, checked against the code (version 0.1.0, offline).
- §3 gives the answers to Play Console's *Data safety* form, which must match the page.
- §4 is a ready-to-adapt draft in French (the app's language, DEC-8).
- §5 is what to add when online play ships (Milestone 2).
- §6 lists the open points to settle before publishing.

Placeholders are written `[LIKE THIS]`.

---

## 1. Requirements for the page

### Google Play (User Data policy)
- A **public, active URL**: not a PDF, not behind a login, not geo-blocked, readable on a phone.
- The **app name** ("Sporz") and the **developer name exactly as shown on the Play listing**.
- A **privacy contact**: an email address that is read.
- What data is **accessed, collected, used and shared**, and with whom.
- **Security**: how the data is protected.
- **Retention and deletion**: how long data is kept and how a user deletes it.
- Clearly labelled as a privacy policy (title "Politique de confidentialité").
- Consistent with the **Data safety** form (§3). A mismatch is a common reason for rejection.
- The same URL goes in Play Console (*App content → Privacy policy*). Linking to it from inside the app is recommended (for example in the Home screen's links or the role guide).

### GDPR (the developer is in France, users mostly in the EU)
Information required by article 13 GDPR, even when almost nothing is collected:
1. **Who is responsible** (the "data controller"): name, contact. For a personal project: your name or developer name, and an email.
2. **What data** is processed.
3. **Why** (purposes), and the **legal basis** of each purpose.
4. **Recipients**: who receives the data.
5. **Transfers outside the EU**, if any.
6. **How long** data is kept.
7. **Rights**: access, rectification, erasure, restriction, objection, portability, and the right to **complain to the CNIL** (www.cnil.fr).
8. Whether providing the data is required, and what happens if not.
9. **No automated decision-making / profiling** (state it).
10. **Date** of the policy, and how changes are announced.

### Good practice
- **Children:** say who the app is for. Sporz is a party game; in Play Console, choose a target audience of 13+ (or 18+) to stay outside the "Designed for Families" rules, and say so on the page.
- Plain language, short sections, no legal filler that isn't true for this app.

---

## 2. Facts about the app (version 0.1.0, offline mode)

Checked in the code on 2026-10-14. Re-check them before each release that adds a feature (see §5).

| Topic | Fact | Where in the code |
|---|---|---|
| Accounts | **None.** No sign-in, no email, no phone number (DEC-9). | — |
| Data typed by the user | The game master types **players' first names or nicknames** for the game. These may be real first names of other people (the players). | Lobby screen |
| Game data | Roles, genomes, night actions, votes and the journal of each game. Fictional game data, but tied to the names above. | `GameState` |
| Where it is stored | **Only on the phone**, in the app's private storage: the current game in a file (`current_game.json`), finished games in a local database (`sporz.db`, "Parties récentes"). No other app can read them. | `SessionStore.kt`, `data/history/` |
| How long | The current game: until a new game replaces it. Finished games: until the user deletes them ("Supprimer la partie") or uninstalls the app / clears its data. No automatic expiry. | — |
| Backups | **Excluded** from Android cloud backup and from device-to-device transfer (`allowBackup="false"`, backup rules exclude everything). Data never reaches the user's Google Drive through Sporz. | `AndroidManifest.xml`, `res/xml/` |
| Network | **No network traffic** in offline mode: no server, no network library is included. *Note:* the app still declares the `INTERNET` permission (prepared for online play); see §6. | `AndroidManifest.xml` |
| Sharing | **Nothing is sent automatically.** The only way data leaves the app is when the user taps **"Exporter le journal"**: the journal text (player names, roles, events) is handed to the app the user picks in Android's share menu (email, messaging…). Sporz does not receive or keep a copy; what happens next depends on that app. | `JournalScreen.kt` |
| Analytics, crash reporting | **None** (no Firebase, no Crashlytics, no analytics SDK). Planned later as **opt-in** only (ticket F5); update the page when it ships. | `app/build.gradle.kts` |
| Advertising | **None.** No ads, no advertising ID. | — |
| Third-party SDKs | Only Android Jetpack libraries (Compose, Room, Hilt, Navigation) and Kotlin libraries. None of them sends data. | `gradle/libs.versions.toml` |
| Fonts | Barlow (OFL) is **bundled** in the app, not downloaded from Google Fonts. | `res/font/`, `docs/licenses/` |
| Permissions | `INTERNET` only. No camera, location, contacts, microphone or storage permissions. | `AndroidManifest.xml` |
| Screen kept on | During a game the screen stays on. A display setting only; no data involved. | `RunningGame.kt` |
| Location of data | On the user's phone. No transfer anywhere, so no transfer outside the EU. | — |
| Profiling, automated decisions | None. | — |

**In one sentence:** in its offline version, Sporz collects no personal data. The names typed for a game stay on the phone, and only the user can share them, through the journal export.

GDPR note: the developer never receives these names, so for the offline version the developer has no data to give access to or delete. The page should still say so, and explain how users delete data themselves (in-app delete, clear storage, uninstall).

---

## 3. Play Console *Data safety* form (offline version)

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No.** Data that stays on the device and is never sent off it doesn't count as "collected" in Google's definition. User-initiated sharing through the Android share sheet isn't "sharing" by the app either. |
| Is all user data encrypted in transit? | Not applicable (nothing is transmitted). If the form requires an answer once a network feature exists, it will be **Yes** (HTTPS/WSS). |
| Do you provide a way for users to request that their data is deleted? | Not applicable (no data collected). The page still explains in-app deletion. |
| Account creation | None. |
| Ads | **No**, the app contains no ads. |
| Target audience | 13+ (or 18+), consistent with the page. |

The answers change with online play: see §5.

---

## 4. Draft page (French)

> Replace the placeholders, keep the date up to date, and remove nothing that is still true.

---

# Politique de confidentialité de Sporz

*Dernière mise à jour : [DATE]*

Sporz est une application d'aide au maître du jeu pour le jeu de rôle *Sporz*. Elle est développée par **[NOM DU DÉVELOPPEUR tel qu'affiché sur Google Play]**, qui est responsable du traitement au sens du RGPD.

Contact pour toute question sur vos données : **[ADRESSE EMAIL]**

## En résumé
Sporz ne collecte aucune donnée personnelle. Il n'y a ni compte, ni publicité, ni outil de mesure d'audience. Les informations saisies pour une partie restent sur votre téléphone.

## Données saisies dans l'application
Pour organiser une partie, le maître du jeu saisit les **prénoms ou pseudonymes des joueurs**. L'application enregistre aussi le déroulement de la partie : rôles attribués, actions de nuit, votes et journal.

Ces informations :
- sont enregistrées **uniquement sur le téléphone**, dans l'espace privé de l'application, auquel les autres applications n'ont pas accès ;
- ne sont **jamais envoyées** au développeur ni à un tiers ;
- sont **exclues des sauvegardes automatiques** d'Android : elles ne sont copiées ni dans votre compte Google, ni vers un nouveau téléphone.

Elles servent uniquement à faire fonctionner le jeu : reprendre une partie interrompue et consulter les parties terminées (« Parties récentes »).

Nous vous conseillons d'utiliser des prénoms ou pseudonymes plutôt que des noms complets.

## Partage
L'application ne partage aucune donnée. Le seul moyen de faire sortir des informations de Sporz est le bouton **« Exporter le journal »** : il transmet le texte du journal de la partie (noms des joueurs, rôles, événements) à l'application que **vous choisissez** (email, messagerie…). Sporz n'en garde pas de copie ; le traitement ultérieur dépend de l'application choisie.

## Durée de conservation et suppression
- La partie en cours est conservée jusqu'à ce qu'une nouvelle partie la remplace.
- Les parties terminées sont conservées jusqu'à ce que vous les supprimiez.

Pour supprimer ces données :
- une partie terminée : **Parties récentes → la partie → « Supprimer la partie »** ;
- toutes les données : **Paramètres Android → Applications → Sporz → Stockage → Effacer les données**, ou désinstallez l'application.

Le développeur ne détenant aucune de ces données, il ne peut ni les consulter ni les supprimer à votre place.

## Ce que l'application n'utilise pas
- Aucun compte ni identifiant.
- Aucune publicité ni identifiant publicitaire.
- Aucun outil de statistiques ou de rapport de plantage.
- Aucun accès à la localisation, aux contacts, à l'appareil photo, au micro ou à vos fichiers.

## Sécurité
Les données sont stockées dans l'espace privé de l'application, protégé par Android. Elles ne transitent par aucun réseau.

## Enfants
Sporz est un jeu de société destiné à un public de **[13 / 18] ans et plus**. L'application ne collecte sciemment aucune donnée concernant des enfants.

## Vos droits
Conformément au RGPD, vous disposez d'un droit d'accès, de rectification, d'effacement, de limitation, d'opposition et de portabilité. Toutes les données restant sur votre appareil, vous pouvez les consulter, les modifier ou les supprimer directement dans l'application. Pour toute question, écrivez à **[ADRESSE EMAIL]**. Vous pouvez aussi adresser une réclamation à la **CNIL** (www.cnil.fr).

L'application ne prend aucune décision automatisée vous concernant et ne fait pas de profilage.

## Modifications
Cette politique sera mise à jour si l'application évolue, en particulier lors de l'arrivée du jeu en ligne. La date en haut de la page indique la dernière version. Les changements importants seront signalés dans les notes de mise à jour de l'application.

---

## 5. To add when online play ships (Milestone 2)

Online and semi-online play changes the answers. Update the page **before** publishing that version, and update the Data safety form at the same time.

| Topic | What to write | Decision / source |
|---|---|---|
| Data sent to the server | Nickname, game code, role and game state, night choices, votes, the journal; a random **player token** identifying the phone within one game. | `docs/openapi.yaml` |
| Recipient | The Sporz server, run by the developer on **[HÉBERGEUR / own server, COUNTRY]**. No other recipient. | DEC-18 |
| Retention on the server | Games deleted **48 hours** after they end (DEC-12). Say how long server logs (with **IP addresses**) are kept: **[DURATION]**. | DEC-12, §6 |
| Legal basis | Performance of the service requested by the user (art. 6.1.b GDPR); logs: legitimate interest (security, art. 6.1.f). | — |
| Security | HTTPS / WSS encryption in transit. | — |
| Transfers outside the EU | Only if the server or host is outside the EU: say where and on what basis. | §6 |
| Camera (QR code to join) | The camera is used only to read the game's QR code. Images are processed **on the phone** and never stored or sent. If Google ML Kit is used, check and mention what ML Kit itself sends to Google (usage metrics, model updates). | Ticket E4 |
| Links `sporz://join/…` | Only carry the game code. | Ticket E4 |
| Rematch | The same lobby is reused; same retention. | DEC-13 |
| Web app (v2, iPhone players) | Same data through the browser. Mention cookies or local storage if any. | Ticket F6, OPEN-1 |
| Data safety form | Answers become **Yes, collected**: "Name" (nickname) and "App activity / other user-generated content" (game actions); purpose "App functionality"; **not shared**; **encrypted in transit**; deletion **automatic after 48 h**. | — |
| Crash reporting (if added, F5) | Opt-in only. Name the provider, the data (device model, stack trace) and retention. | Ticket F5 |

---

## 6. Open points to settle before publishing

1. **Developer name and contact email** to show (must match the Play listing). An alias address is fine; it must be read.
2. **Target age** (13+ or 18+) in Play Console, and the same on the page.
3. **`INTERNET` permission in the offline release:** the offline version never uses it. Either keep it (harmless, and online play will need it) or remove it until Milestone 2 for a cleaner store listing ("no permissions"). Tell me and I'll do it in the app.
4. **Where the page is hosted:** any public HTTPS URL works (GitHub Pages, your own server). It must stay online as long as the app is on the store.
5. **English version:** needed when the app is translated (ticket F2). Until then, a French page is accepted.
6. For online play: the **host and country** of your server, and the **retention of server logs**.

*This brief describes the app's behaviour; it is not legal advice. For a commercial launch, a quick review by someone familiar with the GDPR is worthwhile.*
