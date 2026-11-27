package com.managementstyle.recommender.system;

import com.managementstyle.recommender.system.Questionaire.AssessmentSubmissionRepository;
import com.managementstyle.recommender.system.Questionaire.Category;
import com.managementstyle.recommender.system.Questionaire.TeamActionItemRepository;
import com.managementstyle.recommender.system.Questionaire.TeamSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Integrationstests für Fragebogen, DB-Speicherung, Gegenüberstellung und Verlaufsvergleich
@SpringBootTest
@AutoConfigureMockMvc
class RecommenderSystemApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssessmentSubmissionRepository submissionRepository;

    @Autowired
    private TeamActionItemRepository actionItemRepository;

    @Autowired
    private TeamSettingsRepository teamSettingsRepository;

    // Leert die Test-Datenbank vor jedem Testfall für reproduzierbare Ergebnisse
    @BeforeEach
    void cleanDatabase() {
        submissionRepository.deleteAll();
        actionItemRepository.deleteAll();
        teamSettingsRepository.deleteAll();
    }

    // Erzeugt einen gültigen POST-Request mit 15 identischen Likert-Antworten
    private MockHttpServletRequestBuilder submission(String value) {
        var request = post("/quiz/submit");
        for (int i = 0; i < 15; i++) {
            request.param("answers[" + i + "]", value);
        }
        return request;
    }

    // Prüft, dass alle 15 Aussagen getrennt, auf Deutsch und ohne Vorauswahl gerendert werden
    @Test
    void showsFifteenSeparateLikertGroupsWithoutWeightsOrPreselectedAnswers() throws Exception {
        String html = mvc.perform(get("/quiz")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (String code : new String[]{"ego", "pres", "und", "tc", "man"}) {
            for (int i = 1; i <= 3; i++) {
                assertThat(html).contains("Expl_" + code + i);
            }
        }
        for (int i = 0; i < 15; i++) {
            assertThat(html.split("name=\"answers\\[" + i + "\\]\"", -1)).hasSize(6);
        }
        assertThat(html).contains("value=\"1\"", "value=\"5\"", "type=\"radio\"")
                .contains("Denkanstoß", "Deadline- &amp; Zeitdruck")
                .doesNotContain("name=\"weights\"", "checked");
    }

    // Prüft, dass die Führungskraft-Perspektive in der Ich-Form angezeigt wird
    @Test
    void showsLeaderPerspectiveInFirstPerson() throws Exception {
        String html = mvc.perform(get("/quiz").param("role", "LEADER").param("team", "alpha"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Selbstreflexion als Führungskraft")
                .contains("Ich setze Mitarbeitende unter Druck");
    }

    // Prüft die Mittelwertberechnung an den Skalenrändern (1, 3, 5)
    @Test
    void calculatesMeansAtBothScaleLimitsAndNeutral() throws Exception {
        for (int answer : new int[]{1, 3, 5}) {
            var result = mvc.perform(submission(String.valueOf(answer)))
                    .andExpect(status().isOk()).andExpect(view().name("result"))
                    .andExpect(model().attribute("totalScore", (double) answer)).andReturn();
            Map<?, ?> scores = (Map<?, ?>) result.getModelAndView().getModel().get("scores");
            assertThat(scores).hasSize(5);
            for (Object score : scores.values()) {
                assertThat(score).isEqualTo((double) answer);
            }
        }
    }

    // Prüft unterschiedliche Bereichsmittelwerte und den korrekten Gesamtmittelwert
    @Test
    void calculatesDistinctAreaMeansAndOverallMean() throws Exception {
        var request = post("/quiz/submit");
        for (int i = 0; i < 15; i++) {
            request.param("answers[" + i + "]", i >= 12 ? "5" : String.valueOf(1 + 2 * (i % 3)));
        }
        var result = mvc.perform(request).andExpect(status().isOk())
                .andExpect(model().attribute("totalScore", 3.4)).andReturn();
        Map<?, ?> scores = (Map<?, ?>) result.getModelAndView().getModel().get("scores");
        assertThat(scores.get(Category.EGOISM)).isEqualTo(3.0);
        assertThat(scores.get(Category.MANIPULATION)).isEqualTo(5.0);
    }

    // Prüft, dass fehlende oder ungültige Werte sauber mit HTTP 400 abgelehnt werden
    @Test
    void rejectsMissingAndOutOfRangeAnswers() throws Exception {
        mvc.perform(post("/quiz/submit")).andExpect(status().isBadRequest());
        var missingMiddle = post("/quiz/submit");
        for (int i = 0; i < 15; i++) {
            if (i != 7) {
                missingMiddle.param("answers[" + i + "]", "3");
            }
        }
        mvc.perform(missingMiddle).andExpect(status().isBadRequest());
        for (String value : new String[]{"-3", "0", "6", "2.5", "text"}) {
            mvc.perform(submission(value)).andExpect(status().isBadRequest());
        }
        mvc.perform(submission("3").param("answers[15]", "3")).andExpect(status().isBadRequest());
    }

    // Prüft Speicherung in H2, Gegenüberstellung Selbst-/Fremdbild und Besserung über zwei Runden
    @Test
    void persistsLeaderAndEmployeesAndTracksImprovementAcrossRounds() throws Exception {
        // Runde 1: Führungskraft schätzt niedrig ein (1), zwei Mitarbeitende hoch (4 und 5)
        mvc.perform(submission("1")
                        .param("role", "LEADER")
                        .param("teamCode", "Team Alpha")
                        .param("roundName", "Runde 1")
                        .param("reflectionNote", "Ich sehe wenig Druck."))
                .andExpect(status().isOk());

        mvc.perform(submission("4")
                        .param("role", "EMPLOYEE")
                        .param("teamCode", "Team Alpha")
                        .param("roundName", "Runde 1")
                        .param("situationTags", "Deadline- & Zeitdruck")
                        .param("situationCategory", "PRESSURE")
                        .param("situationNote", "Kurzfristige Wochenend-Aufgaben ohne Priorisierung."))
                .andExpect(status().isOk());

        mvc.perform(submission("5")
                        .param("role", "EMPLOYEE")
                        .param("teamCode", "Team Alpha")
                        .param("roundName", "Runde 1"))
                .andExpect(status().isOk());

        // Runde 2: Mitarbeitende bewerten deutlich besser (2)
        mvc.perform(submission("2")
                        .param("role", "EMPLOYEE")
                        .param("teamCode", "Team Alpha")
                        .param("roundName", "Runde 2"))
                .andExpect(status().isOk());

        // Vergleichsansicht für Runde 1 prüfen (Blind Spot +3,50 und Situationsmeldung)
        String round1Html = mvc.perform(get("/compare").param("team", "team-alpha").param("round", "Runde 1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(round1Html)
                .contains("+3,50")
                .contains("Kurzfristige Wochenend-Aufgaben ohne Priorisierung.")
                .contains("Führungskraft: Wie kann ich dieses Verhalten ändern?")
                .contains("Mitarbeitende: Wie schützen &amp; Führungskraft ansprechen?");

        // Vergleichsansicht für Runde 2 prüfen (Trend "Bessert sich" mit -2,50)
        String round2Html = mvc.perform(get("/compare").param("team", "team-alpha").param("round", "Runde 2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(round2Html)
                .contains("Bessert sich")
                .contains("-2,50")
                .contains("Team-Ø (1 Antwort)");
    }

    // Prüft Hub-Startseite sowie das Laden des Demo-Szenarios in der Datenbank
    @Test
    void loadsHubAndSeedsDemoScenario() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mvc.perform(post("/demo/seed"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/compare?team=demo-team"));

        String compareHtml = mvc.perform(get("/compare").param("team", "demo-team"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(compareHtml)
                .contains("Runde 1 (Start)")
                .contains("Runde 2 (Follow-up)")
                .contains("Bessert sich")
                .contains("Verlauf nach Bereichen")
                .contains("Einzelaussagen mit der größten Abweichung")
                .contains("Kapazitätsboard im Weekly einführen");
    }

    // Prüft QR-Code-Generierung, gemischte Fragenreihenfolge und Datenschutz für private Denkanstoß-Notizen
    @Test
    void supportsQrCodeShuffledQuizAndPrivateVsSharedReflections() throws Exception {
        String qrSvg = mvc.perform(get("/qr").param("text", "http://localhost:8080/quiz?role=EMPLOYEE&team=nord"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(qrSvg).contains("<svg", "<path");

        String shuffledHtml = mvc.perform(get("/quiz").param("shuffle", "true"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (int i = 0; i < 15; i++) {
            assertThat(shuffledHtml.split("name=\"answers\\[" + i + "\\]\"", -1)).hasSize(6);
        }

        String privateResult = mvc.perform(submission("3")
                        .param("role", "EMPLOYEE")
                        .param("teamCode", "team-privat")
                        .param("roundName", "Runde 1")
                        .param("reflectionNote", "Sehr vertraulicher Gedanke"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(privateResult)
                .contains("Sehr vertraulicher Gedanke")
                .contains("Privat (nur für Sie sichtbar)");

        mvc.perform(submission("4")
                        .param("role", "EMPLOYEE")
                        .param("teamCode", "team-privat")
                        .param("roundName", "Runde 1")
                        .param("reflectionNote", "Öffentlich geteilter Impuls")
                        .param("shareReflectionWithTeam", "true"))
                .andExpect(status().isOk());

        String compareHtml = mvc.perform(get("/compare").param("team", "team-privat"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(compareHtml)
                .contains("Öffentlich geteilter Impuls")
                .doesNotContain("Sehr vertraulicher Gedanke");
    }

    // Prüft Anonymitätssperre (< 3 Antworten), Maßnahmen-Checkliste und vollständiges Team-Löschen
    @Test
    void enforcesAnonymityThresholdManagesActionsAndDeletesTeam() throws Exception {
        mvc.perform(post("/teams/settings")
                        .param("team", "team-schutz")
                        .param("anonymityProtected", "true"))
                .andExpect(status().is3xxRedirection());

        for (int i = 0; i < 2; i++) {
            mvc.perform(submission("4")
                            .param("role", "EMPLOYEE")
                            .param("teamCode", "team-schutz")
                            .param("roundName", "Runde 1"))
                    .andExpect(status().isOk());
        }

        String lockedHtml = mvc.perform(get("/compare").param("team", "team-schutz"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(lockedHtml).contains("Anonymitätsschutz aktiv");

        String revealedHtml = mvc.perform(get("/compare").param("team", "team-schutz").param("reveal", "true"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(revealedHtml)
                .contains("Die fünf Bereiche im direkten Vergleich")
                .contains("Team-Ø (2 Antworten)");

        mvc.perform(post("/compare/actions")
                        .param("team", "team-schutz")
                        .param("round", "Runde 1")
                        .param("category", "PRESSURE")
                        .param("reveal", "true")
                        .param("description", "Wöchentliches Kapazitäts-Check-in"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/compare?team=team-schutz&round=Runde+1&reveal=true"));

        mvc.perform(submission("4")
                        .param("role", "EMPLOYEE")
                        .param("teamCode", "team-schutz")
                        .param("roundName", "Runde 1"))
                .andExpect(status().isOk());

        String unlockedHtml = mvc.perform(get("/compare").param("team", "team-schutz"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(unlockedHtml).contains("Die fünf Bereiche im direkten Vergleich");

        Long actionId = actionItemRepository.findByTeamCodeOrderByCompletedAscCreatedAtDesc("team-schutz").get(0).getId();
        mvc.perform(post("/compare/actions/" + actionId + "/toggle").param("round", "Runde 1"))
                .andExpect(status().is3xxRedirection());
        assertThat(actionItemRepository.findById(actionId)).get()
                .satisfies(item -> assertThat(item.isCompleted()).isTrue());

        mvc.perform(post("/compare/actions/" + actionId + "/delete").param("round", "Runde 1"))
                .andExpect(status().is3xxRedirection());
        assertThat(actionItemRepository.findByTeamCodeOrderByCompletedAscCreatedAtDesc("team-schutz")).isEmpty();

        mvc.perform(post("/teams/delete").param("team", "team-schutz"))
                .andExpect(status().is3xxRedirection());
        assertThat(submissionRepository.findByTeamCodeOrderByRoundOrderAscSubmittedAtAsc("team-schutz")).isEmpty();
    }
}
