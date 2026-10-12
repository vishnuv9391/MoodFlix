package com.moodflix.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moodflix.service.TmdbService;

@RestController
@RequestMapping("/api/tmdb")
@CrossOrigin
public class TmdbController {

    private final TmdbService tmdbService;

    public TmdbController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/popular")
    public String getPopularMovies(
            @RequestParam(defaultValue = "1") int page) {
        return tmdbService.getPopularMovies(page);
    }

    @GetMapping("/discover")
    public String discoverMovies(
            @RequestParam(defaultValue = "") String genre,
            @RequestParam(defaultValue = "") String language,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "1") int page) {

        return tmdbService.discoverMovies(
                genre, language, time, page);
    }

    @GetMapping("/search")
    public String searchMovies(
            @RequestParam String query,
            @RequestParam(defaultValue = "") String language,
            @RequestParam(defaultValue = "1") int page) {

        return tmdbService.searchMovies(query, language, page);
    }

    @GetMapping("/recommend")
    public String recommendMovies(
            @RequestParam(defaultValue = "happy") String mood,
            @RequestParam(defaultValue = "") String genre,
            @RequestParam(defaultValue = "") String language,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "1") int page) {

        return tmdbService.recommendMovies(
                mood, genre, language, time, page);
    }
}