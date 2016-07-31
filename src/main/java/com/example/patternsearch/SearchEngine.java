package com.example.patternsearch;

import com.example.jdbc.Contact;
import com.example.jdbc.Contacts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchEngine {
    private Contacts contacts;
    private static SearchEngine searchEngine = null;
    private boolean isEngineWorking = false;
    private final long ROWS_QUANTITY = 10000;

    private SearchEngine() {
    }

    public static SearchEngine getInstance(Contacts contacts) {
        if (searchEngine == null) {
            searchEngine = new SearchEngine();
            searchEngine.contacts = contacts;
        }
        return searchEngine;
    }

    /**
     * Adding new pair of pattern and it`s future to PatternQueue
     * If search engine isn't working than getting
     * all accumulated pairs from PatternQueue and searching for contacts for each pattern
     * @param nameFilter - pattern from request
     * @return completable future of contacts names list
     */
    public CompletableFuture<List<Contact>> search(String nameFilter) {
        Pattern pattern = Pattern.compile(nameFilter);
        CompletableFuture<List<Contact>> future = new CompletableFuture<>();
        PatternQueue.getInstance().addNewPattern(pattern, future);
        if (!isEngineWorking) {
            try {
                searchForContacts(PatternQueue.getInstance().getAllAccumulatedPatterns());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return future;
    }

    /**
     * Searching for matching contact names in the database
     * 1. Creating list of contact names for each pattern (finalResults map)
     * 2. Selecting the specified number of rows from the database by limit and offset
     * 3. Filtering the selected data using concurrent threads
     * 4. Adding filtered data to the list of contact names for each pattern
     * 5. Completing the futures
     * 6. Checking for the new patterns in PatternQueue and executing the method
     *    from the beginning in case if there are new patterns in PatternQueue
     * @param patternFutureList - list of all accumulated pairs of pattern and it`s future
     * @throws InterruptedException
     */
    public void searchForContacts(List<PatternQueue.PatternFuture> patternFutureList) throws InterruptedException {
        isEngineWorking = true;
        final Map<Pattern, List<Contact>> finalResults = fillResultMapWithPatternKeys(patternFutureList);
        int iterationNumber = 0;
        int rowsAmount;
        do {
            final List<Contact> contactList = contacts.getContactsList(ROWS_QUANTITY, iterationNumber++ * ROWS_QUANTITY);
            rowsAmount = contactList.size();
            int threadAmount = Runtime.getRuntime().availableProcessors();
            int rowsPerThread = contactList.size() / threadAmount;
            executeConcurrent(threadAmount, threadNumber -> {
                List<Contact> subList = contactList.subList(threadNumber * rowsPerThread, (threadNumber + 1) * rowsPerThread);
                for (Map.Entry<Pattern, List<Contact>> patternListEntry : finalResults.entrySet()) {
                    List<Contact> result = getContactsByPattern(patternListEntry.getKey(), subList);
                    patternListEntry.getValue().addAll(result);
                }
            });
        } while (rowsAmount == ROWS_QUANTITY);

        for (PatternQueue.PatternFuture patternEntity : patternFutureList) {
            patternEntity.getFuture()
                    .complete(finalResults.get(patternEntity.getPattern()));
        }

        if (!PatternQueue.getInstance().isEmpty()) {
            searchForContacts(PatternQueue.getInstance().getAllAccumulatedPatterns());
        }
        isEngineWorking = false;
        finalResults.clear();
    }

    /**
     * Filling the result map with patterns as keys and empty lists of contact names as values
     * @param patternFutureList - list of all accumulated pairs of pattern and it`s future
     * @return filled result map
     */
    private Map<Pattern, List<Contact>> fillResultMapWithPatternKeys(List<PatternQueue.PatternFuture> patternFutureList) {
        Map<Pattern, List<Contact>> resultMap = new LinkedHashMap<>();
        for (PatternQueue.PatternFuture patternEntity : patternFutureList) {
            resultMap.putIfAbsent(patternEntity.getPattern(), new ArrayList<>());
        }
        return resultMap;
    }

    /**
     * Creating the specified number of threads with the same action and executing them
     * @param threadsAmount - the number of threads that will be executed
     * @param action - the method that will be executed in every thread with it`s number
     * @throws InterruptedException
     */
    public static void executeConcurrent(int threadsAmount, final ThreadAction action) throws InterruptedException {
        final List<Thread> threads = new ArrayList<>(threadsAmount);
        for (int threadNumber = 0; threadNumber < threadsAmount; threadNumber++) {
            final int finalThreadNumber = threadNumber;
            Thread newThread = new Thread(() -> action.execute(finalThreadNumber));
            newThread.start();
            threads.add(newThread);
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    interface ThreadAction {
        void execute(int threadNumber);
    }

    /**
     * Filtering list of contact names with pattern
     * @param pattern - name filter from request
     * @param contactList - current part of rows with contact names from the database
     * @return - list of contact names that match pattern
     */
    private List<Contact> getContactsByPattern(Pattern pattern, List<Contact> contactList) {
        return contactList.stream().filter(contact -> !pattern.matcher(contact.getName()).matches())
                .collect(Collectors.toList());
    }

}
