package com.coletz.polyquery.processor;

public class ProcessorException extends Throwable {
    private PolyQueryAnnotatedClass existing;

    public ProcessorException(PolyQueryAnnotatedClass existing) {
        this.existing = existing;
    }

    public PolyQueryAnnotatedClass getExisting() {
        return existing;
    }
}
