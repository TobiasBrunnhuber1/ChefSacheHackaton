package com.managementstyle.recommender.system.Questionaire;

import java.util.ArrayList;
import java.util.List;

// Formular-DTO für das Absenden des Fragebogens inklusive optionaler Kontextfelder
public class QuizAnswers {
    // Die 15 Likert-Antworten von 1 bis 5
    private List<Integer> answers = new ArrayList<>();
    // Rolle der ausfüllenden Person (Standard: Mitarbeitende)
    private AssessmentRole role = AssessmentRole.EMPLOYEE;
    // Team-Code zur Gruppierung von Selbst- und Fremdeinschätzung
    private String teamCode;
    // Bezeichnung der Erhebungsrunde für Zeitvergleiche
    private String roundName;
    // Ausgewählte Situations-Tags
    private List<String> situationTags = new ArrayList<>();
    // Optionale Kategoriezuordnung einer geschilderten Situation
    private Category situationCategory;
    // Optionaler Freitext zu konkreten Situationen oder Verhaltensmustern
    private String situationNote;
    // Optionaler Freitext im Denkanstoß-Feld
    private String reflectionNote;
    // Steuert, ob die Denkanstoß-Notiz anonym im Team-Vergleich erscheinen darf
    private boolean shareReflectionWithTeam;

    // Liefert die Antwortenliste
    public List<Integer> getAnswers() {
        return answers;
    }

    // Setzt die Antwortenliste
    public void setAnswers(List<Integer> answers) {
        this.answers = answers;
    }

    // Liefert die Rolle
    public AssessmentRole getRole() {
        return role;
    }

    // Setzt die Rolle mit Fallback auf EMPLOYEE bei fehlendem Wert
    public void setRole(AssessmentRole role) {
        this.role = role != null ? role : AssessmentRole.EMPLOYEE;
    }

    // Liefert den Team-Code
    public String getTeamCode() {
        return teamCode;
    }

    // Setzt den Team-Code
    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    // Liefert den Rundennamen
    public String getRoundName() {
        return roundName;
    }

    // Setzt den Rundennamen
    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    // Liefert die gewählten Situations-Tags
    public List<String> getSituationTags() {
        return situationTags;
    }

    // Setzt die Situations-Tags ohne Null-Liste zuzulassen
    public void setSituationTags(List<String> situationTags) {
        this.situationTags = situationTags != null ? situationTags : new ArrayList<>();
    }

    // Liefert die gewählte Situationskategorie
    public Category getSituationCategory() {
        return situationCategory;
    }

    // Setzt die Situationskategorie
    public void setSituationCategory(Category situationCategory) {
        this.situationCategory = situationCategory;
    }

    // Liefert die Situationsbeschreibung
    public String getSituationNote() {
        return situationNote;
    }

    // Setzt die Situationsbeschreibung
    public void setSituationNote(String situationNote) {
        this.situationNote = situationNote;
    }

    // Liefert den Denkanstoß-Eintrag
    public String getReflectionNote() {
        return reflectionNote;
    }

    // Setzt den Denkanstoß-Eintrag
    public void setReflectionNote(String reflectionNote) {
        this.reflectionNote = reflectionNote;
    }

    // Liefert, ob die Denkanstoß-Notiz anonym geteilt werden darf
    public boolean isShareReflectionWithTeam() {
        return shareReflectionWithTeam;
    }

    // Legt fest, ob die Denkanstoß-Notiz anonym geteilt wird
    public void setShareReflectionWithTeam(boolean shareReflectionWithTeam) {
        this.shareReflectionWithTeam = shareReflectionWithTeam;
    }
}
