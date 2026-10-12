package com.moodflix.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.moodflix.model.Movie;
import com.moodflix.repository.MovieRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadMovies(MovieRepository movieRepository) {
        return args -> {

            if (movieRepository.count() == 0) {

                movieRepository.save(new Movie(
                        "Fidaa", "Romantic", "Romance", "Telugu", 134));

                movieRepository.save(new Movie(
                        "Jersey", "Sad", "Drama", "Telugu", 157));

                movieRepository.save(new Movie(
                        "Jathi Ratnalu", "Happy", "Comedy", "Telugu", 148));

                movieRepository.save(new Movie(
                        "RRR", "Excited", "Action", "Telugu", 182));

                movieRepository.save(new Movie(
                        "Arjun Reddy", "Angry", "Drama", "Telugu", 182));

                movieRepository.save(new Movie(
                        "Sita Ramam", "Romantic", "Romance", "Telugu", 163));

                movieRepository.save(new Movie(
                        "3 Idiots", "Happy", "Comedy", "Hindi", 170));

                movieRepository.save(new Movie(
                        "Zindagi Na Milegi Dobara", "Relaxed", "Drama", "Hindi", 155));

                movieRepository.save(new Movie(
                        "Inception", "Excited", "Sci-Fi", "English", 148));

                movieRepository.save(new Movie(
                        "The Hangover", "Happy", "Comedy", "English", 100));

                System.out.println("🎬 Sample movies added successfully!");
            }
        };
    }
}
