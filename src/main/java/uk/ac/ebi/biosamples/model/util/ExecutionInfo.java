package uk.ac.ebi.biosamples.model.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionInfo {
    private final AtomicInteger submitted;
    private final AtomicInteger completed;
    private final AtomicInteger error;

    public ExecutionInfo() {
        submitted = new AtomicInteger(0);
        completed = new AtomicInteger(0);
        error = new AtomicInteger(0);
    }


    public void incrementSubmitted(int value) {
        submitted.getAndAdd(value);
    }

    public void incrementCompleted(int value) {
        completed.getAndAdd(value);
    }

    public void incrementError(int value) {
        error.getAndAdd(value);
    }

    public int getCompleted() {
        return completed.get();
    }

    public int getSubmitted() {
        return submitted.get();
    }

    public int getErrors() {
       return error.get();
    }
}
