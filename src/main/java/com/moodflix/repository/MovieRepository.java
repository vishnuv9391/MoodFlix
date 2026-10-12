package com.moodflix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moodflix.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByMoodAndGenreAndLanguage(
            String mood,
            String genre,
            String language
    );

    List<Movie> findByMoodAndGenreAndLanguageAndDurationBetween(
            String mood,
            String genre,
            String language,
            int minDuration,
            int maxDuration
    );

    List<Movie> findByMoodAndGenreAndLanguageAndDurationGreaterThan(
            String mood,
            String genre,
            String language,
            int duration
    );
}
