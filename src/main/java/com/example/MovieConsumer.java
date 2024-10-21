package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MovieConsumer {
    @Inject
    MovieService  movieService;
    private final Logger logger = Logger.getLogger(MovieConsumer.class);

    @Incoming("movies-in")
    public void receive(String movie) throws JsonProcessingException {
//        logger.infof("Got a movie:  %s",movie );

        movieService.createMovie(movie);
//        movieRepository.addMovie(new Moive(record.value(), record.key()));
//        System.out.println("added movie to database: " +record);
    }
}