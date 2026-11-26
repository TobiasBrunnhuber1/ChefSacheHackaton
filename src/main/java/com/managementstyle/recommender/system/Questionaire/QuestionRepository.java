package com.managementstyle.recommender.system.Questionaire;

import org.springframework.stereotype.Repository;

import java.util.List;

// Stellt die 15 standardisierten deutschen Aussagen über fünf Führungsdimensionen bereit
@Repository
public class QuestionRepository {
    // Unveränderliche Liste aller 15 Items (je 3 pro Kategorie)
    private final List<Question> questions = List.of(
            // Bereich 1: Egoismus
            new Question(
                    "Expl_ego1",
                    Category.EGOISM,
                    "Sie/Er hält es für selbstverständlich, dass meine Arbeit zu ihrem/seinem persönlichen Vorteil genutzt werden kann.",
                    "Ich halte es für selbstverständlich, dass die Arbeit meiner Mitarbeitenden zu meinem persönlichen Vorteil genutzt werden kann."
            ),
            new Question(
                    "Expl_ego2",
                    Category.EGOISM,
                    "Sie/Er sieht Mitarbeitende als Mittel zum Zweck, um eigene Ziele zu erreichen.",
                    "Ich sehe Mitarbeitende als Mittel zum Zweck, um meine eigenen Ziele zu erreichen."
            ),
            new Question(
                    "Expl_ego3",
                    Category.EGOISM,
                    "Sie/Er stellt die Erreichung eigener Ziele über die Bedürfnisse der Mitarbeitenden.",
                    "Ich stelle die Erreichung meiner eigenen Ziele über die Bedürfnisse meiner Mitarbeitenden."
            ),
            // Bereich 2: Druck
            new Question(
                    "Expl_pres1",
                    Category.PRESSURE,
                    "Sie/Er setzt mich unter Druck, damit ich ihre/seine Ziele erreiche.",
                    "Ich setze Mitarbeitende unter Druck, damit sie meine Ziele erreichen."
            ),
            new Question(
                    "Expl_pres2",
                    Category.PRESSURE,
                    "Sie/Er erhöht mein Arbeitspensum ohne Rücksicht auf meine Bedürfnisse, um eigene Ziele zu erreichen.",
                    "Ich erhöhe das Arbeitspensum meiner Mitarbeitenden ohne Rücksicht auf ihre Bedürfnisse, um meine Ziele zu erreichen."
            ),
            new Question(
                    "Expl_pres3",
                    Category.PRESSURE,
                    "Sie/Er berücksichtigt mein aktuelles Arbeitspensum nicht, wenn neue Aufgaben vergeben werden.",
                    "Ich berücksichtige das aktuelle Arbeitspensum meiner Mitarbeitenden nicht, wenn ich neue Aufgaben vergebe."
            ),
            // Bereich 3: Entwicklungshemmung
            new Question(
                    "Expl_und1",
                    Category.UNDERMINING_DEVELOPMENT,
                    "Sie/Er gibt mir mühsame Aufgaben, wenn sie/er selbst davon profitieren kann.",
                    "Ich gebe Mitarbeitenden mühsame Aufgaben, wenn ich selbst davon profitieren kann."
            ),
            new Question(
                    "Expl_und2",
                    Category.UNDERMINING_DEVELOPMENT,
                    "Sie/Er gibt mir keine Möglichkeiten zur beruflichen Weiterentwicklung, weil eigene Ziele Vorrang haben.",
                    "Ich gebe Mitarbeitenden keine Möglichkeiten zur beruflichen Weiterentwicklung, weil meine eigenen Ziele Vorrang haben."
            ),
            new Question(
                    "Expl_und3",
                    Category.UNDERMINING_DEVELOPMENT,
                    "Sie/Er überträgt mir langweilige Routineaufgaben, wenn sie/er einen Nutzen daraus zieht.",
                    "Ich übertrage Mitarbeitenden langweilige Routineaufgaben, wenn ich einen Nutzen daraus ziehe."
            ),
            // Bereich 4: Vereinnahmung von Leistungen
            new Question(
                    "Expl_tc1",
                    Category.TAKING_CREDIT,
                    "Sie/Er nutzt meine Arbeit, um selbst Aufmerksamkeit und Anerkennung zu bekommen.",
                    "Ich nutze die Arbeit meiner Mitarbeitenden, um selbst Aufmerksamkeit und Anerkennung zu bekommen."
            ),
            new Question(
                    "Expl_tc2",
                    Category.TAKING_CREDIT,
                    "Sie/Er gibt die Arbeit des Teams als ihre/seine eigene aus.",
                    "Ich gebe die Arbeit des Teams als meine eigene aus."
            ),
            new Question(
                    "Expl_tc3",
                    Category.TAKING_CREDIT,
                    "Sie/Er nutzt meine Arbeit für ihren/seinen persönlichen Gewinn.",
                    "Ich nutze die Arbeit meiner Mitarbeitenden für meinen persönlichen Gewinn."
            ),
            // Bereich 5: Manipulation
            new Question(
                    "Expl_man1",
                    Category.MANIPULATION,
                    "Sie/Er spielt meine Kolleginnen und Kollegen sowie mich gegeneinander aus, um eigene Ziele zu erreichen.",
                    "Ich spiele Mitarbeitende gegeneinander aus, um meine Ziele zu erreichen."
            ),
            new Question(
                    "Expl_man2",
                    Category.MANIPULATION,
                    "Sie/Er manipuliert andere, um ihre/seine Ziele zu erreichen.",
                    "Ich manipuliere andere, um meine Ziele zu erreichen."
            ),
            new Question(
                    "Expl_man3",
                    Category.MANIPULATION,
                    "Sie/Er zögert nicht, Mitarbeitende zu manipulieren oder zu täuschen, um eigene Ziele zu erreichen.",
                    "Ich zögere nicht, Mitarbeitende zu manipulieren oder zu täuschen, um meine Ziele zu erreichen."
            )
    );

    // Gibt alle 15 Aussagen in fester Reihenfolge zurück
    public List<Question> findAll() {
        return questions;
    }
}
