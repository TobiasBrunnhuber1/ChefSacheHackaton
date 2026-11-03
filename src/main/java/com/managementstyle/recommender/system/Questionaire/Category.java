package com.managementstyle.recommender.system.Questionaire;

public enum Category {
    EGOISM("Egoismus", "ego"),
    PRESSURE("Druck", "pres"),
    UNDERMINING_DEVELOPMENT("Entwicklungshemmung", "und"),
    TAKING_CREDIT("Vereinnahmung von Leistungen", "tc"),
    MANIPULATION("Manipulation", "man");

    private final String label;
    private final String code;

    Category(String label, String code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() { return label; }
    public String getCode() { return code; }
}