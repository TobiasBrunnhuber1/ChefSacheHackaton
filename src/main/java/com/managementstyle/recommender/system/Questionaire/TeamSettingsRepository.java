package com.managementstyle.recommender.system.Questionaire;

import org.springframework.data.jpa.repository.JpaRepository;

// JPA-Repository für teambezogene Anonymitäts- und Erhebungseinstellungen
public interface TeamSettingsRepository extends JpaRepository<TeamSettings, String> {
}
