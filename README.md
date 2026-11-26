# chefsache

Spring-Boot-Webanwendung zur Reflexion und Gegenüberstellung von Führungsverhalten (Selbstbild Führungskraft vs. anonymes Team-Feedback).
Der Fragebogen umfasst 15 deutschsprachige Aussagen in fünf Bereichen auf einer fünfstufigen Likert-Skala sowie optionale Situations-Tags, konkrete Verhaltensbeschreibungen und ein Denkanstoß-Feld.

## Funktionen

- **Start-Hub (`/`)**: Team-Code und Runde festlegen, Einladungslinks für Führungskraft und Mitarbeitende (inkl. lokaler Netzwerk-IP) kopieren oder ein 2-Runden-Demo-Szenario laden.
- **Fragebogen (`/quiz`)**: Rollenabhängige Formulierung für Mitarbeitende (Fremdbild) und Führungskraft (Ich-Form), Situations-Tags, Freitext für Verhaltensmuster und Denkanstoß-Feld.
- **Auswertung (`/quiz/submit`)**: Ampel-Farbskala (Unauffällig / Aufmerksamkeitsbedarf / Kritisch), Erklärung typischer Situationen sowie beidseitige Handlungsempfehlungen.
- **Team-Vergleich (`/compare`)**: Gegenüberstellung Selbst- vs. Fremdbild (Blind Spot), gesammelte Situationen und Verlaufsdiagramm über mehrere Erhebungsrunden (*„Bessert sich das Verhalten?“*).

## Voraussetzungen

- JDK 23 (bzw. kompatibles JDK 21+ mit `-Djava.version=21`)
- Internetzugang beim ersten Build zum Laden der Maven-Abhängigkeiten
- Maven wird über den enthaltenen Maven Wrapper bereitgestellt.

## Lokal starten

Windows / PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```sh
sh mvnw spring-boot:run
```

Danach im Browser öffnen: `http://localhost:8080/` (bzw. für Mitarbeitende im selben WLAN über die auf der Startseite angezeigte lokale IP).

Mit `Strg+C` beenden. Falls Port 8080 belegt ist:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

## Tests

```powershell
.\mvnw.cmd test
```

Unter macOS/Linux: `sh mvnw test`.

## Technik

Java 23, Spring Boot 3.3.4, Spring Data JPA, H2 (dateibasierte lokale Datenbank unter `./data/chefsache`), Thymeleaf, Maven, HTML/CSS und JavaScript.

## Mitwirkende

- Tobias Brunnhuber
- Aaron
