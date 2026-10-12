package com.moodflix.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class TmdbService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiToken;

    public TmdbService(
            @Value("${tmdb.api.url:https://api.themoviedb.org/3}") String apiUrl,
            @Value("${tmdb.api.token}") String apiToken) {

        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.apiUrl = apiUrl.replaceAll("/+$", "");
        this.apiToken = apiToken;
    }

    public String getPopularMovies(int page) {
        return get("/movie/popular", "page=" + safePage(page));
    }

    public String discoverMovies(
            String genre, String language, String time, int page) {

        List<String> params = new ArrayList<>();
        params.add("page=" + safePage(page));

        String id = genreId(genre);
        if (!id.isBlank()) {
            params.add("with_genres=" + encode(id));
        }

        String lang = languageCode(language);
        if (!lang.isBlank()) {
            params.add("with_original_language=" + encode(lang));
        }

        addRuntime(params, time);

        return get("/discover/movie", String.join("&", params));
    }

    public String searchMovies(
            String query, String language, int page) {

        List<String> params = new ArrayList<>();
        params.add("query=" + encode(query == null ? "" : query.trim()));
        params.add("page=" + safePage(page));

        String lang = languageCode(language);
        if (!lang.isBlank()) {
            params.add("language=" + encode(lang));
        }

        return get("/search/movie", String.join("&", params));
    }

    public String recommendMovies(
            String mood, String genre, String language,
            String time, int page) {

        List<String> params = new ArrayList<>();
        params.add("page=" + safePage(page));
        params.add("sort_by=popularity.desc");

        String id = genreId(genre);
        if (!id.isBlank()) {
            params.add("with_genres=" + encode(id));
        }

        String lang = languageCode(language);
        if (!lang.isBlank()) {
            params.add("with_original_language=" + encode(lang));
        }

        addRuntime(params, time);

        String response = get(
                "/discover/movie", String.join("&", params));

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (!results.isArray()) {
                return response;
            }

            String selectedMood = mood == null
                    ? "happy"
                    : mood.toLowerCase(Locale.ROOT).trim();

            List<JsonNode> movies = new ArrayList<>();
            results.forEach(movies::add);

            movies.sort((a, b) ->
                    Integer.compare(
                            moodScore(b, selectedMood),
                            moodScore(a, selectedMood)));

            ObjectNode updated = ((ObjectNode) root).deepCopy();
            var sortedResults = objectMapper.createArrayNode();

            movies.forEach(sortedResults::add);
            updated.set("results", sortedResults);

            return objectMapper.writeValueAsString(updated);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to process recommendations: "
                            + e.getMessage(), e);
        }
    }

    private String get(String path, String query) {
        String uri = apiUrl + path
                + (query == null || query.isBlank()
                ? "" : "?" + query);

        try {
            String response = restClient.get()
                    .uri(uri)
                    .headers(headers -> {
                        headers.setBearerAuth(apiToken);
                        headers.setAccept(
                                List.of(MediaType.APPLICATION_JSON));
                    })
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new IllegalStateException(
                        "TMDB returned an empty response.");
            }

            return response;

        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "TMDB HTTP " + e.getStatusCode().value()
                            + " for " + path
                            + ". Response: " + safeBody(
                            e.getResponseBodyAsString()), e);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "TMDB connection failed for " + path
                            + ": " + e.getMessage(), e);
        }
    }

    private String safeBody(String body) {
        if (body == null || body.isBlank()) {
            return "(empty response)";
        }

        return body.length() > 500
                ? body.substring(0, 500)
                : body;
    }

    private int safePage(int page) {
        return Math.max(1, page);
    }

    private void addRuntime(List<String> params, String time) {
        if (time == null || time.isBlank()) {
            return;
        }

        switch (time.toLowerCase(Locale.ROOT).trim()) {
            case "short", "under 90", "under90", "less than 90" ->
                    params.add("with_runtime.lte=90");

            case "medium", "90-120", "90 to 120" -> {
                params.add("with_runtime.gte=90");
                params.add("with_runtime.lte=120");
            }

            case "long", "over 120", "over120", "more than 120" ->
                    params.add("with_runtime.gte=120");

            default -> {
                // Ignore unknown runtime filters.
            }
        }
    }

    private String genreId(String genre) {
        if (genre == null || genre.isBlank()) {
            return "";
        }

        return switch (genre.toLowerCase(Locale.ROOT).trim()) {
            case "action" -> "28";
            case "adventure" -> "12";
            case "animation" -> "16";
            case "comedy" -> "35";
            case "crime" -> "80";
            case "documentary" -> "99";
            case "drama" -> "18";
            case "family" -> "10751";
            case "fantasy" -> "14";
            case "history" -> "36";
            case "horror" -> "27";
            case "music" -> "10402";
            case "mystery" -> "9648";
            case "romance" -> "10749";
            case "science fiction", "sci-fi", "scifi" -> "878";
            case "thriller" -> "53";
            case "war" -> "10752";
            case "western" -> "37";
            default -> genre.matches("\\d+") ? genre : "";
        };
    }

    private String languageCode(String language) {
        if (language == null || language.isBlank()) {
            return "";
        }

        return switch (language.toLowerCase(Locale.ROOT).trim()) {
            case "english" -> "en";
            case "hindi" -> "hi";
            case "telugu" -> "te";
            case "tamil" -> "ta";
            case "kannada" -> "kn";
            case "malayalam" -> "ml";
            case "korean" -> "ko";
            case "japanese" -> "ja";
            case "spanish" -> "es";
            case "french" -> "fr";
            default -> language.matches("[a-zA-Z]{2}")
                    ? language.toLowerCase(Locale.ROOT)
                    : "";
        };
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private int moodScore(JsonNode movie, String mood) {
        String text = (
                movie.path("title").asText("") + " "
                        + movie.path("overview").asText("")
        ).toLowerCase(Locale.ROOT);

        String[] keywords = switch (mood) {
            case "happy", "happiness", "feel good" ->
                    new String[]{"happy", "friend", "comedy", "love",
                            "fun", "family", "joy", "adventure"};

            case "sad", "sadness" ->
                    new String[]{"sad", "loss", "grief", "heart",
                            "emotional", "lonely", "drama"};

            case "romantic", "romance", "love" ->
                    new String[]{"love", "romance", "relationship",
                            "couple", "wedding", "heart"};

            case "thrilled", "thriller", "excited" ->
                    new String[]{"thriller", "chase", "danger",
                            "mystery", "crime", "action", "escape"};

            case "relaxed", "calm", "peaceful" ->
                    new String[]{"peace", "journey", "nature",
                            "friendship", "life", "family", "calm"};

            case "scared", "fear", "horror" ->
                    new String[]{"horror", "ghost", "haunted", "fear",
                            "dark", "killer", "supernatural"};

            default -> new String[]{mood};
        };

        int score = 0;

        for (String keyword : keywords) {
            if (!keyword.isBlank() && text.contains(keyword)) {
                score++;
            }
        }

        return score;
    }
}