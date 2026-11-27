package com.managementstyle.recommender.system.Questionaire;

import java.time.Instant;
import java.util.List;

// Aufbereitetes Auswertungsmodell für den Team-Vergleich (Selbst- vs. Fremdbild) und Zeitverlauf
public record ComparisonReport(
        String teamCode,
        String selectedRound,
        String previousRound,
        List<String> availableRounds,
        int employeeCount,
        boolean hasLeaderSubmission,
        boolean anonymityProtected,
        boolean anonymityLocked,
        Double leaderTotalScore,
        Double employeeTotalScore,
        Double totalBlindSpot,
        Double previousEmployeeTotalScore,
        Double totalTrendDelta,
        TrendDirection totalTrend,
        ScoreSeverity overallSeverity,
        List<CategoryComparison> categories,
        List<QuestionHotspot> questionHotspots,
        List<CategoryTimelineRow> categoryTimeline,
        List<TeamActionItem> actionItems,
        List<TagCount> situationTagCounts,
        List<FeedbackNote> generalSituationNotes,
        List<FeedbackNote> reflectionNotes,
        List<RoundPoint> timeline,
        String thinkingPromptHeadline,
        String thinkingPromptText
) {
    // Trend-Richtung zwischen Vorrunde und aktueller Runde (niedrigere Werte = Besserung)
    public enum TrendDirection {
        IMPROVING("Bessert sich", "trend-improving", "↓"),
        STABLE("Unverändert", "trend-stable", "→"),
        WORSENING("Verschlechtert sich", "trend-worsening", "↑"),
        NO_BASELINE("Noch kein Vorwert", "trend-none", "·");

        private final String label;
        private final String cssClass;
        private final String arrow;

        TrendDirection(String label, String cssClass, String arrow) {
            this.label = label;
            this.cssClass = cssClass;
            this.arrow = arrow;
        }

        // Liefert das deutsche Trend-Label
        public String getLabel() {
            return label;
        }

        // Liefert die CSS-Klasse für das Trend-Badge
        public String getCssClass() {
            return cssClass;
        }

        // Liefert den Richtungspfeil
        public String getArrow() {
            return arrow;
        }
    }

    // Vergleichsdaten für eine einzelne Führungsdimension
    public record CategoryComparison(
            Category category,
            Double leaderScore,
            Double employeeScore,
            Double employeeMin,
            Double employeeMax,
            Double blindSpotDelta,
            Double previousEmployeeScore,
            Double trendDelta,
            TrendDirection trend,
            ScoreSeverity severity,
            CategoryGuidance guidance,
            List<FeedbackNote> categoryNotes
    ) {
        // Bevorzugt den Team-Wert für die Ampel-Bewertung und fällt sonst auf die Führungskraft zurück
        public double primaryScore() {
            if (employeeScore != null) {
                return employeeScore;
            }
            return leaderScore != null ? leaderScore : 1.0;
        }

        // Prozentuale Marker-Position (0 bis 100 %) der Führungskraft auf der 1–5-Skala
        public double leaderPositionPercent() {
            return leaderScore == null ? 0.0 : (leaderScore - 1.0) * 25.0;
        }

        // Prozentuale Marker-Position (0 bis 100 %) des Team-Mittelwerts auf der 1–5-Skala
        public double employeePositionPercent() {
            return employeeScore == null ? 0.0 : (employeeScore - 1.0) * 25.0;
        }

        // Startposition (in %) der Mitarbeiter-Spannweite (Min bis Max)
        public double employeeRangeStartPercent() {
            return employeeMin == null ? 0.0 : (employeeMin - 1.0) * 25.0;
        }

        // Breite (in %) der Mitarbeiter-Spannweite (Min bis Max)
        public double employeeRangeWidthPercent() {
            if (employeeMin == null || employeeMax == null) {
                return 0.0;
            }
            return Math.max(0.0, (employeeMax - employeeMin) * 25.0);
        }

        // Startposition für die visuelle Markierung der Wahrnehmungslücke
        public double gapStartPercent() {
            if (leaderScore == null || employeeScore == null) {
                return 0.0;
            }
            return Math.min(leaderPositionPercent(), employeePositionPercent());
        }

        // Breite der Wahrnehmungslücke in Prozent der Skala
        public double gapWidthPercent() {
            if (leaderScore == null || employeeScore == null) {
                return 0.0;
            }
            return Math.abs(employeePositionPercent() - leaderPositionPercent());
        }
    }

    // Einzelfragen-Hotspot mit dem größten Unterschied bzw. höchsten Wert
    public record QuestionHotspot(
            Question question,
            Double leaderAnswer,
            Double employeeMean,
            Double blindSpotDelta,
            ScoreSeverity severity
    ) {
    }

    // Verlaufszeile einer einzelnen Kategorie über alle Runden hinweg
    public record CategoryTimelineRow(
            Category category,
            List<Double> roundEmployeeScores,
            Double overallDelta,
            TrendDirection trend
    ) {
    }

    // Zählung eines gemeldeten Situations-Tags in einer Runde
    public record TagCount(String tag, long count) {
    }

    // Anonymisierter Freitext-Beitrag (Situation oder Denkanstoß)
    public record FeedbackNote(
            AssessmentRole role,
            Category category,
            List<String> tags,
            String text,
            Instant submittedAt
    ) {
    }

    // Datenpunkt für eine Runde inklusive vorberechneter SVG-Koordinaten
    public record RoundPoint(
            String roundName,
            int roundOrder,
            Double leaderTotal,
            Double employeeTotal,
            int employeeCount,
            Double blindSpot,
            double xCoord,
            Double leaderYCoord,
            Double employeeYCoord
    ) {
    }

    // Kurzübersicht eines Teams für Startseite und Auswahlmenüs
    public record TeamOverview(
            String teamCode,
            long totalSubmissions,
            long roundCount,
            String latestRoundName,
            Double latestEmployeeScore,
            boolean anonymityProtected
    ) {
    }
}
