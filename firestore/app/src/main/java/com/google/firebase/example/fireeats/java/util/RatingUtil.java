package com.google.firebase.example.fireeats.java.util;

import com.google.firebase.example.fireeats.java.model.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Utilities for Ratings.
 */
public class RatingUtil {

    public static final String[] REVIEW_CONTENTS = {
            // 0 - 1 stars
            "This was awful! Totally inedible.",

            // 1 - 2 stars
            "This was pretty bad, would not go back.",

            // 2 - 3 stars
            "I was fed, so that's something.",

            // 3 - 4 stars
            "This was a nice meal, I'd go back.",

            // 4 - 5 stars
            "This was fantastic!  Best ever!"
    };

    /**
     * Get a list of random Rating POJOs.
     */
    public static List<Rating> getRandomList(int length) {
        List<Rating> result = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            result.add(getRandom());
        }

        return result;
    }

    /**
     * Get the average rating of a List.
     */
    public static double getAverageRating(List<Rating> ratings) {
        double sum = 0.0;

        for (Rating rating : ratings) {
            sum += rating.getRating();
        }

        return sum / ratings.size();
    }

    /**
     * Create a random Rating POJO.
     */
    public static Rating getRandom() {
        Rating rating = new Rating();

        Random random = new Random();

        double score = random.nextDouble() * 5.0;
        String text = REVIEW_CONTENTS[(int) Math.floor(score)];

        rating.setUserId(UUID.randomUUID().toString());
        rating.setUserName("Random User");
        rating.setRating(score);
        rating.setText(text);

        return rating;
    }

}
