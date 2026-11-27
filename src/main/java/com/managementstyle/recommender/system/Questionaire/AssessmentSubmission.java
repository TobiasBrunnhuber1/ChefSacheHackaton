package com.managementstyle.recommender.system.Questionaire;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// JPA-Entität für eine einzelne ausgefüllte Erhebung (Führungskraft oder Mitarbeitende)
@Entity
@Table(name = "assessment_submissions")
public class AssessmentSubmission {

    // Automatisch vergebene Datenbank-ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Normalisierter Team-Code zur gemeinsamen Auswertung
    @Column(nullable = false, length = 80)
    private String teamCode;

    // Name der Erhebungsrunde (z. B. "Runde 1")
    @Column(nullable = false, length = 80)
    private String roundName;

    // Sortierindex der Runde je Team für korrekte Zeitreihen
    @Column(nullable = false)
    private int roundOrder;

    // Rolle der ausfüllenden Person (LEADER oder EMPLOYEE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentRole role;

    // Zeitstempel der Abgabe
    @Column(nullable = false)
    private Instant submittedAt;

    // Berechnete Mittelwerte der fünf Bereiche sowie der Gesamtwert
    private double egoismScore;
    private double pressureScore;
    private double underminingScore;
    private double takingCreditScore;
    private double manipulationScore;
    private double totalScore;

    // Rohantworten (15 Werte von 1 bis 5) in Fragenreihenfolge
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "submission_answers", joinColumns = @JoinColumn(name = "submission_id"))
    @OrderColumn(name = "question_index")
    @Column(name = "answer_value", nullable = false)
    private List<Integer> answers = new ArrayList<>();

    // Ausgewählte Situations-Tags aus dem Fragebogen
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "submission_situation_tags", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "tag_value", length = 100)
    private List<String> situationTags = new ArrayList<>();

    // Optionale Zuordnung der geschilderten Situation zu einem Bereich
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private Category situationCategory;

    // Anonymisierter Freitext zu konkreten Situationen/Verhaltensmustern
    @Column(length = 2000)
    private String situationNote;

    // Anonymisierter Freitext aus dem Denkanstoß-Feld
    @Column(length = 2000)
    private String reflectionNote;

    // Legt fest, ob die Denkanstoß-Notiz im Team-Vergleich angezeigt werden darf
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean shareReflectionWithTeam = true;

    // Standardkonstruktor für JPA
    public AssessmentSubmission() {
    }

    // Getter und Setter für Persistenz und Controller-Zugriff
    public Long getId() {
        return id;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public int getRoundOrder() {
        return roundOrder;
    }

    public void setRoundOrder(int roundOrder) {
        this.roundOrder = roundOrder;
    }

    public AssessmentRole getRole() {
        return role;
    }

    public void setRole(AssessmentRole role) {
        this.role = role;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public double getEgoismScore() {
        return egoismScore;
    }

    public double getPressureScore() {
        return pressureScore;
    }

    public double getUnderminingScore() {
        return underminingScore;
    }

    public double getTakingCreditScore() {
        return takingCreditScore;
    }

    public double getManipulationScore() {
        return manipulationScore;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public List<Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Integer> answers) {
        this.answers = answers != null ? new ArrayList<>(answers) : new ArrayList<>();
    }

    public List<String> getSituationTags() {
        return situationTags;
    }

    public void setSituationTags(List<String> situationTags) {
        this.situationTags = situationTags != null ? new ArrayList<>(situationTags) : new ArrayList<>();
    }

    public Category getSituationCategory() {
        return situationCategory;
    }

    public void setSituationCategory(Category situationCategory) {
        this.situationCategory = situationCategory;
    }

    public String getSituationNote() {
        return situationNote;
    }

    public void setSituationNote(String situationNote) {
        this.situationNote = situationNote;
    }

    public String getReflectionNote() {
        return reflectionNote;
    }

    public void setReflectionNote(String reflectionNote) {
        this.reflectionNote = reflectionNote;
    }

    public boolean isShareReflectionWithTeam() {
        return shareReflectionWithTeam;
    }

    public void setShareReflectionWithTeam(boolean shareReflectionWithTeam) {
        this.shareReflectionWithTeam = shareReflectionWithTeam;
    }

    // Wandelt die gespeicherten Einzelwerte in eine geordnete EnumMap um
    public Map<Category, Double> toScoreMap() {
        Map<Category, Double> map = new EnumMap<>(Category.class);
        map.put(Category.EGOISM, egoismScore);
        map.put(Category.PRESSURE, pressureScore);
        map.put(Category.UNDERMINING_DEVELOPMENT, underminingScore);
        map.put(Category.TAKING_CREDIT, takingCreditScore);
        map.put(Category.MANIPULATION, manipulationScore);
        return map;
    }

    // Liefert den gespeicherten Mittelwert für eine konkrete Kategorie
    public double scoreFor(Category category) {
        return switch (category) {
            case EGOISM -> egoismScore;
            case PRESSURE -> pressureScore;
            case UNDERMINING_DEVELOPMENT -> underminingScore;
            case TAKING_CREDIT -> takingCreditScore;
            case MANIPULATION -> manipulationScore;
        };
    }

    // Übernimmt alle berechneten Kategorie- und Gesamtscores in die Entität
    public void applyScores(Map<Category, Double> scores, double totalScore) {
        this.egoismScore = scores.getOrDefault(Category.EGOISM, 0.0);
        this.pressureScore = scores.getOrDefault(Category.PRESSURE, 0.0);
        this.underminingScore = scores.getOrDefault(Category.UNDERMINING_DEVELOPMENT, 0.0);
        this.takingCreditScore = scores.getOrDefault(Category.TAKING_CREDIT, 0.0);
        this.manipulationScore = scores.getOrDefault(Category.MANIPULATION, 0.0);
        this.totalScore = totalScore;
    }
}
