# Billio

Eine Android-App für Solo-Freelancer zum Erstellen von **Angeboten** und **Rechnungen** — schnell, lokal, ohne Cloud. Teil des Portfolios neben Marea, ClockIN, Carby und Clocky.

> Kein Backend. Keine Cloud. Alle Daten bleiben auf dem Gerät.

## Features

- **Onboarding** — Profil, Adresse, Steuerstatus (§19 Kleinunternehmer / Regelbesteuert), MwSt.-Satz, Zahlungsziel, IBAN/BIC, Logo
- **Dashboard** — 3 animierte Ringe (Einnahmen/Ziel, Offen, Überfällig), Schnellaktionen, letzte Aktivität
- **Rechnungen** — Liste mit Tabs (Alle/Offen/Überfällig/Bezahlt), Swipe-to-pay & Swipe-to-delete, Detailansicht
- **Rechnung erstellen/bearbeiten** — Kundenauswahl, auto-generierte (editierbare) Nummer, dynamische Positionen, Live-Berechnung Netto/MwSt./Brutto pro Satz
- **Angebote** — gleiche Struktur, Swipe-to-convert → Rechnung (1 Tap, neue Nummer)
- **Kunden** — CRUD, Detail mit offenem Betrag, Gesamtumsatz und Historie
- **PDF** — EU-konformes Layout (native `PdfDocument`), Logo, alle Pflichtfelder, §19-Hinweis, Bankverbindung; Sprache folgt App-Sprache; Generierung im IO-Dispatcher
- **Export** — alle Rechnungen als ZIP mit PDFs teilen
- **EU-Konformität** — fortlaufende, lückenlose Nummern (atomar in Room), Stornierung erzeugt Stornorechnung statt Löschung
- **Theme** — Material 3, AMOLED-Dark + Light, folgt automatisch dem System
- **Sprachen** — Deutsch + Englisch (auto-detect, manuell umstellbar)

## Tech Stack

| Bereich | Technologie |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Architektur | MVVM + Repository |
| DI | Hilt |
| Datenbank | Room (SQLite) |
| Settings | DataStore Preferences |
| PDF | Android `PdfDocument` (nativ) |
| Navigation | Compose Navigation |
| Bilder | Coil |
| Min / Target SDK | 26 / 35 |

## Projektstruktur

```
app/src/main/java/com/mihara/billio/
  data/
    db/         Room: Entities, DAOs, Relations, Database, Converters
    prefs/      DataStore: SettingsRepository, UserSettings
    repository/ ClientRepository, InvoiceRepository
    model/      Enums (InvoiceType, InvoiceStatus, TaxMode)
  di/           Hilt-Module
  ui/
    dashboard/ invoice/ quote→invoice/ client/ settings/ onboarding/
    components/ RingChart, SwipeableCard, ExpandableFab, InvoiceCard, …
    theme/      Color, Type, Theme
    navigation/ Routes
  util/         PdfGenerator, Formatters (Money/Dates), InvoiceCalc
```

## Build & Run

Voraussetzungen: Android Studio (Ladybug+), JDK 17.

```bash
./gradlew assembleDebug      # Debug-APK bauen
./gradlew installDebug       # auf verbundenem Gerät installieren
```

Oder das Projekt in Android Studio öffnen und auf einem Gerät/Emulator (API 26+) starten.

## Rechnungsnummern

Format `PRÄFIX-JAHR-NUMMER`, z. B. `RE-2026-001`. Der Zähler wird pro Typ und Jahr in einer
einzigen Room-Transaktion atomar inkrementiert. Stornierte Rechnungen bleiben erhalten und werden
durch eine Stornorechnung (`CREDIT_NOTE`) mit eigener Nummer ausgeglichen.

## Lizenz

Proprietär — Mihara Arcadia.
