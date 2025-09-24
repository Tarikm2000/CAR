package com.example.demo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class AkkaService {
    private static final int NUM_MAPPERS = 3;
    private static final int NUM_REDUCERS = 2;
    private static final int ASK_TIMEOUT = 1000;
    private static final int FUTURE_TIMEOUT = 1;

    private ActorSystem actorSystem;
    private ActorRef[] mappers;
    private ActorRef[] reducers;

    public AkkaService() {
        this.actorSystem = ActorSystem.create("MapReduceSystem");
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public ActorRef[] getMappers() {
        return mappers;
    }

    public ActorRef[] getReducers() {
        return reducers;
    }

    public void init() {
        if (actorSystem == null) {
            throw new IllegalStateException("Actor system not initialized.");
        }

        initializeActors();
    }

    private void initializeActors() {
        if (mappers == null || reducers == null) {
            mappers = new ActorRef[NUM_MAPPERS];
            reducers = new ActorRef[NUM_REDUCERS];

            for (int i = 0; i < NUM_MAPPERS; i++) {
                mappers[i] = actorSystem.actorOf(Props.create(Mapper.class, this), "mapper" + i);
            }

            for (int i = 0; i < NUM_REDUCERS; i++) {
                reducers[i] = actorSystem.actorOf(Props.create(Reducer.class), "reducer" + i);
            }

            System.out.println("Akka system initialized with " + NUM_MAPPERS + " Mappers and " + NUM_REDUCERS + " Reducers.");
        }
    }

    private void resetReducers() {
        for (ActorRef reducer : reducers) {
            reducer.tell("RESET", ActorRef.noSender());
        }
    }


    public void processFile(File file) {
        if (actorSystem == null || mappers == null) {
            throw new IllegalStateException("Akka system not initialized.");
        }

        resetReducers(); // Réinitialisation avant un nouveau traitement

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(this::sendToMapper);
            System.out.println("Finished sending data to mappers.");
        } catch (IOException e) {
            handleError("Error reading the file: ", e);
        }
    }


    private void sendToMapper(String line) {
        int mapperIndex = line.hashCode() % NUM_MAPPERS;
        mappers[mapperIndex].tell(line, ActorRef.noSender());
    }

    public int countOccurrences(String word) {
        if (reducers == null || reducers.length == 0) {
            throw new IllegalStateException("No reducers available.");
        }

        int count = 0;

        for (ActorRef reducer : reducers) {
            count += getCountFromReducer(reducer, word);
        }

        return count;
    }

    private int getCountFromReducer(ActorRef reducer, String word) {
        try {
            Future<Object> future = Patterns.ask(reducer, "COUNT:" + word, ASK_TIMEOUT);
            return (Integer) Await.result(future, Duration.create(FUTURE_TIMEOUT, TimeUnit.SECONDS));
        } catch (Exception e) {
            handleError("Error getting count from reducer: ", e);
            return 0;
        }
    }

    private void handleError(String message, Exception e) {
        System.err.println(message + e.getMessage());
        e.printStackTrace();
    }
}
