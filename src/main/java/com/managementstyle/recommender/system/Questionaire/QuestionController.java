/*package com.managementstyle.recommender.system.Questionaire;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository){
        this.questionRepository = questionRepository;
    }
    @GetMapping("/api/questions*///")
   // List<Question> findAll(){
     //   return questionRepository.findAll();
    //}
/*
    @GetMapping("/{id}")
    Question findById(@PathVariable Integer id){
        Optional<Question> question= questionRepository.findById(id);
        if(question.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return question.get();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    void create(@RequestBody Question question){
        questionRepository.create(question);
    }

    //put
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    void update(@RequestBody Question question,@PathVariable Integer id){
        questionRepository.update(question,id);
    }

    //delete
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    void delete(@PathVariable Integer id){
        questionRepository.delete(id);
    }
    //Testing

*/

//}*/