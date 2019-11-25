package com.coletz.polyquery.core;

class PolyException extends Throwable {

    public PolyException(String message) {
        super(message);
    }

    public PolyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolyException(Throwable cause) {
        super(cause);
    }

    public PolyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}