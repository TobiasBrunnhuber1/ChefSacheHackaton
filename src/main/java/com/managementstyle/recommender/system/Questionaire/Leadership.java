package com.managementstyle.recommender.system.Questionaire;

public enum Leadership {
    AUTORITAER("Autoritärer Führungsstil"),
    KOOPERATIV("Kooperativer Führungsstil"),
    LAISSEZ_FAIRE("Laissez-faire Führungsstil"),
    TRANSAKTIONAL("Transaktionaler Führungsstil"),
    TRANSFORMATIONAL("Transformationaler Führungsstil");

    private final String label;

    Leadership(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

