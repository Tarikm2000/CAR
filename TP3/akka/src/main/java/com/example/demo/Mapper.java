package com.example.demo;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class Mapper extends UntypedActor {

    private final AkkaService akkaService;

    public Mapper(AkkaService akkaService) {
        this.akkaService = akkaService;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof String line) {
            ActorRef[] reducers = akkaService.getReducers();

            if (reducers == null || reducers.length == 0) {
                System.err.println("Aucun reducer disponible pour traiter le mot.");
                return;
            }
            String[] words = line.split("\\s+");

            for (String word : words) {
                word = cleanWord(word);
                if (!word.isEmpty()) {
                    ActorRef reducer = partition(reducers, word);
                    reducer.tell(word, getSelf());
                }
            }
        } else {
            unhandled(message);
        }
    }

    private String cleanWord(String word) {
        return word.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }

    private ActorRef partition(ActorRef[] reducers, String word) {
        int index = Math.abs(word.hashCode()) % reducers.length; // Calcul de l'index
        return reducers[index];
    }
}
