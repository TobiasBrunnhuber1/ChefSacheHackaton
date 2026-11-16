package com.managementstyle.recommender.system.Questionaire;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

// Geschäftslogik für Bewertung, Datenbank-Speicherung, Gegenüberstellung und Zeitvergleiche
@Service
public class AssessmentService {

    // Standard-Team-Code, falls kein Code im Formular angegeben wurde
    public static final String DEFAULT_TEAM_CODE = "mein-team";
    // Standard-Rundenname für die erste Erhebung
    public static final String DEFAULT_ROUND_NAME = "Runde 1";
    // Fester Team-Code für das integrierte Vorher/Nachher-Demo-Szenario
    public static final String DEMO_TEAM_CODE = "demo-team";
    // Schwelle ab 0,20 Punkten Differenz, ab der ein echter Trend ausgewiesen wird
    private static final double TREND_EPSILON = 0.20;

    private final QuestionRepository questionRepository;
    private final AssessmentSubmissionRepository submissionRepository;

    public AssessmentService(
            QuestionRepository questionRepository,
            AssessmentSubmissionRepository submissionRepository
    ) {
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
    }

    // Ergebnis-Record für die validierten Mittelwerte je Kategorie und insgesamt
    public record ScoredAnswers(Map<Category, Double> categoryScores, double totalScore) {
    }

    // Validiert alle 15 Antworten (1 bis 5) und berechnet die Mittelwerte je Dimension
    public ScoredAnswers evaluateAnswers(List<Integer> answers) {
        List<Question> questions = questionRepository.findAll();
        if (answers == null || answers.size() != questions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bitte alle 15 Fragen beantworten.");
        }
        Map<Category, Double> sums = new EnumMap<>(Category.class);
        Map<Category, Integer> counts = new EnumMap<>(Category.class);
        double total = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer answer = answers.get(i);
            if (answer == null || answer < 1 || answer > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Bitte für jede Frage einen Wert von 1 bis 5 wählen.");
            }
            Category category = questions.get(i).getCategory();
            sums.merge(category, answer.doubleValue(), Double::sum);
            counts.merge(category, 1, Integer::sum);
            total += answer;
        }
        Map<Category, Double> scores = new EnumMap<>(Category.class);
        sums.forEach((category, sum) -> scores.put(category, sum / counts.get(category)));
        return new ScoredAnswers(scores, total / questions.size());
    }

    // Wertet den Fragebogen aus und speichert die Abgabe dauerhaft in der Datenbank
    @Transactional
    public AssessmentSubmission saveSubmission(QuizAnswers submission) {
        ScoredAnswers evaluated = evaluateAnswers(submission.getAnswers());
        String teamCode = normalizeTeamCode(submission.getTeamCode());
        String roundName = normalizeRoundName(submission.getRoundName());
        AssessmentRole role = submission.getRole() != null ? submission.getRole() : AssessmentRole.EMPLOYEE;

        // Vorhandene Rundennummer wiederverwenden oder neue laufende Nummer vergeben
        List<AssessmentSubmission> existingForTeam =
                submissionRepository.findByTeamCodeOrderByRoundOrderAscSubmittedAtAsc(teamCode);
        int roundOrder = resolveRoundOrder(existingForTeam, roundName);

        AssessmentSubmission entity = new AssessmentSubmission();
        entity.setTeamCode(teamCode);
        entity.setRoundName(roundName);
        entity.setRoundOrder(roundOrder);
        entity.setRole(role);
        entity.setSubmittedAt(Instant.now());
        entity.applyScores(evaluated.categoryScores(), evaluated.totalScore());
        entity.setAnswers(submission.getAnswers());
        entity.setSituationTags(cleanTags(submission.getSituationTags()));
        entity.setSituationCategory(submission.getSituationCategory());
        entity.setSituationNote(cleanText(submission.getSituationNote()));
        entity.setReflectionNote(cleanText(submission.getReflectionNote()));

        return submissionRepository.save(entity);
    }

    // Erstellt eine Übersicht aller in der Datenbank gespeicherten Teams
    @Transactional(readOnly = true)
    public List<ComparisonReport.TeamOverview> listTeamOverviews() {
        List<AssessmentSubmission> all = submissionRepository.findAllByOrderBySubmittedAtDesc();
        Map<String, List<AssessmentSubmission>> byTeam = new LinkedHashMap<>();
        for (AssessmentSubmission sub : all) {
            byTeam.computeIfAbsent(sub.getTeamCode(), k -> new ArrayList<>()).add(sub);
        }

        List<ComparisonReport.TeamOverview> overviews = new ArrayList<>();
        for (Map.Entry<String, List<AssessmentSubmission>> entry : byTeam.entrySet()) {
            List<AssessmentSubmission> subs = entry.getValue();
            int maxOrder = subs.stream().mapToInt(AssessmentSubmission::getRoundOrder).max().orElse(1);
            String latestRound = subs.stream()
                    .filter(s -> s.getRoundOrder() == maxOrder)
                    .map(AssessmentSubmission::getRoundName)
                    .findFirst()
                    .orElse(DEFAULT_ROUND_NAME);
            long roundCount = subs.stream().map(AssessmentSubmission::getRoundName).distinct().count();
            Double latestEmployeeAvg = averageTotal(subs.stream()
                    .filter(s -> s.getRoundOrder() == maxOrder && s.getRole() == AssessmentRole.EMPLOYEE)
                    .toList());
            overviews.add(new ComparisonReport.TeamOverview(
                    entry.getKey(),
                    subs.size(),
                    roundCount,
                    latestRound,
                    latestEmployeeAvg
            ));
        }
        return overviews;
    }

    // Baut den vollständigen Vergleichsbericht (Selbst- vs. Fremdbild und Entwicklung zur Vorrunde)
    @Transactional(readOnly = true)
    public Optional<ComparisonReport> buildComparisonReport(String rawTeamCode, String rawRoundName) {
        String teamCode = normalizeTeamCode(rawTeamCode);
        List<AssessmentSubmission> allForTeam =
                submissionRepository.findByTeamCodeOrderByRoundOrderAscSubmittedAtAsc(teamCode);
        if (allForTeam.isEmpty()) {
            return Optional.empty();
        }

        // Alle Abgaben je Runde unter Beibehaltung der Rundenreihenfolge gruppieren
        Map<String, List<AssessmentSubmission>> roundsMap = new LinkedHashMap<>();
        for (AssessmentSubmission sub : allForTeam) {
            roundsMap.computeIfAbsent(sub.getRoundName(), k -> new ArrayList<>()).add(sub);
        }
        List<String> availableRounds = new ArrayList<>(roundsMap.keySet());
        String selectedRound = (rawRoundName != null && roundsMap.containsKey(rawRoundName.trim()))
                ? rawRoundName.trim()
                : availableRounds.get(availableRounds.size() - 1);

        int selectedIdx = availableRounds.indexOf(selectedRound);
        String previousRound = selectedIdx > 0 ? availableRounds.get(selectedIdx - 1) : null;

        List<AssessmentSubmission> currentSubs = roundsMap.get(selectedRound);
        List<AssessmentSubmission> previousSubs = previousRound != null ? roundsMap.get(previousRound) : List.of();

        Optional<AssessmentSubmission> latestLeader = latestLeaderIn(currentSubs);
        List<AssessmentSubmission> currentEmployees = employeesIn(currentSubs);
        List<AssessmentSubmission> previousEmployees = employeesIn(previousSubs);

        Double leaderTotal = latestLeader.map(AssessmentSubmission::getTotalScore).orElse(null);
        Double employeeTotal = averageTotal(currentEmployees);
        Double previousEmployeeTotal = averageTotal(previousEmployees);
        Double totalBlindSpot = (leaderTotal != null && employeeTotal != null) ? employeeTotal - leaderTotal : null;
        Double totalTrendDelta = (employeeTotal != null && previousEmployeeTotal != null)
                ? employeeTotal - previousEmployeeTotal
                : null;
        ComparisonReport.TrendDirection totalTrend = classifyTrend(totalTrendDelta);

        double overallForSeverity = employeeTotal != null
                ? employeeTotal
                : (leaderTotal != null ? leaderTotal : 1.0);
        ScoreSeverity overallSeverity = ScoreSeverity.fromScore(overallForSeverity);

        List<ComparisonReport.CategoryComparison> categories = buildCategoryComparisons(
                latestLeader.orElse(null),
                currentEmployees,
                previousEmployees,
                currentSubs
        );

        List<ComparisonReport.TagCount> tagCounts = countTags(currentSubs);
        List<ComparisonReport.FeedbackNote> generalSituationNotes = currentSubs.stream()
                .filter(s -> s.getSituationCategory() == null && hasText(s.getSituationNote()))
                .map(s -> new ComparisonReport.FeedbackNote(
                        s.getRole(), null, s.getSituationTags(), s.getSituationNote(), s.getSubmittedAt()))
                .toList();
        List<ComparisonReport.FeedbackNote> reflectionNotes = currentSubs.stream()
                .filter(s -> hasText(s.getReflectionNote()))
                .map(s -> new ComparisonReport.FeedbackNote(
                        s.getRole(), s.getSituationCategory(), s.getSituationTags(), s.getReflectionNote(), s.getSubmittedAt()))
                .toList();

        List<ComparisonReport.RoundPoint> timeline = buildTimeline(roundsMap);
        PromptPair prompt = buildDynamicThinkingPrompt(categories, employeeTotal, totalTrend);

        return Optional.of(new ComparisonReport(
                teamCode,
                selectedRound,
                previousRound,
                availableRounds,
                currentEmployees.size(),
                latestLeader.isPresent(),
                leaderTotal,
                employeeTotal,
                totalBlindSpot,
                previousEmployeeTotal,
                totalTrendDelta,
                totalTrend,
                overallSeverity,
                categories,
                tagCounts,
                generalSituationNotes,
                reflectionNotes,
                timeline,
                prompt.headline(),
                prompt.text()
        ));
    }

    // Legt ein realistisches 2-Runden-Demo-Szenario an, um Vergleich und Besserung direkt zeigen zu können
    @Transactional
    public String seedDemoData() {
        submissionRepository.deleteByTeamCode(DEMO_TEAM_CODE);
        Instant baseTime = Instant.now().minus(28, ChronoUnit.DAYS);

        // Runde 1: Führungskraft schätzt sich niedrig ein, Team meldet erhöhten Druck und Vereinnahmung
        saveDemoEntry(DEMO_TEAM_CODE, "Runde 1 (Start)", 1, AssessmentRole.LEADER, baseTime,
                List.of(2, 2, 2,  2, 2, 1,  2, 1, 2,  1, 1, 2,  1, 1, 1),
                List.of("Deadline- & Zeitdruck"),
                Category.PRESSURE,
                "Vor dem Quartalsabschluss musste alles sehr schnell gehen, da habe ich Aufgaben eng gesteuert.",
                "Vielleicht habe ich im Stress zu wenig gefragt, wie voll die Schreibtische im Team wirklich sind.");

        saveDemoEntry(DEMO_TEAM_CODE, "Runde 1 (Start)", 1, AssessmentRole.EMPLOYEE, baseTime.plus(1, ChronoUnit.HOURS),
                List.of(3, 3, 4,  4, 5, 4,  3, 3, 3,  4, 4, 4,  2, 2, 2),
                List.of("Deadline- & Zeitdruck", "Präsentation vor Management"),
                Category.TAKING_CREDIT,
                "Unser Konzept wurde im Management-Board präsentiert, ohne dass erwähnt wurde, wer die Ausarbeitung gemacht hat.",
                "Ich wünsche mir, dass Erfolge wieder sichtbar dem ganzen Team zugerechnet werden.");

        saveDemoEntry(DEMO_TEAM_CODE, "Runde 1 (Start)", 1, AssessmentRole.EMPLOYEE, baseTime.plus(2, ChronoUnit.HOURS),
                List.of(3, 4, 3,  4, 4, 5,  4, 3, 3,  3, 4, 3,  2, 3, 2),
                List.of("Deadline- & Zeitdruck", "Aufgabenverteilung & Delegation"),
                Category.PRESSURE,
                "Freitagnachmittags kommen häufig spontane Zusatzaufgaben mit Deadline Montagmorgen, ohne dass Bestehendes verschoben wird.",
                "Ohne klare Prioritätenliste weiß man nicht mehr, welche Aufgabe überhaupt machbar ist.");

        saveDemoEntry(DEMO_TEAM_CODE, "Runde 1 (Start)", 1, AssessmentRole.EMPLOYEE, baseTime.plus(3, ChronoUnit.HOURS),
                List.of(4, 3, 4,  4, 4, 4,  3, 4, 4,  4, 4, 4,  3, 2, 2),
                List.of("Aufgabenverteilung & Delegation", "1:1- oder Feedbackgespräch"),
                Category.UNDERMINING_DEVELOPMENT,
                "Spannende Kundentermine übernimmt die Führungskraft selbst, Routine-Pflege bleibt dauerhaft bei denselben Mitarbeitenden.",
                "Ich frage mich, ob meine Weiterentwicklung im Team überhaupt mitgedacht wird.");

        // Runde 2: Nach konkreten Maßnahmen sinken die Team-Werte deutlich
        Instant round2Time = Instant.now().minus(2, ChronoUnit.DAYS);
        saveDemoEntry(DEMO_TEAM_CODE, "Runde 2 (Follow-up)", 2, AssessmentRole.LEADER, round2Time,
                List.of(2, 2, 2,  2, 2, 2,  2, 2, 2,  2, 1, 2,  1, 1, 1),
                List.of("Aufgabenverteilung & Delegation"),
                Category.PRESSURE,
                "Wir haben im Weekly ein gemeinsames Kapazitätsboard eingeführt und präsentieren jetzt gemeinsam im Management-Review.",
                "Das erste Feedback hat mir erst gezeigt, wie anders mein Tempo im Team ankam.");

        saveDemoEntry(DEMO_TEAM_CODE, "Runde 2 (Follow-up)", 2, AssessmentRole.EMPLOYEE, round2Time.plus(1, ChronoUnit.HOURS),
                List.of(2, 2, 2,  2, 3, 2,  2, 2, 2,  2, 2, 2,  1, 1, 1),
                List.of("Präsentation vor Management"),
                Category.TAKING_CREDIT,
                "Beim letzten Review durften zwei Teammitglieder ihre Ergebnisse selbst vorstellen – das hat spürbar motiviert.",
                "Man merkt, dass Feedback ernst genommen wurde.");

        saveDemoEntry(DEMO_TEAM_CODE, "Runde 2 (Follow-up)", 2, AssessmentRole.EMPLOYEE, round2Time.plus(2, ChronoUnit.HOURS),
                List.of(2, 2, 2,  3, 2, 2,  2, 2, 2,  2, 2, 2,  1, 2, 1),
                List.of("Deadline- & Zeitdruck", "Aufgabenverteilung & Delegation"),
                Category.PRESSURE,
                "Neue Aufgaben werden jetzt zusammen mit bestehenden Aufgaben im Board priorisiert.",
                "Einzelne Spitzen gibt es noch vor Releases, aber die Kommunikation ist viel fairer geworden.");

        saveDemoEntry(DEMO_TEAM_CODE, "Runde 2 (Follow-up)", 2, AssessmentRole.EMPLOYEE, round2Time.plus(3, ChronoUnit.HOURS),
                List.of(2, 2, 2,  2, 2, 3,  2, 2, 2,  2, 2, 1,  1, 1, 1),
                List.of("1:1- oder Feedbackgespräch"),
                Category.UNDERMINING_DEVELOPMENT,
                "Im 1:1 wurde erstmals ein konkretes Lernprojekt für das nächste Quartal vereinbart.",
                "Guter Schritt in die richtige Richtung – dranbleiben!");

        return DEMO_TEAM_CODE;
    }

    // Hilfsmethode zum Speichern eines einzelnen Demo-Datensatzes mit festem Zeitstempel
    private void saveDemoEntry(
            String teamCode,
            String roundName,
            int roundOrder,
            AssessmentRole role,
            Instant submittedAt,
            List<Integer> answers,
            List<String> tags,
            Category situationCategory,
            String situationNote,
            String reflectionNote
    ) {
        ScoredAnswers evaluated = evaluateAnswers(answers);
        AssessmentSubmission entity = new AssessmentSubmission();
        entity.setTeamCode(teamCode);
        entity.setRoundName(roundName);
        entity.setRoundOrder(roundOrder);
        entity.setRole(role);
        entity.setSubmittedAt(submittedAt);
        entity.applyScores(evaluated.categoryScores(), evaluated.totalScore());
        entity.setAnswers(answers);
        entity.setSituationTags(tags);
        entity.setSituationCategory(situationCategory);
        entity.setSituationNote(situationNote);
        entity.setReflectionNote(reflectionNote);
        submissionRepository.save(entity);
    }

    // Erstellt den Detailvergleich für alle fünf Führungsdimensionen
    private List<ComparisonReport.CategoryComparison> buildCategoryComparisons(
            AssessmentSubmission leader,
            List<AssessmentSubmission> currentEmployees,
            List<AssessmentSubmission> previousEmployees,
            List<AssessmentSubmission> currentAll
    ) {
        List<ComparisonReport.CategoryComparison> result = new ArrayList<>();
        for (Category category : Category.values()) {
            Double leaderScore = leader != null ? leader.scoreFor(category) : null;
            Double employeeScore = averageCategory(currentEmployees, category);
            Double employeeMin = currentEmployees.isEmpty() ? null
                    : currentEmployees.stream().mapToDouble(s -> s.scoreFor(category)).min().orElse(1.0);
            Double employeeMax = currentEmployees.isEmpty() ? null
                    : currentEmployees.stream().mapToDouble(s -> s.scoreFor(category)).max().orElse(1.0);
            Double blindSpot = (leaderScore != null && employeeScore != null) ? employeeScore - leaderScore : null;
            Double prevEmployeeScore = averageCategory(previousEmployees, category);
            Double trendDelta = (employeeScore != null && prevEmployeeScore != null)
                    ? employeeScore - prevEmployeeScore
                    : null;
            ComparisonReport.TrendDirection trend = classifyTrend(trendDelta);

            double scoreForSeverity = employeeScore != null
                    ? employeeScore
                    : (leaderScore != null ? leaderScore : 1.0);
            ScoreSeverity severity = ScoreSeverity.fromScore(scoreForSeverity);

            List<ComparisonReport.FeedbackNote> notes = currentAll.stream()
                    .filter(s -> s.getSituationCategory() == category && hasText(s.getSituationNote()))
                    .map(s -> new ComparisonReport.FeedbackNote(
                            s.getRole(), category, s.getSituationTags(), s.getSituationNote(), s.getSubmittedAt()))
                    .toList();

            result.add(new ComparisonReport.CategoryComparison(
                    category,
                    leaderScore,
                    employeeScore,
                    employeeMin,
                    employeeMax,
                    blindSpot,
                    prevEmployeeScore,
                    trendDelta,
                    trend,
                    severity,
                    CategoryGuidance.forCategory(category),
                    notes
            ));
        }
        return result;
    }

    // Berechnet die Datenpunkte und SVG-Koordinaten für das Runden-Verlaufsdiagramm
    private List<ComparisonReport.RoundPoint> buildTimeline(Map<String, List<AssessmentSubmission>> roundsMap) {
        List<Map.Entry<String, List<AssessmentSubmission>>> entries = new ArrayList<>(roundsMap.entrySet());
        int count = entries.size();
        List<ComparisonReport.RoundPoint> points = new ArrayList<>();
        double chartLeft = 56.0;
        double chartWidth = 488.0;

        for (int i = 0; i < count; i++) {
            String roundName = entries.get(i).getKey();
            List<AssessmentSubmission> subs = entries.get(i).getValue();
            int order = subs.get(0).getRoundOrder();
            Double leaderTotal = latestLeaderIn(subs).map(AssessmentSubmission::getTotalScore).orElse(null);
            List<AssessmentSubmission> employees = employeesIn(subs);
            Double employeeTotal = averageTotal(employees);
            Double blindSpot = (leaderTotal != null && employeeTotal != null) ? employeeTotal - leaderTotal : null;

            double x = count == 1 ? chartLeft + chartWidth / 2.0 : chartLeft + (chartWidth * i) / (count - 1);
            points.add(new ComparisonReport.RoundPoint(
                    roundName,
                    order,
                    leaderTotal,
                    employeeTotal,
                    employees.size(),
                    blindSpot,
                    x,
                    scoreToSvgY(leaderTotal),
                    scoreToSvgY(employeeTotal)
            ));
        }
        return points;
    }

    // Rechnet einen Score (1 bis 5) in die vertikale SVG-Position (y=166 unten, y=22 oben) um
    private Double scoreToSvgY(Double score) {
        if (score == null) {
            return null;
        }
        return 166.0 - ((score - 1.0) / 4.0) * 144.0;
    }

    // Internes Hilfspaar für Überschrift und Text des dynamischen Denkanstoßes
    private record PromptPair(String headline, String text) {
    }

    // Wählt den passendsten Denkanstoß anhand der größten Wahrnehmungslücke oder des höchsten Scores
    private PromptPair buildDynamicThinkingPrompt(
            List<ComparisonReport.CategoryComparison> categories,
            Double employeeTotal,
            ComparisonReport.TrendDirection totalTrend
    ) {
        Optional<ComparisonReport.CategoryComparison> biggestGap = categories.stream()
                .filter(c -> c.blindSpotDelta() != null)
                .max(Comparator.comparingDouble(c -> Math.abs(c.blindSpotDelta())));

        if (biggestGap.isPresent() && Math.abs(biggestGap.get().blindSpotDelta()) >= 0.75) {
            ComparisonReport.CategoryComparison gap = biggestGap.get();
            String direction = gap.blindSpotDelta() > 0
                    ? "erlebt das Team spürbar stärker als Sie selbst"
                    : "schätzen Sie selbst strenger ein als Ihr Team";
            return new PromptPair(
                    "Denkanstoß zur größten Wahrnehmungslücke: " + gap.category().getLabel(),
                    String.format(Locale.GERMANY,
                            "Im Bereich „%s“ liegt der Unterschied zwischen Selbst- und Fremdbild bei %+.2f Punkten (%s). %s",
                            gap.category().getLabel(),
                            gap.blindSpotDelta(),
                            direction,
                            gap.guidance().thinkingPrompt())
            );
        }

        Optional<ComparisonReport.CategoryComparison> highestCategory = categories.stream()
                .max(Comparator.comparingDouble(ComparisonReport.CategoryComparison::primaryScore));

        if (highestCategory.isPresent() && highestCategory.get().primaryScore() > 2.0) {
            ComparisonReport.CategoryComparison top = highestCategory.get();
            return new PromptPair(
                    "Denkanstoß zum höchsten Bereich: " + top.category().getLabel(),
                    top.guidance().thinkingPrompt()
            );
        }

        if (totalTrend == ComparisonReport.TrendDirection.IMPROVING && employeeTotal != null) {
            return new PromptPair(
                    "Denkanstoß zur positiven Entwicklung",
                    "Die Werte verbessern sich im Vergleich zur Vorrunde. Welche konkrete Veränderung im Alltag hat diesen Unterschied aus Sicht des Teams bewirkt – und wie sichern Sie sie dauerhaft?"
            );
        }

        return new PromptPair(
                "Denkanstoß für Führung & Team",
                "Welche unscheinbare Routine aus dieser Woche könnte von anderen ganz anders erlebt worden sein, als Sie sie gemeint haben?"
        );
    }

    // Liefert die neueste Selbsteinschätzung der Führungskraft innerhalb einer Runde
    private Optional<AssessmentSubmission> latestLeaderIn(List<AssessmentSubmission> subs) {
        return subs.stream()
                .filter(s -> s.getRole() == AssessmentRole.LEADER)
                .reduce((first, second) -> second);
    }

    // Filtert alle Mitarbeiter-Abgaben einer Runde heraus
    private List<AssessmentSubmission> employeesIn(List<AssessmentSubmission> subs) {
        return subs.stream()
                .filter(s -> s.getRole() == AssessmentRole.EMPLOYEE)
                .toList();
    }

    // Berechnet den durchschnittlichen Gesamtwert einer Abgaben-Liste
    private Double averageTotal(List<AssessmentSubmission> subs) {
        if (subs.isEmpty()) {
            return null;
        }
        return subs.stream().mapToDouble(AssessmentSubmission::getTotalScore).average().orElse(0.0);
    }

    // Berechnet den Durchschnitt einer bestimmten Kategorie über eine Abgaben-Liste
    private Double averageCategory(List<AssessmentSubmission> subs, Category category) {
        if (subs.isEmpty()) {
            return null;
        }
        return subs.stream().mapToDouble(s -> s.scoreFor(category)).average().orElse(0.0);
    }

    // Klassifiziert das Delta zur Vorrunde (sinkende Scores bedeuten Besserung)
    private ComparisonReport.TrendDirection classifyTrend(Double delta) {
        if (delta == null) {
            return ComparisonReport.TrendDirection.NO_BASELINE;
        }
        if (delta <= -TREND_EPSILON) {
            return ComparisonReport.TrendDirection.IMPROVING;
        }
        if (delta >= TREND_EPSILON) {
            return ComparisonReport.TrendDirection.WORSENING;
        }
        return ComparisonReport.TrendDirection.STABLE;
    }

    // Zählt und sortiert alle gemeldeten Situations-Tags nach Häufigkeit
    private List<ComparisonReport.TagCount> countTags(List<AssessmentSubmission> subs) {
        Map<String, Long> counts = subs.stream()
                .flatMap(s -> s.getSituationTags().stream())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .map(e -> new ComparisonReport.TagCount(e.getKey(), e.getValue()))
                .toList();
    }

    // Bestimmt die konsistente Rundennummer je Team-Code
    private int resolveRoundOrder(List<AssessmentSubmission> existingForTeam, String roundName) {
        return existingForTeam.stream()
                .filter(s -> s.getRoundName().equalsIgnoreCase(roundName))
                .mapToInt(AssessmentSubmission::getRoundOrder)
                .findFirst()
                .orElseGet(() -> existingForTeam.stream()
                        .mapToInt(AssessmentSubmission::getRoundOrder)
                        .max()
                        .orElse(0) + 1);
    }

    // Normalisiert Team-Codes auf URL-sichere Kleinbuchstaben
    public String normalizeTeamCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_TEAM_CODE;
        }
        String cleaned = raw.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9äöüß_-]+", "-")
                .replaceAll("^-+|-+$", "");
        return cleaned.isEmpty() ? DEFAULT_TEAM_CODE : cleaned;
    }

    // Normalisiert Rundennamen mit Fallback auf Runde 1
    public String normalizeRoundName(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_ROUND_NAME;
        }
        return raw.trim();
    }

    // Bereinigt und dedupliziert ausgewählte Situations-Tags
    private List<String> cleanTags(List<String> rawTags) {
        if (rawTags == null) {
            return List.of();
        }
        return rawTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    // Trimmt Freitexteingaben und speichert leere Eingaben als null
    private String cleanText(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Prüft, ob ein Textfeld Inhalt besitzt
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
