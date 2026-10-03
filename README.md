# Wahrscheinlichkeitsrechner

Java-Rechner fuer grundlegende Wahrscheinlichkeitsrechnung mit Swing-GUI.

## Funktionen

- Komplementwahrscheinlichkeiten
- Schnittmengen und Vereinigungen
- bedingte Wahrscheinlichkeiten
- Satz der totalen Wahrscheinlichkeit
- Bayes-Rechnung fuer mehrere Faelle
- G*Power-orientiertes Desktop-Layout mit kompakten Eingabe- und Ausgabefeldern
- Diagramme fuer Komplementanteile, Vierfelder-Zerlegung, bedingte Anteile und Bayes-Beitraege

## Starten

Mit Maven:

```bash
mvn exec:java
```

Oder direkt mit Java:

```bash
javac -d target/classes $(find src/main/java -name '*.java')
java -cp target/classes probabilities.ProbabilityCalculatorGUI
```

Eingaben werden als Dezimalzahlen zwischen `0` und `1` erwartet. Kommas sind in der GUI ebenfalls erlaubt, zum Beispiel `0,25`.
