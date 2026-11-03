package com.managementstyle.recommender.system.Questionaire;

public class Question {
    private final String id;
    private final Category category;
    private final String thesis;

    public Question(String id, Category category, String thesis) {
        this.id = id;
        this.category = category;
        this.thesis = thesis;
    }

    public String getId() { return id; }
    public Category getCategory() { return category; }
    public String getThesis() { return thesis; }
}