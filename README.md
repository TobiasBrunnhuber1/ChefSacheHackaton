# chefsache

Spring-Boot-Webanwendung zur Reflexion ausbeuterischen Führungsverhaltens.
Der Fragebogen umfasst 15 englischsprachige Aussagen mit einer fünfstufigen Likert-Skala.
Die Auswertung zeigt die Mittelwerte für Egoismus, Druck, Entwicklungshemmung,
Vereinnahmung von Leistungen und Manipulation sowie den Gesamtmittelwert.

## Voraussetzungen

- JDK 23 (JAVA_HOME auf die lokale JDK-Installation setzen)
- Internetzugang beim ersten Build zum Laden der Maven-Abhängigkeiten
- Maven wird über den enthaltenen Maven Wrapper bereitgestellt.

## Lokal starten

Repository klonen oder als ZIP herunterladen und im Projektordner ausführen.

Windows / PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```sh
sh mvnw spring-boot:run
```

Danach im Browser öffnen: http://localhost:8080/quiz

Mit Strg+C beenden. Falls Port 8080 belegt ist:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Die Seite ist dann unter http://localhost:8081/quiz erreichbar.

## IntelliJ IDEA

Den Projektordner oder die pom.xml öffnen, das Projekt-SDK auf JDK 23 setzen und
`RecommenderSystemApplication.main()` starten.

## Tests

```powershell
.\mvnw.cmd test
```

Unter macOS/Linux: `sh mvnw test`.

## Zusammenarbeit

Für Änderungen einen eigenen Branch anlegen, Änderungen committen und über einen Pull Request vorschlagen.
Ein öffentliches Repository erlaubt das Lesen und Klonen. Direkter Schreibzugriff setzt eine Einladung als Collaborator voraus.

## Technik

Java 23, Spring Boot 3.3.4, Thymeleaf, Maven, HTML/CSS und JavaScript.
Es ist keine Datenbank erforderlich; Antworten werden für die Auswertung verarbeitet und nicht dauerhaft gespeichert.