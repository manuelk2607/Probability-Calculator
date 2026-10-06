# Probability Calculator / Wahrscheinlichkeitsrechner

[Download / Herunterladen](https://github.com/manuelk2607/Probability-Calculator/releases/latest)

A local Java/Swing probability calculator with a compact interface inspired by statistical software, German/English help, charts and reusable calculation history.

Ein lokaler Java/Swing-Wahrscheinlichkeitsrechner mit kompakter, an Statistikprogramme angelehnter Oberflaeche, deutsch/englischer Hilfe, Diagrammen und wiederverwendbarem Eingabeverlauf.

## Deutsch

### Installation und Bedienung

- Im neuesten Release die macOS-DMG oder das plattformunabhaengige JAR-Paket herunterladen.
- Die macOS-App enthaelt eine Java-Runtime. Das JAR benoetigt Java 17 oder neuer: `java -jar Wahrscheinlichkeitsrechner-1.3.0.jar`.
- Berechnungsart auswaehlen, gelbe Eingabefelder ausfuellen und **Berechnen** anklicken. Graue Felder enthalten Informationen.
- Dezimalpunkt und Dezimalkomma sind erlaubt. Wahrscheinlichkeiten muessen zwischen 0 und 1 liegen; 25% wird als `0.25` oder `0,25` eingegeben.
- **Info** erklaert Eingaben, Voraussetzungen, Ergebnisse und Interpretation. Lange Hilfetexte lassen sich scrollen.
- **Sprache** wechselt Deutsch/Englisch. Eingaben bleiben beim Sprach- und Verfahrenswechsel erhalten.
- **Ergebnis kopieren** kopiert die Ausgabe in die Zwischenablage. **Leeren** leert die aktuellen Eingaben und Ergebnisse.

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
- Bayes-Listen werden durch Semikolon oder Leerzeichen getrennt, sind gleich lang und enthalten hoechstens 100 Werte. Die Basiswahrscheinlichkeiten summieren sich auf 1. Bei Pr(B)=0 ist der Posterior nicht definiert.
- Fuer Binomial sind n und k ganze Zahlen mit `0 <= k <= n`; Intervallgrenzen liegen zwischen 0 und n.
- Fuer Poisson gilt `0 < lambda <= 1e9`; k und Grenzen sind nichtnegative ganze Zahlen.
- Fuer Normal gilt `sigma > 0`; alle Zahlen muessen endlich sein. Grenzen sind aufsteigend.
- Wahrscheinlichkeiten erscheinen als Dezimal- und Prozentwerte. Erwartungswerte, Varianzen, Standardabweichungen und Dichten sind keine Prozentwerte.
- Verteilungsdiagramme zeigen den zentralen Bereich zwischen den Quantilen 0,01% und 99,99%. Grosse Bereiche werden zu hoechstens 60 Balken zusammengefasst. Die Berechnung selbst verwendet immer die eingegebenen Grenzen.
- Das Bayes-Diagramm zeigt die Beitraege zu Pr(B) plus Pr(nicht B), sodass der gesamte Balken 100% entspricht. Bei vielen Kategorien fasst es weitere A_i zusammen.

### Verlauf und Datenschutz

Der Reiter **Verlauf** enthaelt die letzten 100 erfolgreichen Berechnungen mit Zeitpunkt, Eingaben und Ergebnissen. Eintrag auswaehlen, Details ansehen und mit **Erneut laden** die Werte uebernehmen und neu berechnen. Laden erzeugt keinen doppelten Eintrag. Einzelne Eintraege und der gesamte Verlauf lassen sich loeschen.

**Verlauf lokal speichern** ist standardmaessig aktiviert. Berechnungen und Sprache liegen in `~/.probability-calculator/history.xml`. Die Historie ist lokal und unverschluesselt. Deaktivieren entfernt gespeicherte Berechnungen von der Festplatte; die aktuelle Sitzung bleibt sichtbar. Beschaedigte Dateien werden gemeldet und nicht automatisch ueberschrieben.

Die App benoetigt keine Datenbank, keinen Account und keine Internetverbindung. Sie sendet keine Eingaben, Ergebnisse oder Telemetrie. Maven benoetigt beim ersten Build Internet fuer Abhaengigkeiten; der fertige Rechner funktioniert offline.

## English

### Installation and Use

- Download the macOS DMG or cross-platform JAR package from the latest release.
- The macOS app bundles Java. The JAR requires Java 17 or newer: `java -jar Wahrscheinlichkeitsrechner-1.3.0.jar`.
- Select an analysis, fill the yellow input fields and click **Calculate**. Grey fields display information.
- Decimal points and commas are accepted. Enter probabilities between 0 and 1, such as `0.25` for 25%.
- **Info** explains inputs, assumptions, results and interpretation. Long help text is scrollable.
- **Language** switches German/English while preserving inputs. Each analysis also retains its inputs during the session.
- **Copy result** copies the output to the clipboard. **Clear** clears the current inputs and results.

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
- Bayes lists use semicolons or spaces, have equal lengths and contain at most 100 values. Priors sum to 1. The posterior is undefined when Pr(B)=0.
- Binomial n and k are integers satisfying `0 <= k <= n`; interval bounds are between 0 and n.
- Poisson requires `0 < lambda <= 1e9`; k and bounds are non-negative integers.
- Normal requires `sigma > 0`, finite numbers and ascending bounds.
- Probabilities have decimal and percentage output. Means, variances, standard deviations and densities do not use percentages.
- Distribution plots cover the central 0.01%-99.99% quantile range. Large ranges are grouped into at most 60 bars. Numerical results always use the entered bounds.
- Bayes plots show contributions to Pr(B) and the complement Pr(not B), filling 100%. Extra categories are grouped when needed.

### History and Privacy

The **History** tab keeps the last 100 successful calculations with timestamps, inputs and results. Select an entry to inspect it, then **Restore** its values and recalculate. Restoring does not add a duplicate entry. Delete individual entries or clear the entire history.

**Save history locally** is enabled by default. Calculations and language are stored in `~/.probability-calculator/history.xml`. Storage is local and unencrypted. Disabling removes stored calculations from disk while preserving the current session. Corrupt files are reported and preserved until explicitly reset.

The app needs no database, account or internet connection and transmits no inputs, results or telemetry. Maven needs internet for the initial dependency download; packaged apps work offline.

## Development / Entwicklung

Requirements: Java 17+, Maven. The numerical implementation uses [Apache Commons Statistics Distribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/dependency-info.html), bundled in the executable JAR.

```bash
mvn test
mvn package
java -jar target/Wahrscheinlichkeitsrechner-1.3.0.jar
# Alternative:
mvn exec:java
```

GUI integration tests require a desktop session. They exercise all seven calculations in both languages, history restoration and layouts at 980x720 and 1280x900; screenshots are generated under `target/ui-review/`.

```bash
mvn test -Dui.tests=true
```

For isolated testing, redirect local data with `-Dprobabilitycalculator.dataDir=/path/to/test-data` when starting Java.

macOS packaging (run on macOS; the JAR already includes dependencies):

```bash
mkdir -p dist/package-input
cp target/Wahrscheinlichkeitsrechner-1.3.0.jar dist/package-input/
jpackage --type dmg --name Wahrscheinlichkeitsrechner \
  --input dist/package-input \
  --main-jar Wahrscheinlichkeitsrechner-1.3.0.jar \
  --main-class probabilities.ProbabilityCalculatorGUI \
  --dest dist --app-version 1.3.0 --vendor manuelk2607
```

The macOS download is unsigned and not notarized. Windows and Linux installers must be built and tested on the respective operating system. The JAR is portable but has only been visually tested on macOS.

Architecture: Swing GUI -> immutable calculation requests -> calculation service -> probability functions. A background worker calculates results; a separate serial writer persists history with atomic replacement. No Java object deserialization, SQL or network clients are used.

[Audit / Pruefbericht](AUDIT.md)
