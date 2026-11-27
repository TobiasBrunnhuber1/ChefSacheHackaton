package com.managementstyle.recommender.system.Questionaire;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Konfigurations-Entität je Team-Code (z. B. Anonymitätsschwelle für die Team-Auswertung)
@Entity
@Table(name = "team_settings")
public class TeamSettings {

    @Id
    @Column(length = 80)
    private String teamCode;

    // Wenn aktiviert, werden Detailwerte im Team-Vergleich erst ab 3 Mitarbeiter-Abgaben freigegeben
    @Column(nullable = false)
    private boolean anonymityProtected;

    public TeamSettings() {
    }

    public TeamSettings(String teamCode, boolean anonymityProtected) {
        this.teamCode = teamCode;
        this.anonymityProtected = anonymityProtected;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public boolean isAnonymityProtected() {
        return anonymityProtected;
    }

    public void setAnonymityProtected(boolean anonymityProtected) {
        this.anonymityProtected = anonymityProtected;
    }
}
