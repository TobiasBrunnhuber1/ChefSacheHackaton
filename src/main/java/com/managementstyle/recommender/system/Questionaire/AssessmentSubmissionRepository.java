package com.managementstyle.recommender.system.Questionaire;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Spring-Data-JPA-Repository für den Datenbankzugriff auf alle Fragebogen-Abgaben
public interface AssessmentSubmissionRepository extends JpaRepository<AssessmentSubmission, Long> {
    // Lädt alle Abgaben eines Teams chronologisch nach Rundenreihenfolge und Zeitstempel
    List<AssessmentSubmission> findByTeamCodeOrderByRoundOrderAscSubmittedAtAsc(String teamCode);

    // Lädt sämtliche Abgaben absteigend nach Abgabezeitpunkt für die Team-Übersicht
    List<AssessmentSubmission> findAllByOrderBySubmittedAtDesc();

    // Entfernt alle vorhandenen Abgaben eines Teams (z. B. beim Zurücksetzen der Demo-Daten)
    void deleteByTeamCode(String teamCode);
}
