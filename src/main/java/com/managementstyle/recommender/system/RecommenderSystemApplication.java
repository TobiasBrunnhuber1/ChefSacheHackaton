package com.managementstyle.recommender.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Einstiegspunkt der Spring-Boot-Webanwendung chefsache
@SpringBootApplication
public class RecommenderSystemApplication {

    // Logger zur Protokollierung des Anwendungsstarts
    private static final Logger log = LoggerFactory.getLogger(RecommenderSystemApplication.class);

    // Startet den eingebetteten Webserver samt Datenbank- und Web-Kontext
    public static void main(String[] args) {
        SpringApplication.run(RecommenderSystemApplication.class, args);
        log.info("chefsache erfolgreich gestartet");
    }
}
