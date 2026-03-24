package com.example.calculator;

import java.io.Serializable;

public class HistoryEntry implements Serializable {
    public final String expression;
    public final String result;

    public HistoryEntry(String expression, String result) {
        this.expression = expression;
        this.result = result;
    }
}
