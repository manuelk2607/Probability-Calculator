# Probability Calculator / Wahrscheinlichkeitsrechner

[Download / Herunterladen](https://github.com/manuelk2607/Probability-Calculator/releases/latest)

A local Java/Swing probability calculator with a compact interface inspired by statistical software, German/English help, charts and reusable calculation history.

Ein lokaler Java/Swing-Wahrscheinlichkeitsrechner mit kompakter, an Statistikprogramme angelehnter Oberflaeche, deutsch/englischer Hilfe, Diagrammen und wiederverwendbarem Eingabeverlauf.

## Deutsch

### Installation und Bedienung

- Im neuesten Release die passende macOS-DMG, Windows-MSI/EXE, Linux-DEB oder das plattformunabhaengige JAR herunterladen. Die Architektur im Dateinamen muss zum Rechner passen.
- Native Pakete enthalten Java; keine separate Java-Installation ist noetig. Das JAR benoetigt Java 17 oder neuer: `java -jar Wahrscheinlichkeitsrechner-1.4.0.jar`.
- Die macOS-App ist bewusst unsigniert und nicht notarisiert. Nach einem blockierten Start kann diese konkrete App unter **Systemeinstellungen > Datenschutz & Sicherheit** freigegeben werden ([Apple-Anleitung](https://support.apple.com/en-us/102445)). Gatekeeper muss nicht global deaktiviert werden. Windows kann bei unsignierten Downloads ebenfalls warnen. Nur Pakete aus diesem Repository verwenden.
- Berechnungsart auswaehlen, gelbe Eingabefelder ausfuellen und **Berechnen** anklicken. Graue Felder enthalten Informationen.
- Dezimalpunkt und Dezimalkomma sind erlaubt. Wahrscheinlichkeiten muessen zwischen 0 und 1 liegen; 25% wird als `0.25` oder `0,25` eingegeben.
- **Info** erklaert Eingaben, Voraussetzungen, Ergebnisse und Interpretation. Lange Hilfetexte lassen sich scrollen.
- **Sprache** wechselt Deutsch/Englisch. Eingaben bleiben beim Sprach- und Verfahrenswechsel erhalten.
- **Ergebnis kopieren** kopiert die Ausgabe in die Zwischenablage. **Leeren** leert die aktuellen Eingaben und Ergebnisse.
- **CSV** bei den Ergebnissen exportiert Eingaben, Resultate und Prozentwerte; fuer Nicht-Wahrscheinlichkeiten bleibt die Prozentspalte leer. **PNG** exportiert das Diagramm mit doppelter Aufloesung.

### Berechnungen

| Verfahren | Eingaben | Ergebnisse |
| --- | --- | --- |
| Komplemente | Pr(A), Pr(B) | Pr(nicht A), Pr(nicht B) |
| Schnitt und Oder | Pr(A), Pr(B), optional Pr(A und B) | Schnitt, Vereinigung, nur A, nur B, weder A noch B |
| Bedingte Wahrscheinlichkeit | Pr(A), Pr(B), Pr(A und B) | Pr(A\|B), Pr(B\|A) |
| Bayes | Listen Pr(B\|A_i), Pr(A_i), Index ab 1 | Pr(B), Pr(A_i\|B), Beitraege zu Pr(B) |
| Binomial | n, k, untere/obere Grenze, p | Exakte, linke, rechte und Intervallwahrscheinlichkeit; Erwartungswert, Varianz, Standardabweichung |
| Poisson | lambda, k, untere/obere Grenze | Exakte, linke, rechte und Intervallwahrscheinlichkeit; Erwartungswert, Varianz, Standardabweichung |
| Normal | Mittelwert mu, Standardabweichung sigma, untere/obere Grenze | Linke, mittlere und rechte Flaeche; Dichte f(mu) |

- Bei leerer Schnittwahrscheinlichkeit wird fuer **Schnitt und Oder** Unabhaengigkeit angenommen.
- Schnittwahrscheinlichkeiten muessen zu beiden Randwahrscheinlichkeiten passen. Die Wahrscheinlichkeit der Bedingung darf nicht null sein.
- Bayes-Listen werden durch Semikolon oder Leerzeichen getrennt, sind gleich lang und enthalten hoechstens 100 Werte. Die Basiswahrscheinlichkeiten summieren sich auf 1; Abweichungen bis 1e-9 werden normalisiert. Bei Pr(B)=0 ist der Posterior nicht definiert.
- Fuer Binomial sind n und k ganze Zahlen mit `0 <= k <= n`; Intervallgrenzen liegen zwischen 0 und n.
- Fuer Poisson gilt `0 < lambda <= 1e9`; k und Grenzen sind nichtnegative ganze Zahlen.
- Fuer Normal gilt `sigma > 0`; alle Zahlen muessen endlich sein. Grenzen sind aufsteigend.
- Wahrscheinlichkeiten erscheinen als Dezimal- und Prozentwerte. Erwartungswerte, Varianzen, Standardabweichungen und Dichten sind keine Prozentwerte.
- Binomial-/Poisson-Diagramme zeigen den zentralen Bereich zwischen den Quantilen 0,01% und 99,99%. Grosse Bereiche werden zu hoechstens 60 Balken zusammengefasst. Die Berechnung selbst verwendet immer die eingegebenen Grenzen.
- Die Normalverteilung zeigt eine Dichtekurve zwischen mu-4*sigma und mu+4*sigma. Blaue, gruene und rote Flaechen markieren die Bereiche unterhalb, innerhalb und oberhalb des Intervalls. Die Wahrscheinlichkeiten umfassen auch nicht sichtbare Verteilungsschwaenze. Beim Darueberfahren erscheinen x, f(x) und Pr(X<=x). Balken zeigen ebenfalls Werte per Tooltip.
- Das Bayes-Diagramm zeigt die Beitraege zu Pr(B) plus Pr(nicht B), sodass der gesamte Balken 100% entspricht. Bei vielen Kategorien fasst es weitere A_i zusammen.

### Verlauf und Datenschutz

Der Reiter **Verlauf** enthaelt 100 aktuelle erfolgreiche Berechnungen plus bis zu 100 **Favoriten**, die nicht automatisch entfernt werden. **Benennen** vergibt einen Namen (max. 80 Zeichen). Suche durchsucht Namen, Eingaben, Berechnungsarten und Ergebnisse; Filter begrenzen die Anzeige auf eine Berechnungsart oder Favoriten. Spalten lassen sich sortieren. **CSV** exportiert nur die sichtbaren Eintraege in der angezeigten Reihenfolge. CSV verwendet UTF-8, Dezimalpunkte und korrekt zitierte Felder; potenzielle Formeln in Textfeldern bekommen zum Schutz ein vorangestelltes Apostroph.

Eintrag auswaehlen, Details ansehen und mit **Erneut laden** die Werte uebernehmen und neu berechnen. Laden erzeugt keinen doppelten Eintrag. Einzelne Eintraege und der gesamte Verlauf lassen sich loeschen.

**Verlauf lokal speichern** ist standardmaessig aktiviert. Berechnungen und Sprache liegen in `~/.probability-calculator/history.xml`. Die Historie ist lokal und unverschluesselt. Deaktivieren entfernt gespeicherte Berechnungen von der Festplatte; die aktuelle Sitzung bleibt sichtbar. Beschaedigte Dateien werden gemeldet und nicht automatisch ueberschrieben.

Eine Dateisperre erlaubt nur einem App-Fenster das Speichern in diesem Datenverzeichnis. Weitere Fenster zeigen einen Hinweis und arbeiten mit einem eigenen Sitzungsverlauf ohne Speichern; sie koennen die Datei nicht ueberschreiben. Nach dem Schliessen des ersten Fensters ist die Sperre frei, weitere Fenster muessen zum Speichern neu gestartet werden. Die vorhandene Historie aus Version 1.3 wird automatisch uebernommen.

Die App benoetigt keine Datenbank, keinen Account und keine Internetverbindung. Sie sendet keine Eingaben, Ergebnisse oder Telemetrie. Maven benoetigt beim ersten Build Internet fuer Abhaengigkeiten; der fertige Rechner funktioniert offline.

## English

### Installation and Use

- Download the macOS DMG, Windows MSI/EXE, Linux DEB or cross-platform JAR from the latest release. Match the filename architecture to your computer.
- Native packages bundle Java. The JAR requires Java 17 or newer: `java -jar Wahrscheinlichkeitsrechner-1.4.0.jar`.
- The macOS app is intentionally unsigned and not notarized. After a blocked start, allow this specific app in **System Settings > Privacy & Security** ([Apple instructions](https://support.apple.com/en-us/102445)). Do not disable Gatekeeper globally. Windows may also warn about unsigned downloads. Use only packages from this repository.
- Select an analysis, fill the yellow input fields and click **Calculate**. Grey fields display information.
- Decimal points and commas are accepted. Enter probabilities between 0 and 1, such as `0.25` for 25%.
- **Info** explains inputs, assumptions, results and interpretation. Long help text is scrollable.
- **Language** switches German/English while preserving inputs. Each analysis also retains its inputs during the session.
- **Copy result** copies the output to the clipboard. **Clear** clears the current inputs and results.
- Result **CSV** exports inputs, values and percentages; scalar results have no percentage. **PNG** exports the chart at double resolution.

### Calculations

| Analysis | Inputs | Outputs |
| --- | --- | --- |
| Complements | Pr(A), Pr(B) | Pr(not A), Pr(not B) |
| Joint and union | Pr(A), Pr(B), optional Pr(A and B) | Intersection, union, A only, B only, neither |
| Conditional | Pr(A), Pr(B), Pr(A and B) | Pr(A\|B), Pr(B\|A) |
| Bayes | Lists Pr(B\|A_i), Pr(A_i), one-based index | Pr(B), Pr(A_i\|B), contributions to Pr(B) |
| Binomial | n, k, lower/upper bounds, p | Exact, left-tail, right-tail and interval probabilities; mean, variance, standard deviation |
| Poisson | lambda, k, lower/upper bounds | Exact, left-tail, right-tail and interval probabilities; mean, variance, standard deviation |
| Normal | Mean mu, standard deviation sigma, lower/upper bounds | Left, middle and right areas; density f(mu) |

- Leaving the joint probability empty assumes independence for joint/union calculations.
- Intersections must agree with both marginal probabilities. Conditioning probabilities must be nonzero.
- Bayes lists use semicolons or spaces, have equal lengths and contain at most 100 values. Priors sum to 1; deviations up to 1e-9 are normalized. The posterior is undefined when Pr(B)=0.
- Binomial n and k are integers satisfying `0 <= k <= n`; interval bounds are between 0 and n.
- Poisson requires `0 < lambda <= 1e9`; k and bounds are non-negative integers.
- Normal requires `sigma > 0`, finite numbers and ascending bounds.
- Probabilities have decimal and percentage output. Means, variances, standard deviations and densities do not use percentages.
- Binomial/Poisson plots cover the central 0.01%-99.99% quantile range. Large ranges are grouped into at most 60 bars. Numerical results always use the entered bounds.
- Normal plots show the density curve from mu-4*sigma to mu+4*sigma. Blue, green and red regions mark values below, inside and above the interval. Numerical probabilities include tails outside the visible range. Hover displays x, f(x) and Pr(X<=x). Bar charts also have value tooltips.
- Bayes plots show contributions to Pr(B) and the complement Pr(not B), filling 100%. Extra categories are grouped when needed.

### History and Privacy

The **History** tab retains 100 recent calculations plus up to 100 **Favorites**, protected from automatic cleanup. **Rename** adds a name (max. 80 characters). Search covers names, inputs, calculation types and results; filters show a calculation type or favorites only. Columns support sorting. **CSV** exports only visible entries in their displayed order, using UTF-8, decimal points and quoted fields. Potential formulas in text cells are prefixed with an apostrophe for spreadsheet safety.

Select an entry to inspect it, then **Restore** its values and recalculate. Restoring does not add a duplicate entry. Delete individual entries or clear the entire history.

**Save history locally** is enabled by default. Calculations and language are stored in `~/.probability-calculator/history.xml`. Storage is local and unencrypted. Disabling removes stored calculations from disk while preserving the current session. Corrupt files are reported and preserved until explicitly reset.

A file lock allows only one window to save in each data directory. Additional windows display a notice and maintain session-only changes without overwriting the shared file. Closing the first window frees the lock; restart other windows to enable saving. Version 1.3 history is migrated automatically.

The app needs no database, account or internet connection and transmits no inputs, results or telemetry. Maven needs internet for the initial dependency download; packaged apps work offline.

## Development / Entwicklung

Requirements: Java 17+, Maven. Numerical functions use [Apache Commons Statistics Distribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/dependency-info.html), CSV uses [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/). Both are bundled in the executable JAR.

```bash
mvn test
mvn package
java -jar target/Wahrscheinlichkeitsrechner-1.4.0.jar
# Alternative:
mvn exec:java
```

GUI integration tests require a desktop session. They exercise all seven calculations in both languages, history restoration, filtering, favorites, multiple windows, curve rendering/tooltips and layouts at 980x720 and 1280x900; screenshots are generated under `target/ui-review/`. On Linux, use `xvfb-run -a mvn test -Dui.tests=true` without a physical desktop.

```bash
mvn test -Dui.tests=true
```

For isolated testing, redirect local data with `-Dprobabilitycalculator.dataDir=/path/to/test-data` when starting Java.

Native packaging (Python 3 is a build dependency only; run on the target OS):

```bash
mvn package
python scripts/package.py
# Optional: --type dmg / msi / exe / deb / app-image
```

Windows installers require WiX 3; Linux DEB requires dpkg-deb and fakeroot. GitHub Actions supplies these build environments. Each app image is tested with its bundled runtime before installer creation. Native installation/uninstallation and security prompts still need manual checks on the target machine.

The workflow tests Java 17 on Windows/Linux/macOS and Java 25 on Linux on every push/PR, creates screenshots and installer artifacts, and publishes a release when a `v<VERSION>` tag matching `pom.xml` is pushed. macOS builds remain unsigned/unnotarized by design. Installer artifacts include their OS and CPU architecture. Release notes are maintained in `RELEASE_NOTES.md`.

Architecture: Swing GUI -> immutable calculation requests -> calculation service -> probability functions. A background worker calculates results; a separate serial writer handles file exports and history with atomic replacement and a per-directory session lock. No Java object deserialization, SQL or network clients are used.

[Audit / Pruefbericht](AUDIT.md)
