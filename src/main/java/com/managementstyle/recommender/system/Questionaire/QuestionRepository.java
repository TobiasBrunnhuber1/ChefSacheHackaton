package com.managementstyle.recommender.system.Questionaire;

import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class QuestionRepository {
    private final List<Question> questions = List.of(
        new Question("Expl_ego1", Category.EGOISM, "Takes it for granted that my work can be used for his or her personal benefit."),
        new Question("Expl_ego2", Category.EGOISM, "Sees employees as a means to reach his or her personal goals."),
        new Question("Expl_ego3", Category.EGOISM, "Values the achievement of his or her own goals over the needs of the employees."),
        new Question("Expl_pres1", Category.PRESSURE, "Puts me under pressure to reach his or her goals."),
        new Question("Expl_pres2", Category.PRESSURE, "Increases my workload without considering my needs in order to reach his or her goals."),
        new Question("Expl_pres3", Category.PRESSURE, "Does not consider my workload when new tasks need to be assigned."),
        new Question("Expl_und1", Category.UNDERMINING_DEVELOPMENT, "Gives me tedious tasks if he or she can benefit from it."),
        new Question("Expl_und2", Category.UNDERMINING_DEVELOPMENT, "Does not give me opportunities to further develop myself professionally because his or her own goals have priority."),
        new Question("Expl_und3", Category.UNDERMINING_DEVELOPMENT, "Gives me boring routine tasks when he or she can benefit from it."),
        new Question("Expl_tc1", Category.TAKING_CREDIT, "Uses my work to get himself or herself noticed."),
        new Question("Expl_tc2", Category.TAKING_CREDIT, "Passes the team’s work off as his or her own."),
        new Question("Expl_tc3", Category.TAKING_CREDIT, "Uses my work for his or her personal gain."),
        new Question("Expl_man1", Category.MANIPULATION, "Plays my colleagues and me off against each other to reach his or her goals."),
        new Question("Expl_man2", Category.MANIPULATION, "Manipulates others to reach his or her goals."),
        new Question("Expl_man3", Category.MANIPULATION, "Does not hesitate to manipulate or deceive employees in order to reach his or her goals.")
    );

    List<Question> findAll() {
        return questions;
    }
}