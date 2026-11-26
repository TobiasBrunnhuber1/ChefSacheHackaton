package com.managementstyle.recommender.system.Questionaire;

// Perspektive, aus der ein Fragebogen ausgefüllt wird
public enum AssessmentRole {
    // Anonyme Fremdeinschätzung durch Teammitglieder
    EMPLOYEE("Mitarbeitende (Fremdeinschätzung)", "Mitarbeitende"),
    // Selbsteinschätzung durch die Führungskraft in der Ich-Form
    LEADER("Führungskraft (Selbsteinschätzung)", "Führungskraft");

    // Ausführliche Bezeichnung für Überschriften und Badges
    private final String label;
    // Kompakte Bezeichnung für Tabellen und Kommentar-Chips
    private final String shortLabel;

    AssessmentRole(String label, String shortLabel) {
        this.label = label;
        this.shortLabel = shortLabel;
    }

    // Liefert die ausführliche Rollenbezeichnung
    public String getLabel() {
        return label;
    }

    // Liefert die kurze Rollenbezeichnung
    public String getShortLabel() {
        return shortLabel;
    }
}
