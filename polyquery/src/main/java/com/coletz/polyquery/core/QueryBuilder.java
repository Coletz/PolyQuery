package com.coletz.polyquery.core;

public class QueryBuilder {
    private SupportedOperation operation;
    private String field;
    private Object value;

    public QueryBuilder(SupportedOperation operation, String field, Object value) {
        this.operation = operation;
        this.field = field;
        this.value = value;
    }

    public SupportedOperation getOperation() {
        return operation;
    }

    public void setOperation(SupportedOperation operation) {
        this.operation = operation;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
