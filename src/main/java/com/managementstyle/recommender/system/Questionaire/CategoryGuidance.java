package com.managementstyle.recommender.system.Questionaire;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Fachliche Erklärungen, typische Situationen, Denkanstöße und beidseitige Handlungsempfehlungen je Kategorie
public record CategoryGuidance(
        String overview,
        List<String> typicalSituations,
        List<String> leaderChangeActions,
        List<String> employeeDefenseActions,
        String employeeAddressGuide,
        String employeeConversationExample,
        String thinkingPrompt
) {
    // Vordefinierte Situations-Tags für den Fragebogen und die Auswertung
    public static final List<String> PREDEFINED_SITUATION_TAGS = List.of(
            "Deadline- & Zeitdruck",
            "Präsentation vor Management",
            "Aufgabenverteilung & Delegation",
            "1:1- oder Feedbackgespräch",
            "Team-Meeting / Konflikt",
            "Ressourcen- & Personalengpass"
    );

    // Einmalig aufgebaute Zuordnung von jeder Kategorie zu ihren Inhalten
    private static final Map<Category, CategoryGuidance> BY_CATEGORY = buildGuidanceMap();

    // Liefert das komplette Leitfaden-Paket für eine Kategorie
    public static CategoryGuidance forCategory(Category category) {
        return BY_CATEGORY.get(category);
    }

    // Befüllt die Inhalte für alle fünf Führungsdimensionen
    private static Map<Category, CategoryGuidance> buildGuidanceMap() {
        Map<Category, CategoryGuidance> map = new EnumMap<>(Category.class);

        // Dimension 1: Egoismus
        map.put(Category.EGOISM, new CategoryGuidance(
                "Eigene Karriere-, Status- oder Zielinteressen stehen spürbar über dem Wohl und der Arbeitsrealität des Teams.",
                List.of(
                        "Vor wichtigen Quartals- oder Projektabschlüssen werden persönliche Kennzahlen der Führungskraft über berechtigte Team-Bedürfnisse (z. B. Erholungszeiten, Urlaubsvertretung) gestellt.",
                        "In Zielvereinbarungen werden Aufgaben so priorisiert, dass sie vor allem der Sichtbarkeit der Führungskraft nach oben dienen.",
                        "Bei Überlastung werden Sonderwünsche von Stakeholdern ungebremst ans Team durchgereicht, um selbst gut dazustehen."
                ),
                List.of(
                        "Eigene Motive vor Entscheidungen prüfen: „Wem nützt diese Maßnahme wirklich – dem Team/Kunden oder vor allem meinem Profil?“",
                        "Interessenkonflikte offen ansprechen und Entscheidungen nach nachvollziehbaren Team-Kriterien treffen.",
                        "Team-Bedürfnisse (z. B. Fokuszeit, realistische Urlaubs- und Kapazitätsplanung) aktiv gegenüber dem oberen Management verteidigen."
                ),
                List.of(
                        "Arbeitsbeiträge, Absprachen und eigene Kapazitäten schriftlich festhalten (z. B. kurzes Follow-up per E-Mail oder Ticket-Kommentar).",
                        "Bei kurzfristigen Sonderaufgaben ruhig nach Prioritäten fragen, statt stillschweigend Überstunden aufzubauen.",
                        "Sich mit Kolleginnen und Kollegen abstimmen, ob das Muster wiederkehrt, und im Bedarfsfall Vertrauensperson, HR oder Betriebsrat einbinden."
                ),
                "Im ruhigen 1:1-Gespräch konkrete Auswirkungen auf das gemeinsame Ergebnis statt Persönlichkeitsurteile benennen (Wahrnehmung – Wirkung – Wunsch).",
                "„Mir ist aufgefallen, dass die Zusatzaufgabe X kurzfristig vor unsere vereinbarten Meilensteine gerückt ist. Dadurch gerät unsere Qualität bei Y unter Druck. Können wir gemeinsam festlegen, was dafür verschoben wird?“",
                "Wann haben Sie zuletzt eine Entscheidung getroffen, die für Sie selbst bequem oder vorteilhaft war, aber für das Team zusätzlichen Reibungsverlust bedeutet hat?"
        ));

        // Dimension 2: Druck
        map.put(Category.PRESSURE, new CategoryGuidance(
                "Mitarbeitende werden über anhaltenden Leistungs- und Zeitdruck zur Zielerreichung gedrängt, ohne ihr tatsächliches Arbeitspensum zu berücksichtigen.",
                List.of(
                        "Bei knappen Deadlines oder Personalengpässen werden neue Aufgaben zugewiesen, ohne bestehende Aufgaben abzunehmen oder neu zu priorisieren.",
                        "In Statusmeetings wird Dringlichkeit künstlich erhöht oder implizit Erwartungshaltung für Erreichbarkeit außerhalb der Arbeitszeit erzeugt.",
                        "Warnsignale zu Überlastung werden als mangelnde Belastbarkeit abgetan statt als Kapazitätsproblem."
                ),
                List.of(
                        "Keine neue Aufgabe vergeben, ohne gemeinsam festzulegen, was dafür pausiert oder entfällt („One in, one out“-Prinzip).",
                        "Kapazitätsampel im Weekly einführen und Überlast-Signale ausdrücklich als wertvolle Frühwarnung würdigen.",
                        "Pufferzeiten und realistische Übergaben einplanen, statt persönliche Notfall-Prioritäten zum Dauerzustand zu machen."
                ),
                List.of(
                        "Aktuelle Aufgabenliste sichtbar machen und bei jedem neuen Auftrag eine klare Priorisierungsentscheidung einfordern.",
                        "Klare Grenzen bei Erreichbarkeit und Arbeitszeit ziehen und diese sachlich kommunizieren.",
                        "Überlastungsanzeigen bzw. sachliche Kapazitätsmeldungen schriftlich dokumentieren, damit die Verantwortung klar bleibt."
                ),
                "Sachlich mit konkreten Zahlen/Aufgabenpaketen arbeiten: Nicht „Es ist zu viel“, sondern die konkrete Kollision zweier Aufgaben aufzeigen.",
                "„Aktuell arbeite ich an A und B mit Abgabe am Donnerstag. Wenn C bis Mittwoch Priorität hat, kann B erst am Montag fertig werden – welche Reihenfolge soll ich umsetzen?“",
                "Welche Dringlichkeit der letzten Woche war objektiv notwendig – und wo wurde Druck weitergegeben, um eigene Unsicherheit zu kompensieren?"
        ));

        // Dimension 3: Entwicklungshemmung
        map.put(Category.UNDERMINING_DEVELOPMENT, new CategoryGuidance(
                "Entwicklungschancen, Lernzeit oder anspruchsvolle Aufgaben werden vorenthalten, wenn die Führungskraft selbst von Routinearbeit des Teams profitiert.",
                List.of(
                        "Spannende, sichtbare Projekte bleiben bei der Führungskraft oder einzelnen Favoriten, während ungeliebte Routinearbeit dauerhaft an dieselben Personen geht.",
                        "Weiterbildungen, Konferenzen oder Job-Rotation werden unter Verweis auf das Tagesgeschäft immer wieder verschoben.",
                        "Qualifizierte Mitarbeitende werden in ihrer bisherigen Rolle „festgehalten“, weil sie dort zuverlässig lästige Arbeit abnehmen."
                ),
                List.of(
                        "Sichtbare und lernintensive Aufgaben gezielt rotieren – auch wenn es kurzfristig Einarbeitungszeit kostet.",
                        "Verbindliche Lern- und Entwicklungszeit im Quartalsplan verankern und nicht fürs Tagesgeschäft opfern.",
                        "Routineaufgaben fair verteilen oder gemeinsam automatisieren/verschlanken, statt sie dauerhaft auf einzelne Schultern zu verlagern."
                ),
                List.of(
                        "Entwicklungsziele konkret schriftlich vereinbaren (inkl. Projekt- oder Lernbudget-Meilensteinen) und regelmäßig nachhalten.",
                        "Proaktiv kleine sichtbare Teilprojekte oder Verbesserungen vorschlagen und Verantwortung einfordern.",
                        "Wenn Entwicklung dauerhaft blockiert wird: Perspektiven mit nächsthöherer Führungsebene oder People/HR-Partner besprechen."
                ),
                "Das Gespräch an gemeinsamen Geschäftszielen aufhängen: Weiterentwicklung stärkt nicht nur den Einzelnen, sondern macht das Team resilienter.",
                "„Ich übernehme die wöchentlichen Routine-Reports gern zuverlässig. Damit ich mich in Richtung X weiterentwickeln kann, möchte ich im nächsten Sprint Teilbereich Y übernehmen – wie können wir die Routinearbeit dafür aufteilen?“",
                "Welche Person in Ihrem Team könnte heute deutlich mehr Verantwortung tragen, wird aber vor allem deshalb in Routineaufgaben gehalten, weil es Ihnen den Rücken freihält?"
        ));

        // Dimension 4: Vereinnahmung von Leistungen
        map.put(Category.TAKING_CREDIT, new CategoryGuidance(
                "Ideen, Analysen oder Erfolge von Mitarbeitenden werden nach oben oder außen so dargestellt, als wären sie die eigene Leistung der Führungskraft.",
                List.of(
                        "In Präsentationen vor der Geschäftsführung stellt die Führungskraft Konzepte vor, ohne die eigentlichen Urheberinnen und Urheber zu nennen.",
                        "Lobe oder Auszeichnungen aus anderen Abteilungen werden kommentarlos entgegengenommen statt ans Team weitergegeben.",
                        "Ausarbeitungen von Mitarbeitenden werden minimal umformuliert und unter eigenem Namen verschickt."
                ),
                List.of(
                        "Urheberschaft konsequent sichtbar machen: Namen in Folien, E-Mails und Meetings nennen und Teammitglieder selbst präsentieren lassen.",
                        "Lob aus höheren Ebenen öffentlich an die konkreten Beitragenden weiterleiten.",
                        "Die eigene Führungsrolle über Ermöglichen und Rahmenbedingungen definieren – nicht über das Einsammeln fachlicher Lorbeeren."
                ),
                List.of(
                        "Eigene Beiträge transparent dokumentieren (z. B. Autorenvermerk auf Dokumenten, geteilte Projekt-Updates im Team-Channel).",
                        "Nach Meetings proaktiv eine sachliche Zusammenfassung mit allen Beteiligten im Verteiler versenden.",
                        "Bei wiederholter Aneignung direkt nachfragen oder das Thema dokumentiert bei der nächsthöheren Instanz bzw. Vertrauensperson klären."
                ),
                "Anerkennung nicht als Vorwurf formulieren, sondern direkt Sichtbarkeit und gemeinsame Präsentation für das nächste Mal vorschlagen.",
                "„Es freut mich, dass unser Konzept beim Management gut ankam. Da ich die Analyse zu X erstellt habe, wäre es für meine Sichtbarkeit hilfreich, beim Follow-up-Termin selbst kurz vorzustellen – passt das für Sie?“",
                "Wann haben Sie zuletzt vor Vorgesetzten oder Kundschaft explizit hervorgehoben, wessen Idee oder Arbeit ein Erfolg eigentlich war?"
        ));

        // Dimension 5: Manipulation
        map.put(Category.MANIPULATION, new CategoryGuidance(
                "Informationen werden zurückgehalten, Teammitglieder gegeneinander ausgespielt oder Unwahrheiten genutzt, um eigene Ziele durchzusetzen.",
                List.of(
                        "In Einzelgesprächen werden Aussagen über Kolleginnen und Kollegen gestreut, um Misstrauen oder Konkurrenz zu schüren.",
                        "Wichtige Hintergrundinformationen werden selektiv geteilt, sodass Entscheidungen nicht nachvollziehbar sind.",
                        "Ziele oder Konsequenzen werden je nach Gesprächspartner unterschiedlich dargestellt, um schnelle Zustimmung zu erzwingen."
                ),
                List.of(
                        "Radikale Informationstransparenz schaffen: Entscheidungen, Gründe und Prioritäten gleichzeitig für alle nachvollziehbar teilen.",
                        "Konflikte zwischen Teammitgliedern direkt am gemeinsamen Tisch moderieren statt über vertrauliche Nebengespräche.",
                        "Zusagen verbindlich dokumentieren und einhalten – Vertrauen entsteht über Berechenbarkeit."
                ),
                List.of(
                        "Bei widersprüchlichen Aussagen oder Gerüchten direkt bei den betroffenen Kolleginnen und Kollegen sachlich rückfragen statt sich auf Hörensagen zu verlassen.",
                        "Wichtige Zusagen und Erwartungen konsequent schriftlich bestätigen lassen („Wie eben besprochen…“).",
                        "Bei systematischer Manipulation oder Täuschung frühzeitig Unterstützung durch Betriebsrat, HR, Compliance oder externe Beratung hinzuziehen."
                ),
                "Unklarheiten und Widersprüche sofort auf Faktenebene zurückführen, ohne Unterstellungen – und schriftliche Klarheit einfordern.",
                "„Ich habe zwei unterschiedliche Informationen zu Ziel X erhalten. Damit wir im Team sauber arbeiten können, möchte ich das gern in unserer gemeinsamen Runde kurz abgleichen und schriftlich festhalten.“",
                "Wo wurden in letzter Zeit Informationen zurückgehalten oder Dinge unterschiedlich dargestellt, weil offene Transparenz unbequem gewesen wäre?"
        ));

        return map;
    }
}
