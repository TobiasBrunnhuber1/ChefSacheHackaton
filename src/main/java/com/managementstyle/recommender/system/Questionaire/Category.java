package com.managementstyle.recommender.system.Questionaire;

// Die fünf gemessenen Dimensionen ausbeuterischen Führungsverhaltens
public enum Category {
    EGOISM("Egoismus", "ego"),
    PRESSURE("Druck", "pres"),
    UNDERMINING_DEVELOPMENT("Entwicklungshemmung", "und"),
    TAKING_CREDIT("Vereinnahmung von Leistungen", "tc"),
    MANIPULATION("Manipulation", "man");

    // Deutscher Anzeigename der Dimension
    private final String label;
    // Kurzes Kürzel passend zu den Item-IDs (z. B. Expl_ego1)
    private final String code;

    Category(String label, String code) {
        this.label = label;
        this.code = code;
    }

    // Liefert den deutschen Bereichsnamen zurück
    public String getLabel() {
        return label;
    }

    // Liefert das Kurz-Kürzel für UI und Auswertung zurück
    public String getCode() {
        return code;
    }
}
