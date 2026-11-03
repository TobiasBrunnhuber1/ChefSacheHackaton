package com.managementstyle.recommender.system;

import com.managementstyle.recommender.system.Questionaire.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class RecommenderSystemApplication {

	private	static final Logger log = LoggerFactory.getLogger(RecommenderSystemApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RecommenderSystemApplication.class, args);
		log.info("Success");
	}

}
