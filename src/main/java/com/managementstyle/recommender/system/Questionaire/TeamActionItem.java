package com.managementstyle.recommender.system.Questionaire;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// Persistierte konkrete Maßnahme für die Team-Entwicklung zwischen Erhebungsrunden
@Entity
@Table(name = "team_action_items")
public class TeamActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String teamCode;

    @Column(nullable = false, length = 80)
    private String roundName;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private Category category;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private Instant createdAt;

    public TeamActionItem() {
    }

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
