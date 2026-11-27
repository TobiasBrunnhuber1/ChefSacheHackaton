package com.managementstyle.recommender.system.Questionaire;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JPA-Repository für teambezogene Maßnahmen-Checklisten
public interface TeamActionItemRepository extends JpaRepository<TeamActionItem, Long> {

    List<TeamActionItem> findByTeamCodeOrderByCompletedAscCreatedAtDesc(String teamCode);

    void deleteByTeamCode(String teamCode);
}
