package com.managementstyle.recommender.system;

import com.managementstyle.recommender.system.Questionaire.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
class RecommenderSystemApplicationTests {
    @Autowired
    private MockMvc mvc;

    private MockHttpServletRequestBuilder submission(String value) {
        var request = post("/quiz/submit");
        for (int i = 0; i < 15; i++) {
            request.param("answers[" + i + "]", value);
        }
        return request;
    }

    @Test
    void showsFifteenSeparateLikertGroupsWithoutWeightsOrPreselectedAnswers() throws Exception {
        String html = mvc.perform(get("/quiz")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (String code : new String[]{"ego", "pres", "und", "tc", "man"}) {
            for (int i = 1; i <= 3; i++) {
                assertThat(html).contains("Expl_" + code + i);
            }
        }
        for (int i = 0; i < 15; i++) {
            assertThat(html.split("name=\"answers\\[" + i + "\\]\"", -1)).hasSize(6);
        }
        assertThat(html).contains("value=\"1\"", "value=\"5\"", "type=\"radio\"")
                .doesNotContain("name=\"weights\"", "checked");
    }

    @Test
    void calculatesMeansAtBothScaleLimitsAndNeutral() throws Exception {
        for (int answer : new int[]{1, 3, 5}) {
            var result = mvc.perform(submission(String.valueOf(answer)))
                    .andExpect(status().isOk()).andExpect(view().name("result"))
                    .andExpect(model().attribute("totalScore", (double) answer)).andReturn();
            Map<?, ?> scores = (Map<?, ?>) result.getModelAndView().getModel().get("scores");
            assertThat(scores).hasSize(5);
            for (Object score : scores.values()) {
                assertThat(score).isEqualTo((double) answer);
            }
        }
    }

    @Test
    void calculatesDistinctAreaMeansAndOverallMean() throws Exception {
        var request = post("/quiz/submit");
        for (int i = 0; i < 15; i++) {
            request.param("answers[" + i + "]", i >= 12 ? "5" : String.valueOf(1 + 2 * (i % 3)));
        }
        var result = mvc.perform(request).andExpect(status().isOk())
                .andExpect(model().attribute("totalScore", 3.4)).andReturn();
        Map<?, ?> scores = (Map<?, ?>) result.getModelAndView().getModel().get("scores");
        assertThat(scores.get(Category.EGOISM)).isEqualTo(3.0);
        assertThat(scores.get(Category.MANIPULATION)).isEqualTo(5.0);
    }

    @Test
    void rejectsMissingAndOutOfRangeAnswers() throws Exception {
        mvc.perform(post("/quiz/submit")).andExpect(status().isBadRequest());
        var missingMiddle = post("/quiz/submit");
        for (int i = 0; i < 15; i++) {
            if (i != 7) missingMiddle.param("answers[" + i + "]", "3");
        }
        mvc.perform(missingMiddle).andExpect(status().isBadRequest());
        for (String value : new String[]{"-3", "0", "6", "2.5", "text"}) {
            mvc.perform(submission(value)).andExpect(status().isBadRequest());
        }
        mvc.perform(submission("3").param("answers[15]", "3")).andExpect(status().isBadRequest());
    }
}