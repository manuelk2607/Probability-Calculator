# Probability Calculator / Wahrscheinlichkeitsrechner

**English:** A Java/Swing desktop tool for basic probability calculations with compact statistical-software-style input panels, result output, info dialogs, charts, and German/English language switching.

**Deutsch:** Ein Java/Swing-Desktoptool fuer grundlegende Wahrscheinlichkeitsrechnung im Stil kompakter Statistikprogramme. Die App bietet Eingabefelder, Ergebniswerte, Info-Dialoge, Diagramme und eine Sprachumschaltung zwischen Deutsch und Englisch.

## Download

Die aktuelle Version / latest version steht im GitHub-Release bereit:

https://github.com/manuelk2607/Wahrscheinlichkeitsrechner/releases

- `Wahrscheinlichkeitsrechner-1.1.0.dmg`: macOS-App mit gebuendelter Java-Runtime.
- `Wahrscheinlichkeitsrechner-1.1.0-jar.zip`: kleine JAR-Version fuer Systeme mit installiertem Java.

## Features / Funktionen

English:

- Complements: `Pr(not A)`, `Pr(not B)`
- Joint and union probabilities: `Pr(A and B)`, `Pr(A or B)`, exclusive parts
- Conditional probabilities: `Pr(A|B)`, `Pr(B|A)`
- Bayes and total probability
- Binomial, Poisson, and normal distributions
- Charts for complements, four-part decompositions, conditional proportions, Bayes contributions, and distributions
- Info buttons for every calculation type
- German/English language switch

Deutsch:

- Komplementwahrscheinlichkeiten: `Pr(not A)`, `Pr(not B)`
- Schnittmengen und Vereinigungen: `Pr(A and B)`, `Pr(A or B)`, exklusive Anteile
- Bedingte Wahrscheinlichkeiten: `Pr(A|B)`, `Pr(B|A)`
- Bayes und Satz der totalen Wahrscheinlichkeit
- Binomialverteilung mit exakter, kumulativer und Intervall-Wahrscheinlichkeit
- Poissonverteilung mit exakter, kumulativer und Intervall-Wahrscheinlichkeit
- Normalverteilung mit Links-, Intervall- und Rechtswahrscheinlichkeit
- Diagramme fuer Komplementanteile, Vierfelder-Zerlegung, bedingte Anteile, Bayes-Beitraege und Verteilungen
- Info-Buttons fuer jede Berechnungsart
- Sprachumschaltung zwischen Deutsch und Englisch

## Bedienung

1. Oben bei `Testfamilie` bzw. `Test family` eine Berechnungsart auswaehlen.
2. Links die benoetigten Werte eintragen.
3. Mit `Berechnen` / `Calculate` die Rechnung starten.
4. Rechts erscheinen die Ergebnisse und darunter ein Diagramm.
5. Mit `Info` oeffnet sich eine kurze Beschreibung der benoetigten Eingaben und Ergebnisse.
6. Ueber `Sprache` / `Language` kann zwischen Deutsch und Englisch gewechselt werden.

Wahrscheinlichkeiten werden als Dezimalzahlen zwischen `0` und `1` eingegeben. Die GUI akzeptiert Punkt und Komma, zum Beispiel `0.25` oder `0,25`.

## Berechnungsarten

### Komplemente

Eingaben:

- `Pr(A)`
- `Pr(B)`

Ergebnisse:

- `Pr(not A) = 1 - Pr(A)`
- `Pr(not B) = 1 - Pr(B)`

### Schnitt und Oder

Eingaben:

- `Pr(A)`
- `Pr(B)`
- optional `Pr(A and B)`

Wenn `Pr(A and B)` leer bleibt, wird Unabhaengigkeit angenommen.

Ergebnisse:

- `Pr(A and B)`
- `Pr(A or B)`
- `Pr(A without B)`
- `Pr(B without A)`
- `Pr(neither)`

### Bedingte Wahrscheinlichkeit

Eingaben:

- `Pr(A and B)`
- `Pr(A)`
- `Pr(B)`

Ergebnisse:

- `Pr(A|B) = Pr(A and B) / Pr(B)`
- `Pr(B|A) = Pr(A and B) / Pr(A)`

### Bayes

Eingaben:

- Liste `Pr(B|A_i)`
- Liste `Pr(A_i)`
- Index des gesuchten `A_i`

Listenwerte koennen mit Semikolon oder Leerzeichen getrennt werden. Die Werte von `Pr(A_i)` muessen zusammen `1` ergeben.

Ergebnisse:

- `Pr(B)`
- `Pr(A_i|B)`
- Beitraege `Pr(B|A_i) * Pr(A_i)`

### Binomialverteilung

Eingaben:

- `n`: Anzahl der unabhaengigen Versuche
- `k`: genaue Trefferzahl
- `lower`, `upper`: Intervallgrenzen
- `p`: Trefferwahrscheinlichkeit

Ergebnisse:

- `Pr(X = k)`
- `Pr(X <= k)`
- `Pr(lower <= X <= upper)`
- `E(X)`
- `Var(X)`

### Poissonverteilung

Eingaben:

- `lambda`: erwartete Ereignisanzahl im Intervall
- `k`: genaue Anzahl
- `lower`, `upper`: Intervallgrenzen

Ergebnisse:

- `Pr(X = k)`
- `Pr(X <= k)`
- `Pr(lower <= X <= upper)`
- `E(X)`
- `Var(X)`

### Normalverteilung

Eingaben:

- `mu`: Mittelwert
- `sigma`: Standardabweichung
- `lower`, `upper`: Grenzen auf der x-Achse

Ergebnisse:

- `Pr(X <= lower)`
- `Pr(lower <= X <= upper)`
- `Pr(X > upper)`
- `f(mu)`

## Entwicklung

Voraussetzungen:

- Java 17 oder neuer
- Maven

Tests ausfuehren:

```bash
mvn test
```

App lokal starten:

```bash
mvn exec:java
```

JAR bauen:

```bash
mvn clean package
java -jar target/Wahrscheinlichkeitsrechner-1.0-SNAPSHOT.jar
```

macOS-DMG bauen:

```bash
mkdir -p dist
jpackage \
  --type dmg \
  --name Wahrscheinlichkeitsrechner \
  --input target \
  --main-jar Wahrscheinlichkeitsrechner-1.0-SNAPSHOT.jar \
  --main-class probabilities.ProbabilityCalculatorGUI \
  --dest dist \
  --app-version 1.0.0 \
  --vendor manuelk2607
```

## English Summary

Probability Calculator is a Java/Swing desktop app for basic probability calculations. It supports complements, joint and conditional probabilities, Bayes, binomial, Poisson, and normal distributions. The interface includes charts, feature-specific info dialogs, and a German/English language switch.
