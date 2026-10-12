
package com.moodflix.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moodflix.model.Watchlist;
import com.moodflix.repository.WatchlistRepository;

@RestController
@RequestMapping("/api/watchlist")
@CrossOrigin
public class WatchlistController {

    private final WatchlistRepository watchlistRepository;

    public WatchlistController(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    @PostMapping("/add")
    public Watchlist addToWatchlist(@RequestBody Watchlist watchlist) {

        if (watchlistRepository.existsByUserIdAndMovieId(
                watchlist.getUserId(),
                watchlist.getMovieId())) {

            return watchlist;
        }

        return watchlistRepository.save(watchlist);
    }

    @GetMapping("/{userId}")
    public List<Watchlist> getWatchlist(@PathVariable Long userId) {

        return watchlistRepository.findByUserId(userId);
    }

    @DeleteMapping("/{userId}/{movieId}")
    public String removeFromWatchlist(
            @PathVariable Long userId,
            @PathVariable Long movieId) {

        watchlistRepository.deleteByUserIdAndMovieId(userId, movieId);

        return "Movie removed from watchlist";
    }
}