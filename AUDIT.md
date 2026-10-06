# Audit / Pruefbericht

Date / Datum: 2026-10-06. Version: 1.3.0.

## Deutsch

Geprueft wurden alle sieben Berechnungsarten, beide Sprachen, Ergebnisdarstellung, Diagramme, Eingaben, Hilfe, lokale Speicherung, Maven-Abhaengigkeiten und die Auslieferung als ausfuehrbares JAR.

| Bereich | Befund und Korrektur |
| --- | --- |
| Berechnungen | Bedingte Wahrscheinlichkeiten und Bayes konnten mit widerspruechlichen Eingaben Werte ueber 1 erzeugen. Schnittwahrscheinlichkeiten und Randwahrscheinlichkeiten werden nun konsistent geprueft; Nullwerte in Listen werden abgefangen. |
| Numerik | Die bisherigen Summen/Fakultaeten wurden bei grossen n und k sehr langsam; Differenzen nahe 1 verloren kleine Wahrscheinlichkeiten. Apache Commons Statistics berechnet Verteilungen, Intervalle und rechte Raender. |
| Reaktionsfaehigkeit | Berechnung und Dateispeicherung laufen ausserhalb des Swing-Ereignisthreads. Aenderungen an Eingaben oder Verfahren verwerfen ausstehende Ergebnisse. |
| Einheiten | Erwartungswert, Varianz und Dichte wurden als Prozentwerte dargestellt. Prozentangaben stehen nun ausschliesslich bei Wahrscheinlichkeiten. |
| Diagramme | Das letzte Bayes-Segment fuellte faelschlich den verbleibenden Balken. Pr(nicht B) ergaenzt nun die Beitraege auf 100%. Verteilungsdiagramme sind auf 60 gruppierte Balken begrenzt und zeigen auch grosse Mittelwerte. |
| Layout und Sprache | Abgeschnittene Texte, zu stark schrumpfende Eingabepanels, ueberlagerte Legenden und zu grosse Hilfedialoge korrigiert. Eingaben bleiben beim Sprachwechsel erhalten. Titel, Beschriftungen und relevante Validierungsfehler sind zweisprachig. |
| Datenhaltung | Neue lokale Historie fuer die letzten 100 erfolgreichen Berechnungen. Atomarer Dateiaustausch, begrenzte Dateigroesse und Inhalte, deaktivierbare Speicherung und Fehlerbehandlung fuer beschaedigte Dateien. Keine Java-Objektdeserialisierung. |
| DB und Netzwerk | Keine Datenbank, kein Server und kein Netzwerkclient vorhanden. Daher keine SQL-Abfragen, Zugangsdaten oder Netzwerkendpunkte zu pruefen. Die fertig gebaute App arbeitet offline. |
| Build | Ungenutztes JUnit 4 entfernt, Test-Runner explizit festgelegt, Laufzeitabhaengigkeiten samt Apache-Lizenzhinweisen im ausfuehrbaren JAR gebuendelt, Versionsnummer und Downloadlinks aktualisiert. |

Verifikation: 32 Rechen-/Speichertests sowie ein GUI-Integrationstest mit allen sieben Verfahren in Deutsch und Englisch, zwei Fenstergroessen, Verlaufsladen und dauerhafter Speicherung. Grenzfaelle umfassen n/lambda=1e9, degenerierte Binomialverteilungen, ungueltige Eingaben und Wahrscheinlichkeiten unter 1e-15. GUI-Screenshots werden bei aktivierter Desktop-Testausfuehrung erzeugt.

Grenzen: Visuelle Tests erfolgten auf macOS. Windows/Linux-Installer und deren native Darstellung wurden nicht geprueft. Der macOS-Download ist nicht signiert/notarisiert. Die lokale Historie ist unverschluesselt; mehrere parallel laufende Instanzen teilen dieselbe Datei und koennen den Verlauf gegenseitig ueberschreiben. Fuer getrennte Instanzen kann ein eigenes Datenverzeichnis konfiguriert werden. Die Berechnung verwendet IEEE-754-Gleitkommazahlen, keine beliebig genaue Arithmetik.

## English

Reviewed all seven calculation types, both languages, results, plots, input validation, help, local storage, Maven dependencies and executable JAR packaging.

Fixed inconsistent conditional/Bayes inputs that could produce probabilities above 1; slow factorial/summation algorithms; numerical loss in small tails; UI blocking; incorrect percent units for scalar results; incorrect Bayes bar proportions; oversized distribution plots; clipped text and overlapping legends; and lost inputs during language switches.

Added local history for the last 100 successful calculations, timestamps, input/result inspection, restoration, deletion, optional persistence, atomic file replacement and recovery from unreadable files. Added right-tail probabilities, standard deviations and result copying. Runtime dependencies and Apache license notices are bundled in the executable JAR.

Validation comprises 32 numerical/storage tests and one desktop GUI integration test covering every analysis in German/English, two window sizes, history restoration and persistence. Numerical cases include n/lambda=1e9, degenerate binomial distributions, invalid inputs and probabilities below 1e-15.

There is no database, backend server or network client; the packaged app works offline. Visual verification was performed on macOS only. Windows/Linux installers and native layouts remain unverified, and the macOS app is unsigned/unnotarized. Local history is unencrypted; concurrent app instances share the file and may overwrite each other's history. Configure separate data directories for independent instances. Calculations use IEEE-754 floating-point arithmetic.
