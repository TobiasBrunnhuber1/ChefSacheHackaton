package com.managementstyle.recommender.system.Questionaire;

// Ampel-Klassifikation für Scores auf der Skala von 1 (niedrig) bis 5 (kritisch)
public enum ScoreSeverity {
    // Werte bis 2,0 gelten als unauffällig
    LOW("Unauffällig", "Geringe Ausprägung – kein akuter Handlungsbedarf.", "severity-low"),
    // Werte über 2,0 bis unter 3,25 erfordern Aufmerksamkeit
    MODERATE("Aufmerksamkeitsbedarf", "Erhöhte Ausprägung – Situationen gemeinsam reflektieren und gegensteuern.", "severity-moderate"),
    // Werte ab 3,25 signalisieren akuten Handlungsbedarf
    HIGH("Kritisch", "Starke Ausprägung – konkreter Handlungsbedarf für Führungskraft und Team.", "severity-high");

    // Sichtbarer Status-Text für das Ampel-Badge
    private final String label;
    // Einordnender Kurzsatz für die Gesamtauswertung
    private final String summary;
    // Zugehörige CSS-Klasse für Farbgebung
    private final String cssClass;

    ScoreSeverity(String label, String summary, String cssClass) {
        this.label = label;
        this.summary = summary;
        this.cssClass = cssClass;
    }

    // Ordnet einen Mittelwert (1,0 bis 5,0) eindeutig einer Ampelstufe zu
    public static ScoreSeverity fromScore(double score) {
        if (score <= 2.0) {
            return LOW;
        }
        if (score < 3.25) {
            return MODERATE;
        }
        return HIGH;
    }

    // Liefert das Status-Label
    public String getLabel() {
        return label;
    }

    // Liefert die erklärende Zusammenfassung
    public String getSummary() {
        return summary;
    }

    // Liefert die CSS-Klasse der Ampelfarbe
    public String getCssClass() {
        return cssClass;
    }

    // Gibt an, ob Handlungsempfehlungen direkt aufgeklappt angezeigt werden sollen
    public boolean isActionRecommended() {
        return this != LOW;
    }
}
