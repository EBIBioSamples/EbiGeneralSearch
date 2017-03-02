package uk.ac.ebi.biosamples.model.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionInfo {
    private AtomicInteger submitted;
    private AtomicInteger completed;
    private AtomicInteger error;

    public ExecutionInfo() {
        submitted = new AtomicInteger(0);
        completed = new AtomicInteger(0);
        error = new AtomicInteger(0);
    }


    public void incrementSubmitted(int value) {
        int oldValue = submitted.get();
        submitted.set(oldValue + value);
    }

    public void incrementCompleted(int value) {
        int oldValue = completed.get();
        completed.set(oldValue + value);
    }

    public void incrementError(int value) {
        int oldValue = error.get();
        error.set(oldValue + value);
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
