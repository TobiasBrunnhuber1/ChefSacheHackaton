package com.managementstyle.recommender.system.Questionaire;

// Einzelne Aussage des Fragebogens inklusive Mitarbeiter- und Führungskraft-Formulierung
public class Question {
    // Eindeutige Item-ID (z. B. Expl_ego1)
    private final String id;
    // Zugehörige Bewertungsdimension
    private final Category category;
    // Formulierung aus Sicht von Mitarbeitenden
    private final String employeeThesis;
    // Formulierung als Ich-Aussage für die Führungskraft
    private final String leaderThesis;

    public Question(String id, Category category, String employeeThesis, String leaderThesis) {
        this.id = id;
        this.category = category;
        this.employeeThesis = employeeThesis;
        this.leaderThesis = leaderThesis;
    }

    // Liefert die Item-ID
    public String getId() {
        return id;
    }

    // Liefert die Kategorie der Aussage
    public Category getCategory() {
        return category;
    }

    // Standard-Aussage (Mitarbeiterperspektive) für Abwärtskompatibilität
    public String getThesis() {
        return employeeThesis;
    }

    // Wählt den passenden deutschen Aussagetext anhand der ausgefüllten Rolle
    public String getThesisFor(AssessmentRole role) {
        return role == AssessmentRole.LEADER ? leaderThesis : employeeThesis;
    }
}
