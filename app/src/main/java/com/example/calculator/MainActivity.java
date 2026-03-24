package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // State keys for onSaveInstanceState
    private static final String KEY_DISPLAY = "display";
    private static final String KEY_OPERAND_A = "operandA";
    private static final String KEY_OPERAND_B = "operandB";
    private static final String KEY_OPERATOR = "operator";
    private static final String KEY_EXPRESSION = "expression";
    private static final String KEY_NEW_NUMBER = "newNumber";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_HISTORY_VISIBLE = "historyVisible";
    private static final String KEY_DARK_MODE = "darkMode";

    // Calculator state
    private String displayValue = "0";
    private double operandA = 0;
    private double operandB = 0;
    private String operator = "";
    private String expressionText = "";
    private boolean isNewNumber = true;

    // UI
    private TextView tvDisplay;
    private TextView tvExpression;
    private RecyclerView rvHistory;
    private MaterialButton btnHistory;
    private MaterialButton btnThemeToggle;

    private HistoryAdapter historyAdapter;
    private boolean historyVisible = false;
    private boolean isDarkMode = false;

    /**
     * Custom operator multiplier derived from last 3 digits of Student ID 22100331.
     * Last 3 digits = 331  →  multiplier = 3.31
     */
    private static final double CUSTOM_MULTIPLIER = 3.31;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore dark mode preference before setContentView so theme is applied
        if (savedInstanceState != null) {
            isDarkMode = savedInstanceState.getBoolean(KEY_DARK_MODE, false);
        }
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        setContentView(R.layout.activity_main);

        // Bind views
        tvDisplay = findViewById(R.id.tvDisplay);
        tvExpression = findViewById(R.id.tvExpression);
        rvHistory = findViewById(R.id.rvHistory);
        btnHistory = findViewById(R.id.btnHistory);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);

        // Set up history RecyclerView
        historyAdapter = new HistoryAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setAdapter(historyAdapter);

        // Restore state if available
        if (savedInstanceState != null) {
            displayValue = savedInstanceState.getString(KEY_DISPLAY, "0");
            operandA = savedInstanceState.getDouble(KEY_OPERAND_A, 0);
            operandB = savedInstanceState.getDouble(KEY_OPERAND_B, 0);
            operator = savedInstanceState.getString(KEY_OPERATOR, "");
            expressionText = savedInstanceState.getString(KEY_EXPRESSION, "");
            isNewNumber = savedInstanceState.getBoolean(KEY_NEW_NUMBER, true);
            historyVisible = savedInstanceState.getBoolean(KEY_HISTORY_VISIBLE, false);

            // Restore history entries
            ArrayList<HistoryEntry> restoredHistory =
                    (ArrayList<HistoryEntry>) savedInstanceState.getSerializable(KEY_HISTORY);
            if (restoredHistory != null) {
                historyAdapter.setEntries(restoredHistory);
            }
        }

        updateDisplay();
        updateHistoryVisibility();
        updateThemeButton();

        // Wire up number buttons
        int[] numIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        String[] numLabels = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (int i = 0; i < numIds.length; i++) {
            final String digit = numLabels[i];
            findViewById(numIds[i]).setOnClickListener(v -> appendDigit(digit));
        }

        // Decimal
        findViewById(R.id.btnDecimal).setOnClickListener(v -> appendDecimal());

        // Operators
        findViewById(R.id.btnAdd).setOnClickListener(v -> setOperator("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> setOperator("−"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> setOperator("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> setOperator("÷"));

        // Equals
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());

        // Clear / Reset
        findViewById(R.id.btnClear).setOnClickListener(v -> clearAll());

        // Delete last character
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteLastChar());

        // Backspace (C) - clears current entry
        findViewById(R.id.btnBackspace).setOnClickListener(v -> clearEntry());

        // Negate
        findViewById(R.id.btnNegate).setOnClickListener(v -> negate());

        // Percent
        findViewById(R.id.btnPercent).setOnClickListener(v -> percent());

        // Custom operator: multiply by 3.31 (last 3 digits of ID = 331)
        findViewById(R.id.btnCustom).setOnClickListener(v -> applyCustomOperator());

        // History toggle
        btnHistory.setOnClickListener(v -> toggleHistory());

        // Theme toggle
        btnThemeToggle.setOnClickListener(v -> toggleTheme());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_DISPLAY, displayValue);
        outState.putDouble(KEY_OPERAND_A, operandA);
        outState.putDouble(KEY_OPERAND_B, operandB);
        outState.putString(KEY_OPERATOR, operator);
        outState.putString(KEY_EXPRESSION, expressionText);
        outState.putBoolean(KEY_NEW_NUMBER, isNewNumber);
        outState.putBoolean(KEY_HISTORY_VISIBLE, historyVisible);
        outState.putBoolean(KEY_DARK_MODE, isDarkMode);
        outState.putSerializable(KEY_HISTORY,
                new ArrayList<>(historyAdapter.getEntries()));
    }

    // -----------------------------------------------------------------------
    // Input handling
    // -----------------------------------------------------------------------

    private void appendDigit(String digit) {
        if (isNewNumber) {
            displayValue = digit;
            isNewNumber = false;
        } else {
            if (displayValue.equals("0")) {
                displayValue = digit;
            } else {
                displayValue = displayValue + digit;
            }
        }
        updateDisplay();
    }

    private void appendDecimal() {
        if (isNewNumber) {
            displayValue = "0.";
            isNewNumber = false;
        } else if (!displayValue.contains(".")) {
            displayValue = displayValue + ".";
        }
        updateDisplay();
    }

    private void setOperator(String op) {
        // If we already have an operator and a second operand pending, compute first
        if (!operator.isEmpty() && !isNewNumber) {
            calculate();
        }
        try {
            operandA = Double.parseDouble(displayValue);
        } catch (NumberFormatException e) {
            operandA = 0;
        }
        operator = op;
        expressionText = formatNumber(operandA) + " " + op;
        isNewNumber = true;
        updateDisplay();
    }

    private void calculate() {
        if (operator.isEmpty()) return;

        try {
            operandB = Double.parseDouble(displayValue);
        } catch (NumberFormatException e) {
            operandB = 0;
        }

        String fullExpression = formatNumber(operandA) + " " + operator + " " + formatNumber(operandB);
        double result;

        switch (operator) {
            case "+":
                result = operandA + operandB;
                break;
            case "−":
                result = operandA - operandB;
                break;
            case "×":
                result = operandA * operandB;
                break;
            case "÷":
                if (operandB == 0) {
                    // Division by zero: show error message, do NOT crash
                    displayValue = getString(R.string.error_divide_by_zero);
                    expressionText = fullExpression + " =";
                    tvExpression.setText(expressionText);
                    tvDisplay.setText(displayValue);
                    operator = "";
                    isNewNumber = true;
                    return;
                }
                result = operandA / operandB;
                break;
            default:
                return;
        }

        String resultStr = formatNumber(result);
        historyAdapter.addEntry(new HistoryEntry(fullExpression + " =", resultStr));

        displayValue = resultStr;
        expressionText = fullExpression + " =";
        operandA = result;
        operator = "";
        isNewNumber = true;
        updateDisplay();
    }

    private void clearAll() {
        displayValue = "0";
        operandA = 0;
        operandB = 0;
        operator = "";
        expressionText = "";
        isNewNumber = true;
        updateDisplay();
    }

    private void clearEntry() {
        displayValue = "0";
        isNewNumber = true;
        updateDisplay();
    }

    private void deleteLastChar() {
        if (isNewNumber || displayValue.equals("0")) return;
        if (displayValue.length() <= 1) {
            displayValue = "0";
            isNewNumber = true;
        } else {
            displayValue = displayValue.substring(0, displayValue.length() - 1);
        }
        updateDisplay();
    }

    private void negate() {
        try {
            double value = Double.parseDouble(displayValue);
            value = -value;
            displayValue = formatNumber(value);
            updateDisplay();
        } catch (NumberFormatException e) {
            // ignore if display shows an error message
        }
    }

    private void percent() {
        try {
            double value = Double.parseDouble(displayValue);
            value = value / 100.0;
            displayValue = formatNumber(value);
            updateDisplay();
        } catch (NumberFormatException e) {
            // ignore if display shows an error message
        }
    }

    /**
     * Custom Operator: multiplies the current displayed value by 3.31.
     * Derived from last 3 digits of Student ID 22100331 (i.e., 331 → 3.31).
     */
    private void applyCustomOperator() {
        try {
            double value = Double.parseDouble(displayValue);
            double result = value * CUSTOM_MULTIPLIER;
            String expression = formatNumber(value) + " × 3.31";
            String resultStr = formatNumber(result);
            historyAdapter.addEntry(new HistoryEntry(expression + " =", resultStr));
            displayValue = resultStr;
            expressionText = expression + " =";
            isNewNumber = true;
            updateDisplay();
        } catch (NumberFormatException e) {
            // ignore if display shows an error message
        }
    }

    // -----------------------------------------------------------------------
    // History & Theme
    // -----------------------------------------------------------------------

    private void toggleHistory() {
        historyVisible = !historyVisible;
        updateHistoryVisibility();
    }

    private void updateHistoryVisibility() {
        if (historyVisible) {
            rvHistory.setVisibility(View.VISIBLE);
            btnHistory.setText("History ▲");
        } else {
            rvHistory.setVisibility(View.GONE);
            btnHistory.setText("History ▼");
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void updateThemeButton() {
        btnThemeToggle.setText(isDarkMode ? "☀" : "☾");
    }

    // -----------------------------------------------------------------------
    // Display helpers
    // -----------------------------------------------------------------------

    private void updateDisplay() {
        tvDisplay.setText(displayValue);
        tvExpression.setText(expressionText);
        updateThemeButton();
    }

    /**
     * Formats a double: strips trailing zeros after the decimal point so
     * whole numbers display cleanly (e.g. 10.0 → "10", 3.5 → "3.5").
     */
    private String formatNumber(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            return getString(R.string.error_divide_by_zero);
        }
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        // Limit to 10 significant digits
        String result = String.format("%.10f", value);
        // Remove trailing zeros
        result = result.replaceAll("0*$", "");
        result = result.replaceAll("\\.$", "");
        return result;
    }
}
