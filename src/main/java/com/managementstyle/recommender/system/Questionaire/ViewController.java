package com.managementstyle.recommender.system.Questionaire;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
public class ViewController {
    private final QuestionRepository questionRepository;

    public ViewController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public String showQuiz(Model model) {
        model.addAttribute("questions", questionRepository.findAll());
        return "quiz";
    }

    @PostMapping("/submit")
    public String processQuiz(@ModelAttribute QuizAnswers submission, Model model) {
        List<Question> questions = questionRepository.findAll();
        List<Integer> answers = submission.getAnswers();
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
        model.addAttribute("scores", scores);
        model.addAttribute("totalScore", total / questions.size());
        return "result";
    }
}