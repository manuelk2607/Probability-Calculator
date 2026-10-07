# Audit / Pruefbericht

Date / Datum: 2026-10-07. Version: 1.4.1.

## Deutsch

Geprueft wurden alle sieben Berechnungsarten, beide Sprachen, Ergebnisdarstellung, Diagramme, Eingaben, Hilfe, lokale Speicherung, Maven-Abhaengigkeiten und die Auslieferung als ausfuehrbares JAR.

| Bereich | Befund und Korrektur |
| --- | --- |
| Berechnungen | Bedingte Wahrscheinlichkeiten und Bayes konnten mit widerspruechlichen Eingaben Werte ueber 1 erzeugen. Schnittwahrscheinlichkeiten und Randwahrscheinlichkeiten werden nun konsistent geprueft; Nullwerte in Listen werden abgefangen. |
| Numerik | Apache Commons Statistics berechnet Verteilungen, Intervalle und rechte Raender. Rundungsfehler an Schnittgrenzen werden innerhalb weniger ULP abgefangen; Roh-Eingaben bleiben strikt auf [0,1] begrenzt. Akzeptierte Rundungsabweichungen in Bayes-Priors werden normalisiert. Standardisierung vermeidet Ueberlauf bei extremen endlichen Normalverteilungswerten. |
| Reaktionsfaehigkeit | Berechnung und Dateispeicherung laufen ausserhalb des Swing-Ereignisthreads. Aenderungen an Eingaben oder Verfahren verwerfen ausstehende Ergebnisse. |
| Einheiten | Erwartungswert, Varianz und Dichte wurden als Prozentwerte dargestellt. Prozentangaben stehen nun ausschliesslich bei Wahrscheinlichkeiten. |
| Diagramme | Bayes-Balken enthalten Pr(nicht B). Diskrete Verteilungsdiagramme sind auf 60 gruppierte Balken begrenzt. Die Normalverteilung zeigt eine Dichtekurve mit schattierten Intervallbereichen. Alle Diagramme bieten Wert-Tooltips; PNG-Export mit doppelter Aufloesung. |
| Layout und Sprache | Abgeschnittene Texte, zu stark schrumpfende Eingabepanels, ueberlagerte Legenden und zu grosse Hilfedialoge korrigiert. Eingaben bleiben beim Sprachwechsel erhalten. Titel, Beschriftungen und relevante Validierungsfehler sind zweisprachig. |
| Datenhaltung | 100 aktuelle Berechnungen plus bis zu 100 benannte Favoriten. Suche, Filter und sortierungsrichtige Auswahl/Exporte. Atomarer Dateiaustausch, begrenzte Dateigroesse/Inhalte und Migration des bisherigen Formats. Eine Sitzung haelt die Dateisperre bis alle Schreibauftraege fertig sind; andere Fenster speichern nicht. Keine Java-Objektdeserialisierung. |
| Exporte | Apache Commons CSV zitiert Felder nach RFC 4180. Textzellen werden gegen typische Formel-Praefixe geschuetzt. Ergebnis-CSV trennt Zahlenwerte und Prozentwerte; Nicht-Wahrscheinlichkeiten haben keine Prozentangabe. Dateizugriffe laufen im Hintergrund. |
| DB und Netzwerk | Keine Datenbank, kein Server und kein Netzwerkclient vorhanden. Daher keine SQL-Abfragen, Zugangsdaten oder Netzwerkendpunkte zu pruefen. Die fertig gebaute App arbeitet offline. |
| Build | Laufzeitabhaengigkeiten samt Apache-Lizenzhinweisen im ausfuehrbaren JAR. CI fuer Java 17 auf Windows/Linux/macOS und Java 25 auf Linux. Native Installer-Builds mit Java-Runtime und Smoke-Test des tatsaechlich gebuendelten JAR. Tag-basierte Release-Veroeffentlichung. |

Verifikation: 43 Rechen-/Speicher-/Exporttests sowie ein GUI-Integrationstest mit allen sieben Verfahren in Deutsch und Englisch, zwei Fenstergroessen, Verlaufsladen, Sortierung, Filtern, Favoriten, mehreren Fenstern, Kurven-Pixelpruefung und Tooltips. Grenzfaelle umfassen n/lambda=1e9, ungueltige Eingaben, Wahrscheinlichkeiten unter 1e-15, dezimale Schnittgrenzen, gerundete Priors und Normalparameter bis 1e308. Screenshots liegen bei aktivierter Desktop-Testausfuehrung unter target/ui-review.

Grenzen: Installer-Builds und automatisierte Desktop-Tests ersetzen keine manuelle Installation/Deinstallation auf jedem Zielsystem. macOS bleibt auf ausdruecklichen Wunsch ohne Developer-ID-Signatur und Notarisierung; auch Windows-Pakete sind unsigniert. Die lokale Historie ist unverschluesselt. Weitere Fenster desselben Datenverzeichnisses sind fuer Speicherung gesperrt und muessen nach Schliessen des ersten Fensters neu gestartet werden. Fuer unabhaengige Instanzen kann ein eigenes Datenverzeichnis konfiguriert werden. Dateisperren setzen ein lokales Dateisystem mit Lock-Unterstuetzung voraus. Berechnungen verwenden IEEE-754-Gleitkommazahlen, keine beliebig genaue Arithmetik. Diagramme zeigen begrenzte Ausschnitte, Ergebnisse aber die vollstaendigen eingegebenen Intervalle.

## English

Reviewed all seven calculation types, both languages, results, plots, input validation, help, local storage, Maven dependencies and executable JAR packaging.

Fixed inconsistent conditional/Bayes inputs that could produce probabilities above 1; slow factorial/summation algorithms; numerical loss in small tails; UI blocking; incorrect percent units for scalar results; incorrect Bayes bar proportions; oversized distribution plots; clipped text and overlapping legends; and lost inputs during language switches.

Version 1.4 adds rounding-safe intersections, normalized priors and overflow-safe normal standardization. History now retains 100 recent entries plus up to 100 named favorites, with search, filters, sorting and CSV export. A per-directory session lock prevents a second window from overwriting the first window's data. Version 1.3 files migrate automatically. Normal plots include a density curve, shaded regions and hover values; charts export as PNG. Result CSV preserves scalar/probability units, quotes fields using Apache Commons CSV and neutralizes typical spreadsheet-formula prefixes in text cells.

Validation comprises 43 numerical/storage/export tests and one desktop integration test covering every analysis in German/English, two window sizes, history restoration, filters, sorted selection, favorites, multiple windows, curve pixels and tooltips. Numerical cases include n/lambda=1e9, probabilities below 1e-15, rounded probability boundaries/priors and extreme normal parameters up to 1e308. CI runs Java 17 on Windows/Linux/macOS and Java 25 on Linux, builds native installers and smoke-tests the bundled runtime/JAR. Version tags publish releases automatically.

There is no database, backend server or network client; the packaged app works offline. Automated desktop/package tests do not replace manual installation/uninstallation on each target OS. macOS intentionally remains without a Developer ID signature or notarization; Windows packages are also unsigned. History is unencrypted. Additional windows have session-only storage and need restarting after the first window closes to acquire the lock. File locking assumes a local filesystem that supports locks. Separate data directories permit independent sessions. Calculations use IEEE-754 arithmetic, not arbitrary precision. Plots show bounded ranges; numerical interval probabilities are not truncated to the plot.
