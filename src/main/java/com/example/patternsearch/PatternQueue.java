package com.example.patternsearch;

import com.example.jdbc.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Singleton class for storing pairs
 * of patterns from requests and their futures
 */

public class PatternQueue {
    private static List<PatternFuture> queue = Collections.synchronizedList(new ArrayList<>());
    private static PatternQueue patternQueue = null;

    private PatternQueue() {
    }

    public static PatternQueue getInstance() {
        if (patternQueue == null) {
            patternQueue = new PatternQueue();
        }
        return patternQueue;
    }

    public void addNewPattern(Pattern pattern, CompletableFuture<List<Contact>> future) {
        queue.add(new PatternFuture(pattern, future));
    }

    public List<PatternFuture> getAllAccumulatedPatterns() {
        List<PatternFuture> newPatterns = new ArrayList<>(queue);
        queue.clear();
        return newPatterns;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    class PatternFuture {
        private Pattern pattern;
        private CompletableFuture<List<Contact>> future;

        public PatternFuture(Pattern pattern, CompletableFuture<List<Contact>> future) {
            this.pattern = pattern;
            this.future = future;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public CompletableFuture<List<Contact>> getFuture() {
            return future;
        }
    }

}
