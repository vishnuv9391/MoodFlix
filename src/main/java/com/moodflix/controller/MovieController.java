package com.moodflix.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.moodflix.model.Movie;
import com.moodflix.repository.MovieRepository;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin
public class MovieController {

    private final MovieRepository movieRepository;

    public MovieController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // READ: Get all movies
    @GetMapping
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // READ: Get a movie by ID
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        Optional<Movie> movie = movieRepository.findById(id);

        return movie.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CREATE: Add a new movie
    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestBody Movie movie) {
        movie.setId(null);

        Movie savedMovie = movieRepository.save(movie);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
    }

    // UPDATE: Update an existing movie
    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(
            @PathVariable Long id,
            @RequestBody Movie updatedMovie) {

        Optional<Movie> existingMovie = movieRepository.findById(id);

        if (existingMovie.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = existingMovie.get();

        movie.setTitle(updatedMovie.getTitle());
        movie.setMood(updatedMovie.getMood());
        movie.setGenre(updatedMovie.getGenre());
        movie.setLanguage(updatedMovie.getLanguage());
        movie.setDuration(updatedMovie.getDuration());

        Movie savedMovie = movieRepository.save(movie);

        return ResponseEntity.ok(savedMovie);
    }

    // DELETE: Delete a movie
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {

        if (!movieRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        movieRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // READ: Recommend movies based on mood, genre, language and time
    @GetMapping("/recommend")
    public List<Movie> recommendMovies(
            @RequestParam String mood,
            @RequestParam String genre,
            @RequestParam String language,
            @RequestParam(required = false) String time) {

        if ("short".equalsIgnoreCase(time)) {
            return movieRepository
                    .findByMoodAndGenreAndLanguageAndDurationBetween(
                            mood, genre, language, 0, 60);
        }

        if ("medium".equalsIgnoreCase(time)) {
            return movieRepository
                    .findByMoodAndGenreAndLanguageAndDurationBetween(
                            mood, genre, language, 61, 120);
        }

        if ("long".equalsIgnoreCase(time)) {
            return movieRepository
                    .findByMoodAndGenreAndLanguageAndDurationGreaterThan(
                            mood, genre, language, 120);
        }

        return movieRepository.findByMoodAndGenreAndLanguage(
                mood, genre, language);
    }
}