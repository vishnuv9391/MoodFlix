package com.moodflix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moodflix.model.Watchlist;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserId(Long userId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    void deleteByUserIdAndMovieId(Long userId, Long movieId);
}