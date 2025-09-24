package com.example.demo;

import akka.actor.UntypedActor;
import java.util.HashMap;
import java.util.Map;

public class Reducer extends UntypedActor {
    private final Map<String, Integer> wordCounts = new HashMap<>();

    @Override
    public void onReceive(Object message) {
        if (message instanceof String word) {
            if (word.equals("RESET")) {
                wordCounts.clear();  // Réinitialiser le compteur
            } else if (word.startsWith("COUNT:")) {
                String searchWord = word.substring(6);
                int count = wordCounts.getOrDefault(searchWord, 0);
                getSender().tell(count, getSelf());
            } else {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
    }



}
