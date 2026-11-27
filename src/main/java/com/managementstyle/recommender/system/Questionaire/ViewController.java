package com.managementstyle.recommender.system.Questionaire;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

// Zentraler Web-Controller für Start-Hub, Fragebogen, Einzelauswertung, QR-Codes und Team-Vergleich
@Controller
public class ViewController {

    private final QuestionRepository questionRepository;
    private final AssessmentService assessmentService;
    private final QrCodeService qrCodeService;

    public ViewController(
            QuestionRepository questionRepository,
            AssessmentService assessmentService,
            QrCodeService qrCodeService
    ) {
        this.questionRepository = questionRepository;
        this.assessmentService = assessmentService;
        this.qrCodeService = qrCodeService;
    }

    // Wrapper für die Anzeige im Fragebogen mit festem Original-Index trotz optionalem Mischen
    public record QuestionItem(int index, Question question) {
    }

    // Aufbereitetes Detail-Objekt für eine einzelne Kategorie auf der Ergebnisseite
    public record ResultCategoryDetail(
            Category category,
            double score,
            ScoreSeverity severity,
            CategoryGuidance guidance
    ) {
        // Rechnet den Score (1 bis 5) in Prozentposition (0 bis 100 %) auf dem Balken um
        public double positionPercent() {
            return (score - 1.0) * 25.0;
        }
    }

    // Startseite mit Einladungslinks für Führungskraft und Mitarbeitende im lokalen Netzwerk
    @GetMapping("/")
    public String showHub(
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String round,
            HttpServletRequest request,
            Model model
    ) {
        String teamCode = assessmentService.normalizeTeamCode(team);
        String roundName = assessmentService.normalizeRoundName(round);
        String baseUrl = buildShareBaseUrl(request);

        String employeeLink = buildRoleQuizUrl(baseUrl, AssessmentRole.EMPLOYEE, teamCode, roundName);
        model.addAttribute("teamCode", teamCode);
        model.addAttribute("roundName", roundName);
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("leaderLink", buildRoleQuizUrl(baseUrl, AssessmentRole.LEADER, teamCode, roundName));
        model.addAttribute("employeeLink", employeeLink);
        model.addAttribute("employeeQrSrc", "/qr?text=" + encode(employeeLink));
        model.addAttribute("compareLink", buildCompareUrl(baseUrl, teamCode, roundName));
        model.addAttribute("anonymityProtected", assessmentService.isAnonymityProtected(teamCode));
        model.addAttribute("teamOverviews", assessmentService.listTeamOverviews());
        return "index";
    }

    // Liefert einen lokalen SVG-QR-Code für beliebige Einladungslinks
    @GetMapping(value = "/qr", produces = "image/svg+xml")
    @ResponseBody
    public ResponseEntity<String> renderQrCode(@RequestParam String text) {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(qrCodeService.generateSvg(text));
    }

    // Zeigt den deutschen Fragebogen passend zur gewählten Rolle, Team-ID und optional gemischt
    @GetMapping("/quiz")
    public String showQuiz(
            @RequestParam(required = false) AssessmentRole role,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String round,
            @RequestParam(defaultValue = "false") boolean shuffle,
            Model model
    ) {
        AssessmentRole activeRole = role != null ? role : AssessmentRole.EMPLOYEE;
        String teamCode = assessmentService.normalizeTeamCode(team);
        String roundName = assessmentService.normalizeRoundName(round);

        List<Question> allQuestions = questionRepository.findAll();
        List<QuestionItem> items = new ArrayList<>();
        for (int i = 0; i < allQuestions.size(); i++) {
            items.add(new QuestionItem(i, allQuestions.get(i)));
        }
        if (shuffle) {
            Collections.shuffle(items);
        }

        model.addAttribute("questions", allQuestions);
        model.addAttribute("questionItems", items);
        model.addAttribute("shuffled", shuffle);
        model.addAttribute("role", activeRole);
        model.addAttribute("teamCode", teamCode);
        model.addAttribute("roundName", roundName);
        model.addAttribute("categories", Category.values());
        model.addAttribute("predefinedTags", CategoryGuidance.PREDEFINED_SITUATION_TAGS);
        model.addAttribute("thinkingQuestion", thinkingQuestionFor(activeRole));
        return "quiz";
    }

    // Speichert den ausgefüllten Fragebogen in der DB und zeigt Auswertung samt Empfehlungen
    @PostMapping("/quiz/submit")
    public String processQuiz(@ModelAttribute QuizAnswers submission, Model model) {
        AssessmentSubmission saved = assessmentService.saveSubmission(submission);
        Map<Category, Double> scores = saved.toScoreMap();

        List<ResultCategoryDetail> details = new ArrayList<>();
        for (Category category : Category.values()) {
            double score = scores.get(category);
            details.add(new ResultCategoryDetail(
                    category,
                    score,
                    ScoreSeverity.fromScore(score),
                    CategoryGuidance.forCategory(category)
            ));
        }

        ResultCategoryDetail topCategory = details.stream()
                .max(Comparator.comparingDouble(ResultCategoryDetail::score))
                .orElse(details.get(0));

        model.addAttribute("scores", scores);
        model.addAttribute("totalScore", saved.getTotalScore());
        model.addAttribute("overallSeverity", ScoreSeverity.fromScore(saved.getTotalScore()));
        model.addAttribute("categoryDetails", details);
        model.addAttribute("submission", saved);
        model.addAttribute("topGuidance", topCategory.guidance());
        model.addAttribute("topCategory", topCategory.category());
        return "result";
    }

    // Zeigt die Gegenüberstellung Führungskraft vs. Team sowie die Entwicklung über mehrere Runden
    @GetMapping("/compare")
    public String showComparison(
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String round,
            @RequestParam(defaultValue = "false") boolean reveal,
            Model model
    ) {
        List<ComparisonReport.TeamOverview> overviews = assessmentService.listTeamOverviews();
        String targetTeam = (team != null && !team.isBlank())
                ? assessmentService.normalizeTeamCode(team)
                : (overviews.isEmpty() ? AssessmentService.DEFAULT_TEAM_CODE : overviews.get(0).teamCode());

        model.addAttribute("teamOverviews", overviews);
        model.addAttribute("selectedTeam", targetTeam);
        model.addAttribute("categories", Category.values());
        model.addAttribute("revealOverride", reveal);
        model.addAttribute("report", assessmentService.buildComparisonReport(targetTeam, round, reveal).orElse(null));
        return "compare";
    }

    // Speichert eine konkrete Maßnahme in der Team-Checkliste
    @PostMapping("/compare/actions")
    public String addActionItem(
            @RequestParam String team,
            @RequestParam(required = false) String round,
            @RequestParam(required = false) Category category,
            @RequestParam String description,
            @RequestParam(defaultValue = "false") boolean reveal,
            RedirectAttributes redirectAttributes
    ) {
        assessmentService.addActionItem(team, round, category, description);
        return redirectToCompare(assessmentService.normalizeTeamCode(team), round, reveal, redirectAttributes);
    }

    // Schaltet eine Maßnahme zwischen offen und erledigt um
    @PostMapping("/compare/actions/{id}/toggle")
    public String toggleActionItem(
            @PathVariable Long id,
            @RequestParam(required = false) String round,
            @RequestParam(defaultValue = "false") boolean reveal,
            RedirectAttributes redirectAttributes
    ) {
        String teamCode = assessmentService.toggleActionItem(id);
        return redirectToCompare(teamCode, round, reveal, redirectAttributes);
    }

    // Entfernt eine Maßnahme aus der Checkliste
    @PostMapping("/compare/actions/{id}/delete")
    public String deleteActionItem(
            @PathVariable Long id,
            @RequestParam(required = false) String round,
            @RequestParam(defaultValue = "false") boolean reveal,
            RedirectAttributes redirectAttributes
    ) {
        String teamCode = assessmentService.deleteActionItem(id);
        return redirectToCompare(teamCode, round, reveal, redirectAttributes);
    }

    // Aktualisiert den Anonymitätsschutz (erst ab 3 Antworten freigeben) für ein Team
    @PostMapping("/teams/settings")
    public String updateTeamSettings(
            @RequestParam String team,
            @RequestParam(required = false) String round,
            @RequestParam(defaultValue = "false") boolean anonymityProtected,
            @RequestParam(defaultValue = "false") boolean reveal,
            @RequestParam(defaultValue = "hub") String returnTo,
            RedirectAttributes redirectAttributes
    ) {
        String teamCode = assessmentService.normalizeTeamCode(team);
        assessmentService.updateAnonymityProtection(teamCode, anonymityProtected);
        if ("compare".equals(returnTo)) {
            return redirectToCompare(teamCode, round, reveal, redirectAttributes);
        }
        redirectAttributes.addAttribute("team", teamCode);
        if (round != null && !round.isBlank()) {
            redirectAttributes.addAttribute("round", round.trim());
        }
        return "redirect:/";
    }

    // Baut eine konsistente Weiterleitung zur Team-Vergleichsansicht inklusive optionalem Reveal-Flag
    private String redirectToCompare(
            String teamCode,
            String round,
            boolean reveal,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addAttribute("team", teamCode);
        if (round != null && !round.isBlank()) {
            redirectAttributes.addAttribute("round", round.trim());
        }
        if (reveal) {
            redirectAttributes.addAttribute("reveal", "true");
        }
        return "redirect:/compare";
    }

    // Löscht alle gespeicherten Antworten und Maßnahmen eines Teams
    @PostMapping("/teams/delete")
    public String deleteTeamData(@RequestParam String team) {
        assessmentService.deleteTeamData(team);
        return "redirect:/";
    }

    // Befüllt die Datenbank per Knopfdruck mit einem zweistufigen Demo-Szenario
    @PostMapping("/demo/seed")
    public String seedDemoData(RedirectAttributes redirectAttributes) {
        String demoTeam = assessmentService.seedDemoData();
        redirectAttributes.addAttribute("team", demoTeam);
        return "redirect:/compare";
    }

    // Liefert eine rollenspezifische Leitfrage für das Denkanstoß-Feld
    private String thinkingQuestionFor(AssessmentRole role) {
        if (role == AssessmentRole.LEADER) {
            return "Welcher Moment der letzten Wochen fällt Ihnen spontan ein, in dem eigene Zielvorgaben oder Zeitdruck Ihr Verhalten gegenüber dem Team spürbar geprägt haben?";
        }
        return "Welche konkrete Situation aus den letzten Wochen fällt Ihnen spontan ein, die Ihr Erleben von Führung besonders geprägt hat?";
    }

    // Erzeugt die Basis-URL inklusive lokaler Netzwerk-IP für den Zugriff von anderen Geräten
    private String buildShareBaseUrl(HttpServletRequest request) {
        String host = resolveLanHost(request);
        int port = request.getServerPort();
        String scheme = request.getScheme();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        return defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
    }

    // Ermittelt nach Möglichkeit die lokale IPv4-Adresse im WLAN/LAN statt localhost
    private String resolveLanHost(HttpServletRequest request) {
        String serverName = request.getServerName();
        if (serverName != null && !"localhost".equalsIgnoreCase(serverName) && !"127.0.0.1".equals(serverName)) {
            return serverName;
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (!nif.isUp() || nif.isLoopback() || nif.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // Fallback auf den Servernamen der Anfrage, falls Netzwerkinterfaces nicht lesbar sind
        }
        return serverName != null ? serverName : "localhost";
    }

    // Baut einen vollständigen Einladungslink für den Fragebogen
    private String buildRoleQuizUrl(String baseUrl, AssessmentRole role, String teamCode, String roundName) {
        return baseUrl + "/quiz?role=" + role.name()
                + "&team=" + encode(teamCode)
                + "&round=" + encode(roundName);
    }

    // Baut einen vollständigen Link zur Vergleichsansicht
    private String buildCompareUrl(String baseUrl, String teamCode, String roundName) {
        return baseUrl + "/compare?team=" + encode(teamCode) + "&round=" + encode(roundName);
    }

    // Kodiert URL-Query-Parameter UTF-8-konform
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
